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
package org.norvelle.addressdiscoverer.parser;

import com.j256.ormlite.support.ConnectionSource;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.IndividualExtractionFailedException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.gui.StatusReporter;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.UnparsableIndividual;
import org.norvelle.addressdiscoverer.parse.EmailElementViaLinksFinder;
import org.norvelle.addressdiscoverer.parse.IndividualExtractor;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ExtractFromFilesTest {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public ExtractFromFilesTest() {
    }

    @BeforeClass
    @SuppressWarnings("UnnecessaryReturnStatement")
    public static void setUpClass() {
        TestUtilities.setupLogger();
        try {
            connection = TestUtilities.getDBConnection("addresses.test.sqlite");
        } catch (SQLException | CannotLoadJDBCDriverException |IOException ex) {
            fail("Encountered problems connecting to database: " + ex.getMessage());
            return;
        }
    }

    public static List extractIndividuals(String htmlUri, String outputFile, String encoding) 
            throws IOException, SQLException, OrmObjectNotConfiguredException, 
            UnsupportedEncodingException, IndividualExtractionFailedException 
    {
        String html;
        html = Utils.loadStringFromResource(htmlUri, encoding);
        Document soup = Jsoup.parse(html);
        logger.log(Level.FINE, String.format("JSoup parsed document as follows:\n%s", soup.toString()));
        EmailElementViaLinksFinder finder = new EmailElementViaLinksFinder(soup);
        List<Element> rows = finder.getRows();
        logger.log(Level.FINE, String.format("EmailElementFinder found %d TR tags", rows.size()));
        StatusReporter status = new StatusReporter(StatusReporter.ParsingStages.READING_FILE, null);
        IndividualExtractor ext = new IndividualExtractor(null, status);
        List<Individual> individuals = ext.parse(html, encoding);
        if (!outputFile.isEmpty())
            FileUtils.writeLines(new File(outputFile), rows);
        return individuals;
    }
    
    @Test
    public void testFilosofia() {
        List<Individual> individuals;
        try {
            individuals = TestUtilities.extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/navarra_filosofia.html",
                    TestUtilities.getTestOutputDirectory() + File.separator + "filosofia.txt"
            );
        } catch (IOException | SQLException | OrmObjectNotConfiguredException
                | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
            return;
        }
        Assert.assertEquals(
                String.format("There should be 48 individuals, %d were found", individuals.size()), 
                48, individuals.size());
        int numNulls = 0;
        for (Individual i: individuals) {
            if (i.getClass().equals(UnparsableIndividual.class))
                numNulls ++;
        }
        logger.log(Level.INFO, String.format("%d NullIndividuals were found", numNulls));
        int percentageOfNulls = numNulls / individuals.size();
        Assert.assertTrue(String.format("Percentage of nulls is too high: %d", percentageOfNulls), 
                percentageOfNulls < 5);
    }
    
    @Test
    public void testFilologia() {
        List<Individual> individuals;
        String logfile = TestUtilities.getTestOutputDirectory() + File.separator + "filologia.txt";
        try {
            individuals = extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/navarra_philology.html",
                    logfile, "windows-1252"
            );
        } catch (IOException | SQLException | OrmObjectNotConfiguredException 
                | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
            return;
        }
        Assert.assertEquals(
                String.format("There should be 46 individuals, %d were found", individuals.size()), 
                46, individuals.size());
        int numNulls = 0;
        for (Individual i: individuals) {
            if (i.getClass().equals(UnparsableIndividual.class))
                numNulls ++;
        }
        logger.log(Level.INFO, String.format("%d NullIndividuals were found", numNulls));
        int percentageOfNulls = numNulls / individuals.size();
        Assert.assertTrue(String.format("Percentage of nulls is too high: %d", percentageOfNulls), 
                percentageOfNulls < 2);
        /*for (Individual i: individuals) 
            Assert.assertFalse("There should be no NullIndividuals, returned: " + i.toString(), 
                i.getClass().equals(NullIndividual.class));*/
    }
    
    @Test
    public void testEducacion() {
        List<Individual> individuals;
        try {
            individuals = TestUtilities.extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/navarra_educacion.html",
                    TestUtilities.getTestOutputDirectory() + File.separator + "educacion.txt",
                    "iso-8859-1"
            );
        } catch (IOException | SQLException | OrmObjectNotConfiguredException 
                | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
            return;
        }
        Assert.assertEquals(
                String.format("There should be 55 individuals, %d were found", individuals.size()), 
                55, individuals.size());
        int numNulls = 0;
        for (Individual i: individuals) {
            if (i.getClass().equals(UnparsableIndividual.class))
                numNulls ++;
        }
        logger.log(Level.INFO, String.format("%d NullIndividuals were found", numNulls));
        int percentageOfNulls = numNulls / individuals.size();
        Assert.assertTrue(String.format("Percentage of nulls is too high: %d", percentageOfNulls), 
                percentageOfNulls < 2);
        for (Individual i: individuals) {
            Assert.assertFalse(
                    String.format(
                            "There should not be any first names with punctuation in them: %s", 
                            i.getFirstName()), 
                i.getFirstName().matches(".*(\\p{P}|\\p{S}).*"));
            Assert.assertTrue(
                String.format(
                    "All first names should begin with a letter, but found '%s'", 
                    i.getFirstName().charAt(0)),
                    i.getFirstName().matches("^\\p{L}.*"));
        }
    }
    
    @Test
    public void testArquitectura() {
        List<Individual> individuals;
        String outfile = TestUtilities.getTestOutputDirectory() + File.separator + "arquitectura.txt";
        try {
            individuals = extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/navarra_arquitectura.html",
                    outfile, "windows-1252"
            );
        } catch (IOException | SQLException | OrmObjectNotConfiguredException 
                | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
            return;
        }
        Assert.assertEquals(
                String.format("There should be 86 individuals, %d were found", individuals.size()), 
                86, individuals.size());
        int numNulls = 0;
        for (Individual i: individuals) {
            if (i.getClass().equals(UnparsableIndividual.class))
                numNulls ++;
        }
        logger.log(Level.INFO, String.format("%d NullIndividuals were found", numNulls));
        int percentageOfNulls = numNulls / individuals.size();
        Assert.assertTrue(String.format("Percentage of nulls is too high: %d", percentageOfNulls), 
                percentageOfNulls < 2);
        /*for (Individual i: individuals) 
            Assert.assertFalse("There should be no NullIndividuals, returned: " + i.toString(), 
                i.getClass().equals(NullIndividual.class));*/
    }
    
}
