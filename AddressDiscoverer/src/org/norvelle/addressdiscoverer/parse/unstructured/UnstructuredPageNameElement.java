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
package org.norvelle.addressdiscoverer.parse.unstructured;

import org.norvelle.addressdiscoverer.parse.ContactLink;
import org.norvelle.addressdiscoverer.parse.structured.StructuredPageEmailContactLink;
import org.norvelle.addressdiscoverer.parse.structured.*;
import org.norvelle.addressdiscoverer.parse.INameElement;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.DoesNotContainContactLinkException;
import org.norvelle.addressdiscoverer.exceptions.MultipleContactLinksOfSameTypeFoundException;
import org.norvelle.addressdiscoverer.model.Name;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class UnstructuredPageNameElement implements INameElement {
    
    private final Element nameContainingJsoupElement;
    private StructuredPageEmailContactLink link;
    private final List<Element> intermediateElements;
    
    public UnstructuredPageNameElement(Element element, List<Element> intermediateElements) {
        this.nameContainingJsoupElement = element;
        this.intermediateElements = intermediateElements;
    }
    
    @Override
    public void setContactLink(StructuredPageEmailContactLink link) {
        this.link = link;
    }
    
    @Override
    public ContactLink getContactLink() 
            throws DoesNotContainContactLinkException,
            MultipleContactLinksOfSameTypeFoundException  
    {
        ContactLink link = UnstructuredPageContactLinkLocator.findLinkForNameElement(this);
        return link; 
    }
    
    @Override
    public Name getName() throws CantParseIndividualException {
        return new Name(this.nameContainingJsoupElement.ownText());
    }
    
    public Element getNameContainingElement() {
        return this.nameContainingJsoupElement;
    }
    
    public List<Element> getIntermediateElements() {
        return this.intermediateElements;
    }

    @Override
    public String toString() {
        return this.nameContainingJsoupElement.ownText();
    }

}
