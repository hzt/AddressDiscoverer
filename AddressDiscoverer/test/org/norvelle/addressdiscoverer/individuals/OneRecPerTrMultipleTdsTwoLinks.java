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
package org.norvelle.addressdiscoverer.individuals;

import com.j256.ormlite.support.ConnectionSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.gui.threading.ExtractIndividualsStatusReporter;
import org.norvelle.addressdiscoverer.parse.EmailContactLink;
import org.norvelle.addressdiscoverer.classifier.IProgressConsumer;
import org.norvelle.addressdiscoverer.parse.NameElement;
import org.norvelle.addressdiscoverer.parse.NameElementFinder;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class OneRecPerTrMultipleTdsTwoLinks implements IProgressConsumer {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public OneRecPerTrMultipleTdsTwoLinks() {
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
            connection = TestUtilities.getDBConnection("addresses.test.sqlite");
        } catch (SQLException | CannotLoadJDBCDriverException |IOException ex) {
            fail("Encountered problems connecting to database: " + ex.getMessage());
            return;
        }
    }

    /*
    @Test
    public void testsdiaz() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/individuals/OneRecPerTrMultipleTdsTwoLinks.html";
            String html = Utils.loadStringFromResource(htmlUri, "UTF-8");
            Document soup = Jsoup.parse(html);
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(
                    ExtractIndividualsStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            NameElementFinder nameElementFinder = 
                new NameElementFinder(soup, "UTF-8", status);
            ContactLinkFinder clFinder = new ContactLinkFinder(nameElementFinder, soup, status);
            
            // Check for correct number of contact links found
            Assert.assertEquals("Should find one contact link", 1, clFinder.getNumContactLinksFound());
            
            // Check we have the correct name found
            List<NameElement> nameElements = nameElementFinder.getNameElements();
            NameElement nm = nameElements.get(0);
            Assert.assertEquals("Name should be SANCHEZ DIAZ, EMILI-MIQUEL", "SANCHEZ DIAZ, EMILI-MIQUEL", nm.toString());

            // Test contact link type = email 
            EmailContactLink link = nm.getContactLink();
            Assert.assertEquals("Contact link should be an email", EmailContactLink.ContactType.EMAIL_IN_CONTENT, link.getType());
            Assert.assertEquals("Contact link should be emilsanchezd@ub.edu", "emilsanchezd@ub.edu", link.getAddress());
        } catch (IOException | EndNodeWalkingException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        }
    } */
  
    
}
