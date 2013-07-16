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
package org.pentaho.mantle.client.commands;

import java.util.ArrayList;
import java.util.Collections;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.JsCreateNewConfig;
import org.pentaho.mantle.client.objects.JsCreateNewConfigComparator;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class NewDropdownCommand extends AbstractCommand {

  private Widget anchorWidget;

  public NewDropdownCommand() {
  }

  public NewDropdownCommand(Widget anchorWidget) {
    this.anchorWidget = anchorWidget;
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    String url = GWT.getHostPageBaseURL() + "api/plugin-manager/settings/new-toolbar-button"; //$NON-NLS-1$
    RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, url);
    rb.setHeader("Content-Type", "text/plain"); //$NON-NLS-1$//$NON-NLS-2$
    try {
      rb.sendRequest(null, new RequestCallback() {

        @Override
        public void onError(Request request, Throwable exception) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), exception.getMessage(), //$NON-NLS-1$ //$NON-NLS-2$
              false, false, true);
          dialogBox.center();
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == 200) {
            JsArray<JsCreateNewConfig> jsarray = parseJson(JsonUtils.escapeJsonForEval(response.getText()));
            final ArrayList<JsCreateNewConfig> sorted = new ArrayList<JsCreateNewConfig>();
            for (int i=0;i<jsarray.length();i++) {
              sorted.add(jsarray.get(i));
            }
            Collections.sort(sorted, new JsCreateNewConfigComparator());
            final PopupPanel popup = new PopupPanel(true, false);
            VerticalPanel buttonPanel = new VerticalPanel();
            popup.add(buttonPanel);
            for (int i=0;i<sorted.size();i++) {
              final int finali = i;
              Button button = new Button(Messages.getString(sorted.get(i).getLabel()));
              button.setStyleName("pentaho-button");
              button.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                  SolutionBrowserPanel.getInstance().getContentTabPanel().showNewURLTab(Messages.getString(sorted.get(finali).getTabName()), Messages.getString(sorted.get(finali).getTabName()), sorted.get(finali).getActionUrl(), false);
                  popup.hide();
                }
              });
              buttonPanel.add(button);
            }
            popup.setPopupPosition(anchorWidget.getAbsoluteLeft(), anchorWidget.getAbsoluteTop()+anchorWidget.getOffsetHeight());
            popup.show();
          } else {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("error"), //$NON-NLS-1$ //$NON-NLS-2$
                false, false, true);
            dialogBox.center();
          }
        }

      });
    } catch (RequestException e) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), e.getMessage(), //$NON-NLS-1$ //$NON-NLS-2$
          false, false, true);
      dialogBox.center();
    }

  }

  private native JsArray<JsCreateNewConfig> parseJson(String json)
  /*-{
    var obj = eval('(' + json + ')');
    return obj.setting;
  }-*/;  
  
  public Widget getAnchorWidget() {
    return anchorWidget;
  }

  public void setAnchorWidget(Widget anchorWidget) {
    this.anchorWidget = anchorWidget;
  }
}
