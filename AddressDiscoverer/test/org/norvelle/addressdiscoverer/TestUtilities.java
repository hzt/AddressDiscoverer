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
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.support.ConnectionSource;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.IndividualExtractionFailedException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.gui.StatusReporter;
import org.norvelle.addressdiscoverer.model.Abbreviations;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.GrammarParticles;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.Institution;
import org.norvelle.addressdiscoverer.model.KnownFirstName;
import org.norvelle.addressdiscoverer.model.KnownLastName;
import org.norvelle.addressdiscoverer.model.KnownSpanishWord;
import org.norvelle.addressdiscoverer.model.UnparsableIndividual;
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

    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public static ConnectionSource getDBConnection(String dbName) 
            throws SQLException, CannotLoadJDBCDriverException, IOException 
    {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new CannotLoadJDBCDriverException(ex.getMessage());
        }
        String outputDir = TestUtilities.getTestOutputDirectory();
        String dbFilePath = outputDir + File.separator + dbName;

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
        
        KnownLastName.initialize(outputDir);
        KnownFirstName.initialize(outputDir);
        KnownSpanishWord.initialize(outputDir);
        Abbreviations.initialize(outputDir);
        GrammarParticles.initialize(outputDir);
        
        return connectionSource;
    }

    public static List extractIndividuals(String htmlUri, String outputFile, String encoding) 
            throws IOException, SQLException, OrmObjectNotConfiguredException, 
            UnsupportedEncodingException, IndividualExtractionFailedException 
    {
        String html;
        html = Utils.loadStringFromResource(htmlUri, encoding);
        StatusReporter status = new StatusReporter(StatusReporter.ParsingStages.READING_FILE, null);
        IndividualExtractor ext = new IndividualExtractor(null, status);
        List<Individual> individuals = ext.parse(html, encoding);
        //if (!outputFile.isEmpty())
        //    FileUtils.writeLines(new File(outputFile), rows);
        return individuals;
    }
    
    public static List extractIndividuals(String htmlUri, String outputFile) 
            throws IOException, SQLException, OrmObjectNotConfiguredException, 
            UnsupportedEncodingException, IndividualExtractionFailedException 
    {
        return extractIndividuals(htmlUri, outputFile, "UTF-8");
    }

    public static void testOneTr(String email, String title, String first, String last, String rest) 
            throws IOException, SQLException, OrmObjectNotConfiguredException, 
            UnsupportedEncodingException, IndividualExtractionFailedException 
    {
        List<Individual> individuals;
        String[] emailParts = StringUtils.split(email, "@");
        String emailBase = emailParts[0];
        String outputFile = TestUtilities.getTestOutputDirectory() + File.separator + emailBase + ".txt";
        individuals = TestUtilities.extractIndividuals("/org/norvelle/addressdiscoverer/resources/" + emailBase + ".html", outputFile);
        Assert.assertEquals(String.format("There should be 1 individual, %d were found", individuals.size()), 1, individuals.size());
        for (Individual i : individuals) {
            Assert.assertFalse("There should be no NullIndividuals returned: " + i.toString(), i.getClass().equals(UnparsableIndividual.class));
        }
        Individual myIndividual = individuals.get(0);
        Assert.assertEquals("The individual's first name should be " + first, first, myIndividual.getFirstName());
        Assert.assertEquals(String.format("The individual's title should be '%s'", title), title, myIndividual.getTitle());
        Assert.assertEquals("The individual's last name should be " + last, last, myIndividual.getLastName());
        Assert.assertEquals("The individual's email should be " + email, email, myIndividual.getEmail());
        Assert.assertEquals(String.format("The remaining text should be '%s'", rest), rest, myIndividual.getUnprocessed());
    }
}
