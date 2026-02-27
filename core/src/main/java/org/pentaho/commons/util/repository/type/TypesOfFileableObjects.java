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

public class TypesOfFileableObjects {

  public static final String DOCUMENTS = "documents"; //$NON-NLS-1$
  public static final String FOLDERS = "folders"; //$NON-NLS-1$
  public static final String POLICIES = "policies"; //$NON-NLS-1$
  public static final String ANY = "any"; //$NON-NLS-1$

  private String value = ANY;

  public TypesOfFileableObjects() {

  }

  public TypesOfFileableObjects( String value ) {
    setValue( value );
  }

  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }

}
