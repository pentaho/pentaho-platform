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

/**
 * Created by jthomas on 5/6/2014.
 */
public class SolutionBrowserFile {

  private String id;
  private String name;
  private String path;

  public SolutionBrowserFile( String id, String name, String path ) {
    this.id = id;
    this.name = name;
    this.path = path;
  }

  public SolutionBrowserFile() {
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath( String path ) {
    this.path = path;
  }
}
