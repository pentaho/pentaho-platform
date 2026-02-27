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

import java.util.Calendar;

public class PropertyDateTime extends CmisProperty {

  private Calendar value;

  public PropertyDateTime( String name ) {
    super( name, new PropertyType( PropertyType.DATETIME ) );
  }

  public PropertyDateTime( String name, Calendar value ) {
    super( name, new PropertyType( PropertyType.DATETIME ) );
    this.value = value;
  }

  @Override
  public Calendar getValue() {
    return value;
  }

  public void setValue( Object value ) {
    this.value = (Calendar) value;
  }

}
