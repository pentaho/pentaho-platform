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

import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.data.xy.XYDataset;

public class StandardBubbleURLGenerator extends StandardXYURLGenerator {

  /**
   *
   */
  private static final long serialVersionUID = -8866266240558511140L;

  private final String seriesParameterName = "series"; //$NON-NLS-1$ 

  public StandardBubbleURLGenerator() {
    super();
  }

  /**
   * Generates a URL for a particular item within a series.
   * 
   * @param dataset
   *          the dataset.
   * @param series
   *          the series number (zero-based index).
   * @param item
   *          the item number (zero-based index).
   * 
   * @return The generated URL.
   */
  @Override
  public String generateURL( final XYDataset dataset, final int series, final int item ) {
    String seriesKey = (String) dataset.getSeriesKey( series );
    String url = seriesParameterName + "=" + seriesKey; //$NON-NLS-1$
    return url;
  }
}
