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
 * @created Nov 3, 2011 
 * @author wseyler
 */

package org.pentaho.mantle.client.solutionbrowser.fileproperties;

import java.util.List;
import java.util.Set;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.XMLToRepositoryFileTreeConverter;
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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
/**
 * @author wseyler
 *
 */
public class GeneratedContentPanel extends VerticalPanel implements IFileModifier {

  private String repositoryFilePath;
  private FixedWidthGrid dataTable;
  
  public GeneratedContentPanel(final String repositoryFilePath) {
    this.repositoryFilePath = repositoryFilePath;

    Toolbar toolbar = new HistoryToolbar();
    this.add(toolbar);

    FixedWidthFlexTable headerTable = new FixedWidthFlexTable();
    headerTable.setHTML(0, 0, Messages.getString("filename")); //$NON-NLS-1$
    headerTable.setHTML(0, 1,  Messages.getString("executed")); //$NON-NLS-1$
    headerTable.setWidth("100%"); //$NON-NLS-1$

    dataTable = new FixedWidthGrid();
    dataTable.setWidth("100%"); //$NON-NLS-1$
    ScrollTable scrollTable = new ScrollTable(dataTable, headerTable);
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
    try {
      builder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), exception.getLocalizedMessage(), false, false, true); //$NON-NLS-1$
          dialogBox.center();
        }

        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == Response.SC_OK) {
            List<RepositoryFile> repositoryFiles = XMLToRepositoryFileTreeConverter.getFileListFromXml(response.getText());

            dataTable.resize(repositoryFiles.size(), 2);
            for (int row=0; row<repositoryFiles.size(); row++) {
              dataTable.setWidget(row, 0, new FileAwaredLabel(repositoryFiles.get(row), 0));
              dataTable.setWidget(row, 1, new FileAwaredLabel(repositoryFiles.get(row), 1));
            }
            dataTable.sortColumn(1);
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
  
  private class FileAwaredLabel extends Label {
    private RepositoryFile file;
    
    public FileAwaredLabel(RepositoryFile file, int column) {
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
        RepositoryFile repoFile = ((FileAwaredLabel)dataTable.getWidget(i, 0)).getFile();
        SolutionBrowserPanel.getInstance().openFile(repoFile, COMMAND.RUN);
      }
    } 
  }

}
