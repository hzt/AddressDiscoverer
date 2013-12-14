/*
 * Provides a SwingWorker to run ResearchAssistant and track its progress.
 */

package org.norvelle.addressdiscoverer.gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.norvelle.addressdiscoverer.AddressDiscoverer;
import org.norvelle.addressdiscoverer.model.Department;

/**
 * Provides a SwingWorker to run ResearchAssistant and track its progress.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ParseRemoteWebsiteWorker extends AbstractExtractIndividualWorker {

    protected final String uri;
    
    /**
     * Creates an instance of the worker... nothing happens until start() is called.
     *
     * @param department
     * @param uri
     * @param panel
     */
    public ParseRemoteWebsiteWorker(final Department department, 
            final String uri, final EmailDiscoveryPanel panel) 
    {
        super(department, panel);
        this.uri = uri;
    }

    /**
     * We perform all the magic of running the external process here in a separate
     * thread. We start the process and collect its output until there's nothing 
     * left to collect. We then give back the process exit code to the Runner that
     * called us.
     * 
     * @return Always returns 0
     * @throws Exception Only in the case of a NullPointerException or other unexpected error
     */
    @Override
    protected Integer doInBackground() throws Exception {
        try {
            URL url = new URL(this.uri);
            URLConnection connection = url.openConnection();
            InputStream in = connection.getInputStream();
            in.mark(500000);
            String charset = getCharsetFromStream(in);
            in.reset();
            StringWriter writer = new StringWriter();
            IOUtils.copy(in, writer, Charset.forName(charset));
            String html = writer.toString();
            updateDepartmentHTML(html);
            this.panel.setHTMLPanelContents(html);
            extractIndividuals(html, charset);
        } catch (IOException ex) {
            AddressDiscoverer.reportException(ex);
        }
        return 0;
    }
}
