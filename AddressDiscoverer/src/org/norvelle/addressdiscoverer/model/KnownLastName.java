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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents an access pathway to our list of last names
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class KnownLastName {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    
    // Our hashmap for tracking existing first names
    private static final HashMap<String, Integer> lastNames = new HashMap<>();
    
    // The file where we store our last names
    private static File namesFile;
    
    // ===================== Static Data Manipulation Methods =============================
    
    public static void initialize(String settingsDir) throws IOException {
        namesFile = new File(settingsDir + File.separator + "lastnames.txt");
        String nameStr = FileUtils.readFileToString(namesFile, "UTF-8");
        String[] namesArray = StringUtils.split(nameStr, "\n");
        for (String name : namesArray) 
            lastNames.put(name, 1);
    }
    
    public static void store() throws IOException {
        String namesStr = StringUtils.join(lastNames.keySet(), "\n");
        FileUtils.writeStringToFile(namesFile, namesStr, "UTF-8");
    }
    
    public static boolean isLastName(String name) {
        if (name == null) return false;
        
        // First, check something easy... if the name has a hyphen, it's a last name
        if (name.contains("-")) return true;
        
        // Instead of using a standard charset translator, we translate only vowels
        name = name.replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ü", "u")
                .replace("ß", "ss").replace("ö", "o").replace("ü", "u")
                .replace("ä", "a").replace("ë", "e").replace("è", "e");
        boolean isMatch = hasLastName(name);
        if (isMatch)
            logger.log(Level.FINE, String.format("%s is a last name", name));
        else
            logger.log(Level.FINE, String.format("%s is NOT a last name", name));
        return isMatch;
    }
    
    private static boolean hasLastName(String name) {
        for (String currName : lastNames.keySet()) {
            String diff = StringUtils.difference(name, currName);
            String diff2 = StringUtils.difference(currName, name);
            if (diff.isEmpty() && diff2.isEmpty())
                return true;
        }
        return false;
    }

    public static void delete(String name) {
        if (lastNames.containsKey(name))
            lastNames.remove(name);
    }
    
    public static void add(String name) {
        lastNames.put(name, 1);
    }
}
