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

    public EmailContactLink(Element element) 
            throws DoesNotContainContactLinkException, 
            MultipleContactLinksOfSameTypeFoundException 
    {
        super(element);
        String content = element.html();
        Matcher emailMatcher = emailPattern.matcher(content);
        String matchFound = "";
        int numMatches = 0;
        ArrayList<String> emails = new ArrayList();
        
        // Extract all email addresses found
        while (emailMatcher.find()) {
            matchFound = content.substring(emailMatcher.start(), emailMatcher.end());
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
                if (!email.equals(firstEmail))
                    throw new MultipleContactLinksOfSameTypeFoundException();
        }
        
        // Otherwise, we can use the email found above.
        this.address = matchFound;
    }    
    
    @Override
    public String toString() {
        return String.format("mailto:%s", this.address);
    }
}
