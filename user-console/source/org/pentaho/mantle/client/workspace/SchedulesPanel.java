/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.client.workspace;

import static org.pentaho.mantle.client.workspace.SchedulesPerspectivePanel.PAGE_SIZE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.commands.RefreshSchedulesCommand;
import org.pentaho.mantle.client.dialogs.scheduling.NewScheduleDialog;
import org.pentaho.mantle.client.dialogs.scheduling.OutputLocationUtils;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.GenericEvent;
import org.pentaho.mantle.client.images.ImageUtil;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.ui.PerspectiveManager;
import org.pentaho.mantle.client.workspace.SchedulesPerspectivePanel.CellTableResources;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractHeaderOrFooterBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
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

public class SchedulesPanel extends SimplePanel {

  private static final int READ_PERMISSION = 0;

  private ToolbarButton controlScheduleButton = new ToolbarButton( ImageUtil.getThemeableImage( "icon-small",
      "icon-run" ) );
  private ToolbarButton editButton = new ToolbarButton( ImageUtil.getThemeableImage( "pentaho-editbutton" ) );
  private ToolbarButton triggerNowButton = new ToolbarButton( ImageUtil
      .getThemeableImage( "icon-small", "icon-execute" ) );
  private ToolbarButton scheduleRemoveButton =
      new ToolbarButton( ImageUtil.getThemeableImage( "pentaho-deletebutton" ) );
  private ToolbarButton filterButton =
      new ToolbarButton( ImageUtil.getThemeableImage( "icon-small", "icon-filter-add" ) );
  private ToolbarButton filterRemoveButton = new ToolbarButton( ImageUtil.getThemeableImage( "icon-small",
      "icon-filter-remove" ) );

  private JsArray<JsJob> allJobs;

  private ArrayList<IJobFilter> filters = new ArrayList<IJobFilter>();

  private CellTable<JsJob> table = new CellTable<JsJob>( PAGE_SIZE, (CellTableResources) GWT
      .create( CellTableResources.class ) );

  private ListDataProvider<JsJob> dataProvider = new ListDataProvider<JsJob>();

  private SimplePager pager;

  private FilterDialog filterDialog;

  private IDialogCallback filterDialogCallback = new IDialogCallback() {
    public void okPressed() {
      filters.clear();
      // create filters
      if ( filterDialog.getAfterDate() != null ) {
        filters.add( new IJobFilter() {
          public boolean accept( JsJob job ) {
            return job.getNextRun().after( filterDialog.getAfterDate() );
          }
        } );
      }
      if ( filterDialog.getBeforeDate() != null ) {
        filters.add( new IJobFilter() {
          public boolean accept( JsJob job ) {
            return job.getNextRun().before( filterDialog.getBeforeDate() );
          }
        } );
      }
      if ( !StringUtils.isEmpty( filterDialog.getResourceName() ) ) {
        filters.add( new IJobFilter() {
          public boolean accept( JsJob job ) {
            return job.getShortResourceName().toLowerCase().contains( filterDialog.getResourceName().toLowerCase() );
          }
        } );
      }
      final String showAll = Messages.getString( "showAll" );
      if ( !StringUtils.isEmpty( filterDialog.getUserFilter() ) && !filterDialog.getUserFilter().equals( showAll ) ) {
        filters.add( new IJobFilter() {
          public boolean accept( JsJob job ) {
            return job.getUserName().equalsIgnoreCase( filterDialog.getUserFilter() );
          }
        } );
      }
      if ( !StringUtils.isEmpty( filterDialog.getStateFilter() ) && !filterDialog.getStateFilter().equals( showAll ) ) {
        filters.add( new IJobFilter() {
          public boolean accept( JsJob job ) {
            return job.getState().toLowerCase().equalsIgnoreCase( filterDialog.getStateFilter() );
          }
        } );
      }
      if ( !StringUtils.isEmpty( filterDialog.getTypeFilter() ) && !filterDialog.getTypeFilter().equals( showAll ) ) {
        filters.add( new IJobFilter() {
          public boolean accept( JsJob job ) {
            return job.getJobTrigger().getScheduleType().equalsIgnoreCase( filterDialog.getTypeFilter() );
          }
        } );
      }
      filterRemoveButton.setEnabled( filters.size() > 0 );
      filterAndShowData();
    }

    public void cancelPressed() {
    }
  };

  @SuppressWarnings ( "unchecked" )
  private Set<JsJob> getSelectedJobs() {
    Set<JsJob> selectedJobs = ( (MultiSelectionModel<JsJob>) table.getSelectionModel() ).getSelectedSet();
    return selectedJobs;
  }

  private IDialogCallback scheduleDialogCallback = new IDialogCallback() {
    public void okPressed() {
      // Remove Next Line to disable deletion of old job
      controlJobs( getSelectedJobs(), "removeJob", RequestBuilder.DELETE, true );
      refresh();
    }

    public void cancelPressed() {
    }
  };

  public SchedulesPanel( final boolean isAdmin, final boolean isScheduler ) {
    createUI( isAdmin, isScheduler );
    refresh();
  }

  public void refresh() {
    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );
    final String url = contextURL + "api/scheduler/jobs"; //$NON-NLS-1$
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder( RequestBuilder.GET, url );
    executableTypesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    executableTypesRequestBuilder.setHeader( "accept", "application/json" );
    try {
      executableTypesRequestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == Response.SC_OK ) {
            allJobs = parseJson( JsonUtils.escapeJsonForEval( response.getText() ) );
            filterAndShowData();
          }
        }
      } );
    } catch ( RequestException e ) {
      // showError(e);
    }
  }

  private void filterAndShowData() {

    filters.add( new IJobFilter() {
      public boolean accept( JsJob job ) {
        return !job.getFullResourceName().equals( "GeneratedContentCleaner" );
      }
    } );

    ArrayList<JsJob> filteredList = new ArrayList<JsJob>();
    for ( int i = 0; i < allJobs.length(); i++ ) {
      filteredList.add( allJobs.get( i ) );
      // filter if needed
      for ( IJobFilter filter : filters ) {
        if ( !filter.accept( allJobs.get( i ) ) ) {
          filteredList.remove( allJobs.get( i ) );
        }
      }
    }
    List<JsJob> list = dataProvider.getList();
    list.clear();
    list.addAll( filteredList );
    pager.setVisible( filteredList.size() > PAGE_SIZE );
    for ( JsJob job : filteredList ) {
      table.getSelectionModel().setSelected( job, false );
    }
    editButton.setEnabled( false );
    controlScheduleButton.setEnabled( false );
    scheduleRemoveButton.setEnabled( false );
    triggerNowButton.setEnabled( false );
    table.setPageStart( 0 );
    table.redraw();
  }

  private void
  updateControlSchedulerButtonState( final ToolbarButton controlSchedulerButton, final boolean isScheduler ) {
    final String url = GWT.getHostPageBaseURL() + "api/scheduler/state"; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
    builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      builder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
          boolean isRunning = "RUNNING".equalsIgnoreCase( response.getText() );
          if ( isRunning ) {
            controlSchedulerButton.setToolTip( Messages.getString( "stopScheduler" ) );
            controlSchedulerButton.setImage( ImageUtil.getThemeableImage( "icon-small", "icon-stop-scheduler" ) );
          } else {
            controlSchedulerButton.setToolTip( Messages.getString( "startScheduler" ) );
            controlSchedulerButton.setImage( ImageUtil.getThemeableImage( "icon-small", "icon-start-scheduler" ) );
          }

          if ( !isScheduler ) {
            controlSchedulerButton.setEnabled( false );
          } else {
            controlSchedulerButton.setEnabled( true );
          }
        }
      } );
    } catch ( RequestException e ) {
      // showError(e);
    }
  }

  private void toggleSchedulerOnOff( final ToolbarButton controlSchedulerButton, final boolean isScheduler ) {
    final String url = GWT.getHostPageBaseURL() + "api/scheduler/state"; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
    builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      builder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
          boolean isRunning = "RUNNING".equalsIgnoreCase( response.getText() );
          if ( isRunning ) {
            controlScheduler( controlSchedulerButton, "pause", isScheduler );
          } else {
            controlScheduler( controlSchedulerButton, "start", isScheduler );
          }
        }
      } );
    } catch ( RequestException e ) {
      // showError(e);
    }
  }

  private void createUI( boolean isAdmin, final boolean isScheduler ) {

    table.getElement().setId( "schedule-table" );
    table.setStylePrimaryName( "pentaho-table" );
    table.setWidth( "100%", true );

    // BISERVER-9331 Column sort indicators should be to the right of header text in the Manage Schedules table.
    if ( table.getHeaderBuilder() instanceof AbstractHeaderOrFooterBuilder ) {
      ( (AbstractHeaderOrFooterBuilder<JsJob>) table.getHeaderBuilder() ).setSortIconStartOfLine( false );
    }

    final MultiSelectionModel<JsJob> selectionModel = new MultiSelectionModel<JsJob>( new ProvidesKey<JsJob>() {
      public Object getKey( JsJob item ) {
        return item.getJobId();
      }
    } );
    table.setSelectionModel( selectionModel );

    Label noDataLabel = new Label( Messages.getString( "noSchedules" ) );
    noDataLabel.setStyleName( "noDataForScheduleTable" );
    table.setEmptyTableWidget( noDataLabel );

    TextColumn<JsJob> idColumn = new TextColumn<JsJob>() {
      public String getValue( JsJob job ) {
        return job.getJobId();
      }
    };
    idColumn.setSortable( true );

    TextColumn<JsJob> nameColumn = new TextColumn<JsJob>() {
      public String getValue( JsJob job ) {
        return job.getJobName();
      }
    };
    nameColumn.setSortable( true );

    TextColumn<JsJob> resourceColumn = new TextColumn<JsJob>() {
      public String getValue( JsJob job ) {
        if ( job.getFullResourceName().contains( "." ) ) {
          return job.getFullResourceName().substring( 0, job.getFullResourceName().lastIndexOf( "." ) );
        } else {
          return job.getFullResourceName();
        }
      }
    };
    resourceColumn.setSortable( true );

    Column<JsJob, SafeHtml> outputPathColumn = new Column<JsJob, SafeHtml>( new ClickableSafeHtmlCell() ) {
      @Override
      public SafeHtml getValue( JsJob jsJob ) {
        try {
          String outputPath = jsJob.getOutputPath();
          if ( StringUtils.isEmpty( outputPath ) ) {
            return new SafeHtmlBuilder().appendHtmlConstant( "-" ).toSafeHtml();
          } else {
            return new SafeHtmlBuilder().appendHtmlConstant(
                "<span class='workspace-resource-link' title='"
                    + new SafeHtmlBuilder().appendEscaped( outputPath ).toSafeHtml().asString() + "'>" + outputPath
                    + "</span>"
            ).toSafeHtml();
          }
        } catch ( Throwable t ) {
          return new SafeHtmlBuilder().appendHtmlConstant( "-" ).toSafeHtml();
        }
      }
    };

    outputPathColumn.setFieldUpdater( new FieldUpdater<JsJob, SafeHtml>() {
      @Override
      public void update( final int index, final JsJob jsJob, final SafeHtml value ) {
        if ( !value.equals( "-" ) ) {

          final Command errorCallback = new Command() {
            @Override
            public void execute() {
              showValidateOutputLocationError();
            }
          };

          final Command successCallback = new Command() {
            @Override
            public void execute() {
              openOutputLocation( jsJob.getOutputPath() );
            }
          };

          OutputLocationUtils.validateOutputLocation( jsJob.getOutputPath(), successCallback, errorCallback );
        }
      }
    } );

    outputPathColumn.setSortable( true );

    TextColumn<JsJob> scheduleColumn = new TextColumn<JsJob>() {
      public String getValue( JsJob job ) {
        try {
          return job.getJobTrigger().getDescription();
        } catch ( Throwable t ) {
          return "-";
        }
      }
    };
    scheduleColumn.setSortable( true );

    TextColumn<JsJob> userNameColumn = new TextColumn<JsJob>() {
      public String getValue( JsJob job ) {
        try {
          return job.getUserName();
        } catch ( Throwable t ) {
          return "-";
        }
      }
    };
    userNameColumn.setSortable( true );

    TextColumn<JsJob> stateColumn = new TextColumn<JsJob>() {
      public String getValue( JsJob job ) {
        try {
          // BISERVER-9965
          final String jobState = "COMPLETE".equalsIgnoreCase( job.getState() ) ? "FINISHED" : job.getState();
          // not css text-transform because tooltip will use pure text from the cell
          return jobState.substring( 0, 1 ).toUpperCase() + jobState.substring( 1 ).toLowerCase();
        } catch ( Throwable t ) {
          return "-";
        }
      }
    };
    stateColumn.setSortable( true );

    TextColumn<JsJob> nextFireColumn = new TextColumn<JsJob>() {
      public String getValue( JsJob job ) {
        try {
          Date date = job.getNextRun();
          if ( date == null ) {
            return "-";
          }
          DateTimeFormat format = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_MEDIUM );
          return format.format( date );
        } catch ( Throwable t ) {
          return "-";
        }
      }
    };
    nextFireColumn.setSortable( true );

    TextColumn<JsJob> lastFireColumn = new TextColumn<JsJob>() {
      public String getValue( JsJob job ) {
        try {
          Date date = job.getLastRun();
          if ( date == null ) {
            return "-";
          }
          DateTimeFormat format = DateTimeFormat.getFormat( PredefinedFormat.DATE_TIME_MEDIUM );
          return format.format( date );
        } catch ( Throwable t ) {
          return "-";
        }
      }
    };
    lastFireColumn.setSortable( true );

    // table.addColumn(idColumn, "ID");
    table.addColumn( nameColumn, Messages.getString( "scheduleName" ) );
    table.addColumn( scheduleColumn, Messages.getString( "recurrence" ) );
    table.addColumn( resourceColumn, Messages.getString( "sourceFile" ) );
    table.addColumn( outputPathColumn, Messages.getString( "outputPath" ) );

    table.addColumn( lastFireColumn, Messages.getString( "lastFire" ) );
    table.addColumn( nextFireColumn, Messages.getString( "nextFire" ) );
    if ( isAdmin ) {
      table.addColumn( userNameColumn, Messages.getString( "user" ) );
    }
    table.addColumn( stateColumn, Messages.getString( "state" ) );

    table.addColumnStyleName( 0, "backgroundContentHeaderTableCell" );
    table.addColumnStyleName( 1, "backgroundContentHeaderTableCell" );
    table.addColumnStyleName( 2, "backgroundContentHeaderTableCell" );
    table.addColumnStyleName( 3, "backgroundContentHeaderTableCell" );
    table.addColumnStyleName( 4, "backgroundContentHeaderTableCell" );
    table.addColumnStyleName( 5, "backgroundContentHeaderTableCell" );
    if ( isAdmin ) {
      table.addColumnStyleName( 6, "backgroundContentHeaderTableCell" );
    }
    table.addColumnStyleName( isAdmin ? 7 : 6, "backgroundContentHeaderTableCell" );

    table.setColumnWidth( nameColumn, 160, Unit.PX );
    table.setColumnWidth( resourceColumn, 200, Unit.PX );
    table.setColumnWidth( outputPathColumn, 180, Unit.PX );
    table.setColumnWidth( scheduleColumn, 170, Unit.PX );
    table.setColumnWidth( lastFireColumn, 120, Unit.PX );
    table.setColumnWidth( nextFireColumn, 120, Unit.PX );
    if ( isAdmin ) {
      table.setColumnWidth( userNameColumn, 100, Unit.PX );
    }
    table.setColumnWidth( stateColumn, 90, Unit.PX );

    dataProvider.addDataDisplay( table );
    List<JsJob> list = dataProvider.getList();

    ListHandler<JsJob> columnSortHandler = new ListHandler<JsJob>( list );

    columnSortHandler.setComparator( idColumn, new Comparator<JsJob>() {
      public int compare( JsJob o1, JsJob o2 ) {
        if ( o1 == o2 ) {
          return 0;
        }

        if ( o1 != null ) {
          return ( o2 != null ) ? o1.getJobId().compareTo( o2.getJobId() ) : 1;
        }
        return -1;
      }
    } );
    columnSortHandler.setComparator( nameColumn, new Comparator<JsJob>() {
      public int compare( JsJob o1, JsJob o2 ) {
        if ( o1 == o2 ) {
          return 0;
        }

        if ( o1 != null ) {
          return ( o2 != null ) ? o1.getJobName().compareTo( o2.getJobName() ) : 1;
        }
        return -1;
      }
    } );
    columnSortHandler.setComparator( resourceColumn, new Comparator<JsJob>() {
      public int compare( JsJob o1, JsJob o2 ) {
        if ( o1 == o2 ) {
          return 0;
        }

        if ( o1 != null ) {
          String r1 = o1.getShortResourceName();
          String r2 = null;
          if ( o2 != null ) {
            r2 = o2.getShortResourceName();
          }

          return ( o2 != null ) ? r1.compareTo( r2 ) : 1;
        }
        return -1;
      }
    } );
    columnSortHandler.setComparator( outputPathColumn, new Comparator<JsJob>() {
      public int compare( JsJob o1, JsJob o2 ) {
        if ( o1 == o2 ) {
          return 0;
        }

        if ( o1 != null ) {
          String r1 = o1.getOutputPath();
          String r2 = null;
          if ( o2 != null ) {
            r2 = o2.getOutputPath();
          }

          return ( o2 != null ) ? r1.compareTo( r2 ) : 1;
        }
        return -1;
      }
    } );
    columnSortHandler.setComparator( scheduleColumn, new Comparator<JsJob>() {
      public int compare( JsJob o1, JsJob o2 ) {
        String s1 = o1.getJobTrigger().getDescription();
        String s2 = o2.getJobTrigger().getDescription();
        return s1.compareTo( s2 );
      }
    } );
    columnSortHandler.setComparator( userNameColumn, new Comparator<JsJob>() {
      public int compare( JsJob o1, JsJob o2 ) {
        if ( o1 == o2 ) {
          return 0;
        }

        if ( o1 != null ) {
          return ( o2 != null ) ? o1.getUserName().compareTo( o2.getUserName() ) : 1;
        }
        return -1;
      }
    } );
    columnSortHandler.setComparator( stateColumn, new Comparator<JsJob>() {
      public int compare( JsJob o1, JsJob o2 ) {
        if ( o1 == o2 ) {
          return 0;
        }

        if ( o1 != null ) {
          return ( o2 != null ) ? o1.getState().compareTo( o2.getState() ) : 1;
        }
        return -1;
      }
    } );
    columnSortHandler.setComparator( nextFireColumn, new Comparator<JsJob>() {
      public int compare( JsJob o1, JsJob o2 ) {
        if ( o1 == o2 ) {
          return 0;
        }

        if ( o1 == null || o1.getNextRun() == null ) {
          return -1;
        }
        if ( o2 == null || o2.getNextRun() == null ) {
          return 1;
        }

        if ( o1.getNextRun() == o2.getNextRun() ) {
          return 0;
        }

        return o1.getNextRun().compareTo( o2.getNextRun() );
      }
    } );
    columnSortHandler.setComparator( lastFireColumn, new Comparator<JsJob>() {
      public int compare( JsJob o1, JsJob o2 ) {
        if ( o1 == o2 ) {
          return 0;
        }

        if ( o1 == null || o1.getLastRun() == null ) {
          return -1;
        }
        if ( o2 == null || o2.getLastRun() == null ) {
          return 1;
        }

        if ( o1.getLastRun() == o2.getLastRun() ) {
          return 0;
        }

        return o1.getLastRun().compareTo( o2.getLastRun() );
      }
    } );
    table.addColumnSortHandler( columnSortHandler );

    table.getColumnSortList().push( idColumn );
    table.getColumnSortList().push( resourceColumn );
    table.getColumnSortList().push( outputPathColumn );
    table.getColumnSortList().push( nameColumn );

    table.getSelectionModel().addSelectionChangeHandler( new Handler() {
      public void onSelectionChange( SelectionChangeEvent event ) {
        Set<JsJob> selectedJobs = getSelectedJobs();
        if ( selectedJobs != null && selectedJobs.size() > 0 ) {
          JsJob[] jobs = selectedJobs.toArray( new JsJob[selectedJobs.size()] );
          editButton.setEnabled( isScheduler );
          if ( "NORMAL".equalsIgnoreCase( jobs[0].getState() ) ) {
            controlScheduleButton.setImage( ImageUtil.getThemeableImage( "icon-small", "icon-stop" ) );
          } else {
            controlScheduleButton.setImage( ImageUtil.getThemeableImage( "icon-small", "icon-run" ) );
          }
          controlScheduleButton.setEnabled( isScheduler );

          boolean isRunning = "NORMAL".equalsIgnoreCase( jobs[0].getState() );
          controlScheduleButton.setToolTip( isRunning ? Messages.getString( "stop" ) : Messages.getString( "start" ) );
          scheduleRemoveButton.setEnabled( isScheduler );
          triggerNowButton.setEnabled( isScheduler );
        } else {
          editButton.setEnabled( false );
          controlScheduleButton.setEnabled( false );
          scheduleRemoveButton.setEnabled( false );
          triggerNowButton.setEnabled( false );
        }
      }
    } );
    // BISERVER-9965
    table.addCellPreviewHandler( new CellPreviewEvent.Handler<JsJob>() {
      @Override
      public void onCellPreview( CellPreviewEvent<JsJob> event ) {
        if ( "mouseover".equals( event.getNativeEvent().getType() ) ) {
          final TableCellElement cell = table.getRowElement( event.getIndex() ).getCells().getItem( event.getColumn() );
          cell.setTitle( cell.getInnerText() );
        }
      }
    } );

    SimplePager.Resources pagerResources = GWT.create( SimplePager.Resources.class );
    pager = new SimplePager( TextLocation.CENTER, pagerResources, false, 0, true ) {
      @Override
      public void setPageStart( int index ) {
        if ( getDisplay() != null ) {
          Range range = getDisplay().getVisibleRange();
          int pageSize = range.getLength();

          // Removed the min to show fixed ranges
          // if (isRangeLimited && display.isRowCountExact()) {
          // index = Math.min(index, display.getRowCount() - pageSize);
          // }

          index = Math.max( 0, index );
          if ( index != range.getStart() ) {
            getDisplay().setVisibleRange( index, pageSize );
          }
        }
      }
    };
    pager.setDisplay( table );
    // pager.setRangeLimited(false);

    VerticalPanel tableAndPager = new VerticalPanel();
    tableAndPager.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );

    Toolbar bar = new Toolbar();
    bar.addSpacer( 10 );

    bar.add( Toolbar.GLUE );

    // Add control scheduler button
    if ( isAdmin ) {
      final ToolbarButton controlSchedulerButton =
          new ToolbarButton( ImageUtil.getThemeableImage( "icon-small", "icon-start-scheduler" ) );

      controlSchedulerButton.setCommand( new Command() {
        public void execute() {
          toggleSchedulerOnOff( controlSchedulerButton, isScheduler );
        }
      } );
      updateControlSchedulerButtonState( controlSchedulerButton, isScheduler );

      bar.add( controlSchedulerButton );
      bar.addSpacer( 20 );
    }

    // Add filter button
    filterButton.setCommand( new Command() {
      public void execute() {
        if ( filterDialog == null ) {
          filterDialog = new FilterDialog( allJobs, filterDialogCallback );
        } else {
          filterDialog.initUI( allJobs );
        }
        filterDialog.center();
      }
    } );
    filterButton.setToolTip( Messages.getString( "filterSchedules" ) );
    if ( isAdmin ) {
      bar.add( filterButton );
    }

    // Add remove filters button
    filterRemoveButton.setCommand( new Command() {
      public void execute() {
        filterDialog = null;
        filters.clear();
        filterAndShowData();
        filterRemoveButton.setEnabled( false );
        filterButton.setImage( ImageUtil.getThemeableImage( "icon-small", "icon-filter-add" ) );
      }
    } );
    filterRemoveButton.setToolTip( Messages.getString( "removeFilters" ) );
    filterRemoveButton.setEnabled( filters.size() > 0 );
    if ( isAdmin ) {
      bar.add( filterRemoveButton );
    }

    // Add refresh button
    ToolbarButton refresh = new ToolbarButton( ImageUtil.getThemeableImage( "icon-small", "icon-refresh" ) );
    refresh.setToolTip( Messages.getString( "refreshTooltip" ) );
    refresh.setCommand( new Command() {
      public void execute() {
        RefreshSchedulesCommand cmd = new RefreshSchedulesCommand();
        cmd.execute();
      }
    } );
    bar.add( refresh );

    bar.addSpacer( 20 );

    // Add execute now button
    triggerNowButton.setToolTip( Messages.getString( "executeNow" ) );
    triggerNowButton.setCommand( new Command() {
      public void execute() {
        Set<JsJob> selectedJobs = getSelectedJobs();
        if ( selectedJobs != null && selectedJobs.size() > 0 ) {
          MessageDialogBox messageDialog =
              new MessageDialogBox( Messages.getString( "executeNow" ), Messages.getString( "executeNowStarted" ),
                  false, true, true );
          messageDialog.setCallback( new IDialogCallback() {
            public void okPressed() {
              // wait a little to refresh to give schedule time to update the last run
              Timer t = new Timer() {
                public void run() {
                  refresh();
                }
              };
              t.schedule( 2000 );
            }

            public void cancelPressed() {
            }
          } );
          messageDialog.center();
          controlJobs( selectedJobs, "triggerNow", RequestBuilder.POST, false );
        }
      }
    } );
    triggerNowButton.setEnabled( false );
    bar.add( triggerNowButton );

    // Add control schedule button
    controlScheduleButton.setCommand( new Command() {
      public void execute() {
        Set<JsJob> selectedJobs = getSelectedJobs();
        if ( selectedJobs != null && selectedJobs.size() > 0 ) {
          JsJob[] jobs = selectedJobs.toArray( new JsJob[selectedJobs.size()] );
          if ( "NORMAL".equals( jobs[0].getState() ) ) {
            controlJobs( selectedJobs, "pauseJob", RequestBuilder.POST, false );
          } else {
            controlJobs( selectedJobs, "resumeJob", RequestBuilder.POST, false );
          }
        }
      }
    } );
    controlScheduleButton.setEnabled( false );
    bar.add( controlScheduleButton );

    bar.addSpacer( 20 );

    // Add edit button
    editButton.setCommand( new Command() {
      public void execute() {
        Set<JsJob> selectedJobs = getSelectedJobs();
        if ( selectedJobs != null && selectedJobs.size() > 0 ) {
          JsJob[] jobs = selectedJobs.toArray( new JsJob[selectedJobs.size()] );
          final JsJob editJob = jobs[0];
          final String url =
              GWT.getHostPageBaseURL() + "api/repo/files/"
                  + SolutionBrowserPanel.pathToId( editJob.getFullResourceName() ) + "/canAccess?cb="
                  + System.currentTimeMillis() + "&permissions=" + READ_PERMISSION;
          RequestBuilder executableTypesRequestBuilder = new RequestBuilder( RequestBuilder.GET, url );
          try {
            executableTypesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
            executableTypesRequestBuilder.sendRequest( null, new RequestCallback() {

              public void onError( Request request, Throwable exception ) {
                promptForScheduleResourceError( editJob );
              }

              public void onResponseReceived( Request request, Response response ) {
                if ( "true".equalsIgnoreCase( response.getText() ) ) {
                  editJob( editJob );
                } else {
                  promptForScheduleResourceError( editJob );
                }
              }
            } );
          } catch ( RequestException e ) {
            // showError(e);
          }
        }
      }
    } );

    editButton.setEnabled( false );
    editButton.setToolTip( Messages.getString( "editTooltip" ) );
    bar.add( editButton );

    // Add remove button
    scheduleRemoveButton.setCommand( new Command() {
      public void execute() {
        Set<JsJob> selectedJobs = getSelectedJobs();
        if ( selectedJobs != null && selectedJobs.size() > 0 ) {
          final PromptDialogBox prompt =
              new PromptDialogBox( Messages.getString( "warning" ), Messages.getString( "yes" ), Messages
                  .getString( "no" ), false, true );
          prompt.setContent( new Label( Messages.getString( "deleteConfirmSchedles", "" + selectedJobs.size() ) ) );

          prompt.setCallback( new IDialogCallback() {
            public void okPressed() {
              controlJobs( getSelectedJobs(), "removeJob", RequestBuilder.DELETE, true );
              prompt.hide();
            }

            public void cancelPressed() {
              prompt.hide();
            }
          } );
          prompt.center();
        }
      }
    } );
    scheduleRemoveButton.setToolTip( Messages.getString( "remove" ) );
    scheduleRemoveButton.setEnabled( false );
    bar.add( scheduleRemoveButton );

    tableAndPager.add( bar );
    tableAndPager.add( table );
    tableAndPager.add( pager );

    // Add it to the root panel.
    setWidget( tableAndPager );
  }

  private void editJob( JsJob editJob ) {
    final String url = GWT.getHostPageBaseURL() + "api/scheduler/jobinfo?jobId=" + URL.encodeQueryString( editJob.getJobId() );
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder( RequestBuilder.GET, url );
    executableTypesRequestBuilder.setHeader( "accept", "application/json" );
    executableTypesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      executableTypesRequestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == Response.SC_OK ) {
            final JsJob jsJob = parseJsonJob( JsonUtils.escapeJsonForEval( response.getText() ) );

            // check email is setup
            RequestBuilder emailValidRequest =
                new RequestBuilder( RequestBuilder.GET, GWT.getHostPageBaseURL() + "api/emailconfig/isValid" );
            emailValidRequest.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
            emailValidRequest.setHeader( "accept", "text/plain" );
            try {
              emailValidRequest.sendRequest( null, new RequestCallback() {

                public void onError( Request request, Throwable exception ) {
                  MessageDialogBox dialogBox =
                      new MessageDialogBox( Messages.getString( "error" ), exception.toString(), false, false, true ); //$NON-NLS-1$
                  dialogBox.center();
                }

                public void onResponseReceived( Request request, Response response ) {
                  if ( response.getStatusCode() == Response.SC_OK ) {
                    final boolean isEmailConfValid = Boolean.parseBoolean( response.getText() );
                    final NewScheduleDialog schedDialog =
                        new NewScheduleDialog( jsJob, scheduleDialogCallback, isEmailConfValid );
                    schedDialog.center();
                  }
                }
              } );
            } catch ( RequestException e ) {
              // showError(e);
            }

          } else {
            MessageDialogBox dialogBox =
                new MessageDialogBox(
                    Messages.getString( "error" ), Messages.getString( "serverErrorColon" ) + " " + response.getStatusCode(), false, false, true ); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            dialogBox.center();
          }
        }
      } );
    } catch ( RequestException e ) {
      // showError(e);
    }
  }

  private void promptForScheduleResourceError( final JsJob job ) {
    final PromptDialogBox prompt =
        new PromptDialogBox( Messages.getString( "fileUnavailable" ), Messages.getString( "yesDelete" ), Messages
            .getString( "no" ), false, true );
    prompt.setContent( new HTML( Messages.getString( "editScheduleResourceDoesNotExist",
        job.getFullResourceName() ) ) );

    prompt.setCallback( new IDialogCallback() {
      public void okPressed() {
        HashSet<JsJob> jobSet = new HashSet<JsJob>();
        jobSet.add( job );
        controlJobs( jobSet, "removeJob", RequestBuilder.DELETE, true );
        prompt.hide();
      }

      public void cancelPressed() {
        prompt.hide();
      }
    } );
    prompt.setWidth( "530px" );
    prompt.center();
  }

  private void controlJobs( final Set<JsJob> jobs, String function, final Method method, final boolean refreshData ) {
    for ( final JsJob job : jobs ) {
      final String url = GWT.getHostPageBaseURL() + "api/scheduler/" + function; //$NON-NLS-1$
      RequestBuilder builder = new RequestBuilder( method, url );
      builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      builder.setHeader( "Content-Type", "application/json" ); //$NON-NLS-1$//$NON-NLS-2$

      JSONObject startJobRequest = new JSONObject();
      startJobRequest.put( "jobId", new JSONString( job.getJobId() ) ); //$NON-NLS-1$

      try {
        builder.sendRequest( startJobRequest.toString(), new RequestCallback() {

          public void onError( Request request, Throwable exception ) {
            // showError(exception);
          }

          public void onResponseReceived( Request request, Response response ) {
            job.setState( response.getText() );
            table.redraw();
            boolean isRunning = "NORMAL".equalsIgnoreCase( response.getText() );
            if ( isRunning ) {
              controlScheduleButton.setToolTip( Messages.getString( "stop" ) );
              controlScheduleButton.setImage( ImageUtil.getThemeableImage( "icon-small", "icon-stop" ) );
            } else {
              controlScheduleButton.setToolTip( Messages.getString( "start" ) );
              controlScheduleButton.setImage( ImageUtil.getThemeableImage( "icon-small", "icon-run" ) );
            }
            if ( refreshData ) {
              refresh();
            }
          }
        } );
      } catch ( RequestException e ) {
        // showError(e);
      }
    }
  }

  private void controlScheduler( final ToolbarButton controlSchedulerButton, final String function,
                                 final boolean isScheduler ) {
    final String url = GWT.getHostPageBaseURL() + "api/scheduler/" + function; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder( RequestBuilder.POST, url );
    builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      builder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
          boolean isRunning = "RUNNING".equalsIgnoreCase( response.getText() );
          if ( isRunning ) {
            controlSchedulerButton.setToolTip( Messages.getString( "stopScheduler" ) );
            controlSchedulerButton.setImage( ImageUtil.getThemeableImage( "icon-small", "icon-stop-scheduler" ) );
          } else {
            controlSchedulerButton.setToolTip( Messages.getString( "startScheduler" ) );
            controlSchedulerButton.setImage( ImageUtil.getThemeableImage( "icon-small", "icon-start-scheduler" ) );
          }

          if ( !isScheduler ) {
            controlSchedulerButton.setEnabled( false );
          } else {
            controlSchedulerButton.setEnabled( true );
          }
        }
      } );
    } catch ( RequestException e ) {
      // showError(e);
    }
  }

  private void openOutputLocation( final String outputLocation ) {
    final String url = GWT.getHostPageBaseURL() + "api/mantle/session-variable?key=scheduler_folder&value=" + outputLocation;
    PerspectiveManager.getInstance().setPerspective( PerspectiveManager.BROWSER_PERSPECTIVE );
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder( RequestBuilder.POST, url );
    try {
      executableTypesRequestBuilder.sendRequest( null, new RequestCallback() {
        public void onError( Request request, Throwable exception ) {
        }

        public void onResponseReceived( Request request, Response response ) {
        }
      } );
    } catch ( RequestException e ) {
      //IGNORE
    }
    GenericEvent event = new GenericEvent();
    event.setEventSubType( "RefreshFolderEvent" );
    event.setStringParam( outputLocation );
    EventBusUtil.EVENT_BUS.fireEvent( event );
  }

  private void showValidateOutputLocationError() {
    String title = Messages.getString( "outputLocationErrorTitle" );
    String message = Messages.getString( "outputLocationErrorMessage" );
    MessageDialogBox dialogBox =
        new MessageDialogBox( title, message, false, false, true, Messages.getString( "close" ), null, null ); //$NON-NLS-1$
    dialogBox.addStyleName( "pentaho-dialog-small" );
    dialogBox.center();
  }

  private native JsArray<JsJob> parseJson( String json )
  /*-{
      var obj = eval('(' + json + ')');
      if (obj != null && obj.hasOwnProperty("job")) {
          return obj.job;
      }
      return [];
  }-*/;

  private native JsJob parseJsonJob( String json )
  /*-{
      var obj = eval('(' + json + ')');
      return obj;
  }-*/;

}
