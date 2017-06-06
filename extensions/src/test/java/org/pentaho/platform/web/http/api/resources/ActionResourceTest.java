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
 * Copyright (c) 2017 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.httpclient.HttpStatus;
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
import org.pentaho.platform.plugin.action.DefaultActionInvoker;
import org.pentaho.platform.plugin.action.builtin.ActionSequenceAction;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tests the {@link ActionResource} within the context of CE.
 */
@RunWith( PowerMockRunner.class )
@PrepareForTest( PentahoSystem.class )
public class ActionResourceTest {

  private ActionResource resource;
  private ActionResource resourceMock;
  private Response expectedResult;
  private StandaloneSession session;
  private StandaloneSpringPentahoObjectFactory springFactory;

  private String actionId = "myActionId";
  private Class<? extends IAction> actionClass = ActionSequenceAction.class;
  private String actionClassName = actionClass.getName();
  private String actionUser = "user";
  private String actionParams = "{\"serializedParams\": \"[ \\\"java.util.HashMap\\\", {\\\"actionUser\\\" : "
    + "\\\"" + actionUser + "\\\", \\\"actionId\\\" : \\\"" + actionId + "\\\"} ]\"}";

  @Before
  public void setUp() throws Exception {
    resourceMock = Mockito.spy( ActionResource.class );
    resource = new ActionResource();
    expectedResult = Response.status( HttpStatus.SC_ACCEPTED ).build();
    final ExecutorService executorService = Mockito.spy( Executors.newFixedThreadPool( 1 ) );
    resource.executorService = Mockito.mock( ExecutorService.class );

    session = new StandaloneSession();
    PentahoSessionHolder.setSession( session );
    springFactory = new StandaloneSpringPentahoObjectFactory();
    springFactory.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", null );
    PentahoSystem.registerObjectFactory( springFactory );

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

  @Test
  public void testRunInBackgroundNegative() {
    // verify that no matter what is passed to the runInBackground method, including nulls and other "bad" input, it
    // returns the expected status
    final String[] badStrInput = new String[] { null, "", " ", "foo" };
    for ( final String actionId : badStrInput ) {
      for ( final String actionClassName : badStrInput ) {
        for ( final String user : badStrInput ) {
          for ( final String params : badStrInput ) {
            final Response response = resource.runInBackground( actionId, actionClassName, user, params );
            Assert.assertNotNull( response );
            Assert.assertEquals( expectedResult.getStatus(), response.getStatus() );
          }
        }
      }
    }
  }

  /**
   * Verifies that calling runInBackground has the desired effect, namely the executor submitting a RunnableAction
   * for execution.
   */
  @Test
  public void testRunInBackground() throws Exception {

    // mock the RunnableAction and how it's created by the resource
    final ActionResource.RunnableAction runnableAction = Mockito.spy( ActionResource.RunnableAction.class );
    Mockito.doReturn( runnableAction ).when( resourceMock ).createRunnable( actionId, actionClassName, actionUser,
      actionParams );

    // call the runInBackground methos
    resourceMock.runInBackground( actionId, actionClassName, actionUser, actionParams );

    // verify that the createRunnable method was called with the expected parameters
    Mockito.verify( resourceMock, Mockito.times( 1 ) ).createRunnable( actionId, actionClassName, actionUser,
      actionParams );

    // verity that the executor submit method was called to execute the expected RunnableAction
    Mockito.verify( resourceMock.executorService, Mockito.times( 1 ) ).submit( runnableAction );
  }

  @Test
  public void testCreateRunnable() {

    final ActionResource.RunnableAction runnable = resource.createRunnable( actionId, actionClassName, actionUser,
      actionParams );

    Assert.assertNotNull( runnable );
    Assert.assertEquals( resource, runnable.resource );
    Assert.assertEquals( actionId, runnable.actionId );
    Assert.assertEquals( actionClassName, runnable.actionClass );
    Assert.assertEquals( actionParams, runnable.actionParams );

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
    final ActionResource.RunnableAction runnableAction = Mockito.spy( ActionResource.RunnableAction.class );
    runnableAction.actionId = actionId;
    runnableAction.actionClass = actionClassName;
    runnableAction.user = actionUser;
    runnableAction.actionParams = actionParams;
    runnableAction.resource = resourceMock;
    Mockito.doReturn( runnableAction ).when( resourceMock ).createRunnable( actionId, actionClassName, actionUser,
      actionParams );

    // mock the action invoker
    final IActionInvoker actionInvoker = Mockito.spy( MyDefaultActionInvoker.class );
    Mockito.doReturn( actionInvoker ).when( runnableAction.resource ).getActionInvoker();
    // mock the DefaultActionInvoker which is created within the runnable
    final DefaultActionInvoker defaultActionInvoker = Mockito.spy( DefaultActionInvoker.class );
    Mockito.doReturn( defaultActionInvoker ).when( runnableAction.resource ).getDefaultActionInvoker();
    // mock the action
    final IAction action = Mockito.spy( ActionSequenceAction.class );
    Mockito.doReturn( action ).when( runnableAction ).createActionBean( actionClassName, actionId );
    // mock the params
    final Map<String, Serializable> params = Mockito.spy( Map.class );
    Mockito.doReturn( params ).when( runnableAction ).deserialize( action, runnableAction.actionParams );

    // invoke the run method directly since we want to run it in the current thread to verify that its content is
    // executed as expected; given that we have already tested that the executor submit(...) method is invoked as
    // expected, we can perform this test separately and have confidence that end-to-end will also work when the
    // RunnableAction executes in a separate thread
    runnableAction.run();

    // within run(), we expect the createActionBean to be called with the expected parameters
    Mockito.verify( runnableAction, Mockito.times( 1 ) ).createActionBean( actionClassName, actionId );
    // verify that deserialize is called with the expected parameters
    Mockito.verify( runnableAction, Mockito.times( 1 ) ).deserialize( Mockito.any( actionClass ),
      Mockito.eq( actionParams ) );
    // verify that runInBackgroundLocally is called with the expected parameters
    Mockito.verify( defaultActionInvoker, Mockito.times( 1 ) ).runInBackground( Mockito.eq( action ), Mockito
      .eq( actionUser ), Mockito.eq( params ) );
  }
}

/**
 * Test class, created so that we can override the runInBackgroundLocally to return null for testing puroses. When
 * mocking the action invoker and verifying a call to runInBackgroundLocally with Matchers, the actual values seen by
 * the method internally are null and that causes exceptions, which we want to avoid - we are only concerned with
 * ensuring that the call to the method is made with the correct parameter types.
 */
class MyDefaultActionInvoker extends DefaultActionInvoker {

  @Override
  public IActionInvokeStatus runInBackground( final IAction actionBean, final String actionUser, final
  Map<String, Serializable> params ) throws Exception {
    return null;
  }
}