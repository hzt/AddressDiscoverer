/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package namedatabasescraper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class NameDatabaseScraper {

    // Property keys
    private static final String DUMMY_PROPERTY = "DummyProperty";
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    // Our "singleton" application instance
    private static NameDatabaseScraper application;
    
    // Private variables for the application
    private final MainWindow window;
    private Properties props;
    private String propsFilename;
    private String settingsDirname;
    private Connection connection;
    
    public NameDatabaseScraper() throws Exception {
        NameDatabaseScraper.application = this;
        
        // First setup our logger
        this.checkSettingsDirExists();
        logger.setLevel(Level.SEVERE);
        FileHandler fh = new FileHandler(this.settingsDirname + File.separator + "debug.log", 
                8096, 1, true);  
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter); 
        logger.info("Starting NameDatabaseScraper");
        
        // Load our properties and attach the database, creating it if it doesn't exist
        this.loadProperties();
        this.attachDatabase();
        
        // Create our GUI
        UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
        window = new MainWindow(this);
        window.setLocationRelativeTo(null);
        window.setTitle("NameDatabaseScraper");
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
        logger.info("Exiting NameDatabaseScraper");
        System.exit(0);
    }

    // ===================== Private Methods =============================
    /**
     * Check to see if our settings directory exists, and create it if
     * it doesn't. The settings directory is always in the user's home
     * directory, with the name ".NameDatabaseScraper".
     */
    private void checkSettingsDirExists() {
        this.settingsDirname = System.getProperty("user.home") + File.separator + 
            ".NameDatabaseScraper";
        File settingsDir = new File(this.settingsDirname);
        if (! settingsDir.exists()) {
            settingsDir.mkdir();
        }
    }
    
    private void attachDatabase() 
            throws CannotLoadJDBCDriverException, SQLException, IOException, ErrorCreatingDatabaseException 
    {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Could not load SQLite JDBC driver: {0}", ex.getMessage());
            throw new CannotLoadJDBCDriverException();
        }
        Connection connection;
        String dbFilename = this.settingsDirname + File.separator + "names.sqlite";
        File dbFile = new File(dbFilename);
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilename);
        Statement statement = connection.createStatement();

        try {
            ResultSet rs = statement.executeQuery(
                    "SELECT name FROM names LIMIT 1");
            while (rs.next()) {
                //lastProgramRun = rs.getString("value");
            }
        } catch (SQLException e) {
            logger.log(Level.INFO, "Creating new name table");
            statement.execute("CREATE TABLE names (name TEXT)");
        } 
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
            this.props.store(fos, "Properties for NameDatabaseScraper");
            fos.flush();
        }
    }
        
    // ===================== Getters and setters =============================
    
    public static void reportException(Exception e) {
        logger.log(Level.SEVERE, e.getMessage());
        logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(e));
        NameDatabaseScraper.application.window.reportException(e.getMessage());
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
            NameDatabaseScraper.application = new NameDatabaseScraper();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(ex));
            System.exit(1);
        }
    }
    
}
