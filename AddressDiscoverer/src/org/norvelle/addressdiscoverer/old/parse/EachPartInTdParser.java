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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.text.WordUtils;
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
public class EachPartInTdParser extends Parser {
    
    private final Pattern findEmailPattern = 
            Pattern.compile(String.format("(%s)", Constants.emailRegex));
    
    public EachPartInTdParser() {}

    /**
     * Given a JSoup TR element, try to create an Individual object based on
     * the fragments of information we find.
     * 
     * @param row
     * @param department The Department the new Individual will belong to.
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
        // First, see if we have more than one TD for our row. If so, then we
        // can potentially parse this with this parser, otherwise fail
        Element myRow = row.clone();
        Elements tds = myRow.select("td");
        if (tds.size() == 1)
            throw new CantParseIndividualException("TdContainerParser only works with multiple TDs");
        
        // Now, we assume that the first TD with text content holds a name
        String nameChunk = "";
        String restChunk = "";
        for (Element td : tds) {
            if (td.hasText()) {
                String text = WordUtils.capitalizeFully(td.text());
                if (Name.isName(text)) {
                    nameChunk = text;
                    td.remove();
                    break;
                }
                else restChunk += text + " ";
            }
            else td.remove();
        }
        if (nameChunk.isEmpty() && restChunk.isEmpty())
            throw new CantParseIndividualException("None of the TDs have text in them");
        
        // Now that we have a chunk of text with a name, see if we can't create a Name
        Name name = new Name(nameChunk);
        
        // Next, find our email TD, and fail if we can't find it.
        String email = "";
        Elements emailTds = myRow.select(
                String.format("td:matches(%s)", Constants.emailRegex));
        if (emailTds.isEmpty())
            throw new CantParseIndividualException("None of the TDs have an email in them");
        for (Element emailTd : emailTds) {
            String emailChunk = emailTd.text();
            Matcher matcher = this.findEmailPattern.matcher(emailChunk);
            if (matcher.lookingAt()) {
                email = matcher.group(1);
                emailTd.remove();
            }
        }
        
        // Now, put everything that remains into the "rest" category, minus any
        // number we might encounter
        String rest = name.getUnprocessed();
        for (Element td : myRow.select("td")) 
            rest += " " + td.text();
        rest = rest.replaceAll("\\d", "").trim();
        
        Individual i = new Individual(name, email, "", rest, this.getClass().getSimpleName(), department);
        return i;
    }
    
}
