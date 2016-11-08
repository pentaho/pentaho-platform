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
