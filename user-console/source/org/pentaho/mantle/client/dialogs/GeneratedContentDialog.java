/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created May 10, 2011 
 * @author wseyler
 */


package org.pentaho.mantle.client.dialogs;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.table.BaseTable;
import org.pentaho.gwt.widgets.client.table.ColumnComparators.BaseColumnComparator;
import org.pentaho.gwt.widgets.client.table.ColumnComparators.ColumnComparatorTypes;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;

import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.widgetideas.table.client.SelectionGrid.SelectionPolicy;

/**
 * @author wseyler
 *
 */
public class GeneratedContentDialog extends PromptDialogBox implements TableListener, IDialogCallback {
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
    super("", Messages.getString("open"), Messages.getString("cancel"), false, true);
  }

  /**
   * @param sourceFile 
   * @param workspaceFiles
   */
  public GeneratedContentDialog(RepositoryFile sourceFile, List<RepositoryFile> workspaceFiles) {
    this();
    this.sourceFile = sourceFile;
    this.workspaceFiles = workspaceFiles;
    
    initGui();
  }
  
  private void initGui() {
    setCallback(this);
    // Make the dialog title "Some Title Archive"
    setText(Messages.getString("archiveTitle", sourceFile.getTitle())); 
    String[] headers = {"Type", "Date"};
    int[] widths = { 20, 200 };
    BaseColumnComparator[] columnComparators = {null, BaseColumnComparator.getInstance(ColumnComparatorTypes.DATE)};
    table = new BaseTable( headers, widths, columnComparators, SelectionPolicy.MULTI_ROW );
    table.setWidth("500px");
    table.setHeight("150px");
    table.addDoubleClickListener(this);
    Object[][] tableContent = new Object[workspaceFiles.size()][2];
    for (int row=0; row<workspaceFiles.size(); row++) {
      String type = workspaceFiles.get(row).getPath().substring(workspaceFiles.get(row).getPath().lastIndexOf("."));
      Date date = workspaceFiles.get(row).getCreatedDate();
      tableContent[row][0] = type;
      tableContent[row][1] = date.toLocaleString();
    }
    table.populateTable(tableContent);
    ScrollPanel scrollPanel = new ScrollPanel(table);
    scrollPanel.setSize("400px", "100px");
    setContent(scrollPanel);
  }

  /* (non-Javadoc)
   * @see com.google.gwt.user.client.ui.TableListener#onCellClicked(com.google.gwt.user.client.ui.SourcesTableEvents, int, int)
   */
  @Override
  public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
    okPressed();
    hide();
  }

  /* (non-Javadoc)
   * @see org.pentaho.gwt.widgets.client.dialogs.IDialogCallback#cancelPressed()
   */
  @Override
  public void cancelPressed() {
    // Nothing to do
  }

  /* (non-Javadoc)
   * @see org.pentaho.gwt.widgets.client.dialogs.IDialogCallback#okPressed()
   */
  @Override
  public void okPressed() {
    Set<Integer> selected = table.getSelectedRows();
    for (Integer selectedRow : selected) {
      String dateStr = table.getText(selectedRow, 1);
      for (RepositoryFile fileDto : workspaceFiles) {
        if (dateStr.equals(fileDto.getCreatedDate().toLocaleString())) {
          SolutionBrowserPanel.getInstance().openFile(fileDto, COMMAND.RUN);
          break;
        }
      }
    }
  }
 
}
