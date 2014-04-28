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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.Constants;
import org.norvelle.addressdiscoverer.exceptions.DoesNotContainContactLinkException;
import org.norvelle.addressdiscoverer.exceptions.NoEmailRetrievedFromWeblinkException;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ContactLink {
    
    private static final Pattern emailPattern = Pattern.compile(Constants.emailRegex);
    private String address;
    private ContactType type;
    private final Element originalElement;
    
    public enum ContactType {
        EMAIL_IN_CONTENT, EMAIL_IN_HREF, LINK_TO_DETAIL_PAGE, NO_CONTACT_INFO_FOUND;
    }
    
    public ContactLink(Element element) throws DoesNotContainContactLinkException {
        this.originalElement = element;
        String content = element.ownText();
        Matcher contentMatcher = emailPattern.matcher(content);
        if (contentMatcher.lookingAt()) {
            this.type = ContactType.EMAIL_IN_CONTENT;
            this.address = contentMatcher.group();
            return;
        }
        if (element.hasAttr("href")) {
            String href = element.attr("href");
            if (href.startsWith("mailto:")) {
                this.type = ContactType.EMAIL_IN_HREF;
                this.address = href.substring(6);
                return;                
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
    
    /**
     * Fetches the web page specified by the contact weblink and extracts
     * an email from it. The email gets stored in the address field for retrieval
     * by the Individual extractor. Note that we fetch the first such email found
     * and discard others.
     * 
     * @throws NoEmailRetrievedFromWeblinkException 
     */
    public void fetchEmailFromWeblink()  {
        String body;
        
        // Try to fetch the webpage linked to
        try {
            URL u = new URL(this.address); // this would check for the protocol
            u.toURI();
            URLConnection con = u.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            body = IOUtils.toString(in, encoding);
        } catch (URISyntaxException | IOException ex) {
            this.type = ContactType.NO_CONTACT_INFO_FOUND;
            return;
        }
        
        // Now, extract the email if we can.
        String matchFound = this.findEmail(body);
        if (matchFound.isEmpty()) {
            this.type = ContactType.NO_CONTACT_INFO_FOUND;
            return;                
        }
        this.address = matchFound;
        this.type = ContactType.EMAIL_IN_CONTENT;
    }
    
    private String findEmail(String text) {
        Matcher emailMatcher = emailPattern.matcher(text);
        String matchFound = "";
        while (emailMatcher.find()) {
            matchFound = text.substring(emailMatcher.start(), emailMatcher.end());
            break;
        }        
        return matchFound;
    }

    public Element getOriginalElement() {
        return originalElement;
    }
    
    public String getAddress() {
        return this.address;
    }
    
    public ContactType getType() {
        return this.type;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s", this.address, this.type);
    }
}
