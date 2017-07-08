/*!
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
 * Copyright (c) 2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.plugin.action.ActionParams;
import org.pentaho.platform.plugin.action.DefaultActionInvoker;
import org.pentaho.platform.plugin.action.builtin.ActionSequenceAction;
import org.pentaho.platform.util.ActionUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Tests the {@link ActionResource} within the context of CE.
 */
@RunWith( PowerMockRunner.class )
@PrepareForTest( PentahoSystem.class )
public class ActionResourceTest {

  private IAction actionMock;
  private Map<String, Serializable> actionMapMock;
  private ActionResource resource;
  private ActionResource resourceMock;
  private Response expectedResult;
  private StandaloneSession session;
  private StandaloneSpringPentahoObjectFactory springFactory;

  private String actionId = "myActionId";
  private Class<? extends IAction> actionClass = ActionSequenceAction.class;
  private String actionClassName = actionClass.getName();
  private String actionUser = "user";
  private String workItemUid = "uid";
  private ActionParams actionParams;
  private String actionParamsJson = "{\"serializedParams\": \"[ \\\"java.util.HashMap\\\", {\\\"actionUser\\\" : "
    + "\\\"" + actionUser + "\\\", \\\"workItemUid\\\" : \\\"" + workItemUid + "\\\", \\\"actionId\\\" : \\\"" + actionId + "\\\"} ]\"}";

  @Before
  public void setUp() throws Exception {
    resourceMock = Mockito.spy( ActionResource.class );
    resource = new ActionResource();
    expectedResult = Response.status( HttpStatus.SC_ACCEPTED ).build();
    resource.executorService = Mockito.mock( ExecutorService.class );

    session = new StandaloneSession();
    PentahoSessionHolder.setSession( session );
    springFactory = new StandaloneSpringPentahoObjectFactory();
    springFactory.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", null );
    PentahoSystem.registerObjectFactory( springFactory );

    actionMock = Mockito.spy( IAction.class );
    actionMapMock = Mockito.spy( Map.class );
    Mockito.doReturn( actionMock ).when( resourceMock ).createActionBean( actionClassName, actionId );
    actionParams = ActionParams.fromJson( actionParamsJson );
  }

  @After
  public void tearDown() {
    resourceMock = null;
    resource = null;
    expectedResult = null;
    session = null;
    springFactory = null;
  }

  @Test
  public void testGetActionInvoker() {
    final IActionInvoker actionInvoker = resource.getActionInvoker();
    Assert.assertNotNull( actionInvoker );
    Assert.assertEquals( DefaultActionInvoker.class, actionInvoker.getClass() );
  }

  /**
   * Verifies that calling invokeAction has the desired effect, namely the executor submitting a RunnableAction
   * for execution.
   */
  @Test
  public void testRunInBackground() throws Exception {
    final Map<String,Serializable> paramMap = ActionParams.deserialize( actionMock , actionParams );
    // mock the RunnableAction and how it's created by the resource
    final ActionResource.CallableAction callableAction = Mockito.spy( ActionResource.CallableAction.class );
    Mockito.doReturn( callableAction ).when( resourceMock ).createCallable( actionMock, actionUser, paramMap );

    // call the runInBackground methos
    resourceMock
      .invokeAction( ActionUtil.INVOKER_DEFAULT_ASYNC_EXEC_VALUE, actionId, actionClassName, actionUser, actionParams );

    // verify that the createRunnable method was called with the expected parameters
    Mockito.verify( resourceMock, Mockito.times( 1 ) ).createCallable( actionMock, actionUser, paramMap ) ;

    // within invokeAction(), we expect the createActionBean to be called with the expected parameters
    Mockito.verify( resourceMock, Mockito.times( 1 ) ).createActionBean( actionClassName, actionId );

    // verity that the executor submit method was called to execute the expected RunnableAction
    Mockito.verify( resourceMock.executorService, Mockito.times( 1 ) ).submit( callableAction );
  }

  @Test
  public void testCreateRunnable() {

    final ActionResource.CallableAction runnable = resource.createCallable( actionMock, actionUser, actionMapMock );

    Assert.assertNotNull( runnable );
    Assert.assertEquals( resource, runnable.resource );
    Assert.assertEquals( actionUser, runnable.actionUser );
    Assert.assertEquals( actionMock, runnable.action );
    Assert.assertEquals( actionMapMock, runnable.params );

  }

  /**
   * Verifies that when the RunnableAction.run() method is called, the expected calls occur with the expected values.
   */
  @Test
  public void testRunnableRun() throws Exception {

    // Mock the IPluginManager, so that when we call ActionHelper.createActionBean, IPluginManager is not null
    IPluginManager pluginManager = Mockito.mock( IPluginManager.class );
    PowerMockito.mockStatic( PentahoSystem.class );
    BDDMockito.given( PentahoSystem.get( IPluginManager.class ) ).willReturn( pluginManager );

    // mock the RunnableAction and how it's created by the resource
    final ActionResource.CallableAction runnableAction = Mockito.spy( ActionResource.CallableAction.class );
    runnableAction.action = actionMock;
    runnableAction.actionUser = actionUser;
    runnableAction.params = actionMapMock;
    runnableAction.resource = resourceMock;
    Mockito.doReturn( runnableAction ).when( resourceMock ).createCallable( actionMock, actionUser, actionMapMock );

    // mock the action invoker
    final IActionInvoker actionInvoker = Mockito.spy( MyDefaultActionInvoker.class );
    Mockito.doReturn( actionInvoker ).when( runnableAction.resource ).getActionInvoker();
    // mock the DefaultActionInvoker which is created within the runnable
    final DefaultActionInvoker defaultActionInvoker = Mockito.spy( DefaultActionInvoker.class );
    Mockito.doReturn( defaultActionInvoker ).when( runnableAction.resource ).getDefaultActionInvoker();

    /*

    Mockito.doReturn( actionMock ).when( resourceMock ).createActionBean( actionClassName, actionId );
    Mockito.doReturn( actionMapMock ).when( resourceMock ).deserialize( actionMock, actionParamsJson );
     */
    // mock the action
    //final IAction action = Mockito.spy( ActionSequenceAction.class );
    //Mockito.doReturn( action ).when( runnableAction ).createActionBean( actionClassName, actionId );
    // mock the params
    //final Map<String, Serializable> params = Mockito.spy( Map.class );
    //Mockito.doReturn( params ).when( runnableAction ).deserialize( action, runnableAction.actionParamsJson );

    // invoke the call() method directly since we want to run it in the current thread to verify that its content is
    // executed as expected; given that we have already tested that the executor submit(...) method is invoked as
    // expected, we can perform this test separately and have confidence that end-to-end will also work when the
    // RunnableAction executes in a separate thread
    runnableAction.call();

    // verify that invokeAction is called with the expected parameters
    Mockito.verify( defaultActionInvoker, Mockito.times( 1 ) ).invokeAction( Mockito.eq( actionMock ), Mockito
      .eq( actionUser ), Mockito.eq( actionMapMock ) );
  }
}

/**
 * Test class, created so that we can override the invokeAction to return null for testing puroses. When
 * mocking the action invoker and verifying a call to invokeAction with Matchers, the actual values seen by
 * the method internally are null and that causes exceptions, which we want to avoid - we are only concerned with
 * ensuring that the call to the method is made with the correct parameter types.
 */
class MyDefaultActionInvoker extends DefaultActionInvoker {

  @Override
  public IActionInvokeStatus invokeAction( final IAction actionBean, final String actionUser, final
  Map<String, Serializable> params ) throws Exception {
    return null;
  }
}
