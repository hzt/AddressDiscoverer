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
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.KnownLastName;
import org.norvelle.addressdiscoverer.model.Name;

/**
 * Handles a chunk of text thought to have a first and last name(s)
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class LastLastNameChunkHandler implements IChunkHandler {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    
    private String firstName;
    private String lastName;
    private String fullName;
    private String rest;
    
    public LastLastNameChunkHandler() {}
    
    /**
     * From a chunk of text representing an HTML table row, find, if possible,
     * the first and last names of the individual, plus his email and other info.
     *
     * @param chunk A String containing HTML for a table row
     * @see getFirstName(), etc. for methods to retrieve the results, if any
     * @throws SQLException
     * @throws OrmObjectNotConfiguredException 
     */
    public Name processChunkForName(String chunk) 
            throws SQLException, OrmObjectNotConfiguredException, CantParseIndividualException
    {
        return new Name(chunk);
    }
}
