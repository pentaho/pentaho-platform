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

public class PropertyBoolean extends CmisProperty {

  private Boolean value;

  public PropertyBoolean( String name ) {
    super( name, new PropertyType( PropertyType.BOOLEAN ) );
  }

  public PropertyBoolean( String name, boolean value ) {
    super( name, new PropertyType( PropertyType.BOOLEAN ) );
    this.value = value;
  }

  public Boolean getValue() {
    return value;
  }

  public void setValue( Object value ) {
    this.value = (Boolean) value;
  }

}
