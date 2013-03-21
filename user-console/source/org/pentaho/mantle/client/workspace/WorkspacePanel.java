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
package org.pentaho.mantle.client.workspace;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.VerticalPanel;

public class WorkspacePanel extends VerticalPanel {
    private static WorkspacePanel instance = new WorkspacePanel();
    private final SchedulesPanel schedulesPanel;
    private final BlockoutPanel blockoutPanel;

    private boolean isScheduler;
    private boolean isAdmin;



    public static WorkspacePanel getInstance() {
        return instance;
    }

    public WorkspacePanel() {
        try {
            final String url = GWT.getHostPageBaseURL() + "api/repo/files/canAdminister"; //$NON-NLS-1$
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
            requestBuilder.setHeader("accept", "text/plain");
            requestBuilder.setHeader("If-Modified-Since", "01 Jan 1970 00:00:00 GMT");
            requestBuilder.sendRequest(null, new RequestCallback() {

                public void onError(Request request, Throwable caught) {
                    isAdmin = false;
                    isScheduler = false;
                }

                public void onResponseReceived(Request request, Response response) {
                    isAdmin = "true".equalsIgnoreCase(response.getText());

                    try {
                        final String url2 = GWT.getHostPageBaseURL() + "api/scheduler/canSchedule"; //$NON-NLS-1$
                        RequestBuilder requestBuilder2 = new RequestBuilder(RequestBuilder.GET, url2);
                        requestBuilder2.setHeader("accept", "text/plain");
                        requestBuilder2.sendRequest(null, new RequestCallback() {

                            public void onError(Request request, Throwable caught) {
                                isScheduler = false;
                            }

                            public void onResponseReceived(Request request, Response response) {
                                isScheduler = "true".equalsIgnoreCase(response.getText());
                            }

                        });
                    } catch (RequestException e) {
                        Window.alert(e.getMessage());
                    }
                }
            });
        } catch (RequestException e) {
            Window.alert(e.getMessage());
        }

        schedulesPanel = new SchedulesPanel(isAdmin, isScheduler);
        add(schedulesPanel);
        blockoutPanel = BlockoutPanel.getInstance();
        add(blockoutPanel);

    }

    public void refresh() {
        schedulesPanel.refresh();
        blockoutPanel.refresh();
    }
}
