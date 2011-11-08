package org.pentaho.mantle.client.workspace;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;

public class WorkspacePanel extends SimplePanel {

  private static WorkspacePanel instance = new WorkspacePanel();

  private WorkspacePanel() {
    MantleServiceCache.getService().isAdministrator(new AsyncCallback<Boolean>() {
      public void onSuccess(Boolean result) {
        refresh(result);
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

            CellTable<JsJob> table = new CellTable<JsJob>(10, (CellTableResources) GWT.create(CellTableResources.class));
            table.setSelectionModel(new MultiSelectionModel<JsJob>() {
            });

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
                    "<span title='" + new SafeHtmlBuilder().appendEscaped(job.getResourceName()).toSafeHtml().asString() + "'>" + val + "</span>").toSafeHtml();
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
            table.addColumn(resourceColumn, "File");
            table.addColumn(scheduleColumn, "Recurrence");
            table.addColumn(lastFireColumn, "Last Fire");
            table.addColumn(nextFireColumn, "Next Fire");
            if (isAdmin) {
              table.addColumn(userNameColumn, "User");
            }
            table.addColumn(stateColumn, "State");

            ListDataProvider<JsJob> dataProvider = new ListDataProvider<JsJob>();
            dataProvider.addDataDisplay(table);

            List<JsJob> list = dataProvider.getList();
            for (int i = 0; i < jobs.length(); i++) {
              list.add(jobs.get(i));
            }

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

            SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
            SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
            // pager.setRangeLimited(false);
            pager.setDisplay(table);

            VerticalPanel tableAndPager = new VerticalPanel();
            tableAndPager.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
            tableAndPager.add(table);
            tableAndPager.add(pager);

            // Add it to the root panel.
            setWidget(tableAndPager);
            getElement().getStyle().setBackgroundColor("white");

          } else {
            // showServerError(response);
          }
        }
      });
    } catch (RequestException e) {
      // showError(e);
    }
  }

  private final native JsArray<JsJob> parseJson(String json)
  /*-{
    var obj = eval('(' + json + ')')
    return obj.job;
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
