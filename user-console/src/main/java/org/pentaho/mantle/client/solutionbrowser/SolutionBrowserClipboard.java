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
