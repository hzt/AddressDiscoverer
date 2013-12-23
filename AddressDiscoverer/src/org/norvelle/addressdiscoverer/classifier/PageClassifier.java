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

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.Constants;
import org.norvelle.addressdiscoverer.classifier.ClassificationStatusReporter.ClassificationStages;
import org.norvelle.addressdiscoverer.classifier.EmailElementFinder.ContactInformationType;
import org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class PageClassifier {
    
    public enum Classification {
        UNSTRUCTURED_P_PAGE, UNSTRUCTURED_TR_PAGE, UNSTRUCTURED_DIV_PAGE,
        TR_STRUCTURED_PAGE, UL_STRUCTURED_PAGE, OL_STRUCTURED_PAGE, UNDETERMINED;
    }
    
    private final Document soup;
    private final String encoding;
    private final ClassificationStatusReporter status;
    private NameElementFinder nameElementFinder;
    private Classification pageClassification;
    private ContactInformationType contactInformationType;
    
    public PageClassifier(Document soup, String encoding, IProgressConsumer progressConsumer) {
        this.soup = soup;
        this.encoding = encoding;
        this.status = new ClassificationStatusReporter(
            ClassificationStages.CREATING_ITERATOR, progressConsumer);
    }
    
    /**
     * Run the classifier algorithm on the page and try to figure out which page 
     * configuration best corresponds to the way the names are distributed.
     * 
     * @return
     * @throws UnsupportedEncodingException
     * @throws EndNodeWalkingException 
     */
    public Classification getClassification() 
            throws UnsupportedEncodingException, EndNodeWalkingException, IllegalStateException 
    {
        this.nameElementFinder = new NameElementFinder(this.soup, this.encoding, this.status);
        EmailElementFinder emailFinder = new EmailElementFinder(this.nameElementFinder);
        int numberOfNames = nameElementFinder.getNumberOfNames();
        
        // Calculate the numbers of the distinct kinds of containers we track
        Approximately.defaultRange = numberOfNames;
        
        int numTrs = nameElementFinder.getNumTrs();
        int numUls = nameElementFinder.getNumUls();
        int numOls = nameElementFinder.getNumOls();
        int numPs = nameElementFinder.getNumPs();
        int numDivs = nameElementFinder.getNumDivs();

        // Now calculate the percentage "fill" for each kind
        double namesPerTr = (double) nameElementFinder.getNumTrs() / (double) numberOfNames;
        double namesPerUl = (double) nameElementFinder.getNumUls() / (double) numberOfNames;
        double namesPerOl = (double) nameElementFinder.getNumOls() / (double) numberOfNames;
        double namesPerP = (double) nameElementFinder.getNumPs() / (double) numberOfNames;
        double namesPerDiv = (double) nameElementFinder.getNumDivs() / (double) numberOfNames;
        double contactLinksPerTr = (double) emailFinder.linksAssociatedWithName("tr").size() 
                / (double) nameElementFinder.getNumTrs();
        
        // See how many elements fall outside UL or OL elements
        int namesInsideTrs = nameElementFinder.getNameElementsByContainer("tr").size();
        int namesOutsideUls = numberOfNames - 
                nameElementFinder.getNameElementsByContainer("ul").size();
        int namesOutsideOls = numberOfNames - 
                nameElementFinder.getNameElementsByContainer("ol").size();
        
        StringBuilder sb = new StringBuilder();
        sb.append("Page statistics:\n")
                .append("Number of elements with names: ").append(numberOfNames).append("\n")
                .append("Number of <TRs>: ").append(numTrs).append("\n")
                .append("Number of <UL>s: ").append(numUls).append("\n")
                .append("Number of <OL>s: ").append(numOls).append("\n")
                .append("Number of <P>s: ").append(numPs).append("\n")
                .append("Number of <DIV>s: ").append(numDivs).append("\n")
                .append("Names per <TR>: ").append(Double.toString(namesPerTr)).append("\n")
                .append("Contact links per <TR>: ").append(Double.toString(contactLinksPerTr))
                    .append("\n")
                .append("Names per <UL>: ").append(Double.toString(namesPerUl)).append("\n")
                .append("Names per <OL>: ").append(Double.toString(namesPerOl)).append("\n")
                .append("Names inside <TR>s: ").append(namesInsideTrs).append("\n")
                .append("Names outside <UL>s: ").append(namesOutsideUls).append("\n")
                .append("Names outside <OL>: ").append(namesOutsideOls).append("\n")
                .append("Ratio of <P>s to total names: ")
                    .append(Double.toString(namesPerP)).append("\n")
                .append("Ratio of <DIV>s to total names: ")
                    .append(Double.toString(namesPerDiv)).append("\n");
        this.status.reportProgressText(sb.toString());

        // See if we have a page structured into natural divisions
        if (Approximately.equals(namesInsideTrs, numberOfNames) 
                && (namesPerTr <= 1.0 && namesPerTr > 0.3)
                && Approximately.equals(contactLinksPerTr, 1))
            this.pageClassification = Classification.TR_STRUCTURED_PAGE;
        else if (Approximately.equals(namesOutsideUls, 0)) // && Approximately.equals(namesPerUl, 1, 1)
                this.pageClassification = Classification.UL_STRUCTURED_PAGE;
        else if (Approximately.equals(namesOutsideOls, 0)) //  && Approximately.equals(namesPerOl, 1, 1)
                this.pageClassification = Classification.OL_STRUCTURED_PAGE;

        // See if we have an unstructured page
        else if (Approximately.equals(numTrs, numberOfNames))
            this.pageClassification = Classification.UNSTRUCTURED_TR_PAGE;
        else if (Approximately.equals(numPs, numberOfNames) ||
                (namesPerP < 1.0 && namesPerP > 0.3))
            this.pageClassification = Classification.UNSTRUCTURED_P_PAGE;
        else if (Approximately.equals(numDivs, numberOfNames) ||
                (namesPerDiv < 1.0 && namesPerDiv > 0.3))
            this.pageClassification = Classification.UNSTRUCTURED_DIV_PAGE;
        
        // Otherwise, we give up
        else this.pageClassification = Classification.UNDETERMINED;
                
        return this.pageClassification;
    }
    

    public List<Element> getContainerElements() {
        return this.nameElementFinder.getContainerElements();
    }

    public ContactInformationType getContactInfoType() {
        return this.contactInformationType;
    }
}
