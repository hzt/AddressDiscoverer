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

import org.norvelle.addressdiscoverer.parse.INameElementFinder;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.gui.threading.ExtractIndividualsStatusReporter;
import org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException;
import org.norvelle.addressdiscoverer.parse.INameElement;

/**
 * Handles finding elements with identifiable names in a given HTML page
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class UnstructuredNameElementFinder implements INameElementFinder {
    
    private final List<INameElement> nameElements;
    private final int numberOfNames;
    private final ForwardsFlattenedDocumentIterator nameNodes;
    
    public UnstructuredNameElementFinder(Document soup, String encoding, 
            ExtractIndividualsStatusReporter status) 
            throws UnsupportedEncodingException, EndNodeWalkingException 
    {
        nameNodes = new ForwardsFlattenedDocumentIterator(soup, encoding, status);
        this.nameElements = this.generateNameElements(nameNodes);
        this.numberOfNames = nameElements.size();
    }
        
    public int getNumberOfNames() {
        return this.numberOfNames;
    }
    
    public List<INameElement> getNameElements() {
        return this.nameElements;
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
    private List<INameElement> generateNameElements(
            ForwardsFlattenedDocumentIterator jsoupNameElementIterator) 
    {
        List<INameElement> myNameElements = new ArrayList<>();
        
        for (Element jsoupNameElement : jsoupNameElementIterator) {
            List<String> intermediateValues = this.nameNodes.getIntermediateElementMap(jsoupNameElement);
            UnstructuredPageNameElement nameElement = 
                    new UnstructuredPageNameElement(jsoupNameElement, intermediateValues);
            myNameElements.add(nameElement);
        }
        
        return myNameElements;
    }

}
