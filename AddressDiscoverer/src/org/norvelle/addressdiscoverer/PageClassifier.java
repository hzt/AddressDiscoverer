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
import org.norvelle.addressdiscoverer.classifier.PageClassifierGUI;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.model.Abbreviations;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.GenderDeterminer;
import org.norvelle.addressdiscoverer.model.GrammarParticles;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.Institution;
import org.norvelle.addressdiscoverer.model.KnownFirstName;
import org.norvelle.addressdiscoverer.model.KnownLastName;
import org.norvelle.addressdiscoverer.model.KnownSpanishWord;

/**
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class PageClassifier {

    // Property keys
    private static final String DUMMY_PROPERTY = "DummyProperty";
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    // Our "singleton" application instance
    public static PageClassifier application;
    
    // Private variables for the application
    private final PageClassifierGUI window;
    private Properties props;
    private String propsFilename;
    private String settingsDirname;
    private Connection connection;
    private String jdbcUrl;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public PageClassifier() throws Exception {
        PageClassifier.application = this;
        
        // First setup our logger. The ORMLite logger is prolix and useless,
        // so we redirect it to a temp file.
        System.setProperty(LocalLog.LOCAL_LOG_FILE_PROPERTY, System.getProperty("java.io.tmpdir") 
                + File.separator + "pageclassifier.ormlite.log");        
        this.checkSettingsDirExists();
        logger.setLevel(Level.SEVERE);
        FileHandler fh = new FileHandler(this.settingsDirname + File.separator + "pageclassifier.log", 
                8096, 1, true);  
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter); 
        logger.info("Starting PageClassifier");
        
        // Load our properties and attach the database, creating it if it doesn't exist
        this.loadProperties();
        this.attachDatabase();
        KnownLastName.initialize(this.settingsDirname);
        KnownFirstName.initialize(this.settingsDirname);
        KnownSpanishWord.initialize(this.settingsDirname);
        Abbreviations.initialize(this.settingsDirname);
        GrammarParticles.initialize(this.settingsDirname);
        GenderDeterminer.initialize(this.settingsDirname);
        
        // Create our GUI
        UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
        window = new PageClassifierGUI(this);
        window.setTitle("PageClassifier"); 
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
        logger.info("Exiting PageClassifier");
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
            throws CannotLoadJDBCDriverException, SQLException, IOException 
    {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Could not load SQLite JDBC driver: {0}", ex.getMessage());
            throw new CannotLoadJDBCDriverException(ex.getMessage());
        }

        // create a database connection and initialize our tables.
        // All object persistence is managed via ORMLite.
        String dbFilename = this.settingsDirname + File.separator + "addresses.sqlite";
        this.jdbcUrl = "jdbc:sqlite:" + dbFilename;
        ConnectionSource connectionSource =
            new JdbcConnectionSource(this.jdbcUrl);
        Institution.initialize(connectionSource);
        Department.initialize(connectionSource);
        Individual.initialize(connectionSource);
    }
    
    private void loadProperties() {
        this.props = new Properties();
        try {
            this.propsFilename = this.settingsDirname + File.separator + "pageclassifier.props";
            this.props.load(new java.io.FileInputStream(this.propsFilename));
        }
        catch (IOException e) {
            this.props.setProperty(DUMMY_PROPERTY, "");
        }
    }
    
    private void saveProperties() throws FileNotFoundException, IOException {
        try (FileOutputStream fos = new FileOutputStream(this.propsFilename)) {
            this.props.store(fos, "Properties for PageClassifier");
            fos.flush();
        }
    }
        
    // ===================== Globally accessible methods =============================
    
    public static void reportException(Exception e) {
        logger.log(Level.SEVERE, e.getMessage());
        logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(e));
        JOptionPane.showMessageDialog(null,
            Utils.wordWrapString(e.getMessage(), 50),
            "Program error", JOptionPane.ERROR_MESSAGE);
    }
    
    // ===================== Getters and setters =============================
    public Properties getProps() {
        return props;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getSettingsDirname() {
        return settingsDirname;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            PageClassifier.application = new PageClassifier();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(ex));
            System.exit(1);
        }
    }
    
}
