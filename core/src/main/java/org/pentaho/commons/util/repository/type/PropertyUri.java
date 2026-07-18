/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.commons.util.repository.type;

public class PropertyUri extends PropertyString {

  public PropertyUri( String name ) {
    super( name, new PropertyType( PropertyType.URI ) );
  }

  public PropertyUri( String name, String value ) {
    super( name, new PropertyType( PropertyType.URI ) );
    setValue( value );
  }

}
