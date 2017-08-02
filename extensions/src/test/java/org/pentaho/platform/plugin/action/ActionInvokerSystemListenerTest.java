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
package org.pentaho.platform.plugin.action;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.engine.IPentahoSession;

import java.io.File;
import java.io.FileInputStream;


import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.powermock.core.classloader.annotations.PrepareForTest;


@RunWith( MockitoJUnitRunner.class )
@PrepareForTest( PentahoSystem.class )
public class ActionInvokerSystemListenerTest {


  private static final Log logger = LogFactory.getLog( ActionInvokerSystemListener.class );
  /**
   * Test class for the ActionInvokerSystemListener
   */
  private static final String resourcesFolder = "src/test/resources/ActionInvokerListenerTestResources/validFiles";


  private static final String noJsonFilesFolder = resourcesFolder + "/NoJsonFiles";
  private IPentahoSession mockSession;
  private DefaultActionInvoker actionInvoker;
  private ActionInvokerSystemListener actionInvokerSystemListener;
  private static final String DEFAULT_CONTENT_FOLDER = resourcesFolder + "/system/default-content";

  private String[] files = {
    "environment.json",
    "environment_invalid_actionIdMisspelled.json",
    "valid_mock_wi.json",
    "unparseable.json",
    "invalid_action_params.json"
  };


  @Before
  public void init( ) {
    mockSession = mock( IPentahoSession.class );
    actionInvoker = mock( DefaultActionInvoker.class );
  }

  @Test
  public void testStartup( ) throws Exception {
    //test consumption of a set of files
    String absFileName = getAbsoluteFilename( resourcesFolder );
    testStartup( absFileName, true );
    testStartup( absFileName, false );

    //test a folder with no json files
    absFileName = getAbsoluteFilename( noJsonFilesFolder );
    testStartup( absFileName, true );
    testStartup( absFileName, false );

    //test non-existent filepath
    absFileName = DEFAULT_CONTENT_FOLDER;
    testStartup( absFileName, true );
    testStartup( absFileName, false );

    //test null input
    testStartup( null, true );
    testStartup( null, false );
  }

  public void testStartup( String absFileName, boolean environmentVariablesFolderSet ) throws Exception {
    ActionInvokerSystemListener tempActionInvokerSystemListener = new ActionInvokerSystemListener();
    actionInvokerSystemListener = spy( tempActionInvokerSystemListener );
    IAction action = spy( IAction.class );
    doReturn( action ).when( actionInvokerSystemListener ).getActionBean( anyString(), anyString() );
    if ( environmentVariablesFolderSet ) {
      actionInvokerSystemListener.setEnvironmentVariablesFolder( absFileName );
    }
    doReturn( absFileName ).when( actionInvokerSystemListener ).getSolutionPath();
    doReturn( actionInvoker ).when( actionInvokerSystemListener ).getActionInvoker();
    IActionInvokeStatus status = new ActionInvokeStatus();
    doReturn( status ).when( actionInvoker ).invokeAction( any(), any(), any() );
    boolean res = actionInvokerSystemListener.startup( mockSession );
    Assert.assertTrue( res );
  }

  /**
   * Returns the absolute filename for this test class in the resource folder
   *
   * @param resourceFileName
   * @return the absolute filename
   */
  private String getAbsoluteFilename( String resourceFileName ) {
    if ( resourceFileName != null ) {
      return new File( resourceFileName ).getAbsolutePath();
    } else {
      return new File( resourcesFolder ).getAbsolutePath();
    }
  }

  @Test
  public void testPayload( ) throws Exception {
    File validMockWi = new File( resourcesFolder + "/" + files[ 2 ] );
    ActionInvokerSystemListener temp = new ActionInvokerSystemListener();
    ActionInvokerSystemListener spy = spy( temp );
    IAction action = spy( IAction.class );
    doReturn( action ).when( spy ).getActionBean( anyString(), anyString() );
    spy.new Payload( IOUtils.toString( new FileInputStream( validMockWi ) ) );
    File valid_unencoded = new File( resourcesFolder + "/" + files[ 0 ] );
    spy.new Payload( IOUtils.toString( new FileInputStream( valid_unencoded ) ) );
    //no exceptions thrown, payloads successfully created
    Assert.assertTrue( true );
  }

  @After
  public void cleanup( ) {
    // delete wi.json
    File wiJson = new File( getAbsoluteFilename( resourcesFolder ) + "/wi-status.ok" );
    wiJson.delete();
    File wiJsonError = new File( getAbsoluteFilename( resourcesFolder ) + "/NoJsonFiles/wi-status.error" );
    wiJsonError.delete();
  }
}



