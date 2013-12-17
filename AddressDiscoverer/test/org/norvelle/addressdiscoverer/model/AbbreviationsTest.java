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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import static org.junit.Assert.*;
import org.junit.Test;
import org.norvelle.addressdiscoverer.TestUtilities;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class AbbreviationsTest {
    
    public AbbreviationsTest() {
    }
    
    /*@BeforeClass
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
    }*/

    //@Test
    public void testAbbreviations() {
        try {
            Abbreviations.initialize(TestUtilities.getTestOutputDirectory());
        } catch (IOException ex) {
            fail("Can't read abbreviations file");
        }
        String francisco = Abbreviations.fixAbbreviations("Fco. Manuel");
        Assert.assertEquals("Fco. Manuel should become Francisco Manuel", "Francisco Manuel", francisco);
        String maria = Abbreviations.fixAbbreviations("Mª Carmen");
        Assert.assertEquals("Mª Carmen should become María Carmen", "María Carmen", maria);
    }
    
}
