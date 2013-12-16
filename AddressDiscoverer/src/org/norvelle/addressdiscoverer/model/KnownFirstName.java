/**
 * Part of the AddressDiscoverer project, licensed under the GPL v.3 license.
 * This project provides intelligence for discovering email addresses in
 * specified web pages, associating them with a given firstName and department
 * and address type.
 *
 * This project is licensed under the GPL v.3. Your rights to copy and modify
 * are regulated by the conditions specified in that license, available at
 * http://www.gnu.org/licenses/gpl-3.0.html
 */
package org.norvelle.addressdiscoverer.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;

/**
 * Represents an access pathway to our list of last names
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
@DatabaseTable(tableName = "first_names")
public class KnownFirstName {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    
    private static Dao<KnownFirstName, String> dao;
    
    @DatabaseField
    private String name;
    
    /**
     * ORMLite needs a no-arg constructor
     */
    public KnownFirstName() {
    }
    
    /**
     * Initialize the first name with a name, plus a unique id.
     * 
     * @param name 
     */
    public KnownFirstName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name;
    }
    
    // ===================== Static Data Manipulation Methods =============================
    
    public static void initialize(ConnectionSource connectionSource) throws SQLException {
        KnownFirstName.dao = 
            DaoManager.createDao(connectionSource, KnownFirstName.class);
        TableUtils.createTableIfNotExists(connectionSource, KnownFirstName.class);
    }
    
    public static boolean isFirstName(String name) throws SQLException, OrmObjectNotConfiguredException {
        KnownFirstName.checkConfigured();
        // First, check something easy... if the name has a hyphen, it's a last name
        if (name.contains("-")) return true;
        
        // Instead of using a standard charset translator, we translate only vowels
        name = name.replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ü", "u")
                .replace("ß", "ss").replace("ö", "o").replace("ü", "u")
                .replace("ä", "a").replace("ë", "e").replace("è", "e");
        List<KnownFirstName> matches = KnownFirstName.dao.queryForEq("name", name);
        boolean isMatch = !matches.isEmpty();
        if (isMatch)
            logger.log(Level.FINE, String.format("%s is a first name", name));
        else
            logger.log(Level.FINE, String.format("%s is NOT a first name", name));
        return isMatch;
    }
    
    public static KnownFirstName get(String name) throws SQLException, OrmObjectNotConfiguredException {
        KnownFirstName.checkConfigured();
        List<KnownFirstName> matches =  KnownFirstName.dao.queryForEq("name", name);
        if (!matches.isEmpty())
            return matches.get(0);
        else return null;
    }
    
    /**
     * Tell OrmLite to store this KnownLastName as data in the SQLite backend
     * 
     * @param name
     * @throws SQLException
     * @throws OrmObjectNotConfiguredException 
     */
    public static void store(KnownFirstName name) throws SQLException, 
            OrmObjectNotConfiguredException 
    {
        KnownFirstName.checkConfigured();
        List<KnownFirstName> matches = KnownFirstName.dao.queryForEq("name", name);
        boolean isMatch = !matches.isEmpty();
        if (! isMatch)
            KnownFirstName.dao.create(name);
    }
    
    public static void delete(KnownFirstName k) throws SQLException {
        KnownFirstName.dao.delete(k);
    }
    
    private static void checkConfigured() throws OrmObjectNotConfiguredException {
        if (KnownFirstName.dao == null)
            throw new OrmObjectNotConfiguredException("FirstName DAO not configured");
    }
    
}
