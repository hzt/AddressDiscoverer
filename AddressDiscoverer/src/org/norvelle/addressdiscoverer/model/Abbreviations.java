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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents an access pathway to our list of first abbreviations
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class Abbreviations {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    
    // Our hashmap for tracking existing first abbreviations
    private static final HashMap<String, String> abbreviations = new HashMap<>();
    
    // The file where we store our last abbreviations
    private static File abbreviationsFile;
        
    // ===================== Static Data Manipulation Methods =============================
    
    public static void initialize(String settingsDir) throws IOException {
        abbreviationsFile = new File(settingsDir + File.separator + "abbreviations.txt");
        String abbreviationStr = FileUtils.readFileToString(abbreviationsFile, "UTF-8");
        String[] abbreviationsArray = StringUtils.split(abbreviationStr, "\n");
        for (String abbreviationPair : abbreviationsArray) 
            if (!abbreviationPair.isEmpty()) {
                String[] pair = abbreviationPair.split("\t");
                abbreviations.put(pair[0], pair[1]);
            }
    }
    
    public static void store() throws IOException {
        throw new UnsupportedOperationException(); 
        //String abbreviationsStr = StringUtils.join(abbreviations.keySet(), "\n");
        //FileUtils.writeStringToFile(abbreviationsFile, abbreviationsStr, "UTF-8");
    }
    
    public static String fixAbbreviations(String name) {
        // Instead of using a standard charset translator, we translate only vowels
        for (String abbreviation : abbreviations.keySet())
            name = name.replace(abbreviation, abbreviations.get(abbreviation));
        return name;
    }

    public static void delete(String abbreviation) {
        throw new UnsupportedOperationException(); 
        //if (abbreviations.containsKey(abbreviation))
        //    abbreviations.remove(abbreviation);
    }

    public static void add(String abbreviation) {
        throw new UnsupportedOperationException(); 
        //abbreviations.put(abbreviation, 1);
    }

}
