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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.LastName;

/**
 * Handles a chunk of text thought to have a first and last name(s)
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class LastLastNameChunkHandler {
    
    private final String firstName;
    private final String lastName;
    private final String fullName;
    private final String rest;
    
    /**
     * From a chunk of text representing an HTML table row, find, if possible,
     * the first and last names of the individual, plus his email and other info.
     * 
     * @see getFirstName(), etc. for methods to retrieve the results, if any
     * @param chunk A String containing HTML for a table row
     * @throws SQLException
     * @throws OrmObjectNotConfiguredException 
     */
    public LastLastNameChunkHandler(String chunk) 
            throws SQLException, OrmObjectNotConfiguredException, CantParseIndividualException
    {
        // First see if we have a name divided by a comma
        chunk = chunk.trim();
        String[] words = StringUtils.split(chunk);
        String myFirstName = "";
        String myLastName = "";
        String rest = "";
        boolean haveFirstName = false;
        for (String word : words) {
            if (! LastName.isLastName(word) && !haveFirstName) 
                myFirstName += " " + word;
            else if (! LastName.isLastName(word)) {
                rest += word + " ";
            }
            else {
                myLastName += " " + word;
                haveFirstName = true;
            }
        }

        // Otherwise, we chop the thing in half and that's it.
        if (myLastName.isEmpty()) {
            throw new CantParseIndividualException("No last name found: " + chunk);
        }
        this.firstName = myFirstName;
        this.lastName = myLastName;
        this.fullName = myFirstName + " " + myLastName;
        this.rest = rest.trim();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRest() {
        return rest;
    }
    
    
}
