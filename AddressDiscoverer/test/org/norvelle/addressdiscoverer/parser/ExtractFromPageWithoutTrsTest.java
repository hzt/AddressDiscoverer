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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import org.norvelle.addressdiscoverer.parse.EmailElementOutsideTrFinder;
import org.norvelle.addressdiscoverer.parse.parser.Parser;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ExtractFromPageWithoutTrsTest {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public ExtractFromPageWithoutTrsTest() {
    }

    @BeforeClass
    @SuppressWarnings("UnnecessaryReturnStatement")
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
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void testDerecho() {
        List<Individual> individuals = new ArrayList<>();
        String html;
        try {
            html = Utils.loadStringFromResource(
                    "/org/norvelle/addressdiscoverer/resources/navarra_derecho.html", 
                    "UTF-8");
        } catch (IOException ex) {
            fail("Couldn't extract individuals due to IOException: " + ex.getMessage());
            return;
        }
        Document soup = Jsoup.parse(html);
        EmailElementOutsideTrFinder finder = new EmailElementOutsideTrFinder(soup, "UTF-8");
        List<Element> trs;
        try {
            trs = finder.getRows();
        } catch (SQLException | UnsupportedEncodingException ex) {
            fail("Couldn't use EmailElementOutsideTrFinder due to exception: " + ex.getMessage());
            return;            
        }
        Assert.assertEquals(String.format("There should be 57 TRs found, but %d were returned", 57),
                57, trs.size());
        
        for (Element tr : trs) {
            try {
                individuals.add(Parser.getBestIndividual(tr, null));
            } catch (SQLException | MultipleRecordsInTrException ex) {
                logger.log(Level.SEVERE, null, ex);
                logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(ex));
                fail("Problem parsing TRs: " + ex.getMessage());
                return;
            } catch (CantParseIndividualException ex) {
                individuals.add(new UnparsableIndividual(tr.text()));
            }
        }
        
        StringBuilder individualLog = new StringBuilder();
        int numNulls = 0;
        for (Individual i: individuals) {
            individualLog.append(i.toString()).append("\n");
            if (i.getClass().equals(UnparsableIndividual.class))
                numNulls ++;
        }
        logger.log(Level.INFO, "Found following individuals:\n{0}", individualLog.toString());
        logger.log(Level.INFO, String.format("%d NullIndividuals were found", numNulls));
        Assert.assertEquals(
                String.format("There should be 57 individuals, %d were found", individuals.size()), 
                57, individuals.size() - numNulls); 
    }
    
    @Test
    public void testDerecho2() {
        List<Individual> individuals;
        try {
            individuals = TestUtilities.extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/navarra_derecho.html",
                    TestUtilities.getTestOutputDirectory() + File.separator + "navarra_derecho.txt"
            );
        } catch (IOException | SQLException
                | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
            return;
        }
        
        Assert.assertEquals(
                String.format("There should be 57 individuals, %d were found", individuals.size()), 
                57, individuals.size());
    }
    

    
}
