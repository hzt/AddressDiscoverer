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
package org.norvelle.addressdiscoverer.parse.parser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.exceptions.CantExtractMultipleIndividualsException;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.MultipleRecordsInTrException;
import org.norvelle.addressdiscoverer.gui.threading.StatusReporter;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.UnparsableIndividual;

/**
 * Base class for all parsers producing Individuals from text chunks.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public abstract class Parser {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    // Our list of parsers to be tried, in the order they should be applied
    private static final List<Parser> singleRecordPerTrParsers = new ArrayList<>();
    private static final List<IMultipleRecordsPerTrParser> multipleRecordsPerTrParsers 
            = new ArrayList<>();
    
    /**
     * A constructor that should never be called
     */
    public Parser() { }
    
    public abstract Individual getIndividual(Element row, Department department) 
            throws CantParseIndividualException, SQLException, 
            MultipleRecordsInTrException;
    
    // ===================== Static Methods =============================

    private static void initializeParsers() {
        // Parsers for handling a single record per TR. The first of these
        // throws an exception if there are multiple records, which causes
        // the parser to swtich to multiple record per TR parsers.
        Parser.singleRecordPerTrParsers.add(new MultipleRecordsInOneTrParser()); 
        Parser.singleRecordPerTrParsers.add(new EachPartInTdParser());
        Parser.singleRecordPerTrParsers.add(new NameEmailPositionParser());
        Parser.singleRecordPerTrParsers.add(new EmailInAttributeParser());
        
        // Parsers for handling different types of multple record arrangements
        Parser.multipleRecordsPerTrParsers.add(new MultipleRecordsInOneTrParser());
    }
    
    /**
     * Run all available parsers on the given chunk of text and choose the
     * resulting Individual with the highest completeness score.
     * 
     * @param row
     * @param department
     * @return Individual with most complete profile
     * @throws org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException
     * @throws java.sql.SQLException
     * @throws org.norvelle.addressdiscoverer.exceptions.MultipleRecordsInTrException
     */
    @SuppressWarnings("UnnecessaryContinue")
    public static Individual getBestIndividual(Element row, Department department) 
            throws SQLException,  CantParseIndividualException, 
            MultipleRecordsInTrException
    {
        if (Parser.singleRecordPerTrParsers.isEmpty())
            Parser.initializeParsers();
        
        double topScore = 0.0; 
        Individual individual = null;
        for (Parser currParser : Parser.singleRecordPerTrParsers) {
            //logger.log(Level.INFO, String.format("Trying parser %s on text: '%s'",
            //    p.getClass().getSimpleName(), row.toString()));
            try {
                individual = currParser.getIndividual(row, department);
                break;
            }
            catch (CantParseIndividualException ex) {
                continue;
            }
        }
        
        // If none of our parsers worked, declare failure
        if (individual == null)
            throw new CantParseIndividualException(row.text());
        
        return individual;
    }

    /**
     * Called when we know that a single TR will contain multiple TDs,
     * each with an individual's data in them.
     * 
     * @param row
     * @param department
     * @param status A StatusReporter that lets us communicate progress to the GUI
     * @return List<Individual> The Individuals we've been able to extract.
     * @throws SQLException
     * @throws CantExtractMultipleIndividualsException 
     */
    public static List<Individual> getMultipleIndividualsFromRow(Element row, 
                Department department, StatusReporter status) 
            throws SQLException,  
                CantExtractMultipleIndividualsException
    {
        double topScore = 0.0; 
        status.setStage(StatusReporter.ParsingStages.EXTRACTING_INDIVIDUALS);
        List<Individual> bestIndividuals = null;
        for (IMultipleRecordsPerTrParser p : Parser.multipleRecordsPerTrParsers) {
            status.incrementNumericProgress();
            //logger.log(Level.INFO, String.format("Trying parser %s on text: '%s'",
            //    p.getClass().getSimpleName(), row.toString()));
            List<Individual> currIndividuals;
            currIndividuals = p.getMultipleIndividuals(row, department, status);
            if (currIndividuals == null) 
                continue;
            
            // If we got some Individuals back, calculate an overall goodness
            // score for them, so we can compare the results of different
            // parsers.
            double currScore = 0.0;
            for (Individual i : currIndividuals)
                if (!i.getClass().equals(UnparsableIndividual.class))
                    currScore += i.getScore();
            if (currScore > topScore) {
                topScore = currScore;
                bestIndividuals = currIndividuals;
            }
        }
        
        // If none of our parsers worked, declare failure
        if (bestIndividuals == null)
            throw new CantExtractMultipleIndividualsException(row.text());
        
        return bestIndividuals;
    }
}
