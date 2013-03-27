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
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.scheduling.NewBlockoutScheduleDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.pentaho.mantle.client.workspace.WorkspacePanel.CellTableResources;
import static org.pentaho.mantle.client.workspace.WorkspacePanel.PAGE_SIZE;

public class BlockoutPanel extends SimplePanel {
  private CellTable<JsJobTrigger> table =
    new CellTable<JsJobTrigger>(PAGE_SIZE, (CellTableResources) GWT.create(CellTableResources.class));
  private ListDataProvider<JsJobTrigger> dataProvider = new ListDataProvider<JsJobTrigger>();
  private SimplePager pager;
  private final VerticalPanel widgets = new VerticalPanel();
  private Button blockoutButton;
  private IDialogCallback blockoutDialogCallback = new IDialogCallback() {
    public void okPressed() {
    }

    public void cancelPressed() {
    }
  };
  private Toolbar tableControls;
  private VerticalPanel tablePanel;

  public BlockoutPanel(final boolean isAdmin) {
    createUI(isAdmin);
    refresh();
  }

  @SuppressWarnings("EmptyCatchBlock")
  private void createUI(final boolean isAdmin) {
    widgets.setWidth("100%");
    table.getElement().setId("schedule-table");

    table.setSelectionModel(new MultiSelectionModel<JsJobTrigger>());
    TextColumn<JsJobTrigger> startColumn = new TextColumn<JsJobTrigger>() {
      public String getValue(JsJobTrigger block) {
        try {
          return formatDate(block.getStartTime());
        } catch (Throwable t) {
        }
        return "-";
      }
    };
    table.addColumn(startColumn, Messages.getString("blockoutColumnStarts"));
    table.addColumnStyleName(0, "backgroundContentHeaderTableCell");
    TextColumn<JsJobTrigger> endColumn = new TextColumn<JsJobTrigger>() {
      public String getValue(JsJobTrigger block) {
        try {
          Date nextFireTime = block.getNextFireTime();
          if(nextFireTime == null) {
            Date endDate = new Date(block.getStartTime().getTime() + block.getBlockDuration());
            return formatDate(endDate);
          } else {
            return formatDate(nextFireTime);
          }
        } catch (Throwable t) {
        }
        return "-";
      }
    };
    table.addColumn(endColumn, Messages.getString("blockoutColumnEnds"));
    table.addColumnStyleName(1, "backgroundContentHeaderTableCell");

    TextColumn<JsJobTrigger> repeatColumn = new TextColumn<JsJobTrigger>() {
      public String getValue(JsJobTrigger block) {
        try {
          return block.getSimpleDescription();
        } catch (Throwable t) {
        }
        return "-";
      }
    };
    table.addColumn(repeatColumn, Messages.getString("blockoutColumnRepeats"));
    table.addColumnStyleName(2, "backgroundContentHeaderTableCell");

    TextColumn<JsJobTrigger> endByColumn = new TextColumn<JsJobTrigger>() {
      public String getValue(JsJobTrigger block) {
        try {
          return formatDate(block.getEndTime());
        } catch (Throwable t) {
        }
        return "-";
      }
    };
    table.addColumn(endByColumn, Messages.getString("blockoutColumnRepeatsEndBy"));
    table.addColumnStyleName(3, "backgroundContentHeaderTableCell");

    Toolbar bar = new Toolbar();
    bar.addSpacer(10);
    bar.add(new Label(Messages.getString("blockoutHeadline")));

    bar.setWidth("100%");
    widgets.add(bar);
    blockoutButton = new Button(Messages.getString("createBlockoutTime"));
    tableControls = new Toolbar();
    tablePanel = new VerticalPanel();

    if (isAdmin) {
      final ClickHandler newBlockoutHandler = new ClickHandler() {
        @Override
        public void onClick(final ClickEvent clickEvent) {
          DialogBox blockoutDialog = new NewBlockoutScheduleDialog("", blockoutDialogCallback, false, true);
          blockoutDialog.center();
        }
      };
      blockoutButton.addClickHandler(newBlockoutHandler);
      blockoutButton.setStyleName("pentaho-button");
      widgets.add(blockoutButton);

      tableControls.addSpacer(10);
      tableControls.add(Toolbar.GLUE);
      ToolbarButton addButton = new ToolbarButton(new Image(MantleImages.images.add_icon()));
      addButton.setCommand(new Command() {
        @Override
        public void execute() {
          newBlockoutHandler.onClick(null);
        }
      });
      tableControls.add(addButton);
      tableControls.add(new ToolbarButton(new Image(MantleImages.images.edit16())));
      tableControls.add(new ToolbarButton(new Image(MantleImages.images.remove16())));
      tablePanel.add(tableControls);
    }


    dataProvider.addDataDisplay(table);
    SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
    pager = new SimplePager(SimplePager.TextLocation.CENTER, pagerResources, false, 0, true);
    pager.setDisplay(table);
    tablePanel.add(table);
    tablePanel.add(pager);
    widgets.add(tablePanel);
    setWidget(widgets);

  }

  private String formatDate(final Date date) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM dd h:mm a");
    return simpleDateFormat.format(date);
  }

  public void refresh() {
    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
    final String url = contextURL + "api/scheduler/blockout/list"; //$NON-NLS-1$
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
    executableTypesRequestBuilder.setHeader("If-Modified-Since", "01 Jan 1970 00:00:00 GMT");
    executableTypesRequestBuilder.setHeader("accept", "application/json");
    try {
      executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          // showError(exception);
        }

        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == Response.SC_OK) {
            if ("null".equals(response.getText())) {
              showData(null);
            } else {
              String json = JsonUtils.escapeJsonForEval(response.getText());
              JsArray<JsJobTrigger> allBlocks = parseJson(json);
              showData(allBlocks);
            }
          } else {
            // showServerError(response);
          }
        }
      });
    } catch (RequestException e) {
      // showError(e);
    }
  }

  private void showData(final JsArray<JsJobTrigger> allBlocks) {
    if (allBlocks == null || allBlocks.length() == 0) {
      table.setVisible(false);
      tableControls.setVisible(false);
      pager.setVisible(false);
      blockoutButton.setVisible(true);
    } else {
      table.setVisible(true);
      tableControls.setVisible(true);
      pager.setVisible(true);
      blockoutButton.setVisible(false);
      List<JsJobTrigger> filteredList = new ArrayList<JsJobTrigger>();
      for (int i = 0; i < allBlocks.length(); i++) {
        filteredList.add(allBlocks.get(i));
      }
      List<JsJobTrigger> list = dataProvider.getList();
      list.clear();
      list.addAll(filteredList);
      pager.setVisible(filteredList.size() > PAGE_SIZE);
      table.setVisible(filteredList.size() > 0);
      table.redraw();
    }

  }

  private native JsArray<JsJobTrigger> parseJson(String json)
  /*-{
    var obj = eval('(' + json + ')');
    return obj.simpleBlockoutTrigger;
  }-*/;
}
