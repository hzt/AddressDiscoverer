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

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class Individual implements Comparable {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    private static Dao<Individual, String> dao;
    
    @DatabaseField
    private String firstName;
    
    @DatabaseField
    private String lastName;
    
    @DatabaseField
    private String email;
    
    @DatabaseField
    private String title;

    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField(canBeNull = false, foreign = true)
    private Department department;
    
    /**
     * A constructor without arguments, required by ORMlite
     */
    public Individual() {}
    
    public Individual(String firstName, String lastName, String email, String title, Department department) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.title = title;
        this.department = department;
    }

    @Override
    public String toString() {
        return String.format("%s, %s <%s>", this.lastName, this.firstName, this.email);
    }

    @Override
    public int compareTo(Object o) {
        String otherString = o.toString();
        return this.toString().compareTo(otherString);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public int getId() {
        return id;
    }
    

    // ===================== Static Data Manipulation Methods =============================
    
    public static void initialize(ConnectionSource connectionSource) throws SQLException {
        Individual.dao = 
            DaoManager.createDao(connectionSource, Individual.class);
        TableUtils.createTableIfNotExists(connectionSource, Individual.class);
    }
    
    public static Individual getById(String id) throws SQLException, OrmObjectNotConfiguredException {
        Individual.checkConfigured();
        return Individual.dao.queryForId(id);
    }
    
    public static Individual create(String firstName, String lastName, 
            String email, String title, Department department) 
            throws SQLException, OrmObjectNotConfiguredException {
        Individual.checkConfigured();
        Individual i = new Individual(firstName, lastName, email, title, department);
        Individual.dao.create(i);
        return i;
    }
    
    public static void update(Individual i) throws SQLException {
        Individual.dao.update(i);
    }
    
    public static void delete(Individual i) throws SQLException {
        Individual.dao.delete(i);
    }
    
    private static void checkConfigured() throws OrmObjectNotConfiguredException {
        if (Individual.dao == null)
            throw new OrmObjectNotConfiguredException("Individual DAO not configured");
    }
    
    public static HashMap<Integer, Individual> getIndividualsForDepartment(
            Department department) throws OrmObjectNotConfiguredException, SQLException {
        Individual.checkConfigured();
        List<Individual> results =
            Individual.dao.queryBuilder().where().
              eq("department_id", department).query();
        HashMap<Integer, Individual> individuals = new HashMap();
        for (Individual i : results) {
            individuals.put(i.getId(), i);
        }
        return individuals;
    }

    public static void deleteIndividualsForDepartment(
            Department department) throws OrmObjectNotConfiguredException, SQLException {
        Individual.checkConfigured();
        List<Individual> results =
            Individual.dao.queryBuilder().where().
              eq("department_id", department).query();
        for (Individual i : results) {
            Individual.delete(i);
        }
    }

}
