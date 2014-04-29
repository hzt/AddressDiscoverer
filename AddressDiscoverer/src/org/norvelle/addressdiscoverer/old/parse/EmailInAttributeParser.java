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
package org.norvelle.addressdiscoverer.old.parse;

import java.sql.SQLException;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.norvelle.addressdiscoverer.Constants;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.MultipleRecordsInTrException;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.Name;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class EmailInAttributeParser extends Parser {
    
    private final Pattern splitByEmailPattern;
    private final String splitByEmailRegex;
    
    public EmailInAttributeParser() {
        this.splitByEmailRegex = String.format("(.*) (%s) (.*)", Constants.emailRegex);
        this.splitByEmailPattern = Pattern.compile(this.splitByEmailRegex);
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
     * @throws org.norvelle.addressdiscoverer.exceptions.MultipleRecordsInTrException
     */
    @Override
    public Individual getIndividual(Element row, Department department) 
            throws CantParseIndividualException, SQLException, 
            MultipleRecordsInTrException
    {
        Elements emailAttrElements = row.select(
                String.format("[href~=(%s)]", Constants.emailRegex));
        if (emailAttrElements.isEmpty())
            throw new CantParseIndividualException("No email in attributes");
        Element elem = emailAttrElements.first();
        String email = elem.attr("href").replace("mailto:", "");
        
        // Based on the text found in the current row, see if we can't
        // extract a more or less complete Individual.
        Name name = new Name(row.text());
        String rest = name.getUnprocessed();
        
        Individual i = new Individual(name, email, "", rest, this.getClass().getSimpleName(), department);
        return i;
    }
    
}
