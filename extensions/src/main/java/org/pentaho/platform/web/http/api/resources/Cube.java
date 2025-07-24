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
import java.io.Serializable;

@XmlRootElement
public class Cube implements Serializable {

  private static final long serialVersionUID = -8982531446305037088L;

  private String catName;
  private String name;
  private String id;

  public Cube() {
  }

  public Cube( String catName, String name, String id ) {
    this.catName = catName;
    this.name = name;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getCatName() {
    return catName;
  }

  public void setCatName( String catName ) {
    this.catName = catName;
  }
}
