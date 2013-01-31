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

package org.pentaho.mantle.client.solutionbrowser.fileproperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.JsonToRepositoryFileTreeConverter;
import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import com.google.gwt.core.client.GWT;
import com.google.gwt.gen2.table.client.FixedWidthFlexTable;
import com.google.gwt.gen2.table.client.FixedWidthGrid;
import com.google.gwt.gen2.table.client.ScrollTable;
import com.google.gwt.gen2.table.client.SortableGrid;
import com.google.gwt.gen2.table.client.SortableGrid.ColumnSorter;
import com.google.gwt.gen2.table.client.SortableGrid.ColumnSorterCallback;
import com.google.gwt.gen2.table.client.TableModelHelper.ColumnSortList;
import com.google.gwt.gen2.table.event.client.RowSelectionEvent;
import com.google.gwt.gen2.table.event.client.RowSelectionHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
/**
 * @author wseyler
 *
 */
public class GeneratedContentPanel extends VerticalPanel implements IFileModifier, RowSelectionHandler {

  private String repositoryFilePath;
  private FixedWidthGrid dataTable;
  protected HistoryToolbar toolbar;
  
  public GeneratedContentPanel(final String repositoryFilePath) {
    this.repositoryFilePath = repositoryFilePath;

    toolbar = new HistoryToolbar();
    toolbar.getRunButton().setEnabled(false);
    this.add(toolbar);

    FixedWidthFlexTable headerTable = new FixedWidthFlexTable();
    headerTable.setHTML(0, 0, Messages.getString("filename")); //$NON-NLS-1$
    headerTable.setHTML(0, 1,  Messages.getString("executed")); //$NON-NLS-1$
    headerTable.setWidth("100%"); //$NON-NLS-1$

    dataTable = new FixedWidthGrid();
    dataTable.setWidth("100%"); //$NON-NLS-1$
    dataTable.setColumnSorter(new ContentSorter());
    dataTable.addRowSelectionHandler(this);
    GeneratedContentTableImages images = GWT.create(GeneratedContentTableImages.class);
    ScrollTable scrollTable = new ScrollTable(dataTable, headerTable, images);
    scrollTable.setSize("100%", "400px");  //$NON-NLS-1$//$NON-NLS-2$
    this.add(scrollTable);
    
    this.sinkEvents(Event.ONDBLCLICK);
    init(this.repositoryFilePath, null);
  }

  /* (non-Javadoc)
   * @see org.pentaho.mantle.client.solutionbrowser.fileproperties.IFileModifier#apply()
   */
  @Override
  public void apply() {
    // TODO Auto-generated method stub
  }

  /* (non-Javadoc)
   * @see org.pentaho.mantle.client.solutionbrowser.fileproperties.IFileModifier#init(org.pentaho.gwt.widgets.client.filechooser.RepositoryFile, com.google.gwt.xml.client.Document)
   */
  @Override
  public void init(final RepositoryFile fileSummary, Document fileInfo) {
    init(SolutionBrowserPanel.pathToId(fileSummary.getPath()), fileInfo);
  }

  private void init(final String fileSummaryPath, Document fileInfo) {
    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
    String url = contextURL + "api/repo/files/" + fileSummaryPath + "/generatedcontent"; //$NON-NLS-1$ //$NON-NLS-2$
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    builder.setHeader("Accept", "application/json");
    try {
      builder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), exception.getLocalizedMessage(), false, false, true); //$NON-NLS-1$
          dialogBox.center();
        }

        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == Response.SC_OK) {
            List<RepositoryFile> repositoryFiles = JsonToRepositoryFileTreeConverter.getFileListFromJson(response.getText());
            Collections.sort(repositoryFiles, new Comparator<RepositoryFile>() {
              @Override
              public int compare(RepositoryFile o1, RepositoryFile o2) {
                return o2.getCreatedDate().compareTo(o1.getCreatedDate());
              }
            });         

            dataTable.resize(repositoryFiles.size(), 2);
            for (int row=0; row<repositoryFiles.size(); row++) {
              dataTable.setWidget(row, 0, new FileAwareLabel(repositoryFiles.get(row), 0));
              dataTable.setWidget(row, 1, new FileAwareLabel(repositoryFiles.get(row), 1));
            }
          } else {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("serverErrorColon") + " " + response.getStatusCode(), false, false, true); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            dialogBox.center();
          }
        }
      });
    } catch (RequestException e) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), e.getLocalizedMessage(), false, false, true); //$NON-NLS-1$
      dialogBox.center();
    }    
  }
  
  public void onBrowserEvent(Event event) {
    if (event.getTypeInt() == Event.ONDBLCLICK) {
      new RunContentCommand().execute();
    }
  }
  
  /**
   * @author wseyler
   *
   */
  public class HistoryToolbar extends Toolbar {
    ToolbarButton refreshBtn, runBtn;

    public HistoryToolbar() {
      super();

      // Formatting stuff
      setHorizontalAlignment(ALIGN_RIGHT);
      setStyleName("pentaho-titled-toolbar");
      setHeight("29px"); //$NON-NLS-1$
      setWidth("100%"); //$NON-NLS-1$

      createMenus();
    }

    private void createMenus() {
      addSpacer(5);
      Label label = new Label(Messages.getString("history"));
      label.setStyleName("pentaho-titled-toolbar-label");
      add(label); //$NON-NLS-1$
      add(GLUE);

      Image runImage = new Image(MantleImages.images.run());
      Image runDisabledImage = new Image(MantleImages.images.runDisabled());
      runBtn = new ToolbarButton(runImage, runDisabledImage);
      runBtn.setId("filesToolbarRun");
      runBtn.setCommand(new RunContentCommand());
      add(runBtn);

      Image refreshImage = new Image();
      refreshImage.setResource(MantleImages.images.refresh());
      Image refreshDisabledImage = new Image();
      refreshDisabledImage.setResource(MantleImages.images.runDisabled());
      refreshBtn = new ToolbarButton(refreshImage, refreshDisabledImage);
      refreshBtn.setCommand(new RefreshHistoryCommand());
      refreshBtn.setToolTip(Messages.getString("refresh")); //$NON-NLS-1$
      add(refreshBtn);
    }
    
    public ToolbarButton getRunButton() {
      return runBtn;
    }
  }
  
  /* (non-Javadoc)
   * @see com.google.gwt.gen2.table.event.client.RowSelectionHandler#onRowSelection(com.google.gwt.gen2.table.event.client.RowSelectionEvent)
   */
  @Override
  public void onRowSelection(RowSelectionEvent event) {
    toolbar.getRunButton().setEnabled(event.getSelectedRows().size() > 0);
  }

  /**
   * @author wseyler
   *
   */
  public class RefreshHistoryCommand implements Command {
  
    /* (non-Javadoc)
     * @see com.google.gwt.user.client.Command#execute()
     */
    @Override
    public void execute() {
      GeneratedContentPanel.this.init(GeneratedContentPanel.this.repositoryFilePath, null);
    } 
  }
  
  /**
   * @author wseyler
   *
   */
  public class RunContentCommand implements Command {
  
    /* (non-Javadoc)
     * @see com.google.gwt.user.client.Command#execute()
     */
    @Override
    public void execute() {
      Set<Integer> selectedRowIndices = dataTable.getSelectedRows();
      for (Integer i : selectedRowIndices) {
        RepositoryFile repoFile = ((FileAwareLabel)dataTable.getWidget(i, 0)).getFile();
        SolutionBrowserPanel.getInstance().openFile(repoFile, COMMAND.RUN);
      }
    } 
  }

  private class FileAwareLabel extends Label {
    private RepositoryFile file;
    
    public FileAwareLabel(RepositoryFile file, int column) {
      super();
      this.file = file;
      switch (column) {
        case 0:
          this.setText(this.file.getName());
          break;
        case 1:
          this.setText(this.file.getCreatedDate().toString());
          break;
      }
    }

    /**
     * @return the file
     */
    public RepositoryFile getFile() {
      return file;
    }
  }
  
  public class ContentSorter extends ColumnSorter {

    /* (non-Javadoc)
     * @see com.google.gwt.gen2.table.client.SortableGrid.ColumnSorter#onSortColumn(com.google.gwt.gen2.table.client.SortableGrid, com.google.gwt.gen2.table.client.TableModelHelper.ColumnSortList, com.google.gwt.gen2.table.client.SortableGrid.ColumnSorterCallback)
     */
    @Override
    public void onSortColumn(SortableGrid grid, ColumnSortList sortList, ColumnSorterCallback callback) {
      int column = sortList.getPrimaryColumn();
      boolean ascending = sortList.isPrimaryAscending();
      int rowCount = grid.getRowCount();
      List<FileAwareLabel> columnWidgets = new ArrayList<FileAwareLabel>(rowCount);
      for (int row=0; row<rowCount; row++) {
        columnWidgets.add((FileAwareLabel) grid.getWidget(row, column));
      }
      if (column == 1) {  // 1 is the date column
        if (ascending) {
          Collections.sort(columnWidgets, new Comparator<FileAwareLabel>() {
            @Override
            public int compare(FileAwareLabel o1, FileAwareLabel o2) {
              return o1.getFile().getCreatedDate().compareTo(o2.getFile().getCreatedDate());
            }
          });         
        } else {
          Collections.sort(columnWidgets, new Comparator<FileAwareLabel>() {
            @Override
            public int compare(FileAwareLabel o1, FileAwareLabel o2) {
              return o2.getFile().getCreatedDate().compareTo(o1.getFile().getCreatedDate());
            }
          });                  
        }
      } else {
        if (ascending) {
          Collections.sort(columnWidgets, new Comparator<FileAwareLabel>() {
            @Override
            public int compare(FileAwareLabel o1, FileAwareLabel o2) {
              return o1.getText().compareTo(o2.getText());
            }
          });         
        } else {
          Collections.sort(columnWidgets, new Comparator<FileAwareLabel>() {
            @Override
            public int compare(FileAwareLabel o1, FileAwareLabel o2) {
              return o2.getText().compareTo(o1.getText());
            }
          });                  
        }
      }
      List<Element> tdElems = new ArrayList<Element>(rowCount);
      for (FileAwareLabel lbl : columnWidgets) {
        tdElems.add(DOM.getParent(lbl.getElement()));
      }
      
      // Convert tdElems to trElems, reversing if needed
      Element[] trElems = new Element[rowCount];
      for (int i = 0; i < rowCount; i++) {
        trElems[i] = DOM.getParent(tdElems.get(i));
      }

      // Use the callback to complete the sorting
      callback.onSortingComplete(trElems);
    }  
  }

}
