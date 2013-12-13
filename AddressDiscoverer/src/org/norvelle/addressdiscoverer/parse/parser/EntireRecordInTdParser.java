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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException;
import org.norvelle.addressdiscoverer.exceptions.EmptyTdException;
import org.norvelle.addressdiscoverer.exceptions.MultipleRecordsInTrException;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.model.Name;
import org.norvelle.addressdiscoverer.model.UnparsableIndividual;
import org.norvelle.addressdiscoverer.parse.BasicNameChunkHandler;

/**
 * Detects when an entire Individual's record is contained within a single
 * TD, in a situation where there are multiple TDs per TR.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class EntireRecordInTdParser extends Parser implements IMultipleRecordsPerTrParser {
    
    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    private final Pattern findEmailPattern = 
            Pattern.compile(String.format("(%s)", Parser.emailRegex));;
    
    // We store our department when parsing multiple records
    Department department;
    
    public EntireRecordInTdParser() { }

    /**
     * Given a JSoup TR element, try to create an Individual object based on
     * the fragments of information we find.
     * 
     * @param row
     * @param department The Department the new Individual will belong to.
     * @return An Individual with appropriately filled in details
     * @throws org.norvelle.addressdiscoverer.exceptions.CantParseIndividualException
     * @throws java.sql.SQLException
     * @throws org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException
     * @throws org.norvelle.addressdiscoverer.exceptions.MultipleRecordsInTrException
     */
    @Override
    public Individual getIndividual(Element row, Department department) 
            throws CantParseIndividualException, SQLException, OrmObjectNotConfiguredException,
            MultipleRecordsInTrException
    {
        // First, see if we have more than one TD for our row. If so, then we
        // can potentially parse this with this parser, otherwise fail
        Element myRow = row.clone();
        Elements tds = myRow.select("td");
        if (tds.size() == 1)
            throw new CantParseIndividualException("EntireRecordInTdParser only works with multiple TDs");

        // Now, check to see if there are multiple email addresses in the text
        // content of this row. If so, we throw a MultipleRecordsInTrException
        // to tell Parser that we need to shift to a multiple records per row
        // type parser. Otherwise, we fail here and let the next single record
        // per row take over.
        Elements elementsWithEmails = myRow.select(
                String.format("td:matches(%s)", Parser.emailRegex));
        if (elementsWithEmails.size() > 1)
            throw new MultipleRecordsInTrException();
        else
            throw new CantParseIndividualException("EntireRecordInTdParser only works with multiple TDs");
    }
    
    /**
     * This method is only called if the presence of multiple records per row
     * has already been ascertained. Finds all the records existing in the
     * current row and returns them as Individuals
     * 
     * @param row
     * @param department
     * @return List<Individual> All the individuals we could extract from this row.
     * @throws CantParseIndividualException
     * @throws SQLException
     * @throws OrmObjectNotConfiguredException 
     */
    @Override
    public List<Individual> getMultipleIndividuals(Element row, Department department) 
            throws SQLException, OrmObjectNotConfiguredException
    {
        this.department = department;
        List<Individual> individuals = new ArrayList<>();
        Elements tds = row.select("td");
        
        // We assume that each TD holds a complete record... find how what the
        // format is and extract the fields appropriately
        for (Element td : tds) {
            try {
                Individual newIndividual = this.extractIndividualFromTd(td);
                individuals.add(newIndividual);
            } catch (EmptyTdException ex) {
                // We skip empty TDs and don't complain.
                continue;
            } catch (CantParseIndividualException ex) {
                // Record unparsable TDs that look like they ought to be parsable.
                individuals.add(new UnparsableIndividual(td.text()));
            }
        }
        
        return individuals;
    }
    
    /**
     * Try various ways to extract an Individual record from the TD... first
     * try splitting the info by P or BR tags, and if that fails, try to 
     * break the text up using other strategies.
     * 
     * @param td
     * @return
     * @throws CantParseIndividualException
     * @throws SQLException
     * @throws OrmObjectNotConfiguredException 
     */
    private Individual extractIndividualFromTd(Element td) 
            throws CantParseIndividualException, SQLException, 
            OrmObjectNotConfiguredException, EmptyTdException 
    {
        // First see if there are multiple P elements
        List<String> chunks = new ArrayList<>();
        Elements ps = td.select("p");
        if (ps.size() > 1) {
            for (Element p : ps)
                chunks.add(p.text().trim());
        }
        
        // If not, see if we can split the content by BR
        else {
            String tdHtml = td.html().replace("&nbsp;", " ").trim();
            tdHtml = tdHtml.replaceAll("(<br\\ ?\\/?>)", "\n");
            String[] lines = StringUtils.split(tdHtml, "\n");
            if (lines.length  > 1)
                for (String line : lines) {
                    line = line.replaceAll("<(.*?)>", "").trim();
                    line = line.replaceAll("&nbsp;", "").trim();
                    chunks.add(line);
                }
            
            // If there are no BRs, then we will process the text as one long string
            else if (lines.length == 1)
                chunks.add(td.text());
        }
        
        Individual i;
        if (chunks.size() == 1)
            i = this.createIndividualFromSingleChunk(chunks.get(0));
        else 
            i = this.createIndividualFromMultipleChunks(chunks);
        return i;
    }
    
    /**
     * If we have a number of different text chunks, we assume that the first 
     * one will have the name, and a later one will have the email. Everything
     * else goes into the Unprocessed part of the Individual.
     * 
     * @param chunks
     * @return
     * @throws CantParseIndividualException
     * @throws SQLException
     * @throws OrmObjectNotConfiguredException 
     */
    private Individual createIndividualFromMultipleChunks(List<String> chunks) 
            throws CantParseIndividualException, SQLException, 
            OrmObjectNotConfiguredException, EmptyTdException 
    {
        // First, throw out any empty chunks
        List<String> noEmptyChunks = new ArrayList<>();
        for (String chunk : chunks)
            if (chunk.matches(".*\\p{L}.*"))
                noEmptyChunks.add(chunk);
        
        // Make sure we have at least some valid text to work on.
        if (noEmptyChunks.isEmpty())
            throw new EmptyTdException("None of the TD parts have any text");
        
        // Now that we have a chunk of text with a name, see if we can't create a Name
        String nameChunk = this.stripInvalidChars(noEmptyChunks.get(0));
        BasicNameChunkHandler np = new BasicNameChunkHandler();
        Name name = np.processChunkForName(nameChunk);
        noEmptyChunks.remove(noEmptyChunks.get(0));
        
        // Next, find our email chunk, and fail if we can't find it.
        String email = "";
        List<String> unprocessedChunks = new ArrayList<>();
        for (String chunk : noEmptyChunks) {
            chunk = chunk.replaceAll("\\xA0", " ").trim();
            Matcher emailMatcher = this.findEmailPattern.matcher(chunk);
            if (emailMatcher.matches() && email.isEmpty()) 
                email = emailMatcher.group(1);
            else
                unprocessedChunks.add(chunk);
        }
        if (email.isEmpty())
            throw new CantParseIndividualException("The record has no email in it: ");
        
        // Stick the rest of the info into the Unprocessed part of the Individual record
        String rest = "";
        for (String chunk : unprocessedChunks) 
            rest += " " + chunk;
        rest = rest.replaceAll("\\d", "").trim();
        
        // Returnour creation
        Individual i = new Individual(name, email, "", rest, 
                this.getClass().getSimpleName(), this.department);
        return i;
                
    }

    /**
     * If we just have a single chunk, we turn it over to another specialized
     * parser to handle it.
     * 
     * @param chunk A text chunk with a name and an email to be extracted
     * @return The Individual created from the info in the chunk
     */
    private Individual createIndividualFromSingleChunk(String chunk) 
            throws CantParseIndividualException, SQLException, OrmObjectNotConfiguredException 
    {
        NameEmailPositionParser subParser = new NameEmailPositionParser();
        try {                
            return subParser.getIndividual(chunk, this.department);
        } catch (MultipleRecordsInTrException ex) {
            logger.log(Level.SEVERE, "We should never get a MultipleRecordsInTrException exception, fatal error");
            System.exit(1);
            return null;
        }
    }
    
    private String stripInvalidChars(String text) {
        text = text.replaceAll("'", "XXXX");
        text = text.replaceAll("\\.", "ZZZZ");
        text = text.replaceAll("(\\p{P}|\\p{S})", "");
        text = text.replace("XXXX", "ʼ");
        text = text.replace("ZZZZ", ".");
        return text;
    }
}
