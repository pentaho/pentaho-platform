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

package org.pentaho.mantle.client.solutionbrowser;

import java.util.List;

import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

/**
 * @author wseyler
 * 
 */
public class SolutionBrowserClipboard {
  public enum ClipboardAction {
    COPY, CUT
  }

  private String mimeType;
  private List<FileItem> data;
  private ClipboardAction clipboardAction;

  private static SolutionBrowserClipboard instance = new SolutionBrowserClipboard();

  public static SolutionBrowserClipboard getInstance() {
    return instance;
  }

  private SolutionBrowserClipboard() {
    super();
  }

  public String getMimeType() {
    return mimeType;
  }
  public void setMimeType( String mimeType ) {
    this.mimeType = mimeType;
  }

  public Object getData() {
    return data;
  }

  public void setDataForCut(List<FileItem> data) {
    clearCutStyling();
    this.data = data;
    clipboardAction = ClipboardAction.CUT;
    applyCutStyling();
  }

  public void setDataForCopy(List<FileItem> data) {
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
  public void setClipboardAction( ClipboardAction clipboardAction ) {
    this.clipboardAction = clipboardAction;
  }

  /**
   * @return the clipboardAction
   */
  public ClipboardAction getClipboardAction() {
    return clipboardAction;
  }

  private void clearCutStyling() {
    if ( hasContent() && clipboardAction == ClipboardAction.CUT ) {
      if ( data instanceof List<?> ) {
        @SuppressWarnings( "unchecked" )
        List<FileItem> values = data;
        for ( FileItem fileItem : values ) {
          fileItem.setStyleName( "fileLabel" ); //$NON-NLS-1$
        }
      }
    }
  }

  private void applyCutStyling() {
    if ( hasContent() && clipboardAction == ClipboardAction.CUT ) {
      if ( data instanceof List<?> ) {
        @SuppressWarnings( "unchecked" )
        List<FileItem> values = (List<FileItem>) data;
        for ( FileItem fileItem : values ) {
          fileItem.setStyleName( "fileLabelCutSelected" ); //$NON-NLS-1$
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
