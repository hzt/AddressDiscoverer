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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.KnownLastName;

/**
 * Used for collecting data about an individual as we crawl through an HTML
 * document. It produces a TR element that we can store and later parse using
 * one of the standard parsers for TR-oriented documents.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
class IndividualCollector {
    private final String email;
    private String name = "";
    private final List<String> unprocessed = new ArrayList<>();

    public IndividualCollector(String email) {
        this.email = email;
    }

    public void addLine(String line) throws SQLException, OrmObjectNotConfiguredException {
        if (this.name.isEmpty() && this.isLastName(line)) {
            this.name = line;
        } else if (!this.name.isEmpty() && this.isLastName(line)) {
            this.unprocessed.add(this.name);
            this.name = line;
        } else  {
            this.unprocessed.add(line);
        }
    }

    /**
     * Create an TR describing an individual from the information we've collected so far. If
     * we detect a line with a last name in it, we set that as the name field.
     * If we haven't found any such line yet, we use the last unrecognized line
     * as containing the name. Anything between the email and the name line
     * is stuck into the unprocessed TD node.
     *
     * @return An Element representing a TR with TDs for each field found.
     */
    public Element createIndividualTr() {
        Document trDocument = new Document("");
        Element tr = trDocument.createElement("tr");
        
        // First create and append a TD for our name, as best as we can guess it.
        Element nameTd = trDocument.createElement("td");
        if (this.name.isEmpty()) {
            String nameGuess = this.unprocessed.get(this.unprocessed.size() - 1);
            nameTd.appendText(nameGuess);
        } else {
            nameTd.appendText(this.name);
        }
        tr.appendChild(nameTd);
        
        // Create a middle TD for the unprocessed text.
        String unprocessedText = StringUtils.join(this.unprocessed, " ");
        Element unprocessedTd = trDocument.createElement("td");
        unprocessedTd.appendText(unprocessedText);
        tr.appendChild(unprocessedTd);
        
        // Finally, append a TD for our email address
        Element emailTd = trDocument.createElement("td");
        emailTd.appendText(this.email);
        tr.appendChild(emailTd);
        
        // Finish creating our "document" and return it.
        trDocument.appendChild(tr);
        return trDocument;
    }

    private boolean isLastName(String line) throws SQLException, OrmObjectNotConfiguredException {
        String[] words = StringUtils.split(line);
        for (String word : words) {
            if (KnownLastName.isLastName(word)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasName() {
        return !this.name.isEmpty();
    }
    
}
