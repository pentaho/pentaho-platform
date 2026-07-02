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

import org.jfree.data.general.DefaultValueDataset;

import java.awt.Font;

public abstract class WidgetDefinition extends DefaultValueDataset {

  /**
   * Test Commit
   */
  private static final long serialVersionUID = -3570099313517484430L;

  private double minimum = 0;

  private double maximum = 100;

  String noDataMessage = null;

  public WidgetDefinition( final double value, final double minimum, final double maximum ) {
    this.minimum = minimum;
    this.maximum = maximum;
    setValue( new Double( value ) );
  }

  /**
   * Gets the minimum value the widget can display
   * 
   * @return The minimum value the widget can display
   */
  public double getMinimum() {
    return minimum;
  }

  /**
   * Sets the minimum value the widget can display
   * 
   * @param minimum
   *          The minimum value the widget can display
   */

  public void setMinimum( final double minimum ) {
    this.minimum = minimum;
  }

  /**
   * Gets the maximum value the widget can display
   * 
   * @return The maximum value the widget can display
   */
  public double getMaximum() {
    return maximum;
  }

  /**
   * Sets the minimum value the widget can display
   * 
   * @param maximum
   *          The maximum value the widget can display
   */

  public void setMaximum( final double maximum ) {
    this.maximum = maximum;
  }

  public abstract Font getValueFont();

  public String getNoDataMessage() {
    return noDataMessage;
  }

}
