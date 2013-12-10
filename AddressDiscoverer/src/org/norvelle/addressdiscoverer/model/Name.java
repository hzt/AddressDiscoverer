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
package org.norvelle.addressdiscoverer.model;

import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class Name {

    private String firstName;
    private String lastName;
    private String rest;
    private String title = "";
    private String suffix = "";
    
    private final String[] possibleTitles = {
        "Dr.", "Dra.", "Ing.", "Lic.", "D.", "Dña.", "Prof."
    };
    
    private final ArrayList<String> list = new ArrayList<String>() {{
        add("Jr."); add("II"); add("III"); add("IV");
        add("Esq"); add("SJ"); add("OP"); add("OFM");
    }};
    
    public Name(String oneLongChunk) throws SQLException, OrmObjectNotConfiguredException {
            String[] words = StringUtils.split(oneLongChunk);
            String myFirstName = "";
            String myLastName = "";
            String myRest = "";
            for (int i = words.length; i > 0; i --) {
                boolean isLastName = KnownLastName.isLastName(words[i]);
                if (isLastName) 
                    myLastName += (words[i] + " ").trim();
                else if (myLastName.isEmpty())
                    myRest += (words[i] + " ").trim();
                else 
                    myFirstName += (words[i] + " ").trim();
            }
            this.firstName = myFirstName;
            this.lastName = myLastName;    
            this.rest = myRest;
            this.extractTitle();
    }
    
    public Name(String first, String last) throws SQLException, OrmObjectNotConfiguredException {
        this.firstName = first;
        this.lastName = last;
        this.extractRest();
    }
    
    public Name(String first, String last, String rest) {
        this.firstName = first;
        this.lastName = last;
        this.rest = rest;
    }
    
    public Name(String first, String last, String title, String rest) {
        this(first, last, rest);
        this.extractTitle();
    }
    /**
     * We calculate a rough score for name quality, emphasizing that a good
     * name should have title, first and last names. However, a name without
     * a first name portion is considered worthless.
     * 
     * @return A numeric score indicating rough name quality. 
     */
    public double getScore() {
        double score = 0.0;
        if (!this.lastName.isEmpty()) score += 1.0;
        if (!this.title.isEmpty()) score += 1.0;
        if (!this.firstName.isEmpty()) score += 1.0;
        else score = 0.0;
        return score / 3;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return (this.title + " " + this.firstName + " " + this.lastName).trim();
    }

    public String getRest() {
        return rest;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Attempt to find words relating to a professional title, eg. "Prof." or 
     * "Dr." and put them in the Title field, and subtracting them from the First Name
     */
    private void extractTitle() {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        String title = "";
        for (String possibleTitle : this.possibleTitles) {
            if (this.firstName.contains(possibleTitle)) {
                this.firstName = this.firstName.replace(possibleTitle, "").trim();
                title += " " + possibleTitle;
            }
        }
        this.title = title.trim();
    }

    /**
     * Seeks to eliminate any garbage from the last name and place it in a "rest"
     * field, that can be processed later for additional information about the
     * person's post, etc.
     * 
     * The algorithm used here is to put into the last name all words that can
     * be identified as a last name based on our database of last names, and
     * putting into the suffix anything that looks like a suffix, and then 
     * putting into the "rest" category anything else that doesn't fit.
     */
    private void extractRest() throws SQLException, OrmObjectNotConfiguredException {
        String possibleRest = this.lastName;
        String possibleLast = "";
        String possibleSuffix = "";
        String[] words = this.lastName.split("\\s+");
        for (String word : words) {
            if (KnownLastName.isLastName(word)) {
                possibleLast += word + " ";
                possibleRest = possibleRest.replace(word, "").trim();
            }
            else {
                if (this.list.contains(word)) {
                    possibleSuffix += word + " ";
                    possibleRest = possibleRest.replace(word, "").trim();
                }
                else break;
            }
        }
        this.lastName = possibleLast;
        this.suffix = possibleSuffix;
        this.rest = possibleRest;
    }

    
}
