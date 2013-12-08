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
package org.norvelle.addressdiscoverer.parser;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Represents the contents of a table found in the HTML source code of a page.
 * Has the intelligence to figure out whether it contains emails or not.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class EmailElementFinder {
    
    private List<Element> rows = new ArrayList<>();
    
    public EmailElementFinder(Document soup) {
        Elements elementsWithEmails = soup.select(
                String.format("tr:matches(%s)", Parser.emailRegex));
        for (Element element: elementsWithEmails)
            this.rows.add(element);

        Elements elementsWithEmailAttributes = soup.select(
                String.format("[href~=(%s)]", Parser.emailRegex));
        for (Element attrElement: elementsWithEmailAttributes)
            this.addIfNotPresent(attrElement);
    }

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

    public List<Element> getRows() {
        return rows;
    }
    
    
}
