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
 * Copyright 2006 - 2015 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.uifoundation.chart;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IParameterResolver;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.ActionSequenceJCRHelper;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.uifoundation.chart.ChartHelper.Error;
import org.pentaho.platform.uifoundation.chart.ChartHelper.LogWriter;
import org.pentaho.platform.util.logging.SimpleLogger;
import org.pentaho.platform.util.messages.MessagesBase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// "@RunWith" very important for verify invocations
@RunWith( JMockit.class )
public class ChartHelperTest {

  private static final ILogger logger = mock( ILogger.class );

  private static final String MESSAGE = "Message";

  // values
  private static final String DATA_ACTION = "dataAction";
  private static final String ACTION_PATH = "actionPath";
  private static final String CHART_TYPE_STR = "DotChart";
  private static final String DATASET_TYPE = "XYSeriesCollection";
  private static final String CONNECTION_NAME = "connectionName";
  private static final String QUERY_TEMPLATE = "testQueryTemplate";
  private static final String QUERY = "testQuery";

  // objects for comparing
  private static final IPentahoResultSet RS = mock( IPentahoResultSet.class );

  private static MockUp<MessagesBase> messagesBaseMock;
  private static MockUp<PentahoSystem> pentahoSystemMock;
  private static MockUp<ActionSequenceJCRHelper> actionSequenceJCRHelperMock;
  private static MockUp<PentahoConnectionFactory> pentahoConnectionFactoryMock;
  private static MockUp<TemplateUtil> templateUtilMock;

  private static final IPentahoConnection connection = mock( IPentahoConnection.class );

  private static final String ERROR_0003_INVALID_CHART_TYPE = LogWriter.KEY_PREFIX
      + Error.ERROR_0003_INVALID_CHART_TYPE;

  private static final IPentahoSession SESSION = new StandaloneSession( "system" );

  private static MockUp<MessagesBase> getMessagesBaseMock() {
    return new MockUp<MessagesBase>() {
      @Mock( invocations = 3 )
      public String getString( final String key, final Object... params ) {
        return key + Arrays.toString( params );
      }
    };
  }

  private static MockUp<PentahoSystem> getPentahoSystemMock() {
    return new MockUp<PentahoSystem>() {
      @SuppressWarnings( "unchecked" )
      @Mock( invocations = 1 )
      public <T> T get( Class<T> interfaceClass, final IPentahoSession session ) {
        MockUp<IMessageFormatter> mouckUp = new MockUp<IMessageFormatter>() {
          @Mock( invocations = 1 )
          public void formatErrorMessage( final String mimeType, final String title, final List<?> messages,
              final StringBuffer messageBuffer ) {
            messageBuffer.append( MESSAGE );
          }
        };
        return (T) mouckUp.getMockInstance();
      }
    };
  }

  private static MockUp<PentahoConnectionFactory> getPentahoConnectionFactoryMock() {
    return new MockUp<PentahoConnectionFactory>() {
      @Mock( invocations = 1 )
      public IPentahoConnection getConnection( final String datasourceType, final String connectStr,
          final IPentahoSession session, final ILogger logger ) throws Exception {
        when( connection.executeQuery( QUERY ) ).thenReturn( RS );
        return connection;
      }
    };
  }

  private static MockUp<TemplateUtil> getTemplateUtilMock() {
    return new MockUp<TemplateUtil>() {
      @Mock( invocations = 1 )
      public String applyTemplate( final String template, final Properties inputs, final IParameterResolver resolver ) {
        return QUERY;
      }
    };
  }

  private static MockUp<ActionSequenceJCRHelper> getActionSequenceJCRHelperMock() {
    return new MockUp<ActionSequenceJCRHelper>() {
      @Mock( invocations = 1 )
      public void $init( Invocation invocation, IPentahoSession session ) {
        // nothing
      }

      @Mock( invocations = 1 )
      public Document getSolutionDocument( final String documentPath, final RepositoryFilePermission actionOperation )
        throws DocumentException, FileNotFoundException {
        SAXReader reader = new SAXReader();
        InputStream inStrm = new FileInputStream( "test-res/solution/test/xml/flash_dotchart.xml" ); //$NON-NLS-1$
        return reader.read( inStrm );
      }
    };
  }

  @After
  public void after() {
    tearDown( messagesBaseMock );
    tearDown( pentahoSystemMock );
    tearDown( actionSequenceJCRHelperMock );
    tearDown( pentahoConnectionFactoryMock );
    tearDown( templateUtilMock );
  }

  private void tearDown( MockUp<?> mockUp ) {
    if ( mockUp != null ) {
      mockUp.tearDown(); // only method "after" can verify invocations
    }
  }

  @Test
  public void testCreateLogWriter() {
    messagesBaseMock = getMessagesBaseMock();
    pentahoSystemMock = getPentahoSystemMock();
    List<?> messages = Collections.emptyList();
    LogWriter logWriter = new LogWriter( SESSION, logger, messages );
    Assert.assertEquals( logWriter.userSession, SESSION );
    Assert.assertEquals( logWriter.getLogger(), logger );
    Assert.assertEquals( logWriter.messages, messages );

    Object[] params = new Object[] { "test", 1 };
    String expectedErrorStr = ERROR_0003_INVALID_CHART_TYPE + Arrays.toString( params );
    String errorStr = logWriter.logError( null, Error.ERROR_0003_INVALID_CHART_TYPE, params );
    verify( logger ).error( expectedErrorStr );
    Assert.assertEquals( errorStr, expectedErrorStr );

    Exception ex = new Exception();
    logWriter.logError( ex, Error.ERROR_0003_INVALID_CHART_TYPE, params );
    verify( logger ).error( expectedErrorStr, ex );

    logWriter = spy( new LogWriter( SESSION, new SimpleLogger( "test" ) {
      @Override
      public void error( String message ) {
        // for clear console
      }
    }, messages ) );
    when( logWriter.logError( null, Error.ERROR_0003_INVALID_CHART_TYPE, params ) ).thenReturn( expectedErrorStr );
    String message = logWriter.logErrorAndGetContent( Error.ERROR_0003_INVALID_CHART_TYPE, params );
    verify( logWriter ).logError( null, Error.ERROR_0003_INVALID_CHART_TYPE, params );
    Assert.assertEquals( message, MESSAGE );
  }

  private ChartHelper.Builder createBuilder( Map<String, Object> parameters ) {
    IParameterProvider parameterProvider = new SimpleParameterProvider( parameters );
    List<?> messages = Collections.emptyList();
    ChartHelper.Builder builder = new ChartHelper.Builder( ACTION_PATH, parameterProvider, SESSION, logger, messages );
    Assert.assertEquals( builder.actionPath, ACTION_PATH );
    Assert.assertEquals( builder.parameterProvider, parameterProvider );
    Assert.assertEquals( builder.userSession, SESSION );
    Assert.assertNotNull( builder.logWriter );
    return builder;
  }

  @Test
  public void testInitChartType() {
    actionSequenceJCRHelperMock = getActionSequenceJCRHelperMock();

    Map<String, Object> parameters = new HashMap<String, Object>();
    ChartHelper.Builder builder = createBuilder( parameters );

    builder.initChartType();
    Assert.assertEquals( CHART_TYPE_STR, builder.chartTypeStr );
    Assert.assertEquals( DATASET_TYPE, builder.datasetType );

    parameters.put( ChartDefinition.TYPE_NODE_NAME, CHART_TYPE_STR );
    builder.initChartType();
    Assert.assertEquals( ChartDefinition.CATEGORY_DATASET_STR, builder.datasetType );
  }

  @Test
  public void testGetValues() throws Exception {
    pentahoConnectionFactoryMock = getPentahoConnectionFactoryMock();
    templateUtilMock = getTemplateUtilMock();

    Map<String, Object> parameters = new HashMap<String, Object>();
    ChartHelper.Builder builder = createBuilder( parameters );
    builder.getValues( CONNECTION_NAME, QUERY_TEMPLATE );
    verify( connection ).executeQuery( QUERY );
  }

  @Test
  public void testInitContent() throws Exception {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put( ChartHelper.DATA_PROCESS, DATA_ACTION );

    ChartHelper.Builder builder = spy( createBuilder( parameters ) );
    doReturn( RS ).when( builder ).getValues( CONNECTION_NAME, QUERY_TEMPLATE );

    AbstractChartComponent chartComponent = mock( AbstractChartComponent.class );
    when( chartComponent.setDataAction( any( String.class ) ) ).thenReturn( true );
    builder.initContent( chartComponent );
    verify( chartComponent ).setDataAction( DATA_ACTION );

    when( chartComponent.setDataAction( DATA_ACTION ) ).thenReturn( true );
    parameters.put( ChartHelper.CONNECTION, CONNECTION_NAME );
    parameters.put( ChartHelper.QUERY, QUERY_TEMPLATE );
    builder.initContent( chartComponent );
    verify( chartComponent ).setDataAction( ACTION_PATH );
  }
}
