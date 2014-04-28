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
import java.util.List;
import org.jsoup.nodes.Document;
import org.norvelle.addressdiscoverer.exceptions.CannotStoreNullIndividualException;
import org.norvelle.addressdiscoverer.exceptions.IndividualHasNoDepartmentException;
import org.norvelle.addressdiscoverer.model.Individual;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public abstract class IndividualExtractor {
    
    protected List<Individual> individuals;
    protected NameElementFinder nameFinder;
    protected ContactLinkFinder clFinder;
    protected ClassificationStatusReporter status;
    protected Document soup;
    
    public IndividualExtractor(Document soup, NameElementFinder nameFinder, 
            ContactLinkFinder clFinder, ClassificationStatusReporter status) 
    {
        this.soup = soup;
        this.nameFinder = nameFinder;
        this.clFinder = clFinder;
        this.status = status;
    }
    
    public abstract void extract() throws SQLException, IndividualHasNoDepartmentException, 
            CannotStoreNullIndividualException;

    public List<Individual> getIndividuals() {
        return individuals;
    }
    
}
