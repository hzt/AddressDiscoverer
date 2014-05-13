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
package org.norvelle.addressdiscoverer.parse.structured;

import org.norvelle.addressdiscoverer.parse.INameElement;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.DoesNotContainContactLinkException;
import org.norvelle.addressdiscoverer.exceptions.MultipleContactLinksOfSameTypeFoundException;
import org.norvelle.addressdiscoverer.model.Name;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class StructuredPageNameElement implements INameElement {
    
    private final Element nameContainingJsoupElement;
    private EmailContactLink link;
    
    public StructuredPageNameElement(Element element) {
        this.nameContainingJsoupElement = element;
    }
    
    @Override
    public void setContactLink(EmailContactLink link) {
        this.link = link;
    }
    
    @Override
    public ContactLink getContactLink() 
            throws MultipleContactLinksOfSameTypeFoundException, 
            DoesNotContainContactLinkException 
    {
        ContactLink link = ContactLinkLocator.findLinkForNameElement(this);
        return link;
    }
    
    @Override
    public Name getName() throws CantParseIndividualException {
        return new Name(this.nameContainingJsoupElement.ownText());
    }
    
    public Element getNameContainingElement() {
        return this.nameContainingJsoupElement;
    }

   
    @Override
    public String toString() {
        return this.nameContainingJsoupElement.ownText();
    }

}
