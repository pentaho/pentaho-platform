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

import java.math.BigDecimal;

public class PropertyDecimal extends CmisProperty {

  private BigDecimal value;

  public PropertyDecimal( String name ) {
    super( name, new PropertyType( PropertyType.DECIMAL ) );
  }

  public PropertyDecimal( String name, BigDecimal value ) {
    super( name, new PropertyType( PropertyType.DECIMAL ) );
    this.value = value;
  }

  @Override
  public BigDecimal getValue() {
    return value;
  }

  public void setValue( Object value ) {
    this.value = (BigDecimal) value;
  }

}
