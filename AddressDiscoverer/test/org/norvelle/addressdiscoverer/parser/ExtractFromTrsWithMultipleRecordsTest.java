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
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.NullIndividual;
import org.norvelle.addressdiscoverer.parse.parser.EntireRecordInTdParser;
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
        } catch (SQLException | CannotLoadJDBCDriverException ex) {
            fail("Encountered problems connecting to database: " + ex.getMessage());
            return;
        }
    }

    @Test
    public void testHistoria() {
        List<Individual> individuals = new ArrayList<Individual>();
        String html;
        try {
            html = Utils.loadStringFromResource("/org/norvelle/addressdiscoverer/resources/navarra_historia.html", 
                    "iso-8859-1");
        } catch (IOException ex) {
            fail("Couldn't extract individuals due to IOException: " + ex.getMessage());
            return;
        }
        Document soup = Jsoup.parse(html);
        Elements trs = soup.select("tr");
        EntireRecordInTdParser parser = new EntireRecordInTdParser();
        for (Element tr : trs) {
            try {
                individuals.addAll(parser.getMultipleIndividuals(tr, null));
            }
            catch (CantParseIndividualException cx) {
                individuals.add(new NullIndividual(tr.text()));
            } catch (SQLException | 
                    OrmObjectNotConfiguredException ex) {
                logger.log(Level.SEVERE, null, ex);
                logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(ex));
                fail("Problem parsing TRs: " + ex.getMessage());
                return;
            }
        }
        
        Assert.assertEquals(
                String.format("There should be 68 individuals, %d were found", individuals.size()), 
                68, individuals.size());
        for (Individual i: individuals) 
            Assert.assertFalse("There should be no NullIndividuals returned: " + i.toString(), 
                i.getClass().equals(NullIndividual.class));
    }
    

    
}
