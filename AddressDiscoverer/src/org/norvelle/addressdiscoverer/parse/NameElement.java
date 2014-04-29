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
package org.norvelle.addressdiscoverer.parse;

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
public class NameElement {
    
    private final Element nameContainingJsoupElement;
    private EmailContactLink link;
    
    public NameElement(Element element) {
        this.nameContainingJsoupElement = element;
    }
    
    public void setContactLink(EmailContactLink link) {
        this.link = link;
    }
    
    public ContactLink getContactLink() 
            throws MultipleContactLinksOfSameTypeFoundException, 
            DoesNotContainContactLinkException 
    {
        ContactLink link = ContactLinkLocator.findLinkForNameElement(this);
        return link;
    }
    
    public Name getName() throws CantParseIndividualException {
        return new Name(this.nameContainingJsoupElement.ownText());
    }
    
    public Element getNameContainingElement() {
        return this.nameContainingJsoupElement;
    }

    /**
     * Extract the route from the indicated name containing element to the
     * container that is its ancestor.
     * 
     * @return 
     */
    public List<String> getPathToContainer() {
        Element container = this.getContainer();
        ArrayList<String> path = new ArrayList<>();
        path.add(0, this.nameContainingJsoupElement.tagName());
        Element currElement = this.nameContainingJsoupElement;
        boolean finished = false;
        while (!finished) {
            currElement = currElement.parent();
            path.add(0, currElement.tagName());
            if (currElement == container)
                finished = true;
        }
        return path;
    }
    
    /**
     * Find the container element (TR, UL, OL, P or DIV) that is the first
     * such director ancestor of the name-containing element.
     * 
     * @return 
     */
    public Element getContainer() {
        List<Element> possibleContainers = this.locateContainerElements();
        
        if (possibleContainers.isEmpty())
            return null;
        
        Element el1 = possibleContainers.get(0);
        if (possibleContainers.size() == 1)
            return el1;
        for (Element otherElement : possibleContainers) {
            if (otherElement.equals(el1))
                continue;
            if (isElementOneAncestorOfElementTwo(otherElement, el1))
                return otherElement;
            else return el1;
        }
        return null;
    }
    
    /**
     * See if one Element has another as its ancestor.
     * 
     * @param element1
     * @param element2
     * @return 
     */
    public static boolean isElementOneAncestorOfElementTwo(Element element1, Element element2) {
        if (element1 == element2)
            return false;
        Element currElement = element2;
        while (currElement != null) {
            if (currElement.parent() == element1)
                return true;
            currElement = currElement.parent();
        }
        
        return false;
    }

    /**
     * Given an element, find the TR, UL, OL or P or DIV that most immediately contains it.
     * 
     * @return 
     */
    private List<Element> locateContainerElements() {
        List<Element> myContainerElements = new ArrayList<>();
        
        // First, see if we can find a TR... giving TRs priority over Ps and other containers
        Element currElement = nameContainingJsoupElement.parent();
        Element trContainer = null;
        while (currElement != null) {
            if (currElement.tagName().equals("tr")) {
                trContainer = currElement;
                break;
            }
            currElement = currElement.parent();
        }
        
        // Next we check for a P, UL, OL or DIV that contains the current element
        Element otherContainer = null;
        if (nameContainingJsoupElement.tagName().equals("p") 
                || nameContainingJsoupElement.tagName().equals("div") 
                || nameContainingJsoupElement.tagName().equals("ul")
                || nameContainingJsoupElement.tagName().equals("ol")
           ) 
        {
            otherContainer = nameContainingJsoupElement;
        }
        currElement = nameContainingJsoupElement.parent();
        if (otherContainer == null)
            while (currElement != null) {
                if (currElement.tagName().equals("p") || currElement.tagName().equals("div") 
                    || currElement.tagName().equals("ul")
                    || currElement.tagName().equals("ol"))
                {
                    otherContainer = currElement;
                    break;
                }
                currElement = currElement.parent();
            }
        
        // Now, return the element that best fits the criterion of being the parent
        if (trContainer == null && otherContainer == null)
            return myContainerElements;
        if (trContainer != null)
            myContainerElements.add(trContainer);
        if (otherContainer != null)
            myContainerElements.add(otherContainer);
        
        return myContainerElements;         
    }

    @Override
    public String toString() {
        return this.nameContainingJsoupElement.ownText();
    }

}
