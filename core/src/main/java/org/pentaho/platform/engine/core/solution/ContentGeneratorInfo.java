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

import org.pentaho.platform.api.engine.IContentGeneratorInfo;

public class ContentGeneratorInfo implements IContentGeneratorInfo {

  private String description;

  private String id;

  private String title;

  private String url;

  private String type;

  private String classname;

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }

  @Deprecated
  public String getUrl() {
    return url;
  }

  @Deprecated
  public void setUrl( String url ) {
    this.url = url;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public String getClassname() {
    return classname;
  }

  public void setClassname( String classname ) {
    this.classname = classname;
  }

  @Deprecated
  public String getFileInfoGeneratorClassname() {
    // do nothing, this method is deprecated and is no longer called by the platform
    return null;
  }
}
