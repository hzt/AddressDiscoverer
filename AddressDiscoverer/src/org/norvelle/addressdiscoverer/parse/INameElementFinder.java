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

import java.util.List;
import org.norvelle.addressdiscoverer.parse.structured.StructuredPageNameElement;

/**
 *
 * @author enorvelle
 */
public interface INameElementFinder {

    List<INameElement> getNameElements();

    int getNumberOfNames();
    
}
