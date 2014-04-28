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
            add("td");
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
        numTrs = this.nameElementsByContainerTypes.get("td").size();
        numUls = this.nameElementsByContainerTypes.get("ul").size();
        numOls = this.nameElementsByContainerTypes.get("ol").size();
        numPs = this.nameElementsByContainerTypes.get("p").size();
        numDivs = this.nameElementsByContainerTypes.get("div").size();
    }
        
    public int getNumberOfNames() {
        return this.numberOfNames;
    }
    
    public List<NameElement> getNameElements() {
        return this.nameElements;
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
    
    public NameElementPath getPathToNameElements() {
        HashMap<String, Integer> namePathsToFrequencies = new HashMap<>();
        HashMap<String, NameElementPath> namePathsBySignatures = new HashMap<>();
        for (NameElement nm : this.getNameElements()) {
            NameElementPath path = new NameElementPath(nm);
            String signature = path.getSignature();
            namePathsBySignatures.put(signature, path);
            int currScore;
            if (namePathsToFrequencies.containsKey(signature))
                currScore = namePathsToFrequencies.get(signature);
            else currScore = 0;
            namePathsToFrequencies.put(signature, currScore + 1);
        }
        
        // Now, find the path with the highest score and return that.
        int highScore = 0;
        NameElementPath highestScoringPath = null;
        for (String signature : namePathsToFrequencies.keySet()) {
            int currScore = namePathsToFrequencies.get(signature);
            if (currScore > highScore) {
                highScore = currScore;
                highestScoringPath = namePathsBySignatures.get(signature);
            }
        }
        return highestScoringPath;
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
        myNameElementsByContainer.put("td", new ArrayList());
        myNameElementsByContainer.put("ul", new ArrayList());
        myNameElementsByContainer.put("ol", new ArrayList());
        myNameElementsByContainer.put("p", new ArrayList());
        myNameElementsByContainer.put("div", new ArrayList());
        
        for (NameElement nm : this.nameElements) {
            Element container = nm.getContainer();
            ArrayList<NameElement> myNameElements = 
                myNameElementsByContainer.get(container.tagName());
            if (!myNameElements.contains(nm))
                myNameElements.add(nm);
        }
        return myNameElementsByContainer;
    }

    private HashMap<String, ArrayList<Element>> createContainerElementMap() {
        HashMap<String, ArrayList<Element>> myNameElementsByContainer = new HashMap<>();
        myNameElementsByContainer.put("td", new ArrayList());
        myNameElementsByContainer.put("ul", new ArrayList());
        myNameElementsByContainer.put("ol", new ArrayList());
        myNameElementsByContainer.put("p", new ArrayList());
        myNameElementsByContainer.put("div", new ArrayList());
        
        for (NameElement nm : this.nameElements) {
            Element container = nm.getContainer();
            ArrayList<Element> myNameElements = 
                    myNameElementsByContainer.get(container.tagName());
            myNameElements.add(container);
        }
        return myNameElementsByContainer;
    }
}
