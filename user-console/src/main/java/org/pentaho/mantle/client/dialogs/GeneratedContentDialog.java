/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.mantle.client.dialogs;

import com.google.gwt.gen2.table.client.SelectionGrid;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.table.BaseTable;
import org.pentaho.gwt.widgets.client.table.ColumnComparators.BaseColumnComparator;
import org.pentaho.gwt.widgets.client.table.ColumnComparators.ColumnComparatorTypes;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author wseyler
 * 
 */
@SuppressWarnings( "deprecation" )
public class GeneratedContentDialog extends PromptDialogBox implements IDialogCallback, TableListener {
  private RepositoryFile sourceFile = null;
  private List<RepositoryFile> workspaceFiles = null;

  private BaseTable table;

  /**
   * @param title
   * @param okText
   * @param cancelText
   * @param autoHide
   * @param modal
   */
  public GeneratedContentDialog() {
    super( "", Messages.getString( "open" ), Messages.getString( "cancel" ), false, true );
  }

  /**
   * @param sourceFile
   * @param workspaceFiles
   */
  public GeneratedContentDialog( RepositoryFile sourceFile, List<RepositoryFile> workspaceFiles ) {
    this();
    this.sourceFile = sourceFile;
    this.workspaceFiles = workspaceFiles;

    initGui();
  }

  private void initGui() {
    setCallback( this );
    // Make the dialog title "Some Title Archive"
    setText( Messages.getString( "archiveTitle", sourceFile.getTitle() ) );
    String[] headers = { "Type", "Date" };
    int[] widths = { 20, 200 };
    BaseColumnComparator[] columnComparators = { null, BaseColumnComparator.getInstance( ColumnComparatorTypes.DATE ) };
    table = new BaseTable( headers, widths, columnComparators, SelectionGrid.SelectionPolicy.MULTI_ROW );
    table.setWidth( "500px" );
    table.setHeight( "150px" );
    table.addDoubleClickListener( this );
    Object[][] tableContent = new Object[workspaceFiles.size()][2];
    for ( int row = 0; row < workspaceFiles.size(); row++ ) {
      String type =
          workspaceFiles.get( row ).getPath().substring( workspaceFiles.get( row ).getPath().lastIndexOf( "." ) );
      Date date = workspaceFiles.get( row ).getCreatedDate();
      String formattedDate = DateFormat.getDateTimeInstance().format( date );
      tableContent[row][0] = type;
      tableContent[row][1] = formattedDate;
    }
    table.populateTable( tableContent );
    ScrollPanel scrollPanel = new ScrollPanel( table );
    scrollPanel.setSize( "400px", "100px" );
    setContent( scrollPanel );
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.google.gwt.user.client.ui.TableListener#onCellClicked(com.google.gwt.user.client.ui.SourcesTableEvents,
   * int, int)
   */
  @Override
  public void onCellClicked( SourcesTableEvents sender, int row, int cell ) {
    okPressed();
    hide();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.gwt.widgets.client.dialogs.IDialogCallback#cancelPressed()
   */
  @Override
  public void cancelPressed() {
    // Nothing to do
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.gwt.widgets.client.dialogs.IDialogCallback#okPressed()
   */
  @Override
  public void okPressed() {
    Set<Integer> selected = table.getSelectedRows();
    for ( Integer selectedRow : selected ) {
      String dateStr = table.getText( selectedRow, 1 );
      for ( RepositoryFile fileDto : workspaceFiles ) {
        String formattedDate = DateFormat.getDateTimeInstance().format( fileDto.getCreatedDate() );
        if ( dateStr.equals( formattedDate ) ) {
          SolutionBrowserPanel.getInstance().openFile( fileDto, COMMAND.RUN );
          break;
        }
      }
    }
  }

}
