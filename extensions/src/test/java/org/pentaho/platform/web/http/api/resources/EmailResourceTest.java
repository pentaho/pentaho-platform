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


package org.pentaho.platform.web.http.api.resources;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.email.IEmailConfiguration;
import org.pentaho.platform.api.email.IEmailService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.plugin.services.email.EmailConfiguration;
import org.pentaho.platform.plugin.services.email.EmailService;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import jakarta.ws.rs.core.Response;
import java.io.File;

import static jakarta.ws.rs.core.Response.Status.OK;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class EmailResourceTest {
  private static EmailResource emailResource = null;
  private static File defaultConfigFile = null;
  private static IEmailService mockEmailService;
  private static IAuthorizationPolicy mockAuthorizationPolicy;
  private static MicroPlatform mp;


  @BeforeClass
  public static void setUpClass() throws Exception {
    mp = new MicroPlatform();

    mockAuthorizationPolicy = Mockito.mock( IAuthorizationPolicy.class );
    when( mockAuthorizationPolicy.isAllowed( anyString() ) ).thenReturn( true );
    when( mockAuthorizationPolicy.getAllowedActions( anyString() ) ).thenReturn(  null );

    mockEmailService = Mockito.mock( IEmailService.class );
    mp.defineInstance( IAuthorizationPolicy.class, mockAuthorizationPolicy );
    mp.defineInstance( IEmailService.class, mockEmailService );
    mp.start();
    // Setup the temp email config file
    defaultConfigFile = File.createTempFile( "email_config_", ".xml" );
    emailResource = new EmailResource( new EmailService( defaultConfigFile ) ); // class under test
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    mp.stop();
  }

  @Test
  public void testConstructor_IEmailService() throws Exception {

    try {
      new EmailResource( null );
      fail( "Exception should be thrown when a null EmailService is provided" );
    } catch ( IllegalArgumentException success ) {
      //ignore
    }
  }


  @Test
  public void testSendEmailTest_Entity_SUCCESS() throws Exception {
    when( mockAuthorizationPolicy.isAllowed( anyString() ) ).thenReturn( true );
    mockEmailService = Mockito.mock( IEmailService.class );
    when( mockEmailService.sendEmailTest( any( IEmailConfiguration.class ) ) )
            .thenReturn( EmailService.TEST_EMAIL_SUCCESS );

    emailResource = new EmailResource( mockEmailService );

    Response ret = emailResource.sendEmailTest( new EmailConfiguration() );
    assertEquals( OK.getStatusCode(), ret.getStatus() );
    assertEquals( EmailService.TEST_EMAIL_SUCCESS, ret.getEntity().toString() );
  }

  @Test
  public void testSendEmailTest_Entity_FAIL() throws Exception {
    when( mockAuthorizationPolicy.isAllowed( anyString() ) ).thenReturn( true );
    mockEmailService = Mockito.mock( IEmailService.class );
    when( mockEmailService.sendEmailTest( any( IEmailConfiguration.class ) ) )
            .thenReturn( EmailService.TEST_EMAIL_FAIL );

    emailResource = new EmailResource( mockEmailService );

    Response ret = emailResource.sendEmailTest( new EmailConfiguration() );
    assertEquals( OK.getStatusCode(), ret.getStatus() );
    assertEquals( EmailService.TEST_EMAIL_FAIL, ret.getEntity().toString() );
  }

  @Test
  public void testSendEmailTest_notAuthorized() throws Exception {
    when( mockAuthorizationPolicy.isAllowed( anyString() ) ).thenReturn( false ); // deny code to get to EmailService

    emailResource = new EmailResource( mockEmailService );

    Response ret = emailResource.sendEmailTest( new EmailConfiguration() );
    assertEquals( UNAUTHORIZED.getStatusCode(), ret.getStatus() );
  }

  @Test
  public void testSetEmailConfig() {
    when( mockAuthorizationPolicy.isAllowed( anyString() ) ).thenReturn( true );
    mockEmailService = Mockito.mock( IEmailService.class );

    emailResource = new EmailResource( mockEmailService );

    Response ret = emailResource.setEmailConfig( new EmailConfiguration() );
    assertEquals( OK.getStatusCode(), ret.getStatus() );
  }

  @Test
  public void testSetEmailConfig_notAuthorized() {
    when( mockAuthorizationPolicy.isAllowed( anyString() ) ).thenReturn( false ); // deny code to get to EmailService

    emailResource = new EmailResource( mockEmailService );

    Response ret = emailResource.setEmailConfig( new EmailConfiguration() );
    assertEquals( UNAUTHORIZED.getStatusCode(), ret.getStatus() );
  }

  @Test
  public void testGetEmailConfig() {
    when( mockAuthorizationPolicy.isAllowed( anyString() ) ).thenReturn( true );
    mockEmailService = Mockito.mock( IEmailService.class );

    emailResource = new EmailResource( mockEmailService );

    IEmailConfiguration emailConfiguration  = emailResource.getEmailConfig( );
    assertNull( emailConfiguration );
  }

  @Test
  public void testGetEmailConfig_notAuthorized() {
    when( mockAuthorizationPolicy.isAllowed( anyString() ) ).thenReturn( false ); // deny code to get to EmailService

    emailResource = new EmailResource( mockEmailService );

    IEmailConfiguration emailConfig = emailResource.getEmailConfig( );
    assertNotNull( emailConfig );
  }
}
