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
package org.norvelle.addressdiscoverer.fullpages.unstructured;

import com.j256.ormlite.support.ConnectionSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.gui.threading.ExtractIndividualsStatusReporter;
import org.norvelle.addressdiscoverer.classifier.IProgressConsumer;
import org.norvelle.addressdiscoverer.parse.structured.StructuredPageNameElement;
import org.norvelle.addressdiscoverer.parse.INameElementFinder;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class InfoInSuccessiveTdsAndTrs implements IProgressConsumer {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public InfoInSuccessiveTdsAndTrs() {
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
    public void testSameElement() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/InfoInSuccessiveTdsAndTrsNoDivisions.html";
            String html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
            Document soup = Jsoup.parse(html);
            Elements strongs = soup.select("strong");
            Assert.assertTrue("There must be at least one STRONG found", strongs.size() >= 1);
            Element strong = strongs.first();
            List<NameElement> dummy = new ArrayList<>();
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(
                    ExtractIndividualsStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            NameElementFinder nameElementFinder = 
                new NameElementFinder(soup, "iso-8859-1", status);
            ContactLinkFinder clFinder = new ContactLinkFinder(nameElementFinder, soup, status);
            boolean isAncestorToSelf = NameElement.isElementOneAncestorOfElementTwo(strong, strong);
            Assert.assertFalse("Strong cannot be ancestor to Strong", isAncestorToSelf);
        } catch (IOException | EndNodeWalkingException ex) {
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
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(
                    ExtractIndividualsStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            NameElementFinder nameElementFinder = 
                new NameElementFinder(soup, "iso-8859-1", status);
            ContactLinkFinder clFinder = new ContactLinkFinder(nameElementFinder, soup, status);
            boolean distantAncestorIsParent = NameElement.isElementOneAncestorOfElementTwo(distantAncestor, strong);
            Assert.assertTrue("Distant ancestor is ancestor to strong", distantAncestorIsParent);
        } catch (IOException | EndNodeWalkingException ex) {
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
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(
                    ExtractIndividualsStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            NameElementFinder nameElementFinder = 
                new NameElementFinder(soup, "iso-8859-1", status);
            ContactLinkFinder clFinder = new ContactLinkFinder(nameElementFinder, soup, status);
            boolean distantAncestorIsParent = NameElement.isElementOneAncestorOfElementTwo(otherElement, strong);
            Assert.assertFalse("Other element cannot be ancestor to strong", distantAncestorIsParent);
        } catch (IOException | EndNodeWalkingException  ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        }
    }
    
    @Test
    public void testContactLinkFinderOnInfoInSuccessiveTdsAndTrsNoDivisions() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/InfoInSuccessiveTdsAndTrsNoDivisions.html";
            String html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
            Document soup = Jsoup.parse(html);
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(
                    ExtractIndividualsStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            NameElementFinder nameElementFinder = 
                new NameElementFinder(soup, "iso-8859-1", status);
            ContactLinkFinder clFinder = new ContactLinkFinder(nameElementFinder, soup, status);
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
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(
                    ExtractIndividualsStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            NameElementFinder nameElementFinder = 
                new NameElementFinder(soup, "iso-8859-1", status);
            ContactLinkFinder clFinder = new ContactLinkFinder(nameElementFinder, soup, status);
            PageContactType contactType = clFinder.getPageContactType();
            Assert.assertEquals("Page contact type should be HAS_ASSOCIATED_CONTACT_INFO", 
                    PageContactType.HAS_ASSOCIATED_CONTACT_INFO, contactType);
        } catch (IOException | EndNodeWalkingException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        } 
    }
    
    @Test
    public void testInfoInSuccessiveTdsAndTrsNoDivisions() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/InfoInSuccessiveTdsAndTrsNoDivisions.html";
            String html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
            Document soup = Jsoup.parse(html);

            // Classify the page to discover its structure
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(
                ExtractIndividualsStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            NameElementFinder nameElementFinder = 
                new NameElementFinder(soup, "iso-8859-1", status);
            ContactLinkFinder clFinder = new ContactLinkFinder(nameElementFinder, soup, status);
            PageClassifier classifier = new PageClassifier(nameElementFinder, clFinder, status);
            
            PageClassifier.Classification classification = classifier.getClassification();
            org.junit.Assert.assertEquals("The classification should be TR structured page", 
                    PageClassifier.Classification.UNSTRUCTURED, classification);
        } catch (IOException | EndNodeWalkingException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        }
    }
    */
    
}
