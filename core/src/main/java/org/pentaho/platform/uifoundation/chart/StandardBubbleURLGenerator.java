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
