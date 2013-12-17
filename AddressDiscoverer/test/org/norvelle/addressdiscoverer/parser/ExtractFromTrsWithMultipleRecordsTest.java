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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.IndividualExtractionFailedException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.UnparsableIndividual;
import org.norvelle.addressdiscoverer.parse.parser.MultipleRecordsInOneTrParser;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ExtractFromTrsWithMultipleRecordsTest {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public ExtractFromTrsWithMultipleRecordsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        TestUtilities.setupLogger();
        try {
            connection = TestUtilities.getDBConnection("addresses.test.sqlite");
        } catch (SQLException | CannotLoadJDBCDriverException | IOException ex) {
            fail("Encountered problems connecting to database: " + ex.getMessage());
            return;
        }
    }

    @Test
    public void testHistoria() {
        List<Individual> individuals = new ArrayList<Individual>();
        String html;
        try {
            html = Utils.loadStringFromResource(
                    "/org/norvelle/addressdiscoverer/resources/navarra_historia.html", 
                    "iso-8859-1");
        } catch (IOException ex) {
            fail("Couldn't extract individuals due to IOException: " + ex.getMessage());
            return;
        }
        Document soup = Jsoup.parse(html);
        Elements trs = soup.select("tr");
        MultipleRecordsInOneTrParser parser = new MultipleRecordsInOneTrParser();
        for (Element tr : trs) {
            try {
                individuals.addAll(parser.getMultipleIndividuals(tr, null));
            } catch (SQLException | 
                    OrmObjectNotConfiguredException ex) {
                logger.log(Level.SEVERE, null, ex);
                logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(ex));
                fail("Problem parsing TRs: " + ex.getMessage());
                return;
            }
        }
        
        StringBuilder individualLog = new StringBuilder();
        int numNulls = 0;
        for (Individual i: individuals) {
            individualLog.append(i.toString()).append("\n");
            if (i.getClass().equals(UnparsableIndividual.class))
                numNulls ++;
        }
        logger.log(Level.INFO, "Found following individuals:\n" + individualLog.toString());
        logger.log(Level.INFO, String.format("%d NullIndividuals were found", numNulls));
        Assert.assertEquals(
                String.format("There should be 34 individuals, %d were found", individuals.size()), 
                34, individuals.size() - numNulls);
    }
    
    @Test
    public void testHistoria2() {
        List<Individual> individuals;
        try {
            individuals = TestUtilities.extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/navarra_historia.html",
                    TestUtilities.getTestOutputDirectory() + File.separator + "navarra_historia.txt"
            );
        } catch (IOException | SQLException | OrmObjectNotConfiguredException
                | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
            return;
        }
        
        Assert.assertEquals(
                String.format("There should be 36 individuals, %d were found", individuals.size()), 
                36, individuals.size());
    }
    

    
}
