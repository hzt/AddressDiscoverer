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
package org.norvelle.addressdiscoverer.gui.action;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.norvelle.addressdiscoverer.AddressDiscoverer;
import org.norvelle.addressdiscoverer.exceptions.CannotLoadJDBCDriverException;

/**
 * Given a likeClause, moves Individuals from the individual table to the deleted_individuals
 * table.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class DatabasePruner {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    
    // Our database connection
    private String likeClause;
    private String field;
    private final String jdbcUrl;

    public DatabasePruner() throws SQLException, CannotLoadJDBCDriverException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Could not load SQLite JDBC driver: {0}", ex.getMessage());
            throw new CannotLoadJDBCDriverException(ex.getMessage());
        }

        // create a database connection and initialize our tables.
        // All object persistence is managed via ORMLite.
        String dbFilename = AddressDiscoverer.application.getSettingsDirname()
                + File.separator + "addresses.sqlite";
        this.jdbcUrl = "jdbc:sqlite:" + dbFilename;
    }
    
    /**
     * Given a regular expression and a field to apply it to, sets the private
     * fields and returns the number of rows that would be affected by the change.
     * 
     * @param likeClause The regular expression to be applied
     * @param field The field name to run the LIKE clause against
     * @return The number of Individuals that would be moved to the deleted_individuals table.
     * @throws java.sql.SQLException
     */
    public int setLikeClauseAndField(String likeClause, String field) throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(this.jdbcUrl);
            this.likeClause = likeClause;
            this.field = field;
            String sql = String.format(
                    "SELECT COUNT(*) AS total FROM individual WHERE %s LIKE '%%%s%%'",
                    this.field, this.likeClause);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            int numAffected = rs.getInt("total");
            conn.close();
            return numAffected;
        } catch (SQLException ex) {
            if (conn != null)
                conn.close();
            throw ex;
        }
    }
    
    /**
     * Run the pruner with the parameters tested by the SetRegexAndField method
     * 
     * @throws SQLException
     */
    @SuppressWarnings("ConvertToTryWithResources")
    public void runPrune() throws SQLException {
        Connection conn = null;
        try {
            // First back the records up to the deleted_individuals table
            conn = DriverManager.getConnection(this.jdbcUrl);
            Statement stmt = conn.createStatement();
            String sql = String.format(
                    "INSERT INTO deleted_individuals SELECT * FROM individual WHERE %s LIKE '%%%s%%'",
                    this.field, this.likeClause);
            stmt.execute(sql);
            
            // Save a timestamp so we can later do a rollback
            String sql2 = "UPDATE deleted_individuals SET timestamp = "
                    + "datetime('now','localtime') WHERE timestamp is NULL";
            stmt.execute(sql2);
            
            // Now, perform the delete itself
            String sql3 = String.format("DELETE FROM individual WHERE %s LIKE '%%%s%%'",
                    this.field, this.likeClause);
            stmt.execute(sql3);
            
            conn.close();
        }  catch (SQLException ex) {
            if (conn != null)
                conn.close();
            throw ex;
        }
    }
    
}
