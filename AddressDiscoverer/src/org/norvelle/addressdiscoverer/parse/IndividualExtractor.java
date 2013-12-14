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
package org.norvelle.addressdiscoverer.parse;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.AddressDiscoverer;
import org.norvelle.addressdiscoverer.exceptions.CantExtractMultipleIndividualsException;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.MultipleRecordsInTrException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.gui.IProgressConsumer;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.UnparsableIndividual;
import org.norvelle.addressdiscoverer.parse.parser.Parser;

/**
 * Given some HTML, searches for a list (or lists) of Individuals (i.e. faculty
 * members)
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
     * Given some HTML, attempt to scrape a list of Individuals from the tables
     * found in the HTML
     *
     * @param html The HTML for the page that is to be scraped.
     * @param encoding
     * @return List<Individual> The list of Individuals found, if any
     * @throws java.sql.SQLException
     * @throws
     * org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException
     * @throws java.io.UnsupportedEncodingException
     */
    public List<Individual> parse(String html, String encoding) throws SQLException,
            OrmObjectNotConfiguredException,
            UnsupportedEncodingException {
        logger.log(Level.INFO, "Entering IndividualExtractor.parse()");
        List<Individual> myIndividuals = new ArrayList<>();
        if (html.isEmpty()) {
            return myIndividuals;
        }

        // See if we get more email-containing elements via the TR or P 
        // based element finders
        Document soup = Jsoup.parse(html);
        List<Element> tableRows;
        EmailElementInTrFinder trFinder = new EmailElementInTrFinder(soup);
        List<Element> trRows = trFinder.getRows();
        EmailElementOutsideTrFinder pFinder = new EmailElementOutsideTrFinder(soup, encoding);
        if (pFinder.getEmailToTrProportion() < 0.45) {
            logger.log(Level.FINE, String.format("EmailElementOutsideTrFinder found %d P tags", trRows.size()));
            tableRows = pFinder.getRows();
        } else {
            logger.log(Level.FINE, String.format("EmailElementInTrFinder found %d P tags", trRows.size()));
            tableRows = trRows;
        }

        // Now, send the rows found to the parsers and choose the best result found
        if (this.progressConsumer != null) {
            this.progressConsumer.setTotalElementsToProcess(tableRows.size());
        }
        int rowCount = 0;
        try {
            myIndividuals = this.getSingleIndividualsFromTrs(tableRows);
        } catch (MultipleRecordsInTrException ex) {
            try {
                myIndividuals = this.getMultipleIndividualsFromTrs(tableRows);
            } catch (CantExtractMultipleIndividualsException ex2) {
                myIndividuals = new ArrayList<>();
                logger.log(Level.SEVERE,
                        String.format("Could not generate individuals from %d table rows",
                                tableRows.size()));
            }
        }

        this.individuals = myIndividuals;
        logger.log(Level.INFO, "Exiting IndividualExtractor.parse()");
        return myIndividuals;
    }

    /**
     * If the page being parsed has single records per TR, this routine
     * encapsulates the logic for dealing with that case
     *
     * @param tableRows
     * @return List<Individual> A List of all the individuals found in the page
     */
    private List<Individual> getSingleIndividualsFromTrs(List<Element> tableRows)
            throws MultipleRecordsInTrException {
        List<Individual> myIndividuals = new ArrayList<Individual>();
        int rowCount = 0;
        for (Element row : tableRows) {
            Individual in;
            try {
                in = Parser.getBestIndividual(row, this.department);
            } catch (CantParseIndividualException ex) {
                in = new UnparsableIndividual(row.text() + ": " + ex.getMessage());
            } catch (SQLException | OrmObjectNotConfiguredException ex) {
                AddressDiscoverer.reportException(ex);
                continue;
            }
            logger.log(Level.INFO, "Adding new Individual: {0}", in.toString());
            in.setOriginalText(row.text());
            myIndividuals.add(in);
            rowCount++;
            if (this.progressConsumer != null) {
                this.progressConsumer.publishProgress(rowCount);
            }
        }
        return myIndividuals;
    }

    /**
     * If the page being parsed has multiple records per TR, this routine
     * encapsulates the logic for dealing with that case
     *
     * @param tableRows
     * @return List<Individual> A List of all the individuals found in the page
     */
    private List<Individual> getMultipleIndividualsFromTrs(List<Element> tableRows)
            throws CantExtractMultipleIndividualsException, SQLException,
            OrmObjectNotConfiguredException {
        List<Individual> myIndividuals = new ArrayList<>();
        int rowCount = 0;
        for (Element row : tableRows) {
            myIndividuals.addAll(
                    Parser.getMultipleIndividualsFromRow(row, this.department));
            logger.log(Level.INFO, String.format("Adding %d new Individuals", myIndividuals.size()));
            rowCount++;
            if (this.progressConsumer != null) {
                this.progressConsumer.publishProgress(rowCount);
            }
        }
        return myIndividuals;
    }

    public List<Individual> getIndividuals() {
        return this.individuals;
    }

}
