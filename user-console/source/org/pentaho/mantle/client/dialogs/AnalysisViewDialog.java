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
import java.util.HashMap;
import java.util.Map.Entry;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AnalysisViewDialog extends PromptDialogBox {

  private ListBox lboxSchema = new ListBox();

  private ListBox lboxCube = new ListBox();

  public static final String FOCUS_ON_TITLE = "title"; //$NON-NLS-1$

  private HashMap<String, ArrayList<String[]>> schemaCubeHashMap;

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
    AsyncCallback<HashMap<String, ArrayList<String[]>>> callback = new AsyncCallback<HashMap<String, ArrayList<String[]>>>() {
      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetFileProperties"), false, false, //$NON-NLS-1$ //$NON-NLS-2$
            true);
        dialogBox.center();
      }

      public void onSuccess(HashMap<String, ArrayList<String[]>> result) {
        if (result != null) {
          schemaCubeHashMap = result;

          if (schemaCubeHashMap != null && schemaCubeHashMap.size() >= 1) {

            for (Entry<String, ArrayList<String[]>> entry : schemaCubeHashMap.entrySet()) {
              lboxSchema.addItem(entry.getKey());
            }

            lboxSchema.setSelectedIndex(0);
            updateCubeListBox(lboxSchema.getItemText(lboxSchema.getSelectedIndex()));
          }
        } else {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("noMondrianSchemas"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
          dialogBox.center();
        }
      }
    };

    MantleServiceCache.getService().getMondrianCatalogs(callback);
  }

  /**
   * This method updates the cube list box based on the selection in the schema list box.
   * 
   * @param currentSchema
   *          The schema currently selected.
   */
  public void updateCubeListBox(String currentSchema) {
    lboxCube.clear();

    ArrayList<String[]> cubeNamesList = schemaCubeHashMap.get(currentSchema);
    int size = cubeNamesList.size();

    for (int i = 0; i < size; i++) {
      String name = cubeNamesList.get(i)[0];
      String id = cubeNamesList.get(i)[1];
      lboxCube.addItem(name, id);
    }
  }

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
