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

import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EmailAdminPanel extends SimplePanel {

	public EmailAdminPanel() {

		FlexTable mainPanel = new FlexTable();
		mainPanel.setWidget(0, 0, new Label("Email" + "/" + "SMTP Server"));

		HorizontalPanel hPanel = new HorizontalPanel();

		SimplePanel hSpacer = new SimplePanel();
		hSpacer.setWidth("10px");
		hPanel.add(hSpacer);

		hPanel.add(createEmailPanel());

		mainPanel.setWidget(1, 0, hPanel);
		setWidget(mainPanel);

	}

	private Widget createEmailPanel() {
		VerticalPanel mailPanel = new VerticalPanel();

		SimplePanel vSpacer = new SimplePanel();
		vSpacer.setHeight("15px");
		mailPanel.add(vSpacer);

		mailPanel.add(new Label(Messages.getString("smtpHost") + ":"));
		TextBox smtpHostTextBox = new TextBox();
		smtpHostTextBox.setWidth("400px");
		mailPanel.add(smtpHostTextBox);

		mailPanel.add(new Label(Messages.getString("port") + ":"));
		TextBox portTextBox = new TextBox();
		portTextBox.setWidth("400px");
		mailPanel.add(portTextBox);

		mailPanel.add(new Label(Messages.getString("protocol") + ":"));
		ListBox protocolsListBox = new ListBox();
		protocolsListBox.addItem(Messages.getString("smtp"));
		protocolsListBox.addItem(Messages.getString("smtps"));
		mailPanel.add(protocolsListBox);

		CheckBox useStartTLSCheckBox = new CheckBox(Messages.getString("useStartTLS"));
		mailPanel.add(useStartTLSCheckBox);

		CheckBox useSSLCheckBox = new CheckBox(Messages.getString("useSSL"));
		mailPanel.add(useSSLCheckBox);

		mailPanel.add(new Label(Messages.getString("defaultFromAddress") + ":"));

		HorizontalPanel hPanel = new HorizontalPanel();
		TextBox fromAddressTextBox = new TextBox();
		fromAddressTextBox.setWidth("400px");
		hPanel.add(fromAddressTextBox);
		SimplePanel hSpacer = new SimplePanel();
		hSpacer.setWidth("15px");
		hPanel.add(hSpacer);
		Label emailOrginLabel = new Label(Messages.getString("emailOriginLabel"));
		emailOrginLabel.setStyleName("msg-Label");
		hPanel.add(emailOrginLabel);
		mailPanel.add(hPanel);

		CheckBox authenticationCheckBox = new CheckBox(Messages.getString("useAuthentication"));
		mailPanel.add(authenticationCheckBox);

		mailPanel.add(new Label(Messages.getString("userName") + ":"));
		TextBox userNameTextBox = new TextBox();
		userNameTextBox.setWidth("400px");
		mailPanel.add(userNameTextBox);

		mailPanel.add(new Label(Messages.getString("password") + ":"));

		hPanel = new HorizontalPanel();
		PasswordTextBox passwordTextBox = new PasswordTextBox();
		passwordTextBox.setWidth("400px");
		passwordTextBox.setEnabled(false);
		hPanel.add(passwordTextBox);
		
		hSpacer = new SimplePanel();
		hSpacer.setWidth("15px");
		hPanel.add(hSpacer);

		Button editPasswordButton = new Button(Messages.getString("edit") + "...");
		editPasswordButton.setStylePrimaryName("pentaho-button");
		hPanel.add(editPasswordButton);

		mailPanel.add(hPanel);

		CheckBox debuggingCheckBox = new CheckBox(Messages.getString("enableDebugging"));
		mailPanel.add(debuggingCheckBox);
		
	    vSpacer = new SimplePanel();
	    vSpacer.setHeight("45px");
		mailPanel.add(vSpacer);
		
		Button saveButton = new Button(Messages.getString("save"));
		saveButton.setStylePrimaryName("pentaho-button");
		mailPanel.add(saveButton);

		return mailPanel;
	}
}
