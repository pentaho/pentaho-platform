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

public class PropertiesFolder {

  public static final String OBJECTID = "ObjectId"; //$NON-NLS-1$
  public static final String URI = "Uri"; //$NON-NLS-1$
  public static final String OBJECTTYPEID = "ObjectTypeId"; //$NON-NLS-1$
  public static final String CREATEDBY = "CreatedBy"; //$NON-NLS-1$
  public static final String CREATIONDATE = "CreationDate"; //$NON-NLS-1$
  public static final String LASTMODIFIEDBY = "LastModifiedBy"; //$NON-NLS-1$
  public static final String LASTMODIFICATIONDATE = "LastModificationDate"; //$NON-NLS-1$
  public static final String CHANGETOKEN = "ChangeToken"; //$NON-NLS-1$
  public static final String PARENTID = "ParentId"; //$NON-NLS-1$
  public static final String ALLOWEDCHILDOBJECTTYPEIDS = "AllowedChildObjectTypeIds"; //$NON-NLS-1$

  private String value;

  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }
}
