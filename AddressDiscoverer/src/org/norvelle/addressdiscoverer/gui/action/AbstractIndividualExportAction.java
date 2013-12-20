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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.norvelle.addressdiscoverer.AddressDiscoverer;
import org.norvelle.addressdiscoverer.model.Individual;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class AbstractIndividualExportAction {

    protected final File file;
    
    public AbstractIndividualExportAction(File file) {
        this.file = file;
    }

    protected void export(List<Individual> individuals) throws IOException, SQLException {
        StringBuilder csvBuilder = new StringBuilder();
        
        // Create out header
        csvBuilder.append("First Name(s)").append("\tLast Name(s)").append("\tEmail")
                .append("\tSalutation").append("\tInstitution").append("\tDepartment")
                .append("\tRole").append("\tGender").append("\tOther").append("\n");
        
        // Write all the individuals out in CSV format
        for (Individual i : individuals) {
            csvBuilder.append(i.getFirstName()).append("\t").append(i.getLastName())
                    .append("\t").append(i.getEmail()).append("\t").append(i.getTitle())
                    .append("\t").append(i.getDepartment().getInstitution().toString())
                    .append("\t").append(i.getDepartment().toString())
                    .append("\t").append(i.getRole()).append("\t").append(i.getGender())
                    .append("\t").append(i.getUnprocessed()).append("\n");
        }
        FileUtils.write(file, csvBuilder, "UTF-8");
        
        // Now mark all the exported individuals as having been exported
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(AddressDiscoverer.application.getJdbcUrl());
            String sql = "UPDATE individual SET exported = 1";
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            conn.close();
        } catch (SQLException ex) {
            if (conn != null)
                conn.close();
            throw ex;
        }
            
    }
    
}
