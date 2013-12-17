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

import java.util.HashMap;
import java.util.Map;

/**
 * A class for communicating status and progress information back to the GUI.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class StatusReporter {
    
    /**
     * An enum that specifies the different parsing steps and gives their order
     */
    public enum ParsingStages {

        READING_FILE(1), 
        DELETING(2), 
        PARSING_HTML(3), 
        FINDING_EMAILS(4),
        FINDING_EMAILS_IN_TRS(5),
        FINDING_EMAILS_IN_LINKS(6), 
        EXTRACTING_INDIVIDUALS(7), 
        SAVING(8);

        private final int value;

        private ParsingStages(int value) {
            this.value = value;
        }
    };

    /**
     * A set of labels for each stage of the parsing process
     */
    private final Map<Integer, String> parsingStages = new HashMap<Integer, String>()
    {{
         put(ParsingStages.READING_FILE.ordinal(), "Reading Source Document");
         put(ParsingStages.DELETING.ordinal(), "Deleting");
         put(ParsingStages.PARSING_HTML.ordinal(), "Parsing HTML");
         put(ParsingStages.FINDING_EMAILS.ordinal(), "Finding emails (pass 1)");
         put(ParsingStages.FINDING_EMAILS_IN_TRS.ordinal(), "Finding emails (pass 2)");
         put(ParsingStages.FINDING_EMAILS_IN_LINKS.ordinal(), "Finding Emails in Links");
         put(ParsingStages.EXTRACTING_INDIVIDUALS.ordinal(), "Extracting Individuals");
         put(ParsingStages.SAVING.ordinal(), "Saving");
    }};
    
    private ParsingStages stage;
    private String baseLabel;
    private String label;
    private int totalNumericSteps = 1;
    private int currentNumericStep;
    private final IProgressConsumer progressConsumer;
   
    /**
     * Constructor
     * 
     * @param stage
     * @param progressConsumer 
     */    
    @SuppressWarnings("LeakingThisInConstructor")
    public StatusReporter(ParsingStages stage, IProgressConsumer progressConsumer) {
        this.stage = stage;
        this.label = this.parsingStages.get(stage.ordinal());
        this.baseLabel = label;
        this.progressConsumer = progressConsumer;
        this.currentNumericStep = 0;
        if (this.progressConsumer != null)
            this.progressConsumer.reportProgressStage(this);
    }
    
    public void setTotalNumericSteps(int totalSteps) {
        this.totalNumericSteps = totalSteps;
        this.currentNumericStep = 0;
        this.baseLabel = this.parsingStages.get(this.stage.ordinal());
        this.reportNumericProgress();
    }
    
    public void setNumericProgress(int stepsCompleted) {
        this.currentNumericStep = stepsCompleted;
        this.reportNumericProgress();
    }
    
    public void incrementNumericProgress() {
        this.currentNumericStep ++;
        this.reportNumericProgress();
    }

    private void reportNumericProgress() {
        int percentComplete = (this.currentNumericStep / this.totalNumericSteps) * 100;
        if (percentComplete % 10 == 0) 
            this.label = String.format("%s: %2d%% complete", this.baseLabel, percentComplete);
        if (this.progressConsumer != null)
            this.progressConsumer.reportProgressStage(this);
    }
    
    public int getStage() {
        return stage.ordinal();
    }

    public String getLabel() {
        return this.label;
    }    

    public void setLabel(String label) {
        this.label = label;
    }

    public void setStage(ParsingStages stage) {
        this.stage = stage;
        this.label = this.parsingStages.get(stage.ordinal());
        if (this.progressConsumer != null)
            this.progressConsumer.reportProgressStage(this);
    }
    
    @Override
    public String toString() {
        return String.format("Stage %d: %s", this.stage.ordinal(), this.label);
    }
}
