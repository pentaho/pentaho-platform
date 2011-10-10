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
 * @created Mar 24, 2011 
 * @author wseyler
 */


package org.pentaho.mantle.client.commands;

import java.util.List;

import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserClipboard;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

/**
 * @author wseyler
 *
 */
public class CutFilesCommand extends AbstractCommand {
  private List<FileItem> repositoryFiles;
  
  /**
   * @param selectedItems
   */
  public CutFilesCommand(List<FileItem> selectedItems) {
    super();
    this.repositoryFiles = selectedItems;
  }

  /* (non-Javadoc)
   * @see org.pentaho.mantle.client.commands.AbstractCommand#performOperation()
   */
  @Override
  protected void performOperation() {
    performOperation(false);
  }

  /* (non-Javadoc)
   * @see org.pentaho.mantle.client.commands.AbstractCommand#performOperation(boolean)
   */
  @Override
  protected void performOperation(boolean feedback) {
    SolutionBrowserClipboard clipBoard = SolutionBrowserPanel.getInstance().getClipboard();
    clipBoard.setDataForCut(repositoryFiles);
    clipBoard.setMimeType("jcrFiles/list");
  }

}
