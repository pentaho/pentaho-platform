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

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPentahoSession;

import javax.ws.rs.core.Response;


import java.io.File;
import java.io.FileInputStream;


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.web.http.api.resources.ActionResource;
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
  private ActionResource actionResource;
  private StandaloneApplicationContext applicationContext;
  private ActionInvokerSystemListener actionInvokerSystemListener;
  private static final String DEFAULT_CONTENT_FOLDER = resourcesFolder + "/system/default-content";
  private static final String HTTP_202 = "202";

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
    actionResource = mock( ActionResource.class );
    applicationContext = mock( StandaloneApplicationContext.class );

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
    if ( environmentVariablesFolderSet ) {
      actionInvokerSystemListener.setEnvironmentVariablesFolder( absFileName );
    }
    doReturn( absFileName ).when( actionInvokerSystemListener ).getSolutionPath();
    doReturn( actionResource ).when( actionInvokerSystemListener ).getActionResource();
    int httpStatus = HttpStatus.SC_ACCEPTED;
    Response response = Response.status( httpStatus ).build();
    doReturn( response ).when( actionResource ).invokeAction( anyString(), anyString(), anyString(), anyString(), any() );
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
    ActionInvokerSystemListener.Payload payload = new ActionInvokerSystemListener().new Payload( IOUtils.toString( new FileInputStream( validMockWi ) ) );
    File valid_unencoded = new File( resourcesFolder + "/" + files[ 0 ] );
    payload = new ActionInvokerSystemListener().new Payload( IOUtils.toString( new FileInputStream( valid_unencoded ) ) );
    //no exceptions thrown, payloads successfully created
    Assert.assertTrue( true );
  }

  @Test
  public void testGetActionResource( ) {
    ActionInvokerSystemListener testActionInvokerSystemListener = new ActionInvokerSystemListener();
    ActionResource actionResource = testActionInvokerSystemListener.getActionResource();
    Assert.assertTrue( actionResource.getClass() == ActionResource.class );
  }

  @After
  public void cleanup( ) {
    // Remove timestamp extension from all JSON files
    File resourceDir = new File( resourcesFolder );
    for ( File f : resourceDir.listFiles() ) {
      if ( !f.getName().endsWith( "json" ) && !f.isDirectory() && f.getName().contains( "json" ) ) {
        f.renameTo( new File( resourcesFolder + "/" + f.getName().substring( 0, f.getName().lastIndexOf( "." ) ) ) );
      }
    }
  }
}



