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

import org.norvelle.addressdiscoverer.IndividualExtractor;
import org.norvelle.addressdiscoverer.EmailElementFinder;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.support.ConnectionSource;
import java.io.File;
import java.io.IOException;
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
import org.norvelle.utils.Utils;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.NullIndividual;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class IndividualExtractorTest {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public IndividualExtractorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        TestUtilities.setupLogger();
        try {
            connection = TestUtilities.getDBConnection("addresses.sqlite");
        } catch (SQLException | CannotLoadJDBCDriverException ex) {
            fail("Encountered problems connecting to database: " + ex.getMessage());
            return;
        }
    }
    
    private static List<Individual> extractIndividuals(String htmlUri, String outputFile) {
        String html;
        try {
            html = Utils.loadStringFromResource(htmlUri);
        } catch (IOException ex) {
            fail("Encountered IOException: " + ex.getMessage());
            return null;
        }
        Document soup = Jsoup.parse(html);
        logger.log(Level.FINE, String.format("JSoup parsed document as follows:\n" + soup.toString() ));
        EmailElementFinder finder = new EmailElementFinder(soup);
        List<Element> rows = finder.getRows();
        logger.log(Level.FINE, String.format("EmailElementFinder found %d TR tags", rows.size()));

        // Instantiate an AddressExtractor see how many addresses we get.
        IndividualExtractor ext = new IndividualExtractor(null);
        ext.setHtml(html);
        List<Individual> individuals = ext.getIndividuals();
        try {
            FileUtils.writeLines(new File(outputFile), rows);
        } catch (IOException ex) {
            fail("Encountered IOException when writing individuals: " + ex.getMessage());
        }
        return individuals;
    }

    @Test
    public void testCmpasamar() {
        List<Individual> individuals = extractIndividuals(
            "/org/norvelle/addressdiscoverer/resources/cmpasamar.html",
            TestUtilities.getTestOutputDirectory() + File.separator + "cmpasamar.txt"
        );
        Assert.assertEquals(
                String.format("There should be 1 individual, %d were found", individuals.size()), 
                individuals.size(), 1);
        for (Individual i: individuals) 
            Assert.assertFalse("There should be no NullIndividuals returned: " + i.toString(), 
                i.getClass().equals(NullIndividual.class));
        Individual cmpasamar = individuals.get(0);
        Assert.assertEquals("The individual's first name should be Dra. Concepción", 
                "Dra. Concepción", cmpasamar.getFirstName());
        Assert.assertEquals("The individual's last name should be Martínez Pasamar", 
                "Martínez Pasamar", cmpasamar.getLastName());
        Assert.assertEquals("The individual's email should be cmpasamar@unav.es", 
                "cmpasamar@unav.es", cmpasamar.getEmail());
    }
    
    //@Test
    public void testThreeColumnRecords() {
        List<Individual> individuals = extractIndividuals(
            "/org/norvelle/addressdiscoverer/resources/ThreeFieldsAcrossNames.html",
            TestUtilities.getTestOutputDirectory() + File.separator + "individuals.txt"
        );
        Assert.assertEquals(
                String.format("There should be 3 individuals, %d were found", individuals.size()), 
                individuals.size() == 3);
        for (Individual i: individuals) 
            Assert.assertFalse("There should be no NullIndividuals returned: " + i.toString(), 
                i.getClass().equals(NullIndividual.class));
    }
    
}
