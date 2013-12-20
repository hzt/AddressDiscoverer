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

import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.apache.commons.lang3.StringUtils;
import org.norvelle.addressdiscoverer.gui.DatabaseToolsForm;
import org.norvelle.addressdiscoverer.model.GenderDeterminer;
import org.norvelle.addressdiscoverer.model.Individual;

/**
 * A SwingWorker to handle setting genders for all Individuals in the background,
 * allowing the progress bar to be painted while the operation is ongoing.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class DetermineGenderWorker 
    extends SwingWorker<Integer, Integer> 
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final DatabaseToolsForm parent;
    private final boolean limitToNonExported;

    public DetermineGenderWorker(DatabaseToolsForm parent, boolean limitToNonExported) {
        this.parent = parent;
        this.limitToNonExported = limitToNonExported;
    }

    @Override
    protected Integer doInBackground() throws Exception {
        List<Individual> individuals = Individual.getAll();
        this.parent.setMaxProgress(individuals.size());
        int rowCount = 0;
        for (Individual i : individuals) {
            if (this.limitToNonExported && i.isExported()) {
                rowCount ++;
                continue;
            }
            String wholeName = i.getFirstName();
            String[] parts = StringUtils.split(wholeName);
            String firstName = parts[0];
            GenderDeterminer.Gender gender = GenderDeterminer.getGender(firstName);
            if (gender == GenderDeterminer.Gender.FEMALE)
                i.setGender("F");
            else if (gender == GenderDeterminer.Gender.MALE) 
                i.setGender("M");
            else {
                // If there's a second name, try again with that.
                if (parts.length > 1) {
                    GenderDeterminer.Gender gender2 = GenderDeterminer.getGender(parts[1]);
                    if (gender2 == GenderDeterminer.Gender.FEMALE)
                        i.setGender("F");
                    else if (gender2 == GenderDeterminer.Gender.MALE) 
                        i.setGender("M");
                    else i.setGender("U");
                }
                
                // Only give up if nothing works.
                else i.setGender("U");
            }
            Individual.update(i);
            
            // Tell the GUI how far we've gotten.
            publish(rowCount++);
            
            // Check to see if we've been canceled
            if (Thread.currentThread().isInterrupted())
                break;
        }
        this.parent.setProgressDone();
        return 0;
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
        for (final int progress : progressUpdates) {
            this.parent.setCurrProgress(progress);
        }
    }
    
}
