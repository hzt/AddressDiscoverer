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
import org.norvelle.addressdiscoverer.gui.ExportProgressDialog;

/**
 * handles exporting Individual records to CSV
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class AllIndividualExportAction extends AbstractIndividualExportAction {
    
    
    
    public AllIndividualExportAction(File file) throws SQLException {
        super(file);
    }
    
    public void export() throws SQLException, IOException {
        //ExportProgressDialog form = new ExportProgressDialog(null);
        //form.setLocationRelativeTo(null);
        //form.setVisible(true);
    }
}
