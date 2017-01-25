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
