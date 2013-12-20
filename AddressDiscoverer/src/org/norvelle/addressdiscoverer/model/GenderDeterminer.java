/**
 * Part of the AddressDiscoverer project, licensed under the GPL v.3 license.
 * This project provides intelligence for discovering email addresses in
 * specified web pages, associating them with a given firstName and department
 * and address type.
 *
 * This project is licensed under the GPL v.3. Your rights to copy and modify
 * are regulated by the conditions specified in that license, available at
 * http://www.gnu.org/licenses/gpl-3.0.html
 */
package org.norvelle.addressdiscoverer.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.norvelle.utils.Utils;

/**
 * Represents an access pathway to our list of firstNames
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class GenderDeterminer {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    
    // Our hashmap for tracking existing first firstNames
    private static final HashMap<String, Integer> girls = new HashMap<>();
    private static final HashMap<String, Integer> boys = new HashMap<>();
    
    // The file where we store our last firstNames
    private static File firstNamesFile;
        
    public enum Gender {
        MALE, FEMALE, UNKNOWN
    }
    
    // ===================== Static Data Manipulation Methods =============================
    
    public static void initialize(String settingsDir) throws IOException {
        firstNamesFile = new File(settingsDir + File.separator + "firstnames.gender.txt");
        String abbreviationStr = FileUtils.readFileToString(firstNamesFile, "UTF-8");
        String[] firstNamesArray = StringUtils.split(abbreviationStr, "\n");
        for (String abbreviationPair : firstNamesArray) 
            if (!abbreviationPair.isEmpty()) {
                String[] pair = abbreviationPair.split(",");
                if (pair[1].trim().equals("M"))
                    boys.put(pair[0], 1);
                else
                    girls.put(pair[0], 1);
            }
    }
    
    /**
     * Determine whether a first name is of a boy or a girl, if we have the name
     * in our database. If it is listed for both boys and girls we return UNKNOWN.
     * 
     * @param name The name to look up
     * @return A Gender value.
     */
    public static GenderDeterminer.Gender getGender(String name) {
        // First, normalize our name so we can match it with our database
        name = Utils.normalizeName(name);
        
        // Handle the case of having a hyphenated name
        if (name.contains("-")) {
            String[] nameParts = StringUtils.split(name, "-");
            name = nameParts[0];
        }
        
        // Now, look it up and see if we can match it to a gender.
        boolean isMale = boys.containsKey(name);
        boolean isFemale = girls.containsKey(name);
        if (isMale && !isFemale)
            return Gender.MALE;
        else if (isFemale && !isMale)
            return Gender.FEMALE;
        
        // Apply some tricks
        if (name.charAt(name.length() - 1) == 'a')
            return Gender.FEMALE;
        if (name.charAt(name.length() - 1) == 'o')
            return Gender.MALE;
        if (name.startsWith("J."))
            return Gender.MALE;
        if (name.startsWith("M."))
            return Gender.FEMALE;
        
        // Otherwise give up.
        else return Gender.UNKNOWN;
    }

}
