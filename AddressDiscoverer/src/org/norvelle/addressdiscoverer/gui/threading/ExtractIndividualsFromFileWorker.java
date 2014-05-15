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
import javax.swing.JOptionPane;
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
import org.norvelle.addressdiscoverer.gui.EmailDiscoveryPanel;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.UnamName;
import org.norvelle.addressdiscoverer.parse.INameElement;
import org.norvelle.addressdiscoverer.parse.ContactLink;
import org.norvelle.addressdiscoverer.parse.structured.StructuredPageContactLinkLocator;
import org.norvelle.addressdiscoverer.parse.INameElementFinder;
import org.norvelle.addressdiscoverer.parse.structured.StructuredNameElementFinder;
import org.norvelle.addressdiscoverer.parse.unstructured.UnstructuredNameElementFinder;
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
    private final boolean useSequentialParser;

    /**
     * Run the classification process on the contents of a file in the filesystem
     * 
     * @param parent
     * @param fileToClassify
     * @param department
     * @param useSequentialParser
     */
    public ExtractIndividualsFromFileWorker(EmailDiscoveryPanel parent, 
            File fileToClassify, Department department, boolean useSequentialParser) 
    {
        this.parent = parent;
        this.fileToClassify = fileToClassify;
        this.department = department;
        this.useSequentialParser = useSequentialParser;
    }

    @Override
    @SuppressWarnings({"UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch"})
    protected String doInBackground() throws Exception {
        InputStream in = null;
        try {
            // Set the base URL if one is specified (in order to resolve urls
            // that point to web links within downloaded pages
            if (!department.getBaseUrl().isEmpty())
                StructuredPageContactLinkLocator.baseUrl = department.getBaseUrl();
            
            // Delete any individuals present from last parse.
            Individual.deleteIndividualsForDepartment(department);
            
            // Fetch the page and parse it into a JSoup document
            in = new FileInputStream(this.fileToClassify);
            String charset = Utils.getCharsetFromStream(in);
            String html = FileUtils.readFileToString(this.fileToClassify, Charset.forName(charset));
            Document soup = Jsoup.parse(html, charset);
            
            // Classify the page to discover its structure
            parent.getjStageNameLabel().setText("Finding names");
            ExtractIndividualsStatusReporter status = new ExtractIndividualsStatusReporter(
                ClassificationStages.CREATING_ITERATOR, this);
            INameElementFinder nameElementFinder;
            if (!this.useSequentialParser)
                nameElementFinder = new StructuredNameElementFinder(soup, charset, status);
            else
                nameElementFinder = new UnstructuredNameElementFinder(soup, charset, status);

            // Now, fetch the individuals we've extract as a List
            List<Individual> individuals = null; //extractor.getIndividuals();
            List<INameElement> nameElements = nameElementFinder.getNameElements();
            int count = 1;
            for (INameElement ne : nameElements) {
                parent.getjStageNameLabel().setText(String.format(
                        "Processing name %d out of %d", count ++, nameElements.size()));
                
                // First, see if we can parse the name; if not, we skip this name
                UnamName nm;
                try {
                    nm = ne.getUnamName();
                }
                catch (CantParseIndividualException e) {
                    this.reportException("Couldn't parse name for " + ne.toString());
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
                    this.reportException(String.format("Exception '%s' while processing %s", ex3.getMessage(), ne.toString()));
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
    
    /**
     * A single-access point for reporting exceptions to the user.
     * 
     * @param message
     */
    public void reportException(String message) {
        JOptionPane.showMessageDialog(null,
            Utils.wordWrapString(message, 70),
            "Program error", JOptionPane.ERROR_MESSAGE);
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
