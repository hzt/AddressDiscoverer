/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.norvelle.addressdiscoverer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.ErrorCreatingDatabaseException;
import org.norvelle.addressdiscoverer.gui.MainWindow;

/**
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class AddressDiscoverer {

    // A logger instance
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    // Our "singleton" application instance
    private static AddressDiscoverer application;
    
    // Private variables for the application
    private final MainWindow window;
    private Properties props;
    private String propsFilename;
    private String settingsDirname;
    private Connection connection;
    
    public AddressDiscoverer() throws Exception {
        // First setup our logger
        this.checkSettingsDirExists();
        logger.setLevel(Level.INFO);
        FileHandler fh = new FileHandler(this.settingsDirname + File.separator + "debug.log", 
                8096, 1, true);  
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter); 
        logger.info("Starting AddressDiscoverer");
        
        // Load our properties and attach the database, creating it if it doesn't exist
        this.loadProperties();
        this.attachDatabase();
        
        // Create our GUI
        UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
        window = new MainWindow(this);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    public void shutdown()  {
        try {
            this.saveProperties();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    Utils.wordWrapString("Could not store properties: " + ex.getMessage(), 30), 
                    "Property storage failure", JOptionPane.ERROR_MESSAGE);
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
            throws CannotLoadJDBCDriverException, ErrorCreatingDatabaseException, SQLException, IOException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Could not load SQLite JDBC driver: {0}", ex.getMessage());
            throw new CannotLoadJDBCDriverException(ex.getMessage());
        }

        // create a database connection
        String dbFilename = this.settingsDirname + File.separator + "addresses.sqlite";
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilename);
        Statement statement = connection.createStatement();

        // See if our database is already configured.
        try {
            ResultSet rs = statement.executeQuery("SELECT * FROM config");
        } 
        // If not, run our database creation code.
        catch (SQLException e) {
            InputStream inputStream = getClass().getResourceAsStream("/org/norvelle/addressdiscoverer/resources/create_database.sql");
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, "UTF-8");
            String createSQL = writer.toString();
            String[] createCommands = StringUtils.split(createSQL, "===");
            for (String createCommand : createCommands) {
                statement.addBatch(StringUtils.trim(createCommand));
            }
            try {
                statement.executeBatch();
            }
            catch (SQLException sqle) {
                throw new ErrorCreatingDatabaseException(sqle.getMessage());
            }
        }
    }
    
    private void loadProperties() {
        this.props = new Properties();
        try {
            this.propsFilename = this.settingsDirname + File.separator + "settings.props";
            this.props.load(new java.io.FileInputStream(this.propsFilename));
        }
        catch (IOException e) {
            //this.props.setProperty(PROXY_FILE_LOCATION_PROPERTY, "");
        }
    }
    
    private void saveProperties() throws FileNotFoundException, IOException {
        try (FileOutputStream fos = new FileOutputStream(this.propsFilename)) {
            this.props.store(fos, "Properties for AddressDiscoverer");
            fos.flush();
        }
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
