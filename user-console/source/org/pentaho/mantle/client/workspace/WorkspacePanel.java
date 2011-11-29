package org.pentaho.mantle.client.workspace;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.commands.RefreshWorkspaceCommand;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.fileproperties.GeneratedContentPanel;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.resources.client.ImageResource;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public class WorkspacePanel extends SimplePanel {

  private static final int PAGE_SIZE = 25;

  private static WorkspacePanel instance = new WorkspacePanel();

  private ToolbarButton controlScheduleButton = new ToolbarButton(new Image(MantleImages.images.run16()));
  private ToolbarButton triggerNowButton = new ToolbarButton(new Image(MantleImages.images.execute16()));
  private ToolbarButton scheduleRemoveButton = new ToolbarButton(new Image(MantleImages.images.remove16()));
  private ToolbarButton filterButton = new ToolbarButton(new Image(MantleImages.images.filter16()));
  private ToolbarButton filterRemoveButton = new ToolbarButton(new Image(MantleImages.images.filterRemove16()));

  private JsArray<JsJob> allJobs;
  private Set<JsJob> selectedJobs = null;
  private ArrayList<IJobFilter> filters = new ArrayList<IJobFilter>();

  private CellTable<JsJob> table = new CellTable<JsJob>(PAGE_SIZE, (CellTableResources) GWT.create(CellTableResources.class));
  private ListDataProvider<JsJob> dataProvider = new ListDataProvider<JsJob>();
  private SimplePager pager;

  private FilterDialog filterDialog;
  private IDialogCallback filterDialogCallback = new IDialogCallback() {
    public void okPressed() {
      filters.clear();
      // create filters
      if (filterDialog.getAfterDate() != null) {
        filters.add(new IJobFilter() {
          public boolean accept(JsJob job) {
            return job.getNextRun().after(filterDialog.getAfterDate());
          }
        });
      }
      if (filterDialog.getBeforeDate() != null) {
        filters.add(new IJobFilter() {
          public boolean accept(JsJob job) {
            return job.getNextRun().before(filterDialog.getBeforeDate());
          }
        });
      }
      if (!StringUtils.isEmpty(filterDialog.getResourceName())) {
        filters.add(new IJobFilter() {
          public boolean accept(JsJob job) {
            return job.getShortResourceName().toLowerCase().contains(filterDialog.getResourceName().toLowerCase());
          }
        });
      }
      if (!StringUtils.isEmpty(filterDialog.getUserFilter()) && !filterDialog.getUserFilter().equals("ALL")) {
        filters.add(new IJobFilter() {
          public boolean accept(JsJob job) {
            return job.getUserName().equalsIgnoreCase(filterDialog.getUserFilter());
          }
        });
      }
      if (!StringUtils.isEmpty(filterDialog.getStateFilter()) && !filterDialog.getStateFilter().equals("ALL")) {
        filters.add(new IJobFilter() {
          public boolean accept(JsJob job) {
            return job.getState().toLowerCase().equalsIgnoreCase(filterDialog.getStateFilter());
          }
        });
      }
      if (!StringUtils.isEmpty(filterDialog.getTypeFilter()) && !filterDialog.getTypeFilter().equals("ALL")) {
        filters.add(new IJobFilter() {
          public boolean accept(JsJob job) {
            return job.getJobTrigger().getScheduleType().equalsIgnoreCase(filterDialog.getTypeFilter());
          }
        });
      }
      if (filters.size() > 0) {
        filterButton.setImage(new Image(MantleImages.images.filterActive16()));
        filterRemoveButton.setEnabled(true);
      } else {
        filterButton.setImage(new Image(MantleImages.images.filter16()));
        filterRemoveButton.setEnabled(false);
      }
      filterAndShowData();
    }

    public void cancelPressed() {
    }
  };

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

  public void refresh() {
    refresh(isAdmin);
  }

  public void refresh(final boolean isAdmin) {
    controlScheduleButton.setEnabled(false);
    triggerNowButton.setEnabled(false);
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
            allJobs = parseJson(JsonUtils.escapeJsonForEval(response.getText()));
            filterAndShowData();
          } else {
            // showServerError(response);
          }
        }
      });
    } catch (RequestException e) {
      // showError(e);
    }
  }

  private void filterAndShowData() {
    ArrayList<JsJob> filteredList = new ArrayList<JsJob>();
    for (int i = 0; i < allJobs.length(); i++) {
      filteredList.add(allJobs.get(i));
      // filter if needed
      for (IJobFilter filter : filters) {
        if (!filter.accept(allJobs.get(i))) {
          filteredList.remove(allJobs.get(i));
        }
      }
    }
    List<JsJob> list = dataProvider.getList();
    list.clear();
    list.addAll(filteredList);
    pager.setVisible(filteredList.size() > PAGE_SIZE);
    table.redraw();
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
            controlSchedulerButton.setToolTip(Messages.getString("stopScheduler"));
            controlSchedulerButton.setImage(new Image(MantleImages.images.stop_scheduler16()));
          } else {
            controlSchedulerButton.setToolTip(Messages.getString("startScheduler"));
            controlSchedulerButton.setImage(new Image(MantleImages.images.start_scheduler16()));
          }
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

    table.getElement().setId("schedule-table");

    table.addCellPreviewHandler(new CellPreviewEvent.Handler<JsJob>() {

      public void onCellPreview(CellPreviewEvent<JsJob> event) {

        if (event.getNativeEvent().getType().contains("click") && event.getColumn() == 0 && event.getValue().hasResourceName()) {
          PromptDialogBox dialog = new PromptDialogBox(Messages.getString("history"), Messages.getString("ok"), null, false, false);
          String resource = event.getValue().getFullResourceName();
          resource = resource.replace("/", ":");
          dialog.setContent(new GeneratedContentPanel(resource));
          dialog.setSize("600px", "300px");
          dialog.center();
        }
      }
    });

    table.setWidth("100%", true);

    // final SingleSelectionModel<JsJob> selectionModel = new SingleSelectionModel<JsJob>();
    final MultiSelectionModel<JsJob> selectionModel = new MultiSelectionModel<JsJob>(new ProvidesKey<JsJob>() {
      public Object getKey(JsJob item) {
        return item.getJobId();
      }
    });
    table.setSelectionModel(selectionModel);
    Label noDataLabel = new Label(Messages.getString("noSchedules"));
    noDataLabel.setStyleName("noDataForScheduleTable");
    table.setEmptyTableWidget(noDataLabel);

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
        try {
          if (!job.hasResourceName()) {
            String val = job.getShortResourceName();
            return new SafeHtmlBuilder().appendHtmlConstant(
                "<span title='" + new SafeHtmlBuilder().appendEscaped(job.getFullResourceName()).toSafeHtml().asString() + "'>" + val + "</span>").toSafeHtml();
          } else {
            String val = job.getShortResourceName();
            return new SafeHtmlBuilder().appendHtmlConstant(
                "<span class='workspace-resource-link' title='" + new SafeHtmlBuilder().appendEscaped(job.getFullResourceName()).toSafeHtml().asString() + "'>"
                    + val + "</span>").toSafeHtml();
          }
        } catch (Throwable t) {
        }
        return new SafeHtmlBuilder().appendHtmlConstant("-").toSafeHtml();
      }
    };
    resourceColumn.setSortable(true);

    TextColumn<JsJob> scheduleColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob job) {
        try {
          return job.getJobTrigger().getDescription();
        } catch (Throwable t) {
        }
        return "-";
      }
    };
    scheduleColumn.setSortable(true);

    TextColumn<JsJob> userNameColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob job) {
        try {
          return job.getUserName();
        } catch (Throwable t) {
        }
        return "-";
      }
    };
    userNameColumn.setSortable(true);

    TextColumn<JsJob> stateColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob job) {
        try {
          return job.getState();
        } catch (Throwable t) {
        }
        return "-";
      }
    };
    stateColumn.setSortable(true);

    TextColumn<JsJob> nextFireColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob job) {
        try {
          Date date = job.getNextRun();
          if (date == null) {
            return "-";
          }
          DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
          return format.format(date);
        } catch (Throwable t) {
        }
        return "-";
      }
    };
    nextFireColumn.setSortable(true);

    TextColumn<JsJob> lastFireColumn = new TextColumn<JsJob>() {
      public String getValue(JsJob job) {
        try {
          Date date = job.getLastRun();
          if (date == null) {
            return "-";
          }
          DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
          return format.format(date);
        } catch (Throwable t) {
        }
        return "-";
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

    table.addColumnStyleName(0, "backgroundContentHeaderTableCell");
    table.addColumnStyleName(1, "backgroundContentHeaderTableCell");
    table.addColumnStyleName(2, "backgroundContentHeaderTableCell");
    table.addColumnStyleName(3, "backgroundContentHeaderTableCell");
    table.addColumnStyleName(4, "backgroundContentHeaderTableCell");
    if (isAdmin) {
      table.addColumnStyleName(5, "backgroundContentHeaderTableCell");
    }

    table.setColumnWidth(resourceColumn, 220, Unit.PX);
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
          String r1 = o1.getShortResourceName();
          String r2 = null;
          if (o2 != null) {
            r2 = o2.getShortResourceName();
          }

          return (o2 != null) ? r1.compareTo(r2) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(scheduleColumn, new Comparator<JsJob>() {
      public int compare(JsJob o1, JsJob o2) {
        String s1 = o1.getJobTrigger().getDescription();
        String s2 = o2.getJobTrigger().getDescription();
        return s1.compareTo(s2);
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

        if (o1 == null || o1.getNextRun() == null) {
          return -1;
        }
        if (o2 == null || o2.getNextRun() == null) {
          return 1;
        }

        if (o1.getNextRun() == o2.getNextRun()) {
          return 0;
        }

        return o1.getNextRun().compareTo(o2.getNextRun());
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
        selectedJobs = ((MultiSelectionModel<JsJob>) table.getSelectionModel()).getSelectedSet();
        JsJob[] jobs = (JsJob[]) selectedJobs.toArray(new JsJob[] {});
        if ("NORMAL".equalsIgnoreCase(jobs[0].getState())) {
          controlScheduleButton.setImage(new Image(MantleImages.images.stop16()));
        } else {
          controlScheduleButton.setImage(new Image(MantleImages.images.run16()));
        }
        controlScheduleButton.setEnabled(jobs != null);

        boolean isRunning = "NORMAL".equalsIgnoreCase(jobs[0].getState());
        controlScheduleButton.setToolTip(isRunning ? Messages.getString("stop") : Messages.getString("start"));
        scheduleRemoveButton.setEnabled(jobs != null);
        triggerNowButton.setEnabled(jobs != null);
      }
    });

    SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
    pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true) {
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

    filterButton.setCommand(new Command() {
      public void execute() {
        if (filterDialog == null) {
          filterDialog = new FilterDialog(allJobs, filterDialogCallback);
        } else {
          filterDialog.initUI(allJobs);
        }
        filterDialog.center();
      }
    });
    filterButton.setToolTip(Messages.getString("filterSchedules"));
    bar.add(filterButton);
    
    filterRemoveButton.setCommand(new Command() {
      public void execute() {
        filterDialog = null;
        filters.clear();
        filterAndShowData();
        filterRemoveButton.setEnabled(false);
        filterButton.setImage(new Image(MantleImages.images.filter16()));
      }
    });
    filterRemoveButton.setToolTip(Messages.getString("removeFilters"));
    filterRemoveButton.setEnabled(filters.size() > 0);
    bar.add(filterRemoveButton);

    bar.addSpacer(20);
    triggerNowButton.setToolTip(Messages.getString("executeNow"));
    triggerNowButton.setCommand(new Command() {
      public void execute() {
        if (selectedJobs != null) {
          controlJobs(selectedJobs, "triggerNow", RequestBuilder.POST, false);
        }
      }
    });
    bar.add(triggerNowButton);
    controlScheduleButton.setCommand(new Command() {
      public void execute() {
        if (selectedJobs != null) {
          JsJob[] jobs = (JsJob[]) selectedJobs.toArray(new JsJob[] {});
          if ("NORMAL".equals(jobs[0].getState())) {
            controlJobs(selectedJobs, "pauseJob", RequestBuilder.POST, false);
          } else {
            controlJobs(selectedJobs, "resumeJob", RequestBuilder.POST, false);
          }
        }
      }
    });
    controlScheduleButton.setEnabled(false);
    scheduleRemoveButton.setEnabled(false);
    triggerNowButton.setEnabled(false);
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
        if (selectedJobs != null) {
          final PromptDialogBox prompt = new PromptDialogBox(Messages.getString("warning"), Messages.getString("yes"), Messages.getString("no"), false, true);
          prompt.setContent(new Label(Messages.getString("deleteConfirmSchedles", "" + selectedJobs.size())));

          prompt.setCallback(new IDialogCallback() {
            public void okPressed() {
              controlJobs(selectedJobs, "removeJob", RequestBuilder.DELETE, true);
              prompt.hide();
            }

            public void cancelPressed() {
              prompt.hide();
            }
          });
          prompt.center();
        }
      }
    });
    scheduleRemoveButton.setToolTip(Messages.getString("remove"));
    bar.add(scheduleRemoveButton);
    bar.addSpacer(10);

    tableAndPager.add(bar);
    tableAndPager.add(table);
    tableAndPager.add(pager);

    // Add it to the root panel.
    setWidget(tableAndPager);
    getElement().getStyle().setBackgroundColor("white");
  }

  private void controlJobs(final Set<JsJob> jobs, String function, final Method method, final boolean refreshData) {
    for (final JsJob job : jobs) {
      final String url = GWT.getHostPageBaseURL() + "api/scheduler/" + function; //$NON-NLS-1$
      RequestBuilder builder = new RequestBuilder(method, url);
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
            boolean isRunning = "NORMAL".equalsIgnoreCase(response.getText());
            if (isRunning) {
              controlScheduleButton.setToolTip(Messages.getString("stop"));
              controlScheduleButton.setImage(new Image(MantleImages.images.stop16()));
            } else {
              controlScheduleButton.setToolTip(Messages.getString("start"));
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
  }

  private void controlScheduler(final ToolbarButton controlSchedulerButton, final String function) {
    final String url = GWT.getHostPageBaseURL() + "api/scheduler/" + function; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
    try {
      builder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          // showError(exception);
        }

        public void onResponseReceived(Request request, Response response) {
          boolean isRunning = "RUNNING".equalsIgnoreCase(response.getText());
          if (isRunning) {
            controlSchedulerButton.setToolTip(Messages.getString("stopScheduler"));
            controlSchedulerButton.setImage(new Image(MantleImages.images.stop_scheduler16()));
          } else {
            controlSchedulerButton.setToolTip(Messages.getString("startScheduler"));
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

    @Override
    public ImageResource cellTableSortAscending();

    @Override
    public ImageResource cellTableSortDescending();

    /**
     * The styles used in this widget.
     */
    @Source("org/pentaho/mantle/client/workspace/CellTable.css")
    public Style cellTableStyle();
  }

}
