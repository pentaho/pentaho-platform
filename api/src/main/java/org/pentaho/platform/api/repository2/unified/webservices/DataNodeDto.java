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


package org.pentaho.platform.api.repository2.unified.webservices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataNodeDto implements Serializable {
  private static final long serialVersionUID = -670354483372381494L;

  private String id;

  private String name;

  private List<DataNodeDto> childNodes = new ArrayList<DataNodeDto>( 0 );

  private List<DataPropertyDto> childProperties = new ArrayList<DataPropertyDto>( 0 );

  public DataNodeDto() {
    super();
    // TODO Auto-generated constructor stub
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "DataNodeDto [id=" + id + ", name=" + name + ", childNodes=" + childNodes + ", childProperties="
        + childProperties + "]";
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

  public List<DataNodeDto> getChildNodes() {
    return childNodes;
  }

  public void setChildNodes( List<DataNodeDto> childNodes ) {
    this.childNodes = childNodes;
  }

  public List<DataPropertyDto> getChildProperties() {
    return childProperties;
  }

  public void setChildProperties( List<DataPropertyDto> childProperties ) {
    this.childProperties = childProperties;
  }

}
