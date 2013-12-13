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
package org.norvelle.addressdiscoverer.parser;

import com.j256.ormlite.support.ConnectionSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.UnparsableIndividual;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ExtractFromFilesTest {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public ExtractFromFilesTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        TestUtilities.setupLogger();
        try {
            connection = TestUtilities.getDBConnection("addresses.test.sqlite");
        } catch (SQLException | CannotLoadJDBCDriverException ex) {
            fail("Encountered problems connecting to database: " + ex.getMessage());
            return;
        }
    }

    @Test
    public void testFilosofia() {
        List<Individual> individuals;
        try {
            individuals = TestUtilities.extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/navarra_filosofia.html",
                    TestUtilities.getTestOutputDirectory() + File.separator + "filosofia.txt"
            );
        } catch (IOException ex) {
            fail("Couldn't extract individuals due to IOException: " + ex.getMessage());
            return;
        }
        Assert.assertEquals(
                String.format("There should be 1 individuals, %d were found", individuals.size()), 
                48, individuals.size());
        for (Individual i: individuals) 
            Assert.assertFalse("There should be no NullIndividuals returned: " + i.toString(), 
                i.getClass().equals(UnparsableIndividual.class));
    }
    
    @Test
    public void testFilologia() {
        List<Individual> individuals;
        try {
            individuals = TestUtilities.extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/navarra_philology.html",
                    TestUtilities.getTestOutputDirectory() + File.separator + "filologia.txt",
                    "windows-1252"
            );
        } catch (IOException ex) {
            fail("Couldn't extract individuals due to IOException: " + ex.getMessage());
            return;
        }
        Assert.assertEquals(
                String.format("There should be 47 individuals, %d were found", individuals.size()), 
                47, individuals.size());
        int numNulls = 0;
        for (Individual i: individuals) {
            if (i.getClass().equals(UnparsableIndividual.class))
                numNulls ++;
        }
        logger.log(Level.INFO, String.format("%d NullIndividuals were found", numNulls));
        int percentageOfNulls = numNulls / individuals.size();
        Assert.assertTrue(String.format("Percentage of nulls is too high: %d", percentageOfNulls), 
                percentageOfNulls < 2);
        /*for (Individual i: individuals) 
            Assert.assertFalse("There should be no NullIndividuals, returned: " + i.toString(), 
                i.getClass().equals(NullIndividual.class));*/
    }
    
    @Test
    public void testEducacion() {
        List<Individual> individuals;
        try {
            individuals = TestUtilities.extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/navarra_educacion.html",
                    TestUtilities.getTestOutputDirectory() + File.separator + "educacion.txt",
                    "iso-8859-1"
            );
        } catch (IOException ex) {
            fail("Couldn't extract individuals due to IOException: " + ex.getMessage());
            return;
        }
        Assert.assertEquals(
                String.format("There should be 55 individuals, %d were found", individuals.size()), 
                55, individuals.size());
        int numNulls = 0;
        for (Individual i: individuals) {
            if (i.getClass().equals(UnparsableIndividual.class))
                numNulls ++;
        }
        logger.log(Level.INFO, String.format("%d NullIndividuals were found", numNulls));
        int percentageOfNulls = numNulls / individuals.size();
        Assert.assertTrue(String.format("Percentage of nulls is too high: %d", percentageOfNulls), 
                percentageOfNulls < 2);
        for (Individual i: individuals) {
            Assert.assertFalse(
                    String.format(
                            "There should not be any first names with punctuation in them: %s", 
                            i.getFirstName()), 
                i.getFirstName().matches(".*(\\p{P}|\\p{S}).*"));
            Assert.assertTrue(
                String.format(
                    "All first names should begin with a letter, but found '%s'", 
                    i.getFirstName().charAt(0)),
                    i.getFirstName().matches("^\\p{L}.*"));
        }
    }
    
}
