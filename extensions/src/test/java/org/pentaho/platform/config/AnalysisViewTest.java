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


package org.pentaho.platform.config;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 10/20/15.
 */
public class AnalysisViewTest {
  @Test
  public void testGetInterval() throws Exception {
    assertEquals( 0.25D, AnalysisView.FIFTEEN_MINUTES.getInterval(), 0.0 );
  }

  @Test
  public void testGetPeriods() throws Exception {
    assertEquals( 60, AnalysisView.ONE_HOUR.getPeriods() );
  }
}
