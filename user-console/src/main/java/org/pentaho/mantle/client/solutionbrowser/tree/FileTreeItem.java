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


package org.pentaho.mantle.client.solutionbrowser.tree;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;

public class FileTreeItem extends TreeItem {
  public String fileName;
  public String url;
  private RepositoryFile repositoryFile;

  public FileTreeItem() {
    super();
  }

  public FileTreeItem( Widget widget ) {
    super( widget );
  }

  /**
   * @param string
   */
  public FileTreeItem( String string ) {
    super( ( new SafeHtmlBuilder() ).appendEscaped( string ).toSafeHtml() );
    getElement().setId( string );
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName( String fileName ) {
    this.fileName = fileName;
  }

  public String getURL() {
    return url;
  }

  public void setURL( String url ) {
    this.url = url;
  }

  public RepositoryFile getRepositoryFile() {
    return this.repositoryFile;
  }

  public void setRepositoryFile( RepositoryFile repositoryFile ) {
    this.repositoryFile = repositoryFile;
  }
}
