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
public class GenderDeterminerTest {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public GenderDeterminerTest() {
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
    public void testJuan() {
        String name = "Juan";
        GenderDeterminer.Gender gender = GenderDeterminer.getGender(name);
        Assert.assertEquals("Gender should be male", GenderDeterminer.Gender.MALE, gender);
    }
    
    @Test
    public void testCarmen() {
        String name = "Carmen";
        GenderDeterminer.Gender gender = GenderDeterminer.getGender(name);
        Assert.assertEquals("Gender should be female", GenderDeterminer.Gender.FEMALE, gender);
    }
    
    @Test
    public void testZanzibar() {
        String name = "Zanzibar";
        GenderDeterminer.Gender gender = GenderDeterminer.getGender(name);
        Assert.assertEquals("Gender should be unknown", GenderDeterminer.Gender.UNKNOWN, gender);
    }
    
    @Test
    public void testVíctor() {
        String name = "Víctor";
        GenderDeterminer.Gender gender = GenderDeterminer.getGender(name);
        Assert.assertEquals("Gender should be Male", GenderDeterminer.Gender.MALE, gender);
    }
    
    @Test
    public void testMaría() {
        String name = "María";
        GenderDeterminer.Gender gender = GenderDeterminer.getGender(name);
        Assert.assertEquals("Gender should be Female", GenderDeterminer.Gender.FEMALE, gender);
    }
    
}
