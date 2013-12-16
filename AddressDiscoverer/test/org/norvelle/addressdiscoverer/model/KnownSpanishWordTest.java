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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.norvelle.addressdiscoverer.TestUtilities;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class KnownSpanishWordTest {
    
    // Our hashmap for tracking existing first words
    private static final HashMap<String, Integer> spanishWords = new HashMap<>();
    
    // The file where we store our last words
    private static File wordsFile;
    
    public KnownSpanishWordTest() {
    }
    
    @BeforeClass
    @SuppressWarnings("UnnecessaryReturnStatement")
    public static void setUpClass() {
        try {
            KnownSpanishWord.initialize(TestUtilities.getTestOutputDirectory());
            wordsFile = new File(TestUtilities.getTestOutputDirectory() 
                    + File.separator + "spanish_words.txt");
            String wordStr;
            wordStr = FileUtils.readFileToString(wordsFile, "UTF-8");
            String[] wordsArray = StringUtils.split(wordStr, "\n");
            for (String word : wordsArray) 
            spanishWords.put(word, 1);
        } catch (IOException ex) {
            fail("Can't read word file");
            return;
        }
    }

    @Test
    public void testFlamarique() {
        //List<String> lastNames = new ArrayList<>();
        boolean found = false;
        for (String word : spanishWords.keySet()) {
            if (word.startsWith("Flam")) {
                String difference = StringUtils.difference(word, "Flamarique");
                if (difference.isEmpty()) {
                    found = true;
                    break;
                }
                if (word.equals("Flamarique")) {
                    found = true;
                    break;
                }
            }
        }
        Assert.assertFalse(found); 
        
        boolean isWord = KnownSpanishWord.isWord("Flamarique");
        Assert.assertFalse("Flamarique should NOT be a word", isWord);
        boolean isWord2 = KnownSpanishWord.isWord("catedrático");
        Assert.assertTrue("catedrático should be a word", isWord2);
    }
    
}
