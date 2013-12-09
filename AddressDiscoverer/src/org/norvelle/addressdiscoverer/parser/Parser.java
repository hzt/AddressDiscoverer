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
package org.norvelle.addressdiscoverer.parser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.Individual;

/**
 * Base class for all parsers producing Individuals from text chunks.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public abstract class Parser {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    // Our list of parsers to be tried, in the order they should be applied
    private static List<Parser> parsers = new ArrayList<>();
    
    /**
     * A regex string for finding emails
     */
    public static final String emailRegex = "(\\w+\\.)*\\w+[@](\\w+\\.)+(\\w+)";
    
    /**
     * A constructor that should never be called
     */
    public Parser() { }
    
    public abstract Individual getIndividual(Element row) 
            throws CantParseIndividualException, SQLException, OrmObjectNotConfiguredException;
    
    // ===================== Static Methods =============================

    private static void initializeParsers() {
        Parser.parsers.add(new NameEmailPositionParser());
        Parser.parsers.add(new EmailInAttributeParser());
    }
    
    /**
     * Run all available parsers on the given chunk of text and choose the
     * resulting Individual with the highest completeness score.
     * 
     * @param row
     * @return Individual with most complete profile
     * @throws org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException
     * @throws java.sql.SQLException
     * @throws org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException
     */
    public static Individual getBestIndividual(Element row) 
            throws CantParseIndividualException, SQLException, OrmObjectNotConfiguredException
    {
        if (Parser.parsers.isEmpty())
            Parser.initializeParsers();
        
        double topScore = 0.0; 
        Individual bestIndividual = null;
        for (Parser p : Parser.parsers) {
            logger.log(Level.INFO, String.format("Trying parser %s on text: '%s'",
                p.getClass().getSimpleName(), row.toString()));
            Individual currIndividual = p.getIndividual(row);
            if (currIndividual == null) continue;
            double currScore = currIndividual.getScore();
            if (currScore > topScore) {
                topScore = currScore;
                bestIndividual = currIndividual;
            }
        }
        return bestIndividual;
    }
    
}
