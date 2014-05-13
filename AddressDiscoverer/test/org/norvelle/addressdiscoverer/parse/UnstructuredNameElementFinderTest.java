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

import com.j256.ormlite.support.ConnectionSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
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
import org.norvelle.addressdiscoverer.exceptions.DoesNotContainContactLinkException;
import org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException;
import org.norvelle.addressdiscoverer.exceptions.MultipleContactLinksOfSameTypeFoundException;
import org.norvelle.addressdiscoverer.parse.structured.ContactLink;
import org.norvelle.addressdiscoverer.parse.structured.ContactLinkLocator;
import org.norvelle.addressdiscoverer.parse.structured.StructuredNameElementFinder;
import org.norvelle.addressdiscoverer.parse.unstructured.UnstructuredNameElementFinder;
import org.norvelle.addressdiscoverer.parse.unstructured.UnstructuredPageNameElement;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class UnstructuredNameElementFinderTest {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    public UnstructuredNameElementFinderTest() {
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
    public void testBackwardsIteratorOnMultipleTdsWithRecordsPerTrIndividual() {
        String htmlUri = "/org/norvelle/addressdiscoverer/resources/individuals/InfoInSuccessiveTablesNoDivisions_UNAM.html";
        String html;
        try {
            html = Utils.loadStringFromResource(htmlUri, "UTF-8");
        } catch (IOException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
            return;
        }
        
        INameElementFinder finder;
        Document soup = Jsoup.parse(html);
        try { 
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(
                    ClassificationStages.CREATING_ITERATOR, null);
            //iterator = new BackwardsFlattenedDocumentIterator(soup, "iso-8859-1", status);
            finder = new UnstructuredNameElementFinder(soup, "UTF-8", status);
            Assert.assertTrue("The finder should have found some names", finder.getNumberOfNames() != 0);
            Assert.assertEquals("The finder should have found 1 names", 1, finder.getNumberOfNames());

            // Check we have the correct name found
           /* List<INameElement> nameElements = finder.getNameElements();
            UnstructuredPageNameElement adeval = (UnstructuredPageNameElement) nameElements.get(0);
            Assert.assertEquals("The name element should have 13 intermediate elements", 
                    13, adeval.getIntermediateElements().size());

            // Check to name sure the contact link is correct.
            ContactLink cl = adeval.getContactLink();
            ContactLinkLocator.baseUrl = "http://www.directorio.unam.mx/consultasvarias.htm";
            String emailAddress = cl.getAddress();
            Assert.assertEquals("pedros@unam.mx", emailAddress); */
        } catch (UnsupportedEncodingException | EndNodeWalkingException ex) {
            fail("Encountered problems iterating over document: " + ex.getMessage());
        /*} catch (MultipleContactLinksOfSameTypeFoundException ex) {
            fail("Found too many contact links");
        } catch (DoesNotContainContactLinkException ex) {
            fail("No contact link was found"); */
        }
    }
    
    //@Test
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void testBackwardsIteratorOnMultipleTdsWithRecordsPerTrFullpage() {
        String htmlUri = "/org/norvelle/addressdiscoverer/resources/fullpages/InfoInSuccessiveTablesNoDivisions_UNAM.html";
        String html;
        try {
            html = Utils.loadStringFromResource(htmlUri, "UTF-8");
        } catch (IOException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
            return;
        }
        
        Document soup = Jsoup.parse(html);
        try { 
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(ClassificationStages.CREATING_ITERATOR, null);
            //iterator = new BackwardsFlattenedDocumentIterator(soup, "iso-8859-1", status);
            StructuredNameElementFinder finder = new StructuredNameElementFinder(soup, "UTF-8", status);
            Assert.assertTrue("The finder should have found some names", finder.getNumberOfNames() != 0);
            Assert.assertEquals("The finder should have found 80 names", 80, finder.getNumberOfNames());
        } catch (UnsupportedEncodingException | EndNodeWalkingException ex) {
            fail("Encountered problems iterating over document: " + ex.getMessage());
        }
    }
    
}
