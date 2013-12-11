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
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.Name;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class NameChunkTest {

    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static ConnectionSource connection;
    private static String html;
    private static Document soup;

    public NameChunkTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        TestUtilities.setupLogger();
        try {
            connection = TestUtilities.getDBConnection("addresses.test.sqlite");
        } catch (SQLException | CannotLoadJDBCDriverException ex) {
            fail("Encountered problems connecting to database: " + ex.getMessage());
            return;
        }
    }

    /**
     * Test to make sure that the setUpClass method loads the html properly.
     */
    //@Test
    public void testHtmlLoaded() {
        Assert.assertNotNull("The .html static property should not be null", html);
        Assert.assertTrue("The length of the html should be greater than 100", html.length() > 100);
        Assert.assertTrue("The JSoup document must have at least one child node", !soup.children().isEmpty());
    }

    /**
     * See if we can use JSoup to properly extract a chunk of text with a name
     * and an email
     *
     */
    //@Test
    public void testParensRegex() {
        Pattern pp = Pattern.compile("\\(.*\\)");
        Matcher matcherFirst = pp.matcher("José María (en ICS)");
        while (matcherFirst.find()) {
            System.out.print("Start index: " + matcherFirst.start());
            System.out.print(" End index: " + matcherFirst.end() + " ");
            System.out.println(matcherFirst.group());
        }
        Assert.assertTrue("José María (en ICS) must contain a parentheses group",
                matcherFirst.matches());
    }

    //@Test
    public void testVogelRegex() {
        final String EXAMPLE_TEST = "This is my small example string which I'm going to use for pattern matching.";

        Pattern pattern = Pattern.compile("\\w+");
    // in case you would like to ignore case sensitivity,
        // you could use this statement:
        // Pattern pattern = Pattern.compile("\\s+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(EXAMPLE_TEST);
        // check all occurance
        while (matcher.find()) {
            System.out.print("Start index: " + matcher.start());
            System.out.print(" End index: " + matcher.end() + " ");
            System.out.println(matcher.group());
        }
        // now create a new pattern and matcher to replace whitespace with tabs
        Pattern replace = Pattern.compile("\\s+");
        Matcher matcher2 = replace.matcher(EXAMPLE_TEST);
        System.out.println(matcher2.replaceAll("\t"));
    }

    @Test
    public void testTextWithParentheses() {
        String text = "Torralba, José María (en ICS)";
        BasicNameChunkHandler handler = new BasicNameChunkHandler();
        Name name;
        try {
            name = handler.processChunkForName(text);
        } catch (SQLException | OrmObjectNotConfiguredException | CantParseIndividualException ex) {
            fail("Name handler failed: " + ex.getMessage());
            return;
        }
        Assert.assertEquals("The individual's first name should be José María",
                "José María", name.getFirstName());
        Assert.assertEquals("The individual's title should be ''",
                "", name.getTitle());
        Assert.assertEquals("The individual's last name should be Torralba",
                "Torralba", name.getLastName());
        Assert.assertEquals("The remaining text should be ''",
                "(en ICS)", name.getRest());
    }

}
