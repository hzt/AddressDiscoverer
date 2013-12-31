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
public class NameElementPathTest implements IProgressConsumer {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public NameElementPathTest() {
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
    public void testNameElementPathForMultipleTdsForSingleRecordPerTr() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/MultipleTdsForSingleRecordPerTr.html";
            String html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
            Document soup = Jsoup.parse(html);
            ClassificationStatusReporter status = new ClassificationStatusReporter(
                    ClassificationStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
            NameElementFinder nameElementFinder = 
                new NameElementFinder(soup, "iso-8859-1", status);
            NameElementPath path = nameElementFinder.getPathToNameElements();
            Assert.assertFalse("Path is not null", path == null);
            //Assert.assertEquals("Page contact type should be NO_ASSOCIATED_CONTACT_INFO", 
            //        PageContactType.NO_ASSOCIATED_CONTACT_INFO, contactType);
        } catch (IOException | EndNodeWalkingException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        } 
    }
    
    //@Test
    public void testContactLinkFinderOnInfoInSuccessivePsNoDivisions() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/InfoInSuccessivePsNoDivisions.html";
            String html = Utils.loadStringFromResource(htmlUri, "UTF-8");
            Document soup = Jsoup.parse(html);
            ClassificationStatusReporter status = new ClassificationStatusReporter(
                    ClassificationStatusReporter.ClassificationStages.CREATING_ITERATOR, this);
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
        
    
}
