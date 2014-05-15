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

import com.j256.ormlite.support.ConnectionSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class UnamNameTest {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public UnamNameTest() {
    }
    
    @BeforeClass
    @SuppressWarnings("UnnecessaryReturnStatement")
    public static void setUpClass() {
        TestUtilities.setupLogger();
        try {
            connection = TestUtilities.getDBConnection("addresses.sqlite");
        } catch (SQLException | CannotLoadJDBCDriverException | IOException ex) {
            fail("Encountered problems connecting to database: " + ex.getMessage());
            return;
        }
    }

    @Test
    public void testPasamar() {
        String chunk = "Zirión Quijano Antonio, Dr.";
        UnamName name;
        try { 
            name = new UnamName(chunk);
        } catch (CantParseIndividualException ex) {
            fail("Can't parse individual");
            return;
        }
        
        Assert.assertEquals("First name should be Antonio", "Antonio", name.getFirstName());
        Assert.assertEquals("Last name should be Zirión Quijano", "Zirión Quijano", name.getLastName());
        Assert.assertEquals("Title should be empty", "", name.getTitle());
    }

    @Test
    public void testLizasoain() {
        String chunk = "Villalba Ana Claudia, Lic.";
        UnamName name;
        try { 
            name = new UnamName(chunk);
        } catch (CantParseIndividualException ex) {
            fail("Can't parse individual");
            return;
        }
        
        Assert.assertEquals("First name should be Claudia", "Claudia", name.getFirstName());
        Assert.assertEquals("Last name should be Villalba Ana", "Villalba Ana", name.getLastName());
        Assert.assertEquals("Title should be ''", "", name.getTitle());
    }

    @Test
    public void testLizasoain2() {
        String chunk = "Rodríguez Sidharta, Lic.";
        UnamName name;
        try { 
            name = new UnamName(chunk);
        } catch (CantParseIndividualException ex) {
            fail("Can't parse individual");
            return;
        }
        
        Assert.assertEquals("First name should be Sidharta", "Sidharta", name.getFirstName());
        Assert.assertEquals("Last name should be Rodríguez", "Rodríguez", name.getLastName());
        Assert.assertEquals("Title should be ''", "", name.getTitle());
    }

}
