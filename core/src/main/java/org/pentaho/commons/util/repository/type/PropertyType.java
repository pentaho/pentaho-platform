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


package org.pentaho.commons.util.repository.type;

public class PropertyType {

  public static final String BOOLEAN = "boolean"; //$NON-NLS-1$
  public static final String ID = "id"; //$NON-NLS-1$
  public static final String INTEGER = "integer"; //$NON-NLS-1$
  public static final String DATETIME = "datetime"; //$NON-NLS-1$
  public static final String DECIMAL = "decimal"; //$NON-NLS-1$
  public static final String HTML = "html"; //$NON-NLS-1$
  public static final String STRING = "string"; //$NON-NLS-1$
  public static final String URI = "uri"; //$NON-NLS-1$
  public static final String XML = "xml"; //$NON-NLS-1$

  private String type;

  public PropertyType( String type ) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

}
