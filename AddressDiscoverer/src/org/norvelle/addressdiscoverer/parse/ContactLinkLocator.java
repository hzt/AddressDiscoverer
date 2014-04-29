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

import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.exceptions.DoesNotContainContactLinkException;
import org.norvelle.addressdiscoverer.exceptions.MultipleContactLinksOfSameTypeFoundException;

/**
 * Specialist static class that knows how to locate associated contact links
 * for a given NameElement. It works by climbing up the Jsoup Element tree
 * looking for a given kind of link (email or http), giving priority to email
 * links. It returns the best link it can find, or throw an exception if no 
 * links are found.
 * 
 * @author enorvelle
 */
public class ContactLinkLocator {
    
    public static ContactLink findLinkForNameElement(NameElement nm) 
            throws MultipleContactLinksOfSameTypeFoundException, DoesNotContainContactLinkException 
    {
        int i = 0;
        Element currElement = nm.getNameContainingElement();
        
        // We check up to two levels up from where the name was found.
        while (i < 5) {
            try {
                EmailContactLink link = new EmailContactLink(currElement);
                return link;
            } catch (DoesNotContainContactLinkException ex) {
                currElement = currElement.parent();
                i ++;
            }
        } // while (i < 3) {
        
        // Now check for href elements.
        currElement = nm.getNameContainingElement();
        i = 0;
        while (i < 5) {
            try {
                WebContactLink link = new WebContactLink(currElement);
                return link;
            } catch (DoesNotContainContactLinkException ex) {
                currElement = currElement.parent();
                i ++;
            }
        } // while (i < 3) {

        throw new DoesNotContainContactLinkException();
    }
    
}
