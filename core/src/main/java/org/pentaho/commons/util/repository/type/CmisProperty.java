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

public abstract class CmisProperty {

  private String name;

  private PropertyType propertyType;

  public CmisProperty( String name, PropertyType propertyType ) {
    setName( name );
    setPropertyType( propertyType );
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public PropertyType getPropertyType() {
    return propertyType;
  }

  public void setPropertyType( PropertyType propertyType ) {
    this.propertyType = propertyType;
  }

  public abstract <T> T getValue();

}
