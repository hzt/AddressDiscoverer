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
package org.norvelle.addressdiscoverer.gui;

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
import org.norvelle.addressdiscoverer.IndividualExtractor;
import org.norvelle.addressdiscoverer.exceptions.CannotStoreNullIndividualException;
import org.norvelle.addressdiscoverer.exceptions.IndividualHasNoDepartmentException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.NullIndividual;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public abstract class AbstractExtractIndividualWorker 
    extends SwingWorker<Integer, Integer> implements IProgressConsumer 
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

    protected void extractIndividuals(String html) {
        try {
            Individual.deleteIndividualsForDepartment(this.department);
            IndividualExtractor addressParser = new IndividualExtractor(this.department, this);
            List<Individual> individuals = addressParser.parse(html);
            for (Individual i : individuals) {
                if (!i.getClass().equals(NullIndividual.class))
                    Individual.store(i);
            }
            this.panel.populateResultsTable(individuals);
            this.panel.notifyParsingFinished();
        } catch (OrmObjectNotConfiguredException | SQLException | IndividualHasNoDepartmentException | CannotStoreNullIndividualException ex) {
            AddressDiscoverer.reportException(ex);
        }
    }

    protected String getCharsetFromStream(InputStream in) throws IOException {
        String charset = "";
        StringWriter writer = new StringWriter();
        IOUtils.copy(in, writer, Charset.forName("UTF-8"));
        String html = writer.toString();
        Pattern charsetPattern = Pattern.compile("charset=(.*?)\"");
        Matcher charsetMatcher = charsetPattern.matcher(html);
        while (charsetMatcher.find()) {
            charset = charsetMatcher.group(1);
            break;
        }
        // Check to see if the charset found is valid
        boolean charsetNotValid = true;
        if (!charset.isEmpty()) {
            try {
                Charset c = Charset.forName(charset);
                charsetNotValid = false;
                charset = c.displayName();
            } catch (IllegalCharsetNameException e) {
                charset = "";
            }
        }
        if (charset.isEmpty() || charsetNotValid) {
            charset = "UTF-8";
        }
        logger.log(Level.INFO, "Found charset: {0}", charset);
        return charset;
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
    public void publishProgress(int progress) {
        publish(progress);
    }
    
    @Override
    public void setTotalElementsToProcess(int size) {
        this.panel.getjParsingProgressBar().setMaximum(size);
    }

    /**
     * This method receives the signals that the doInBackground method sends out,
     * allowing the SwingWorker to periodically check those signals and process
     * them here.
     *
     * @param progressUpdates
     */
    @Override
    protected void process(final List<Integer> progressUpdates) {
        for (final Integer progress : progressUpdates) {
            this.panel.getjParsingProgressBar().setValue(progress);
        }
    }
    
}
