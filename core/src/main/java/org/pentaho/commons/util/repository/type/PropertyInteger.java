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

public class PropertyInteger extends CmisProperty {

  private Integer value;

  public PropertyInteger( String name ) {
    super( name, new PropertyType( PropertyType.INTEGER ) );
  }

  public PropertyInteger( String name, Integer value ) {
    super( name, new PropertyType( PropertyType.INTEGER ) );
    this.value = value;
  }

  @Override
  public Integer getValue() {
    return value;
  }

  public void setValue( Object value ) {
    this.value = (Integer) value;
  }

}
