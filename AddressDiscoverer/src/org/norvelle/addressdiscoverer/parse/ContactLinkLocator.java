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

import java.io.UnsupportedEncodingException;
import org.norvelle.addressdiscoverer.parse.structured.StructuredPageContactLinkLocator;
import org.norvelle.utils.Utils;

/**
 *
 * @author enorvelle
 */
public class ContactLinkLocator {
    
    public static String baseUrl = null;
    
    public static String resolveAddress(String address) {
        String newAddress;
        // Do we already have a fully-formed URL?
        if (address.startsWith("http:")) {
            newAddress = address;
        }
        // Change https to http
        if (address.startsWith("https:")) {
            newAddress = address;
            //    newAddress = address.replace("https:", "http:");
            // Now check is we have an absolute path but no protocol
        } else if (address.startsWith("/")) {
            int slashslash = StructuredPageContactLinkLocator.baseUrl.indexOf("//") + 2;
            String domainAndProtocol = StructuredPageContactLinkLocator.baseUrl.substring(0, StructuredPageContactLinkLocator.baseUrl.indexOf('/', slashslash));
            String fullUrl = domainAndProtocol + address;
            newAddress = fullUrl;
        }
        // We have only a relative path
        else {
            if (address.startsWith("./")) {
                address = address.substring(2);
            }
            int lastSlash = StructuredPageContactLinkLocator.baseUrl.lastIndexOf("/");
            String choppedUrl = StructuredPageContactLinkLocator.baseUrl.substring(0, lastSlash + 1);
            String fullUrl = choppedUrl + address;
            newAddress = fullUrl;
        }
        // De-urlencode the new address
        String unencodedAddress;
        try {
            unencodedAddress = Utils.decodeHtml(newAddress, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return newAddress;
        }
        return unencodedAddress;
    }
    
}
