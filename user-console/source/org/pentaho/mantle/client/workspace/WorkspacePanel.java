package org.pentaho.mantle.client.workspace;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.mantle.client.commands.RefreshWorkspaceCommand;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.CellTable.Style;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

public class WorkspacePanel extends SimplePanel {

  private static WorkspacePanel instance = new WorkspacePanel();

  private ToolbarButton controlScheduleButton = new ToolbarButton(new Image(MantleImages.images.run16()));
  private ToolbarButton scheduleRemoveButton = new ToolbarButton(new Image(MantleImages.images.remove16()));

  private JsJob selectedJob = null;

  private CellTable<JsJob> table = new CellTable<JsJob>(20, (CellTableResources) GWT.create(CellTableResources.class));
  private ListDataProvider<JsJob> dataProvider = new ListDataProvider<JsJob>();

  private boolean isAdmin = false;

  private WorkspacePanel() {
    MantleServiceCache.getService().isAdministrator(new AsyncCallback<Boolean>() {
      public void onSuccess(Boolean isAdmin) {
        WorkspacePanel.this.isAdmin = isAdmin;
        createUI(isAdmin);
        refresh(isAdmin);
      }

      public void onFailure(Throwable caught) {
        refresh(false);
      }
    });
  }

  public static WorkspacePanel getInstance() {
    return instance;
  }

  public void refresh(final boolean isAdmin) {
    controlScheduleButton.setEnabled(false);
    scheduleRemoveButton.setEnabled(false);

    final String url = GWT.getHostPageBaseURL() + "api/scheduler/jobs"; //$NON-NLS-1$
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, url);
    executableTypesRequestBuilder.setHeader("accept", "application/json");
    try {
      executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          // showError(exception);
        }

        public void onResponseReceived(Request request, Response response) {

          if (response.getStatusCode() == Response.SC_OK) {
            JsArray<JsJob> jobs = parseJson(JsonUtils.escapeJsonForEval(response.getText()));
            List<JsJob> list = dataProvider.getList();
            list.clear();
            for (int i = 0; i < jobs.length(); i++) {
              list.add(jobs.get(i));
            }
            table.redraw();
          } else {
            // showServerError(response);
          }
        }
      });
    } catch (RequestException e) {
      // showError(e);
    }
  }

  private void updateControlSchedulerButtonState(final ToolbarButton controlSchedulerButton) {
    final String url = GWT.getHostPageBaseURL() + "api/scheduler/state"; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    try {
      builder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          // showError(exception);
        }

        public void onResponseReceived(Request request, Response response) {
          boolean isRunning = "RUNNING".equalsIgnoreCase(response.getText());
          if (isRunning) {
            controlSchedulerButton.setImage(new Image(MantleImages.images.stop_scheduler16()));
          } else {
            controlSchedulerButton.setImage(new Image(MantleImages.images.start_scheduler16()));
          }
          controlSchedulerButton.setToolTip(response.getText());
        }
      });
    } catch (RequestException e) {
      // showError(e);
    }
  }

  private void toggleSchedulerOnOff(final ToolbarButton controlSchedulerButton) {
    final String url = GWT.getHostPageBaseURL() + "api/scheduler/state"; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    try {
      builder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          // showError(exception);
        }

        public void onResponseReceived(Request request, Response response) {
          boolean isRunning = "RUNNING".equalsIgnoreCase(response.getText());
          if (isRunning) {
            controlScheduler(controlSchedulerButton, "pause");
          } else {
            controlScheduler(controlSchedulerButton, "start");
          }
        }
      });
    } catch (RequestException e) {
      // showError(e);
    }
  }

  private void createUI(boolean isAdmin) {

    table.addCellPreviewHandler(new CellPreviewEvent.Handler<JsJob>() {

      public void onCellPreview(CellPreviewEvent<JsJob> event) {
        
        if (event.getColumn() == 0 && event.getNativeEvent().getType().contains("click")) {
          Window.alert("Load execution history for: " + event.getValue().getResourceName() + " !");
        }
      }
    });

    table.setWidth("100%", true);

    final SingleSelectionModel<JsJob> selectionModel = new SingleSelectionModel<JsJob>();
    table.setSelectionModel(selectionModel);
    table.setEmptyTableWidget(new Label(Messages.getString("noSchedules")));

    TextColumn<JsJob> idColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob job) {
        return job.getJobId();
      }
    };
    idColumn.setSortable(true);

    TextColumn<JsJob> nameColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob job) {
        return job.getJobName();
      }
    };
    nameColumn.setSortable(true);

    Column<JsJob, SafeHtml> resourceColumn = new Column<JsJob, SafeHtml>(new SafeHtmlCell()) {
      public SafeHtml getValue(JsJob job) {
        String val = job.getResourceName();
        if (val.indexOf("/") != -1) {
          val = val.substring(val.lastIndexOf("/") + 1);
        }
        return new SafeHtmlBuilder().appendHtmlConstant(
            "<span style='cursor: hand; color: blue; text-decoration: underline' title='" + new SafeHtmlBuilder().appendEscaped(job.getResourceName()).toSafeHtml().asString() + "'>" + val + "</span>").toSafeHtml();
      }
    };
    resourceColumn.setSortable(true);

    TextColumn<JsJob> scheduleColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob job) {
        return job.getJobTrigger().getDescription();
      }
    };
    scheduleColumn.setSortable(false);

    TextColumn<JsJob> userNameColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob job) {
        return job.getUserName();
      }
    };
    userNameColumn.setSortable(true);

    TextColumn<JsJob> stateColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob job) {
        return job.getState();
      }
    };
    stateColumn.setSortable(true);

    TextColumn<JsJob> nextFireColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob job) {
        Date date = job.getNextRun();
        if (date == null) {
          return "-";
        }
        DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
        return format.format(date);
      }
    };
    nextFireColumn.setSortable(true);

    TextColumn<JsJob> lastFireColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob job) {
        Date date = job.getLastRun();
        if (date == null) {
          return "-";
        }
        DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
        return format.format(date);
      }
    };
    lastFireColumn.setSortable(true);

    // table.addColumn(idColumn, "ID");
    // table.addColumn(nameColumn, "Name");
    table.addColumn(resourceColumn, Messages.getString("file"));
    table.addColumn(scheduleColumn, Messages.getString("recurrence"));
    table.addColumn(lastFireColumn, Messages.getString("lastFire"));
    table.addColumn(nextFireColumn, Messages.getString("nextFire"));
    if (isAdmin) {
      table.addColumn(userNameColumn, Messages.getString("user"));
    }
    table.addColumn(stateColumn, Messages.getString("state"));

    table.setColumnWidth(resourceColumn, 260, Unit.PX);
    table.setColumnWidth(lastFireColumn, 150, Unit.PX);
    table.setColumnWidth(nextFireColumn, 150, Unit.PX);
    table.setColumnWidth(userNameColumn, 150, Unit.PX);
    table.setColumnWidth(stateColumn, 100, Unit.PX);

    dataProvider.addDataDisplay(table);
    List<JsJob> list = dataProvider.getList();

    ListHandler<JsJob> columnSortHandler = new ListHandler<JsJob>(list);

    columnSortHandler.setComparator(idColumn, new Comparator<JsJob>() {
      public int compare(JsJob o1, JsJob o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          return (o2 != null) ? o1.getJobId().compareTo(o2.getJobId()) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(nameColumn, new Comparator<JsJob>() {
      public int compare(JsJob o1, JsJob o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          return (o2 != null) ? o1.getJobName().compareTo(o2.getJobName()) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(resourceColumn, new Comparator<JsJob>() {
      public int compare(JsJob o1, JsJob o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          String r1 = o1.getResourceName();
          if (r1.indexOf("/") != -1) {
            r1 = r1.substring(r1.lastIndexOf("/") + 1);
          }
          String r2 = null;
          if (o2 != null) {
            r2 = o2.getResourceName();
            if (r2.indexOf("/") != -1) {
              r2 = r2.substring(r2.lastIndexOf("/") + 1);
            }
          }

          return (o2 != null) ? r1.compareTo(r2) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(userNameColumn, new Comparator<JsJob>() {
      public int compare(JsJob o1, JsJob o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          return (o2 != null) ? o1.getUserName().compareTo(o2.getUserName()) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(stateColumn, new Comparator<JsJob>() {
      public int compare(JsJob o1, JsJob o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          return (o2 != null) ? o1.getState().compareTo(o2.getState()) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(nextFireColumn, new Comparator<JsJob>() {
      public int compare(JsJob o1, JsJob o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          return (o2 != null) ? o1.getNextRun().compareTo(o2.getNextRun()) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(lastFireColumn, new Comparator<JsJob>() {
      public int compare(JsJob o1, JsJob o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 == null || o1.getLastRun() == null) {
          return -1;
        }
        if (o2 == null || o2.getLastRun() == null) {
          return 1;
        }

        if (o1.getLastRun() == o2.getLastRun()) {
          return 0;
        }

        return o1.getLastRun().compareTo(o2.getLastRun());
      }
    });
    table.addColumnSortHandler(columnSortHandler);

    table.getColumnSortList().push(idColumn);
    table.getColumnSortList().push(nameColumn);
    table.getColumnSortList().push(resourceColumn);

    table.getSelectionModel().addSelectionChangeHandler(new Handler() {
      @SuppressWarnings("unchecked")
      public void onSelectionChange(SelectionChangeEvent event) {
        selectedJob = ((SingleSelectionModel<JsJob>) table.getSelectionModel()).getSelectedObject();
        if ("NORMAL".equalsIgnoreCase(selectedJob.getState())) {
          controlScheduleButton.setImage(new Image(MantleImages.images.stop16()));
        } else {
          controlScheduleButton.setImage(new Image(MantleImages.images.run16()));
        }
        controlScheduleButton.setEnabled(selectedJob != null);
        controlScheduleButton.setToolTip(selectedJob.getState());
        scheduleRemoveButton.setEnabled(selectedJob != null);
      }
    });

    SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
    SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true) {
      @Override
      public void setPageStart(int index) {
        if (getDisplay() != null) {
          Range range = getDisplay().getVisibleRange();
          int pageSize = range.getLength();

          // Removed the min to show fixed ranges
          // if (isRangeLimited && display.isRowCountExact()) {
          // index = Math.min(index, display.getRowCount() - pageSize);
          // }

          index = Math.max(0, index);
          if (index != range.getStart()) {
            getDisplay().setVisibleRange(index, pageSize);
          }
        }
      }
    };
    pager.setDisplay(table);
    // pager.setRangeLimited(false);

    VerticalPanel tableAndPager = new VerticalPanel();
    tableAndPager.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    Toolbar bar = new Toolbar();
    bar.addSpacer(10);
    bar.add(new Label(Messages.getString("schedules")));
    bar.add(Toolbar.GLUE);

    ToolbarButton refresh = new ToolbarButton(new Image(MantleImages.images.refresh()));
    refresh.setCommand(new Command() {
      public void execute() {
        RefreshWorkspaceCommand cmd = new RefreshWorkspaceCommand();
        cmd.execute();
      }
    });
    bar.add(refresh);
    bar.addSpacer(20);
    controlScheduleButton.setCommand(new Command() {
      public void execute() {
        if (selectedJob != null) {
          if ("NORMAL".equals(selectedJob.getState())) {
            controlJob(selectedJob, "pauseJob", false);
          } else {
            controlJob(selectedJob, "resumeJob", false);
          }
        }
      }
    });
    controlScheduleButton.setEnabled(false);
    scheduleRemoveButton.setEnabled(false);
    bar.add(controlScheduleButton);
    bar.addSpacer(20);

    final ToolbarButton controlSchedulerButton = new ToolbarButton(new Image(MantleImages.images.start_scheduler16()));
    controlSchedulerButton.setCommand(new Command() {
      public void execute() {
        toggleSchedulerOnOff(controlSchedulerButton);
      }
    });
    updateControlSchedulerButtonState(controlSchedulerButton);

    bar.add(controlSchedulerButton);
    bar.addSpacer(20);

    ToolbarButton editButton = new ToolbarButton(new Image(MantleImages.images.edit16()));
    editButton.setEnabled(false);
    bar.add(editButton);

    scheduleRemoveButton.setCommand(new Command() {
      public void execute() {
        if (selectedJob != null) {
          controlJob(selectedJob, "removeJob", true);
        }
      }
    });
    bar.add(scheduleRemoveButton);
    bar.addSpacer(10);

    tableAndPager.add(bar);
    tableAndPager.add(table);
    tableAndPager.add(pager);

    // Add it to the root panel.
    setWidget(tableAndPager);
    getElement().getStyle().setBackgroundColor("white");
  }

  private void controlJob(final JsJob job, String function, final boolean refreshData) {
    final String url = GWT.getHostPageBaseURL() + "api/scheduler/" + function; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
    builder.setHeader("Content-Type", "application/json"); //$NON-NLS-1$//$NON-NLS-2$

    JSONObject startJobRequest = new JSONObject();
    startJobRequest.put("jobId", new JSONString(job.getJobId())); //$NON-NLS-1$

    try {
      builder.sendRequest(startJobRequest.toString(), new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          // showError(exception);
        }

        public void onResponseReceived(Request request, Response response) {
          job.setState(response.getText());
          table.redraw();
          boolean isRunning = "RUNNING".equalsIgnoreCase(response.getText());
          controlScheduleButton.setToolTip(job.getState());
          if (isRunning) {
            controlScheduleButton.setImage(new Image(MantleImages.images.stop16()));
          } else {
            controlScheduleButton.setImage(new Image(MantleImages.images.run16()));
          }
          if (refreshData) {
            refresh(isAdmin);
          }
        }
      });
    } catch (RequestException e) {
      // showError(e);
    }
  }

  private void controlScheduler(final ToolbarButton controlSchedulerButton, final String function) {
    final String url = GWT.getHostPageBaseURL() + "api/scheduler/" + function; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    try {
      builder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          // showError(exception);
        }

        public void onResponseReceived(Request request, Response response) {
          boolean isRunning = "RUNNING".equalsIgnoreCase(response.getText());
          controlSchedulerButton.setToolTip(response.getText());
          if (isRunning) {
            controlSchedulerButton.setImage(new Image(MantleImages.images.stop_scheduler16()));
          } else {
            controlSchedulerButton.setImage(new Image(MantleImages.images.start_scheduler16()));
          }
        }
      });
    } catch (RequestException e) {
      // showError(e);
    }
  }

  private final native JsArray<JsJob> parseJson(String json)
  /*-{
    var obj = eval('(' + json + ')');
    var arr = [];
    if (obj == null) {
      return arr;
    }
    if (obj.job.constructor.toString().indexOf("Array") == -1) {
      arr.push(obj.job);
    } else {
      arr = obj.job; 
    }
    return arr;
    //return obj.job;
  }-*/;

  public interface CellTableResources extends Resources {
    public interface CellTableStyle extends CellTable.Style {
    };

    /**
     * The styles used in this widget.
     */
    @Source("org/pentaho/mantle/client/workspace/CellTable.css")
    public Style cellTableStyle();
  }

}
