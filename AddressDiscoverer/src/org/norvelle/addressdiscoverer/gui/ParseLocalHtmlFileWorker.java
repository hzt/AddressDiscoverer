/*
 * Provides a SwingWorker to run ResearchAssistant and track its progress.
 */

package org.norvelle.addressdiscoverer.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.norvelle.addressdiscoverer.AddressDiscoverer;
import org.norvelle.addressdiscoverer.Constants;
import org.norvelle.addressdiscoverer.model.Department;

/**
 * Provides a SwingWorker to run ResearchAssistant and track its progress.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class ParseLocalHtmlFileWorker extends AbstractExtractIndividualWorker {

    protected final File localFile;
    
    /**
     * Creates an instance of the worker... nothing happens until start() is called.
     *
     * @param department
     * @param localFile
     * @param panel
     */
    public ParseLocalHtmlFileWorker(final Department department, 
            final File localFile, final EmailDiscoveryPanel panel) 
    {
        super(department, panel);
        this.localFile = localFile;
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
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    protected Integer doInBackground() throws Exception {
        try {
            StatusReporter status = new StatusReporter(StatusReporter.ParsingStages.READING_FILE, this);
            InputStream in = new FileInputStream(this.localFile);
            String charset = this.getCharsetFromStream(in);
            String html = FileUtils.readFileToString(this.localFile, Charset.forName(charset));
            this.updateDepartmentHTML(html);
            this.panel.setHTMLPanelContents(html); 
            this.extractIndividuals(html, charset, status);
        } catch (Exception ex) {
            AddressDiscoverer.reportException(ex);
            this.panel.notifyParsingFinished();
        } // try
        return 0;
    }
}
