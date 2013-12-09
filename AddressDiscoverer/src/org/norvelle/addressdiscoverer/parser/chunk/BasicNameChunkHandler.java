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
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.LastName;

/**
 * Handles a chunk of text thought to have a first and last name(s)
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class BasicNameChunkHandler {
    
    private static final String hasCommaRegex = "^(.*),(.*)$";
    private static Pattern hasCommaPattern;

    private final String firstName;
    private final String lastName;
    private final String fullName;
    
    /**
     * From a chunk of text representing an HTML table row, find, if possible,
     * the first and last names of the individual, plus his email and other info.
     * 
     * @see getFirstName(), etc. for methods to retrieve the results, if any
     * @param chunk A String containing HTML for a table row
     * @throws SQLException
     * @throws OrmObjectNotConfiguredException 
     */
    public BasicNameChunkHandler(String chunk) 
            throws SQLException, OrmObjectNotConfiguredException
    {
        if (BasicNameChunkHandler.hasCommaPattern == null)
            BasicNameChunkHandler.hasCommaPattern = Pattern.compile(BasicNameChunkHandler.hasCommaRegex);
        
        // First see if we have a name divided by a comma
        chunk = chunk.trim();
        Matcher hasCommaMatcher = BasicNameChunkHandler.hasCommaPattern.matcher(chunk);
        if (hasCommaMatcher.matches()) {
            this.firstName = hasCommaMatcher.group(2);
            this.lastName = hasCommaMatcher.group(1);
            this.fullName = this.firstName + " " + this.lastName;
        }
        
        // Otherwise, we try to split the string into first and last names as 
        // best we can.
        else {
            String[] words = StringUtils.split(chunk);
            String myFirstName = "";
            String myLastName = "";
            for (String word : words) {
                if (LastName.isLastName(word) || !myLastName.isEmpty())
                    myLastName += " " + word;
                else myFirstName += " " + word;
            }
            
            // Otherwise, we chop the thing in half and that's it.
            if (myLastName.isEmpty()) {
                int middle = (int) words.length / 2;
                String first[] = ArrayUtils.subarray(words, 0, middle - 1);
                String last[] = ArrayUtils.subarray(words, middle - 1, words.length);   
                myFirstName = StringUtils.join(first, " ");
                myLastName = StringUtils.join(last, " ");
            }
            this.firstName = myFirstName;
            this.lastName = myLastName;
            this.fullName = chunk;
        }
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
    
}
