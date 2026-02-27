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

public class UnfileNonfolderObjects {

  public static final String UNFILE = "unfile"; //$NON-NLS-1$
  public static final String DELETESINGLEFILED = "deletesinglefiled"; //$NON-NLS-1$
  public static final String DELETE = "delete"; //$NON-NLS-1$

  private String value;

  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }

}
