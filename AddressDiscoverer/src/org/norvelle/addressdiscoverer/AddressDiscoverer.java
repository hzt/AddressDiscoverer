/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.norvelle.addressdiscoverer;

import org.norvelle.utils.Utils;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.support.ConnectionSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.gui.MainWindow;
import org.norvelle.addressdiscoverer.model.Abbreviations;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.Institution;
import org.norvelle.addressdiscoverer.model.KnownFirstName;
import org.norvelle.addressdiscoverer.model.KnownLastName;
import org.norvelle.addressdiscoverer.model.KnownSpanishWord;

/**
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class AddressDiscoverer {

    // Property keys
    private static final String DUMMY_PROPERTY = "DummyProperty";
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    // Our "singleton" application instance
    public static AddressDiscoverer application;
    
    // Private variables for the application
    private final MainWindow window;
    private Properties props;
    private String propsFilename;
    private String settingsDirname;
    private Connection connection;
    
    public AddressDiscoverer() throws Exception {
        AddressDiscoverer.application = this;
        
        // First setup our logger. The ORMLite logger is prolix and useless,
        // so we redirect it to a temp file.
        System.setProperty(LocalLog.LOCAL_LOG_FILE_PROPERTY, System.getProperty("java.io.tmpdir") 
                + File.separator + "addressdiscoverer.ormlite.log");        
        this.checkSettingsDirExists();
        logger.setLevel(Level.SEVERE);
        FileHandler fh = new FileHandler(this.settingsDirname + File.separator + "debug.log", 
                8096, 1, true);  
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter); 
        logger.info("Starting AddressDiscoverer");
        
        // Load our properties and attach the database, creating it if it doesn't exist
        this.loadProperties();
        this.attachDatabase();
        KnownLastName.initialize(this.settingsDirname);
        KnownFirstName.initialize(this.settingsDirname);
        KnownSpanishWord.initialize(this.settingsDirname);
        Abbreviations.initialize(this.settingsDirname);
        
        // Create our GUI
        UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
        window = new MainWindow(this);
        window.setTitle("AddressDiscoverer"); 
        window.setExtendedState( window.getExtendedState() | java.awt.Frame.MAXIMIZED_BOTH );
        window.setVisible(true);
    }

    public void shutdown()  {
        try {
            this.saveProperties();
            KnownLastName.store();
            KnownFirstName.store();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    Utils.wordWrapString("Could not store properties or names files: " 
                            + ex.getMessage(), 60), 
                    "Data storage failure", JOptionPane.ERROR_MESSAGE);
        }
        logger.info("Exiting AddressDiscoverer");
        System.exit(0);
    }

    // ===================== Private Methods =============================
    /**
     * Check to see if our settings directory exists, and create it if
     * it doesn't. The settings directory is always in the user's home
     * directory, with the name ".addressdiscoverer".
     */
    private void checkSettingsDirExists() {
        this.settingsDirname = System.getProperty("user.home") + File.separator + 
            ".addressdiscoverer";
        File settingsDir = new File(this.settingsDirname);
        if (! settingsDir.exists()) {
            settingsDir.mkdir();
        }
    }
    
    private void attachDatabase() 
            throws CannotLoadJDBCDriverException, SQLException, IOException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Could not load SQLite JDBC driver: {0}", ex.getMessage());
            throw new CannotLoadJDBCDriverException(ex.getMessage());
        }

        // create a database connection and initialize our tables.
        // All object persistence is managed via ORMLite.
        String dbFilename = this.settingsDirname + File.separator + "addresses.sqlite";
        ConnectionSource connectionSource =
            new JdbcConnectionSource("jdbc:sqlite:" + dbFilename);
        Institution.initialize(connectionSource);
        Department.initialize(connectionSource);
        Individual.initialize(connectionSource);
    }
    
    private void loadProperties() {
        this.props = new Properties();
        try {
            this.propsFilename = this.settingsDirname + File.separator + "settings.props";
            this.props.load(new java.io.FileInputStream(this.propsFilename));
        }
        catch (IOException e) {
            this.props.setProperty(DUMMY_PROPERTY, "");
        }
    }
    
    private void saveProperties() throws FileNotFoundException, IOException {
        try (FileOutputStream fos = new FileOutputStream(this.propsFilename)) {
            this.props.store(fos, "Properties for AddressDiscoverer");
            fos.flush();
        }
    }
        
    public void statusChanged() {
        try {
            int numInstitutions = (int) Institution.getCount();
            int numDepartments = (int) Department.getCount();
            int numIndividuals = (int) Individual.getCount();
            String statusText = String.format(
                    "%d individuals in %d departments from %d institutions",
                    numIndividuals, numDepartments, numInstitutions);
            if (this.window != null)
                this.window.updateStatus(statusText);
        } catch (SQLException | OrmObjectNotConfiguredException ex) {
            reportException(ex);
        }
    }
    
    // ===================== Globally accessible methods =============================
    
    public static void reportException(Exception e) {
        logger.log(Level.SEVERE, e.getMessage());
        logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(e));
        AddressDiscoverer.application.window.reportException(e.getMessage());
    }
    
    // ===================== Getters and setters =============================
    public Properties getProps() {
        return props;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            AddressDiscoverer.application = new AddressDiscoverer();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(ex));
            System.exit(1);
        }
    }
    
}
