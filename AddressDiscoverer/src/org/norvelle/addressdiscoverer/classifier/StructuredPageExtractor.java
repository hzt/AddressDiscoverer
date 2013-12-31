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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import org.norvelle.addressdiscoverer.classifier.ClassificationStatusReporter.ClassificationStages;
import org.norvelle.addressdiscoverer.classifier.ContactLink.ContactType;
import org.norvelle.addressdiscoverer.exceptions.CannotStoreNullIndividualException;
import org.norvelle.addressdiscoverer.exceptions.IndividualHasNoDepartmentException;
import org.norvelle.addressdiscoverer.model.Individual;

/**
 * For a structured page, use its pattern of listing individuals in order to
 * obtain a complete list of all the individuals (and not just those with identifiable
 * name) together with their contact details and extra information.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class StructuredPageExtractor extends IndividualExtractor {
    
    /**
     * Given a page that has been determined to have a regular container-based
     * structure, find the Individuals that are described in it.
     * 
     * @param nameFinder
     * @param clFinder
     * @param status
     * @throws SQLException
     * @throws IndividualHasNoDepartmentException
     * @throws CannotStoreNullIndividualException 
     */
    public StructuredPageExtractor(NameElementFinder nameFinder, 
            ContactLinkFinder clFinder, ClassificationStatusReporter status) 
            throws SQLException, IndividualHasNoDepartmentException, 
            CannotStoreNullIndividualException 
    {
        super(nameFinder, clFinder, status);
        NameElementPath path = nameFinder.getPathToNameElements();
        List<NameElement> nameElements = this.gatherNameElements(path);
        
        // Find the contact info for the names found, and if necessary,
        // fetch the email from the weblink provided.
        HashMap<NameElement, ContactLink> namesToContacts = new HashMap<>();
        status.setStage(ClassificationStages.FETCHING_EMAILS_FROM_WEBLINKS);
        status.setTotalNumericSteps(nameElements.size());
        for (NameElement ne : nameElements) {
            ContactLink cl = clFinder.findContactLinkForNameElement(ne);
            if (cl.getType() == ContactType.LINK_TO_DETAIL_PAGE) 
                cl.fetchEmailFromWeblink();  
            status.incrementNumericProgress();
        }
        
        // Create and store the Individual records we create on the basis of
        // the information found.
        this.individuals = this.createIndividuals(nameElements, namesToContacts);
        for (Individual i : individuals) {
            Individual.store(i);
        }
    }

    private List<NameElement> gatherNameElements(NameElementPath path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private List<Individual> createIndividuals(List<NameElement> nameElements, 
            HashMap<NameElement, ContactLink> namesToContacts) 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
