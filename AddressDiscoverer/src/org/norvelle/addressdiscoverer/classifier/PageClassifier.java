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
import org.norvelle.addressdiscoverer.classifier.ClassificationStatusReporter.ClassificationStages;
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
    private List<Element> containerElements;
    private Classification pageClassification;
    
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
            throws UnsupportedEncodingException, EndNodeWalkingException 
    {
        BackwardsFlattenedDocumentIterator nameElements = this.getNameElements();
        this.containerElements = this.findContainerElements(nameElements);
        nameElements.rewind();
        HashMap<String, List<Element>> elementsByContainer = this.classifyElementsByContainer(nameElements);
        
        // Calculate the numbers of the distinct kinds of containers we track
        Approximately.defaultRange = nameElements.size();
        int numTrs = 0;
        int numUls = 0;
        int numOls = 0;
        int numPs = 0;
        int numDivs = 0;
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
        
        // Now calculate the percentage "fill" for each kind
        double elementsPerTr = (double) numTrs / (double) nameElements.size();
        double elementsPerUl = (double) numUls / (double) nameElements.size();
        double elementsPerOl = (double) numOls / (double) nameElements.size();
        double elementsPerP = (double) numPs / (double) nameElements.size();
        double elementsPerDiv = (double) numDivs / (double) nameElements.size();
        
        // See how many elements fall outside UL or OL elements
        int elementsInsideTrs = elementsByContainer.get("tr").size();
        int elementsOutsideUls = nameElements.size() - elementsByContainer.get("ul").size();
        int elementsOutsideOls = nameElements.size() - elementsByContainer.get("ol").size();
        
        StringBuilder sb = new StringBuilder();
        sb.append("Page statistics:\n")
                .append("Number of elements with names: ").append(nameElements.size()).append("\n")
                .append("Number of <TRs>: ").append(numTrs).append("\n")
                .append("Number of <UL>s: ").append(numUls).append("\n")
                .append("Number of <OL>s: ").append(numOls).append("\n")
                .append("Number of <P>s: ").append(numPs).append("\n")
                .append("Number of <DIV>s: ").append(numDivs).append("\n")
                .append("Elements per <TR>: ").append(Double.toString(elementsPerTr)).append("\n")
                .append("Elements per <UL>: ").append(Double.toString(elementsPerUl)).append("\n")
                .append("Elements per <OL>: ").append(Double.toString(elementsPerOl)).append("\n")
                .append("Elements inside <TR>s: ").append(elementsInsideTrs).append("\n")
                .append("Elements outside <UL>s: ").append(elementsOutsideUls).append("\n")
                .append("Elements outside <OL>: ").append(elementsOutsideOls).append("\n")
                .append("Ratio of <P>s to total elements: ")
                    .append(Double.toString(elementsPerP)).append("\n")
                .append("Ratio of <DIV>s to total elements: ")
                    .append(Double.toString(elementsPerDiv)).append("\n");
        this.status.reportProgressText(sb.toString());

        // See if we have a page structured into natural divisions
        if (Approximately.equals(elementsInsideTrs, nameElements.size()) 
                && (elementsPerTr <= 1.0 && elementsPerTr > 0.3))
                this.pageClassification = Classification.TR_STRUCTURED_PAGE;
        else if (Approximately.equals(elementsOutsideUls, 0) 
                 && Approximately.equals(elementsPerUl, 1, 1)) // || Approximately.equals(elementsOutsideOls, 0)
                this.pageClassification = Classification.UL_STRUCTURED_PAGE;
        else if (Approximately.equals(elementsOutsideOls, 0) 
                && Approximately.equals(elementsPerOl, 1, 1)) // || Approximately.equals(elementsOutsideOls, 0)
                this.pageClassification = Classification.OL_STRUCTURED_PAGE;
       
        // See if we have an unstructured page
        else if (Approximately.equals(numTrs, nameElements.size()))
            this.pageClassification = Classification.UNSTRUCTURED_TR_PAGE;
        else if (Approximately.equals(numPs, nameElements.size()) ||
                (elementsPerP < 1.0 && elementsPerP > 0.3))
            this.pageClassification = Classification.UNSTRUCTURED_P_PAGE;
        else if (Approximately.equals(numDivs, nameElements.size()) ||
                (elementsPerDiv < 1.0 && elementsPerDiv > 0.3))
            this.pageClassification = Classification.UNSTRUCTURED_DIV_PAGE;
        
        // Otherwise, we give up
        else this.pageClassification = Classification.UNDETERMINED;
        
        return this.pageClassification;
    }
    
    /**
     * Walk the document tree backwards and create an iterator that contains all
     * of the Elements whose contents are flagged as names.
     * 
     * @return
     * @throws UnsupportedEncodingException 
     * @throws org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException 
     */
    public BackwardsFlattenedDocumentIterator getNameElements() 
            throws UnsupportedEncodingException, EndNodeWalkingException 
    {
        BackwardsFlattenedDocumentIterator iterator = 
                new BackwardsFlattenedDocumentIterator(this.soup, this.encoding, this.status);
        return iterator;
    }
    
    private HashMap<String, List<Element>> classifyElementsByContainer(BackwardsFlattenedDocumentIterator nameElements) {
        HashMap<String, List<Element>> containers = new HashMap<>();
        containers.put("tr", new ArrayList<Element>());
        containers.put("ul", new ArrayList<Element>());
        containers.put("ol", new ArrayList<Element>());
        containers.put("p", new ArrayList<Element>());
        containers.put("div", new ArrayList<Element>());
        for (Element element : nameElements) {
            Element containingElement = this.getContainerElement(element);
            
            // Elements not in one of the approved containers are ignored.
            if (containingElement == null)
                continue;
            
            // Otherwise, we store the container element if it's not already there.
            containers.get(containingElement.tagName()).add(element);
        }
        return containers;
    }

    /**
     * Given a backwards document iterator, we find container elements for each of the
     * name-containing elements we find in the document.
     * 
     */
    private List<Element> findContainerElements(BackwardsFlattenedDocumentIterator nameElements) {
        List<Element> containers = new ArrayList<>();
        for (Element element : nameElements) {
            Element containingElement = this.getContainerElement(element);
            
            // Elements not in one of the approved containers are ignored.
            if (containingElement == null)
                continue;
            
            // Otherwise, we store the container element if it's not already there.
            if (!containers.contains(containingElement))
                containers.add(containingElement);
        }
        return containers;
    }

    /**
     * Given an element, find the TR, UL, OL or P or DIV that most immediately contains it.
     * 
     * @param element
     * @return 
     */
    private Element getContainerElement(Element element) {
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
                    || element.tagName().equals("ul")
                    || element.tagName().equals("ol"))
                {
                    otherContainer = currElement;
                    break;
                }
                currElement = currElement.parent();
            }
        
        // Now, return the element that best fits the criterion of being the parent
        if (trContainer == null && otherContainer == null)
            return null;
        if (trContainer != null && otherContainer == null)
            return trContainer;
        if (trContainer == null && otherContainer != null)
            return otherContainer;
        if (this.isParentOf(trContainer, otherContainer))
            return otherContainer;
        
        return trContainer;         
    }

    public List<Element> getContainerElements() {
        return containerElements;
    }

    private boolean isParentOf(Element trContainer, Element otherContainer) {
        Element currElement = otherContainer;
        while (currElement != null) {
            if (currElement == trContainer) 
                return true;
            currElement = currElement.parent();
        }
        return false;
    }

    
}
