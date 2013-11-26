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

package org.pentaho.platform.engine.core;

import junit.framework.TestCase;
import org.pentaho.platform.api.util.ITempFileDeleter;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.StandaloneTempFileDeleter;
import org.pentaho.platform.util.UUIDUtil;

import java.io.File;
import java.util.Locale;

@SuppressWarnings( "nls" )
public class StandaloneSessionTest extends TestCase {

  private static final String SolutionDeleterTopFolderName = "test-session-solution";

  public void testDefaultConstructor() {

    StandaloneSession session = new StandaloneSession();

    assertEquals( "session name is wrong", "unknown", session.getName() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session id is wrong", "unknown", session.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session locale is wrong", Locale.getDefault(), session.getLocale() ); //$NON-NLS-1$

    // make sure this does not blow up
    session.destroy();

    assertEquals( "session object name is wrong", StandaloneSession.class.getName(), session.getObjectName() ); //$NON-NLS-1$
  }

  protected void setUp() throws Exception {
    File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) ); //$NON-NLS-1$
    File solnDir = new File( tmpDir, "test-session-solution/system/tmp" ); //$NON-NLS-1$
    if ( !solnDir.exists() ) {
      solnDir.mkdirs();
    }
  }

  protected void tearDown() throws Exception {
    File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) ); //$NON-NLS-1$
    File solnDir = new File( tmpDir, "test-session-solution" ); //$NON-NLS-1$
    emptyDir( solnDir );
    if ( ( solnDir != null ) & ( solnDir.exists() ) ) {
      solnDir.delete();
    }
  }

  protected void emptyDir( File aFile ) throws Exception {
    File[] files = aFile.listFiles();
    for ( File aTmpFile : files ) {
      if ( aTmpFile.isDirectory() ) {
        emptyDir( aTmpFile );
      } else if ( aTmpFile.isFile() ) {
        aTmpFile.delete();
      }
    }
  }

  public void testNameConstructor() {

    StandaloneSession session = new StandaloneSession( "testname" ); //$NON-NLS-1$

    assertEquals( "session name is wrong", "testname", session.getName() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session id is wrong", "testname", session.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session locale is wrong", Locale.getDefault(), session.getLocale() ); //$NON-NLS-1$
  }

  public void testIdConstructor() {

    StandaloneSession session = new StandaloneSession( "testname", "testid" ); //$NON-NLS-1$ //$NON-NLS-2$

    assertEquals( "session name is wrong", "testname", session.getName() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session id is wrong", "testid", session.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session locale is wrong", Locale.getDefault(), session.getLocale() ); //$NON-NLS-1$
  }

  public void testConstructor() {

    StandaloneSession session = new StandaloneSession( "testname", "testid", Locale.CHINESE ); //$NON-NLS-1$ //$NON-NLS-2$

    assertEquals( "session name is wrong", "testname", session.getName() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session id is wrong", "testid", session.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "session locale is wrong", Locale.CHINESE, session.getLocale() ); //$NON-NLS-1$
  }

  public void testAttributes() {

    StandaloneSession session = new StandaloneSession();
    assertFalse( "Wrong attributes", session.getAttributeNames().hasNext() ); //$NON-NLS-1$

    session.setAttribute( "testattribute", this ); //$NON-NLS-1$
    assertTrue( "Wrong attributes", session.getAttributeNames().hasNext() ); //$NON-NLS-1$
    assertEquals( "Wrong attribute name", "testattribute", session.getAttributeNames().next() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Wrong attribute value", this, session.getAttribute( "testattribute" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    session.removeAttribute( "testattribute" ); //$NON-NLS-1$
    assertFalse( "Wrong attributes", session.getAttributeNames().hasNext() ); //$NON-NLS-1$
    assertNull( "Wrong attribute value", session.getAttribute( "testattribute" ) ); //$NON-NLS-1$ //$NON-NLS-2$

  }

  public void testLogger() {

    StandaloneSession session = new StandaloneSession();
    assertNotNull( "Bad logger", session.getLogger() ); //$NON-NLS-1$    
  }

  public void testAuthenticated() {

    StandaloneSession session = new StandaloneSession( "testname" ); //$NON-NLS-1$
    assertFalse( "Wrong authenication", session.isAuthenticated() ); //$NON-NLS-1$

    session.setAuthenticated( null );
    assertFalse( "Wrong authenication", session.isAuthenticated() ); //$NON-NLS-1$

    session.setAuthenticated( "testname" ); //$NON-NLS-1$
    assertTrue( "Wrong authenication", session.isAuthenticated() ); //$NON-NLS-1$

    session.setNotAuthenticated();
    assertNull( "session name is wrong", session.getName() ); //$NON-NLS-1$
    assertFalse( "Wrong authenication", session.isAuthenticated() ); //$NON-NLS-1$

  }

  public void testActionProcess() {

    StandaloneSession session = new StandaloneSession();
    assertEquals( "Wrong action name", "", session.getActionName() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Wrong process id", null, session.getProcessId() ); //$NON-NLS-1$

    session.setActionName( "testaction" ); //$NON-NLS-1$
    session.setProcessId( "testprocess" ); //$NON-NLS-1$
    assertEquals( "Wrong action name", "testaction", session.getActionName() ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "Wrong process id", "testprocess", session.getProcessId() ); //$NON-NLS-1$ //$NON-NLS-2$

  }

  public void testBackgroundAlert() {

    StandaloneSession session = new StandaloneSession();
    assertFalse( "Wrong alert", session.getBackgroundExecutionAlert() ); //$NON-NLS-1$ 

    session.setBackgroundExecutionAlert();
    assertTrue( "Wrong alert", session.getBackgroundExecutionAlert() ); //$NON-NLS-1$ 

    session.resetBackgroundExecutionAlert();
    assertFalse( "Wrong alert", session.getBackgroundExecutionAlert() ); //$NON-NLS-1$ 

  }

  protected String getSolutionRoot() {
    String tmpDir = System.getProperty( "java.io.tmpdir" ); //$NON-NLS-1$
    if ( tmpDir.endsWith( "/" ) ) { //$NON-NLS-1$
      return tmpDir + SolutionDeleterTopFolderName;
    } else {
      return tmpDir + "/" + SolutionDeleterTopFolderName; //$NON-NLS-1$
    }
  }

  public void testTempFileDeleter() throws Exception {
    StandaloneSession session = new StandaloneSession( "tempfiledeleter", UUIDUtil.getUUIDAsString() ); // get one with an id. //$NON-NLS-1$
    StandaloneTempFileDeleter deleter = new StandaloneTempFileDeleter();

    StandaloneApplicationContext appContext = new StandaloneApplicationContext( getSolutionRoot(), "" ); //$NON-NLS-1$ //$NON-NLS-2$
    File file1 = appContext.createTempFile( session, "testTempFileDeleter", "txt", true ); //$NON-NLS-1$ //$NON-NLS-2$
    assertNotNull( file1 ); // File object was returned
    assertTrue( file1.exists() ); // File exists
    assertFalse( deleter.hasTempFile( file1.getName() ) ); // Deleter wasn't bound to session, so no delete
    // Bind deleter to the session
    session.setAttribute( ITempFileDeleter.DELETER_SESSION_VARIABLE, deleter );
    File file2 = appContext.createTempFile( session, "testTempFileDeleter", "txt", true ); //$NON-NLS-1$ //$NON-NLS-2$
    assertNotNull( file2 ); // File object was returned
    assertTrue( file2.exists() ); // File exists
    assertTrue( deleter.hasTempFile( file2.getName() ) ); // Deleter is bound to session
    // File names should be unique
    assertFalse( file1.getName().equals( file2.getName() ) );

    deleter.doTempFileCleanup();
    assertTrue( file1.exists() ); // This file will be left over
    assertFalse( file2.exists() ); // The deleter should have removed this
    assertFalse( deleter.hasTempFile( file2.getName() ) ); // After doTempFileCleanup() the list should be empty
    // The tearDown should blow away everything else...
    deleter.trackTempFile( file2 ); // Add known deleted file to the deleter
    deleter.doTempFileCleanup(); // Validates cleanup doesn't choke on missing files
    // Test that IllegalArgumentException if passed a null
    try {
      deleter.trackTempFile( null );
      fail();
    } catch ( IllegalArgumentException expected ) {
      //ignored
    }
  }

}
