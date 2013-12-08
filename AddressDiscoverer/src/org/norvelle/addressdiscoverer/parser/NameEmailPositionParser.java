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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.norvelle.addressdiscoverer.model.Individual;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class NameEmailPositionParser extends Parser {
    
    private final Pattern splitByEmailPattern;
    
    public NameEmailPositionParser() {
        this.splitByEmailPattern = Pattern.compile(
                String.format("(.*)%s(.*)", Parser.emailRegex));
        
    }

    @Override
    public Individual getIndividual(String chunk) {
        String firstName = "", lastName = "", email = "", title = "";
        String fullName = "", affiliation = "";
        Matcher matcher = this.splitByEmailPattern.matcher(chunk); 
        if (!matcher.matches()) return null;
        String nameChunk = matcher.group(1);
        email = matcher.group(2);
        String rest = matcher.group(3);
        
        Individual i = new Individual(firstName, lastName, fullName, email, title, affiliation);
        return i;
    }
    
}
