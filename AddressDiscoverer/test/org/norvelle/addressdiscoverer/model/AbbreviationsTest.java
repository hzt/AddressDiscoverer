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
import junit.framework.Assert;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class AbbreviationsTest {
    
    public AbbreviationsTest() {
    }
    
    @BeforeClass
    @SuppressWarnings("UnnecessaryReturnStatement")
    public static void setUpClass() {
        try {
            Abbreviations.initialize(TestUtilities.getTestOutputDirectory());
        } catch (IOException ex) {
            fail("Can't read abbreviations file");
        }        
    }

    //@Test
    public void testAbbreviations() {
        String francisco = Abbreviations.fixAbbreviations("Fco. Manuel");
        Assert.assertEquals("Fco. Manuel should become Francisco Manuel", "Francisco Manuel", francisco);
        String maria = Abbreviations.fixAbbreviations("Mª Carmen");
        Assert.assertEquals("Mª Carmen should become María Carmen", "María Carmen", maria);
    }
    
    @Test
    public void testVinardell() {
        String chunk = "Vinardell, Ma. Pilar";
        Name name;
        try { 
            name = new Name(chunk);
        } catch (CantParseIndividualException ex) {
            fail("Can't parse individual");
            return;
        }
        
        org.junit.Assert.assertEquals("First name should be María Pilar", "María Pilar", name.getFirstName());
        org.junit.Assert.assertEquals("Last name should be Vinardell", "Vinardell", name.getLastName());
        org.junit.Assert.assertEquals("Title should be ''", "", name.getTitle());
    }

}
