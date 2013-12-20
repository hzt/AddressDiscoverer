/*
 * Copyright (C) 2013 Erik Norvelle <erik.norvelle@cyberlogos.co>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.norvelle.addressdiscoverer.gui;

import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout.Divider;
import org.jdesktop.swingx.MultiSplitLayout.Leaf;
import org.jdesktop.swingx.MultiSplitLayout.Split;
import org.norvelle.addressdiscoverer.AddressDiscoverer;
import org.norvelle.addressdiscoverer.model.Department;
import org.norvelle.addressdiscoverer.model.Institution;
import org.xml.sax.SAXException;

/**
 * Creates a three-way split pane without using nesting, thanks to the SwingX
 * JXMultiSplitPane component.
 * 
 * @author Erik Norvelle <erik.norvelle@cyberlogos.co>
 */
public class GUIManagementPane extends JPanel {
    
    private JFrame mainWindow;
    private AddressDiscoverer application;
    private EmailDiscoveryPanel emailDiscoveryPanel;
    private DepartmentListPanel departmentListPanel;
    private InstitutionListPanel institutionListPanel;
    
    public GUIManagementPane(JFrame parent, AddressDiscoverer application) 
    {
        this.mainWindow = parent;
        this.application = application;
        
        //Simple case: creates a split pane with three compartments
        JXMultiSplitPane sp = new JXMultiSplitPane();
        this.emailDiscoveryPanel = new EmailDiscoveryPanel(this);
        this.departmentListPanel = new DepartmentListPanel(this);
        this.institutionListPanel = new InstitutionListPanel(this);

        sp.setModel(new ThreeHorizontalSplitPaneModel(true));
        sp.add(this.institutionListPanel, ThreeHorizontalSplitPaneModel.P1);
        sp.add(this.departmentListPanel, ThreeHorizontalSplitPaneModel.P2);
        sp.add(this.emailDiscoveryPanel, ThreeHorizontalSplitPaneModel.P3);

        this.setLayout(new BorderLayout());
        this.add(sp);
    }

    public void setSelectedInstitution(Institution selectedInstitution) {
        this.departmentListPanel.setInstitution(selectedInstitution);
        AddressDiscoverer.application.statusChanged();
    }

    public void setSelectedDepartment(Department selectedDepartment) {
        try {
            this.emailDiscoveryPanel.setDepartment(selectedDepartment);
            AddressDiscoverer.application.statusChanged();
        } catch (IOException | SAXException ex) {
            AddressDiscoverer.reportException(ex);
        }
    }
    
}

class ThreeHorizontalSplitPaneModel extends Split
{
    // 3 possible positions
    public static final String P1 = "1";
    public static final String P2 = "2";
    public static final String P3 = "3";

    public ThreeHorizontalSplitPaneModel()
    {
        this(false);
    }

    public ThreeHorizontalSplitPaneModel(boolean isEquallyWeighted)
    {
        setRowLayout(true);
        Leaf p1 = new Leaf(P1);
        Leaf p2 = new Leaf(P2);
        Leaf p3 = new Leaf(P3);
        if (isEquallyWeighted)
        {
            p1.setWeight(0.2);
            p2.setWeight(0.2);
            p3.setWeight(0.2);
        }
        setChildren(p1, new Divider(), p2, new Divider(), p3, new Divider());
    }
}