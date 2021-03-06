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

import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.norvelle.addressdiscoverer.AddressDiscoverer;
import org.norvelle.addressdiscoverer.model.Institution;
import org.norvelle.utils.Utils;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class EditInstitutionDialog extends javax.swing.JDialog {

    private Institution institution;
    private final InstitutionListPanel parent;
    private final boolean isNew;
    
    /**
     * Creates new form EditIndividualDialog
     * @param parent
     * @param institution
     */
    public EditInstitutionDialog(InstitutionListPanel parent, Institution institution) 
    {
        super((JFrame) null, true);
        this.institution = institution;
        this.parent = parent;
        this.isNew = false;
        initComponents();
        this.jNameField.setText(this.institution.getName());
        this.jCountryField.setText(this.institution.getCountry());
        this.jCityField.setText(this.institution.getCity());
        this.jAffiliationField.setText(this.institution.getAffiliation());
    }

    /**
     * Creates new form EditIndividualDialog, and create a new Institution when
     * the user closes the dialog with the OK button.
     * 
     * @param parent
     */
    public EditInstitutionDialog(InstitutionListPanel parent) 
    {
        super((JFrame) null, true);
        this.parent = parent;
        this.isNew = true;
        initComponents();
        this.jDeleteIndividualButton.setVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jCountryField = new javax.swing.JTextField();
        jCityField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jNameField = new javax.swing.JTextField();
        jCancelButton = new javax.swing.JButton();
        jOkButton = new javax.swing.JButton();
        jDeleteIndividualButton = new javax.swing.JButton();
        jAffiliationField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit Institution");

        jLabel1.setText("Country:");

        jLabel2.setText("City:");

        jLabel4.setText("Name:");

        jNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jNameFieldActionPerformed(evt);
            }
        });

        jCancelButton.setText("Cancel");
        jCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelButtonActionPerformed(evt);
            }
        });

        jOkButton.setText("Ok");
        jOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOkButtonActionPerformed(evt);
            }
        });

        jDeleteIndividualButton.setText("Delete");
        jDeleteIndividualButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDeleteIndividualButtonActionPerformed(evt);
            }
        });

        jLabel6.setText("Affiliation:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jDeleteIndividualButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 264, Short.MAX_VALUE)
                        .addComponent(jOkButton, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jCancelButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jAffiliationField)
                            .addComponent(jNameField)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jCityField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCountryField))
                                .addGap(97, 97, 97)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jCountryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jCityField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jAffiliationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCancelButton)
                    .addComponent(jOkButton)
                    .addComponent(jDeleteIndividualButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jNameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jNameFieldActionPerformed

    private void jOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jOkButtonActionPerformed
        try {
            // If we were called without a pre-existing institution, we'll hve
            // to create one before setting its attributes.
            if (this.isNew) {
                if (this.jNameField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                        Utils.wordWrapString("Institution name cannot be empty", 60), 
                        "Name cannot be empty", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                this.institution = Institution.create(this.jNameField.getText());
            }
            
            // Otherwise, we can just update the existing institution
            else if (!this.jNameField.getText().equals(this.institution.getName()))
                this.institution.setName(this.jNameField.getText());
            
            // In either case, we can now update the rest of the fields.
            if (!this.jCountryField.getText().equals(this.institution.getCountry()))
                this.institution.setCountry(this.jCountryField.getText());
            if (!this.jCityField.getText().equals(this.institution.getCity()))
                this.institution.setCity(this.jCityField.getText());
            if (!this.jAffiliationField.getText().equals(this.institution.getAffiliation()))
                this.institution.setAffiliation(this.jAffiliationField.getText());
            Institution.update(institution);
        } catch (SQLException ex) {
            AddressDiscoverer.reportException(ex);
            return;
        }
        this.parent.refreshList();
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jOkButtonActionPerformed

    private void jCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCancelButtonActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jCancelButtonActionPerformed

    private void jDeleteIndividualButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDeleteIndividualButtonActionPerformed
        int reply = JOptionPane.showConfirmDialog(null, "Are you sure?", "Confirm delete", 
                JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.NO_OPTION) 
            return;
        try {
            Institution.delete(this.institution);
        } catch (SQLException ex) {
            AddressDiscoverer.reportException(ex);
        }
        this.parent.notifyInstitutionDeleted();
        this.setVisible(false);
        this.dispose();        
    }//GEN-LAST:event_jDeleteIndividualButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField jAffiliationField;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JTextField jCityField;
    private javax.swing.JTextField jCountryField;
    private javax.swing.JButton jDeleteIndividualButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTextField jNameField;
    private javax.swing.JButton jOkButton;
    // End of variables declaration//GEN-END:variables
}
