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
import java.io.UnsupportedEncodingException;
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
import org.norvelle.addressdiscoverer.parse.structured.BackwardsFlattenedDocumentIterator;
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
public class InfoInSuccessivePsNoDivisionTwoContacts implements IProgressConsumer {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public InfoInSuccessivePsNoDivisionTwoContacts() {
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
    public void testContactLinkFinderTwoContactsNoStructure() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/individuals/InfoInSuccessivePsNoDivisionTwoContacts.html";
            String html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
            Document soup = Jsoup.parse(html);
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(
                    ExtractIndividualsStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            NameElementFinder nameElementFinder = 
                new NameElementFinder(soup, "iso-8859-1", status);
            List<NameElement> nameElements = nameElementFinder.getNameElements();
            String nameString = StringUtils.join(nameElements, "\n");
            //logger.log(Level.INFO, nameString);
            ContactLinkFinder clFinder = new ContactLinkFinder(nameElementFinder, soup, status);
            PageContactType contactType = clFinder.getPageContactType();
            Assert.assertEquals("Page contact type should be NO_ASSOCIATED_CONTACT_INFO", 
                    PageContactType.NO_ASSOCIATED_CONTACT_INFO, contactType);
            //Assert.assertTrue("Number of contact links should be approximately equal to number of names",
            //        Approximately.equals(clFinder.getContactLinksFound(), nameElements.size())); 
        } catch (IOException | EndNodeWalkingException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        } 
    }
    
    @Test
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void testClassification() {
        String htmlUri = "/org/norvelle/addressdiscoverer/resources/TwoContactsNoStructure.html";
        String html;
        try {
            html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
        } catch (IOException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
            return;
        }
        
        Document soup = Jsoup.parse(html);
        BackwardsFlattenedDocumentIterator iterator;
        try { 
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(ExtractIndividualsStatusReporter.ClassificationStages.CREATING_ITERATOR, null);
            iterator = new BackwardsFlattenedDocumentIterator(soup, "iso-8859-1", status);
        } catch (UnsupportedEncodingException | EndNodeWalkingException ex) {
            fail("Encountered problems iterating over document: " + ex.getMessage());
            return;
        }
        org.junit.Assert.assertTrue("The iterator should not be empty", iterator.size() != 0);
        org.junit.Assert.assertEquals("The iterator should have 3 elements", 3, iterator.size());
    }
    */
    
}
