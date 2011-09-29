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
 * @created Mar 28, 2011 
 * @author wseyler
 */


package org.pentaho.mantle.client.solutionbrowser;

import java.util.List;

import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

/**
 * @author wseyler
 *
 */
public class SolutionBrowserClipboard {
  public enum ClipboardAction {COPY, CUT}
  private String mimeType;
  private Object data;
  private ClipboardAction clipboardAction;
  
  public SolutionBrowserClipboard() {
    super();
  }

  public String getMimeType() {
    return mimeType;
  }
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
  
  public Object getData() {
    return data;
  }
  
  public void setDataForCut(Object data) {
    clearCutStyling();
    this.data = data;
    clipboardAction = ClipboardAction.CUT;
    applyCutStyling();
  }
  
  public void setDataForCopy(Object data) {
    clearCutStyling();
    this.data = data;
    clipboardAction = ClipboardAction.COPY;
  }
  
  public Boolean hasContent() {
    return data != null;
  }

  /**
   * @param clipboardAction the clipboardAction to set
   */
  public void setClipboardAction(ClipboardAction clipboardAction) {
    this.clipboardAction = clipboardAction;
  }

  /**
   * @return the clipboardAction
   */
  public ClipboardAction getClipboardAction() {
    return clipboardAction;
  }
  
  private void clearCutStyling() {
    if (hasContent() && clipboardAction == ClipboardAction.CUT) {
      if (data instanceof List<?>) {
        List<FileItem> values = (List<FileItem>) data;
        for(FileItem fileItem : values) {
          fileItem.setStyleName("fileLabel"); //$NON-NLS-1$
        }
      }
    }
  }
  
  private void applyCutStyling() {
    if (hasContent() && clipboardAction == ClipboardAction.CUT) {
      if (data instanceof List<?>) {
        List<FileItem> values = (List<FileItem>) data;
        for(FileItem fileItem : values) {
          fileItem.setStyleName("fileLabelCutSelected"); //$NON-NLS-1$
        }
      }      
    }
  }

  /**
   * 
   */
  public void clear() {
    this.data = null;
    this.mimeType = null;
  }
  
}
