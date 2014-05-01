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
import org.norvelle.addressdiscoverer.AddressDiscoverer;
import org.norvelle.addressdiscoverer.gui.threading.ExtractIndividualsStatusReporter.ClassificationStages;
import org.norvelle.addressdiscoverer.classifier.IProgressConsumer;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.DoesNotContainContactLinkException;
import org.norvelle.addressdiscoverer.exceptions.MultipleContactLinksOfSameTypeFoundException;
import org.norvelle.addressdiscoverer.parse.NameElementFinder;
import org.norvelle.addressdiscoverer.gui.EmailDiscoveryPanel;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.Name;
import org.norvelle.addressdiscoverer.parse.ContactLink;
import org.norvelle.addressdiscoverer.parse.NameElement;
import org.norvelle.utils.Utils;

/**
 * A SwingWorker to handle background processing of the page classification
 * process. 
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ExtractIndividualsFromFileWorker 
    extends SwingWorker<String, String> implements IProgressConsumer
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected File fileToClassify;
    private final EmailDiscoveryPanel parent;
    private final Department department;

    /**
     * Run the classification process on the contents of a file in the filesystem
     * 
     * @param parent
     * @param fileToClassify
     */
    public ExtractIndividualsFromFileWorker(EmailDiscoveryPanel parent, 
            File fileToClassify, Department department) 
    {
        this.parent = parent;
        this.fileToClassify = fileToClassify;
        this.department = department;
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
            parent.getjStageNameLabel().setText("Finding names");
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(
                ClassificationStages.CREATING_ITERATOR, this);
            NameElementFinder nameElementFinder = 
                new NameElementFinder(soup, charset, status);

            // Now, fetch the individuals we've extract as a List
            List<Individual> individuals = null; //extractor.getIndividuals();
            List<NameElement> nameElements = nameElementFinder.getNameElements();
            int count = 1;
            for (NameElement ne : nameElements) {
                parent.getjStageNameLabel().setText(String.format(
                        "Processing name %d out of %d", count ++, nameElements.size()));
                
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
                }
                catch (DoesNotContainContactLinkException ex) {
                    email = "Not found";
                }
                catch (MultipleContactLinksOfSameTypeFoundException ex2) {
                    email = ex2.getMessage();
                }
                catch (Exception ex3) {
                    continue;
                }
                Individual i = new Individual(nm, email, "", department);
                Individual.store(i);
            }
            parent.getjStageNameLabel().setText(String.format("Found %d individuals", Individual.getCount()));

            // All done    
            this.parent.notifyParsingFinished();
            this.parent.refreshResultsTable();
        } catch (Exception ex) {
            AddressDiscoverer.reportException(ex);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException ex) {
                AddressDiscoverer.reportException(ex);
            }
        }
        
        parent.getjStageNameLabel().setText("Done");
        return "";
    }
    
    @Override
    public void reportProgressStage(ExtractIndividualsStatusReporter progress) {
        publish(progress.toString());
    }
    
    @Override
    public void reportText(String text) {
        this.parent.getjStageNameLabel().setText(text);
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
