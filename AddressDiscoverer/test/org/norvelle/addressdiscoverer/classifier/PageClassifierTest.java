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
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.classifier.PageClassifier.Classification;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class PageClassifierTest implements IProgressConsumer {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public PageClassifierTest() {
    }

    @Override
    public void reportProgressStage(ClassificationStatusReporter progress) {
        System.out.println(progress.toString());
    }
    
    @Override
    public void reportText(String text) {
        System.out.println(text);
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
    public void testMultipleTdsWithRecordsPerTr() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/MultipleTdsWithRecordsPerTr.html";
            String html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
            Document soup = Jsoup.parse(html);
            PageClassifier classifier = new PageClassifier(soup, "iso-8859-1", this);
            
            PageClassifier.Classification classification = classifier.getClassification();
            Assert.assertEquals("The classification should be TR structured page", 
                    Classification.TR_STRUCTURED_PAGE, classification);
        } catch (IOException | EndNodeWalkingException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        }
    }
    
    @Test
    public void testMultipleTdsForSingleRecordPerTr() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/MultipleTdsForSingleRecordPerTr.html";
            String html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
            Document soup = Jsoup.parse(html);
            PageClassifier classifier = new PageClassifier(soup, "iso-8859-1", this);
            
            PageClassifier.Classification classification = classifier.getClassification();
            Assert.assertEquals("The classification should be TR structured page", 
                    Classification.TR_STRUCTURED_PAGE, classification);
        } catch (IOException | EndNodeWalkingException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        }
    }
    
    @Test
    public void testSingleRecordPerLi() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/SingleRecordPerLi.html";
            String html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
            Document soup = Jsoup.parse(html);
            PageClassifier classifier = new PageClassifier(soup, "iso-8859-1", this);
            
            PageClassifier.Classification classification = classifier.getClassification();
            Assert.assertEquals("The classification should be TR structured page", 
                    Classification.UL_STRUCTURED_PAGE, classification);
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
            PageClassifier classifier = new PageClassifier(soup, "iso-8859-1", this);
            
            PageClassifier.Classification classification = classifier.getClassification();
            Assert.assertEquals("The classification should be TR structured page", 
                    Classification.UNSTRUCTURED_TR_PAGE, classification);
        } catch (IOException | EndNodeWalkingException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        }
    }
    
    @Test
    public void testInfoInSuccessivePsNoDivisions() {
        try {
            String htmlUri = "/org/norvelle/addressdiscoverer/resources/InfoInSuccessivePsNoDivisions.html";
            String html = Utils.loadStringFromResource(htmlUri, "iso-8859-1");
            Document soup = Jsoup.parse(html);
            PageClassifier classifier = new PageClassifier(soup, "iso-8859-1", this);
            
            PageClassifier.Classification classification = classifier.getClassification();
            Assert.assertEquals("The classification should be TR structured page", 
                    Classification.UNSTRUCTURED_P_PAGE, classification);
        } catch (IOException | EndNodeWalkingException ex) {
            fail("Encountered problems reading file: " + ex.getMessage());
        }
    }
    
}
