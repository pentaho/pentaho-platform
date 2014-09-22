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

package org.pentaho.platform.web.http.api.resources;

import junit.framework.TestCase;
import org.pentaho.platform.api.email.IEmailConfiguration;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.plugin.services.email.EmailConfiguration;
import org.pentaho.platform.plugin.services.email.EmailService;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class EmailResourceTest extends TestCase {
  private EmailResource emailResource = null;
  private File defaultConfigFile = null;
  private final int OK_STATUS = Response.ok().build().getStatus();
  private final EmailConfiguration BLANK_CONFIG = new EmailConfiguration();
  private MicroPlatform mp;

  @Override
  protected void setUp() throws Exception {
    // Setup the temp email config file
    mp = new MicroPlatform();
    mp.defineInstance( IAuthorizationPolicy.class, new TestAuthorizationPolicy() );
    mp.start();

    defaultConfigFile = File.createTempFile( "email_config_", ".xml" );
    this.emailResource = new EmailResource( new EmailService( defaultConfigFile ) );
  }

  public void testEmailConfig() throws Exception {

    try {
      new EmailResource( null );
      fail( "Exception should be thrown when a null EmailService is provided" );
    } catch ( IllegalArgumentException success ) {
      //ignore
    }

    try {
      new EmailResource( new EmailService( new File( defaultConfigFile, "cannot.exist" ) ) );
      fail( "Exception should be thrown when an invalid EmailService is provided" );
    } catch ( IllegalArgumentException success ) {
      //ignore
    }

    IEmailConfiguration emptyConfig = emailResource.getEmailConfig();
    assertTrue( BLANK_CONFIG.equals( emptyConfig ) );

    Response response = emailResource.setEmailConfig( new EmailConfiguration() );
    assertEquals( OK_STATUS, response.getStatus() );
    emptyConfig = emailResource.getEmailConfig();
    assertTrue( BLANK_CONFIG.equals( emptyConfig ) );

    final EmailConfiguration emailConfigOriginal =
        new EmailConfiguration( true, false, "test@pentaho.com", "Pentaho Scheduler", null, 36,
            "", true, "user", null, false, true );
    response = emailResource.setEmailConfig( emailConfigOriginal );
    assertEquals( OK_STATUS, response.getStatus() );

    final IEmailConfiguration emailConfigNew = emailResource.getEmailConfig();
    assertTrue( emailConfigOriginal.equals( emailConfigNew ) );

  }

  public void testSendEmailTest() throws Exception {

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
