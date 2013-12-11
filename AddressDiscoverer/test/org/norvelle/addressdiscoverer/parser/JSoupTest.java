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

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class JSoupTest {
    
    public JSoupTest() {
    }

    /**
     * Test of extractTables method, of class Table.
     */
    @Test
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void testFindEmailsInTable() {
        String html;
        try {
            html = Utils.loadStringFromResource(
                "/org/norvelle/addressdiscoverer/resources/fgolle.html", "");
        } catch (IOException ex) {
            fail("Encountered IOException: " + ex.getMessage());
            return;
        }
        
        String emailRegex = "(\\w+\\.)*\\w+[@](\\w+\\.)+(\\w+)";
        Document soup = Jsoup.parse(html);
        Elements elementsWithEmails = soup.select(
                String.format(":matches(%s)", emailRegex));
        Assert.assertTrue(String.format(
                "There should be exactly nine elements discovered, but %d were found", elementsWithEmails.size()),
                elementsWithEmails.size() == 9);

        String emailRegex2 = "(\\w+\\.)*\\w+[@](\\w+\\.)+(\\w+)";
        Document soup2 = Jsoup.parse(html);
        Elements trsWithEmails = soup2.select(
                String.format("tr:matches(%s)", emailRegex2));
        Assert.assertTrue(String.format(
                "There should be exactly one tr discovered, but %d were found", trsWithEmails.size()),
                trsWithEmails.size() == 1);
    }
}
