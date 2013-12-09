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
package org.norvelle.addressdiscoverer;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.norvelle.addressdiscoverer.parser.Parser;

/**
 * Represents the contents of a table found in the HTML source code of a page.
 * Has the intelligence to figure out whether it contains emails or not.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class EmailElementFinder {
    
    private final List<Element> rows = new ArrayList<>();
    
    /**
     * Given a JSoup Document, parse it looking for TRs with emails in their
     * content, either as a text node, or else as the value of an HREF attribute.
     * The rows found by this object are available via the getRows() method.
     * 
     * @param soup A JSoup Document that is the root of a web page.
     */
    public EmailElementFinder(Document soup) {
        Elements elementsWithEmails = soup.select(
                String.format("tr:matches(%s)", Parser.emailRegex));
        for (Element element: elementsWithEmails)
            this.rows.add(element);

        Elements elementsWithEmailAttributes = soup.select(
                String.format("[href~=(%s)]", Parser.emailRegex));
        for (Element attrElement: elementsWithEmailAttributes) {
            Element trElement = this.translateToTr(attrElement);
            if (trElement != null)
                this.addIfNotPresent(attrElement);
        }
    }

    /**
     * We only want to add elements looked up by their attributes (i.e. if their
     * HREF attribute contains an email) just in case that element wasn't
     * found by the earlier sweep based on element content.
     * 
     * @param attrElement 
     */
    private void addIfNotPresent(Element attrElement) {
        Element currElement = attrElement;
        while (currElement != null && !currElement.tagName().equals("tr")) {
            currElement = currElement.parent();
        }
        
        
        // Skip if the email-containing element is not in a TR, or the TR is already there
        if (currElement != null) 
            if (!this.rows.contains(currElement))
                this.rows.add(currElement);
    }

    /**
     * Get the rows found when this object was constructed.
     * 
     * @return List<Element> TRs found during parse
     */
    public List<Element> getRows() {
        return rows;
    }

    /**
     * Given an element found in a Jsoup with an attribute containing an email,
     * move up the hierarchy to the nearest TR element and return that.
     * 
     * @param attrElement
     * @return 
     */
    private Element translateToTr(Element attrElement) {
        if (attrElement.tagName().equals("tr"))
            return attrElement;
        Element currElement = attrElement.parent();
        while (currElement != null) {
            if (currElement.tagName().equals("tr"))
                return currElement;
            currElement = currElement.parent();
        }
        return null;
        
    }
    
    
}
