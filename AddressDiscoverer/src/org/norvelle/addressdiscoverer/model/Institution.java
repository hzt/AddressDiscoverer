/**
 *  Part of the AddressDiscoverer project, licensed under the GPL v.2 license. 
 *  This project provides intelligence for discovering email addresses in 
 *  specified web pages, associating them with a given institution and department 
 *  and address type.
 */

package org.norvelle.addressdiscoverer.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;

/**
 * Represents a single institution. Uses the ORMLite framework for persistence.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */

@DatabaseTable(tableName = "institution")
public class Institution implements Comparable {
    
    private final HashMap<String, Department> departments = new HashMap();
    private static Dao<Institution, String> dao;
    
    @DatabaseField
    private String name;
    
    @DatabaseField
    private String country;
    
    @DatabaseField
    private String city;
    
    @DatabaseField
    private String affiliation;
    
    @DatabaseField(generatedId = true)
    private int id;
    
    /**
     * ORMLite needs a no-arg constructor
     */
    public Institution() {
    }
    
    /**
     * Initialize the institution with a name, plus a unique id.
     * 
     * @param name 
     */
    public Institution(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public int getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    @Override
    public int compareTo(Object o) {
        String otherString = o.toString();
        return this.name.compareTo(otherString);
    }

    // ===================== Static Data Manipulation Methods =============================
    
    public static void initialize(ConnectionSource connectionSource) throws SQLException {
        Institution.dao = 
            DaoManager.createDao(connectionSource, Institution.class);
        TableUtils.createTableIfNotExists(connectionSource, Institution.class);
    }
    
    public static Institution getById(String id) throws SQLException {
        return Institution.dao.queryForId(id);
    }
    
    public static List<Institution> getAll() throws SQLException {
        return Institution.dao.queryForAll();
    }
    
    public static Institution create(String name) throws SQLException {
        Institution i = new Institution(name);
        Institution.dao.create(i);
        return i;
    }
    
    public static void update(Institution i) throws SQLException {
        Institution.dao.update(i);
    }
    
    public static void delete(Institution i) throws SQLException {
        Department.deleteDepartmentsForInstitution(i);
        Institution.dao.delete(i);
    }
    
    public static HashMap<Integer, Institution> getInstitutions() {
        HashMap<Integer, Institution> institutions = new HashMap();
        for (Institution i : Institution.dao) {
            institutions.put(i.getId(), i);
        }
        return institutions;
    }
    
    public static long getCount() throws SQLException {
        long total = Institution.dao.countOf();
        return total;
    }

}
