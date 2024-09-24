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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ISessionStartupAction;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.engine.core.system.boot.PentahoSystemBoot;
import org.pentaho.platform.util.logging.Logger;

@SuppressWarnings( { "all" } )
public class BootTest {

  @Test
  public void testBoot() throws Exception {
    PentahoSystemBoot boot = new PentahoSystemBoot();
    boot.setFilePath( "solution" );

    IPentahoObjectFactory factory = boot.getFactory();
    assertNotNull( "object factory is null", factory );

    assertTrue( "object factory not definable", factory instanceof IPentahoDefinableObjectFactory );

    boot.define( ISolutionEngine.class.getSimpleName(), Object1.class.getName(),
        IPentahoDefinableObjectFactory.Scope.GLOBAL );
    boot.define( "MyObject", Object1.class.getName(), IPentahoDefinableObjectFactory.Scope.GLOBAL );
    boot.define( "MyObject", Object2.class.getName(), IPentahoDefinableObjectFactory.Scope.GLOBAL );

    assertFalse( boot.isInitialized() );

    boolean ok = boot.start();

    assertNull( boot.getSettingsProvider() );

    assertTrue( boot.isInitialized() );
    assertTrue( ok );

    factory = boot.getFactory();
    Object2 object = factory.get( Object2.class, "MyObject", null );

    assertNotNull( "object get failed", object );

    assertEquals( "file path is wrong", "solution", boot.getFilePath() );

    boot.stop();
    assertFalse( boot.isInitialized() );
  }

  @Test
  public void testBootListeners() throws Exception {
    PentahoSystemBoot boot = new PentahoSystemBoot();
    boot.setFilePath( "src/test/resources/solution" );
    boot.define( ISolutionEngine.class.getSimpleName(), Object1.class.getName(),
        IPentahoDefinableObjectFactory.Scope.GLOBAL );

    TestLifecycleListener lifecycleListener1 = new TestLifecycleListener();
    TestLifecycleListener lifecycleListener2 = new TestLifecycleListener();
    boot.addLifecycleListener( lifecycleListener1 );

    List<IPentahoSystemListener> lifecycleListeners1 = boot.getLifecycleListeners();
    assertEquals( 1, lifecycleListeners1.size() );
    assertEquals( lifecycleListener1, lifecycleListeners1.get( 0 ) );
    assertFalse( TestLifecycleListener.startupCalled );
    assertFalse( TestLifecycleListener.shutdownCalled );

    List<IPentahoSystemListener> lifecycleListeners2 = new ArrayList<IPentahoSystemListener>();
    lifecycleListeners2.add( lifecycleListener2 );
    boot.setLifecycleListeners( lifecycleListeners2 );
    List<IPentahoSystemListener> lifecycleListeners3 = boot.getLifecycleListeners();
    assertEquals( 1, lifecycleListeners3.size() );
    assertEquals( lifecycleListener2, lifecycleListeners3.get( 0 ) );
    assertEquals( lifecycleListeners2, lifecycleListeners3 );

    IPentahoObjectFactory factory = boot.getFactory();
    assertNotNull( "object factory is null", factory );

    assertTrue( "object factory not definable", factory instanceof IPentahoDefinableObjectFactory );

    assertFalse( boot.isInitialized() );

    boolean ok = boot.start();

    assertNull( boot.getSettingsProvider() );

    assertTrue( boot.isInitialized() );
    assertTrue( ok );

    assertTrue( TestLifecycleListener.startupCalled );
    assertFalse( TestLifecycleListener.shutdownCalled );

    boot.stop();
    assertFalse( boot.isInitialized() );

    assertTrue( TestLifecycleListener.startupCalled );
    assertTrue( TestLifecycleListener.shutdownCalled );
  }

  @Test
  public void testBootActions() throws Exception {
    PentahoSystemBoot boot = new PentahoSystemBoot();
    boot.setFilePath( "src/test/resources/solution" );
    boot.define( ISolutionEngine.class.getSimpleName(), Object1.class.getName(),
        IPentahoDefinableObjectFactory.Scope.GLOBAL );

    TestStartupAction startupAction1 = new TestStartupAction();
    TestStartupAction startupAction2 = new TestStartupAction();
    boot.addStartupAction( startupAction1 );

    List<ISessionStartupAction> startupActions1 = boot.getStartupActions();
    assertEquals( 1, startupActions1.size() );
    assertEquals( startupAction1, startupActions1.get( 0 ) );

    List<ISessionStartupAction> startupActions2 = new ArrayList<ISessionStartupAction>();
    startupActions2.add( startupAction2 );
    boot.setStartupActions( startupActions2 );
    List<ISessionStartupAction> startupActions3 = boot.getStartupActions();
    assertEquals( 1, startupActions3.size() );
    assertEquals( startupAction2, startupActions3.get( 0 ) );
    assertEquals( startupActions2, startupActions3 );

    IPentahoObjectFactory factory = boot.getFactory();
    assertNotNull( "object factory is null", factory );

    assertTrue( "object factory not definable", factory instanceof IPentahoDefinableObjectFactory );

    assertFalse( boot.isInitialized() );

    boolean ok = boot.start();

    assertNull( boot.getSettingsProvider() );

    assertTrue( boot.isInitialized() );
    assertTrue( ok );

    boot.stop();
    assertFalse( boot.isInitialized() );
  }

  @Test
  public void testBootSettings() throws Exception {
    PentahoSystemBoot boot = new PentahoSystemBoot();
    boot.setFilePath( "src/test/resources/solution" );

    IPentahoObjectFactory factory = boot.getFactory();
    assertNotNull( "object factory is null", factory );

    SystemSettings settings = new SystemSettings();
    boot.setSettingsProvider( settings );

    assertEquals( settings, boot.getSettingsProvider() );
  }

  @Test( expected = NoSuchMethodError.class )
  public void testReadOnlyFactory() {

    PentahoSystemBoot boot = new PentahoSystemBoot();
    boot.setFilePath( "src/test/resources/solution" );
    TestObjectFactory objectFactory = new TestObjectFactory();
    boot.setFactory( objectFactory );

    boot.define( ISolutionEngine.class.getSimpleName(), Object1.class.getName(),
        IPentahoDefinableObjectFactory.Scope.GLOBAL );
  }

  @Test
  public void testObjectFactoryAvailableThruShutdown() {

    final AtomicBoolean objectFactoryWasValid = new AtomicBoolean( false );
    IPentahoSystemListener listener = new IPentahoSystemListener() {
      @Override
      public boolean startup( IPentahoSession session ) {
        return true;
      }

      @Override
      public void shutdown() {
        // Verify that the ObjectFactory is still valid at this point
        String s = PentahoSystem.get( String.class );

        // Not possible to assert within here as PentahoSystem catches all Exceptions, setting marker boolean instead.
        objectFactoryWasValid.set( "Testing".equals( s ) );

      }
    };
    PentahoSystem.setSystemListeners( Collections.singletonList( listener ) );
    PentahoSystem.init();

    // Add an object to PentahoSystem, then verify that it can be retrieved
    PentahoSystem.registerObject( "Testing" );
    String s = PentahoSystem.get( String.class );
    assertEquals( "Testing", s );

    PentahoSystem.shutdown();
    // At this point the shutdown() method on the listener has been called, check the boolean flag.
    assertTrue( "ShutdownListener wasn't able to get object from PentahoSystem", objectFactoryWasValid.get() );

  }

  @Test
  /**
   * Tests that multiple calls to PentahoSystem.init() without an intervening shutdown() results in an error being
   * logged.
   */
  public void testMultipleInitWithoutShutdownLogsError() {
    // capture current length of exceptions

    int currentExceptionNo = ( Logger.getExceptions() == null ) ? 0 : Logger.getExceptions().size();
    boolean initialized = PentahoSystem.init();
    assertTrue( initialized );
    Logger.getExceptions().clear();// Clear exception stack in case there was exception added during the first call.
    PentahoSystem.init();
    List<Throwable> exceptions = Logger.getExceptions();
    assertTrue( "Exception was not thrown as expected", exceptions.size() > currentExceptionNo );
    StackTraceElement stackTraceElement = exceptions.get( currentExceptionNo ).getStackTrace()[0];
    assertEquals( PentahoSystem.class.getName(), stackTraceElement.getClassName() );
    assertEquals( "init", stackTraceElement.getMethodName() );
    assertEquals( "'Init' method was run twice without 'shutdown'", exceptions.get( 0 ).getMessage() );

  }
}
