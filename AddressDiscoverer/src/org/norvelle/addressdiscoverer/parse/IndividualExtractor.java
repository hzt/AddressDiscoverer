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
import org.norvelle.addressdiscoverer.exceptions.IndividualExtractionFailedException;
import org.norvelle.addressdiscoverer.exceptions.MultipleRecordsInTrException;
import org.norvelle.addressdiscoverer.gui.threading.StatusReporter;
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

    private List<Individual> individuals;
    private final Department department;
    private final StatusReporter status;

    public IndividualExtractor(Department department, StatusReporter status) {
        this.individuals = new ArrayList<>();
        this.department = department;
        this.status = status;
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
     * @throws java.io.UnsupportedEncodingException
     * @throws org.norvelle.addressdiscoverer.exceptions.IndividualExtractionFailedException
     */
    public List<Individual> parse(String html, String encoding) throws SQLException,
            UnsupportedEncodingException,
            IndividualExtractionFailedException 
    {
        logger.log(Level.INFO, "Entering IndividualExtractor.parse()");
        List<Individual> myIndividuals = new ArrayList<>();
        if (html.isEmpty()) {
            return myIndividuals;
        }

        // See if we get more email-containing elements via the TR or P 
        // based element finders
        status.setStage(StatusReporter.ParsingStages.PARSING_HTML);
        Document soup = Jsoup.parse(html);
        List<Element> tableRows;
        status.setStage(StatusReporter.ParsingStages.FINDING_EMAILS);
        EmailElementViaLinksFinder trFinder = new EmailElementViaLinksFinder(soup);
        List<Element> trRows = trFinder.getRows();
        status.setStage(StatusReporter.ParsingStages.FINDING_EMAILS);
        EmailElementOutsideTrFinder pFinder = new EmailElementOutsideTrFinder(soup, encoding);
        List<Element> outsideTrRows = pFinder.getRows();
        
        // Now, we select the appropriate colleciton of TRs based on which method
        // gave us more.
        if (outsideTrRows.size() > trRows.size()) {
            logger.log(Level.FINE, String.format("EmailElementOutsideTrFinder found %d P tags", trRows.size()));
            tableRows = outsideTrRows;
        } else if (!trRows.isEmpty()) {
            logger.log(Level.FINE, String.format("EmailElementInTrFinder found %d P tags", trRows.size()));
            tableRows = trRows;
        }
        
        // If we are unable to find any email TRs, this probably means we have a
        // page that embeds its emails in secondary pages. Run a link finder 
        // designed to pick those up.
        else { 
            status.setStage(StatusReporter.ParsingStages.FINDING_EMAILS_IN_LINKS);
            EmailElementViaLinksFinder vlFinder = new EmailElementViaLinksFinder(soup);
            List<Element> viaLinksTrRows = vlFinder.getRows();
            
            // If we find some email TRs via this method, we go ahead and proceed
            // with those TRs and see what we get.
            if (!viaLinksTrRows.isEmpty()) {
                tableRows = viaLinksTrRows;
            }
            else throw new IndividualExtractionFailedException(
                "No emails were found for this page");
        }
        
        try {
            myIndividuals = this.getSingleIndividualsFromTrs(tableRows, status);
        } catch (MultipleRecordsInTrException ex) {
            try {
                myIndividuals = this.getMultipleIndividualsFromTrs(tableRows, status);
            } catch (CantExtractMultipleIndividualsException ex2) {
                throw new IndividualExtractionFailedException(ex2.getMessage());
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
    private List<Individual> getSingleIndividualsFromTrs(
            List<Element> tableRows, StatusReporter status)
            throws MultipleRecordsInTrException 
    {
        List<Individual> myIndividuals = new ArrayList<>();
        status.setStage(StatusReporter.ParsingStages.EXTRACTING_INDIVIDUALS);
        status.setTotalNumericSteps(tableRows.size());
        for (Element row : tableRows) {
            Individual in;
            try {
                status.incrementNumericProgress();
                in = Parser.getBestIndividual(row, this.department);
            } catch (CantParseIndividualException ex) {
                in = new UnparsableIndividual(row.text() + ": " + ex.getMessage());
            } catch (SQLException ex) {
                AddressDiscoverer.reportException(ex);
                continue;
            }
            logger.log(Level.INFO, "Adding new Individual: {0}", in.toString());
            in.setOriginalText(row.text());
            myIndividuals.add(in);
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
    private List<Individual> getMultipleIndividualsFromTrs(List<Element> tableRows,
            StatusReporter status)
            throws CantExtractMultipleIndividualsException, SQLException
    {
        List<Individual> myIndividuals = new ArrayList<>();
        
        // Calculate how many TDs we're going to have to look at
        int totalTds = 0;
        for (Element tr : tableRows) 
            totalTds += tr.select("td").size();
        status.setStage(StatusReporter.ParsingStages.EXTRACTING_INDIVIDUALS);
        status.setTotalNumericSteps(totalTds);
 
        // Go through each row and extract any individuals found
        for (Element row : tableRows) {
            myIndividuals.addAll(
                    Parser.getMultipleIndividualsFromRow(
                            row, this.department, status));
            logger.log(Level.INFO, String.format("Adding %d new Individuals", myIndividuals.size()));
        }
        return myIndividuals;
    }

    public List<Individual> getIndividuals() {
        return this.individuals;
    }

}
