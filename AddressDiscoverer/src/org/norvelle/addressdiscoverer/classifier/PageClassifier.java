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

import java.io.UnsupportedEncodingException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class PageClassifier {

    public enum Classification {
        UNSTRUCTURED_PAGE, TR_STRUCTURED_PAGE, LI_STRUCTURED_PAGE, UNDETERMINED;
    }
    
    private final Document soup;
    private final String encoding;
    private final IProgressConsumer progressConsumer;
    
    public PageClassifier(Document soup, String encoding, IProgressConsumer progressConsumer) {
        this.soup = soup;
        this.encoding = encoding;
        this.progressConsumer = progressConsumer;
    }
    
    public Classification getClassification() throws UnsupportedEncodingException {
        BackwardsFlattenedDocumentIterator nameElements = this.getNameElements();
        
        return Classification.UNDETERMINED;
    }
    
    /**
     * Walk the document tree backwards and create an iterator that contains all
     * of the Elements whose contents are flagged as names.
     * 
     * @return
     * @throws UnsupportedEncodingException 
     */
    public BackwardsFlattenedDocumentIterator getNameElements() 
            throws UnsupportedEncodingException 
    {
        BackwardsFlattenedDocumentIterator iterator = 
                new BackwardsFlattenedDocumentIterator(this.soup, this.encoding);
        return iterator;
    }
    
}
