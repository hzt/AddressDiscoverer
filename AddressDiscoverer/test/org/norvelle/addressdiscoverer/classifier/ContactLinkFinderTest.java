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
package org.norvelle.addressdiscoverer.classifier;

import com.j256.ormlite.support.ConnectionSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.classifier.ContactLinkFinder.PageContactType;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ContactLinkFinderTest implements IProgressConsumer {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public ContactLinkFinderTest() {
    }

    @Override
    public void reportProgressStage(ClassificationStatusReporter progress) {
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

    @Test
    public void testSameElement() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/InfoInSuccessiveTdsAndTrsNoDivisions.html";
            String html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
            Document soup = Jsoup.parse(html);
            Elements strongs = soup.select("strong");
            Assert.assertTrue("There must be at least one STRONG found", strongs.size() >= 1);
            Element strong = strongs.first();
            List<NameElement> dummy = new ArrayList<>();
            ClassificationStatusReporter status = new ClassificationStatusReporter(
                    ClassificationStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            ContactLinkFinder lf = new ContactLinkFinder(dummy, soup, status);
            boolean isAncestorToSelf = lf.isElementOneAncestorOfElementTwo(strong, strong);
            Assert.assertFalse("Strong cannot be ancestor to Strong", isAncestorToSelf);
        } catch (IOException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        }
    }
    
    @Test
    public void testDistantAncestor() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/InfoInSuccessiveTdsAndTrsNoDivisions.html";
            String html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
            Document soup = Jsoup.parse(html);
            Elements strongs = soup.select("strong");
            Element strong = strongs.first();
            Element distantAncestor = strong.parent().parent();
            List<NameElement> dummy = new ArrayList<>();
            ClassificationStatusReporter status = new ClassificationStatusReporter(
                    ClassificationStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            ContactLinkFinder lf = new ContactLinkFinder(dummy, soup, status);
            boolean distantAncestorIsParent = lf.isElementOneAncestorOfElementTwo(distantAncestor, strong);
            Assert.assertTrue("Distant ancestor is ancestor to strong", distantAncestorIsParent);
        } catch (IOException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        }
    }
    
    @Test
    public void testNonParent() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/InfoInSuccessiveTdsAndTrsNoDivisions.html";
            String html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
            Document soup = Jsoup.parse(html);
            Elements strongs = soup.select("strong");
            Element strong = strongs.first();
            Elements otherElements = soup.select("title");
            Element otherElement = otherElements.first();
            List<NameElement> dummy = new ArrayList<>();
            ClassificationStatusReporter status = new ClassificationStatusReporter(
                    ClassificationStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            ContactLinkFinder lf = new ContactLinkFinder(dummy, soup, status);
            boolean distantAncestorIsParent = lf.isElementOneAncestorOfElementTwo(otherElement, strong);
            Assert.assertFalse("Other element cannot be ancestor to strong", distantAncestorIsParent);
        } catch (IOException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        }
    }
    
    @Test
    public void testContactLinkFinderOnInfoInSuccessiveTdsAndTrsNoDivisions() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/InfoInSuccessiveTdsAndTrsNoDivisions.html";
            String html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
            Document soup = Jsoup.parse(html);
            ClassificationStatusReporter status = new ClassificationStatusReporter(
                    ClassificationStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            NameElementFinder nameElementFinder = 
                new NameElementFinder(soup, "iso-8859-1", status);
            List<NameElement> nameElements = nameElementFinder.getNameElements();
            ContactLinkFinder clFinder = new ContactLinkFinder(nameElements, soup, status);
            PageContactType contactType = clFinder.getPageContactType();
            Assert.assertEquals("Page contact type should be NO_ASSOCIATED_CONTACT_INFO", 
                    PageContactType.NO_ASSOCIATED_CONTACT_INFO, contactType);
        } catch (IOException | EndNodeWalkingException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        } 
    }
    
    @Test
    public void testContactLinkFinderOnInfoInSuccessivePsNoDivisions() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/InfoInSuccessivePsNoDivisions.html";
            String html = Utils.loadStringFromResource(htmlUri, "UTF-8");
            Document soup = Jsoup.parse(html);
            ClassificationStatusReporter status = new ClassificationStatusReporter(
                    ClassificationStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            NameElementFinder nameElementFinder = 
                new NameElementFinder(soup, "iso-8859-1", status);
            List<NameElement> nameElements = nameElementFinder.getNameElements();
            ContactLinkFinder clFinder = new ContactLinkFinder(nameElements, soup, status);
            PageContactType contactType = clFinder.getPageContactType();
            Assert.assertEquals("Page contact type should be HAS_ASSOCIATED_CONTACT_INFO", 
                    PageContactType.HAS_ASSOCIATED_CONTACT_INFO, contactType);
        } catch (IOException | EndNodeWalkingException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        } 
    }
    
    @Test
    public void testContactLinkFinderOnMultipleTdsForSingleRecordPerTr() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/MultipleTdsForSingleRecordPerTr.html";
            String html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
            Document soup = Jsoup.parse(html);
            ClassificationStatusReporter status = new ClassificationStatusReporter(
                    ClassificationStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            NameElementFinder nameElementFinder = 
                new NameElementFinder(soup, "iso-8859-1", status);
            List<NameElement> nameElements = nameElementFinder.getNameElements();
            String nameString = StringUtils.join(nameElements, "\n");
            //logger.log(Level.INFO, nameString);
            ContactLinkFinder clFinder = new ContactLinkFinder(nameElements, soup, status);
            PageContactType contactType = clFinder.getPageContactType();
            Assert.assertEquals("Page contact type should be HAS_ASSOCIATED_CONTACT_INFO", 
                    PageContactType.HAS_ASSOCIATED_CONTACT_INFO, contactType);
            //Assert.assertTrue("Number of contact links should be approximately equal to number of names",
            //        Approximately.equals(clFinder.getContactLinksFound(), nameElements.size())); 
        } catch (IOException | EndNodeWalkingException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        } 
    }
    
    
}
