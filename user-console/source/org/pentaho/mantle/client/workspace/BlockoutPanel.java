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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.table.BaseTable;
import org.pentaho.gwt.widgets.client.table.ColumnComparators.BaseColumnComparator;
import org.pentaho.gwt.widgets.client.table.ColumnComparators.ColumnComparatorTypes;
import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.dialogs.scheduling.NewBlockoutScheduleDialog;
import org.pentaho.mantle.client.images.ImageUtil;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.gen2.table.client.SelectionGrid.SelectionPolicy;
import com.google.gwt.gen2.table.event.client.RowSelectionEvent;
import com.google.gwt.gen2.table.event.client.RowSelectionHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class BlockoutPanel extends SimplePanel {
  private BaseTable table;
  // private ListDataProvider<JsJob> dataProvider = new ListDataProvider<JsJob>();
  private List<JsJob> list = new ArrayList<JsJob>();
  private final VerticalPanel widgets = new VerticalPanel();
  private Button blockoutButton;
  private Toolbar tableControls;
  private VerticalPanel tablePanel;
  private ToolbarButton editButton;
  private ToolbarButton removeButton;

  private IDialogCallback refreshCallBack = new IDialogCallback() {
    public void okPressed() {
      refresh();
    }

    public void cancelPressed() {
      refresh();
    }
  };
  private Label headlineLabel;
  private Label blockoutHeading;
  private boolean isAdmin;

  public BlockoutPanel( final boolean isAdmin ) {
    this.isAdmin = isAdmin;
    createUI( isAdmin );
    refresh();
  }

  private void createUI( final boolean isAdmin ) {
    widgets.setWidth( "100%" );
    createBlockoutHeadingBar();
    createHeadlineBar();
    createControls( isAdmin );
    createTable();
    widgets.add( tablePanel );
    setWidget( widgets );
  }

  private void createBlockoutHeadingBar() {
    blockoutHeading = new Label( "" );
    blockoutHeading.setStyleName( "workspaceHeading" );
    widgets.add( blockoutHeading );
  }

  private void createHeadlineBar() {
    headlineLabel = new Label( "" );
    widgets.add( headlineLabel );
  }

  private void createControls( final boolean isAdmin ) {
    blockoutButton = new Button( Messages.getString( "createBlockoutTime" ) );
    tableControls = new Toolbar();
    tablePanel = new VerticalPanel();
    tablePanel.setVisible( false );
    if ( isAdmin ) {
      final ClickHandler newBlockoutHandler = new ClickHandler() {
        @Override
        public void onClick( final ClickEvent clickEvent ) {
          DialogBox blockoutDialog = new NewBlockoutScheduleDialog( "", refreshCallBack, false, true );
          blockoutDialog.center();
        }
      };
      createBlockoutButton( newBlockoutHandler );
      createTableControls( newBlockoutHandler );
    }
  }

  private void createBlockoutButton( final ClickHandler newBlockoutHandler ) {
    SimplePanel buttonPanel = new SimplePanel();
    buttonPanel.setStyleName( "schedulesButtonPanel" );
    blockoutButton.addClickHandler( newBlockoutHandler );
    blockoutButton.setStyleName( "pentaho-button" );
    buttonPanel.add( blockoutButton );
    widgets.add( buttonPanel );
  }

  private void createTableControls( final ClickHandler newBlockoutHandler ) {
    tableControls.addSpacer( 10 );
    tableControls.add( Toolbar.GLUE );
    ToolbarButton addButton = new ToolbarButton( ImageUtil.getThemeableImage( "pentaho-addbutton" ) );
    addButton.setCommand( new Command() {
      @Override
      public void execute() {
        newBlockoutHandler.onClick( null );
      }
    } );
    addButton.setToolTip( Messages.getString( "blockoutAdd" ) );
    editButton = new ToolbarButton( ImageUtil.getThemeableImage( "pentaho-editbutton" ) );
    editButton.setEnabled( false );
    editButton.setCommand( new Command() {
      @Override
      public void execute() {
        Set<JsJob> jobs = getSelectedSet();
        final JsJob jsJob = jobs.iterator().next();

        IDialogCallback callback = new IDialogCallback() {
          public void okPressed() {
            // delete the old one
            removeBlockout( jsJob );
            refreshCallBack.okPressed();
          }

          public void cancelPressed() {
            refreshCallBack.cancelPressed();
          }
        };

        NewBlockoutScheduleDialog blockoutDialog = new NewBlockoutScheduleDialog( jsJob, callback, false, true );
        table.selectRow( list.indexOf( jsJob ) );
        blockoutDialog.setUpdateMode();
        blockoutDialog.center();
      }
    } );
    editButton.setToolTip( Messages.getString( "blockoutEdit" ) );
    removeButton = new ToolbarButton( ImageUtil.getThemeableImage( "pentaho-deletebutton" ) );
    removeButton.setEnabled( false );
    removeButton.setCommand( new Command() {
      public void execute() {

        final Set<JsJob> selectedSet = getSelectedSet();

        final Label messageTextBox = new Label( Messages.getString( "deleteBlockoutWarning", ""
            + selectedSet.size() ) );
        final PromptDialogBox blockoutDeleteWarningDialogBox =
            new PromptDialogBox( Messages.getString( "delete" ), Messages.getString( "yesDelete" ), Messages
                .getString( "no" ), true, true );
        blockoutDeleteWarningDialogBox.setContent( messageTextBox );
        final IDialogCallback callback = new IDialogCallback() {

          public void cancelPressed() {
            blockoutDeleteWarningDialogBox.hide();
          }

          public void okPressed() {
            for ( JsJob jsJob : selectedSet ) {
              removeBlockout( jsJob );
              table.selectRow( list.indexOf( jsJob ) );
            }
          }
        };
        blockoutDeleteWarningDialogBox.setCallback( callback );
        blockoutDeleteWarningDialogBox.center();
      }
    } );
    removeButton.setToolTip( Messages.getString( "blockoutDelete" ) );
    tableControls.add( editButton );
    tableControls.add( addButton );
    tableControls.add( removeButton );
    tablePanel.add( tableControls );
  }

  private void createTable() {
    int columnSize = 139;
    String[] tableHeaderNames =
    {Messages.getString( "blockoutColumnStarts" ), Messages.getString( "blockoutColumnEnds" ),
            Messages.getString( "blockoutColumnRepeats" ), Messages.getString( "blockoutColumnRepeatsEndBy" )};
    int[] columnWidths = {columnSize, columnSize, columnSize, columnSize};
    BaseColumnComparator[] columnComparators =
    {BaseColumnComparator.getInstance( ColumnComparatorTypes.DATE ),
            BaseColumnComparator.getInstance( ColumnComparatorTypes.DATE ),
            BaseColumnComparator.getInstance( ColumnComparatorTypes.STRING_NOCASE ),
            BaseColumnComparator.getInstance( ColumnComparatorTypes.STRING_NOCASE )};
    table = new BaseTable( tableHeaderNames, columnWidths, columnComparators, SelectionPolicy.MULTI_ROW );
    table.getElement().setId( "blockout-table" );
    table.setWidth( "640px" );
    table.setHeight( "328px" );
    table.fillWidth();
    table.addRowSelectionHandler( new RowSelectionHandler() {
      @Override
      public void onRowSelection( RowSelectionEvent event ) {
        boolean isSelected = event.getNewValue().size() > 0;
        boolean isSingleSelect = event.getNewValue().size() == 1;
        editButton.setEnabled( isSingleSelect );
        removeButton.setEnabled( isSelected );
      }
    } );
    tablePanel.add( table );
  }

  private String formatDate( final Date date ) {
    DateTimeFormat simpleDateFormat = DateTimeFormat.getFormat( "EEE, MMM dd h:mm a" );
    return simpleDateFormat.format( date );
  }

  private void removeBlockout( final JsJob jsJob ) {
    JSONObject jobRequest = new JSONObject();
    jobRequest.put( "jobId", new JSONString( jsJob.getJobId() ) ); //$NON-NLS-1$
    makeServiceCall( "removeJob", RequestBuilder.DELETE, jobRequest.toString(), "text/plain", new RequestCallback() {

      public void onError( Request request, Throwable exception ) {
        // todo: do something
      }

      public void onResponseReceived( Request request, Response response ) {
        if ( response.getStatusCode() == Response.SC_OK ) {
          refresh();
        }
      }
    } );
  }

  public void refresh() {
    makeServiceCall( "blockout/blockoutjobs", RequestBuilder.GET, null, "application/json", new RequestCallback() {

      public void onError( Request request, Throwable exception ) {
        // todo: do something
      }

      public void onResponseReceived( Request request, Response response ) {
        if ( response.getStatusCode() == Response.SC_OK ) {
          if ( "null".equals( response.getText() ) ) {
            showData( null );
          } else {
            showData( parseJson( JsonUtils.escapeJsonForEval( response.getText() ) ) );
          }
        }
      }
    } );
  }

  private void makeServiceCall( final String urlSuffix, final RequestBuilder.Method httpMethod,
                                final String requestData, final String acceptHeader, final RequestCallback callback ) {
    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );
    final String url = contextURL + "api/scheduler/" + urlSuffix;
    RequestBuilder builder = new RequestBuilder( httpMethod, url );
    builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    builder.setHeader( "Content-Type", "application/json" );
    if ( !StringUtils.isEmpty( acceptHeader ) ) {
      builder.setHeader( "accept", acceptHeader );
    }
    try {
      builder.sendRequest( requestData, callback );
    } catch ( RequestException e ) {
      // showError(e);
    }
  }

  private void showData( final JsArray<JsJob> allBlocks ) {
    blockoutHeading.setText( Messages.getString( "blockoutTimes" ) );
    if ( allBlocks == null || allBlocks.length() == 0 ) {
      tablePanel.setVisible( false );
      blockoutButton.setVisible( true );
      headlineLabel.setText( Messages.getString( "blockoutNone" ) );

      if ( !isAdmin ) {
        blockoutHeading.setVisible( false );
        headlineLabel.setVisible( false );
      }

    } else {
      tablePanel.setVisible( true );
      blockoutButton.setVisible( false );
      blockoutHeading.setVisible( true );
      headlineLabel.setText( Messages.getString( "blockoutHeadline" ) );
      if ( !isAdmin ) {
        headlineLabel.setVisible( true );
      }
      List<JsJob> jobList = new ArrayList<JsJob>();
      for ( int i = 0; i < allBlocks.length(); i++ ) {
        JsJob job = allBlocks.get( i );
        jobList.add( job );
      }
      // List<JsJob> list = dataProvider.getList();
      list.clear();
      list.addAll( jobList );

      int row = 0;
      Object[][] tableContent = new Object[list.size()][4];
      for ( JsJob block : list ) {
        tableContent[row][0] = getStartValue( block );
        tableContent[row][1] = getEndValue( block );
        tableContent[row][2] = getRepeatValue( block );
        tableContent[row][3] = getRepeatEndValue( block );
        row++;
      }
      table.populateTable( tableContent );
      table.addStyleName( "" );
      table.setVisible( jobList.size() > 0 );

    }

  }

  private native JsArray<JsJob> parseJson( String json )
  /*-{
    var obj = eval('(' + json + ')');
    return obj.job;
  }-*/;

  private String convertDateToValue( Date date ) {
    if ( date != null ) {
      try {
        return formatDate( date );
      } catch ( Throwable t ) {
        //ignored
      }
    }
    return "-";
  }

  private String getStartValue( JsJob block ) {

    long now = System.currentTimeMillis();
    long duration = block.getJobTrigger().getBlockDuration();
    Date lastRun = block.getLastRun();

    // if we have a last execution and we are still within the range of that, the
    // starts / ends need to still reflect this rather than the next execution
    if ( lastRun != null && now < lastRun.getTime() + duration && now > lastRun.getTime() ) {
      return convertDateToValue( lastRun );
    }

    if ( block.getNextRun() != null ) {
      return convertDateToValue( block.getNextRun() );
    } else if ( block.getJobTrigger() != null && block.getJobTrigger().getStartTime() != null ) {
      return convertDateToValue( block.getJobTrigger().getStartTime() );
    } else if ( "COMPLETE".equals( block.getState() ) && block.getJobTrigger() != null ) {
      // if a job is complete, it will not have the date in the nextRun attribute
      return convertDateToValue( block.getJobTrigger().getStartTime() );
    } else {
      return "-";
    }
  }

  private String getEndValue( JsJob block ) {

    long now = System.currentTimeMillis();
    long duration = block.getJobTrigger().getBlockDuration();
    Date lastRun = block.getLastRun();

    // if we have a last execution and we are still within the range of that, the
    // starts / ends need to still reflect this rather than the next execution
    if ( lastRun != null && now < lastRun.getTime() + duration && now > lastRun.getTime() ) {
      return convertDateToValue( new Date( lastRun.getTime() + duration ) );
    }

    if ( block.getNextRun() instanceof Date ) {
      return convertDateToValue( new Date( block.getNextRun().getTime() + duration ) );
    } else if ( "COMPLETE".equals( block.getState() ) && block.getJobTrigger() != null
        && block.getJobTrigger().getStartTime() != null ) {
      // if a job is complete, it will not have the date in the nextRun attribute
      return convertDateToValue( new Date( block.getJobTrigger().getStartTime().getTime()
          + block.getJobTrigger().getBlockDuration() ) );

    } else {
      return "-";
    }
  }

  private String getRepeatValue( JsJob block ) {
    try {
      return block.getJobTrigger().getDescription();
    } catch ( Throwable t ) {
      //ignored
    }
    return "-";
  }

  private String getRepeatEndValue( JsJob block ) {
    try {
      Date endTime = block.getJobTrigger().getEndTime();
      if ( endTime == null ) {
        return "Never";
      } else {
        return formatDate( endTime );
      }
    } catch ( Throwable t ) {
      //ignored
    }
    return "-";
  }

  private Set<JsJob> getSelectedSet() {
    Set<Integer> selected = table.getSelectedRows();
    Set<JsJob> selectedSet = new HashSet<JsJob>();
    for ( Integer selectedRow : selected ) {
      selectedSet.add( list.get( selectedRow ) );
    }
    return selectedSet;
  }
}
