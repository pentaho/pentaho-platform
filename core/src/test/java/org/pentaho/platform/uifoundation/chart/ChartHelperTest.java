/*!
 *
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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */
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
