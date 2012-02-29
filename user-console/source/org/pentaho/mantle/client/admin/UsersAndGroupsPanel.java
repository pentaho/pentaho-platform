package org.pentaho.mantle.client.admin;

import org.pentaho.gwt.widgets.client.buttons.ImageButton;
import org.pentaho.gwt.widgets.client.listbox.CustomListBox;
import org.pentaho.gwt.widgets.client.tabs.PentahoTabPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class UsersAndGroupsPanel extends SimplePanel implements ISysAdminPanel {

	String moduleBaseURL = GWT.getModuleBaseURL();
	String moduleName = GWT.getModuleName();
	String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));

	public UsersAndGroupsPanel() {
		FlexTable mainPanel = new FlexTable();

		mainPanel.setWidget(0, 0, new Label("Users/Roles"));
		
		PentahoTabPanel mainTabPanel = new PentahoTabPanel();
		mainTabPanel.setWidth("700px");
		mainTabPanel.setHeight("500px");
		mainTabPanel.addTab("Users", "", false, createUsersPanel());
		mainTabPanel.addTab("Roles", "", false, createGroupsPanel());
		mainPanel.setWidget(1, 0, mainTabPanel);
		
		setWidget(mainPanel);
	}
	
	private Widget createUsersPanel() {
		
		HorizontalPanel mainPanel = new HorizontalPanel();
		SimplePanel spacer3 = new SimplePanel();
		spacer3.setWidth("15px");
		mainPanel.add(spacer3);
		
		VerticalPanel availablePanel = new VerticalPanel();
		mainPanel.add(availablePanel);
		SimplePanel spacer4 = new SimplePanel();
		spacer4.setHeight("15px");
		availablePanel.add(spacer4);
		
		HorizontalPanel labelAndButtonsPanel = new HorizontalPanel();
		availablePanel.add(labelAndButtonsPanel);
		labelAndButtonsPanel.add(new Label("Available:"));
		SimplePanel spacer1 = new SimplePanel();
		spacer1.setWidth("103px");
		labelAndButtonsPanel.add(spacer1);
		labelAndButtonsPanel.add(new ImageButton(moduleBaseURL + "images/Add.png", "",""));
		SimplePanel spacer6 = new SimplePanel();
		spacer6.setWidth("7px");
		labelAndButtonsPanel.add(spacer6);
		labelAndButtonsPanel.add(new ImageButton(moduleBaseURL + "images/Remove.png", "",""));
				
		CustomListBox usersListBox = new CustomListBox();
		availablePanel.add(usersListBox);
		usersListBox.setVisibleRowCount(20);
		usersListBox.setWidth("200px");
		usersListBox.setHeight("415px");
		
		VerticalPanel detailsPanel = new VerticalPanel();
		mainPanel.add(detailsPanel);
		SimplePanel spacer5 = new SimplePanel();
		spacer5.setHeight("15px");
		detailsPanel.add(spacer5);		
		
		detailsPanel.add(new Label("Name:"));
		TextBox nameTextBox = new TextBox();
		detailsPanel.add(nameTextBox);
		
		detailsPanel.add(new Label("Password:"));
		PasswordTextBox passwordTextBox = new PasswordTextBox();
		detailsPanel.add(passwordTextBox);
		
		SimplePanel spacer2 = new SimplePanel();
		spacer2.setHeight("15px");
		detailsPanel.add(spacer2);
		
		detailsPanel.add(new Label("Role"));
		
		HorizontalPanel groupsPanel = new HorizontalPanel();
		detailsPanel.add(groupsPanel);
		
		VerticalPanel availableGroupsPanel = new VerticalPanel();
		groupsPanel.add(availableGroupsPanel);
		availableGroupsPanel.add(new Label("Available:"));
		CustomListBox availableGroupsListBox = new CustomListBox();
		availableGroupsPanel.add(availableGroupsListBox);
		availableGroupsListBox.setVisibleRowCount(20);
		availableGroupsListBox.setWidth("200px");
		availableGroupsListBox.setHeight("300px");
		
		VerticalPanel spacer7 = new VerticalPanel();
		spacer7.setWidth("15px");
		groupsPanel.add(spacer7);
		
		VerticalPanel arrowsPanel = new VerticalPanel();
		groupsPanel.add(arrowsPanel);
		arrowsPanel.setWidth("35px");		
		
		SimplePanel spacer8 = new SimplePanel();
		spacer8.setHeight("100px");
		arrowsPanel.add(spacer8);
		
		arrowsPanel.add(new ImageButton(moduleBaseURL + "images/accum_add.png", "",""));
		SimplePanel spacer9 = new SimplePanel();
		spacer9.setHeight("10px");
		arrowsPanel.add(spacer9);

		arrowsPanel.add(new ImageButton(moduleBaseURL + "images/accum_remove.png", "",""));
		SimplePanel spacer10 = new SimplePanel();
		spacer10.setHeight("30px");
		arrowsPanel.add(spacer10);

		arrowsPanel.add(new ImageButton(moduleBaseURL + "images/accum_add_all.png", "",""));
		SimplePanel spacer11 = new SimplePanel();
		spacer11.setHeight("10px");
		arrowsPanel.add(spacer11);

		arrowsPanel.add(new ImageButton(moduleBaseURL + "images/accum_remove_all.png", "",""));
		
		VerticalPanel selectedGroupsPanel = new VerticalPanel();
		groupsPanel.add(selectedGroupsPanel);
		selectedGroupsPanel.add(new Label("Selected:"));
		CustomListBox selectedGroupsListBox = new CustomListBox();
		selectedGroupsPanel.add(selectedGroupsListBox);
		selectedGroupsListBox.setVisibleRowCount(20);
		selectedGroupsListBox.setWidth("200px");
		selectedGroupsListBox.setHeight("300px");
		
		return mainPanel;
	}
	
	private Widget createGroupsPanel() {
		FlowPanel groupsPanel = new FlowPanel();
		return groupsPanel;
	}

	public void activate() {

	}

	public String getId() {
		return "actionBasedUsersAndGroupsPanel";
	}

	public void passivate(final AsyncCallback<Boolean> callback) {
		callback.onSuccess(true);
	}
}
