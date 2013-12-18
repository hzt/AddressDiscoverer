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
package org.norvelle.addressdiscoverer.parse.parser;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.Constants;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.MultipleRecordsInTrException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.Name;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class NameEmailPositionParser extends Parser {
    
    private final Pattern splitByEmailPattern;
    private final Pattern splitByEmailPattern2;
    
    public NameEmailPositionParser() {
        this.splitByEmailPattern = Pattern.compile(
                String.format("^(.*) (%s) (.*)$", Constants.emailRegex));
        this.splitByEmailPattern2 = Pattern.compile(
                String.format("^(.*) (%s)$", Constants.emailRegex));
    }

     /**
     * Given a JSoup TR element, try to create an Individual object based on
     * the fragments of information we find.
     * 
     * @param row A JSoup Element object representing an HTML TR tag
     * @param department
     * @return An Individual with appropriately filled in details
     * @throws org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException
     * @throws java.sql.SQLException
     * @throws org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException
     * @throws org.norvelle.addressdiscoverer.exceptions.MultipleRecordsInTrException
     */
    @Override
    public Individual getIndividual(Element row, Department department) 
            throws CantParseIndividualException, SQLException, OrmObjectNotConfiguredException, 
            MultipleRecordsInTrException
    {
        return this.getIndividual(row.text(), department);
    }

            
    public Individual getIndividual(String chunk, Department department) 
            throws CantParseIndividualException, SQLException, OrmObjectNotConfiguredException, 
            MultipleRecordsInTrException
    {        
        // Based on the text found in the current row, see if we can't
        // extract a more or less complete Individual. We try it first with a regex
        // that matches an email address that is in the middle, and then with 
        // one that looks for a final email address.
        String nameChunk;
        String email;
        String rest;
        Matcher matcher = this.splitByEmailPattern.matcher(chunk); 
        if (!matcher.matches()) {
            Matcher matcher2 = this.splitByEmailPattern2.matcher(chunk); 
            if (!matcher2.matches())
                throw new CantParseIndividualException(chunk + ": doesn't match regex");
            nameChunk = matcher2.group(1);
            email = matcher2.group(2);
            rest = "";
        }
        else {
            nameChunk = matcher.group(1);
            email = matcher.group(2);
            rest = matcher.group(matcher.groupCount());
        }
        if (email == null)
            throw new CantParseIndividualException(chunk + ": No email");
        
        // Now that we have a chunk of text with a name, parse it into its parts
        // and create an Individual out of it.
        Name name = new Name(nameChunk);
        Individual i = new Individual(name, email, "", rest + " " + name.getUnprocessed(), 
                this.getClass().getSimpleName(), department);
        return i;
    }
    
}
