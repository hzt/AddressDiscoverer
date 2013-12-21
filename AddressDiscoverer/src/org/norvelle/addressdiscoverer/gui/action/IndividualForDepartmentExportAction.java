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
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;

/**
 * handles exporting Individual records to CSV
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class IndividualForDepartmentExportAction extends AbstractIndividualExportAction {
    
    private final List<Individual> individuals;
    private final int departmentId;
    
    public IndividualForDepartmentExportAction(File file, Department department) 
            throws SQLException 
    {
        super(file);
        this.individuals = Individual.getIndividualsForDepartment(department);
        this.departmentId = department.getId();
    }
    
    public void export() throws IOException, SQLException {
        this.export(this.individuals);

        // Now mark all the exported individuals as having been exported
        try {
            String sql = "UPDATE individual SET exported = 1 WHERE department_id = " 
                    + this.departmentId;
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            this.conn.close();
        } catch (SQLException ex) {
            if (this.conn != null)
                this.conn.close();
            throw ex;
        }
            
    }
}
