/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core;

import org.pentaho.platform.api.engine.IFileInfo;

public class TestFileInfo implements IFileInfo {

  private String author;
  private String description;
  private String icon;
  private String title;
  private String displayType;

  public TestFileInfo( String title, String author, String description, String icon, String displayType ) {
    this.title = title;
    this.author = author;
    this.description = description;
    this.icon = icon;
    this.displayType = displayType;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor( String author ) {
    this.author = author;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon( String icon ) {
    this.icon = icon;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }

  public String getDisplayType() {
    return displayType;
  }

  public void setDisplayType( String displayType ) {
    this.displayType = displayType;
  }

}
