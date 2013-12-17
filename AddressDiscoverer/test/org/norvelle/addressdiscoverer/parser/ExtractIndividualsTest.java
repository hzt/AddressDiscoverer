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
import java.util.logging.Logger;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.norvelle.addressdiscoverer.TestUtilities;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.exceptions.IndividualExtractionFailedException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.UnparsableIndividual;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ExtractIndividualsTest {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private static ConnectionSource connection;

    public ExtractIndividualsTest() {
    }

    @BeforeClass
    @SuppressWarnings("UnnecessaryReturnStatement")
    public static void setUpClass() {
        TestUtilities.setupLogger();
        try {
            connection = TestUtilities.getDBConnection("addresses.test.sqlite");
        } catch (SQLException | CannotLoadJDBCDriverException |IOException ex) {
            fail("Encountered problems connecting to database: " + ex.getMessage());
            return;
        }
    }

    @Test
    public void testCmpasamar() {
        List<Individual> individuals;
        try {
            individuals = TestUtilities.extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/cmpasamar.html",
                    TestUtilities.getTestOutputDirectory() + File.separator + "cmpasamar.txt"
            );
        } catch (IOException | SQLException | OrmObjectNotConfiguredException
                | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
            return;
        }
        Assert.assertEquals(
                String.format("There should be 1 individual, %d were found", individuals.size()), 1, individuals.size());
        for (Individual i: individuals) 
            Assert.assertFalse("There should be no NullIndividuals returned: " + i.toString(), 
                i.getClass().equals(UnparsableIndividual.class));
        Individual cmpasamar = individuals.get(0);
        Assert.assertEquals("The individual's first name should be Concepción", 
                "Concepción", cmpasamar.getFirstName());
        Assert.assertEquals("The individual's first name should be Dra.", 
                "Dra.", cmpasamar.getTitle());
        Assert.assertEquals("The individual's last name should be Martínez Pasamar", 
                "Martínez Pasamar", cmpasamar.getLastName());
        Assert.assertEquals("The individual's email should be cmpasamar@unav.es", 
                "cmpasamar@unav.es", cmpasamar.getEmail());
    }
    
    @Test
    public void testMzugasti() {
        List<Individual> individuals;
        try {
            individuals = TestUtilities.extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/mzugasti.html",
                    TestUtilities.getTestOutputDirectory() + File.separator + "mzugasti.txt"
            );
        } catch (IOException | SQLException | OrmObjectNotConfiguredException
                | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
            return;
        }
        Assert.assertEquals(
                String.format("There should be 1 individual, %d were found", individuals.size()), 1, individuals.size());
        for (Individual i: individuals) 
            Assert.assertFalse("There should be no NullIndividuals returned: " + i.toString(), 
                i.getClass().equals(UnparsableIndividual.class));
        Individual mzugasti = individuals.get(0);
        Assert.assertEquals("The individual's first name should be Miguel", 
                "Miguel", mzugasti.getFirstName());
        Assert.assertEquals("The individual's title should be Dr.", 
                "Dr.", mzugasti.getTitle());
        Assert.assertEquals("The individual's last name should be Zugasti Zugasti", 
                "Zugasti Zugasti", mzugasti.getLastName());
        Assert.assertEquals("The individual's email should be cmpasamar@unav.es", 
                "mzugasti@unav.es", mzugasti.getEmail());
        Assert.assertEquals("The remaining text should be 'Literatura Hispánica y Teoría de la Literatura'", 
                "Literatura Hispánica y Teoría de la Literatura", mzugasti.getUnprocessed());
    }
    
    @Test
    public void testLmflamarique() {
        List<Individual> individuals;
        String outputFile = TestUtilities.getTestOutputDirectory() + File.separator + "lflamarique.txt";
        try {
            individuals = TestUtilities.extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/lflamarique.html",
                    outputFile
            );
        } catch (IOException | SQLException | OrmObjectNotConfiguredException
                | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
            return;
        }
        Assert.assertEquals(
                String.format("There should be 1 individual, %d were found", individuals.size()), 1, individuals.size());
        for (Individual i: individuals) 
            Assert.assertFalse("There should be no NullIndividuals returned: " + i.toString(), 
                i.getClass().equals(UnparsableIndividual.class));
        Individual lmflamarique = individuals.get(0);
        Assert.assertEquals("The individual's first name should be Lourdes", 
                "Lourdes", lmflamarique.getFirstName());
        Assert.assertEquals("The individual's title should be Dr.", 
                "", lmflamarique.getTitle());
        Assert.assertEquals("The individual's last name should be Flamarique", 
                "Flamarique", lmflamarique.getLastName());
        Assert.assertEquals("The individual's email should be lflamarique@unav.es", 
                "lflamarique@unav.es", lmflamarique.getEmail());
        Assert.assertEquals("The remaining text should be ''", 
                "", lmflamarique.getUnprocessed());
    }
    
    @Test
    public void testOlizas() {
        List<Individual> individuals;
        try {
            individuals = TestUtilities.extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/olizas.html",
                    TestUtilities.getTestOutputDirectory() + File.separator + "olizas.txt"
            );
        } catch (IOException | SQLException | OrmObjectNotConfiguredException
                | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
            return;
        }
        Assert.assertEquals(
                String.format("There should be 1 individual, %d were found", 
                        individuals.size()), 1, individuals.size());
        for (Individual i: individuals) 
            Assert.assertFalse("There should be no NullIndividuals returned: " + i.toString(), 
                i.getClass().equals(UnparsableIndividual.class));
        Individual olizas = individuals.get(0);
        Assert.assertEquals("The individual's first name should be Olga", 
                "Olga", olizas.getFirstName());
        Assert.assertEquals("The individual's title should be ''", 
                "", olizas.getTitle());
        Assert.assertEquals("The individual's last name should be Lizasoain Rumeu", 
                "Lizasoain Rumeu", olizas.getLastName());
        Assert.assertEquals("The individual's email should be cmpasamar@unav.es", 
                "olizas@unav.es", olizas.getEmail());
    }
    
    //@Test
    public void testThreeColumnRecords() {
        List<Individual> individuals;
        try {
            individuals = TestUtilities.extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/ThreeFieldsAcrossNames.html",
                    TestUtilities.getTestOutputDirectory() + File.separator + "individuals.txt"
            );
        } catch (IOException | SQLException | OrmObjectNotConfiguredException
                | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
            return;
        }
        Assert.assertEquals(
                String.format("There should be 3 individuals, %d were found", individuals.size()), 
                individuals.size() == 3);
        for (Individual i: individuals) 
            Assert.assertFalse("There should be no NullIndividuals returned: " + i.toString(), 
                i.getClass().equals(UnparsableIndividual.class));
    }
    
    @Test
    public void testAenciso() {
        List<Individual> individuals;
        try {
            individuals = TestUtilities.extractIndividuals(
                    "/org/norvelle/addressdiscoverer/resources/agenciso.html",
                    TestUtilities.getTestOutputDirectory() + File.separator + "agenciso.txt"
            );
        } catch (IOException | SQLException | OrmObjectNotConfiguredException
                | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
            return;
        }
        Assert.assertEquals(
                String.format("There should be 1 individual, %d were found", individuals.size()), 1, individuals.size());
        for (Individual i: individuals) 
            Assert.assertFalse("There should be no NullIndividuals returned: " + i.toString(), 
                i.getClass().equals(UnparsableIndividual.class));
        Individual aenciso = individuals.get(0);
        Assert.assertEquals("The individual's first name should be Agustín", 
                "Agustín", aenciso.getFirstName());
        Assert.assertEquals("The individual's title should be Dr.", 
                "Dr.", aenciso.getTitle());
        Assert.assertEquals("The individual's last name should be González Enciso", 
                "González Enciso", aenciso.getLastName());
        Assert.assertEquals("The individual's email should be agenciso@unav.es", 
                "agenciso@unav.es", aenciso.getEmail());
        //Assert.assertEquals("The remaining text should be 'Catedrático Historia Moderna'", 
        //        "Catedrático Historia Moderna", aenciso.getUnprocessed());
    }
    
    @Test
    public void testSarda() {
        try {
            TestUtilities.testOneTr("jalmirallsa@ub.edu", "", "Jaume", "Almirall Sarda", 
                    "Professor associat Dept Filologia Grega");
        } catch (IOException | SQLException | OrmObjectNotConfiguredException | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
        }
    }

    @Test
    public void testDelval() {
        try {
            TestUtilities.testOneTr("adeval@unav.es", "", "M. A.", "Alonso Del Val", "email");
        } catch (IOException | SQLException | OrmObjectNotConfiguredException | IndividualExtractionFailedException ex) {
            fail("Couldn't extract individuals due to exception: " + ex.getMessage());
        }
    }
}
