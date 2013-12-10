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
@DatabaseTable(tableName = "last_names")
public class KnownLastName {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    
    private static Dao<KnownLastName, String> dao;
    
    @DatabaseField
    private String name;
    
    @DatabaseField(generatedId = true)
    private int id;
    
    /**
     * ORMLite needs a no-arg constructor
     */
    public KnownLastName() {
    }
    
    /**
     * Initialize the first name with a name, plus a unique id.
     * 
     * @param name 
     */
    public KnownLastName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    // ===================== Static Data Manipulation Methods =============================
    
    public static void initialize(ConnectionSource connectionSource) throws SQLException {
        KnownLastName.dao = 
            DaoManager.createDao(connectionSource, KnownLastName.class);
        TableUtils.createTableIfNotExists(connectionSource, KnownLastName.class);
    }
    
    public static boolean isLastName(String name) throws SQLException, OrmObjectNotConfiguredException {
        KnownLastName.checkConfigured();
        /* http://stackoverflow.com/questions/285228/how-to-convert-utf-8-to-us-ascii-in-java
        String strippedName = 
                java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD)
                        .replaceAll("\\p{InCombiningDiacriticalMarks}+",""); */
        // Instead of using a standard charset translator, we translate only vowels
        name = name.replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ü", "u")
                .replace("ß", "ss").replace("ö", "o").replace("ü", "u")
                .replace("ä", "a").replace("ë", "e").replace("è", "e");
        List<KnownLastName> matches = KnownLastName.dao.queryForEq("name", name);
        boolean isMatch = !matches.isEmpty();
        if (isMatch)
            logger.log(Level.FINE, String.format("%s is a last name", name));
        else
            logger.log(Level.FINE, String.format("%s is NOT a last name", name));
        return isMatch;
    }
    
    private static void checkConfigured() throws OrmObjectNotConfiguredException {
        if (KnownLastName.dao == null)
            throw new OrmObjectNotConfiguredException("LastName DAO not configured");
    }
    
}
