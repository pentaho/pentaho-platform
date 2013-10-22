/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

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
