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
import org.norvelle.addressdiscoverer.gui.IProgressConsumer;
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
    private List<Individual> individuals;
    private final Department department;
    private final IProgressConsumer progressConsumer;
    
    public IndividualExtractor(Department department, IProgressConsumer progressConsumer) {
        this.individuals = new ArrayList<>();
        this.department = department;
        this.progressConsumer = progressConsumer;
    }
    
    /**
     * Given some HTML, attempt to scrape a list of Individuals from the
     * tables found in the HTML
     * 
     * @param html The HTML for the page that is to be scraped.
     * @return List<Individual> The list of Individuals found, if any
     */
    public List<Individual> parse(String html) {
        logger.log(Level.INFO, "Entering IndividualExtractor.parse()");
        List<Individual> myIndividuals = new ArrayList<>();
        if (html.isEmpty())
            return myIndividuals;
        
        // We use JSoup to do our parsing
        Document soup = Jsoup.parse(html);
        EmailElementFinder finder = new EmailElementFinder(soup);
        List<Element> tableRows = finder.getRows();
        if (this.progressConsumer != null)
            this.progressConsumer.setTotalElementsToProcess(tableRows.size());
        int rowCount = 0;
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
            logger.log(Level.INFO, "Adding new Individual: {0}", in.toString());
            in.setOriginalText(row.text());
            myIndividuals.add(in);
            rowCount ++;
            if (this.progressConsumer != null)
                this.progressConsumer.publishProgress(rowCount);
        }
        
        this.individuals = myIndividuals;
        logger.log(Level.INFO, "Exiting IndividualExtractor.parse()");
        return myIndividuals;
    }
    
    public List<Individual> getIndividuals() {
        return this.individuals;
    }
    
    
}
