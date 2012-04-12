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

import org.pentaho.mantle.client.workspace.WorkspacePanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;

public class RefreshWorkspaceCommand extends AbstractCommand {

  public RefreshWorkspaceCommand() {
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    try {
      final String url = GWT.getHostPageBaseURL() + "api/mantle/isAdministrator"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
      requestBuilder.setHeader("accept", "text/plain");
      requestBuilder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable caught) {
          GWT.runAsync(new RunAsyncCallback() {
            
            public void onSuccess() {
              WorkspacePanel.getInstance().refresh(false);
            }
            
            public void onFailure(Throwable reason) {
            }
          });
        }

        public void onResponseReceived(Request request, final Response response) {
          GWT.runAsync(new RunAsyncCallback() {
            
            public void onSuccess() {
              WorkspacePanel.getInstance().refresh("true".equalsIgnoreCase(response.getText()));
            }
            
            public void onFailure(Throwable reason) {
            }
          });
        }

      });
    } catch (RequestException e) {
      Window.alert(e.getMessage());
    }
  }

}
