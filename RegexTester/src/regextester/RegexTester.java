/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package regextester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.UIManager;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class RegexTester {


    // Property keys
    private static final String DUMMY_PROPERTY = "DummyProperty";
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    // Our "singleton" application instance
    private static RegexTester application;
    
    // Private variables for the application
    private final MainWindow window;
    private Properties props;
    private String propsFilename;
    private String settingsDirname;
    private Connection connection;
    
    public RegexTester() throws Exception {
        RegexTester.application = this;
        
        // First setup our logger
        this.checkSettingsDirExists();
        logger.setLevel(Level.SEVERE);
        FileHandler fh = new FileHandler(this.settingsDirname + File.separator + "debug.log", 
                8096, 1, true);  
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter); 
        logger.info("Starting RegexTester");
        
        // Load our properties and attach the database, creating it if it doesn't exist
        this.loadProperties();
        
        // Create our GUI
        UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
        window = new MainWindow(this);
        window.setLocationRelativeTo(null);
        window.setTitle("RegexTester");
        window.setVisible(true);
    }

    public void shutdown()  {
        try {
            this.saveProperties();
        } catch (IOException ex) {
            this.window.reportException("Could not store properties" + ex.getMessage());;
        }
        logger.info("Exiting RegexTester");
        System.exit(0);
    }

    // ===================== Private Methods =============================
    /**
     * Check to see if our settings directory exists, and create it if
     * it doesn't. The settings directory is always in the user's home
     * directory, with the name ".regextester".
     */
    private void checkSettingsDirExists() {
        this.settingsDirname = System.getProperty("user.home") + File.separator + 
            ".regextester";
        File settingsDir = new File(this.settingsDirname);
        if (! settingsDir.exists()) {
            settingsDir.mkdir();
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
            this.props.store(fos, "Properties for RegexTester");
            fos.flush();
        }
    }
        
    // ===================== Getters and setters =============================
    
    public static void reportException(Exception e) {
        logger.log(Level.SEVERE, e.getMessage());
        logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(e));
        RegexTester.application.window.reportException(e.getMessage());
    }
    
    // ===================== Getters and setters =============================
    public Properties getProps() {
        return RegexTester.application.props;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            RegexTester.application = new RegexTester();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(ex));
            System.exit(1);
        }
    }
    
    
}
