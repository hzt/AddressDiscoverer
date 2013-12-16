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
import org.norvelle.utils.Utils;

/**
 * Represents an access pathway to our list of first names
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class KnownFirstName {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    
    // Our hashmap for tracking existing first names
    private static final HashMap<String, Integer> firstNames = new HashMap<>();
    
    // The file where we store our last names
    private static File namesFile;
        
    // ===================== Static Data Manipulation Methods =============================
    
    public static void initialize(String settingsDir) throws IOException {
        namesFile = new File(settingsDir + File.separator + "firstnames.txt");
        String nameStr = FileUtils.readFileToString(namesFile, "UTF-8");
        String[] namesArray = StringUtils.split(nameStr, "\n");
        for (String name : namesArray) 
            firstNames.put(name, 1);
    }
    
    public static void store() throws IOException {
        String namesStr = StringUtils.join(firstNames.keySet(), "\n");
        FileUtils.writeStringToFile(namesFile, namesStr, "UTF-8");
    }
    
    public static boolean isFirstName(String name) {
        // Instead of using a standard charset translator, we translate only vowels
        name = name.replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ü", "u")
                .replace("ß", "ss").replace("ö", "o").replace("ü", "u")
                .replace("ä", "a").replace("ë", "e").replace("è", "e");
        boolean isMatch = firstNames.containsKey(name);
        if (isMatch)
            logger.log(Level.FINE, String.format("%s is a first name", name));
        else
            logger.log(Level.FINE, String.format("%s is NOT a first name", name));
        return isMatch;
    }

    public static void delete(String name) {
        if (firstNames.containsKey(name))
            firstNames.remove(name);
    }

    public static void add(String name) {
        firstNames.put(name, 1);
    }

}
