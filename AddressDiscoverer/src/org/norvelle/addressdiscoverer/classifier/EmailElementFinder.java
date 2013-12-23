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

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.norvelle.addressdiscoverer.Constants;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class EmailElementFinder {
    
    public enum ContactInformationType {
        EMAILS_IN_CONTENT, EMAILS_IN_HREFS, LINKS_TO_DETAIL_PAGE, NO_CONTACT_INFO_FOUND;
    }
    
    private static final Pattern emailPattern = Pattern.compile(Constants.emailRegex);
    
    private int elementsWithEmails = 0;
    private int elementsWithWebLinks = 0;
    private ContactInformationType contactInformationType;
    private HashMap<String, HashMap<Element, ContactInformationType>> 
            linksAssociatedWithNamesByContainer;

    public EmailElementFinder(NameElementFinder nameFinder) {
        // Find out whether emails are hidden behind web links or not.
        this.linksAssociatedWithNamesByContainer = 
                this.findLinksAssociatedWithNames(nameFinder.getNameElementsByContainer());
        
        // Now, figure out what kinds of email information we have... is it
        // directly given in the page itself, or is it hidden behind weblinks?
        if (this.elementsWithEmails == 0 && this.elementsWithWebLinks == 0)
            this.contactInformationType = ContactInformationType.NO_CONTACT_INFO_FOUND;
        else if (Approximately.equals(this.elementsWithEmails, nameFinder.getNumberOfNames()))
            this.contactInformationType = ContactInformationType.EMAILS_IN_CONTENT;
        else if (Approximately.equals(this.elementsWithWebLinks, nameFinder.getNumberOfNames()))
            this.contactInformationType = ContactInformationType.LINKS_TO_DETAIL_PAGE;
    }
    
    /**
     * Find all contact info links (emails, hyperlinks) that are associated with
     * the names we have found, and link them to the container tags that the names
     * are also found in.
     * 
     * @param nameElementsByContainer
     * @return HashMap<String, HashMap<Element, String>> Map that links container 
     *      types to maps of elements to link types
     */
    private HashMap<String, HashMap<Element, ContactInformationType>> 
        findLinksAssociatedWithNames(
            HashMap<String, List<Element>> nameElementsByContainer) 
    {
        HashMap<String, HashMap<Element, ContactInformationType>> 
                linksToContainerTypes = new HashMap<>();
        for (String containerType : nameElementsByContainer.keySet()) {
            HashMap<Element, ContactInformationType> linksForContainerType = 
                    this.findLinksWithinContainerType(
                            nameElementsByContainer.get(containerType), containerType);
            linksToContainerTypes.put(containerType, linksForContainerType);
        }
        return linksToContainerTypes;
    }
    
    private HashMap<Element, ContactInformationType> findLinksWithinContainerType(
            List<Element> namesWithinContainer, String containerType) 
    {
        HashMap<Element, ContactInformationType> linksWithType = new HashMap<>();
        if (namesWithinContainer.isEmpty())
            return linksWithType;

        for (Element nameElement : namesWithinContainer) {
            ContactInformationType linkType = 
                    this.findLinkOrEmailTypeForName(nameElement, containerType);
            linksWithType.put(nameElement, linkType);
        }
        
        return linksWithType;
    }
    
    private ContactInformationType findLinkOrEmailTypeForName(
            Element nameElement, String containerType) 
    {
        // First, climb up to the containing element
        Element currElement = nameElement.parent();
        Element containerElement = null;
        while (currElement != null) {
            if (currElement.tagName().equals(containerType)) {
                containerElement = currElement;
                break;
            }
            currElement = currElement.parent();
        }
        if (containerElement == null) 
            throw new IllegalStateException(
                    String.format("Cannot have a name in a %s that doesn't have a %s parent",
                            containerType, containerType));
        
        // Now, find out whether the TR element has an email child
        // or a web link child
        Elements allChildren = containerElement.getAllElements();
        for (Element child : allChildren) {
            ContactInformationType contactInfoType = this.elementHasEmail(child);
            if (contactInfoType == ContactInformationType.EMAILS_IN_CONTENT || 
                    contactInfoType == ContactInformationType.EMAILS_IN_HREFS) {
                this.elementsWithEmails ++;
                return contactInfoType;
            }
            if (contactInfoType == ContactInformationType.LINKS_TO_DETAIL_PAGE) {
                this.elementsWithWebLinks ++;
                return contactInfoType;
            }
        }
        
        return ContactInformationType.NO_CONTACT_INFO_FOUND;
    }

    private ContactInformationType elementHasEmail(Element element) {
        if (element.hasAttr("href")) {
            Matcher matcher = emailPattern.matcher(element.attr("href"));
            if (matcher.lookingAt())
                return ContactInformationType.EMAILS_IN_HREFS;
        }
        String content = element.ownText();
        Matcher contentMatcher = emailPattern.matcher(content);
        if (contentMatcher.lookingAt())
            return ContactInformationType.EMAILS_IN_CONTENT;
        else return ContactInformationType.NO_CONTACT_INFO_FOUND;
    }


    public ContactInformationType getContactInformationType() {
        return this.contactInformationType;
    }
    
    public HashMap<Element, ContactInformationType> linksAssociatedWithName(String containerType) {
        return this.linksAssociatedWithNamesByContainer.get(containerType);
    }
}
