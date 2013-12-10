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
package org.norvelle.addressdiscoverer.parser.chunk;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.Name;

/**
 * Base class for all parsers producing Individuals from text chunks.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public abstract class ChunkHandlers {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    // Our list of parsers to be tried, in the order they should be applied
    private static List<IChunkHandler> handlers = new ArrayList<>();
    
    /**
     * A regex string for finding emails
     */
    public static final String emailRegex = "(\\w+\\.)*\\w+[@](\\w+\\.)+(\\w+)";
    
    /**
     * A constructor that should never be called
     */
    public ChunkHandlers() { }
    
    public abstract Individual getIndividual(Element row) 
            throws CantParseIndividualException, SQLException, OrmObjectNotConfiguredException;
    
    // ===================== Static Methods =============================

    private static void initializeChunkHandlerss() {
        ChunkHandlers.handlers.add(new BasicNameChunkHandler());
        ChunkHandlers.handlers.add(new LastLastNameChunkHandler());
    }
    
    /**
     * Run all available parsers on the given chunk of text and choose the
     * resulting Individual with the highest completeness score.
     * 
     * @param row
     * @return Name with most complete profile
     * @throws java.sql.SQLException
     * @throws org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException
     */
    public static Name getBestName(Element row) 
            throws SQLException, OrmObjectNotConfiguredException
    {
        if (ChunkHandlers.handlers.isEmpty())
            ChunkHandlers.initializeChunkHandlerss();
        
        double topScore = 0.0; 
        Name bestName = null;
        for (IChunkHandler p : ChunkHandlers.handlers) {
            logger.log(Level.INFO, String.format("Trying chunk handler %s on text: '%s'",
                p.getClass().getSimpleName(), row.toString()));
            Name currName;
            try {
                currName = p.processChunkForName(row.text());
            }
            catch (CantParseIndividualException ex) {
                continue;
            }
            if (currName == null) 
                continue;
            double currScore = currName.getScore();
            if (currScore > topScore) {
                topScore = currScore;
                bestName = currName;
            }
        }
        return bestName;
    }
    
}
