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
 */
package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.tabs.PentahoTabPanel;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.fileproperties.FilePropertiesDialog;
import org.pentaho.mantle.client.solutionbrowser.fileproperties.FilePropertiesDialog.Tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;

public class FilePropertiesCommand implements Command {

  Tabs defaultTab = Tabs.GENERAL;

  private RepositoryFile fileSummary;
  private String moduleBaseURL = GWT.getModuleBaseURL();
  private String moduleName = GWT.getModuleName();
  private String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
  private static final int MANAGE_ACLS = 3;
  public FilePropertiesCommand(RepositoryFile fileSummary) {
    this(fileSummary, Tabs.GENERAL);
  }
  
  public FilePropertiesCommand(RepositoryFile fileSummary, Tabs defaultTab) {
    this.fileSummary = fileSummary;
    this.defaultTab = defaultTab;
  }

  public void execute() {
    // Checking if the user has access to manage permissions
    String url = contextURL + "api/repo/files/" + SolutionBrowserPanel.pathToId(fileSummary.getPath()) + "/canAccess?permissions="+MANAGE_ACLS; //$NON-NLS-1$ //$NON-NLS-2$
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    try {
      builder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          FilePropertiesDialog dialog = new FilePropertiesDialog(fileSummary, new PentahoTabPanel(), null, defaultTab, false);
          dialog.showTab(defaultTab);
          dialog.center();            
        }

        public void onResponseReceived(Request request, Response response) {
            FilePropertiesDialog dialog = new FilePropertiesDialog(fileSummary, new PentahoTabPanel(), null, defaultTab, Boolean.parseBoolean(response.getText()));
            dialog.showTab(defaultTab);
            dialog.center();            
        }
      });
    } catch (RequestException e) {
      FilePropertiesDialog dialog = new FilePropertiesDialog(fileSummary, new PentahoTabPanel(), null, defaultTab, false);
      dialog.showTab(defaultTab);
      dialog.center();            
    }
  }

  public Tabs getDefaultTab() {
    return defaultTab;
  }

  public void setDefaultTab(Tabs defaultTab) {
    this.defaultTab = defaultTab;
  }
}
