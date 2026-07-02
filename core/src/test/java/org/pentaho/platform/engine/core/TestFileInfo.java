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
