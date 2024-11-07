/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.commons.util.repository.type;

public class PropertyId extends PropertyString {

  public PropertyId( String name ) {
    super( name, new PropertyType( PropertyType.ID ) );
  }

  public PropertyId( String name, String value ) {
    super( name, new PropertyType( PropertyType.ID ) );
    setValue( value );
  }

}
