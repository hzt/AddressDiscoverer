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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.norvelle.addressdiscoverer.model.Individual;
import org.norvelle.addressdiscoverer.parse.parser.Parser;

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
    private final Pattern emailPattern = Pattern.compile(Parser.emailRegex);
    
    /**
     * Given a JSoup Document, parse it looking for TRs with emails in their
     * content, either as a text node, or else as the value of an HREF attribute.
     * The rows found by this object are available via the getRows() method.
     * 
     * @param soup A JSoup Document that is the root of a web page.
     */
    public EmailElementOutsideTrFinder(Document soup) {
        logger.log(Level.FINE, "Entering EmailElementOutsideTrFinder.new()");
        this.soup = soup;
        
        // First, we look for any email containing elements...  
        this.elementsWithEmailsAsContent = this.soup.select(
                String.format(":matches(%s)", Parser.emailRegex));
        this.elementsWithEmailAttributes = this.soup.select(
                String.format("[href~=(%s)]", Parser.emailRegex));

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
     * @return 
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
     * @return 
     */
    public List<Element> getRows() {
        List<Element> rows = this.collectIndividualsGoingBackwards();
        return rows;
    }

    private List<Element> collectIndividualsGoingBackwards() {
        String currEmail = "";
        String currLastName = "";
        Individual currIndividual;
        BackwardsFlattenedDocumentIterator terminalElements = new BackwardsFlattenedDocumentIterator(this.soup);
        for (String nodeData : terminalElements) {
            if (this.emailPattern.matcher(nodeData).matches()) {
                String email = nodeData;
                
                // If we have a new email, we have to close off the current individual collector,
                // creating a new one that starts with the email just found.
                if (!email.equals(currEmail)) {
                    currEmail = email;
                }
            }
        }
    }

    class IndividualCollector {
        
        private final String email;
        private String name = "";
        private List<String> unprocessed = new ArrayList<>();
        
        public IndividualCollector(String email) {
            this.email = email;
        }
        
        public void addLine(String line) {
            if (this.name.isEmpty() && this.isLastName(line)) 
                this.name = line;
            else if (this.name.isEmpty())
                this.unprocessed.add(line);
        }
        
        /**
         * Create an TR describing an individual from the information we've collected so far. If
         * we detect a line with a last name in it, we set that as the name field.
         * If we haven't found any such line yet, we use the last unrecognized line
         * as containing the name. Anything between the email and the name line
         * is stuck into the unprocessed TD node.
         * 
         * @return An Element representing a TR with TDs for each field found.
         */
        public Element createIndividualTr() {
            Document trDocument = new Document("");
            Element tr = trDocument.createElement("tr");
            
            // First create and append a TD for our name, as best as we can guess it.
            Element nameTd = trDocument.createElement("td");
            if (this.name.isEmpty()) {
                String nameGuess = this.unprocessed.get(this.unprocessed.size() - 1);
                nameTd.appendText(nameGuess);
            }
            else
                nameTd.appendText(this.name);
            tr.appendChild(nameTd);
            
            // Create a middle TD for the unprocessed text.
            String unprocessedText = StringUtils.join(this.unprocessed, " ");
            Element unprocessedTd = trDocument.createElement("td");
            unprocessedTd.appendText(unprocessedText);
            
            // Finally, append a TD for our email address
            Element emailTd = trDocument.createElement("td");
            emailTd.appendText(this.email);
            tr.appendChild(emailTd);
            
            // Finish creating our "document" and return it.
            trDocument.appendChild(tr);
            return trDocument;
        }

        private boolean isLastName(String line) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
}
