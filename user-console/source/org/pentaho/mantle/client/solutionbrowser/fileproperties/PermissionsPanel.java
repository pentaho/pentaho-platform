/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.solutionbrowser.fileproperties;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.*;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

/**
 * Permissions tab sub panel of FilePropertiesDialog. GET ACL call is performed
 * by FilePropertiesDialog and then passed to sub panels to consolidate
 */
public class PermissionsPanel extends FlexTable implements IFileModifier {

  private static final String RECIPIENT_TYPE_ELEMENT_NAME = "recipientType"; //$NON-NLS-1$
  private static final String PERMISSIONS_ELEMENT_NAME = "permissions"; //$NON-NLS-1$
  private static final String RECIPIENT_ELEMENT_NAME = "recipient"; //$NON-NLS-1$
  private static final String ACES_ELEMENT_NAME = "aces"; //$NON-NLS-1$
  private static final String OWNER_NAME_ELEMENT_NAME = "owner"; //$NON-NLS-1$
  private static final String OWNER_TYPE_ELEMENT_NAME = "ownerType"; //$NON-NLS-1$

  public static final int USER_TYPE = 0;
  public static final int ROLE_TYPE = 1;
  public static final int PERM_READ = 0;
  public static final int PERM_WRITE = 1;
  public static final int PERM_ACE_READ = 2;
  public static final int PERM_ACE_WRITE = 3;
  private static final String INHERITS_ELEMENT_NAME = "entriesInheriting"; //$NON-NLS-1$

  boolean dirty = false;

  ArrayList<String> existingUsersAndRoles = new ArrayList<String>();

  RepositoryFile fileSummary;
  Document fileInfo;

  ListBox usersAndRolesList = new ListBox(false);
  Label permissionsLabel = new Label(Messages.getString("permissionsColon")); //$NON-NLS-1$
  FlexTable permissionsTable = new FlexTable();
  Button removeButton = new Button(Messages.getString("remove")); //$NON-NLS-1$
  Button addButton = new Button(Messages.getString("addPeriods")); //$NON-NLS-1$

  final CheckBox readPermissionCheckBox = new CheckBox(Messages.getString("read")); //$NON-NLS-1$
  final CheckBox readAcePermissionCheckBox = new CheckBox(Messages.getString("readAcl")); //$NON-NLS-1$
  final CheckBox writePermissionCheckBox = new CheckBox(Messages.getString("write")); //$NON-NLS-1$
  final CheckBox writeAcePermissionCheckBox = new CheckBox(Messages.getString("writeAcl")); //$NON-NLS-1$

  final CheckBox inheritsCheckBox = new CheckBox(Messages.getString("inherits")); //$NON-NLS-1$

  /**
   *
   * @param fileSummary
   */
  public PermissionsPanel(RepositoryFile fileSummary) {
    this.fileSummary = fileSummary;

    removeButton.setStylePrimaryName("pentaho-button");
    addButton.setStylePrimaryName("pentaho-button");
    usersAndRolesList.getElement().setId("sharePanelUsersAndRolesList");
    addButton.getElement().setId("sharePanelAddButton");
    removeButton.getElement().setId("sharePanelRemoveButton");

    removeButton.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent clickEvent) {
        // find list to remove
        if (usersAndRolesList.getItemCount() == 0) {
          return;
        }
        dirty = true;
        final String userOrRoleString = usersAndRolesList.getValue(usersAndRolesList.getSelectedIndex());
        removeRecipient(userOrRoleString);
        usersAndRolesList.removeItem(usersAndRolesList.getSelectedIndex());
      }
    });

    addButton.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent clickEvent) {
        dirty = true;
        final SelectUserOrRoleDialog pickUserRoleDialog = new SelectUserOrRoleDialog(existingUsersAndRoles, new IUserRoleSelectedCallback() {

          public void roleSelected(String role) {
            usersAndRolesList.addItem(role, role); //$NON-NLS-1$
            existingUsersAndRoles.add(role);
            usersAndRolesList.setSelectedIndex(usersAndRolesList.getItemCount() - 1);
            addRecipient(role, ROLE_TYPE);
            buildPermissionsTable();
          }

          public void userSelected(String user) {
            usersAndRolesList.addItem(user, user); //$NON-NLS-1$
            existingUsersAndRoles.add(user);
            usersAndRolesList.setSelectedIndex(usersAndRolesList.getItemCount() - 1);
            addRecipient(user, USER_TYPE);
            buildPermissionsTable();
          }
        });
        pickUserRoleDialog.center();
      }

    });

    FlowPanel buttonPanel = new FlowPanel();
    buttonPanel.add(addButton);
    buttonPanel.add(removeButton);
    usersAndRolesList.setVisibleItemCount(7);
    usersAndRolesList.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent clickEvent) {
        // update permissions list and permission label (put username in it)
        // rebuild permissionsTable settings based on selected mask
        buildPermissionsTable();
      }

    });
    usersAndRolesList.setWidth("100%"); //$NON-NLS-1$
    buttonPanel.setWidth("100%"); //$NON-NLS-1$

    readPermissionCheckBox.getElement().setId("sharePermissionRead"); //$NON-NLS-1$
    readAcePermissionCheckBox.getElement().setId("sharePermissionReadAcl"); //$NON-NLS-1$
    writePermissionCheckBox.getElement().setId("sharePermissionWrite"); //$NON-NLS-1$
    writeAcePermissionCheckBox.getElement().setId("sharePermissionWriteAcl"); //$NON-NLS-1$

    readPermissionCheckBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        updatePermissionMask(readPermissionCheckBox.getValue(), PERM_READ);
      }
    });
    readAcePermissionCheckBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        updatePermissionMask(readAcePermissionCheckBox.getValue(), PERM_ACE_READ);
      }
    });
    writePermissionCheckBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        updatePermissionMask(writePermissionCheckBox.getValue(), PERM_WRITE);
      }
    });
    writeAcePermissionCheckBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        updatePermissionMask(writeAcePermissionCheckBox.getValue(), PERM_ACE_WRITE);
      }
    });

    readPermissionCheckBox.setEnabled(false);
    readAcePermissionCheckBox.setEnabled(false);

    inheritsCheckBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent clickEvent) {
        dirty = true;
        setInheritsAcls(inheritsCheckBox.getValue());
        writePermissionCheckBox.setEnabled(!inheritsCheckBox.getValue());
        writeAcePermissionCheckBox.setEnabled(!inheritsCheckBox.getValue());
        addButton.setEnabled(!inheritsCheckBox.getValue());
        removeButton.setEnabled(!inheritsCheckBox.getValue());
      }
    });

    int row = 0;
    setWidget(row++, 0, inheritsCheckBox);
    setWidget(row++, 0, new Label(Messages.getString("usersAndRoles"))); //$NON-NLS-1$
    setWidget(row++, 0, usersAndRolesList);

    // right justify button panel
    CellFormatter buttonPanelCellFormatter = new CellFormatter();
    buttonPanelCellFormatter.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
    setCellFormatter(buttonPanelCellFormatter);
    setWidget(row++, 0, buttonPanel);

    setWidget(row++, 0, permissionsLabel);
    setWidget(row++, 0, permissionsTable);

    setCellPadding(4);

    setWidth("100%"); //$NON-NLS-1$

    permissionsTable.setWidget(0, 0, readPermissionCheckBox);
    permissionsTable.setWidget(0, 1, readAcePermissionCheckBox);

    permissionsTable.setWidget(1, 0, writePermissionCheckBox);
    permissionsTable.setWidget(1, 1, writeAcePermissionCheckBox);

    permissionsTable.setStyleName("permissionsTable"); //$NON-NLS-1$
    permissionsTable.setWidth("100%"); //$NON-NLS-1$
    permissionsTable.setHeight("100%"); //$NON-NLS-1$

    init();
  }

  /**
   * Set the widgets according to what is currently in the DOM.
   */
  public void buildPermissionsTable() {
    String userOrRoleString = ""; //$NON-NLS-1$
    if (usersAndRolesList.getItemCount() == 0) {
      permissionsLabel.setText(Messages.getString("permissionsColon")); //$NON-NLS-1$
    } else {
      userOrRoleString = usersAndRolesList.getValue(usersAndRolesList.getSelectedIndex());
      permissionsLabel.setText(Messages.getString("permissionsFor", userOrRoleString)); //$NON-NLS-1$
    }

    List<Integer> perms = getPermissionsForUserOrRole(userOrRoleString);

    // create checkboxes, with listeners who update the fileInfo lists

    if ("".equals(userOrRoleString)) { //$NON-NLS-1$
      writePermissionCheckBox.setEnabled(false);
      writeAcePermissionCheckBox.setEnabled(false);
    }

    readPermissionCheckBox.setValue(perms.contains(PERM_READ));
    readAcePermissionCheckBox.setValue(perms.contains(PERM_ACE_READ));
    writePermissionCheckBox.setValue(perms.contains(PERM_WRITE));
    writeAcePermissionCheckBox.setValue(perms.contains(PERM_ACE_WRITE));
    inheritsCheckBox.setValue(isInheritsAcls());

    writePermissionCheckBox.setEnabled(!inheritsCheckBox.getValue());
    writeAcePermissionCheckBox.setEnabled(!inheritsCheckBox.getValue());

    addButton.setEnabled(!inheritsCheckBox.getValue());
    removeButton.setEnabled(!(isOwner(userOrRoleString, USER_TYPE) || isOwner(userOrRoleString, ROLE_TYPE)) && !inheritsCheckBox.getValue());
  }

  /**
   *
   * @param grant
   * @param perm
   */
  public void updatePermissionMask(boolean grant, int perm) {
    if (usersAndRolesList.getSelectedIndex() >= 0) {
      dirty = true;
      final String userOrRoleString = usersAndRolesList.getValue(usersAndRolesList.getSelectedIndex());
      updatePermissionForUserOrRole(userOrRoleString, grant, perm);
    }
  }

  /**
   * PUT acl changes back via REST call to /acl
   */
  public void apply() {
    // not used
  }

  /**
   *
   * @return
   */
  public List<RequestBuilder> prepareRequests(){
    ArrayList<RequestBuilder> requestBuilders = new ArrayList<RequestBuilder>();

    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
    String url = contextURL + "api/repo/files/" + SolutionBrowserPanel.pathToId(fileSummary.getPath()) + "/acl"; //$NON-NLS-1$//$NON-NLS-2$
    RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT, url);
    builder.setHeader("Content-Type", "application/xml");

    // At this point if we're inheriting we need to remove all the acls so that the inheriting flag isn't set by default
    if (isInheritsAcls()) {
      removeAllAces();
    }

    // set request data in builder itself
    builder.setRequestData(fileInfo.toString());

    // add builder to list to return to parent for execution
    requestBuilders.add(builder);

    return requestBuilders;
  }

  /**
   * Take permissions from fileInfo response and create roles and users list
   *
   * @param fileSummary
   * @param fileInfo
   */
  public void init(RepositoryFile fileSummary, Document fileInfo) {
    this.fileInfo = fileInfo;
    usersAndRolesList.clear();
    existingUsersAndRoles.clear();

    for (String name : getNames(USER_TYPE)) {
      usersAndRolesList.addItem(name, name); //$NON-NLS-1$
      existingUsersAndRoles.add(name);
    }
    for (String name : getNames(ROLE_TYPE)) {
      usersAndRolesList.addItem(name, name); //$NON-NLS-1$
      existingUsersAndRoles.add(name);
    }
    if (usersAndRolesList.getItemCount() > 0) {
      usersAndRolesList.setSelectedIndex(0);
    }

    buildPermissionsTable();
  }

  /**
   *
   */
  public void init() {
    // not doing anything right now. GET moved to FilePropertiesDialog parent and
    // response set in PermissionsPanel.setAclResponse
  }

  // *********************
  // Document manipulation
  // *********************
  void removeRecipient(String recipient) {
    NodeList aces = fileInfo.getElementsByTagName(ACES_ELEMENT_NAME);
    for (int i = 0; i < aces.getLength(); i++) {
      Element ace = (Element) aces.item(i);
      if (ace.getElementsByTagName(RECIPIENT_ELEMENT_NAME).item(0).getFirstChild().getNodeValue().equals(recipient)) {
        ace.getParentNode().removeChild(ace);
        break;
      }
    }
  }

  /**
   *
   * @param name
   * @param type
   * @return
   */
  private Boolean isOwner(String name, Integer type) {
    return name == getOwnerName() && type == getOwnerType();
  }

  /**
   *
   * @return
   */
  private String getOwnerName() {
    return fileInfo.getElementsByTagName(OWNER_NAME_ELEMENT_NAME).item(0).getFirstChild().getNodeValue();
  }

  /**
   *
   * @return
   */
  private Integer getOwnerType() {
    return Integer.parseInt(fileInfo.getElementsByTagName(OWNER_TYPE_ELEMENT_NAME).item(0).getFirstChild().getNodeValue());
  }

  /**
   *
   * @param recipientName
   * @param recipientType
   */
  void addRecipient(String recipientName, int recipientType) {
    Element newAces = fileInfo.createElement(ACES_ELEMENT_NAME);
    Element newPermission = fileInfo.createElement(PERMISSIONS_ELEMENT_NAME);
    Element newRecipient = fileInfo.createElement(RECIPIENT_ELEMENT_NAME);
    Element newRecipientType = fileInfo.createElement(RECIPIENT_TYPE_ELEMENT_NAME);
    Text textNode = fileInfo.createTextNode(recipientName);
    newRecipient.appendChild(textNode);
    textNode = fileInfo.createTextNode(Integer.toString(recipientType));
    newRecipientType.appendChild(textNode);
    newAces.appendChild(newPermission);
    newAces.appendChild(newRecipient);
    newAces.appendChild(newRecipientType);

    fileInfo.getDocumentElement().appendChild(newAces);
    // Base recipient is created at this point.
    // Now give them the default perms.
    updatePermissionForUserOrRole(recipientName, true, PERM_READ);
    updatePermissionForUserOrRole(recipientName, true, PERM_ACE_READ);
  }

  /**
   *
   * @param recipient
   * @param permission
   */
  private void addPermission(String recipient, int permission) {
    NodeList aces = fileInfo.getElementsByTagName(ACES_ELEMENT_NAME);
    for (int i = 0; i < aces.getLength(); i++) {
      Element ace = (Element) aces.item(i);
      if (ace.getElementsByTagName(RECIPIENT_ELEMENT_NAME).item(0).getFirstChild().getNodeValue().equals(recipient)) {
        Element newPerm = fileInfo.createElement(PERMISSIONS_ELEMENT_NAME);
        Text textNode = fileInfo.createTextNode(Integer.toString(permission));
        newPerm.appendChild(textNode);
        ace.appendChild(newPerm);
      }
    }
  }

  /**
   * @param type
   * @return list of names of given "type"
   */
  private List<String> getNames(int type) {
    List<String> names = new ArrayList<String>();
    NodeList aces = fileInfo.getElementsByTagName(ACES_ELEMENT_NAME);
    for (int i = 0; i < aces.getLength(); i++) {
      Element ace = (Element) aces.item(i);
      NodeList recipientTypeList = ace.getElementsByTagName(RECIPIENT_TYPE_ELEMENT_NAME);
      Node recipientNode = recipientTypeList.item(0);
      String nodeValue = recipientNode.getFirstChild().getNodeValue();
      int recipientType = Integer.parseInt(nodeValue);
      if (recipientType == type) {
        names.add(ace.getElementsByTagName(RECIPIENT_ELEMENT_NAME).item(0).getFirstChild().getNodeValue());
      }
    }
    return names;
  }

  /**
   * @param recipient
   * @return
   */
  private List<Integer> getPermissionsForUserOrRole(String recipient) {
    List<Integer> values = new ArrayList<Integer>();
    NodeList aces = fileInfo.getElementsByTagName(ACES_ELEMENT_NAME);
    for (int i = 0; i < aces.getLength(); i++) {
      Element ace = (Element) aces.item(i);
      if (ace.getElementsByTagName(RECIPIENT_ELEMENT_NAME).item(0).getFirstChild().getNodeValue().equals(recipient)) {
        NodeList permissions = ace.getElementsByTagName(PERMISSIONS_ELEMENT_NAME);
        for (int j = 0; j < permissions.getLength(); j++) {
          if (permissions.item(j).getFirstChild() != null) {
            values.add(new Integer(permissions.item(j).getFirstChild().getNodeValue()));
          }
        }
        break;
      }
    }
    return values;
  }

  /**
   * @param recipient
   * @param grant true = grant the Permission, false = deny the Permission (remove it if present)
   * @param perm The integer value of the Permission as defined in <code>RepositoryFilePermissions</code>
   */
  private void updatePermissionForUserOrRole(String recipient, boolean grant, int perm) {
    // first let's see if this node exists
    Node foundPermission = null;
    NodeList aces = fileInfo.getElementsByTagName(ACES_ELEMENT_NAME);
    for (int i = 0; i < aces.getLength(); i++) {
      Element ace = (Element) aces.item(i);
      if (ace.getElementsByTagName(RECIPIENT_ELEMENT_NAME).item(0).getFirstChild().getNodeValue().equals(recipient)) {
        NodeList permissions = ace.getElementsByTagName(PERMISSIONS_ELEMENT_NAME);
        for (int j = 0; j < permissions.getLength(); j++) {
          Node testNode = permissions.item(j);
          if (testNode.getFirstChild() != null && Integer.parseInt(testNode.getFirstChild().getNodeValue()) == perm) {
            foundPermission = testNode;
            break;
          }
        }
        break;
      }
    }

    if (grant) {
      if (foundPermission != null) { // This permission already exists.
        return;
      }
      addPermission(recipient, perm);
    } else {
      foundPermission.getParentNode().removeChild(foundPermission);
    }
  }

  /**
   * 
   */
  private void removeAllAces() {
    // Window.alert("removeAllAces() called with: \n" + fileInfo.toString());
    NodeList aces = fileInfo.getElementsByTagName(ACES_ELEMENT_NAME);
    while (aces != null && aces.getLength() > 0) {
      for (int i = 0; i < aces.getLength(); i++) {
        Node ace = aces.item(i);
        ace.getParentNode().removeChild(ace);
      }
      aces = fileInfo.getElementsByTagName(ACES_ELEMENT_NAME);
    }
  }

  /**
   *
   * @return
   */
  Boolean isInheritsAcls() {
    return Boolean.valueOf(fileInfo.getElementsByTagName(INHERITS_ELEMENT_NAME).item(0).getFirstChild().getNodeValue());
  }

  /**
   *
   * @param inherits
   */
  void setInheritsAcls(Boolean inherits) {
    fileInfo.getElementsByTagName(INHERITS_ELEMENT_NAME).item(0).getFirstChild().setNodeValue(inherits.toString());
  }

  /**
   * Get owner name from acl response
   * @param response
   */
  protected void setAclResponse(Response response){
    init(fileSummary, XMLParser.parse(response.getText()));
  }
}
