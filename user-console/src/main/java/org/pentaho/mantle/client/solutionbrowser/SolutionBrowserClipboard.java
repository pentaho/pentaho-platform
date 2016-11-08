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

/**
 * @author wseyler
 */
public class SolutionBrowserClipboard {
  public enum ClipboardAction {
    COPY, CUT
  }

  private String mimeType;
  private List<SolutionBrowserFile> clipboardItems;
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

  public List<SolutionBrowserFile> getClipboardItems() {
    return clipboardItems;
  }

  public void setClipboardItemsForCut( List<SolutionBrowserFile> clipboardItems ) {
    this.clipboardItems = clipboardItems;
    clipboardAction = ClipboardAction.CUT;
  }

  public void setClipboardItemsByIdForCopy( List<SolutionBrowserFile> clipboardItems ) {
    this.clipboardItems = clipboardItems;
    clipboardAction = ClipboardAction.COPY;
  }

  public Boolean hasContent() {
    return clipboardItems != null;
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

  /**
   *
   */
  public void clear() {
    this.clipboardItems = null;
    this.mimeType = null;
  }

}
