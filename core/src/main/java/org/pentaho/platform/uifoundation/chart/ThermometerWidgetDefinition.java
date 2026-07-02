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


package org.pentaho.platform.uifoundation.chart;

import java.awt.Font;

public class ThermometerWidgetDefinition extends WidgetDefinition {

  /**
   * 
   */
  private static final long serialVersionUID = 2902723672393895969L;

  // TODO make this work in a similar way to DialWidgetDefinition
  public ThermometerWidgetDefinition( final double value, final double minimum, final double maximum ) {
    super( value, minimum, maximum );
  }

  @Override
  public Font getValueFont() {
    return null;
  }

}
