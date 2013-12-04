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
package org.norvelle.addressdiscoverer.gui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;
import javax.swing.JPanel;
import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.layout.BrowserCanvas;
import org.norvelle.addressdiscoverer.Utils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The HTMLPageRenderer allows the user to see the rendered HTML of the
 * part of the page that contains the professor names.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class HTMLPageRenderer extends JPanel {
    
    private final EmailDiscoveryPanel parent;
    
    public HTMLPageRenderer (EmailDiscoveryPanel parent) {
        this.parent = parent;
        this.setLayout(new BorderLayout());
    }
    
    public void setURLSource(String urlstring) throws IOException, SAXException {
        //Open the network connection 
        DocumentSource docSource = new DefaultDocumentSource(urlstring);

        //Parse the input document
        DOMSource parser = new DefaultDOMSource(docSource);
        Document doc = parser.parse(); //doc represents the obtained DOM   
        
        // Analyze the DOM in order to create the BrowserCanvas
        DOMAnalyzer da = new DOMAnalyzer(doc, docSource.getURL());
        da.attributesToStyles(); //convert the HTML presentation attributes to inline styles
        da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT); //use the standard style sheet
        da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT); //use the additional style sheet
        da.getStyleSheets(); //load the author style sheets
        
        // Request the rendering of the HTML
        BrowserCanvas browser = 
            new BrowserCanvas(da.getRoot(), da, this.getSize(), new URL(Utils.basename(urlstring)));
        this.add(browser, BorderLayout.CENTER);
    }
    
}
