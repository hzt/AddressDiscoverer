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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.norvelle.addressdiscoverer.exceptions.CannotStoreNullIndividualException;
import org.norvelle.addressdiscoverer.exceptions.IndividualHasNoDepartmentException;

/**
 * Represents an individual found to be associated with a Department; tracks
 * his or her first and last names, email and title.
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
    private String fullName;
    
    @DatabaseField
    private String email;
    
    @DatabaseField
    private String title;

    @DatabaseField
    private String affiliation;
    
    @DatabaseField
    private String role;
    
    @DatabaseField
    private String gender;
    
    @DatabaseField
    private String unprocessed;
    
    @DatabaseField
    private String parserName;
    
    @DatabaseField
    private String originalText;
    
    @DatabaseField
    private boolean exported;    

    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField(canBeNull = false, foreign = true)
    private Department department;
    
    /**
     * A constructor without arguments, required by ORMlite
     */
    public Individual() {}
    
    public Individual(Name name, String email, String unprocessed, Department department) {
        this.firstName = name.getFirstName();
        this.lastName = name.getLastName();
        this.fullName = name.getFullName();
        this.email = email;
        this.title = name.getTitle();
        this.department = department;
        this.affiliation = "";
        this.unprocessed = unprocessed;
        this.parserName = "";
    }
    
    public Individual(UnamName name, String email, String unprocessed, Department department) {
        this.firstName = name.getFirstName();
        this.lastName = name.getLastName();
        this.fullName = name.getFullName();
        this.email = email;
        this.title = name.getTitle();
        this.department = department;
        this.affiliation = "";
        this.unprocessed = unprocessed;
        this.parserName = "";
    }
    
    public Individual(Name name, String email, String affiliation, String unprocessed, String parserName, Department department) {
        this.firstName = name.getFirstName();
        this.lastName = name.getLastName();
        this.fullName = name.getFullName();
        this.email = email;
        this.title = name.getTitle();
        this.department = department;
        this.affiliation = "";
        this.unprocessed = unprocessed;
        this.parserName = parserName;
    }
    
    public Individual(String firstName, String lastName, String fullName, String email, 
            String title, String affiliation, String unprocessed, String parserName, Department department) 
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.email = email;
        this.title = title;
        this.department = department;
        this.affiliation = affiliation;
        this.unprocessed = unprocessed;
        this.parserName = parserName;
    }

    public Individual(String firstName, String lastName, String fullName,
            String email, String title, String affiliation, String unprocessed, String parserName) 
    {
        this(firstName, lastName, fullName, email, title, affiliation, unprocessed, parserName, null);
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
    
    public double getScore() {
        double score = 0.0;
        if (!this.firstName.isEmpty()) score += 2.0;
        if (!this.lastName.isEmpty()) score += 3.0;
        if (!this.email.isEmpty()) score += 5.0;
        if (!this.affiliation.isEmpty()) score += 1.0;
        if (!this.title.isEmpty()) score += 1.0;
        score += StringUtils.split(this.unprocessed).length;

        return score / 5;
    }

    // ===================== Getters and setters =============================
    
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getUnprocessed() {
        return unprocessed;
    }

    public void setUnprocessed(String unprocessed) {
        this.unprocessed = unprocessed;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getParserName() {
        return parserName;
    }

    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        this.exported = exported;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    // ===================== Static Data Manipulation Methods =============================
    
    public static void initialize(ConnectionSource connectionSource) throws SQLException {
        Individual.dao = 
            DaoManager.createDao(connectionSource, Individual.class);
        TableUtils.createTableIfNotExists(connectionSource, Individual.class);
    }
    
    public static Individual getById(String id) throws SQLException {
        return Individual.dao.queryForId(id);
    }
    
    /**
     * Tell OrmLite to store this Individual as data in the SQLite backend
     * 
     * @param i The Individual to store
     * @throws SQLException
     * @throws IndividualHasNoDepartmentException
     * @throws CannotStoreNullIndividualException 
     */
    public static void store(Individual i) throws SQLException, 
            IndividualHasNoDepartmentException, 
            CannotStoreNullIndividualException 
    {
        if (i.getClass().equals(UnparsableIndividual.class))
            throw new CannotStoreNullIndividualException(i);
        if (i.getDepartment() == null) 
            throw new IndividualHasNoDepartmentException();
        Individual.dao.create(i);
    }
    
    public static void update(Individual i) throws SQLException {
        Individual.dao.update(i);
    }
    
    public static void delete(Individual i) throws SQLException {
        Individual.dao.delete(i);
    }
    
    public static List<Individual> getIndividualsForDepartment(
            Department department) throws SQLException {
        List<Individual> results =
            Individual.dao.queryBuilder().where().
              eq("department_id", department).query();
        for (Individual i : results)
            i.getDepartment().setName(department.getName()); // Hack
        return results;
    }

    public static List<Individual> getAll() 
            throws SQLException 
    {
        List<Individual> results = new ArrayList();
        List<Institution> institutions = Institution.getAll();
        for (Institution institution : institutions) {
            HashMap<Integer, Department> departments = Department.getDepartmentsForInstitution(institution);
            for (Department department : departments.values()) {
                department.setInstitution(institution);
                List<Individual> individuals = Individual.getIndividualsForDepartment(department);
                for (Individual individual : individuals) {
                    individual.setDepartment(department);
                    results.add(individual);
                }
            }
        }
        //Individual.dao.queryForAll();
        return results;
    }

    public static void deleteIndividualsForDepartment(Department department) 
            throws SQLException 
    {
        List<Individual> results =
            Individual.dao.queryBuilder().where().
              eq("department_id", department).query();
        for (Individual i : results) {
            Individual.delete(i);
        }
    }
    
    public static long getCount() throws SQLException {
        long total = Individual.dao.countOf();
        return total;
    }


}
