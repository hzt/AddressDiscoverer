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
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.DoesNotContainContactLinkException;
import org.norvelle.addressdiscoverer.exceptions.MultipleContactLinksOfSameTypeFoundException;
import org.norvelle.addressdiscoverer.model.Name;
import org.norvelle.addressdiscoverer.parse.structured.ContactLink;
import org.norvelle.addressdiscoverer.parse.structured.EmailContactLink;

/**
 *
 * @author enorvelle
 */
public interface INameElement {

    ContactLink getContactLink() throws MultipleContactLinksOfSameTypeFoundException, DoesNotContainContactLinkException;

    Name getName() throws CantParseIndividualException;

    void setContactLink(EmailContactLink link);
    
    public Element getNameContainingElement();

    String toString();
    
}
