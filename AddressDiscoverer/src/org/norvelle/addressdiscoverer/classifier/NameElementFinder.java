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
    //
    private final int numberOfNames;
    private int numTrs = 0;
    private int numUls = 0;
    private int numOls = 0;
    private int numPs = 0;
    private int numDivs = 0;
    private final HashMap<String, ArrayList<NameElement>> nameElementsByContainerTypes;
    private final HashMap<String, ArrayList<Element>> containerElementsMap;
    
    public final ArrayList<String> containerTypes = new ArrayList<String>() {
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
        this.numberOfNames = nameElements.size();
        
        // Create a mapping between container types and lists of the container 
        // themselves
        this.containerElementsMap = this.createContainerElementMap();

        // Create our mapping between container types and the name elements
        // they contain.
        this.nameElementsByContainerTypes = this.sortNameElementsByContainer();
        numTrs = this.nameElementsByContainerTypes.get("tr").size();
        numUls = this.nameElementsByContainerTypes.get("ul").size();
        numOls = this.nameElementsByContainerTypes.get("ol").size();
        numPs = this.nameElementsByContainerTypes.get("p").size();
        numDivs = this.nameElementsByContainerTypes.get("div").size();
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
        return this.nameElementsByContainerTypes.get(containerType);
    }
    
    /**
     * Return a List of all of the ContactLink objects that we have found. If a
     * contact link could not be found for a particular name-containing element,
     * we return null.
     * 
     * @return 
     */
    public List<ContactLink> getContactLinks() {
        List<ContactLink> contactLinks = new ArrayList<>();
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
        }
        
        return myNameElements;
    }

    private HashMap<String, ArrayList<NameElement>> sortNameElementsByContainer() {
        HashMap<String, ArrayList<NameElement>> myNameElementsByContainer = new HashMap<>();
        myNameElementsByContainer.put("tr", new ArrayList());
        myNameElementsByContainer.put("ul", new ArrayList());
        myNameElementsByContainer.put("ol", new ArrayList());
        myNameElementsByContainer.put("p", new ArrayList());
        myNameElementsByContainer.put("div", new ArrayList());
        
        for (NameElement nm : this.nameElements) {
            List<Element> containers = nm.getContainerElements();
            for (Element container : containers) {
                ArrayList<NameElement> myNameElements = 
                    myNameElementsByContainer.get(container.tagName());
                if (!myNameElements.contains(nm))
                    myNameElements.add(nm);
            }
        }
        return myNameElementsByContainer;
    }

    private HashMap<String, ArrayList<Element>> createContainerElementMap() {
        HashMap<String, ArrayList<Element>> myNameElementsByContainer = new HashMap<>();
        myNameElementsByContainer.put("tr", new ArrayList());
        myNameElementsByContainer.put("ul", new ArrayList());
        myNameElementsByContainer.put("ol", new ArrayList());
        myNameElementsByContainer.put("p", new ArrayList());
        myNameElementsByContainer.put("div", new ArrayList());
        
        for (NameElement nm : this.nameElements) {
            List<Element> containers = nm.getContainerElements();
            for (Element container : containers) {
                ArrayList<Element> myNameElements = 
                        myNameElementsByContainer.get(container.tagName());
                myNameElements.add(container);
            }
        }
        return myNameElementsByContainer;
    }
}
