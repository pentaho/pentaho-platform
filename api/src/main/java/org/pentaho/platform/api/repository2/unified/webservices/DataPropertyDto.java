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

public class DataPropertyDto implements Serializable {
  private static final long serialVersionUID = 4827387343270199835L;

  /**
   * DataPropertyType enum
   */
  private int type = -1;

  private String value;

  private String name;

  public DataPropertyDto() {
    super();
    // TODO Auto-generated constructor stub
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "DataPropertyDto [name=" + name + ", type=" + type + ", value=" + value + "]";
  }

  public int getType() {
    return type;
  }

  public void setType( int type ) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

}
