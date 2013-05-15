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

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Command;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.tabs.PentahoTabPanel;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionFileActionEvent;
import org.pentaho.mantle.client.events.SolutionFileHandler;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.fileproperties.FilePropertiesDialog;

public abstract class AbstractFilePropertiesCommand implements Command {

  private String moduleBaseURL = GWT.getModuleBaseURL();
  private String moduleName = GWT.getModuleName();
  private String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
  private static final int MANAGE_ACLS = 3;

  public AbstractFilePropertiesCommand() {
  }

  private RepositoryFile repositoryFile = null;

  public RepositoryFile getRepositoryFile() {
    return repositoryFile;
  }

  public void setRepositoryFile(RepositoryFile repositoryFile) {
    this.repositoryFile = repositoryFile;
  }

  public void execute() {
    final SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();
    if(this.getSolutionPath() != null){
      sbp.getFile(this.getSolutionPath(), new SolutionFileHandler() {
        @Override
        public void handle(RepositoryFile repositoryFile) {
          setRepositoryFile(repositoryFile);
          performOperation();
        }
      });
    }
    else{
      performOperation();
    }
  }

  public void performOperation() {

    final SolutionFileActionEvent event = new SolutionFileActionEvent();
    event.setAction(this.getClass().getName());

    if (getRepositoryFile() != null) {
      final RepositoryFile item = getRepositoryFile();
      
      // Checking if the user has access to manage permissions
      String url = contextURL + "api/repo/files/" + SolutionBrowserPanel.pathToId(item.getPath()) + "/canAccess?permissions="+MANAGE_ACLS; //$NON-NLS-1$ //$NON-NLS-2$
      RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
      try {
        builder.sendRequest(null, new RequestCallback() {

          public void onError(Request request, Throwable exception) {
            FilePropertiesDialog dialog = new FilePropertiesDialog(item, new PentahoTabPanel(), null, getActiveTab(), false);
            dialog.showTab(getActiveTab());
            dialog.center();

            event.setMessage(exception.getMessage());
            EventBusUtil.EVENT_BUS.fireEvent(event);
          }

          public void onResponseReceived(Request request, Response response) {
              FilePropertiesDialog dialog = new FilePropertiesDialog(item, new PentahoTabPanel(), null, getActiveTab(), Boolean.parseBoolean(response.getText()));
              dialog.showTab(getActiveTab());
              dialog.center();

              event.setMessage("Success");
              EventBusUtil.EVENT_BUS.fireEvent(event);
          }
        });
      } catch (RequestException e) {
        FilePropertiesDialog dialog = new FilePropertiesDialog(item, new PentahoTabPanel(), null, getActiveTab(), false);
        dialog.showTab(getActiveTab());
        dialog.center();

        event.setMessage(e.getMessage());
        EventBusUtil.EVENT_BUS.fireEvent(event);
      }
    }
  }

  protected abstract String getSolutionPath();
  protected abstract void setSolutionPath(String solutionPath);
  protected abstract FilePropertiesDialog.Tabs getActiveTab();
}
