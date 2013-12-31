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
import org.norvelle.addressdiscoverer.classifier.ContactLink.ContactType;
import org.norvelle.addressdiscoverer.classifier.ContactLinkFinder.PageContactType;
import org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class PageClassifier {
    
    public enum Classification {
        UNSTRUCTURED, STRUCTURED, UNDETERMINED;
    }
    
    private final NameElementFinder nameElementFinder;
    private final ContactLinkFinder clFinder;
    private final ClassificationStatusReporter status;
    private Classification pageClassification;
    private ContactType contactInformationType;
    
    public PageClassifier(NameElementFinder nameElementFinder, 
            ContactLinkFinder clFinder, ClassificationStatusReporter status) 
    {
        this.nameElementFinder = nameElementFinder;
        this.clFinder = clFinder;
        this.status = status;
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
        int numberOfNames = this.nameElementFinder.getNumberOfNames();
        Approximately.defaultRange = numberOfNames;
        PageContactType contactType = this.clFinder.getPageContactType();
               
        // Calculate the numbers of the distinct kinds of containers we track
        int numTrs = this.nameElementFinder.getNumTrs();
        int numUls = this.nameElementFinder.getNumUls();
        int numOls = this.nameElementFinder.getNumOls();
        int numPs = this.nameElementFinder.getNumPs();
        int numDivs = this.nameElementFinder.getNumDivs();

        // Now calculate the percentage "fill" for each kind
        double namesPerTr = (double) this.nameElementFinder.getNumTrs() / (double) numberOfNames;
        double namesPerUl = (double) this.nameElementFinder.getNumUls() / (double) numberOfNames;
        double namesPerOl = (double) this.nameElementFinder.getNumOls() / (double) numberOfNames;
        double namesPerP = (double) this.nameElementFinder.getNumPs() / (double) numberOfNames;
        double namesPerDiv = (double) this.nameElementFinder.getNumDivs() / (double) numberOfNames;
        
        // See how many elements fall outside UL or OL elements
        int namesInsideTrs = this.nameElementFinder.getNameElementsByContainer("tr").size();
        int namesOutsideUls = numberOfNames - 
                this.nameElementFinder.getNameElementsByContainer("ul").size();
        int namesOutsideOls = numberOfNames - 
                this.nameElementFinder.getNameElementsByContainer("ol").size();
        
        StringBuilder sb = new StringBuilder();
        sb.append("Page statistics:\n")
                .append("Number of elements with names: ").append(numberOfNames).append("\n")
                .append("Number of <TRs>: ").append(numTrs).append("\n")
                .append("Number of <UL>s: ").append(numUls).append("\n")
                .append("Number of <OL>s: ").append(numOls).append("\n")
                .append("Number of <P>s: ").append(numPs).append("\n")
                .append("Number of <DIV>s: ").append(numDivs).append("\n")
                .append("Names per <TR>: ").append(Double.toString(namesPerTr)).append("\n")
                .append("Contact types on page: ").append(contactType).append("\n")
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
                && contactType == PageContactType.HAS_ASSOCIATED_CONTACT_INFO)
            this.pageClassification = Classification.STRUCTURED;
        else if (Approximately.equals(namesOutsideUls, 0)) // && Approximately.equals(namesPerUl, 1, 1)
                this.pageClassification = Classification.STRUCTURED;
        else if (Approximately.equals(namesOutsideOls, 0)) //  && Approximately.equals(namesPerOl, 1, 1)
                this.pageClassification = Classification.STRUCTURED;

        // See if we have an unstructured page
        else if (Approximately.equals(numTrs, numberOfNames))
            this.pageClassification = Classification.UNSTRUCTURED;
        else if (Approximately.equals(numPs, numberOfNames) ||
                (namesPerP < 1.0 && namesPerP > 0.3))
        {
            if (contactType == PageContactType.HAS_ASSOCIATED_CONTACT_INFO)
                this.pageClassification = Classification.STRUCTURED;
            else 
                this.pageClassification = Classification.UNSTRUCTURED;
        }
        else if (Approximately.equals(numDivs, numberOfNames) ||
                (namesPerDiv < 1.0 && namesPerDiv > 0.3))
            this.pageClassification = Classification.UNSTRUCTURED;
        
        // Otherwise, we give up
        else this.pageClassification = Classification.UNDETERMINED;
                
        return this.pageClassification;
    }
    

    /*public List<Element> getContainerElements() {
        return this.nameElementFinder.getContainerElements();
    }*/

    public ContactType getContactInfoType() {
        return this.contactInformationType;
    }
}
