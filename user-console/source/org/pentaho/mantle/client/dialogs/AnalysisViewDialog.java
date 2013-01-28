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
package org.pentaho.mantle.client.dialogs;

import java.util.ArrayList;
import java.util.HashSet;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AnalysisViewDialog extends PromptDialogBox {

  private ListBox lboxSchema = new ListBox();

  private ListBox lboxCube = new ListBox();

  public static final String FOCUS_ON_TITLE = "title"; //$NON-NLS-1$

  private JsArray<JsCube> cubes;

  public AnalysisViewDialog() {
    super(Messages.getString("newAnalysisView"), Messages.getString("ok"), Messages.getString("cancel"), false, true, new VerticalPanel()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    buildAnalysisView();
    lboxSchema.getElement().setId("schemaList");
    lboxSchema.setTabIndex(1);
    lboxCube.getElement().setId("cubeList");
    lboxCube.setTabIndex(2);

    setFocusWidget(lboxSchema);
  }

  /**
   * Actual construction of the New Analysis View Dialog.
   * 
   * @return The container that contains all the requisite widget.
   */
  private Widget buildAnalysisView() {
    VerticalPanel mainPanel = (VerticalPanel) getContent();
    mainPanel.setSpacing(5);
    Label schemaLabel = new Label(Messages.getString("schema")); //$NON-NLS-1$
    Label cubeLabel = new Label(Messages.getString("cube")); //$NON-NLS-1$

    lboxSchema.addChangeHandler(new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        final String currentSchema = lboxSchema.getItemText(lboxSchema.getSelectedIndex());
        updateCubeListBox(currentSchema);
      }
    });

    // Get the pertinent information for the cube and the schema.
    getSchemaAndCubeInfo();

    lboxSchema.setWidth("15em"); //$NON-NLS-1$
    lboxCube.setWidth("15em"); //$NON-NLS-1$
    mainPanel.add(schemaLabel);
    mainPanel.add(lboxSchema);
    mainPanel.add(cubeLabel);
    mainPanel.add(lboxCube);

    return mainPanel;
  }

  /*
   * We only need get methods because the set is implemented by the widget itself.
   */

  public String getSchema() {
    return lboxSchema.getItemText(lboxSchema.getSelectedIndex());
  }

  public String getCube() {
    return lboxCube.getValue(lboxCube.getSelectedIndex());
  }

  /**
   * Populates the schema and cube list box based on the information retrieved from the catalogs.
   */
  private void getSchemaAndCubeInfo() {

    final String url = GWT.getHostPageBaseURL() + "api/mantle/cubes"; //$NON-NLS-1$
    RequestBuilder cubesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
    cubesRequestBuilder.setHeader("accept", "application/json");
    try {
      cubesRequestBuilder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetFileProperties"), false, false, //$NON-NLS-1$ //$NON-NLS-2$
              true);
          dialogBox.center();
        }

        public void onResponseReceived(Request request, Response response) {
          cubes = parseCubeJson(JsonUtils.escapeJsonForEval(response.getText()));
          HashSet<String> schemas = new HashSet<String>();
          for (int i = 0; i < cubes.length(); i++) {
            schemas.add(cubes.get(i).getCatName());
          }
          for (String catName : schemas) {
            lboxSchema.addItem(catName);
          }
          lboxSchema.setSelectedIndex(0);
          updateCubeListBox(lboxSchema.getItemText(lboxSchema.getSelectedIndex()));
        }
      });
    } catch (RequestException e) {
      // showError(e);
    }

  }

  /**
   * This method updates the cube list box based on the selection in the schema list box.
   * 
   * @param currentSchema
   *          The schema currently selected.
   */
  public void updateCubeListBox(String currentSchema) {
    lboxCube.clear();

    ArrayList<JsCube> cubesForSchema = new ArrayList<JsCube>();

    for (int i = 0; i < cubes.length(); i++) {
      JsCube cube = cubes.get(i);
      if (cube.getCatName().equalsIgnoreCase(currentSchema)) {
        cubesForSchema.add(cube);
      }
    }

    for (int i = 0; i < cubesForSchema.size(); i++) {
      String name = cubesForSchema.get(i).getName();
      String id = cubesForSchema.get(i).getId();
      lboxCube.addItem(name, id);
    }
  }

  private final native JsArray<JsCube> parseCubeJson(String json)
  /*-{
    var obj = eval('(' + json + ')');
    return obj.cubes;
  }-*/;

  /**
   * Checks the input fields for input.
   * 
   * @return Returns false if the expected inputs were not provided.
   */
  public boolean validate() {
    final String schema = getSchema();
    if (schema == null || schema.length() == 0) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("selectSchema"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
      dialogBox.setWidth("15em"); //$NON-NLS-1$
      dialogBox.center();
      return false;
    }

    final String cube = getSchema();
    if (cube == null || cube.length() == 0) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("selectCube"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
      dialogBox.setWidth("15em"); //$NON-NLS-1$
      dialogBox.center();
      return false;
    }

    return true;
  }
} // End of class.
