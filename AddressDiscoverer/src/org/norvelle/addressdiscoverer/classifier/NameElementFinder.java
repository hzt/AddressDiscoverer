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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException;

/**
 * Handles finding elements with identifiable names in a given HTML page
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class NameElementFinder {
    
    private final List<NameElement> nameElements;
    private final HashMap<NameElement, String> nameElementsAndContainerTypes;
    private final int numberOfNames;
    private int numTrs = 0;
    private int numUls = 0;
    private int numOls = 0;
    private int numPs = 0;
    private int numDivs = 0;
    private HashMap<String, List<Element>> containerTypesWithContainerElements;
    
    private final ArrayList<String> containerTypes = new ArrayList<String>() {
        {
            add("tr");
            add("ul");
            add("ol");
            add("p");
            add("div");
        }
    };

    public NameElementFinder(Document soup, String encoding, 
            ClassificationStatusReporter status) 
            throws UnsupportedEncodingException, EndNodeWalkingException 
    {
        BackwardsFlattenedDocumentIterator nameNodes = 
                new BackwardsFlattenedDocumentIterator(soup, encoding, status);
        this.nameElements = this.generateNameElements(nameNodes);
        this.nameElementsAndContainerTypes = new HashMap<>();
        this.numberOfNames = nameElements.size();
        
        for (NameElement nameElement : nameElements) {
            if (nameElement.getContainerElement().tagName().equals("tr"))
                numTrs ++;
            if (nameElement.getContainerElement().tagName().equals("ul"))
                numUls ++;
            if (nameElement.getContainerElement().tagName().equals("ol"))
                numOls ++;
            if (nameElement.getContainerElement().tagName().equals("p"))
                numPs ++;
            if (nameElement.getContainerElement().tagName().equals("div"))
                numDivs ++;
        }
        
        // Initialize our mapping between container types and the name elements
        // they contain.
        for (String containerType : this.containerTypes) 
            this.containerTypesWithContainerElements.put(containerType, new ArrayList<NameElement>());
    }
        
    public int getNumberOfNames() {
        return this.numberOfNames;
    }

    public int getNumTrs() {
        return numTrs;
    }

    public int getNumUls() {
        return numUls;
    }

    public int getNumOls() {
        return numOls;
    }

    public int getNumPs() {
        return numPs;
    }

    public int getNumDivs() {
        return numDivs;
    }
    
    /**
     * Given a container type, find and return all the NameElements that have that
     * container type as an ancestor.
     * 
     * @param containerType String identifying the container type
     * @return List<NameElement> A List of the NameElements that have that container type as an ancestor.
     */
    public List<NameElement> getNameElementsByContainer(String containerType) {
        List<NameElement> nameElementsForContainer = new ArrayList<>();
        for (NameElement nameElement : this.nameElementsAndContainerTypes.keySet()) {
            if (nameElement.getContainerElement().tagName().equals(containerType))
                nameElementsForContainer.add(nameElement);
        }
        return nameElementsForContainer;
    }
    
    /**
     * Return a List of all of the ContactLink objects that we have found. If a
     * contact link could not be found for a particular name-containing element,
     * we return null.
     * 
     * @return 
     */
    public List<ContactLink> getContactLinks() {
        List<ContactLink> contactLinks = new ArrayList<ContactLink>();
        for (NameElement nameElement : this.nameElements) {
            ContactLink link = nameElement.getContactLink();
            if (link != null)
                contactLinks.add(link);
        }
        return contactLinks;
    }

    /**
     * Given the BackwardsFlattenedDocumentIterator that holds our Jsoup Element
     * objects that have names as their contents, generate our intelligent
     * NameElement objects for each of them, and return these new objects in
     * a list.
     * 
     * @param jsoupNameElementIterator
     * @return 
     */
    private List<NameElement> generateNameElements(
            BackwardsFlattenedDocumentIterator jsoupNameElementIterator) 
    {
        List<NameElement> myNameElements = new ArrayList<>();
        
        for (Element jsoupNameElement : jsoupNameElementIterator) {
            NameElement nameElement = new NameElement(jsoupNameElement);
            myNameElements.add(nameElement);
            this.nameElementsAndContainerTypes.put(nameElement, 
                    nameElement.getContainerElement().tagName());
            List<Element> containerElementsForContainerType = 
                this.containerTypesWithContainerElements.get(
                    nameElement.getContainerElement().tagName());
            containerElementsForContainerType.add(nameElement.getContainerElement());
        }
        
        return myNameElements;
    }
}
