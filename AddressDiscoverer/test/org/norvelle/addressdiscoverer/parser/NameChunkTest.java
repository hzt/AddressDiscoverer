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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
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

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class NameChunkTest {
    
    private static String html;
    private static Document soup;
    
    public NameChunkTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        Object o = new Object();
        URL htmlFileUrl = o.getClass().getResource(
                "/org/norvelle/addressdiscoverer/resources/philology_names_table.html");
        String htmlFilePath = htmlFileUrl.getPath();
        try {
            html = FileUtils.readFileToString(new File(htmlFilePath), Charset.defaultCharset());
        } catch (IOException ex) {
            fail("Encountered IOException: " + ex.getMessage());
        }
        
        soup = Jsoup.parse(html);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test to make sure that the setUpClass method loads the html properly.
     */
    @Test
    public void testHtmlLoaded() {
        Assert.assertNotNull("The .html static property should not be null", html);
        Assert.assertTrue("The length of the html should be greater than 100", html.length() > 100);
        Assert.assertTrue("The JSoup document must have at least one child node", ! soup.children().isEmpty());
    }
    
    /**
     * See if we can use JSoup to properly extract a chunk of text with a name and an email
     */
    @Test
    public void testExtractNameChunk() {
        Elements elementsWithEmails = soup.select(
                String.format("tr:matches(%s)", Parser.emailRegex));
        Assert.assertTrue("Must find at least five elements with an email", elementsWithEmails.size() > 5);

        
    }

    
}
