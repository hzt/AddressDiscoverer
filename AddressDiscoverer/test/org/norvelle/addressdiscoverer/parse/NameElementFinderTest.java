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
package org.norvelle.addressdiscoverer.parse;

import org.norvelle.addressdiscoverer.parse.NameElementFinder;
import com.j256.ormlite.support.ConnectionSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.gui.threading.ExtractIndividualsStatusReporter;
import org.norvelle.addressdiscoverer.gui.threading.ExtractIndividualsStatusReporter.ClassificationStages;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class NameElementFinderTest {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    public NameElementFinderTest() {
    }

    @BeforeClass
    @SuppressWarnings("UnnecessaryReturnStatement")
    public static void setUpClass() {
        TestUtilities.setupLogger();
        try {
            ConnectionSource connection = TestUtilities.getDBConnection("addresses.test.sqlite");
        } catch (SQLException | CannotLoadJDBCDriverException |IOException ex) {
            fail("Encountered problems connecting to database: " + ex.getMessage());
            return;
        }
    }

    @Test
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void testBackwardsIteratorOnMultipleTdsWithRecordsPerTr() {
        String htmlUri = "/org/norvelle/addressdiscoverer/resources/MultipleTdsWithRecordsPerTr.html";
        String html;
        try {
            html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
        } catch (IOException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
            return;
        }
        
        Document soup = Jsoup.parse(html);
        try { 
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(ClassificationStages.CREATING_ITERATOR, null);
            //iterator = new BackwardsFlattenedDocumentIterator(soup, "iso-8859-1", status);
            NameElementFinder finder = new NameElementFinder(soup, "iso-8859-1", status);
            Assert.assertTrue("The finder should have found some names", finder.getNumberOfNames() != 0);
            Assert.assertEquals("The finder should have found 40 names", 40, finder.getNumberOfNames());
        } catch (UnsupportedEncodingException | EndNodeWalkingException ex) {
            fail("Encountered problems iterating over document: " + ex.getMessage());
        }
    }
    
}
