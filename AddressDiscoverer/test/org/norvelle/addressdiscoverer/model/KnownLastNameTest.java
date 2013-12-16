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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class KnownLastNameTest {
    
    // Our hashmap for tracking existing first names
    private static final HashMap<String, Integer> lastNames = new HashMap<>();
    
    // The file where we store our last names
    private static File namesFile;
    
    public KnownLastNameTest() {
    }
    
    @BeforeClass
    @SuppressWarnings("UnnecessaryReturnStatement")
    public static void setUpClass() {
        try {
            KnownLastName.initialize(TestUtilities.getTestOutputDirectory());
            namesFile = new File(TestUtilities.getTestOutputDirectory() 
                    + File.separator + "lastnames.txt");
            String nameStr;
            nameStr = FileUtils.readFileToString(namesFile, "UTF-8");
            String[] namesArray = StringUtils.split(nameStr, "\n");
            for (String name : namesArray) 
            lastNames.put(name, 1);
        } catch (IOException ex) {
            fail("Can't read name file");
            return;
        }
    }

    @Test
    public void testLizasoain() {
        //List<String> lastNames = new ArrayList<>();
        boolean found = false;
        for (String name : lastNames.keySet()) {
            if (name.startsWith("Lizas")) {
                String difference = StringUtils.difference(name, "Lizasoain");
                if (difference.isEmpty()) {
                    found = true;
                    break;
                }
                if (name.equals("Lizasoain")) {
                    found = true;
                    break;
                }
            }
        }
        Assert.assertTrue(found); 
        
        boolean isLastName = KnownLastName.isLastName("Lizasoain");
        Assert.assertTrue("Lizasoain should be a last name", isLastName);
    }
    
}
