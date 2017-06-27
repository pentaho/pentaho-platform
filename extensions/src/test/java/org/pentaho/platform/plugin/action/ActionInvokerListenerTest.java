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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPentahoSession;
import java.io.File;
import static org.mockito.Mockito.mock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@RunWith( MockitoJUnitRunner.class )
/**
 * Test class for the ActionInvokerSystemListener
 */
public class ActionInvokerListenerTest {
  private static final String resourcesFolder = "src/test/resources/ActionInvokerListenerTestResources/";
  private static final Log logger = LogFactory.getLog( ActionInvokerSystemListener.class );
  private IPentahoSession mockSession;

  private final String[] testJsonFiles = {
    "environment_valid.json",
    "environment_valid_2.json",
    "environment_invalid.json",
    "invalid_action_params.json",
    "invalid_missing_parameter.json",
    "missingparameter.json",
    "unparseable_json.json"
  };

  @Mock
  private DefaultActionInvoker defaultActionInvoker;

  @Spy
  private ActionInvokerSystemListener wnl = new ActionInvokerSystemListener();

  @Before
  public void init( ) {
    mockSession = mock( IPentahoSession.class );
  }

  @Test
  public void testJson( ) {
    for ( String fileName : testJsonFiles ) {
      wnl.setEnvironmentVariablesFilepath( getAbsoluteFilename( fileName ) );
      Assert.assertTrue( wnl.startup( mockSession ) );
      Assert.assertTrue( isRenamed( fileName ) );
    }
  }

  /**
   * Returns the absolute filename for this test class in the resource folder
   * @param resourceFileName
   * @return the absolute filename
   */
  private String getAbsoluteFilename( String resourceFileName ) {
    return new File( resourcesFolder + resourceFileName ).getAbsolutePath();
  }

  /**
   * Checks whether a file has been renamed or not
   * @param fileName
   * @return true if the file has been renamed, false if it has not been renamed
   */
  private boolean isRenamed( String fileName ) {
    return !( new File( resourcesFolder + fileName ).exists() );
  }

  @After
  public void cleanup() {
    // Remove timestamp extension from all JSON files
    File resourceDir = new File( resourcesFolder );
    for ( File f : resourceDir.listFiles() ) {
      if ( !f.getName().endsWith( "json" ) ) {
        f.renameTo( new File( resourcesFolder + f.getName().substring( 0, f.getName().lastIndexOf( "." ) ) ) );
      }
    }
  }
}
