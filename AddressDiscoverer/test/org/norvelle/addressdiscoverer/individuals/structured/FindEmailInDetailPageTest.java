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
package org.norvelle.addressdiscoverer.individuals.structured;

import com.j256.ormlite.support.ConnectionSource;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class FindEmailInDetailPageTest {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;
    public static final String emailRegex = "([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+)+\\.([A-Za-z]{2,6})";
    public static final String emailRegexDomainPart = "@([A-Za-z0-9.-]+)+\\.([A-Za-z]{2,6})";
    //private static final Pattern emailPattern = Pattern.compile(Constants.emailRegex);

    public FindEmailInDetailPageTest() {
    }
    
    @Test
    public void testEmailPattern() {
        String str = "mailto:jpons@unav.es";
        boolean matches = str.matches("mailto:jpons@unav.es");
        Assert.assertTrue("String should match mailto:jpons@unav.es", matches);

        // Use traditional String.matches()
        matches = str.matches(emailRegexDomainPart);
        Assert.assertFalse("String should match email domain pattern", matches);
        matches = str.matches(emailRegex);
        Assert.assertFalse("String should match whole email pattern", matches);
        
        Pattern emailPattern = Pattern.compile(emailRegex);
        Matcher emailMatcher = emailPattern.matcher(str);
        int numMatches = 0;
        String matchFound = "";
        while (emailMatcher.find()) {
            numMatches ++;
            matchFound = str.substring(emailMatcher.start(), emailMatcher.end());
        }        
        Assert.assertEquals("There should be one match", 1, numMatches);
        Assert.assertEquals("The email should be jpons@unav.es", "jpons@unav.es", matchFound);

        /*Pattern emailPattern = Pattern.compile(emailRegex);
        Matcher emailMatcher = emailPattern.matcher(str);
        if (!emailMatcher.lookingAt()) {
            Assert.fail("Could not find email pattern in string");
        }     */   
    }

    @Test
    public void testPons() {
        String html;
        try {
            html = Utils.loadStringFromResource(
                    "/org/norvelle/addressdiscoverer/resources/individuals/EmailInDetailPage.html", "");
        } catch (IOException ex) {
            fail("Couldn't find email address: " + ex.getMessage());
            return;
        }
        //html = html.replace("mailto:", "");
        
        // Now, extract the email if we can.
        Pattern emailPattern = Pattern.compile(emailRegex);
        Matcher emailMatcher = emailPattern.matcher(html);
        int numMatches = 0;
        String matchFound = "";
        while (emailMatcher.find()) {
            numMatches ++;
            matchFound = html.substring(emailMatcher.start(), emailMatcher.end());
        }        
        Assert.assertEquals("There should be one match", 1, numMatches);
        Assert.assertEquals("The email should be jpons@unav.es", "jpons@unav.es", matchFound);
    }
    
    
}
