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
import java.util.List;

import static org.pentaho.mantle.client.workspace.WorkspacePanel.CellTableResources;
import static org.pentaho.mantle.client.workspace.WorkspacePanel.PAGE_SIZE;

public class BlockoutPanel extends SimplePanel {
    private CellTable<JsBlock> table =
        new CellTable<JsBlock>(PAGE_SIZE, (CellTableResources) GWT.create(CellTableResources.class));
    private ListDataProvider<JsBlock> dataProvider = new ListDataProvider<JsBlock>();
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

        table.setSelectionModel(new MultiSelectionModel<JsBlock>());
        Label noDataLabel = new Label(Messages.getString("noBlockouts"));
        noDataLabel.setStyleName("noDataForScheduleTable");
        table.setEmptyTableWidget(noDataLabel);
        TextColumn<JsBlock> startColumn = new TextColumn<JsBlock>() {
            public String getValue(JsBlock block) {
                try {
                    return block.getStartTime().toString();
                } catch (Throwable t) {
                }
                return "-";
            }
        };
        table.addColumn(startColumn, "Starts");  //todo:resource
        table.addColumnStyleName(0, "backgroundContentHeaderTableCell");
        TextColumn<JsBlock> endColumn = new TextColumn<JsBlock>() {
            public String getValue(JsBlock block) {
                try {
                    return Integer.toString(block.getBlockDuration());
                } catch (Throwable t) {
                }
                return "-";
            }
        };
        table.addColumn(endColumn, "Ends");  //todo: resource
        table.addColumnStyleName(1, "backgroundContentHeaderTableCell");

        TextColumn<JsBlock> repeatColumn = new TextColumn<JsBlock>() {
            public String getValue(JsBlock block) {
                try {
                    return Integer.toString(block.getRepeatCount());
                } catch (Throwable t) {
                }
                return "-";
            }
        };
        table.addColumn(repeatColumn, "Repeats");//todo: resource
        table.addColumnStyleName(2, "backgroundContentHeaderTableCell");
        Toolbar bar = new Toolbar();
        bar.addSpacer(10);
        bar.add(new Label("Blockout Times - All schedules will be blocked out during the following times:"));      //todo: resource

        bar.setWidth("100%");
        widgets.add(bar);
        blockoutButton = new Button("Create Blockout Time");   //todo: resource
        tableControls = new Toolbar();
        tablePanel = new VerticalPanel();

        if (isAdmin) {
            blockoutButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent clickEvent) {
                    DialogBox blockoutDialog = new NewBlockoutScheduleDialog("", blockoutDialogCallback, false, true);
                    blockoutDialog.center();
                }
            });
            blockoutButton.setStyleName("pentaho-button");
            widgets.add(blockoutButton);

            tableControls.addSpacer(10);
            tableControls.add(Toolbar.GLUE);
            tableControls.add(new ToolbarButton(new Image(MantleImages.images.add_icon())));
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
                            JsArray<JsBlock> allBlocks = parseJson(json);
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

    private void showData(final JsArray<JsBlock> allBlocks) {
        if(allBlocks == null || allBlocks.length() == 0) {
            table.setVisible(false);
            tableControls.setVisible(false);
            pager.setVisible(false);
            blockoutButton.setVisible(true);
        } else {
            table.setVisible(true);
            tableControls.setVisible(true);
            pager.setVisible(true);
            blockoutButton.setVisible(false);
            List<JsBlock> filteredList = new ArrayList<JsBlock>();
            for (int i = 0; i < allBlocks.length(); i++) {
                filteredList.add(allBlocks.get(i));
            }
            List<JsBlock> list = dataProvider.getList();
            list.clear();
            list.addAll(filteredList);
            pager.setVisible(filteredList.size() > PAGE_SIZE);
            table.setVisible(filteredList.size() > 0);
            table.redraw();
        }

    }

    private native JsArray<JsBlock> parseJson(String json)
  /*-{
    var obj = eval('(' + json + ')');
    return obj.simpleBlockoutTrigger;
  }-*/;
}
