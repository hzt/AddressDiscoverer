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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.norvelle.addressdiscoverer.gui.threading.ExtractIndividualsStatusReporter;
import org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException;
import org.norvelle.addressdiscoverer.model.Name;
import org.norvelle.utils.Utils;

/**
 * Given a standard tree-shaped JSoup Document, create a flattened list of
 * final elements (specifically, textual elements and emails) that can be
 * navigated from last to first in order to extract information for building
 * Individuals.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class BackwardsFlattenedDocumentIterator  
        implements Iterable<Element>, Iterator<Element> 
{
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private final List<Element> elementsWithNames = new ArrayList<>(); 
    private final List<Node> allNodes = new ArrayList<>(); 
    private int currPosition;
    private final ExtractIndividualsStatusReporter status;
    private static int counter = 0;

    /**
     * Generate the iterator and position its pointer so it can be walked backward
     * using next()
     * 
     * @param soup
     * @param encoding
     * @param status
     * @throws java.io.UnsupportedEncodingException
     * @throws org.norvelle.addressdiscoverer.exceptions.EndNodeWalkingException
     */
    public BackwardsFlattenedDocumentIterator(Document soup, String encoding, 
            ExtractIndividualsStatusReporter status) 
            throws UnsupportedEncodingException, EndNodeWalkingException 
    {
        this.status = status;
        this.status.setTotalNumericSteps(soup.getAllElements().size());
        
        // First we generate the flattened list of elements
        this.walkNodeBackwards(soup, encoding);
        this.status.reportProgressText("Backwards document iterator created successfully");
        logger.log(Level.FINE, "Flattened document: \n{0}", StringUtils.join(this.elementsWithNames, "\n"));
        
        // Now, we set the cursor to the end so we can iterate backwards
        this.currPosition = this.elementsWithNames.size() - 1;
    }
    
    /**
     * A reverse treewalker that accumulates its results in the textNodes List of nodes.
     * 
     * @param currNode 
     */
    private void walkNodeBackwards(Node currNode, String encoding) 
            throws UnsupportedEncodingException, EndNodeWalkingException 
    {
        this.status.incrementNumericProgress();
        List<Node> children = currNode.childNodes();
        for (int i = children.size() - 1; i >= 0; i --) {
            Node child = children.get(i);
            if (!child.getClass().equals(TextNode.class))
                this.walkNodeBackwards(child, encoding);
            else {
                TextNode textChild = (TextNode) child;
                String htmlEncodedString = WordUtils.capitalizeFully(textChild.getWholeText());
                String processedString = Utils.decodeHtml(htmlEncodedString, encoding);
                boolean isName;
                try {
                    counter ++;
                    if (processedString.trim().isEmpty()) isName = false;
                    else 
                        isName = Name.isName(processedString);
                }
                catch (Exception ex) {
                    logger.log(Level.SEVERE, ex.getMessage());
                    logger.log(Level.SEVERE, ExceptionUtils.getStackTrace(ex));
                    throw new EndNodeWalkingException(String.format(
                            "Could not test for nameness: %s %s", ex.getClass().getName(),
                            ex.getMessage()));
                }
                if (isName)
                    this.status.reportProgressText("Found name: " + processedString);
                if (!this.elementsWithNames.contains((Element) currNode) && isName) {
                    this.elementsWithNames.add(0, (Element) currNode);
                    /*this.status.reportProgressText(
                            String.format(" Adding <%s> with content '%s'", 
                                    currNode.nodeName(), processedString)); */
                }
            }
        }
    }

    @Override
    public boolean hasNext() {
        return this.currPosition >= 0;
    }

    @Override
    public Element next() {
        return this.elementsWithNames.get(this.currPosition --);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<Element> iterator() {
        return this;
    }
    
    public void rewind() {
        this.currPosition = this.elementsWithNames.size() - 1;
    }
    
    public int size() {
        return this.elementsWithNames.size();
    }
    
}
