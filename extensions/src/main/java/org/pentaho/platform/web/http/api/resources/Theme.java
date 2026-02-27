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


package org.pentaho.platform.web.http.api.resources;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Theme {

  private String id;
  private String name;

  public Theme() {
  }

  public Theme( String id, String name ) {
    this.id = id;
    this.name = name;
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

}
