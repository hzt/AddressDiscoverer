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
package org.norvelle.addressdiscoverer.classifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.Constants;
import org.norvelle.addressdiscoverer.exceptions.DoesNotContainContactLinkException;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ContactLink {
    
    private static final Pattern regexPattern = Pattern.compile(Constants.emailRegex);
    private String address;
    private ContactType type;
    
    public enum ContactType {
        EMAIL_IN_CONTENT, EMAIL_IN_HREF, LINK_TO_DETAIL_PAGE, NO_CONTACT_INFO_FOUND;
    }
    
    public ContactLink(Element element) throws DoesNotContainContactLinkException {
        String content = element.ownText();
        Matcher contentMatcher = regexPattern.matcher(content);
        if (contentMatcher.lookingAt()) {
            this.type = ContactType.EMAIL_IN_CONTENT;
            this.address = contentMatcher.group();
            return;
        }
        if (element.hasAttr("href")) {
            String href = element.attr("href");
            if (href.startsWith("mailto:")) {
                Matcher hrefMatcher = regexPattern.matcher(href);
                if (hrefMatcher.lookingAt()) {
                    this.type = ContactType.EMAIL_IN_HREF;
                    this.address = hrefMatcher.group();
                    return;
                }
            }
            else {
                this.type = ContactType.LINK_TO_DETAIL_PAGE;
                this.address = href;
                return;
            }
        } 
        
        // If we get here, no contact info was found.
        throw new DoesNotContainContactLinkException();        
    }
    
    public String getAddress() {
        return this.address;
    }
    
    public ContactType getType() {
        return this.type;
    }
    
}
