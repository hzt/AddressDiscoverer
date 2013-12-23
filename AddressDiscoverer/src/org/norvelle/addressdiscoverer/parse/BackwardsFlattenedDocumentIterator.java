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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.norvelle.addressdiscoverer.Constants;
import org.norvelle.utils.Utils;

/**
 * Given a standard tree-shaped JSoup Document, create a flattened list of
 * final elements (specifically, textual elements and emails) that can be
 * navigated from last to first in order to extract information for building
 * Individuals.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class BackwardsFlattenedDocumentIterator implements Iterable<String>, Iterator<String> {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private final List<String> textNodes = new ArrayList<>(); 
    private final Pattern emailPattern = Pattern.compile(Constants.emailRegex);
    private int currPosition;

    public BackwardsFlattenedDocumentIterator(Document soup, String encoding) 
            throws UnsupportedEncodingException 
    {
        // First we generate the flattened list of elements
        this.walkNodeBackwards(soup, encoding);
        logger.log(Level.FINE, "Flattened document: \n" + StringUtils.join(this.textNodes, "\n"));
        
        // Now, we set the cursor to the end so we can iterate backwards
        this.currPosition = this.textNodes.size() - 1;
    }
    
    /**
     * A reverse treewalker that accumulates its results in the textNodes List of nodes.
     * 
     * @param currNode 
     */
    private void walkNodeBackwards(Node currNode, String encoding) 
            throws UnsupportedEncodingException 
    {
        List<Node> children = currNode.childNodes();
        for (int i = children.size() - 1; i >= 0; i --) {
            Node child = children.get(i);
            this.walkNodeBackwards(child, encoding);
        }
        if (currNode.hasAttr("href") && 
                emailPattern.matcher(currNode.attr("href")).matches()) 
        {
            this.textNodes.add(0, currNode.attr("href"));
            return;
        }
        if (currNode.getClass().equals(TextNode.class) && !currNode.toString().trim().isEmpty()) {
            String htmlEncodedString = currNode.toString();
            String processedString = Utils.decodeHtml(htmlEncodedString, encoding);
            if (!processedString.trim().isEmpty())
                this.textNodes.add(0, processedString.trim());
        }
    }

    @Override
    public boolean hasNext() {
        return this.currPosition >= 0;
    }

    @Override
    public String next() {
        return this.textNodes.get(this.currPosition --);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<String> iterator() {
        return this;
    }
    
}
