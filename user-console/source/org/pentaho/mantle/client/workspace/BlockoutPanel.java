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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.pentaho.mantle.client.workspace.WorkspacePanel.CellTableResources;
import static org.pentaho.mantle.client.workspace.WorkspacePanel.PAGE_SIZE;

public class BlockoutPanel extends SimplePanel {
  private CellTable<JsJob> table =
    new CellTable<JsJob>(PAGE_SIZE, (CellTableResources) GWT.create(CellTableResources.class));
  private ListDataProvider<JsJob> dataProvider = new ListDataProvider<JsJob>();
  private SimplePager pager;
  private final VerticalPanel widgets = new VerticalPanel();
  private Button blockoutButton;
  private Toolbar tableControls;
  private VerticalPanel tablePanel;
  private IDialogCallback refreshCallBack = new IDialogCallback() {
    public void okPressed() {refresh(); }

    public void cancelPressed() {refresh(); }
  };

  public BlockoutPanel(final boolean isAdmin) {
    createUI(isAdmin);
    refresh();
  }

  @SuppressWarnings("EmptyCatchBlock")
  private void createUI(final boolean isAdmin) {
    widgets.setWidth("100%");
    createHeadlineBar();
    createControls(isAdmin);
    createTable();
    createPager();
    widgets.add(tablePanel);
    setWidget(widgets);
  }

  private void createHeadlineBar() {
    Toolbar bar = new Toolbar();
    bar.addSpacer(10);
    bar.add(new Label(Messages.getString("blockoutHeadline")));

    bar.setWidth("100%");
    widgets.add(bar);
  }

  private void createControls(final boolean isAdmin) {
    blockoutButton = new Button(Messages.getString("createBlockoutTime"));
    tableControls = new Toolbar();
    tablePanel = new VerticalPanel();
    tablePanel.setVisible(false);
    if (isAdmin) {
      final ClickHandler newBlockoutHandler = new ClickHandler() {
        @Override
        public void onClick(final ClickEvent clickEvent) {
          DialogBox blockoutDialog = new NewBlockoutScheduleDialog("", refreshCallBack, false, true);
          blockoutDialog.center();
        }
      };
      createBlockoutButton(newBlockoutHandler);
      createTableControls(newBlockoutHandler);
    }
  }

  private void createBlockoutButton(final ClickHandler newBlockoutHandler) {
    blockoutButton.addClickHandler(newBlockoutHandler);
    blockoutButton.setStyleName("pentaho-button");
    widgets.add(blockoutButton);
  }

  private void createTableControls(final ClickHandler newBlockoutHandler) {
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
    ToolbarButton editButton = new ToolbarButton(new Image(MantleImages.images.edit16()));
    editButton.setCommand(new Command() {
      @Override
      public void execute() {
        Set<JsJob> jobs = ((MultiSelectionModel<JsJob>) table.getSelectionModel()).getSelectedSet();

        JsJob jsJob = jobs.iterator().next();

        DialogBox blockoutDialog = new NewBlockoutScheduleDialog(jsJob, refreshCallBack, false, true, false);
        blockoutDialog.center();
      }
    });
    tableControls.add(editButton);
    ToolbarButton removeButton = new ToolbarButton(new Image(MantleImages.images.remove16()));
    removeButton.setCommand(new Command() {
      @Override
      public void execute() {
        Set<JsJob> selectedSet =
          ((MultiSelectionModel<JsJob>) table.getSelectionModel()).getSelectedSet();
        for (JsJob jsJob : selectedSet) {
          removeBlockout(jsJob);
        }
      }
    });
    tableControls.add(removeButton);
    tablePanel.add(tableControls);
  }

  private void createTable() {
    table.getElement().setId("blockout-table");
    table.setSelectionModel(new MultiSelectionModel<JsJob>());
    TextColumn<JsJob> startColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob block) {
        try {
          Date nextFireTime = block.getNextRun();
          return formatDate(nextFireTime);
        } catch (Throwable t) {
        }
        return "-";
      }
    };
    table.addColumn(startColumn, Messages.getString("blockoutColumnStarts"));
    table.addColumnStyleName(0, "backgroundContentHeaderTableCell");
    TextColumn<JsJob> endColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob block) {
        try {
          Date nextFireTime = block.getNextRun();
            return formatDate(
              new Date(nextFireTime.getTime() + block.getJobTrigger().getBlockDuration()));
        } catch (Throwable t) {
        }
        return "-";
      }
    };
    table.addColumn(endColumn, Messages.getString("blockoutColumnEnds"));
    table.addColumnStyleName(1, "backgroundContentHeaderTableCell");

    TextColumn<JsJob> repeatColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob block) {
        try {
          return block.getJobTrigger().getDescription();
        } catch (Throwable t) {
        }
        return "-";
      }
    };
    table.addColumn(repeatColumn, Messages.getString("blockoutColumnRepeats"));
    table.addColumnStyleName(2, "backgroundContentHeaderTableCell");

    TextColumn<JsJob> endByColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob block) {
        try {
          Date endTime = block.getJobTrigger().getEndTime();
          if(endTime == null) {
              return "Never";
          } else {
            return formatDate(endTime);
          }
        } catch (Throwable t) {
        }
        return "-";
      }
    };
    table.addColumn(endByColumn, Messages.getString("blockoutColumnRepeatsEndBy"));
    table.addColumnStyleName(3, "backgroundContentHeaderTableCell");
    tablePanel.add(table);
    dataProvider.addDataDisplay(table);
  }

  private void createPager() {
    SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
    pager = new SimplePager(SimplePager.TextLocation.CENTER, pagerResources, false, 0, true);
    pager.setDisplay(table);
    tablePanel.add(pager);
  }

  private String formatDate(final Date date) {
    DateTimeFormat simpleDateFormat = DateTimeFormat.getFormat("EEE, MMM dd h:mm a");
    return simpleDateFormat.format(date);
  }

  private void removeBlockout(final JsJob jsJob) {
    JSONObject jobRequest = new JSONObject();
    jobRequest.put("jobId", new JSONString(jsJob.getJobId())); //$NON-NLS-1$
    makeServiceCall("removeJob", RequestBuilder.DELETE, jobRequest.toString(), new RequestCallback() {

      public void onError(Request request, Throwable exception) {
        // todo: do something
      }

      public void onResponseReceived(Request request, Response response) {
        if (response.getStatusCode() == Response.SC_OK) {
          refresh();
        } else {
          // todo: do something
        }
      }
    });
  }

  public void refresh() {
    makeServiceCall("blockout/blockoutjobs", RequestBuilder.GET, null, new RequestCallback() {

      public void onError(Request request, Throwable exception) {
        //todo: do something
      }

      public void onResponseReceived(Request request, Response response) {
        if (response.getStatusCode() == Response.SC_OK) {
          if ("null".equals(response.getText())) {
            showData(null);
          } else {
            showData(parseJson(JsonUtils.escapeJsonForEval(response.getText())));
          }
        } else {
          //todo: do something
        }
      }
    });
  }

  private void makeServiceCall(
    final String urlSuffix, final RequestBuilder.Method httpMethod, final String requestData, final RequestCallback callback)
  {
    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
    final String url = contextURL + "api/scheduler/" + urlSuffix;
    RequestBuilder builder = new RequestBuilder(httpMethod, url);
    builder.setHeader("If-Modified-Since", "01 Jan 1970 00:00:00 GMT");
    builder.setHeader("Content-Type", "application/json");
    try {
      builder.sendRequest(requestData, callback);
    } catch (RequestException e) {
      // showError(e);
    }
  }

  private void showData(final JsArray<JsJob> allBlocks) {
    if (allBlocks == null || allBlocks.length() == 0) {
      tablePanel.setVisible(false);
      blockoutButton.setVisible(true);
    } else {
      tablePanel.setVisible(true);
      blockoutButton.setVisible(false);
      List<JsJob> jobList = new ArrayList<JsJob>();
      for (int i = 0; i < allBlocks.length(); i++) {
        JsJob job = allBlocks.get(i);
        jobList.add(job);
      }
      List<JsJob> list = dataProvider.getList();
      list.clear();
      list.addAll(jobList);
      pager.setVisible(jobList.size() > PAGE_SIZE);
      table.setVisible(jobList.size() > 0);
      table.redraw();
    }

  }

  private native JsArray<JsJob> parseJson(String json)
  /*-{
    var obj = eval('(' + json + ')');
    return obj.job;
  }-*/;
}
