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

import com.google.gwt.user.client.ui.*;
import org.pentaho.gwt.widgets.client.buttons.ProgressIndicatorWidget;
import org.pentaho.gwt.widgets.client.panel.ActionBar;
import org.pentaho.gwt.widgets.client.text.ValidationPasswordTextBox;
import org.pentaho.gwt.widgets.client.text.ValidationTextBox;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;

public class EmailAdminPanel extends SimplePanel {

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	protected TextBox smtpHostTextBox;
	protected TextBox portTextBox;
	protected ListBox protocolsListBox;
	protected CheckBox useStartTLSCheckBox;
	protected CheckBox useSSLCheckBox;
	protected TextBox fromAddressTextBox;
	protected TextBox fromNameTextBox;
	protected CheckBox authenticationCheckBox;
	protected TextBox userNameTextBox;
	protected PasswordTextBox passwordTextBox;
	protected CheckBox debuggingCheckBox;
	protected ProgressIndicatorWidget progressIndicator;
	protected Button editPasswordButton;
	protected Button testButton;
	protected Button saveButton;
	protected VerticalPanel authenticationPanel;
	protected ActionBar actionBar;
	protected DockPanel dockPanel;

	public static native String getUserAgent() 
	/*-{
	  return navigator.userAgent.toLowerCase();
	}-*/;

	public static boolean isIE() {
	  return getUserAgent().contains("msie");
	}
	
	public EmailAdminPanel() {
		dockPanel = new DockPanel();
		actionBar = new ActionBar();
		FlexTable mainPanel = new FlexTable();
		HorizontalPanel hPanel = new HorizontalPanel();
		SimplePanel hSpacer = new SimplePanel();
		hSpacer.setWidth("10px");
		hPanel.add(hSpacer);
		hPanel.add(new Label(Messages.getString("emailSmtpServer")));
		mainPanel.setWidget(0, 0, hPanel);
		hPanel = new HorizontalPanel();
		hSpacer = new SimplePanel();
		hSpacer.setWidth("10px");
		hPanel.add(hSpacer);
		hPanel.add(createEmailPanel());
		mainPanel.setWidget(1, 0, hPanel);
		dockPanel.add(mainPanel, DockPanel.CENTER);
		dockPanel.setCellWidth(mainPanel, "100%");
		saveButton = new Button(Messages.getString("save"));
		progressIndicator = new ProgressIndicatorWidget(saveButton);
		actionBar.addWidget(progressIndicator, HorizontalPanel.ALIGN_RIGHT);
		dockPanel.add(actionBar, DockPanel.SOUTH);
		dockPanel.setCellVerticalAlignment(actionBar, HorizontalPanel.ALIGN_BOTTOM);
		dockPanel.setCellWidth(actionBar, "100%");
		dockPanel.setCellHeight(actionBar, "100%");
		setWidget(dockPanel);
		dockPanel.setHeight("100%");
		dockPanel.setWidth("100%");
		this.setWidth("100%");
		this.setHeight("100%");
    if (isIE()) {
      saveButton.setEnabled(true);
    } else {
      actionBar.expand(1);
    }
	}

	private Widget createEmailPanel() {
		VerticalPanel mailPanel = new VerticalPanel();

		SimplePanel vSpacer = new SimplePanel();
		vSpacer.setHeight("15px");
		mailPanel.add(vSpacer);

		mailPanel.add(new Label(Messages.getString("smtpHost") + ":"));
		smtpHostTextBox = new TextBox();
		smtpHostTextBox.setWidth("400px");
		mailPanel.add(smtpHostTextBox);

		vSpacer = new SimplePanel();
		vSpacer.setHeight("10px");
		mailPanel.add(vSpacer);

		mailPanel.add(new Label(Messages.getString("port") + ":"));
		portTextBox = new TextBox();
		portTextBox.setWidth("400px");
		mailPanel.add(portTextBox);

		vSpacer = new SimplePanel();
		vSpacer.setHeight("10px");
		mailPanel.add(vSpacer);

		mailPanel.add(new Label(Messages.getString("protocol") + ":"));
		protocolsListBox = new ListBox();
		protocolsListBox.addItem(Messages.getString("smtp"));
		protocolsListBox.addItem(Messages.getString("smtps"));
		mailPanel.add(protocolsListBox);

		vSpacer = new SimplePanel();
		vSpacer.setHeight("10px");
		mailPanel.add(vSpacer);

		useStartTLSCheckBox = new CheckBox(Messages.getString("useStartTLS"));
		mailPanel.add(useStartTLSCheckBox);

		useSSLCheckBox = new CheckBox(Messages.getString("useSSL"));
		mailPanel.add(useSSLCheckBox);

		vSpacer = new SimplePanel();
		vSpacer.setHeight("10px");
		mailPanel.add(vSpacer);

    mailPanel.add(new Label(Messages.getString("fromName") + ":"));
    fromNameTextBox = new TextBox();
    fromNameTextBox.setWidth("400px");
    mailPanel.add(fromNameTextBox);
    
    
		mailPanel.add(new Label(Messages.getString("defaultFromAddress") + ":"));
		HorizontalPanel hPanel = new HorizontalPanel();
		fromAddressTextBox = new TextBox();
		fromAddressTextBox.setWidth("400px");
		hPanel.add(fromAddressTextBox);
		SimplePanel hSpacer = new SimplePanel();
		hSpacer.setWidth("15px");
		hPanel.add(hSpacer);
		Label emailOrginLabel = new Label(Messages.getString("emailOriginLabel"));
		emailOrginLabel.setStyleName("msg-Label");
		hPanel.add(emailOrginLabel);
		mailPanel.add(hPanel);

		vSpacer = new SimplePanel();
		vSpacer.setHeight("10px");
		mailPanel.add(vSpacer);

		authenticationCheckBox = new CheckBox(Messages.getString("useAuthentication"));
		mailPanel.add(authenticationCheckBox);

		authenticationPanel = new VerticalPanel();
		mailPanel.add(authenticationPanel);
		authenticationPanel.add(new Label(Messages.getString("userName") + ":"));
		userNameTextBox = new TextBox();
		userNameTextBox.setWidth("400px");
		authenticationPanel.add(userNameTextBox);

		vSpacer = new SimplePanel();
		vSpacer.setHeight("10px");
		authenticationPanel.add(vSpacer);

		authenticationPanel.add(new Label(Messages.getString("password") + ":"));
		hPanel = new HorizontalPanel();
		passwordTextBox = new PasswordTextBox();
		passwordTextBox.setWidth("319px");
		hPanel.add(passwordTextBox);

		hSpacer = new SimplePanel();
		hSpacer.setWidth("15px");
		hPanel.add(hSpacer);

		editPasswordButton = new Button(Messages.getString("edit") + "...");
		editPasswordButton.setStylePrimaryName("pentaho-button");
		hPanel.add(editPasswordButton);
		editPasswordButton.setEnabled(false);
		authenticationPanel.add(hPanel);

		debuggingCheckBox = new CheckBox(Messages.getString("enableDebugging"));
		mailPanel.add(debuggingCheckBox);

		vSpacer = new SimplePanel();
		vSpacer.setHeight("20px");
		mailPanel.add(vSpacer);

		HorizontalPanel buttonsPanel = new HorizontalPanel();
		mailPanel.add(buttonsPanel);

		hSpacer = new SimplePanel();
		hSpacer.setWidth("230px");
		buttonsPanel.add(hSpacer);

		testButton = new Button(Messages.getString("connectionTest.label"));
		testButton.setStylePrimaryName("pentaho-button");
		buttonsPanel.add(testButton);

		return mailPanel;
	}

	protected boolean isPortValid(String portValue) {
		boolean portValid = true;
		try {
			Short port = Short.parseShort(portValue);
			if (port == -1) {
				portValid = false;
			}
		} catch (Exception e) {
			portValid = false;
		}
		return portValid;
	}
}
