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
package org.norvelle.addressdiscoverer;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.Institution;
import org.norvelle.addressdiscoverer.model.LastName;

/**
 * Provides routines useful in doing unit tests
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class TestUtilities {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    public static ConnectionSource getDBConnection(String dbName) 
            throws SQLException, CannotLoadJDBCDriverException 
    {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new CannotLoadJDBCDriverException(ex.getMessage());
        }
        Object o = new Object();
        URL dbUrl = o.getClass().getResource(
                "/org/norvelle/addressdiscoverer/resources/" + dbName);
        String dbFilePath = dbUrl.getPath();

        // create a database connection and initialize our tables.
        // All object persistence is managed via ORMLite.
        logger.log(Level.INFO, "Opening SQLite db at {0}", dbFilePath);
        ConnectionSource connectionSource;
        try {
            connectionSource =
                new JdbcConnectionSource("jdbc:sqlite:" + dbFilePath);
        }
        catch (Exception ex) {
            logger.log(Level.SEVERE, "Could not connect to database: {0}", ex.getMessage());
            throw ex;
        }
        Institution.initialize(connectionSource);
        Department.initialize(connectionSource);
        Individual.initialize(connectionSource);
        LastName.initialize(connectionSource);
        
        return connectionSource;
    }
    
}
