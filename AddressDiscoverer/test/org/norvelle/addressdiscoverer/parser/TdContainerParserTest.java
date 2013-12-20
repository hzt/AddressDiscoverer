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

import org.norvelle.addressdiscoverer.parse.parser.EachPartInTdParser;
import com.j256.ormlite.support.ConnectionSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.IndividualExtractionFailedException;
import org.norvelle.addressdiscoverer.exceptions.MultipleRecordsInTrException;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.UnparsableIndividual;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class TdContainerParserTest {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public TdContainerParserTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        TestUtilities.setupLogger();
        try {
            connection = TestUtilities.getDBConnection("addresses.test.sqlite");
        } catch (SQLException | CannotLoadJDBCDriverException |IOException ex) {
            fail("Encountered problems connecting to database: " + ex.getMessage());
            return;
        }
    }

    @Test
    public void testLfmugica() {
        String html;
        try {
            html = Utils.loadStringFromResource(
                    "/org/norvelle/addressdiscoverer/resources/lfmugica.html", "");
        } catch (IOException ex) {
            fail("Couldn't extract individuals due to IOException: " + ex.getMessage());
            return;
        }
        EachPartInTdParser parser = new EachPartInTdParser();
        Document soup = Jsoup.parse(html);
        Elements rows = soup.select("tr");
        Individual i;
        try {
            i = parser.getIndividual(rows.first(), null);
        } catch (CantParseIndividualException | SQLException ex) {
            fail("Problem with parsing: " + ex.getMessage());
            return;
        } catch (MultipleRecordsInTrException ex) {
            fail("File contains multiple records per TR, should only have one");
            return;
        }
        
        Assert.assertEquals("The individual's first name should be Fernando", 
                "Fernando", i.getFirstName());
        Assert.assertEquals("The individual's first name should be empty", 
                "", i.getTitle());
        Assert.assertEquals("The individual's last name should be Múgica", 
                "Múgica", i.getLastName());
        Assert.assertEquals("The individual's email should be lfmugica@unav.es", 
                "lfmugica@unav.es", i.getEmail());
    }
    
    @Test
    public void testLfmugicaExtract() {
        List<Individual> individuals;
        try {
            individuals = TestUtilities.extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/lfmugica.html",
                    TestUtilities.getTestOutputDirectory() + File.separator + "lfmugica.txt"
            );
        } catch (IOException | SQLException
                | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
            return;
        }
        Assert.assertEquals(
                String.format("There should be 1 individual, %d were found", individuals.size()), 1, individuals.size());
        for (Individual i: individuals) 
            Assert.assertFalse("There should be no NullIndividuals returned: " + i.toString(), 
                i.getClass().equals(UnparsableIndividual.class));
        Individual lfmugica = individuals.get(0);
        Assert.assertEquals("The individual's first name should be Fernando", 
                "Fernando", lfmugica.getFirstName());
        Assert.assertEquals("The individual's first name should be empty", 
                "", lfmugica.getTitle());
        Assert.assertEquals("The individual's last name should be Múgica", 
                "Múgica", lfmugica.getLastName());
        Assert.assertEquals("The individual's email should be lfmugica@unav.es", 
                "lfmugica@unav.es", lfmugica.getEmail());
        //Assert.assertEquals("The parser name should be TdContainerParser", 
        //        "TdContainerParser", lfmugica.getParserName());
    }
    
    
}
