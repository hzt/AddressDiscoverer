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
package org.norvelle.addressdiscoverer.parser.chunk;

import java.sql.SQLException;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.Name;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public interface IChunkHandler {

    /**
     * From a chunk of text representing an HTML table row, find, if possible,
     * the first and last names of the individual, plus his email and other info.
     *
     * @param chunk A String containing HTML for a table row
     * @return A Name object with information about first, last names, etc.
     * @throws org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException
     * @throws SQLException
     * @throws OrmObjectNotConfiguredException
     */
    Name processChunkForName(String chunk) throws SQLException, 
            OrmObjectNotConfiguredException, CantParseIndividualException;
    
}
