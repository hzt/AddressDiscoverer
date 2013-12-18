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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import org.norvelle.addressdiscoverer.AddressDiscoverer;
import org.norvelle.utils.Utils;
import org.norvelle.addressdiscoverer.exceptions.OrmObjectNotConfiguredException;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Institution;

/**
 *
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class DepartmentListPanel extends javax.swing.JPanel implements IListPanel {

    // A logger instance
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 

    private final GUIManagementPane parent;
    private Institution institution;
    private DefaultListModel listModel;

    /**
     * Creates new form InstitutionListPanel
     * @param parent
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public DepartmentListPanel(GUIManagementPane parent) {
        this.parent = parent;
        initComponents();
        this.jAddModifyDeleteButtonPanel.setParent(this);
        this.listModel = new DefaultListModel();
        this.jDepartmentList.setModel(listModel);
        this.jAddModifyDeleteButtonPanel.setDisabledCondition();

        // Add a mouse listener for double clicks on the table
        final DepartmentListPanel myThis = this;
        this.jDepartmentList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    int selectedDepartment = jDepartmentList.getSelectedIndex();
                    Department department = (Department) 
                        listModel.elementAt(selectedDepartment);                    
                    EditDepartmentDialog dialog = 
                            new EditDepartmentDialog(myThis, department);
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                }
            }
        });
        
    }
    
    public void setInstitution(Institution selectedInstitution) {
        try {
            this.institution = selectedInstitution;
            this.listModel.clear();
            if (selectedInstitution != null) {
                HashMap<Integer, Department> departments = Department.
                        getDepartmentsForInstitution(selectedInstitution);
                for (int key : departments.keySet()) 
                    this.listModel.addElement(departments.get(key));
            }
            this.jAddModifyDeleteButtonPanel.setNoObjectSelectedCondition();
        } catch (SQLException ex) {
            AddressDiscoverer.reportException(ex);
        }
        
    }
    
    @Override
    public void deleteSelected() {
        int reply = JOptionPane.showConfirmDialog(null, "Are you sure?", "Confirm delete", 
                JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.NO_OPTION) 
            return;
        int selectedDepartment = this.jDepartmentList.getSelectedIndex();
        Department departmentToDelete = (Department) 
                this.listModel.elementAt(selectedDepartment);
        try {
            Department.delete(departmentToDelete);
            this.jAddModifyDeleteButtonPanel.setNoObjectSelectedCondition();
            this.refreshList();
            this.parent.setSelectedDepartment(null);
        } catch (SQLException ex) {
            AddressDiscoverer.reportException(ex);
        }        
    }
    
    @Override
    public void addNew() {
        EditDepartmentDialog dialog = new EditDepartmentDialog(this, this.institution);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
    
    @Override
    public void modifySelected() {
        int selectedDepartment = this.jDepartmentList.getSelectedIndex();
        if (selectedDepartment != -1) {
            Department departmentToModify = (Department) 
                    this.listModel.elementAt(selectedDepartment);
            EditDepartmentDialog dialog = 
                    new EditDepartmentDialog(this, departmentToModify);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }
    }

    public void refreshList() {
        this.listModel = new DefaultListModel();
        HashMap<Integer, Department> departments;
        try {
            departments = Department.getDepartmentsForInstitution(this.institution);
            List<Department> sortedDepartments = 
                    Utils.asSortedList(departments.values(), Utils.ASCENDING_SORT);
            for (Department i : sortedDepartments)
                this.listModel.addElement(i);
            this.jDepartmentList.setModel(this.listModel);   
            if (this.jDepartmentList.getSelectedIndex() == -1) {
                this.jAddModifyDeleteButtonPanel.setNoObjectSelectedCondition();
                this.parent.setSelectedDepartment(null);
            }
        } catch (SQLException ex) {
            AddressDiscoverer.reportException(ex);
        } 
    }

    public void notifyDepartmentDeleted() {
        this.jAddModifyDeleteButtonPanel.setNoObjectSelectedCondition();
        this.refreshList();
        this.parent.setSelectedDepartment(null);
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
        jScrollPane2 = new javax.swing.JScrollPane();
        jDepartmentList = new javax.swing.JList();
        jAddModifyDeleteButtonPanel = new org.norvelle.addressdiscoverer.gui.AddModifyDeleteButtonPanel();

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Departments");
        jLabel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jDepartmentList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jDepartmentList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jDepartmentListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jDepartmentList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane2)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jAddModifyDeleteButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jAddModifyDeleteButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jDepartmentListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jDepartmentListValueChanged
        this.jAddModifyDeleteButtonPanel.setObjectSelectedCondition();
        int selection = this.jDepartmentList.getSelectedIndex();
        if (selection < 0 || selection > this.listModel.size())
            return;
        Department selectedDepartment = (Department) this.listModel.get(selection);
        this.parent.setSelectedDepartment(selectedDepartment);
    }//GEN-LAST:event_jDepartmentListValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.norvelle.addressdiscoverer.gui.AddModifyDeleteButtonPanel jAddModifyDeleteButtonPanel;
    private javax.swing.JList jDepartmentList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
