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


package org.pentaho.platform.engine.core.solution;

import org.pentaho.platform.api.engine.IFileInfo;

public class FileInfo implements IFileInfo {

  private String title;

  private String description;

  private String author;

  private String icon;

  private String displayType;

  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor( String author ) {
    this.author = author;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon( String icon ) {
    this.icon = icon;
  }

  public String getDisplayType() {
    return displayType;
  }

  public void setDisplayType( String displayType ) {
    this.displayType = displayType;
  }

}
