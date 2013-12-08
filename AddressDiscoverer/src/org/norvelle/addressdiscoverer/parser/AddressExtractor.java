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

import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.norvelle.addressdiscoverer.gui.AddressListChangeListener;
import org.norvelle.addressdiscoverer.model.Individual;

/**
 * Given some HTML, searches for a list (or lists) of Individuals (i.e. faculty members)
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class AddressExtractor {
    
    private String html;
    private AddressListChangeListener changeListener;
    private List<Individual> individuals;
    
    public AddressExtractor() {
        this.individuals = new ArrayList<>();
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
        this.changeListener.notifyAddressListChanged();
    }
    
    /**
     * Given some HTML, attempt to scrape a list of Individuals from the
     * tables found in the HTML
     * 
     * @return List<Individual> The list of Individuals found, if any
     */
    private List<Individual> parse() {
        List<Individual> myIndividuals = new ArrayList<>();
        if (this.html.isEmpty())
            return myIndividuals;
        
        // We use JSoup to do our parsing
        Document soup = Jsoup.parse(html);
        EmailElementFinder finder = new EmailElementFinder(soup);
        List<Element> tableRows = finder.getRows();
        for (Element row : tableRows) {
            Individual in = Parser.getBestIndividual(row.text());
            myIndividuals.add(in);
        }
        
        return myIndividuals;
    }
    
    public List<Individual> getIndividuals() {
        return this.individuals;
    }
    
    public void registerChangeListener(AddressListChangeListener l) {
        this.changeListener = l;
    }
    
}
