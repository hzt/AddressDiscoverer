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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    
    /**
     * A regex for detecting numbers
     */
    private final Pattern numberPattern = Pattern.compile("\\b\\d+\\b");
    
    /**
     * A regex for detecting parentheses
     */
    private final Pattern parensPattern = Pattern.compile("(\\(.*\\))");
    
    /**
     * A short list of likely titles
     */
    private final ArrayList<String> possibleTitles = new ArrayList<String>() {{
        add("Dr."); add("Dra."); add("Ing."); add("Lic.");
        add("D."); add("Dña."); add("Prof."); 
    }};
    
    /**
     * A short list of some common suffixes
     */
    private final ArrayList<String> suffixes = new ArrayList<String>() {{
        add("Jr."); add("II"); add("III"); add("IV");
        add("Esq"); add("SJ"); add("OP"); add("OFM");
    }};
    
    /**
     * Our primary constructor... tries to apply intelligence to parsing the name
     * 
     * @param textChunk A chunk of text that supposedly contains a name
     * @throws SQLException
     * @throws OrmObjectNotConfiguredException 
     */
    public Name(String textChunk) throws SQLException, OrmObjectNotConfiguredException {
        if (textChunk.contains(","))
            this.parseCommaSeparatedChunk(textChunk);
        else
            this.parseOneLongChunk(textChunk);
        this.moveParens();
        this.stealFromFirst();
    }
    
    public Name(String first, String last) throws SQLException, OrmObjectNotConfiguredException {
        this.firstName = first;
        this.lastName = last;
        this.extractRest();
        this.moveParens();
        this.stealFromFirst();
    }
    
    public Name(String first, String last, String rest) {
        this.firstName = first;
        this.lastName = last;
        this.rest = rest.trim();
        this.moveParens();
        this.stealFromFirst();
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

    /**
     * Given a chunk divided by a comma, assume that the first portion contains
     * the last name and possible suffix, and the second chunk is the first name
     * and title(s)
     * 
     * @param textChunk 
     */
    private void parseCommaSeparatedChunk(String textChunk) throws SQLException, OrmObjectNotConfiguredException {
        // Split things into two parts
        String[] parts = textChunk.split(",");
        String myFirstName = parts[1];
        String myLastName = parts[0];
        String myTitle = "";
        String mySuffix = "";
        String myRest = "";
        
        // First see if we can pull out anything from the first name that
        // is a title
        for (String word : StringUtils.split(myFirstName)) 
            if (this.possibleTitles.contains(word)) {
                myFirstName = myFirstName.replace(word, "");
                myTitle += word + " ";
            }
        this.firstName = myFirstName.trim();
        this.title = myTitle.trim();
        
        // Next, divide the "last name" into last names, suffix and "rest"
        boolean restHasBegun = false;
        for (String word : StringUtils.split(myLastName)) {
            if (this.suffixes.contains(word)) {
                myLastName = myLastName.replace(word, "");
                mySuffix += word + " ";
            }
            else if (!KnownLastName.isLastName(word) || restHasBegun) {
                restHasBegun = true;
                myRest += myRest + " ";
                myLastName = myLastName.replace(word, "");
            }
        }
        this.lastName = myLastName.trim();
        this.suffix = mySuffix.trim();
        this.rest = myRest.trim();
    }

    /**
     * Given a chunk with no comma, parses into a name, where the first words are
     * considered titles or first names, and the later words are candidates for
     * last names or "rest" chunk. Uses our last name database to detect when the
     * last name portion starts.
     * 
     * @param oneLongChunk
     * @throws SQLException
     * @throws OrmObjectNotConfiguredException 
     */
    private void parseOneLongChunk(String oneLongChunk) throws SQLException, OrmObjectNotConfiguredException {
        String[] words = StringUtils.split(oneLongChunk);
        String myFirstName = "";
        String myLastName = "";
        String myRest = "";
        String myTitle = "";
        for (String word : words) {
            // First order of business is to see if we have a title
            if (this.possibleTitles.contains(word))
                myTitle += word + " ";
            // If not, but we are at the first word, we always put it as a first name
            else if (myFirstName.isEmpty())
                myFirstName = word + " ";
            // Otherwise, if it is a known last name we add it to our last names
            else if (KnownLastName.isLastName(word))
                myLastName += word + " ";
            // Otherwise, it's not a last name and we haven't seen any last 
            // names yet: it's a firstn name
            else if (myLastName.isEmpty())
                myFirstName += word + " ";
            // Otherwise, we're past the last names and this should go into our "rest" pile
            else
                myRest += word + " ";
        }
        this.firstName = myFirstName.trim();
        this.lastName = myLastName.trim();    
        this.rest = myRest.trim();
        this.title = myTitle.trim();        
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
     * Seeks to eliminate any garbage from the first and last names and place it in a "rest"
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
                if (this.suffixes.contains(word)) {
                    possibleSuffix += word + " ";
                    possibleRest = possibleRest.replace(word, "").trim();
                }
                else break;
            }
        }
        this.lastName = possibleLast.trim();
        this.suffix = possibleSuffix.trim();
        this.rest = possibleRest.trim();
    }

    private void moveParens() {
        Pattern pp = Pattern.compile("\\(.*\\)");
        Matcher matcherFirst = pp.matcher(this.firstName);
        while (matcherFirst.find())  {
            String foundParens = matcherFirst.group();
            this.firstName = this.firstName.replace(foundParens, "").trim();
            this.rest = (this.rest + " " + foundParens).trim();
        }
        Matcher matcherLast = this.parensPattern.matcher(this.lastName);
        if (matcherLast.find())  {
            String foundParens = matcherLast.group();
            this.lastName = this.lastName.replace(foundParens, "").trim();
            this.rest = (this.rest + " " + foundParens).trim();
        }
    }
    
    private void stealFromFirst() {
        if (this.lastName.isEmpty() && !this.firstName.isEmpty()) {
            String[] words = StringUtils.split(this.firstName);
            this.lastName = words[words.length - 1];
            this.firstName = this.firstName.replace(this.lastName, "");
        }
    }
    
    private String eliminateWordsWithSymbols(String chunk) {
        String result = "";
        for (String word : StringUtils.split(chunk)) {
            if (!word.matches(".*(\\p{P}|\\p{S}).*"))
                result += " " + word;
        }
        return result.trim();
    }

    // ===================== Getters =============================
    
    public String getFirstName() {
        return this.eliminateWordsWithSymbols(firstName);
    }

    public String getLastName() {
        return this.eliminateWordsWithSymbols(lastName);
    }

    public String getFullName() {
        return (this.title + " " + this.getFirstName() + " " + 
                this.getLastName()).trim();
    }

    public String getRest() {
        return rest.trim();
    }

    public String getTitle() {
        return title.trim();
    }

    
}
