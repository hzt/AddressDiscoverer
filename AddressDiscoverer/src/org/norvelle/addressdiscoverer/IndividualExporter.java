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
package org.norvelle.addressdiscoverer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;

/**
 * handles exporting Individual records to CSV
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class IndividualExporter {
    
    private final File file;
    private final List<Individual> individuals;
    private final Department department;
    
    public IndividualExporter(File file, List<Individual> individuals, Department department) {
        this.file = file;
        this.individuals = individuals;
        this.department = department;
    }
    
    public void export() throws IOException {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("first").append("\tlast").append("\temail").append("\ttitle")
                .append("\tinstitution").append("\tdepartment").append("\tother").append("\n");
        for (Individual i : this.individuals) {
            csvBuilder.append(i.getFirstName()).append("\t").append(i.getLastName())
                    .append("\t").append(i.getEmail()).append("\t").append(i.getTitle())
                    .append("\t").append(this.department.getInstitution().toString())
                    .append("\t").append(this.department.toString()).append("\t")
                    .append(i.getUnprocessed()).append("\n");
        }
        FileUtils.write(file, csvBuilder, "UTF-8");
    }
}