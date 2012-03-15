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
 * Created Mar 03, 2012
 * @author Ezequiel Cuellar
 */

package org.pentaho.mantle.client.admin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EmailAdminPanelController extends EmailAdminPanel implements ISysAdminPanel {

	public EmailAdminPanelController() {
		super();
		saveButton.addClickHandler(new SaveButtonChangeListener());
	}

	// -- Remote Calls.

	private void getEmailConfig(String params) {
		String serviceUrl = GWT.getHostPageBaseURL() + "api/emailconfig/setEmailConfig" + params;
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, serviceUrl);
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
				}

				public void onResponseReceived(Request request, Response response) {

				}
			});
		} catch (RequestException e) {
		}
	}

	private void getEmailConfig() {
		String serviceUrl = GWT.getHostPageBaseURL() + "api/emailconfig/getEmailConfig";
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, serviceUrl);
		try {
			executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
				}

				public void onResponseReceived(Request request, Response response) {
					JSONObject emailData = (JSONObject) JSONParser.parseLenient(response.getText());
					JSONBoolean authenticate = (JSONBoolean) emailData.get("authenticate");
					JSONBoolean debug = (JSONBoolean) emailData.get("debug");
					JSONString defaultFrom = (JSONString) emailData.get("defaultFrom");
					JSONString smtpHost = (JSONString) emailData.get("smtpHost");
					JSONNumber smtpPort = (JSONNumber) emailData.get("smtpPort");
					JSONString smtpProtocol = (JSONString) emailData.get("smtpProtocol");
					JSONString userId = (JSONString) emailData.get("userId");
					JSONString password = (JSONString) emailData.get("password");
					JSONBoolean useSsl = (JSONBoolean) emailData.get("useSsl");
					JSONBoolean useStartTls = (JSONBoolean) emailData.get("useStartTls");

					authenticationCheckBox.setValue(authenticate.booleanValue());
					smtpHostTextBox.setValue(smtpHost.stringValue());
					portTextBox.setValue(smtpPort.toString());
					useStartTLSCheckBox.setValue(useStartTls.booleanValue());
					useSSLCheckBox.setValue(useSsl.booleanValue());
					fromAddressTextBox.setValue(defaultFrom.stringValue());
					userNameTextBox.setValue(userId.stringValue());
					passwordTextBox.setValue(password.stringValue());
					debuggingCheckBox.setValue(debug.booleanValue());

					String protocol = smtpProtocol.stringValue();
					protocolsListBox.setSelectedIndex(protocol.equalsIgnoreCase("smtp") ? 0 : 1);
				}
			});
		} catch (RequestException e) {
		}
	}

	// -- ISysAdminPanel implementation.

	public void activate() {
		getEmailConfig();
	}

	public String getId() {
		return "emailAdminPanel";
	}

	public void passivate(final AsyncCallback<Boolean> callback) {
		callback.onSuccess(true);
	}

	// -- Event Listeners.

	class SaveButtonChangeListener implements ClickHandler {
		public void onClick(ClickEvent event) {

			StringBuffer params = new StringBuffer();
			params.append("?authenticate=");
			params.append(authenticationCheckBox.getValue());
			params.append("&debug=");
			params.append(debuggingCheckBox.getValue());
			params.append("&defaultFrom=");
			params.append(fromAddressTextBox.getValue());
			params.append("&smtpHost=");
			params.append(smtpHostTextBox.getValue());
			params.append("&smtpPort=");
			params.append(portTextBox.getValue());
			params.append("&smtpProtocol=");
			params.append(protocolsListBox.getValue(protocolsListBox.getSelectedIndex()).toLowerCase());
			params.append("&userId=");
			params.append(userNameTextBox.getValue());
			params.append("&password=");
			params.append(passwordTextBox.getValue());
			params.append("&useSsl=");
			params.append(useSSLCheckBox.getValue());
			params.append("&useStartTls=");
			params.append(useStartTLSCheckBox.getValue());

			getEmailConfig(params.toString());
		}
	}
}
