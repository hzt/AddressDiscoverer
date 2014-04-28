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
package org.norvelle.addressdiscoverer.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.norvelle.addressdiscoverer.Constants;
import org.norvelle.addressdiscoverer.classifier.ClassificationStatusReporter.ClassificationStages;
import org.norvelle.addressdiscoverer.classifier.ContactLink.ContactType;
import org.norvelle.addressdiscoverer.exceptions.DoesNotContainContactLinkException;

/**
 * Handles finding elements with identifiable names in a given HTML page
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ContactLinkFinder {

    public enum PageContactType {
        HAS_ASSOCIATED_CONTACT_INFO, NO_ASSOCIATED_CONTACT_INFO;
    }
    
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
    private final List<NameElement> nameElements;
    private final HashMap<NameElement, ContactLink> nameToContactMap;
    private int associatedContactLinksFound = 0;
    private final PageContactType pageContactType;
    
    public ContactLinkFinder(NameElementFinder nameElementFinder, 
            Document soup, ClassificationStatusReporter status) 
    {
        this.nameElements = nameElementFinder.getNameElements();
        this.nameToContactMap = new HashMap<>();
        status.setStage(ClassificationStages.FINDING_CONTACT_LINKS);
        status.setTotalNumericSteps(nameElements.size());
        Approximately.defaultRange = nameElements.size();
        
        // First, figure out how many email addresses there are on the page
        List<Element> emailElements = this.findEmailContainingElements(soup);
        
        // For each name element we have, see if we can find an associated
        // contact link
        for (NameElement nm : this.nameElements) {
            ContactLink cl = this.findContactLinkForNameElement(nm);
            if (cl != null) {
                this.nameToContactMap.put(nm, cl);
                nm.setContactLink(cl);
                this.associatedContactLinksFound ++;
            }
            status.incrementNumericProgress();
        }
        
        // Figure out how to characterize the page. If more or less each name element
        // has an associated contact link, characterize it as an "associated" page
        if (Order.of(this.associatedContactLinksFound, emailElements.size()))
            this.pageContactType = PageContactType.HAS_ASSOCIATED_CONTACT_INFO;
        
        // If there are lots of emails and few contact links, this is a non-associatec page.
        else if (emailElements.size() / 4 > this.associatedContactLinksFound)
            this.pageContactType = PageContactType.NO_ASSOCIATED_CONTACT_INFO;
        
        // Otherwise, if we have lots of email links but few associations, it's a
        // non-associated page.
        else if (Approximately.equals(nameElements.size(), emailElements.size()))
            this.pageContactType = PageContactType.NO_ASSOCIATED_CONTACT_INFO;
        
        // If we have a low number of associated links, then we assume that this
        // is a page that just doesn't have contact info for most faculty, but
        // is nonetheless an "associated" page.
        else if (this.associatedContactLinksFound > nameElements.size() / 4)
            this.pageContactType = PageContactType.HAS_ASSOCIATED_CONTACT_INFO;
        
        // Otherwise, we assume it's non-associated.
        else
            this.pageContactType = PageContactType.NO_ASSOCIATED_CONTACT_INFO;
    }

    public PageContactType getPageContactType() {
        return pageContactType;
    }

    public int getNumContactLinksFound() {
        return associatedContactLinksFound;
    }
    
    /**
     * Given a NameElement, find the contact-containing element for it, and
     * return a ContactLink object containing all its information.
     * 
     * @param nm
     * @return 
     */
    public ContactLink findContactLinkForNameElement(NameElement nm) {
        Element realContainer = nm.getContainer();
        if (realContainer == null)
            return null;
        String content = realContainer.html();
        
        // Check all the container's children to see if we can find an 
        // Element with readable contact info.
        List<ContactLink> candidateLinks = new ArrayList<>();
        Elements allChildren = realContainer.getAllElements();
        for (Element child : allChildren) {
            try {
                ContactLink cl = new ContactLink(child);
                candidateLinks.add(cl);
            }
            catch (DoesNotContainContactLinkException ex) {
                //
            }
        }
        
        // If we have found various links, prioritize the email link
        ContactLink hrefLink = null;
        for (ContactLink cl : candidateLinks) {
            if (cl.getType() == ContactType.EMAIL_IN_CONTENT || 
                    cl.getType() == ContactType.EMAIL_IN_HREF)
                return cl;
            else hrefLink = cl;
        }
        
        return hrefLink;
    }

    private List<Element> findEmailContainingElements(Document soup) {
        List<Element> allEmailElements = new ArrayList<>();
        Elements elementsWithEmails = soup.select(
                String.format("tr:matches(%s)", Constants.emailRegex));
        for (Element element: elementsWithEmails) 
            allEmailElements.add(element);

        Elements elementsWithEmailAttributes = soup.select(
                String.format("[href~=(%s)]", Constants.emailRegex));
        for (Element attrElement: elementsWithEmailAttributes) {
            if (!allEmailElements.contains(attrElement))
                allEmailElements.add(attrElement);
        }
        
        return allEmailElements;
    }

}
