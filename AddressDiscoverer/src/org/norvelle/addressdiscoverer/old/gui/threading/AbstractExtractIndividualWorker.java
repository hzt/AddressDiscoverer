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
package org.norvelle.addressdiscoverer.old.gui.threading;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingWorker;
import org.apache.commons.io.IOUtils;
import org.norvelle.addressdiscoverer.AddressDiscoverer;
import org.norvelle.addressdiscoverer.gui.EmailDiscoveryPanel;
import org.norvelle.addressdiscoverer.gui.threading.IProgressConsumer;
import org.norvelle.addressdiscoverer.gui.threading.ProcessListener;
import org.norvelle.addressdiscoverer.gui.threading.StatusReporter;
import org.norvelle.addressdiscoverer.old.parse.IndividualExtractor;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.UnparsableIndividual;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public abstract class AbstractExtractIndividualWorker 
    extends SwingWorker<Integer, StatusReporter> implements IProgressConsumer 
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected final HashMap<String, Integer> programProgress = new HashMap<>();
    protected List<ProcessListener> listeners = new ArrayList<>();
    protected final EmailDiscoveryPanel panel;
    protected final Department department;

    public AbstractExtractIndividualWorker(final Department department, 
            final EmailDiscoveryPanel panel) 
    {
        this.department = department;
        this.panel = panel;
    }

    /**
     * Adds a process listener.
     * @param listener the listener to be added
     */
    public void addProcessListener(ProcessListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a process listener.
     * @param listener the listener to be removed
     */
    public void removeProcessListener(ProcessListener listener) {
        listeners.remove(listener);
    }

    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
    protected void extractIndividuals(String html, String encoding, StatusReporter status) {
        try {
            status.setStage(StatusReporter.ParsingStages.DELETING);
            Individual.deleteIndividualsForDepartment(this.department);
            IndividualExtractor addressParser = new IndividualExtractor(this.department, status);
            List<Individual> individuals = addressParser.parse(html, encoding);
            status.setStage(StatusReporter.ParsingStages.SAVING);
            int individualNumber = 0;
            for (Individual i : individuals) {
                status.setNumericProgress(individualNumber++);
                this.reportProgressStage(status);
                if (!i.getClass().equals(UnparsableIndividual.class))
                    Individual.store(i);
            }
            this.panel.populateResultsTable(individuals);
            this.panel.notifyParsingFinished();
        } 
        catch (Exception ex) 
        {
            AddressDiscoverer.reportException(ex);
        } 
    }

    /**
     * Given new HTML that has been retrieved, update the Department object to
     * store it.
     *
     * @param html
     */
    void updateDepartmentHTML(String html) {
        this.department.setHtml(html);
        try {
            Department.update(this.department);
        } catch (SQLException ex) {
            AddressDiscoverer.reportException(ex);
        }
    }
    
    @Override
    public void reportProgressStage(StatusReporter stage) {
        publish(stage);
    }

    /**
     * This method receives the signals that the doInBackground method sends out,
     * allowing the SwingWorker to periodically check those signals and process
     * them here.
     *
     * @param statusUpdates List of status updates to be sent to the GUI
     */
    @Override
    protected void process(final List<StatusReporter> statusUpdates) {
        for (final StatusReporter status : statusUpdates) {
            this.panel.notifyParsingStage(status);
        }
    }
    
}
