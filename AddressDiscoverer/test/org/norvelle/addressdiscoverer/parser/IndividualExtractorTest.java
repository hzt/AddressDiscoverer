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

import com.j256.ormlite.support.ConnectionSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import static org.junit.Assert.*;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.Utils;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.model.Individual;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class IndividualExtractorTest {
    
    public IndividualExtractorTest() {
    }

    @Test
    public void testSomeMethod() {
        try {
            ConnectionSource connection = TestUtilities.getDBConnection("addresses.sqlite");
        } catch (SQLException | CannotLoadJDBCDriverException ex) {
            fail("Encountered problems connecting to database: " + ex.getMessage());
            return;
        }
        String html;
        try {
            html = Utils.loadStringFromResource(
                    "/org/norvelle/addressdiscoverer/resources/navarra_philology.html");
        } catch (IOException ex) {
            fail("Encountered IOException: " + ex.getMessage());
            return;
        }
        Document soup = Jsoup.parse(html);
        EmailElementFinder finder = new EmailElementFinder(soup);
        List<Element> rows = finder.getRows();

        // Instantiate an AddressExtractor see how many addresses we get.
        IndividualExtractor ext = new IndividualExtractor();
        ext.setHtml(html);
        List<Individual> individuals = ext.getIndividuals();
        
        Assert.assertEquals(String.format(
                "There should be %d individuals, %d were found", rows.size(), individuals.size()), 
                individuals.size(), 32);
    }
    
}
