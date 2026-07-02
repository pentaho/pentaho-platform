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

import junit.framework.TestCase;
import org.pentaho.platform.api.util.ITempFileDeleter;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.StandaloneTempFileDeleter;
import org.pentaho.platform.util.UUIDUtil;

import java.io.File;
import java.util.Locale;

public class StandaloneSessionTest extends TestCase {
  private static final String SOLUTION_DELETER_TOP_FOLDER_NAME = "test-session-solution";

  public void testDefaultConstructor() {

    StandaloneSession session = new StandaloneSession();

    assertEquals( "session name is wrong", "unknown", session.getName() );
    assertEquals( "session id is wrong", "unknown", session.getId() );
    assertEquals( "session locale is wrong", Locale.getDefault(), session.getLocale() );

    // make sure this does not blow up
    session.destroy();

    assertEquals( "session object name is wrong", StandaloneSession.class.getName(),
      session.getObjectName() );
  }

  protected void setUp() throws Exception {
    File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) );
    File solnDir = new File( tmpDir, "test-session-solution/system/tmp" );
    if ( !solnDir.exists() ) {
      solnDir.mkdirs();
    }
  }

  protected void tearDown() throws Exception {
    File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) );
    File solnDir = new File( tmpDir, "test-session-solution" );
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

    StandaloneSession session = new StandaloneSession( "testname" );

    assertEquals( "session name is wrong", "testname", session.getName() );
    assertEquals( "session id is wrong", "testname", session.getId() );
    assertEquals( "session locale is wrong", Locale.getDefault(), session.getLocale() );
  }

  public void testIdConstructor() {

    StandaloneSession session = new StandaloneSession( "testname", "testid" );

    assertEquals( "session name is wrong", "testname", session.getName() );
    assertEquals( "session id is wrong", "testid", session.getId() );
    assertEquals( "session locale is wrong", Locale.getDefault(), session.getLocale() );
  }

  public void testConstructor() {

    StandaloneSession session =
      new StandaloneSession( "testname", "testid", Locale.CHINESE );

    assertEquals( "session name is wrong", "testname", session.getName() );
    assertEquals( "session id is wrong", "testid", session.getId() );
    assertEquals( "session locale is wrong", Locale.CHINESE, session.getLocale() );
  }

  public void testAttributes() {

    StandaloneSession session = new StandaloneSession();
    assertFalse( "Wrong attributes", session.getAttributeNames().hasNext() );

    session.setAttribute( "testattribute", this );
    assertTrue( "Wrong attributes", session.getAttributeNames().hasNext() );
    assertEquals( "Wrong attribute name", "testattribute",
      session.getAttributeNames().next() );
    assertEquals( "Wrong attribute value", this, session.getAttribute( "testattribute" ) );

    assertTrue( "testattribute", session.getAttributeNames().hasNext() );

    session.setAttribute( "testattribute", null );
    assertFalse( "testattribute", session.getAttributeNames().hasNext() );
    assertNull( "Attribute removed", session.getAttribute( "testattribute" ) );

    session.removeAttribute( "testattribute" );
    assertFalse( "Wrong attributes", session.getAttributeNames().hasNext() );
    assertNull( "Wrong attribute value", session.getAttribute( "testattribute" ) );

  }

  public void testLogger() {

    StandaloneSession session = new StandaloneSession();
    assertNotNull( "Bad logger", session.getLogger() );
  }

  public void testAuthenticated() {

    StandaloneSession session = new StandaloneSession( "testname" );
    assertFalse( "Wrong authenication", session.isAuthenticated() );

    session.setAuthenticated( null );
    assertFalse( "Wrong authenication", session.isAuthenticated() );

    session.setAuthenticated( "testname" );
    assertTrue( "Wrong authenication", session.isAuthenticated() );

    session.setNotAuthenticated();
    assertNull( "session name is wrong", session.getName() );
    assertFalse( "Wrong authenication", session.isAuthenticated() );

  }

  public void testActionProcess() {

    StandaloneSession session = new StandaloneSession();
    assertEquals( "Wrong action name", "", session.getActionName() );
    assertEquals( "Wrong process id", null, session.getProcessId() );

    session.setActionName( "testaction" );
    session.setProcessId( "testprocess" );
    assertEquals( "Wrong action name", "testaction", session.getActionName() );
    assertEquals( "Wrong process id", "testprocess", session.getProcessId() );

  }

  public void testBackgroundAlert() {

    StandaloneSession session = new StandaloneSession();
    assertFalse( "Wrong alert", session.getBackgroundExecutionAlert() );

    session.setBackgroundExecutionAlert();
    assertTrue( "Wrong alert", session.getBackgroundExecutionAlert() );

    session.resetBackgroundExecutionAlert();
    assertFalse( "Wrong alert", session.getBackgroundExecutionAlert() );

  }

  protected String getSolutionRoot() {
    String tmpDir = System.getProperty( "java.io.tmpdir" );
    if ( tmpDir.endsWith( "/" ) ) {
      return tmpDir + SOLUTION_DELETER_TOP_FOLDER_NAME;
    } else {
      return tmpDir + "/" + SOLUTION_DELETER_TOP_FOLDER_NAME;
    }
  }

  public void testTempFileDeleter() throws Exception {
    StandaloneSession session =
      new StandaloneSession( "tempfiledeleter", UUIDUtil.getUUIDAsString() ); // get one with an id.
    StandaloneTempFileDeleter deleter = new StandaloneTempFileDeleter();

    StandaloneApplicationContext appContext =
      new StandaloneApplicationContext( getSolutionRoot(), "" );
    File file1 = appContext.createTempFile( session, "testTempFileDeleter", "txt", true );
    assertNotNull( file1 ); // File object was returned
    assertTrue( file1.exists() ); // File exists
    assertFalse( deleter.hasTempFile( file1.getName() ) ); // Deleter wasn't bound to session, so no delete
    // Bind deleter to the session
    session.setAttribute( ITempFileDeleter.DELETER_SESSION_VARIABLE, deleter );
    File file2 = appContext.createTempFile( session, "testTempFileDeleter", "txt", true );
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
