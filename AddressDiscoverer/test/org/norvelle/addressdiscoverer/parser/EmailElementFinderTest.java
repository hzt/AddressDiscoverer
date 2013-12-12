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

import org.norvelle.addressdiscoverer.parse.EmailElementFinder;
import java.io.IOException;
import java.util.List;
import junit.framework.Assert;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import static org.junit.Assert.*;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class EmailElementFinderTest {
    
    public EmailElementFinderTest() {
    }
    
    /**
     * Test of extractTables method, of class Table.
     */
    @Test
    public void testExtractTables() {
        String html;
        try {
            html = Utils.loadStringFromResource(
                    "/org/norvelle/addressdiscoverer/resources/navarra_philology.html", "");
        } catch (IOException ex) {
            fail("Encountered IOException: " + ex.getMessage());
            return;
        }
        
        // Test building a JSoup document
        Document soup = Jsoup.parse(html);
        Assert.assertTrue("The JSoup document must have at least one child node", ! soup.children().isEmpty());
        
        // Test the EmailElementFinder
        EmailElementFinder finder = new EmailElementFinder(soup);
        List<Element> rows = finder.getRows();
        
        Assert.assertTrue(String.format(
                "There should be more than 10 emails found, but %d were found", 
                rows.size()), rows.size() > 10);

        // Make sure that the Pasamar tr has been found
        boolean found = false;
        boolean found2 = false;
        for (Element element: rows) {
            if (element.select(":contains(Pasamar)").size() > 0)
                found = true;
            
            if (element.text().contains("Pasamar"))
                found2 = true;
        }
        Assert.assertTrue("2. The Pasamar record should have been found, but was not", found2);
        Assert.assertTrue("1. The Pasamar record should have been found, but was not", found);
    }
    
}
