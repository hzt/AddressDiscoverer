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
 * Represents an access pathway to our list of last words
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class KnownSpanishWord {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    
    // Our hashmap for tracking existing first words
    private static final HashMap<String, Integer> lastNames = new HashMap<>();
    
    // The file where we store our last words
    private static File wordsFile;
    
    // ===================== Static Data Manipulation Methods =============================
    
    public static void initialize(String settingsDir) throws IOException {
        wordsFile = new File(settingsDir + File.separator + "spanish_words.txt");
        String wordStr = FileUtils.readFileToString(wordsFile, "UTF-8");
        String[] wordsArray = StringUtils.split(wordStr, "\n");
        for (String word : wordsArray) 
            lastNames.put(word, 1);
    }
    
    public static void store() throws IOException {
        String wordsStr = StringUtils.join(lastNames.keySet(), "\n");
        FileUtils.writeStringToFile(wordsFile, wordsStr, "UTF-8");
    }
    
    public static boolean isWord(String word) {
        word = word.toLowerCase();
        boolean isMatch = false;
        for (String currName : lastNames.keySet()) {
            String diff = StringUtils.difference(word, currName);
            String diff2 = StringUtils.difference(currName, word);
            if (diff.isEmpty() && diff2.isEmpty())
                isMatch = true;
        }
        if (isMatch)
            logger.log(Level.FINE, String.format("%s is a word", word));
        else
            logger.log(Level.FINE, String.format("%s is NOT a word", word));
        return isMatch;
    }

    public static void delete(String word) {
        if (lastNames.containsKey(word))
            lastNames.remove(word);
    }
    
    public static void add(String word) {
        lastNames.put(word, 1);
    }
}
