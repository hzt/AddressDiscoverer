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

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class EmailElementFinder {
    

    /**
     * For a given webpage (represented as a Jsoup document) locate all the elements
     * that contain contact info (emails or web links to CV pages) associated with the
     * name-containing elements. We use this information to help determine a page's
     * type, as well as for later extracting the email for building Individual objects.
     * 
     * @param nameFinder 
     */
    public EmailElementFinder(NameElementFinder nameFinder) {
        List<NameElement> nameElements = nameFinder.getNameElements();
for (NameElement nameElement : nameElementFinder.getNameElements()) {
            nameElement.findContactInfo();
        }        
    }
}
