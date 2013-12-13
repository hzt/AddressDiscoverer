/**
 * Part of the AddressDiscoverer project, licensed under the GPL v.3 license.
 * This project provides intelligence for discovering email addresses in
 * specified web pages, associating them with a given institution and department
 * and address type.
 *
 * This project is licensed under the GPL v.3. Your rights to copy and modify
 * are regulated by the conditions specified in that license, available at
 * http://www.gnu.org/licenses/gpl-3.0.html
 */
package org.norvelle.addressdiscoverer;

import org.norvelle.addressdiscoverer.parse.IndividualExtractor;
import org.norvelle.addressdiscoverer.parse.EmailElementInTrFinder;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.support.ConnectionSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.Institution;
import org.norvelle.addressdiscoverer.model.KnownLastName;
import org.norvelle.addressdiscoverer.parse.EmailElementOutsideTrFinder;
import org.norvelle.utils.Utils;

/**
 * Provides routines useful in doing unit tests
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class TestUtilities {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    
    public static void setupLogger() {
        logger.setLevel(Level.INFO);
        System.setProperty(LocalLog.LOCAL_LOG_FILE_PROPERTY, System.getProperty("java.io.tmpdir") 
                + File.separator + "addressdiscoverer.ormlite.log");        
    }
    
    public static String getTestOutputDirectory() {
        String settingsDirname = System.getProperty("user.home") + File.separator + 
            ".addressdiscoverer";
        File settingsDir = new File(settingsDirname);
        if (! settingsDir.exists()) {
            settingsDir.mkdir();
        }    
        return settingsDir.getAbsolutePath();
    }

    public static ConnectionSource getDBConnection(String dbName) 
            throws SQLException, CannotLoadJDBCDriverException 
    {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new CannotLoadJDBCDriverException(ex.getMessage());
        }
        String dbFilePath = TestUtilities.getTestOutputDirectory() + File.separator + dbName;

        // create a database connection and initialize our tables.
        // All object persistence is managed via ORMLite.
        logger.log(Level.INFO, "Opening SQLite db at {0}", dbFilePath);
        ConnectionSource connectionSource;
        try {
            connectionSource =
                new JdbcConnectionSource("jdbc:sqlite:" + dbFilePath);
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, "Could not connect to database: {0}", ex.getMessage());
            throw ex;
        }
        Institution.initialize(connectionSource);
        Department.initialize(connectionSource);
        Individual.initialize(connectionSource);
        KnownLastName.initialize(connectionSource);
        
        return connectionSource;
    }

    public static List extractIndividuals(String htmlUri, String outputFile, String encoding) 
            throws IOException, SQLException, OrmObjectNotConfiguredException 
    {
        String html;
        html = Utils.loadStringFromResource(htmlUri, encoding);
        Document soup = Jsoup.parse(html);
        logger.log(Level.FINE, String.format("JSoup parsed document as follows:\n%s", soup.toString()));
        EmailElementInTrFinder finder = new EmailElementInTrFinder(soup);
        List<Element> rows = finder.getRows();
        logger.log(Level.FINE, String.format("EmailElementFinder found %d TR tags", rows.size()));
        IndividualExtractor ext = new IndividualExtractor(null, null);
        List<Individual> individuals = ext.parse(html);
        if (!outputFile.isEmpty())
            FileUtils.writeLines(new File(outputFile), rows);
        return individuals;
    }
    
    public static List extractIndividuals(String htmlUri, String outputFile) 
            throws IOException, SQLException, OrmObjectNotConfiguredException 
    {
        return extractIndividuals(htmlUri, outputFile, "UTF-8");
    }
}
