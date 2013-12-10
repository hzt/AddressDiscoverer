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
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;

/**
 * Represents an access pathway to our list of last names
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
@DatabaseTable(tableName = "last_names")
public class LastName {
    
    private static Dao<LastName, String> dao;
    
    @DatabaseField
    private String name;
    
    @DatabaseField(generatedId = true)
    private int id;
    
    /**
     * ORMLite needs a no-arg constructor
     */
    public LastName() {
    }
    
    /**
     * Initialize the first name with a name, plus a unique id.
     * 
     * @param name 
     */
    public LastName(String name) {
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
        LastName.dao = 
            DaoManager.createDao(connectionSource, LastName.class);
        TableUtils.createTableIfNotExists(connectionSource, LastName.class);
    }
    
    public static boolean isLastName(String name) throws SQLException, OrmObjectNotConfiguredException {
        LastName.checkConfigured();
        name = name.replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ü", "u")
                .replace("ß", "ss").replace("ö", "o").replace("ü", "u")
                .replace("ä", "a").replace("ë", "e").replace("è", "e");
        List<LastName> matches = LastName.dao.queryForEq("name", name);
        return (!matches.isEmpty());
    }
    
    private static void checkConfigured() throws OrmObjectNotConfiguredException {
        if (LastName.dao == null)
            throw new OrmObjectNotConfiguredException("LastName DAO not configured");
    }
    
}
