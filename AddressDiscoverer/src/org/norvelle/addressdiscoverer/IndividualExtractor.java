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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.gui.AddressListChangeListener;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.NullIndividual;
import org.norvelle.addressdiscoverer.parser.Parser;

/**
 * Given some HTML, searches for a list (or lists) of Individuals (i.e. faculty members)
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class IndividualExtractor {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    private String html;
    private AddressListChangeListener changeListener;
    private List<Individual> individuals;
    private final Department department;
    
    public IndividualExtractor(Department department) {
        this.individuals = new ArrayList<>();
        this.department = department;
    }
    
    /**
     * Lets the AddressParser be notified of a change in the HTML...
     * calls the parser to derive a new list of Individuals from the HTML given.
     * 
     * @param html 
     */
    public void setHtml(String html) {
        this.html = html;
        this.individuals = this.parse();
        if (this.changeListener != null)
            this.changeListener.notifyAddressListChanged();
    }
    
    /**
     * Given some HTML, attempt to scrape a list of Individuals from the
     * tables found in the HTML
     * 
     * @return List<Individual> The list of Individuals found, if any
     */
    private List<Individual> parse() {
        logger.log(Level.INFO, "Entering IndividualExtractor.parse()");
        List<Individual> myIndividuals = new ArrayList<>();
        if (this.html.isEmpty())
            return myIndividuals;
        
        // We use JSoup to do our parsing
        Document soup = Jsoup.parse(html);
        EmailElementFinder finder = new EmailElementFinder(soup);
        List<Element> tableRows = finder.getRows();
        for (Element row : tableRows) {
            Individual in;
            try {
                in = Parser.getBestIndividual(row, this.department);
            } catch (CantParseIndividualException ex) {
                in = new NullIndividual(row.text() + ": " + ex.getMessage());
            } catch (SQLException | OrmObjectNotConfiguredException ex) {
                AddressDiscoverer.reportException(ex);
                continue;
            }
            logger.log(Level.INFO, "Adding new Individual: " + in.toString());
            myIndividuals.add(in);
        }
        
        logger.log(Level.INFO, "Exiting IndividualExtractor.parse()");
        return myIndividuals;
    }
    
    public List<Individual> getIndividuals() {
        return this.individuals;
    }
    
    public void registerChangeListener(AddressListChangeListener l) {
        this.changeListener = l;
    }
    
}
