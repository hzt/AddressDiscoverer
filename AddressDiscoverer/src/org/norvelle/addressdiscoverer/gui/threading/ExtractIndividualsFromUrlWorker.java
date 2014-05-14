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
package org.norvelle.addressdiscoverer.gui.threading;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.norvelle.addressdiscoverer.gui.EmailDiscoveryPanel;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.parse.structured.StructuredPageContactLinkLocator;

/**
 * A SwingWorker to handle setting genders for all Individuals in the background,
 * allowing the progress bar to be painted while the operation is ongoing.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ExtractIndividualsFromUrlWorker extends  ExtractIndividualsFromFileWorker 
{

    /**
     * Run the classification process on the contents of a remote web page.
     * 
     * @param parent
     * @param uri
     * @param department
     * @throws java.net.MalformedURLException
     * @throws java.net.URISyntaxException
     */
    public ExtractIndividualsFromUrlWorker(EmailDiscoveryPanel parent, String uri, 
            Department department, boolean useSequentialParser) 
            throws MalformedURLException, URISyntaxException, IOException 
    {
        super(parent, null, department, useSequentialParser);
        StructuredPageContactLinkLocator.baseUrl = uri;
        parent.getjStageNameLabel().setText("Reading remote web page");
        URL u = new URL(uri); // this would check for the protocol
        u.toURI();
        URLConnection con = u.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; "
                + "en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        InputStream in = con.getInputStream();
        String encoding = con.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        String body = IOUtils.toString(in, encoding);
        File tempDir = FileUtils.getTempDirectory();
        File tempFile = new File(tempDir.getAbsolutePath() + File.separator + "classifier.html.tmp");
        FileUtils.write(tempFile, body, encoding);
        this.fileToClassify = tempFile;
        parent.getjBytesReceivedLabel().setText(String.valueOf(body.length()));
    }
    
}
