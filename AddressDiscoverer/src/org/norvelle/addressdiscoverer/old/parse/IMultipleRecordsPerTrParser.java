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
package org.norvelle.addressdiscoverer.old.parse;

import java.sql.SQLException;
import java.util.List;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.gui.threading.StatusReporter;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public interface IMultipleRecordsPerTrParser {

    List<Individual> getMultipleIndividuals(Element row, Department department, 
            StatusReporter status) 
            throws SQLException;
    
}
