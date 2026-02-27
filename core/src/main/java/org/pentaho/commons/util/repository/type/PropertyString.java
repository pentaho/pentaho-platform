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

public class PropertyString extends CmisProperty {

  private String value;

  public PropertyString( String name ) {
    super( name, new PropertyType( PropertyType.STRING ) );
  }

  public PropertyString( String name, String value ) {
    super( name, new PropertyType( PropertyType.STRING ) );
    this.value = value;
  }

  protected PropertyString( String name, PropertyType propertyType ) {
    super( name, propertyType );
  }

  @Override
  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }

}
