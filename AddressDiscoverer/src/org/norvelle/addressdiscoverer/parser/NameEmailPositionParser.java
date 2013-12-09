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
package org.norvelle.addressdiscoverer.parser;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.Individual;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class NameEmailPositionParser extends Parser {
    
    private final Pattern splitByEmailPattern;
    private final String splitByEmailRegex;
    
    public NameEmailPositionParser() {
        this.splitByEmailRegex = String.format("(.*) (%s) (.*)", Parser.emailRegex);
        this.splitByEmailPattern = Pattern.compile(this.splitByEmailRegex);
    }

    /**
     * Given a JSoup TR element, try to create an Individual object based on
     * the fragments of information we find.
     * 
     * @param row A JSoup Element object representing an HTML TR tag
     * @return An Individual with appropriately filled in details
     * @throws org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException
     * @throws java.sql.SQLException
     * @throws org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException
     */
    @Override
    public Individual getIndividual(Element row) 
            throws CantParseIndividualException, SQLException, OrmObjectNotConfiguredException
    {
        String firstName = "", lastName = "", email = "", title = "";
        String fullName = "", affiliation = "";
        String chunk = row.text();
        
        // Based on the text found in the current row, see if we can't
        // extract a more or less complete Individual.
        Matcher matcher = this.splitByEmailPattern.matcher(chunk); 
        if (!matcher.matches()) 
            throw new CantParseIndividualException(chunk + ": doesn't match regex");
        String nameChunk = matcher.group(1);
        NameChunkParser np = new NameChunkParser(nameChunk);
        if (matcher.group(2) == null)
            throw new CantParseIndividualException(chunk + ": No email");
        email = matcher.group(2);
        String rest = matcher.group(3);
        
        Individual i = new Individual(np.getFirstName(), np.getLastName(), np.getFullName(), 
                email, title, affiliation);
        return i;
    }
    
}
