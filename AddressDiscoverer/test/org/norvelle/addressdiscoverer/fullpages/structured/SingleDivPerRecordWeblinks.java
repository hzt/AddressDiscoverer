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
package org.norvelle.addressdiscoverer.fullpages.structured;

import com.j256.ormlite.support.ConnectionSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.gui.threading.ExtractIndividualsStatusReporter;
import org.norvelle.addressdiscoverer.classifier.IProgressConsumer;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.DoesNotContainContactLinkException;
import org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException;
import org.norvelle.addressdiscoverer.exceptions.MultipleContactLinksOfSameTypeFoundException;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.Institution;
import org.norvelle.addressdiscoverer.model.Name;
import org.norvelle.addressdiscoverer.parse.INameElement;
import org.norvelle.addressdiscoverer.parse.structured.ContactLink;
import org.norvelle.addressdiscoverer.parse.structured.ContactLinkLocator;
import org.norvelle.addressdiscoverer.parse.INameElementFinder;
import org.norvelle.addressdiscoverer.parse.structured.StructuredNameElementFinder;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class SingleDivPerRecordWeblinks implements IProgressConsumer {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;
    private static Document soup;
    private ExtractIndividualsStatusReporter status;
    private INameElementFinder nameElementFinder;
    private static Department department;

    public SingleDivPerRecordWeblinks() {
    }

    @Override
    public void reportProgressStage(ExtractIndividualsStatusReporter progress) {
        //System.out.println(progress.toString());
    }
    
    @Override
    public void reportText(String text) {
        //System.out.println(text);
    }

    @BeforeClass
    @SuppressWarnings("UnnecessaryReturnStatement")
    public static void setUpClass() {
        TestUtilities.setupLogger();
        try {            
            // Set up a fresh database with dummy objects
            TestUtilities.deleteDatabase("addresses.test.sqlite");
            connection = TestUtilities.getDBConnection("addresses.test.sqlite");
            Institution i = Institution.create("Dummy institution");
            department = Department.create("Dummy department", i);
            
            // Fetch the page we'll be working on
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/fullpages/SingleDivPerRecordWeblinks.html";
            String html = Utils.loadStringFromResource(htmlUri, "UTF-8");
            soup = Jsoup.parse(html);
        } catch (SQLException | CannotLoadJDBCDriverException |IOException ex) {
            fail("Encountered problems connecting to database: " + ex.getMessage());
            return;
        }
    }

    @Before
    public void setUp() {
        try {
            status = new ExtractIndividualsStatusReporter(
                    ExtractIndividualsStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            nameElementFinder = new StructuredNameElementFinder(soup, "UTF-8", status);
        } catch (UnsupportedEncodingException | EndNodeWalkingException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        }
    }    
    
    @Test
    public void testCreateIndividuals() {
        ContactLinkLocator.baseUrl = "http://departamentos.uca.es/";
        int emailsNotFound = 0;
        int multipleEmailsFound = 0;
        int singleEmailsFound = 0;
        try {            
            List<INameElement> nameElements = nameElementFinder.getNameElements();
            for (INameElement ne : nameElements) {
                // First, see if we can parse the name; if not, we skip this name
                Name nm;
                try {
                    nm = ne.getName();
                }
                catch (CantParseIndividualException e) {
                    continue;
                }

                String email;
                try {
                    ContactLink cl = ne.getContactLink();
                    email = cl.getAddress();
                    singleEmailsFound ++;
                }
                catch (DoesNotContainContactLinkException ex) {
                    email = "Not found";
                    emailsNotFound ++;
                }
                catch (MultipleContactLinksOfSameTypeFoundException ex2) {
                    email = ex2.getMessage();
                    multipleEmailsFound ++;
                }
                Individual i = new Individual(nm, email, "", department);
                Individual.store(i);
            }
            Assert.assertEquals("There should be 14 individiuals", 14, Individual.getCount());
            Assert.assertEquals("There should be 0 individuals without emails", 0, emailsNotFound);
            Assert.assertEquals("There should be 1 individuals with multiple emails", 1, multipleEmailsFound);
            Assert.assertEquals("There should be 13 individuals with single", 13, singleEmailsFound);
        } catch (Exception ex) {
            System.err.println(ExceptionUtils.getStackTrace(ex));
            fail("Encountered problems: " + ex.getMessage());
        }
    }
    
}
