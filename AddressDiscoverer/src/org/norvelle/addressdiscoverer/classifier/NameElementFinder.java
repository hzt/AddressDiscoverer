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
    
    private final List<Element> containerElements;
    private final HashMap<String, List<Element>> nameElementsByContainer;
    private final int numberOfNames;
    private int numTrs = 0;
    private int numUls = 0;
    private int numOls = 0;
    private int numPs = 0;
    private int numDivs = 0;

    public NameElementFinder(Document soup, String encoding, 
            ClassificationStatusReporter status) 
            throws UnsupportedEncodingException, EndNodeWalkingException 
    {
        BackwardsFlattenedDocumentIterator nameElements = 
                new BackwardsFlattenedDocumentIterator(soup, encoding, status);
        this.containerElements = this.findContainerElements(nameElements);
        nameElements.rewind();
        this.nameElementsByContainer = this.classifyElementsByContainer(nameElements);
        this.numberOfNames = nameElements.size();

        for (Element containerElement : this.containerElements) {
            if (containerElement.tagName().equals("tr"))
                numTrs ++;
            if (containerElement.tagName().equals("ul"))
                numUls ++;
            if (containerElement.tagName().equals("ol"))
                numOls ++;
            if (containerElement.tagName().equals("p"))
                numPs ++;
            if (containerElement.tagName().equals("div"))
                numDivs ++;
        }
    }
    
    public List<Element> getNameElementsByContainer(String containerType) {
        return this.nameElementsByContainer.get(containerType);
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

    public List<Element> getContainerElements() {
        return containerElements;
    }

    /**
     * Given a backwards document iterator, we find container elements for each of the
     * name-containing elements we find in the document.
     *
     */
    private List findContainerElements(BackwardsFlattenedDocumentIterator nameElements) {
        List<Element> containers = new ArrayList<>();
        for (Element element : nameElements) {
            List<Element> myContainerElements = this.getContainerElement(element);
            if (myContainerElements.isEmpty()) {
                continue;
            }
            for (Element containingElement : myContainerElements) {
                if (!containers.contains(containingElement)) {
                    containers.add(containingElement);
                }
            }
        }
        return containers;
    }

    private HashMap classifyElementsByContainer(BackwardsFlattenedDocumentIterator nameElements) {
        HashMap<String, List<Element>> containers = new HashMap<>();
        containers.put("tr", new ArrayList<Element>());
        containers.put("ul", new ArrayList<Element>());
        containers.put("ol", new ArrayList<Element>());
        containers.put("p", new ArrayList<Element>());
        containers.put("div", new ArrayList<Element>());
        for (Element element : nameElements) {
            List<Element> myContainerElements = this.getContainerElement(element);
            if (myContainerElements.isEmpty()) {
                continue;
            }
            for (Element containingElement : myContainerElements) {
                containers.get(containingElement.tagName()).add(element);
            }
        }
        return containers;
    }
    
    /**
     * Given an element, find the TR, UL, OL or P or DIV that most immediately contains it.
     * 
     * @param element
     * @return 
     */
    private List<Element> getContainerElement(Element element) {
        List<Element> myContainerElements = new ArrayList<>();
        
        // First, see if we can find a TR... giving TRs priority over Ps and other containers
        Element currElement = element.parent();
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
        if (element.tagName().equals("p") || element.tagName().equals("div") 
                || element.tagName().equals("ul")
                || element.tagName().equals("ol"))
            otherContainer = element;
        currElement = element.parent();
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

    
}
