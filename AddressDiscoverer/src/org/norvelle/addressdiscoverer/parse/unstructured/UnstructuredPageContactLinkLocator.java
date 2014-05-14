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
package org.norvelle.addressdiscoverer.parse.unstructured;

import org.norvelle.addressdiscoverer.parse.ContactLink;
import org.norvelle.addressdiscoverer.parse.WebContactLink;
import org.norvelle.addressdiscoverer.parse.EmailContactLink;
import org.norvelle.addressdiscoverer.parse.structured.*;
import org.norvelle.addressdiscoverer.parse.ContactLinkLocator;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.exceptions.DoesNotContainContactLinkException;
import org.norvelle.addressdiscoverer.exceptions.MultipleContactLinksOfSameTypeFoundException;
import org.norvelle.addressdiscoverer.parse.INameElement;
import org.norvelle.utils.Utils;

/**
 * Specialist static class that knows how to locate associated contact links
 * for a given NameElement. It works by climbing up the Jsoup Element tree
 * looking for a given kind of link (email or http), giving priority to email
 * links. It returns the best link it can find, or throw an exception if no 
 * links are found.
 * 
 * @author enorvelle
 */
public class UnstructuredPageContactLinkLocator extends ContactLinkLocator {
    
    public static String baseUrl = null;
    
    public static ContactLink findLinkForNameElement(INameElement nm) 
            throws DoesNotContainContactLinkException, 
            MultipleContactLinksOfSameTypeFoundException 
    {
        UnstructuredPageNameElement unm = (UnstructuredPageNameElement) nm;
        List<Element> intermediateElements = unm.getIntermediateElements();
        
        // First, look for email addresses
        for (Element el : intermediateElements) {
            try {
                EmailContactLink link = new EmailContactLink(el);
                return link;
            } catch (DoesNotContainContactLinkException ex) {
                //;
            }
            
        }
        
        // Now check for href elements.
        for (Element el : intermediateElements) {
            try {
                WebContactLink link = new WebContactLink(el);
                return link;
            } catch (DoesNotContainContactLinkException ex) {
                //
            }
        } // while (i < 3) {

        throw new DoesNotContainContactLinkException();
    }
    
}
