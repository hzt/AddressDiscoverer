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

import java.util.ArrayList;
import java.util.regex.Matcher;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.exceptions.DoesNotContainContactLinkException;
import org.norvelle.addressdiscoverer.exceptions.MultipleContactLinksOfSameTypeFoundException;

/**
 * Given a name-containing Jsoup element, search it and its children for an email
 * address. Throw an exception if either nothing is found, or more than one email
 * is found (which would indicate an unstructured page.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class EmailContactLink extends ContactLink {

    /**
     * Try to find an email address in both the HTML (so that we can get attributes
     * of elements) as well as in the plain text (in case the HTML has been scrambled
     * to obfuscate the address).
     * 
     * @param element
     * @throws DoesNotContainContactLinkException
     * @throws MultipleContactLinksOfSameTypeFoundException 
     */
    public EmailContactLink(Element element) 
            throws DoesNotContainContactLinkException, 
            MultipleContactLinksOfSameTypeFoundException 
    {
        super(element);
        String content = element.html();
        try {
            this.address = this.findLinkInString(content);
        }
        catch (DoesNotContainContactLinkException ex) {
            content = element.text();
            this.address = this.findLinkInString(content);
        }
    }
    
    /**
     * Given a chunk of text, see if we can't find a single email address in it.
     * If there are multiple instances of the same address, that's fine, but if there
     * are multiple different emails we raise an exception.
     * 
     * @param str
     * @return
     * @throws DoesNotContainContactLinkException
     * @throws MultipleContactLinksOfSameTypeFoundException 
     */
    private String findLinkInString(String str) 
            throws DoesNotContainContactLinkException, 
            MultipleContactLinksOfSameTypeFoundException 
    {
        Matcher emailMatcher = emailPattern.matcher(str);
        String matchFound = "";
        int numMatches = 0;
        ArrayList<String> emails = new ArrayList();
        
        // Extract all email addresses found
        while (emailMatcher.find()) {
            matchFound = str.substring(emailMatcher.start(), emailMatcher.end());
            emails.add(matchFound);
            numMatches ++;
        }      
        
        // Throw exception if nothing found.
        if (numMatches == 0)
            throw new DoesNotContainContactLinkException(); 

        // If we have multiple emails that are distinct, we throw an exception
        if (numMatches > 1) {
            String firstEmail = emails.get(0);
            for (String email : emails) 
                if (!email.equals(firstEmail)) {
                    String emailStr = StringUtils.join(emails, ", ");
                    if (emailStr.length() > 100)
                        emailStr = emailStr.substring(0, 99);
                    throw new MultipleContactLinksOfSameTypeFoundException(emailStr);
                }
        }
        
        // Otherwise, we can use the email found above.
        return matchFound;
    }    
    
    @Override
    public String toString() {
        return String.format("mailto:%s", this.address);
    }
}
