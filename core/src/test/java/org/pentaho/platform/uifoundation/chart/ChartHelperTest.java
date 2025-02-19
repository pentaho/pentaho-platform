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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.logging.SimpleLogger;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class ChartHelperTest {

  @Mock
  IParameterProvider parameterProvider;
  ILogger logger;

  @Before
  public void setUp() {
    logger = new SimpleLogger( ChartHelperTest.class );
    when( parameterProvider.getStringParameter( anyString(), any() ) )
      .thenThrow( new RuntimeException( "Failing on purpose, only testing deprecate warning" ) );
  }

  @Test
  public void doChartDeprecateWarningTest() {
    try ( MockedStatic<Logger> staticLogger = Mockito.mockStatic( Logger.class ) ) {
      try {
        ChartHelper.doChart( "testActionPath", parameterProvider,
          new StringBuffer( "testStringBuffer" ), new StandaloneSession(), new ArrayList<>(), logger );
      } catch ( Exception e ) {
        //do nothing, purpose of test just to confirm deprecation warning
      }
      //Verify chartHelper.deprecateWarning was called by confirming the deprecation warning log was logged
      staticLogger.verify( () -> Logger.warn( eq( ChartHelper.class ), anyString() ),
        times( 1 ) );
    }
  }

  @Test
  public void doPieChartDeprecateWarningTest() {
    try ( MockedStatic<Logger> staticLogger = Mockito.mockStatic( Logger.class ) ) {
      try {
        ChartHelper.doPieChart( "testActionPath", parameterProvider,
          new StringBuffer( "testStringBuffer" ), new StandaloneSession(), new ArrayList<>(), logger );
      } catch ( Exception e ) {
        //do nothing, purpose of test just to confirm deprecation warning
      }
      //Verify chartHelper.deprecateWarning was called by confirming the deprecation warning log was logged
      staticLogger.verify( () -> Logger.warn( eq( ChartHelper.class ), anyString() ),
        times( 1 ) );
    }
  }

  @Test
  public void doDialDeprecateWarningTest() {
    try ( MockedStatic<Logger> staticLogger = Mockito.mockStatic( Logger.class ) ) {
      try {
        ChartHelper.doDial( "solutionName", "testActionPath", "chartName",
          parameterProvider, new StringBuffer( "testStringBuffer" ), new StandaloneSession(),
          new ArrayList<>(), logger );
      } catch ( Exception e ) {
        //do nothing, purpose of test just to confirm deprecation warning
      }
      //Verify chartHelper.deprecateWarning was called by confirming the deprecation warning log was logged
      staticLogger.verify( () -> Logger.warn( eq( ChartHelper.class ), anyString() ),
        times( 1 ) );
    }
  }
}
