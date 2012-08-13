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
 * Created Aug, 2012
 * @author Ezequiel Cuellar
 */

package org.pentaho.mantle.client.admin;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public class LicenseManagerAdminPanel extends SimplePanel {

	public LicenseManagerAdminPanel() {

		FlexTable mainPanel = new FlexTable();
		HorizontalPanel hPanel = new HorizontalPanel();
		hPanel.add(new Label("License Manager"));
		mainPanel.setWidget(0, 0, hPanel);
		hPanel = new HorizontalPanel();
		mainPanel.setWidget(1, 0, hPanel);
		setWidget(mainPanel);
		this.setWidth("100%");
		this.setHeight("100%");
	}

}
