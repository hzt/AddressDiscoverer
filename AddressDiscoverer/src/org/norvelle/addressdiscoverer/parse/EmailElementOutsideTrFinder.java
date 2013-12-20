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
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.norvelle.addressdiscoverer.Constants;
import org.norvelle.addressdiscoverer.exceptions.CannotCreateIndividualTrException;

/**
 * Parses a page to find elements that contain contact elements that are
 * not in TRs, and puts them into TRs so they can be extracted by a
 * standard parser.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class EmailElementOutsideTrFinder {

    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    
    private final Elements elementsWithEmailsAsContent;
    private final Elements elementsWithEmailAttributes;
    private final List<Element> testTrs = new ArrayList<>();
    private final Document soup;
    private final Pattern emailPattern = Pattern.compile("" + Constants.emailRegex + "(.*)");
    private final String encoding;
    
    /**
     * Given a JSoup Document, parse it looking for TRs with emails in their
     * content, either as a text node, or else as the value of an HREF attribute.
     * The rows found by this object are available via the getRows() method.
     * 
     * @param soup A JSoup Document that is the root of a web page.
     */
    public EmailElementOutsideTrFinder(Document soup, String encoding) {
        logger.log(Level.FINE, "Entering EmailElementOutsideTrFinder.new()");
        this.soup = soup;
        this.encoding = encoding;
        
        // First, we look for any email containing elements...  
        this.elementsWithEmailsAsContent = this.soup.select(
                String.format(":matches(%s)", Constants.emailRegex));
        this.elementsWithEmailAttributes = this.soup.select(
                String.format("[href~=(%s)]", Constants.emailRegex));

        // Figure out how many of these are under TR elements
        for (Element el : this.elementsWithEmailsAsContent) {
            Element trParent = this.translateToTr(el);
            if (trParent != null && !this.testTrs.contains(trParent))
                this.testTrs.add(trParent);
        }
        for (Element el : this.elementsWithEmailAttributes) {
            Element trParent = this.translateToTr(el);
            if (trParent != null && !this.testTrs.contains(trParent))
                this.testTrs.add(trParent);
        }
        
        logger.log(Level.FINE, "Exiting EmailElementOutsideTrFinder.new()");
    }
    
    /**
     * Given an element found in a Jsoup with an attribute containing an email,
     * move up the hierarchy to the nearest TR element and return that.
     * 
     * @param attrElement
     * @return A TR element that is the first such parent of the element with an email attribute
     */
    private Element translateToTr(Element attrElement) {
        if (attrElement.tagName().equals("tr"))
            return attrElement;
        Element currElement = attrElement.parent();
        while (currElement != null) {
            if (currElement.tagName().equals("tr"))
                return currElement;
            currElement = currElement.parent();
        }
        return null;        
    }

    /**
     * Provide a measure of how many TRs there are relative to the number of
     * email addresses in this document. A low number here indicates that there
     * are way too many emails per TR for this to be a table-oriented document.
     * 
     * @return 
     */
    public double getEmailToTrProportion() {
        return this.testTrs.size() / (this.elementsWithEmailAttributes.size() 
                + this.elementsWithEmailsAsContent.size());
    }

    /**
     * Find all the information we can about "individuals" and package it into
     * TR elements that we can send off to the parsers.
     * 
     * @return List<Element> A list of JSoup TR elements with TDs for the individual's info
     * @throws java.sql.SQLException 
     * @throws java.io.UnsupportedEncodingException 
     */
    public List<Element> getRows() 
            throws SQLException, UnsupportedEncodingException 
    {
        List<Element> rows = this.collectIndividualsGoingBackwards();
        return rows;
    }

    /**
     * Read through the flattened document tree from back to front, collecting
     * records for probable individuals as we go.
     * 
     * @return List<Element> A list of JSoup TR elements with TDs for the individual's info
     * @throws SQLException
     */
    private List<Element> collectIndividualsGoingBackwards() 
            throws SQLException, UnsupportedEncodingException 
    {
        String currEmail = "";
        IndividualCollector currIndividual = null;
        List<Element> trs = new ArrayList<>();
        BackwardsFlattenedDocumentIterator terminalElements = 
                new BackwardsFlattenedDocumentIterator(this.soup, this.encoding);
        for (String nodeData : terminalElements) {
            if (this.emailPattern.matcher(nodeData).matches()) {
                String email = nodeData;
                
                // If we have a new email, we have to close off the current individual collector,
                // creating a new one that starts with the email just found.
                if (!email.equals(currEmail)) {
                    currEmail = email;
                    if (currIndividual != null) 
                        try {
                            trs.add(currIndividual.createIndividualTr());
                        } catch (CannotCreateIndividualTrException ex) {
                            // Do nothing... we essentially discard the invalid information,
                            // don't add a TR.
                        }
                    currIndividual = new IndividualCollector(currEmail);
                }
            }
            else if (currIndividual != null)
                currIndividual.addLine(nodeData);
        }
        if (currIndividual != null)
            try {
                trs.add(currIndividual.createIndividualTr());
            } catch (CannotCreateIndividualTrException ex) {
                // Ignore invalid data, don't add any TR
            }
        
        return trs;
    }
}
