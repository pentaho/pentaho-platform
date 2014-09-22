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

package org.pentaho.platform.engine.services;

import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings( { "all" } )
public class PojoComponentTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-res/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public static boolean doneCalled = false;
  public static boolean executeCalled = false;
  public static boolean validateCalled = false;
  public static boolean setSessionCalled = false;
  public static boolean setLoggerCalled = false;
  public static boolean setResourceInputStreamCalled = false;
  public static boolean setActionSequenceResourceCalled = false;

  public void testSimplePojoInput() {
    startTest();
    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    try {
      String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo1a.xaction" );
      PojoComponentTest.doneCalled = false;
      PojoComponentTest.setSessionCalled = false;
      PojoComponentTest.setLoggerCalled = false;
      TestPojo1.int1 = 0;
      TestPojo1.int2 = null;
      SimpleParameterProvider inputs = new SimpleParameterProvider();
      inputs.setParameter( "int2", new Integer( 22 ) );
      inputs.setParameter( "bool2", new Boolean( true ) );
      inputs.setParameter( "long2", new Long( 99 ) );
      inputs.setParameter( "bigdecimal", new BigDecimal( "77.7" ) );
      inputs.setParameter( "float2", new Float( 44.4 ) );
      inputs.setParameter( "double2", new Double( 66.6 ) );
      Map providers = new HashMap();
      providers.put( IParameterProvider.SCOPE_REQUEST, inputs );
      IRuntimeContext runtimeContext =
          solutionEngine
              .execute(
                  xactionStr,
                  "test1a.xaction", "empty action sequence test", false, true, null, false, providers, null, null, new SimpleUrlFactory( "" ), new ArrayList() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull( "RuntimeContext is null", runtimeContext );
      IActionParameter param = runtimeContext.getOutputParameter( "output1" );
      assertNotNull( "param is null", param );
      assertEquals( "setting is wrong", "value1", TestPojo1.setting1 );
      assertEquals( "setting is wrong", "value2", TestPojo1.setting2 );
      assertEquals( "setting is wrong", null, TestPojo1.setting3 );
      assertEquals( "param is wrong", "abcdeabcde", param.getValue() );
      assertEquals( "setInt2 failed", new Integer( 22 ), TestPojo1.int2 );
      assertEquals( "setBoolean2 failed", new Boolean( true ), TestPojo1.bool2 );
      assertEquals( "setLong2 failed", new Long( 99 ), TestPojo1.long2 );
      assertEquals( "setBigDecimal failed", new BigDecimal( "77.7" ), TestPojo1.bigDecimal );
      assertEquals( "setFloat2 failed", "44.4", TestPojo1.float2.toString() );
      assertEquals( "setDouble2 failed", "66.6", TestPojo1.double2.toString() );
      assertTrue( "done() was not called", PojoComponentTest.doneCalled );
      assertTrue( "setSession() was not called", PojoComponentTest.setSessionCalled );
      assertTrue( "setLogger() was not called", PojoComponentTest.setLoggerCalled );
      assertEquals( "Action sequence execution failed", runtimeContext.getStatus(),
          IRuntimeContext.RUNTIME_STATUS_SUCCESS );
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    }
    finishTest();
  }

  public void testSimplePojoInputFormat2() {
    // test the alternate action definition format
    startTest();
    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    try {
      String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo1-alternate.xaction" );
      PojoComponentTest.doneCalled = false;
      PojoComponentTest.setSessionCalled = false;
      PojoComponentTest.setLoggerCalled = false;
      TestPojo1.int1 = 0;
      TestPojo1.int2 = null;
      SimpleParameterProvider inputs = new SimpleParameterProvider();
      inputs.setParameter( "int2", new Integer( 22 ) );
      inputs.setParameter( "bool2", new Boolean( true ) );
      inputs.setParameter( "long2", new Long( 99 ) );
      inputs.setParameter( "bigdecimal", new BigDecimal( "77.7" ) );
      inputs.setParameter( "float2", new Float( 44.4 ) );
      inputs.setParameter( "double2", new Double( 66.6 ) );
      Map providers = new HashMap();
      providers.put( IParameterProvider.SCOPE_REQUEST, inputs );
      IRuntimeContext runtimeContext =
          solutionEngine
              .execute(
                  xactionStr,
                  "test1a.xaction", "empty action sequence test", false, true, null, false, providers, null, null, new SimpleUrlFactory( "" ), new ArrayList() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull( "RuntimeContext is null", runtimeContext );
      IActionParameter param = runtimeContext.getOutputParameter( "output1" );
      assertNotNull( "param is null", param );
      assertEquals( "setting is wrong", "value1", TestPojo1.setting1 );
      assertEquals( "setting is wrong", "value2", TestPojo1.setting2 );
      assertEquals( "setting is wrong", null, TestPojo1.setting3 );
      assertEquals( "param is wrong", "abcdeabcde", param.getValue() );
      assertEquals( "setInt2 failed", new Integer( 22 ), TestPojo1.int2 );
      assertEquals( "setBoolean2 failed", new Boolean( true ), TestPojo1.bool2 );
      assertEquals( "setLong2 failed", new Long( 99 ), TestPojo1.long2 );
      assertEquals( "setBigDecimal failed", new BigDecimal( "77.7" ), TestPojo1.bigDecimal );
      assertEquals( "setFloat2 failed", "44.4", TestPojo1.float2.toString() );
      assertEquals( "setDouble2 failed", "66.6", TestPojo1.double2.toString() );
      assertTrue( "done() was not called", PojoComponentTest.doneCalled );
      assertTrue( "setSession() was not called", PojoComponentTest.setSessionCalled );
      assertTrue( "setLogger() was not called", PojoComponentTest.setLoggerCalled );
      assertEquals( "Action sequence execution failed", runtimeContext.getStatus(),
          IRuntimeContext.RUNTIME_STATUS_SUCCESS );
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    }
    finishTest();
  }

  public void testSimplePojoSettings() {
    startTest();
    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    try {
      String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo1b.xaction" );
      PojoComponentTest.doneCalled = false;
      IRuntimeContext runtimeContext =
          solutionEngine
              .execute(
                  xactionStr,
                  "pojo1b.xaction", "empty action sequence test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory( "" ), new ArrayList() ); //$NON-NLS-1$ //$NON-NLS-2$
      IActionParameter param = runtimeContext.getOutputParameter( "output1" );
      assertNotNull( "RuntimeContext is null", runtimeContext );
      assertTrue( "done() was not called", PojoComponentTest.doneCalled );
      assertEquals( "setInt1 failed", 11, TestPojo1.int1 );
      assertEquals( "setInt2 failed", new Integer( 22 ), TestPojo1.int2 );
      assertEquals( "setBoolean1 failed", true, TestPojo1.bool1 );
      assertEquals( "setBoolean2 failed", new Boolean( true ), TestPojo1.bool2 );
      assertEquals( "setLong1 failed", 88, TestPojo1.long1 );
      assertEquals( "setLong2 failed", new Long( 99 ), TestPojo1.long2 );
      assertEquals( "setBigDecimal failed", new BigDecimal( "77.7" ), TestPojo1.bigDecimal );
      assertEquals( "setFloat1 failed", "33.3", Float.toString( TestPojo1.float1 ) );
      assertEquals( "setFloat2 failed", "44.4", TestPojo1.float2.toString() );
      assertEquals( "setDouble1 failed", "55.5", Double.toString( TestPojo1.double1 ) );
      assertEquals( "setDouble2 failed", "66.6", TestPojo1.double2.toString() );
      assertEquals( "Action sequence execution failed", runtimeContext.getStatus(),
          IRuntimeContext.RUNTIME_STATUS_SUCCESS );
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    }
    finishTest();
  }

  public void testSimplestCase() {
    startTest();
    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    try {
      String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo4.xaction" );
      PojoComponentTest.doneCalled = false;
      IRuntimeContext runtimeContext =
          solutionEngine
              .execute(
                  xactionStr,
                  "pojo4.xaction", "empty action sequence test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory( "" ), new ArrayList() ); //$NON-NLS-1$ //$NON-NLS-2$
      IActionParameter param = runtimeContext.getOutputParameter( "output1" );
      assertNotNull( "RuntimeContext is null", runtimeContext );
      assertNotNull( "param is null", param );
      assertEquals( "abcdeabcde", param.getValue().toString() );
      assertEquals( "done() was called", false, PojoComponentTest.doneCalled );
      assertEquals( "Action sequence execution failed", runtimeContext.getStatus(),
          IRuntimeContext.RUNTIME_STATUS_SUCCESS );
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    }
    finishTest();
  }

  public void testRuntimeInputsAndOutputs() {
    startTest();
    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    try {
      String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo5.xaction" );
      IRuntimeContext runtimeContext =
          solutionEngine
              .execute(
                  xactionStr,
                  "pojo5.xaction", "empty action sequence test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory( "" ), new ArrayList() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull( "RuntimeContext is null", runtimeContext );
      assertEquals( "Action sequence execution failed", runtimeContext.getStatus(),
          IRuntimeContext.RUNTIME_STATUS_SUCCESS );
      IActionParameter param = runtimeContext.getOutputParameter( "output1" );
      assertNotNull( "param is null", param );
      assertEquals( "hello", param.getValue().toString() );
      param = runtimeContext.getOutputParameter( "output2" );
      assertNotNull( "param is null", param );
      assertEquals( "world", param.getValue().toString() );
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    }
    finishTest();
  }

  public void testMissingClassSetting() {
    startTest();
    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    try {
      String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo-bad1.xaction" );
      PojoComponentTest.doneCalled = false;
      PojoComponentTest.executeCalled = false;
      PojoComponentTest.validateCalled = false;
      IRuntimeContext runtimeContext =
          solutionEngine
              .execute(
                  xactionStr,
                  "test", "invalid class setting test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory( "" ), new ArrayList() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull( "RuntimeContext is null", runtimeContext );
      assertEquals( "execute was called", false, PojoComponentTest.executeCalled );
      assertEquals( "validate was called", false, PojoComponentTest.validateCalled );
      assertEquals( "Action sequence validation succeeded", runtimeContext.getStatus(),
          IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL );
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    }
    finishTest();
  }

  public void testBadClassSetting() {
    startTest();
    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    try {
      String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo-bad2.xaction" );
      PojoComponentTest.doneCalled = false;
      PojoComponentTest.executeCalled = false;
      PojoComponentTest.validateCalled = false;
      IRuntimeContext runtimeContext =
          solutionEngine
              .execute(
                  xactionStr,
                  "test", "invalid class setting test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory( "" ), new ArrayList() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull( "RuntimeContext is null", runtimeContext );
      assertEquals( "execute was called", false, PojoComponentTest.executeCalled );
      assertEquals( "validate was called", false, PojoComponentTest.validateCalled );
      assertEquals( "Action sequence validation succeeded", runtimeContext.getStatus(),
          IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL );
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    }
    finishTest();
  }

  public void testBadValidate() {
    startTest();
    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    try {
      String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo-bad3.xaction" );
      PojoComponentTest.doneCalled = false;
      PojoComponentTest.executeCalled = false;
      PojoComponentTest.validateCalled = false;
      IRuntimeContext runtimeContext =
          solutionEngine
              .execute(
                  xactionStr,
                  "test", "invalid class setting test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory( "" ), new ArrayList() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull( "RuntimeContext is null", runtimeContext );
      assertEquals( "execute was called", false, PojoComponentTest.executeCalled );
      assertEquals( "validate was not called", true, PojoComponentTest.validateCalled );
      assertEquals( "Action sequence execution succeeded", runtimeContext.getStatus(),
          IRuntimeContext.RUNTIME_STATUS_FAILURE );
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    }
    finishTest();
  }

  /**
   * Test that unused parameters being passed in do not impact the running of the PojoComponent. A warning is
   * written to the log for user feedback on execution.
   */
  public void testUnusedInput() {
    startTest();
    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    try {
      String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo-bad4.xaction" );
      PojoComponentTest.doneCalled = false;
      PojoComponentTest.executeCalled = false;
      PojoComponentTest.validateCalled = false;
      IRuntimeContext runtimeContext =
          solutionEngine
              .execute(
                  xactionStr,
                  "test", "invalid class setting test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory( "" ), new ArrayList() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull( "RuntimeContext is null", runtimeContext );
      assertEquals( "Action sequence succeeded", IRuntimeContext.RUNTIME_STATUS_SUCCESS, runtimeContext.getStatus() );
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    }
    finishTest();
  }

  public void testBadOutput() {
    startTest();
    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    try {
      String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo-bad5.xaction" );
      PojoComponentTest.doneCalled = false;
      PojoComponentTest.executeCalled = false;
      PojoComponentTest.validateCalled = false;
      IRuntimeContext runtimeContext =
          solutionEngine
              .execute(
                  xactionStr,
                  "test", "invalid class setting test", false, true, null, false, new HashMap(), null, null, new SimpleUrlFactory( "" ), new ArrayList() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertNotNull( "RuntimeContext is null", runtimeContext );
      assertEquals( "Action sequence succeeded", runtimeContext.getStatus(), IRuntimeContext.RUNTIME_STATUS_FAILURE );
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    }
    finishTest();
  }

  public void testStreamingPojo() {
    String instanceId = null;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    SimpleOutputHandler outputHandler = new SimpleOutputHandler( out, false );
    outputHandler.setOutputPreference( IOutputHandler.OUTPUT_TYPE_DEFAULT );

    startTest();
    IPentahoSession session = new StandaloneSession( "system" );
    ISolutionEngine solutionEngine = ServiceTestHelper.getSolutionEngine();
    if ( outputHandler != null ) {
      outputHandler.setSession( session );
    }
    try {
      String xactionStr = ServiceTestHelper.getXAction( SOLUTION_PATH, "test/pojo/pojo2.xaction" );
      PojoComponentTest.setActionSequenceResourceCalled = false;
      IRuntimeContext runtimeContext =
          solutionEngine
              .execute(
                  xactionStr,
                  "test1.xaction", "empty action sequence test", false, true, null, false, new HashMap(), outputHandler, null, new SimpleUrlFactory( "" ), new ArrayList() ); //$NON-NLS-1$ //$NON-NLS-2$
      IActionParameter param = runtimeContext.getOutputParameter( "outputstream" );
      assertNotNull( "RuntimeContext is null", runtimeContext );
      assertEquals( "Action sequence execution failed", runtimeContext.getStatus(),
          IRuntimeContext.RUNTIME_STATUS_SUCCESS );
      assertTrue( "setResource was not called", PojoComponentTest.setResourceInputStreamCalled );
      assertTrue( "setResource was not called", PojoComponentTest.setActionSequenceResourceCalled );
    } catch ( Exception e ) {
      // we should not get here
      e.printStackTrace();
      assertTrue( e.getMessage(), false );
    }

    String output = new String( out.toByteArray() );
    assertEquals( "outputstream", "abcdeabcde", output );
    finishTest();
  }

}
