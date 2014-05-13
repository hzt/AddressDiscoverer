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
package org.norvelle.addressdiscoverer.gui.threading;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.apache.commons.io.FileUtils;
import org.norvelle.addressdiscoverer.AddressDiscoverer;
import org.norvelle.addressdiscoverer.gui.ExportProgressDialog;
import org.norvelle.addressdiscoverer.model.Individual;

/**
 * A SwingWorker to handle setting genders for all Individuals in the background,
 * allowing the progress bar to be painted while the operation is ongoing.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ExportAllIndividualsWorker 
    extends SwingWorker<Integer, Integer> 
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final ExportProgressDialog dialog;
    private final File file;
    private Connection conn;
    private final String jdbcUrl;

    public ExportAllIndividualsWorker(File file, ExportProgressDialog dialog) 
    {
        this.dialog = dialog;
        this.file = file;
        // create a database connection and initialize our tables.
        // All object persistence is managed via ORMLite.
        String dbFilename = AddressDiscoverer.application.getSettingsDirname()
                + File.separator + "addresses.sqlite";
        this.jdbcUrl = "jdbc:sqlite:" + dbFilename;
     }

    @Override
    protected Integer doInBackground() throws Exception {
        conn = DriverManager.getConnection(this.jdbcUrl);
        List<Individual> individuals = Individual.getAll();
        dialog.getjExportProgressBar().setIndeterminate(false);
        dialog.getjExportProgressBar().setMaximum(individuals.size());
        StringBuilder csvBuilder = new StringBuilder();
        
        // Create out header
        csvBuilder.append("Gender").append("\tFirst Name(s)").append("\tLast Name(s)")
                .append("\tEmail").append("\tSalutation").append("\tInstitution")
                .append("\tDepartment").append("\tRole").append("\tOther").append("\n")
                .append("\tSalutation").append("\tGreeting").append("\tProfesor")
                .append("\tInvestigador").append("\n");
        
        // Write all the individuals out in CSV format
        int count = 1;
        for (Individual i : individuals) {
            String salutation, greeting, profesor, investigador;
            if ("M".equals(i.getGender())) {
                salutation = "Dr.";
                greeting = "Estimado";
                profesor = "profesor";
                investigador = "investigador";
            }
            else {
                salutation = "Dra.";
                greeting = "Estimada";
                profesor = "profesora";
                investigador = "investigadora";
            }
            csvBuilder.append(i.getGender()).append("\t").append(i.getFirstName())
                    .append("\t").append(i.getLastName())
                    .append("\t").append(i.getEmail())
                    .append("\t").append(i.getTitle())
                    .append("\t").append(i.getDepartment().getInstitution().toString())
                    .append("\t").append(i.getDepartment().toString())
                    .append("\t").append(i.getRole())
                    .append("\t").append(i.getUnprocessed())
                    .append("\t").append(salutation)
                    .append("\t").append(greeting)
                    .append("\t").append(profesor)
                    .append("\t").append(investigador)
                    .append("\n");
            dialog.getjExportProgressBar().setValue(count ++);
        }
        FileUtils.write(file, csvBuilder, "UTF-8");
        
        // Now mark all the exported individuals as having been exported
        try {
            String sql = "UPDATE individual SET exported = 1";
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            this.conn.close();
        } catch (SQLException ex) {
            if (this.conn != null)
                this.conn.close();
            dialog.setVisible(false);
            AddressDiscoverer.reportException(ex);
        }
        dialog.setVisible(false);
        dialog.dispose();

        return 0;
    }

    /**
     * This method receives the signals that the doInBackground method sends out,
     * allowing the SwingWorker to periodically check those signals and process
     * them here.
     *
     * @param progressUpdates
     */
    @Override
    protected void process(final List<Integer> progressUpdates) {
        for (final int progress : progressUpdates) {
            //this.parent.setCurrProgress(progress);
        }
    }
    
}
