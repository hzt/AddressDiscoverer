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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.norvelle.addressdiscoverer.Constants;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.gui.threading.ExtractIndividualsStatusReporter;
import org.norvelle.addressdiscoverer.gui.threading.ExtractIndividualsStatusReporter.ClassificationStages;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.DoesNotContainContactLinkException;
import org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException;
import org.norvelle.addressdiscoverer.exceptions.MultipleContactLinksOfSameTypeFoundException;
import static org.norvelle.addressdiscoverer.parse.ContactLink.emailPattern;
import org.norvelle.addressdiscoverer.parse.structured.StructuredPageContactLinkLocator;
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
    protected static final Pattern emailPattern = Pattern.compile(Constants.emailRegex);

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

    //@Test
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void testBackwardsIteratorOnMultipleTdsWithRecordsPerTrIndividual() {
        String htmlUri = "/org/norvelle/addressdiscoverer/resources/individuals/InfoInSuccessiveTablesNoDivisions_UNAM.html";
        String html;
        try {
            html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
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
            finder = new UnstructuredNameElementFinder(soup, "iso-8859-1", status);
            Assert.assertTrue("The finder should have found some names", finder.getNumberOfNames() != 0);
            Assert.assertEquals("The finder should have found 1 names", 1, finder.getNumberOfNames());

            // Check we have the correct name found
            List<INameElement> nameElements = finder.getNameElements();
            UnstructuredPageNameElement adeval = (UnstructuredPageNameElement) nameElements.get(0);
            Assert.assertEquals("The name element should have 11 intermediate elements", 
                    11, adeval.getIntermediateValues().size());
 
            // Check to name sure the contact link is correct.
            ContactLink cl = adeval.getContactLink();
            ContactLinkLocator.baseUrl = "http://www.directorio.unam.mx/consultasvarias.htm";
            String emailAddress = cl.getAddress();
            Assert.assertEquals("pedros@unam.mx", emailAddress); 
        } catch (UnsupportedEncodingException | EndNodeWalkingException ex) {
            fail("Encountered problems iterating over document: " + ex.getMessage());
        } catch (MultipleContactLinksOfSameTypeFoundException ex) {
            fail("Found too many contact links");
        } catch (DoesNotContainContactLinkException ex) {
            fail("No contact link was found"); 
        }
    }
    
    @Test
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void testUnstructuredNameElementFinder() {
        String htmlUri = "/org/norvelle/addressdiscoverer/resources/fullpages/InfoInSuccessiveTablesNoDivisions_UNAM.html";
        String html;
        try {
            html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
        } catch (IOException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
            return;
        }
        
        Document soup = Jsoup.parse(html);
        int numWithoutEmails = 0;
        try { 
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(ClassificationStages.CREATING_ITERATOR, null);
            //iterator = new BackwardsFlattenedDocumentIterator(soup, "iso-8859-1", status);
            UnstructuredNameElementFinder finder = new UnstructuredNameElementFinder(soup, "iso-8859-1", status);
            Assert.assertTrue("The finder should have found some names", finder.getNumberOfNames() != 0);
            Assert.assertEquals("The finder should have found 7 names", 7, finder.getNumberOfNames());

            // Check to make sure we have enough NameElements with email addresses
            List<INameElement> nameElements = finder.getNameElements();
            for (INameElement nm : nameElements) {
                try {
                    // Check to name sure the contact link is correct.
                    ContactLink cl = nm.getContactLink();
                    ContactLinkLocator.baseUrl = "http://www.directorio.unam.mx/consultasvarias.htm";
                    String emailAddress = cl.getAddress();
                    Matcher emailMatcher = emailPattern.matcher(emailAddress);
                    boolean matchFound = emailMatcher.matches();
                    Assert.assertEquals("There should be an email address, but found: " 
                            + emailAddress, true, matchFound); 
                }
                catch (MultipleContactLinksOfSameTypeFoundException ex) {
                    fail("Found too many contact links");
                } catch (DoesNotContainContactLinkException ex) {
                    numWithoutEmails ++; 
                }
            } // for (INameElement...
        } catch (UnsupportedEncodingException | EndNodeWalkingException ex) {
            fail("Encountered problems iterating over document: " + ex.getMessage());
        } 
        Assert.assertEquals("There should only be 0 names without emails", 0, numWithoutEmails);
    }
    
}
