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

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class NameTest {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public NameTest() {
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
        String chunk = "Dra. Concepción Martínez Pasamar";
        Name name;
        name = new Name(chunk); 
        
        Assert.assertFalse("The Name object should not have a score of 0", name.getScore() == 0.0);
        Assert.assertEquals("First name should be Concepción", "Concepción", name.getFirstName());
        Assert.assertEquals("Last name should be Martínez Pasamar", "Martínez Pasamar", name.getLastName());
        Assert.assertEquals("Title should be Dra.", "Dra.", name.getTitle());
    }

    @Test
    public void testLizasoain() {
        String chunk = "Lizasoain Rumeu, Olga";
        Name name;
        name = new Name(chunk); 
        
        Assert.assertFalse("The Name object should not have a score of 0", name.getScore() == 0.0);
        Assert.assertEquals("First name should be Olga", "Olga", name.getFirstName());
        Assert.assertEquals("Last name should be Lizasoain Rumeu", "Lizasoain Rumeu", name.getLastName());
        Assert.assertEquals("Title should be ''", "", name.getTitle());
    }

    @Test
    public void testCommaSeparated() {
        String chunk = "Martínez Pasamar, Dra. Concepción";
        Name name;
        name = new Name(chunk); 
        
        Assert.assertFalse("The Name object should not have a score of 0", name.getScore() == 0.0);
        Assert.assertEquals("First name should be Concepción", "Concepción", name.getFirstName());
        Assert.assertEquals("Last name should be Martínez Pasamar", "Martínez Pasamar", name.getLastName());
        Assert.assertEquals("Title should be Dra.", "Dra.", name.getTitle());
    }
    
}
