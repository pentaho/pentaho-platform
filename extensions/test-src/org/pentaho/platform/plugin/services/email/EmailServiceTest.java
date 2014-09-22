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

package org.pentaho.platform.plugin.services.email;

import junit.framework.TestCase;
import org.apache.commons.io.FilenameUtils;
import org.pentaho.platform.api.email.IEmailConfiguration;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemEntryPoint;
import org.pentaho.platform.api.engine.IPentahoSystemExitPoint;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.EmailResource;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Unit tests the EmailResource REST services
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class EmailServiceTest extends TestCase {

  private File tempDir = null;
  private File defaultConfigFile = null;
  private MicroPlatform mp;
  @Override
  public void setUp() throws Exception {
    mp = new MicroPlatform();
    mp.defineInstance( IAuthorizationPolicy.class, new TestAuthorizationPolicy() );
    mp.start();

    // Setup the temp directory
    tempDir = File.createTempFile( "EmailServiceTest", "" );
    assertTrue( "Error setting up testing scenario", tempDir.delete() );
    assertTrue( "Error setting up testing scenario", tempDir.mkdir() );
    tempDir.deleteOnExit();

    final File systemDir = new File( tempDir, "system" );
    assertTrue( "Error setting up testing scenario", systemDir.mkdir() );
    final File configDir = new File( systemDir, "smtp-email" );
    assertTrue( "Error setting up testing scenario", configDir.mkdir() );
    defaultConfigFile = new File( configDir, "email_config.xml" );
    assertTrue( "Error setting up testing scenario", defaultConfigFile.createNewFile() );

    PentahoSystem.setApplicationContext( new MockApplicationContext( tempDir.getAbsolutePath() ) );
  }

  public void testConstruction() throws Exception {
    // null File parameter
    try {
      new EmailResource( null );
      fail( "Null file should throw an exception" );
    } catch ( IllegalArgumentException success ) {
      // ignore
    }

    // Parent directory doesn't exist
    try {
      final File tempFile = new File( tempDir, "EmailResourceTest1" );
      new EmailService( new File( tempFile, "email_config.xml" ) );
      fail( "Exception should be thrown when parent directory of file provided doesn't exist" );
    } catch ( IllegalArgumentException success ) {
      // ignore
    }

    // File exists but is a directory
    try {
      new EmailService( tempDir );
      fail( "Exception should be thrown when providing a filename that is a directory" );
    } catch ( IllegalArgumentException success ) {
      // ignore
    }

    // Parent exists, but is not a directory
    try {
      final File tempFile = new File( tempDir, "EmailResourceTest2" );
      assertTrue( "Testing scenario could not be setup correctly", tempFile.createNewFile() );
      new EmailService( new File( tempFile, "email_config.xml" ) );
      fail( "Exception should be thrown when parent directory exists but is a file" );
    } catch ( IllegalArgumentException success ) {
      // ignore
    }

    // File doesn't exist (but is ok)
    final File tempFile = new File( tempDir, "temp.xml" );
    assertFalse( "Testing scenario not setup correctly", tempFile.exists() );
    new EmailService( tempFile );

    // File exists (but it is ok)
    assertTrue( "Testing scenario could not be setup correctly", tempFile.createNewFile() );
    new EmailService( tempFile );

    // Default parameters

    // This should work
    new EmailResource();

  }

  public void testEmailConfig() throws Exception {
    assertTrue( defaultConfigFile.delete() );
    assertFalse( defaultConfigFile.exists() );

    final EmailResource emailResource = new EmailResource();
    final IEmailConfiguration emptyEmailConfig = emailResource.getEmailConfig();
    assertTrue( new EmailConfiguration().equals( emptyEmailConfig ) );

    // Create an email config to save
    assertFalse( defaultConfigFile.exists() );
    final EmailConfiguration newEmailConfig = new EmailConfiguration();
    newEmailConfig.setSmtpProtocol( "smtp" );
    newEmailConfig.setSmtpPort( 35 );
    newEmailConfig.setAuthenticate( true );
    newEmailConfig.setUserId( "test_user" );
    final Response OK_RESPONSE = Response.ok().build();
    final Response actual = emailResource.setEmailConfig( newEmailConfig );
    assertEquals( OK_RESPONSE.getStatus(), actual.getStatus() );

    // Get the email config and compare the values
    assertTrue( defaultConfigFile.exists() );
    final IEmailConfiguration actualEmailConfig = emailResource.getEmailConfig();
    assertTrue( newEmailConfig.equals( actualEmailConfig ) );

    // Update the config
    newEmailConfig.setSmtpPort( 36 );
    newEmailConfig.setUserId( "" );
    newEmailConfig.setPassword( "password" );
    assertEquals( OK_RESPONSE.getStatus(), emailResource.setEmailConfig( newEmailConfig ).getStatus() );
    assertTrue( newEmailConfig.equals( emailResource.getEmailConfig() ) );
  }

  public void testSendTestEmail() throws Exception {
    // Testing with a blank config should fail
    final EmailResource emailResource = new EmailResource();
    final EmailConfiguration blankEmailConfig = new EmailConfiguration();
    try {
      emailResource.sendEmailTest( blankEmailConfig );
      fail( "Testing with a blank email config should fail" );
    } catch ( Throwable success ) {
      // ignore
    }
  }

  /**
   * Mock Application Context used for testing
   */
  private class MockApplicationContext implements IApplicationContext {
    final String rootPath;

    private MockApplicationContext( final String rootPath ) {
      this.rootPath = rootPath;
    }

    @Override
    public String getSolutionPath( final String path ) {
      return FilenameUtils.concat( rootPath, path );
    }

    @Override
    public String getSolutionRootPath() {
      return rootPath;
    }

    @Override
    public String getFileOutputPath( final String path ) {
      return null;
    }

    @Override
    public String getPentahoServerName() {
      return null;
    }

    @Override
    public String getBaseUrl() {
      return null;
    }

    @Override
    public String getFullyQualifiedServerURL() {
      return null;
    }

    @Override
    public String getApplicationPath( final String path ) {
      return null;
    }

    @Override
    public String getProperty( final String key ) {
      return null;
    }

    @Override
    public String getProperty( final String key, final String defaultValue ) {
      return null;
    }

    @Override
    public void addEntryPointHandler( final IPentahoSystemEntryPoint entryPoint ) {
    }

    @Override
    public void removeEntryPointHandler( final IPentahoSystemEntryPoint entryPoint ) {
    }

    @Override
    public void addExitPointHandler( final IPentahoSystemExitPoint exitPoint ) {
    }

    @Override
    public void removeExitPointHandler( final IPentahoSystemExitPoint exitPoint ) {
    }

    @Override
    public void invokeEntryPoints() {
    }

    @Override
    public void invokeExitPoints() {
    }

    @Override
    public void setFullyQualifiedServerURL( final String url ) {
    }

    @Override
    public void setBaseUrl( final String url ) {
    }

    @Override
    public void setSolutionRootPath( final String path ) {
    }

    @Override
    public Object getContext() {
      return null;
    }

    @Override
    public void setContext( final Object context ) {
    }

    @Override
    public File createTempFile( final IPentahoSession session, final String prefix, final String extension,
        final File parentDir, final boolean trackFile ) throws IOException {
      return null;
    }

    @Override
    public File createTempFile( final IPentahoSession session, final String prefix, final String extension,
        final boolean trackFile ) throws IOException {
      return null;
    }
  }
  class TestAuthorizationPolicy implements IAuthorizationPolicy {

    @Override
    public boolean isAllowed( String actionName ) {
      // TODO Auto-generated method stub
      return true;
    }

    @Override
    public List<String> getAllowedActions( String actionNamespace ) {
      // TODO Auto-generated method stub
      return null;
    }
  }
}
