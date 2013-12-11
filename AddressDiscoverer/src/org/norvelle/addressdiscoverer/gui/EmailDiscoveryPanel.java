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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import org.xml.sax.SAXException;
import org.lobobrowser.html.*;
import org.lobobrowser.html.parser.*;
import org.lobobrowser.html.test.*;
import org.norvelle.addressdiscoverer.AddressDiscoverer;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Individual;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class EmailDiscoveryPanel extends javax.swing.JPanel {

    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    private final GUIManagementPane parent;
    private Department currentDepartment;
    private final UserAgentContext ucontext;
    private final HtmlRendererContext rendererContext;
    private final DocumentBuilderImpl dbi;
    private AbstractExtractIndividualWorker worker;
    
    /**
     * Creates new form EmailDiscoveryPanel
     * 
     * @param parent The GUIManagementPane that acts as our controller
     */
    public EmailDiscoveryPanel(GUIManagementPane parent) {
        this.parent = parent;
        initComponents();
        this.jWebAddressField.getDocument().addDocumentListener(
                new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent de) {
                updateDepartmentWebAddress();
                jRetrieveHTMLButton.setEnabled(true);
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                updateDepartmentWebAddress();
                jRetrieveHTMLButton.setEnabled(true);
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                updateDepartmentWebAddress();
                jRetrieveHTMLButton.setEnabled(true);
            }
        });

        // Set up the HTMLPanel's requirements
        this.ucontext = new SimpleUserAgentContext();
        this.rendererContext = 
            new SimpleHtmlRendererContext(this.jHTMLPanel, this.ucontext); 
        this.dbi = new DocumentBuilderImpl(ucontext, this.rendererContext);
    }
    
    /**
     * If the user selects a department in the GUI, this pane should be activated
     * so that he user can adjust its properties and retrieve HTML for its personnel.
     * 
     * @param department The Department object selected by the user.
     * @throws IOException
     * @throws SAXException 
     */
    public void setDepartment(Department department) throws IOException, SAXException {
        this.currentDepartment = department;
        if (department == null) {
            this.jWebAddressField.setText("");
            this.jWebAddressField.setEnabled(false);
            this.jRetrieveHTMLButton.setEnabled(false);
            this.jOpenFileButton.setEnabled(false);
            this.jHTMLPanel.setEnabled(false);
            this.setHTMLPanelContents("");
            this.jBytesReceivedLabel.setEnabled(false);
        }
        else {
            String url = department.getWebAddress();
            this.jWebAddressField.setText(url);
            this.jWebAddressField.setEnabled(true);
            this.jBytesReceivedLabel.setEnabled(true);
            this.jRetrieveHTMLButton.setEnabled(true);
            this.jOpenFileButton.setEnabled(true);
            this.jHTMLPanel.setEnabled(true);
            String html = department.getHtml();
            if ((html != null && !html.isEmpty())) {
                this.setHTMLPanelContents(html);
            }
            else this.setHTMLPanelContents(""); 
            List<Individual> individuals;
            try {
                individuals = Individual.getIndividualsForDepartment(department);
                this.populateResultsTable(individuals);
            } catch (OrmObjectNotConfiguredException | SQLException ex) {
                AddressDiscoverer.reportException(ex);
            }
            
        }
    }
    
    /**
     * Asynchronously fetch the HTML for the specified webpage and update our
     * HTML rendering pane with that content.
     */
    private void webAddressChanged() {
        this.jRetrieveHTMLButton.setEnabled(false);
        final String myURI = this.jWebAddressField.getText();
        if (!myURI.isEmpty()) {
            final File file = new File(myURI);
            
            // If the user has specified a local file, we use that to fetch HTML
            if (file.exists()) {
                this.worker = new ParseLocalHtmlFileWorker(this.currentDepartment, 
                    file, this) ;
                this.worker.execute();
            } // if file.exists()
            
            // Otherwise we fetch the HTML from the website via HTTP
            else {
                this.worker = new ParseRemoteWebsiteWorker(this.currentDepartment, 
                    myURI, this) ;
                this.worker.execute();
            } // else
        } // if (!myURI
    }
    
    public void populateResultsTable(List<Individual> individuals) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Title");
        model.addColumn("First");
        model.addColumn("Last");
        model.addColumn("Email");
        model.addColumn("Other");
        model.setRowCount(individuals.size());
        int rowCount = 0;
        for (Individual i : individuals) {
            model.setValueAt(i.getTitle(), rowCount, 0);
            model.setValueAt(i.getFirstName(), rowCount, 1);
            model.setValueAt(i.getLastName(), rowCount, 2);
            model.setValueAt(i.getEmail(), rowCount, 3);
            model.setValueAt(i.getUnprocessed(), rowCount, 4);
            rowCount ++;
        }
        this.jAddressesFoundTable.setModel(model);
    }
    
    /**
     * Handles turning the HTML retrieved into a W3C Document that can be
     * displayed in the HTML panel.
     * 
     * @param html 
     */
    void setHTMLPanelContents(String html) {
        jRetrieveHTMLButton.setEnabled(true);
        if (html.isEmpty()) {
            this.jHTMLPanel.setEnabled(false);
            this.jHTMLPanel.clearDocument();
        }
        else {
            this.jBytesReceivedLabel.setText(Integer.toString(html.length()));
            this.jBytesReceivedLabel.setEnabled(true);
            /* Reader reader = new StringReader(html);
            InputSource is = new InputSourceImpl(reader, this.jWebAddressField.getText());
            try {
                Document document = this.dbi.parse(is);
                this.jHTMLPanel.setDocument(document, this.rendererContext);
                this.jHTMLPanel.setEnabled(true);
            } catch (SAXException | IOException ex) {
                CouldNotRenderHTMLException ex2 = 
                        new CouldNotRenderHTMLException("Could not render HTML: " + ex.getMessage());
                AddressDiscoverer.reportException(ex2);
            } */
        }
    }
    
    /**
     * Gets called when the user changes the value for the web address field.
     */
    private void updateDepartmentWebAddress() {
        this.jRetrieveHTMLButton.setEnabled(true);
        String newAddress = this.jWebAddressField.getText();
        if (newAddress != null && this.currentDepartment != null) {
            this.currentDepartment.setWebAddress(newAddress);
            try {
                Department.update(this.currentDepartment);
            } catch (SQLException ex) {
                AddressDiscoverer.reportException(ex);
            }        
        }
    }

    public JProgressBar getjParsingProgressBar() {
        return jParsingProgressBar;
    }

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jOpenFileChooser = new javax.swing.JFileChooser();
        jHTMLRenderPanel = new javax.swing.JTabbedPane();
        jEmailSourceTab = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jWebAddressField = new javax.swing.JTextField();
        jRetrieveHTMLButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jBytesReceivedLabel = new javax.swing.JLabel();
        jOpenFileButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jAddressesFoundTable = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jParsingProgressBar = new javax.swing.JProgressBar();
        jLabel4 = new javax.swing.JLabel();
        jPageContentTab = new javax.swing.JPanel();
        jPageContentPanel = new javax.swing.JPanel();
        jHTMLPanel = new org.lobobrowser.html.gui.HtmlPanel();

        jLabel1.setText("Web address:");

        jWebAddressField.setEnabled(false);
        jWebAddressField.setMaximumSize(new java.awt.Dimension(6, 22));

        jRetrieveHTMLButton.setText("Retrieve HTML");
        jRetrieveHTMLButton.setEnabled(false);
        jRetrieveHTMLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRetrieveHTMLButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Bytes received:");

        jBytesReceivedLabel.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jBytesReceivedLabel.setText("0");
        jBytesReceivedLabel.setEnabled(false);

        jOpenFileButton.setText("Open file");
        jOpenFileButton.setEnabled(false);
        jOpenFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOpenFileButtonActionPerformed(evt);
            }
        });

        jAddressesFoundTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "First Name", "Last Name", "Email", "Title"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jAddressesFoundTable);

        jLabel3.setText("Addresses found:");

        jLabel4.setText("Parsing progress:");

        javax.swing.GroupLayout jEmailSourceTabLayout = new javax.swing.GroupLayout(jEmailSourceTab);
        jEmailSourceTab.setLayout(jEmailSourceTabLayout);
        jEmailSourceTabLayout.setHorizontalGroup(
            jEmailSourceTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jEmailSourceTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jEmailSourceTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jEmailSourceTabLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jWebAddressField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOpenFileButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE)
                    .addGroup(jEmailSourceTabLayout.createSequentialGroup()
                        .addGroup(jEmailSourceTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jEmailSourceTabLayout.createSequentialGroup()
                                .addComponent(jRetrieveHTMLButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBytesReceivedLabel))
                            .addComponent(jLabel3))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jEmailSourceTabLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(26, 26, 26)
                        .addComponent(jParsingProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jEmailSourceTabLayout.setVerticalGroup(
            jEmailSourceTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jEmailSourceTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jEmailSourceTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jWebAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jOpenFileButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jEmailSourceTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRetrieveHTMLButton)
                    .addComponent(jLabel2)
                    .addComponent(jBytesReceivedLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jEmailSourceTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jParsingProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                .addContainerGap())
        );

        jHTMLRenderPanel.addTab("Source Page", jEmailSourceTab);

        jPageContentPanel.setLayout(new java.awt.BorderLayout());
        jPageContentPanel.add(jHTMLPanel, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout jPageContentTabLayout = new javax.swing.GroupLayout(jPageContentTab);
        jPageContentTab.setLayout(jPageContentTabLayout);
        jPageContentTabLayout.setHorizontalGroup(
            jPageContentTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPageContentTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPageContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPageContentTabLayout.setVerticalGroup(
            jPageContentTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPageContentTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPageContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jHTMLRenderPanel.addTab("Page Content", jPageContentTab);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jHTMLRenderPanel)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jHTMLRenderPanel)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jRetrieveHTMLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRetrieveHTMLButtonActionPerformed
        if (!this.jWebAddressField.getText().isEmpty())
            this.webAddressChanged();
    }//GEN-LAST:event_jRetrieveHTMLButtonActionPerformed

    private void jOpenFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jOpenFileButtonActionPerformed
        int returnVal = this.jOpenFileChooser.showOpenDialog(this.parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jOpenFileChooser.getSelectedFile();
            this.jWebAddressField.setText(file.getAbsolutePath());
            this.updateDepartmentWebAddress();
        }
        
    }//GEN-LAST:event_jOpenFileButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable jAddressesFoundTable;
    private javax.swing.JLabel jBytesReceivedLabel;
    private javax.swing.JPanel jEmailSourceTab;
    private org.lobobrowser.html.gui.HtmlPanel jHTMLPanel;
    private javax.swing.JTabbedPane jHTMLRenderPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JButton jOpenFileButton;
    private javax.swing.JFileChooser jOpenFileChooser;
    private javax.swing.JPanel jPageContentPanel;
    private javax.swing.JPanel jPageContentTab;
    private javax.swing.JProgressBar jParsingProgressBar;
    private javax.swing.JButton jRetrieveHTMLButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jWebAddressField;
    // End of variables declaration//GEN-END:variables
}
