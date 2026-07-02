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


package org.pentaho.platform.engine.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISessionStartupAction;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.GlobalListsPublisher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SessionStartupAction;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;

import junit.framework.TestCase;

@SuppressWarnings( { "all" } )
public class GlobalListPublisherTest extends TestCase {

  public void test1() throws ObjectFactoryException {

    StandaloneSession session = new StandaloneSession( "test" );

    StandaloneApplicationContext appContext = new StandaloneApplicationContext( "src/test/resources/solution", "" );

    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "src/test/resources/solution/system/pentahoObjects.GlobalListPublisherTest.spring.xml", null );

    PentahoSystem.init( appContext );
    PentahoSystem.registerObjectFactory( factory );
    PentahoSystem.setSystemSettingsService( factory.get( ISystemSettings.class, "systemSettingsService", session ) );

    List<ISessionStartupAction> actions = new ArrayList<ISessionStartupAction>();

    SessionStartupAction startupAction1 = new SessionStartupAction();
    startupAction1.setSessionType( PentahoSystem.SCOPE_GLOBAL );
    startupAction1.setActionPath( "testsolution/testpath/test.xaction" );
    startupAction1.setActionOutputScope( PentahoSystem.SCOPE_GLOBAL );
    actions.add( startupAction1 );

    TestRuntimeContext context = new TestRuntimeContext();
    context.status = IRuntimeContext.RUNTIME_STATUS_SUCCESS;
    TestSolutionEngine engine = PentahoSystem.get( TestSolutionEngine.class, "ISolutionEngine", session );
    engine.testRuntime = context;
    Map<String, IActionParameter> outputs = new HashMap<String, IActionParameter>();
    TestActionParameter param = new TestActionParameter();
    param.setValue( "testvalue" );
    outputs.put( "testoutput", param );
    context.outputParameters = outputs;

    engine.executeCount = 0;
    GlobalListsPublisher globals = new GlobalListsPublisher();
    assertEquals( Messages.getInstance().getString( "GlobalListsPublisher.USER_SYSTEM_SETTINGS" ), globals.getName() );
    assertEquals( Messages.getInstance().getString( "GlobalListsPublisher.USER_DESCRIPTION" ), globals
        .getDescription() );
    assertTrue( !globals.getName().startsWith( "!" ) );
    assertTrue( !globals.getDescription().startsWith( "!" ) );
    assertNotNull( globals.getLogger() );
    String resultMsg = globals.publish( session );
    assertEquals( Messages.getInstance().getString( "GlobalListsPublisher.USER_SYSTEM_SETTINGS_UPDATED" ), resultMsg );

    assertEquals( 0, engine.executeCount );
    PentahoSystem.setSessionStartupActions( actions );
    IParameterProvider globalParams = PentahoSystem.getGlobalParameters();

    resultMsg = globals.publish( session );
    assertEquals( 1, engine.executeCount );
    assertEquals( Messages.getInstance().getString( "GlobalListsPublisher.USER_SYSTEM_SETTINGS_UPDATED" ), resultMsg );

    // check that we made it all the way to executing the startup action
    assertEquals( session, engine.initSession );
    assertEquals( startupAction1.getActionPath(), engine.actionPath );
    assertEquals( "testvalue", globalParams.getParameter( "testoutput" ) );

    param.setValue( "testvalue2" );

    resultMsg = globals.publish( session );
    assertEquals( Messages.getInstance().getString( "GlobalListsPublisher.USER_SYSTEM_SETTINGS_UPDATED" ), resultMsg );
    assertEquals( 2, engine.executeCount );

    assertNotNull( globalParams );
    assertEquals( "testvalue2", globalParams.getParameter( "testoutput" ) );

    assertEquals( 2, engine.executeCount );
  }

}
