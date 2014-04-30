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
package org.norvelle.addressdiscoverer.parse;

import org.norvelle.addressdiscoverer.model.*;
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
public class UrlResolverTest {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public UrlResolverTest() {
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
    public void testMoreso() {
        ContactLinkLocator.baseUrl = "http://www.upf.edu/filosofiadeldret/en/professors/permanents/";
        String resolvedUrl = ContactLinkLocator.resolveAddress("/filosofiadeldret/en/professors/permanents/moreso.html");
        Assert.assertEquals("Bad URL resolution", "http://www.upf.edu/filosofiadeldret/en/professors/permanents/moreso.html", resolvedUrl);
    }
    
    @Test
    public void testBazalcorrales() {
        ContactLinkLocator.baseUrl = "http://www.unav.es/arquitectura/profesores/claustro/";
        String resolvedUrl = ContactLinkLocator.resolveAddress("http://www.unav.es/arquitectura/profesores/cv/bazalcorralesjesus/");
        Assert.assertEquals("Bad URL resolution", "http://www.unav.es/arquitectura/profesores/cv/bazalcorralesjesus/", resolvedUrl);
    }
    
    @Test
    public void testSanchez() {
        ContactLinkLocator.baseUrl = "http://directori.ub.edu/dir/?accio=SRCH&unitat=65541";
        String resolvedUrl = ContactLinkLocator.resolveAddress("./?accio=SEL&amp;id=1h6zw0m31i67t72r&amp;lang=ca");
        Assert.assertEquals("Bad URL resolution", "http://directori.ub.edu/dir/?accio=SEL&id=1h6zw0m31i67t72r&lang=ca", resolvedUrl);
    }
    
}
