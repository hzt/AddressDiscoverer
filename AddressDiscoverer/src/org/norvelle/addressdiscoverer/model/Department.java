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
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.norvelle.utils.Utils;

/**
 * The Department object represents a department or center associated with an
 * institution. Its data persistence is managed by ORMLite.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
@DatabaseTable(tableName = "department")
public class Department implements Comparable {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    private final HashMap<String, Individual> individuals = new HashMap();
    private static Dao<Department, String> dao;
    
    @DatabaseField
    private String name;
    
    @DatabaseField
    private String webAddress;
    
    @DatabaseField
    private String html;
    
    @DatabaseField
    private String director;
    
    @DatabaseField
    private String directorEmail;
    
    @DatabaseField
    private String baseUrl;
    
    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField(canBeNull = false, foreign = true)
    private Institution institution;
    
    /**
     * A constructor without arguments, required by ORMlite
     */
    public Department() {}
    
    public Department(String name, Institution institution) {
        this.name = name;
        this.institution = institution;
    }
    
    @Override
    public String toString() {
        return Utils.chop(this.name, 50);
    }

    @Override
    public int compareTo(Object o) {
        String otherString = o.toString();
        return this.name.compareTo(otherString);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getDirectorEmail() {
        return directorEmail;
    }

    public void setDirectorEmail(String directorEmail) {
        this.directorEmail = directorEmail;
    }

    public int getId() {
        return id;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getWebAddress() {
        return webAddress;
    }

    public void setWebAddress(String webAddress) {
        this.webAddress = webAddress;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    // ===================== Static Data Manipulation Methods =============================
    
    public static void initialize(ConnectionSource connectionSource) throws SQLException {
        Department.dao = 
            DaoManager.createDao(connectionSource, Department.class);
        TableUtils.createTableIfNotExists(connectionSource, Department.class);
    }
    
    public static Department getById(String id) throws SQLException {
        return Department.dao.queryForId(id);
    }
    
    public static List<Department> getAll() throws SQLException {
        return Department.dao.queryForAll();
    }
    
    public static Department create(String name, Institution institution) 
            throws SQLException {
        Department i = new Department(name, institution);
        Department.dao.create(i);
        return i;
    }
    
    public static void update(Department i) throws SQLException {
        Department.dao.update(i);
    }
    
    public static void delete(Department d) throws SQLException {
        Individual.deleteIndividualsForDepartment(d);
        Department.dao.delete(d);
    }
    
    public static HashMap<Integer, Department> getDepartmentsForInstitution(
            Institution institution) throws SQLException {
        List<Department> results =
            Department.dao.queryBuilder().where().
              eq("institution_id", institution).query();
        HashMap<Integer, Department> departments = new HashMap();
        for (Department d : results) {
            departments.put(d.getId(), d);
            d.getInstitution().setName(institution.getName()); // This is a hack
        }
        return departments;
    }

    public static void deleteDepartmentsForInstitution(
            Institution institution) throws SQLException 
    {
        List<Department> results =
            Department.dao.queryBuilder().where().
              eq("institution_id", institution).query();
        HashMap<Integer, Department> departments = new HashMap();
        for (Department d : results) {
            Individual.deleteIndividualsForDepartment(d);
            Department.delete(d);
        }
    }
    
    public static long getCount() throws SQLException {
        long total = Department.dao.countOf();
        return total;
    }

}
