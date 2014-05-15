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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.norvelle.addressdiscoverer.Constants;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class UnamName {

    private String firstName = "";
    private String lastName = "";
    private String rest = "";
    private String title = "";
    private String suffix = "";
    
    private static final Pattern hyphenatedPattern = Pattern.compile("^(.*)-(.*)$");
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    /**
     * Our primary constructor... tries to apply intelligence to parsing the name
     * 
     * @param textChunk A chunk of text that supposedly contains a name 
     * @throws org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException 
     */
    public UnamName(String textChunk) throws CantParseIndividualException {
        // Eliminate non-breaking spaces
        textChunk = textChunk.replaceAll("\\xA0", " ");
        
        // Replace single quotes with apostrophes, to avoid SQL problems
        textChunk = textChunk.replaceAll("'", "ʼ");

        // Eliminate numbers... no use for them, but only ones that exist separately
        // and not as part of emails.
        textChunk = textChunk.replaceAll("\\b\\d+\\b", "").trim();

        // chop off commas and anything following them.
        if (textChunk.contains(","))
            textChunk = textChunk.substring(0, textChunk.indexOf(","));
        
        String[] chunks = StringUtils.split(textChunk, " ");
        this.firstName = chunks[chunks.length - 1];
        for (int i = 0; i < chunks.length - 1; i ++)
            this.lastName += chunks[i] + " ";
        this.lastName = this.lastName.trim();
    }


    // ===================== Getters =============================
    
    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getFullName() {
        return this.getFirstName() + " " + this.getLastName().trim();
    }

    public String getUnprocessed() {
        return "";
    }

    public String getTitle() {
        return title.trim();
    }

    @Override
    public String toString() {
        return (this.title + " " + this.firstName + " " + this.lastName).trim();
    }
    
}
