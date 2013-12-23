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

import java.util.HashMap;
import java.util.Map;

/**
 * A class for communicating status and progress information back to the GUI.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ClassificationStatusReporter {
    
    /**
     * An enum that specifies the different classification steps and gives their order
     */
    public enum ClassificationStages {

        CREATING_ITERATOR(1), 
        SAVING(8);

        private final int value;

        private ClassificationStages(int value) {
            this.value = value;
        }
    };

    /**
     * A set of labels for each stage of the parsing process
     */
    private final Map<Integer, String> classificationStages = new HashMap<Integer, String>()
    {{
         put(ClassificationStages.CREATING_ITERATOR.ordinal(), "Creating backwards iterator");
         put(ClassificationStages.SAVING.ordinal(), "Saving");
    }};
    
    private ClassificationStages stage;
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
    public ClassificationStatusReporter(ClassificationStages stage, IProgressConsumer progressConsumer) {
        this.stage = stage;
        this.label = this.classificationStages.get(stage.ordinal());
        this.baseLabel = label;
        this.progressConsumer = progressConsumer;
        this.currentNumericStep = 0;
        if (this.progressConsumer != null)
            this.progressConsumer.reportProgressStage(this);
    }
    
    public void setTotalNumericSteps(int totalSteps) {
        this.totalNumericSteps = totalSteps;
        this.currentNumericStep = 0;
        this.baseLabel = this.classificationStages.get(this.stage.ordinal());
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
        int percentComplete = (int) (((double) this.currentNumericStep / 
                (double) this.totalNumericSteps) * 100.0);
        this.label = String.format("%s: %2d%% complete", this.baseLabel, percentComplete);
        if (this.progressConsumer != null && percentComplete % 10 == 0)
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

    public void setStage(ClassificationStages stage) {
        this.stage = stage;
        this.label = this.classificationStages.get(stage.ordinal());
        if (this.progressConsumer != null)
            this.progressConsumer.reportProgressStage(this);
    }
    
    @Override
    public String toString() {
        return String.format("Stage %d: %s", this.stage.ordinal(), this.label);
    }
}
