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
     * @throws java.net.MalformedURLException
     * @throws java.net.URISyntaxException
     */
    public ExtractIndividualsFromUrlWorker(EmailDiscoveryPanel parent, String uri) 
            throws MalformedURLException, URISyntaxException, IOException 
    {
        super(parent, null);
        parent.addToOutput("Reading remote web page");
        URL u = new URL(uri); // this would check for the protocol
        u.toURI();
        URLConnection con = u.openConnection();
        InputStream in = con.getInputStream();
        String encoding = con.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        String body = IOUtils.toString(in, encoding);
        File tempDir = FileUtils.getTempDirectory();
        File tempFile = new File(tempDir.getAbsolutePath() + File.separator + "classifier.html.tmp");
        FileUtils.write(tempFile, body, encoding);
        this.fileToClassify = tempFile;
    }
    
}
