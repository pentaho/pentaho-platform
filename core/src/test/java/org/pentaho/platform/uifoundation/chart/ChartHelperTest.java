/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
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
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.logging.SimpleLogger;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ChartHelper}.
 * <p>
 * The class under test is deprecated (BISERVER-12899), so referencing it here is intentional and
 * unavoidable: deprecated code still has to be covered for as long as it ships.
 */
@SuppressWarnings( { "deprecation", "java:S1874" } )
@RunWith( MockitoJUnitRunner.class )
public class ChartHelperTest {

  @Mock
  IParameterProvider parameterProvider;
  ILogger logger;

  @Before
  public void setUp() {
    logger = new SimpleLogger( ChartHelperTest.class );
    // the latch lives for the whole JVM, so it has to be cleared to keep the warning tests deterministic
    ChartHelper.QUERY_PARAMETER_ENABLED_WARNING_LOGGED.set( false );
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

  @Test
  public void isChartQueryParameterEnabledReturnsFalseByDefault() {
    final ISystemConfig systemConfig = Mockito.mock( ISystemConfig.class );
    when( systemConfig.getProperty( ChartHelper.CHART_QUERY_PARAMETER_ENABLED_PROPERTY, "false" ) )
      .thenReturn( "false" );

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      assertFalse( ChartHelper.isChartQueryParameterEnabled() );
    }
  }

  @Test
  public void isChartQueryParameterEnabledFailsClosedWithoutSystemConfig() {
    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( null );

      assertFalse( ChartHelper.isChartQueryParameterEnabled() );
    }
  }

  @Test
  public void isChartQueryParameterEnabledReturnsTrueWhenExplicitlyEnabled() {
    final ISystemConfig systemConfig = Mockito.mock( ISystemConfig.class );
    when( systemConfig.getProperty( ChartHelper.CHART_QUERY_PARAMETER_ENABLED_PROPERTY, "false" ) )
      .thenReturn( "true" );

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      assertTrue( ChartHelper.isChartQueryParameterEnabled() );
    }
  }

  @Test
  public void isChartQueryParameterEnabledIsCaseInsensitive() {
    final ISystemConfig systemConfig = Mockito.mock( ISystemConfig.class );
    when( systemConfig.getProperty( ChartHelper.CHART_QUERY_PARAMETER_ENABLED_PROPERTY, "false" ) )
      .thenReturn( "TRUE" );

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      assertTrue( ChartHelper.isChartQueryParameterEnabled() );
    }
  }

  @Test
  public void isChartQueryParameterEnabledIsReadOnEveryCall() {
    final ISystemConfig systemConfig = Mockito.mock( ISystemConfig.class );
    when( systemConfig.getProperty( ChartHelper.CHART_QUERY_PARAMETER_ENABLED_PROPERTY, "false" ) )
      .thenReturn( "false" );

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      ChartHelper.isChartQueryParameterEnabled();
      ChartHelper.isChartQueryParameterEnabled();

      // the property is not cached, so a configuration change is picked up without a class reload
      Mockito.verify( systemConfig, times( 2 ) )
        .getProperty( ChartHelper.CHART_QUERY_PARAMETER_ENABLED_PROPERTY, "false" );
    }
  }

  @Test
  public void chartQueryParameterIsIgnoredWhenDisabled() {
    final ILogger mockedLogger = Mockito.mock( ILogger.class );
    final ISystemConfig systemConfig = Mockito.mock( ISystemConfig.class );
    when( systemConfig.getProperty( ChartHelper.CHART_QUERY_PARAMETER_ENABLED_PROPERTY, "false" ) )
      .thenReturn( "false" );

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      final String resolved =
        ChartHelper.resolveChartQueryParameter( "select department, actual from QUADRANT_ACTUALS", mockedLogger );

      // the query is dropped, so the supplied statement will never be executed
      assertNull( resolved );
      Mockito.verify( mockedLogger ).error( anyString() );
    }
  }

  @Test
  public void chartQueryParameterIsKeptWhenEnabled() {
    final ILogger mockedLogger = Mockito.mock( ILogger.class );
    final ISystemConfig systemConfig = Mockito.mock( ISystemConfig.class );
    when( systemConfig.getProperty( ChartHelper.CHART_QUERY_PARAMETER_ENABLED_PROPERTY, "false" ) )
      .thenReturn( "true" );

    final String query = "select department, actual from QUADRANT_ACTUALS";

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      // the query is kept exactly as supplied
      assertEquals( query, ChartHelper.resolveChartQueryParameter( query, mockedLogger ) );
      Mockito.verifyNoInteractions( mockedLogger );
    }
  }

  @Test
  public void isChartQueryParameterEnabledDoesNotLog() {
    final ISystemConfig systemConfig = Mockito.mock( ISystemConfig.class );
    when( systemConfig.getProperty( ChartHelper.CHART_QUERY_PARAMETER_ENABLED_PROPERTY, "false" ) )
      .thenReturn( "true" );

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class );
          MockedStatic<Logger> staticLogger = Mockito.mockStatic( Logger.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      assertTrue( ChartHelper.isChartQueryParameterEnabled() );

      // reporting the flag state is the caller's concern, this method only answers the question
      staticLogger.verifyNoInteractions();
      assertFalse( ChartHelper.QUERY_PARAMETER_ENABLED_WARNING_LOGGED.get() );
    }
  }

  @Test
  public void chartQueryParameterResolutionWarnsOnlyOnceWhenEnabled() {
    final ILogger mockedLogger = Mockito.mock( ILogger.class );
    final ISystemConfig systemConfig = Mockito.mock( ISystemConfig.class );
    when( systemConfig.getProperty( ChartHelper.CHART_QUERY_PARAMETER_ENABLED_PROPERTY, "false" ) )
      .thenReturn( "true" );

    final String query = "select department, actual from QUADRANT_ACTUALS";

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class );
          MockedStatic<Logger> staticLogger = Mockito.mockStatic( Logger.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      ChartHelper.resolveChartQueryParameter( query, mockedLogger );
      ChartHelper.resolveChartQueryParameter( query, mockedLogger );

      // the latch keeps the deviation from the secure default visible without flooding the log
      staticLogger.verify( () -> Logger.warn( eq( ChartHelper.class ), anyString() ), times( 1 ) );
    }
  }

  @Test
  public void chartQueryParameterResolutionDoesNotWarnWhenDisabled() {
    final ILogger mockedLogger = Mockito.mock( ILogger.class );
    final ISystemConfig systemConfig = Mockito.mock( ISystemConfig.class );
    when( systemConfig.getProperty( ChartHelper.CHART_QUERY_PARAMETER_ENABLED_PROPERTY, "false" ) )
      .thenReturn( "false" );

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class );
          MockedStatic<Logger> staticLogger = Mockito.mockStatic( Logger.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( systemConfig );

      ChartHelper.resolveChartQueryParameter( "select department, actual from QUADRANT_ACTUALS", mockedLogger );

      // the secure default is not a deviation, so only the ignored query is reported
      staticLogger.verifyNoInteractions();
      Mockito.verify( mockedLogger ).error( anyString() );
    }
  }

  @Test
  public void chartQueryParameterResolutionIgnoresTheFlagWhenNoQueryIsSupplied() {
    final ILogger mockedLogger = Mockito.mock( ILogger.class );

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( ISystemConfig.class ) ).thenReturn( null );

      // no query was supplied, so the flag is never consulted and nothing is logged
      assertNull( ChartHelper.resolveChartQueryParameter( null, mockedLogger ) );
      Mockito.verifyNoInteractions( mockedLogger );
    }
  }
}
