package org.pentaho.mantle.client.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SecurityPanel extends SimplePanel {

  private ArrayList<String> TEST_ROLES = new ArrayList<String>();
  private HashMap<String, Boolean> TEST_PERMISSIONS_MAP = new HashMap<String, Boolean>();
  private Button saveButton = new Button();
  
  public SecurityPanel() {
    
    TEST_ROLES.add("Authenticated");
    TEST_ROLES.add("Anonymous");
    
    TEST_PERMISSIONS_MAP.put("Read Content", true);
    TEST_PERMISSIONS_MAP.put("Administrate", true);
    TEST_PERMISSIONS_MAP.put("Create Content", true);
    
    saveButton.setStylePrimaryName("pentaho-button");
    
    HorizontalPanel roleTypeSelectionPanel = new HorizontalPanel();
    roleTypeSelectionPanel.add(new RadioButton("securityRole", "Roles"));
    roleTypeSelectionPanel.add(new RadioButton("securityRole", "System"));
    
    // add role type panel
    FlexTable securityPanel = new FlexTable();
    securityPanel.setWidget(0, 0, roleTypeSelectionPanel);
    securityPanel.getFlexCellFormatter().setColSpan(0, 0, 2);
    
    // add available roles list
    securityPanel.setWidget(1, 0, createRolesListBox(TEST_ROLES));
    securityPanel.getFlexCellFormatter().setRowSpan(1, 0, 2);
    
    // add description label
    securityPanel.setWidget(1, 1, new Label("System roles are built in roles...."));
    
    // add permission panel
    securityPanel.setWidget(2, 0, createPermissionPanel(TEST_PERMISSIONS_MAP));
    securityPanel.getFlexCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
    
    setWidget(securityPanel);
  }

  private ListBox createRolesListBox(List<String> roles) {
    ListBox box = new ListBox();
    for (String role : roles) {
      box.addItem(role);
    }
    box.setVisibleItemCount(20);
    box.setHeight("100%");
    box.setWidth("200px");
    return box;
  }
  
  private Widget createPermissionPanel(HashMap<String,Boolean> permissions) {
    CaptionPanel permissionCaptionPanel = new CaptionPanel("Permissions");
    
    VerticalPanel permissionPanel = new VerticalPanel();
    for (String perm : permissions.keySet()) {
      CheckBox permCB = new CheckBox(perm);
      permCB.setValue(permissions.get(perm));
      permissionPanel.add(permCB);
    }
    
    permissionCaptionPanel.add(permissionPanel);
    return permissionCaptionPanel;
  }
  
}
