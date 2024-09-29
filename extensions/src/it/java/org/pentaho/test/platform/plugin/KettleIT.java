/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.plugin;

import static org.junit.Assert.assertArrayEquals;

import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.OutputStream;
import java.util.Map;

public class KettleIT extends BaseTest {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution";

  private SimpleParameterProvider parameterProvider;
  private SimpleOutputHandler outputHandler;
  private StandaloneSession session;

  @Override
  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  @Override
  public void setUp() {
    super.setUp();
    startTest();
    parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter( "type", "html" );
    OutputStream outputStream = getOutputStream( "KettleTest.testKettle", ".html" );
    assertNotNull( outputStream );
    outputHandler = new SimpleOutputHandler( outputStream, true );
    assertNotNull( outputHandler );
    session = new StandaloneSession( "test" );
    assertNotNull( session );
  }

  @Override
  public void tearDown() {
    finishTest();
    super.tearDown();
  }

  public void testKettleSuccessResult() {
    assertNotNull( session );
    IRuntimeContext context =
      run( "/test/etl/SampleTransformation.xaction", null, false, parameterProvider, outputHandler, session );
    assertEquals( IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() );

    assertSuccessResult( context, new Object[][] {
      { "Central", "Sales", "District Manager", "Hello, District Manager" },
      { "Central", "Sales", "Senior Sales Rep", "Hello, Senior Sales Rep" } } );
  }

  public void testKettleValidatationFailure() {
    IRuntimeContext context =
      run( "/test/etl/SampleTransformationInvalid.xaction", null, false, parameterProvider, outputHandler, session );
    assertEquals( IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() );

    assertFailResult( context );
  }

  public void testKettleMissingTransform() {
    IRuntimeContext context =
      run( "/test/etl/SampleTransformationMissingKTR.xaction", null, false, parameterProvider, outputHandler, session );
    assertEquals( IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() );

    assertFailResult( context );
  }

  private MemoryResultSet getResult( IRuntimeContext context ) {
    assertNotNull( context.getOutputNames() );
    assertEquals( 1, context.getOutputNames().size() );
    String outputName = (String) context.getOutputNames().iterator().next();
    assertEquals( "rule-result", outputName );

    MemoryResultSet result = (MemoryResultSet) context.getOutputParameter( outputName ).getValue();
    return result;
  }

  private void assertSuccessResult( IRuntimeContext context, Object[][] data ) {
    MemoryResultSet result = getResult( context );
    assertNotNull( result );
    assertEquals( data.length, result.getRowCount() );
    for ( int rowIndex = 0; rowIndex < data.length; rowIndex++ ) {
      Object[] row = data[rowIndex];
      assertArrayEquals( row, result.getDataRow( rowIndex ) );
    }
  }

  private void assertFailResult( IRuntimeContext context ) {
    MemoryResultSet result = getResult( context );
    assertNull( result );
  }
}
