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
package org.norvelle.addressdiscoverer.gui.threading;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.norvelle.addressdiscoverer.PageClassifierApp;
import org.norvelle.addressdiscoverer.classifier.ClassificationStatusReporter;
import org.norvelle.addressdiscoverer.classifier.ClassificationStatusReporter.ClassificationStages;
import org.norvelle.addressdiscoverer.classifier.ContactLinkFinder;
import org.norvelle.addressdiscoverer.classifier.IProgressConsumer;
import org.norvelle.addressdiscoverer.classifier.IndividualExtractor;
import org.norvelle.addressdiscoverer.classifier.NameElementFinder;
import org.norvelle.addressdiscoverer.classifier.PageClassifier;
import org.norvelle.addressdiscoverer.classifier.PageClassifier.Classification;
import org.norvelle.addressdiscoverer.classifier.StructuredPageExtractor;
import org.norvelle.addressdiscoverer.classifier.UnstructuredPageExtractor;
import org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException;
import org.norvelle.addressdiscoverer.gui.EmailDiscoveryPanel2;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.utils.Utils;

/**
 * A SwingWorker to handle background processing of the page classification
 * process. 
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ClassifyPageWorker 
    extends SwingWorker<String, String> implements IProgressConsumer
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected File fileToClassify;
    private final EmailDiscoveryPanel2 parent;

    /**
     * Run the classification process on the contents of a file in the filesystem
     * 
     * @param parent
     * @param fileToClassify
     */
    public ClassifyPageWorker(EmailDiscoveryPanel2 parent, File fileToClassify) 
    {
        this.parent = parent;
        this.fileToClassify = fileToClassify;
    }

    @Override
    @SuppressWarnings({"UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch"})
    protected String doInBackground() throws Exception {
        InputStream in = null;
        try {
            // Fetch the page and parse it into a JSoup document
            in = new FileInputStream(this.fileToClassify);
            String charset = Utils.getCharsetFromStream(in);
            String html = FileUtils.readFileToString(this.fileToClassify, Charset.forName(charset));
            Document soup = Jsoup.parse(html, charset);
            
            // Classify the page to discover its structure
            ClassificationStatusReporter status = new ClassificationStatusReporter(
                ClassificationStages.CREATING_ITERATOR, this);
            NameElementFinder nameElementFinder = 
                new NameElementFinder(soup, charset, status);
            ContactLinkFinder clFinder = new ContactLinkFinder(nameElementFinder, soup, status);
            PageClassifier classifier = new PageClassifier(nameElementFinder, clFinder, status);
            Classification classification = classifier.getClassification();
            
            // Depending on its structure, run the appropriate extractor
            IndividualExtractor extractor;
            if (classification == Classification.UNSTRUCTURED) 
                extractor = new UnstructuredPageExtractor(soup, nameElementFinder, clFinder, status);
            else
                extractor = new StructuredPageExtractor(soup, nameElementFinder, clFinder, status);
            extractor.extract();
            List<Individual> individuals = extractor.getIndividuals();
            publish(String.format("Found %d individuals", individuals.size()));

            // All done    
            this.parent.notifyParsingFinished();
            this.parent.refreshResultsTable();
        } catch (Exception ex) {
            PageClassifierApp.reportException(ex);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException ex) {
                PageClassifierApp.reportException(ex);
            }
        }
        
        return "";
    }
    
    @Override
    public void reportProgressStage(ClassificationStatusReporter progress) {
        publish(progress.toString());
    }
    
    @Override
    public void reportText(String text) {
        publish(text);
    }

    /**
     * This method receives the signals that the doInBackground method sends out,
     * allowing the SwingWorker to periodically check those signals and process
     * them here.
     *
     * @param progressUpdates
     */
    @Override
    protected void process(final List<String> progressUpdates) {
        for (final String progress : progressUpdates) {
            this.parent.addToOutput(progress);
        }
    }
    
}
