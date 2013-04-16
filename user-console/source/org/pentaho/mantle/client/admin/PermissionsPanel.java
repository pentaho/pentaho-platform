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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Apr 10, 2013
 * @author Ezequiel Cuellar
 */

package org.pentaho.mantle.client.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PermissionsPanel extends VerticalPanel {
	
	private Map<String, LogicalRoleInfo> logicalRoles = new HashMap<String, LogicalRoleInfo>();
	private Map<String, List<String>> masterRoleMap = new HashMap<String, List<String>>();
	private Map<String, List<String>> newRoleAssignments = new HashMap<String, List<String>>();
	private ListBox rolesListBox;
	
	public PermissionsPanel(ListBox rolesListBox) {
		add(new Label(Messages.getString("absCaption")));
		this.rolesListBox = rolesListBox;
		initializeLogicalRoleMappings();
	}

	private void initializeLogicalRoleMappings() {
		final String url = GWT.getHostPageBaseURL() + "api/userrole/logicalRoleMap"; 
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
		executableTypesRequestBuilder.setHeader("accept", "application/json");
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {

				public void onError(Request request, Throwable exception) {
				}

				public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode() == Response.SC_OK) {
						JsLogicalRoleMap logicalRoleMap = (JsLogicalRoleMap) parseRoleMappings(JsonUtils.escapeJsonForEval(response.getText()));
						if (logicalRoles.size() == 0) {
							for (int i = 0; i < logicalRoleMap.getLogicalRoles().length(); i++) {

								CheckBox permCB = new CheckBox(logicalRoleMap.getLogicalRoles().get(i).getLocalizedName());
								permCB.addValueChangeHandler(new RolesValueChangeListener());
								add(permCB);
								logicalRoles.put(logicalRoleMap.getLogicalRoles().get(i).getLocalizedName(), new LogicalRoleInfo(logicalRoleMap.getLogicalRoles().get(i).getRoleName(), permCB));
							}
						}
						for (int j = 0; j < logicalRoleMap.getRoleAssignments().length(); j++) {
							String roleName = logicalRoleMap.getRoleAssignments().get(j).getRoleName();
							List<String> logicalRoles = new ArrayList<String>();
							JsArrayString jsLogicalRoles = logicalRoleMap.getRoleAssignments().get(j).getAssignedLogicalRoles();
							if (jsLogicalRoles != null) {
								for (int k = 0; k < jsLogicalRoles.length(); k++) {
									logicalRoles.add(jsLogicalRoles.get(k));
								}
							}
							masterRoleMap.put(roleName, logicalRoles);
						}
					}
				}
			});
		} catch (RequestException e) {
		}
	}
	
	public void setSelectedPermissions() {

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

	class RolesValueChangeListener implements ValueChangeHandler<Boolean> {
		public void onValueChange(ValueChangeEvent<Boolean> event) {
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
	        saveSecuritySettings();
	      }
		}
	}
	
	private void saveSecuritySettings() {
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
	    jsNewRoleAssignments.put("assignments", jsLogicalRoleAssignments);    
	    RequestBuilder saveSettingRequestBuilder = new RequestBuilder(RequestBuilder.PUT, GWT.getHostPageBaseURL() + "api/userrole/roleAssignments");
	    saveSettingRequestBuilder.setHeader("Content-Type", "application/json"); 
	    try {
	      saveSettingRequestBuilder.sendRequest(jsNewRoleAssignments.toString(), new RequestCallback() {

	        @Override
	        public void onError(Request request, Throwable exception) {
	        }

	        @Override
	        public void onResponseReceived(Request request, Response response) {
	          if (response.getStatusCode() == 200) {
	            masterRoleMap.putAll(newRoleAssignments);
	            newRoleAssignments.clear();
	          }
	        }
	      });
	    } catch (RequestException e) {
	    }
	  }

	class LogicalRoleInfo {
		String roleName;
		CheckBox checkBox;

		LogicalRoleInfo(String roleName, CheckBox checkBox) {
			this.roleName = roleName;
			this.checkBox = checkBox;
		}
	}

	private final native JavaScriptObject parseRoleMappings(String json)
	/*-{
	  var arr = [];
	  var obj = eval('(' + json + ')');
	  if (obj != null) {
	    if (obj.assignments.constructor.toString().indexOf("Array") == -1) {
	      arr.push(obj.assignments);
	      obj.assignments = arr;
	    }
	    for (var i = 0; i < obj.assignments.length; i++) {
	      arr = [];
	      if (obj.assignments[i].logicalRoles == undefined) continue;
	      if (obj.assignments[i].logicalRoles.constructor.toString().indexOf("Array") == -1) {
	        arr.push(obj.assignments[i].logicalRoles);
	        obj.assignments[i].logicalRoles = arr;
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
}
