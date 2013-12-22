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

import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 * A SwingWorker to handle setting genders for all Individuals in the background,
 * allowing the progress bar to be painted while the operation is ongoing.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ClassifyPageWorker 
    extends SwingWorker<String, String> implements IProgressConsumer
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final File fileToClassify;
    private final PageClassifierGUI parent;

    public ClassifyPageWorker(PageClassifierGUI parent, File fileToClassify) 
    {
        this.parent = parent;
        this.fileToClassify = fileToClassify;
    }

    @Override
    protected String doInBackground() throws Exception {
        
        return "";
    }
    
    @Override
    public void reportProgress(String line) {
        publish(line);
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
