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
 */

package org.pentaho.mantle.client.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.gwt.widgets.client.dialogs.IThreeButtonDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.messages.Messages;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SecurityPanel extends SimplePanel implements ChangeHandler, ValueChangeHandler<Boolean>, ClickHandler, ISysAdminPanel {

  class LogicalRoleInfo {
    String roleName;
    CheckBox checkBox;
    
    LogicalRoleInfo(String roleName, CheckBox checkBox) {
      this.roleName = roleName;
      this.checkBox = checkBox;
    }
  }
  private static final int SYSTEM_ROLES_TYPE = 0;
  private static final int REGULAR_ROLES_TYPE = 1;
    
  String moduleBaseURL = GWT.getModuleBaseURL();
  String moduleName = GWT.getModuleName();
  String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
  
  private ArrayList<String> userRoles = new ArrayList<String>();
  private ArrayList<String> systemRoles = new ArrayList<String>();
  private ListBox rolesListBox;
  private Button saveButton = new Button("Save");
  private VerticalPanel permissionPanel;
  private Map<String, List<String>> masterRoleMap = new HashMap<String, List<String>>();
  private Map<String, List<String>> newRoleAssignments = new HashMap<String, List<String>>();
  private Map<String, LogicalRoleInfo> logicalRoles = new HashMap<String, LogicalRoleInfo>();
  private RadioButton regularRolesBtn = new RadioButton("securityRole", "Roles");
  private RadioButton systemRolesBtn = new RadioButton("securityRole", "System");
  
  private static SecurityPanel instance = new SecurityPanel();
  
  public static SecurityPanel getInstance() {
    return instance;
  }
  
  private SecurityPanel() {
    
    saveButton.setStylePrimaryName("pentaho-button");
    
    HorizontalPanel roleTypeSelectionPanel = new HorizontalPanel();
    regularRolesBtn.addValueChangeHandler(this);
    systemRolesBtn.addValueChangeHandler(this);
    roleTypeSelectionPanel.add(regularRolesBtn);
    roleTypeSelectionPanel.add(systemRolesBtn);
    
    // add role type panel
    FlexTable securityPanel = new FlexTable();
    securityPanel.setWidget(0, 0, roleTypeSelectionPanel);
    securityPanel.getFlexCellFormatter().setColSpan(0, 0, 2);
    
    // add available roles list
    securityPanel.setWidget(1, 0, createRolesListBox());
    securityPanel.getFlexCellFormatter().setRowSpan(1, 0, 2);
    
    saveButton.addClickHandler(this);
    securityPanel.setWidget(2, 0, saveButton);
    
    securityPanel.setWidget(1, 1, createPermissionPanel());
    securityPanel.getFlexCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
    
    setWidget(securityPanel);
    
    initializeAvailUserRoles();
    initializeSystemRoles();
    initializeLogicalRoleMappings();

  }

  private ListBox createRolesListBox() {
    rolesListBox = new ListBox();
    rolesListBox.setVisibleItemCount(20);
    rolesListBox.setHeight("100%");
    rolesListBox.setWidth("200px");
    rolesListBox.addChangeHandler(this);
    return rolesListBox;
  }
  
  private Widget createPermissionPanel() {
    CaptionPanel permissionCaptionPanel = new CaptionPanel("Permissions");
    
    permissionPanel = new VerticalPanel();
    
    permissionCaptionPanel.add(permissionPanel);
    return permissionCaptionPanel;
  }
  
  public void initializeAvailUserRoles() {
    final String url = GWT.getHostPageBaseURL() + "api/userrole/roles"; //$NON-NLS-1$
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
    executableTypesRequestBuilder.setHeader("accept", "application/json");
    try {
      executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
        }

        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == Response.SC_OK) {
            
            JsUserRoleList roleList = (JsUserRoleList)parseRoleList(JsonUtils.escapeJsonForEval(response.getText()));
            JsArrayString jsRoleNames = roleList.getRoles();
            for (int i = 0; i < jsRoleNames.length(); i++) {
              userRoles.add(jsRoleNames.get(i));
            }
            Collections.sort(userRoles);
            showRoles(REGULAR_ROLES_TYPE);
          } else {
           }
        }
      });
    } catch (RequestException e) {
    }
  }

  private void initializeLogicalRoleMappings() {
    final String url = GWT.getHostPageBaseURL() + "api/userrole/logicalRoleMap"; //$NON-NLS-1$
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
    executableTypesRequestBuilder.setHeader("accept", "application/json");
    try {
      executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
        }

        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == Response.SC_OK) {           
            JsLogicalRoleMap logicalRoleMap = (JsLogicalRoleMap)parseRoleMappings(JsonUtils.escapeJsonForEval(response.getText()));
            if (logicalRoles.size() == 0) {
              for (int i = 0; i < logicalRoleMap.getLogicalRoles().length(); i++) {
                
                CheckBox permCB = new CheckBox(logicalRoleMap.getLogicalRoles().get(i).getLocalizedName());         
                permCB.addValueChangeHandler(SecurityPanel.this);
                permissionPanel.add(permCB);
                logicalRoles.put(logicalRoleMap.getLogicalRoles().get(i).getLocalizedName(), new LogicalRoleInfo(logicalRoleMap.getLogicalRoles().get(i).getRoleName(), permCB) );
              }
            }
            for (int j = 0; j < logicalRoleMap.getRoleAssignments().length(); j++) {
              String roleName = logicalRoleMap.getRoleAssignments().get(j).getRoleName();
              List<String> logicalRoles = new ArrayList<String>();
              for (int k = 0; k < logicalRoleMap.getRoleAssignments().get(j).getAssignedLogicalRoles().length(); k++) {
                logicalRoles.add(logicalRoleMap.getRoleAssignments().get(j).getAssignedLogicalRoles().get(k));
              }
              masterRoleMap.put(roleName, logicalRoles);
            }
          } else {
          }
        }
      });
    } catch (RequestException e) {
     }
  }
  
  public void onChange(ChangeEvent event) {
    int selectedIndex = rolesListBox.getSelectedIndex();
    if (selectedIndex >= 0) {
      String roleName = rolesListBox.getItemText(selectedIndex);
      List<String> logicalRoleAssignments = newRoleAssignments.get(roleName);
      if (logicalRoleAssignments == null) {
        logicalRoleAssignments = masterRoleMap.get(roleName);
      }
      for (LogicalRoleInfo logicalRoleInfo : logicalRoles.values()) {
        logicalRoleInfo.checkBox.setValue((logicalRoleAssignments != null) && logicalRoleAssignments.contains(logicalRoleInfo.roleName));
      }
    }
  }
  
  public void onValueChange(ValueChangeEvent<Boolean> event) {
    if (event.getSource() == regularRolesBtn) {
      if (regularRolesBtn.getValue()) {
        showRoles(REGULAR_ROLES_TYPE);
      }
    } else if (event.getSource() == systemRolesBtn) {
      if (systemRolesBtn.getValue()) {
        showRoles(SYSTEM_ROLES_TYPE);
      }
    } else {
      if (rolesListBox.getSelectedIndex() >= 0) {
        ArrayList<String> selectedLogicalRoles = new ArrayList<String>();
        for (LogicalRoleInfo logicalRoleInfo : logicalRoles.values()) {
          if (logicalRoleInfo.checkBox.getValue()) {
            selectedLogicalRoles.add(logicalRoleInfo.roleName);
          }
        }
        String runtimeRole = rolesListBox.getItemText(rolesListBox.getSelectedIndex());
        List<String> originalRoles = masterRoleMap.get(runtimeRole);
        if (((originalRoles == null) || (originalRoles.size() == 0)) && (selectedLogicalRoles.size() == 0)) {
          newRoleAssignments.remove(runtimeRole);
        } else if ((originalRoles != null) && (originalRoles.containsAll(selectedLogicalRoles)) && selectedLogicalRoles.containsAll(originalRoles)) {
          newRoleAssignments.remove(runtimeRole);
        } else {
          newRoleAssignments.put(runtimeRole, selectedLogicalRoles);
        }
      }
    }
    
  }

  private final native JavaScriptObject parseRoleList(String json)
  /*-{
    var obj = eval('(' + json + ')');
    if (obj != null) {
      if (obj.roles.constructor.toString().indexOf("Array") == -1) {
        var arr = [];
        arr.push(obj.roles);
        obj.roles = arr;
      }
    }
    return obj;
  }-*/;

  
  
  private final native JavaScriptObject parseRoleMappings(String json)
  /*-{
    var arr = [];
    var obj = eval('(' + json + ')');
    if (obj != null) {
      if (obj.logicalRoleAssignments.constructor.toString().indexOf("Array") == -1) {
        arr.push(obj.logicalRoleAssignments);
        obj.logicalRoleAssignments = arr;
      }
      for (var i = 0; i < obj.logicalRoleAssignments.length; i++) {
        arr = [];
        if (obj.logicalRoleAssignments[i].logicalRoles == undefined) continue;
        if (obj.logicalRoleAssignments[i].logicalRoles.constructor.toString().indexOf("Array") == -1) {
          arr.push(obj.logicalRoleAssignments[i].logicalRoles);
          obj.logicalRoleAssignments[i].logicalRoles = arr;
        }
      }
      if (obj.localizedRoleNames.constructor.toString().indexOf("Array") == -1) {
        arr = [];
        arr.push(obj.localizedRoleNames);
        obj.localizedRoleNames = arr;
      }
    }
    
    return obj;
  }-*/;
  
  private void saveSecuritySettings(final AsyncCallback<Boolean> callback) {
    JSONObject jsNewRoleAssignments = new JSONObject();
    JSONArray jsLogicalRoleAssignments = new JSONArray();
    int x = 0;
    for (Map.Entry<String, List<String>> roleAssignment : newRoleAssignments.entrySet()) {
      JSONArray jsLogicalRoles = new JSONArray();
      int y = 0;
      for (String logicalRoleName : roleAssignment.getValue()) {
        jsLogicalRoles.set(y++, new JSONString(logicalRoleName));
      }
      JSONObject jsRoleAssignment = new JSONObject();
      jsRoleAssignment.put("roleName", new JSONString(roleAssignment.getKey()));
      jsRoleAssignment.put("logicalRoles", jsLogicalRoles);
      jsLogicalRoleAssignments.set(x++, jsRoleAssignment);
    }   
    jsNewRoleAssignments.put("logicalRoleAssignments", jsLogicalRoleAssignments);    
    RequestBuilder saveSettingRequestBuilder = new RequestBuilder(RequestBuilder.PUT, contextURL + "api/userrole/roleAssignments");
    saveSettingRequestBuilder.setHeader("Content-Type", "application/json");  //$NON-NLS-1$//$NON-NLS-2$
    WaitPopup.getInstance().setVisible(true);
    try {
      saveSettingRequestBuilder.sendRequest(jsNewRoleAssignments.toString(), new RequestCallback() {

        @Override
        public void onError(Request request, Throwable exception) {
          WaitPopup.getInstance().setVisible(false);
          callback.onFailure(exception);
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
          WaitPopup.getInstance().setVisible(false);
          if (response.getStatusCode() == 200) {
            masterRoleMap.putAll(newRoleAssignments);
            newRoleAssignments.clear();
            callback.onSuccess(true);
          } else {
            callback.onSuccess(false);
          }                
        }
        
      });
    } catch (RequestException e) {
      WaitPopup.getInstance().setVisible(false);
      callback.onFailure(e);
    }
  }
  
  private void showRoles(int roleType) {
    regularRolesBtn.setValue(roleType == REGULAR_ROLES_TYPE);
    systemRolesBtn.setValue(roleType == SYSTEM_ROLES_TYPE);
    for (LogicalRoleInfo logicalRoleInfo : logicalRoles.values()) {
      logicalRoleInfo.checkBox.setValue(false);
    }
    rolesListBox.clear();
    for (String role : userRoles) {
      if (((roleType == SYSTEM_ROLES_TYPE) && systemRoles.contains(role))
          || ((roleType != SYSTEM_ROLES_TYPE) && !systemRoles.contains(role))) {
        rolesListBox.addItem(role);
      }
    }
  }
  
  public void onClick(ClickEvent event) {
    if (event.getSource() == saveButton) {
      saveSecuritySettings(new AsyncCallback<Boolean>() {

        public void onFailure(Throwable caught) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), caught.toString(), false, false, true); //$NON-NLS-1$
          dialogBox.center();
        }

        public void onSuccess(Boolean result) {
          if (result) {
            MessageDialogBox dialogBox = new MessageDialogBox("ABS", "Role Mappings Saved", //$NON-NLS-1$ //$NON-NLS-2$
                false, false, true);
            dialogBox.center();
          } else {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), "Unable to save changes. Check server log for errors.", //$NON-NLS-1$ //$NON-NLS-2$
                false, false, true);
            dialogBox.center();
          }
        }
      });
    }
  }

  public void activate() {
    masterRoleMap.clear();
    newRoleAssignments.clear();
    rolesListBox.clear();
    userRoles.clear();
    for (LogicalRoleInfo logicalRoleInfo : logicalRoles.values()) {
      logicalRoleInfo.checkBox.setValue(false);
    }
    initializeAvailUserRoles();
    initializeLogicalRoleMappings();
  }

  public String getId() {
    return "actionBasedSecurityAdminPanel";
  }

  
  public void passivate(final AsyncCallback<Boolean> callback) {
    if (newRoleAssignments.size() > 0) {
      MessageDialogBox dialog = new MessageDialogBox("ABS", "Save changes? Choosing \"No\" will result is the loss of changes made.", false, true, true, "Yes", "No", "Cancel");
      dialog.setCallback(new IThreeButtonDialogCallback() {
        
        public void okPressed() {
          saveSecuritySettings(callback);
        }
        
        public void cancelPressed() {
          callback.onSuccess(false);
        }
        
        public void notOkPressed() {
          callback.onSuccess(true);
        }
      });
      dialog.center();
    } else {
      callback.onSuccess(true);
    }
  }

  private void initializeSystemRoles() {
    final String url = GWT.getHostPageBaseURL() + "api/userrole/systemRoles"; //$NON-NLS-1$
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
    executableTypesRequestBuilder.setHeader("accept", "application/json");
    try {
      executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
        }

        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == Response.SC_OK) {
            
            JsUserRoleList roleList = (JsUserRoleList)parseRoleList(JsonUtils.escapeJsonForEval(response.getText()));
            JsArrayString jsRoleNames = roleList.getRoles();
            for (int i = 0; i < jsRoleNames.length(); i++) {
              systemRoles.add(jsRoleNames.get(i));
            }
            Collections.sort(systemRoles);
          } else {
           }
        }
      });
    } catch (RequestException e) {
    }
  }
}
