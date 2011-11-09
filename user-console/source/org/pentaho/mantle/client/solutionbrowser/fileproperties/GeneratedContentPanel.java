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

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.XMLToRepositoryFileTreeConverter;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.gen2.table.client.FixedWidthFlexTable;
import com.google.gwt.gen2.table.client.FixedWidthGrid;
import com.google.gwt.gen2.table.client.ScrollTable;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
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

    String name = repositoryFilePath;
    if (repositoryFilePath.lastIndexOf(":") != -1) {
      name = repositoryFilePath.substring(repositoryFilePath.lastIndexOf(":")+1);
    }
    
    this.add(new Label(name));
    FixedWidthFlexTable headerTable = new FixedWidthFlexTable();
    headerTable.setHTML(0, 0, Messages.getString("filename")); //$NON-NLS-1$
    headerTable.setHTML(0, 1,  Messages.getString("executed")); //$NON-NLS-1$
    headerTable.setWidth("100%"); //$NON-NLS-1$

    dataTable = new FixedWidthGrid();
    dataTable.setWidth("100%"); //$NON-NLS-1$
    ScrollTable scrollTable = new ScrollTable(dataTable, headerTable);
    scrollTable.setSize("100%", "400px");  //$NON-NLS-1$//$NON-NLS-2$
    this.add(scrollTable);

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
              dataTable.setHTML(row, 0, repositoryFiles.get(row).getName());
              dataTable.setHTML(row, 1, repositoryFiles.get(row).getCreatedDate().toString());
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
  
}
