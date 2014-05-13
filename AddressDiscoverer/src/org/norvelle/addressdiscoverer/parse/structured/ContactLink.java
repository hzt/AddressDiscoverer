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
package org.norvelle.addressdiscoverer.parse.structured;

import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.Constants;
import org.norvelle.addressdiscoverer.exceptions.DoesNotContainContactLinkException;

/**
 *
 * @author enorvelle
 */
public class ContactLink {
    
    protected static final Pattern emailPattern = Pattern.compile(Constants.emailRegex);
    
    protected String address;
    protected final Element originalElement;

    public ContactLink(Element element) {
        this.originalElement = element;
    }

    public Element getOriginalElement() {
        return originalElement;
    }

    public String getAddress() throws DoesNotContainContactLinkException {
        return this.address;
    }
    
    public String getUnderlyingUrl() {
        return null;
    }
    
}
