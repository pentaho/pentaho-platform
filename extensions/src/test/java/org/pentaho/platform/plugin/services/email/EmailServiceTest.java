/*!
 *
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
 *
 * Copyright (c) 2023 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.email;

import junit.framework.TestCase;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang.ObjectUtils;
import org.apache.http.Consts;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.platform.api.email.EmailServiceException;
import org.pentaho.platform.api.email.IEmailAuthenticationResponse;
import org.pentaho.platform.api.email.IEmailConfiguration;
import org.pentaho.platform.util.EmailConstants;
import org.pentaho.platform.util.MockMail;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.HttpMethod;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@RunWith( MockitoJUnitRunner.class )
public class EmailServiceTest extends TestCase {

  private EmailService emailService = null;

  private File defaultConfigFile = null;

  private static EmailConfiguration BLANK_CONFIG = new EmailConfiguration();

  @Before
  public void setUp() throws Exception {
    defaultConfigFile = File.createTempFile( "email_config_", ".xml" );
    this.emailService = new EmailService( defaultConfigFile );
    MockMail.clear();
  }


  @Test
  public void testSendMailGraphApi() throws HttpException, IOException, InterruptedException {
    MockWebServer server = new MockWebServer();
    server.enqueue( new MockResponse().setResponseCode( HttpStatus.SC_ACCEPTED ) );

    String graphApiUrl = "/v1.0/users/test@pentaho.com/sendMail";
    String mockFullUrlGraphApi = server.url( graphApiUrl ).toString();
    emailService.sendMailGraphApi( mockFullUrlGraphApi, "token", "message" );

    RecordedRequest request1 = server.takeRequest();
    assertEquals( graphApiUrl, request1.getPath() );
    assertEquals( HttpMethod.POST, request1.getMethod() );
    String message = request1.getBody().readUtf8();
    assertEquals( "message", message );
    String token = request1.getHeader( "Authorization" );
    assertEquals( "Bearer token", token );

    server.shutdown();
  }

  @Test
  public void testIsValidEmailConfigSmtpBasic() {

    final EmailConfiguration emailConfigOriginal =
            new EmailConfiguration( true, false, "test@pentaho.com", "Pentaho Scheduler", "smtp.com", 36,
                    EmailConstants.PROTOCOL_SMTP, true, "user", "password", false, true );
    boolean response = emailService.isValid( emailConfigOriginal );
    assertTrue( response );

    emailConfigOriginal.setUserId( "" );
    assertEquals( false, emailService.isValid( emailConfigOriginal ) );

    emailConfigOriginal.setUserId( "user" );
    emailConfigOriginal.setPassword( "" );
    assertEquals( false, emailService.isValid( emailConfigOriginal ) );

  }

  @Test
  public void testIsValidEmailConfigClientCredentials() {

    final EmailConfiguration emailConfigOriginal =
            new EmailConfiguration( true, false, "test@pentaho.com", "Pentaho Scheduler", "", 36,
                    EmailConstants.PROTOCOL_SMTP, true, "user", "password", false, true );

    emailConfigOriginal.setAuthMechanism( EmailConstants.AUTH_TYPE_XOAUTH2 );
    emailConfigOriginal.setGrantType( EmailConstants.GRANT_TYPE_CLIENT_CREDENTIALS );
    emailConfigOriginal.setClientId( "cid" );
    emailConfigOriginal.setClientSecret( "clientsecret" );

    boolean response = emailService.isValid( emailConfigOriginal );
    assertEquals( true, response );

    emailConfigOriginal.setClientId( "" );
    assertEquals( false, emailService.isValid( emailConfigOriginal ) );

    emailConfigOriginal.setClientId( "cid" );
    emailConfigOriginal.setClientSecret( "" );
    assertEquals( false, emailService.isValid( emailConfigOriginal ) );

    emailConfigOriginal.setClientSecret( "secret" );
    emailConfigOriginal.setDefaultFrom( "" );
    assertEquals( false, emailService.isValid( emailConfigOriginal ) );

  }

  @Test
  public void testIsValidEmailConfigRefreshToken() {

    final EmailConfiguration emailConfigOriginal =
            new EmailConfiguration( true, false, "test@pentaho.com", "Pentaho Scheduler", "", 36,
                    EmailConstants.PROTOCOL_SMTP, true, "user", "password", false, true );

    emailConfigOriginal.setAuthMechanism( EmailConstants.AUTH_TYPE_XOAUTH2 );
    emailConfigOriginal.setClientId( "cid" );
    emailConfigOriginal.setClientSecret( "clientsecret" );
    emailConfigOriginal.setGrantType( EmailConstants.GRANT_TYPE_REFRESH_TOKEN );
    emailConfigOriginal.setRefreshToken( "rtoken" );
    boolean response = emailService.isValid( emailConfigOriginal );
    assertEquals( true, response );

    emailConfigOriginal.setClientId( "" );
    assertEquals( false, emailService.isValid( emailConfigOriginal ) );

    emailConfigOriginal.setClientId( "cid" );
    emailConfigOriginal.setClientSecret( "" );
    assertEquals( false, emailService.isValid( emailConfigOriginal ) );

    emailConfigOriginal.setClientSecret( "secret" );
    emailConfigOriginal.setDefaultFrom( "" );
    assertEquals( false, emailService.isValid( emailConfigOriginal ) );

    emailConfigOriginal.setDefaultFrom( "from" );
    emailConfigOriginal.setRefreshToken( "" );
    assertEquals( false, emailService.isValid( emailConfigOriginal ) );

  }

  @Test
  public void testIsValidEmailConfigAuthCode() {

    final EmailConfiguration emailConfigOriginal =
            new EmailConfiguration( true, false, "test@pentaho.com", "Pentaho Scheduler", "", 36,
                    EmailConstants.PROTOCOL_SMTP, true, "user", "password", false, true );
    emailConfigOriginal.setAuthMechanism( EmailConstants.AUTH_TYPE_XOAUTH2 );
    emailConfigOriginal.setClientId( "cid" );
    emailConfigOriginal.setClientSecret( "clientsecret" );
    emailConfigOriginal.setAuthorizationCode( "code" );
    emailConfigOriginal.setGrantType( EmailConstants.GRANT_TYPE_AUTH_CODE );
    emailConfigOriginal.setRedirectUri( "uri" );
    boolean response = emailService.isValid( emailConfigOriginal );
    assertEquals( true, response );

    emailConfigOriginal.setClientId( "" );
    assertEquals( false, emailService.isValid( emailConfigOriginal ) );

    emailConfigOriginal.setClientId( "cid" );
    emailConfigOriginal.setClientSecret( "" );
    assertEquals( false, emailService.isValid( emailConfigOriginal ) );

    emailConfigOriginal.setClientSecret( "secret" );
    emailConfigOriginal.setDefaultFrom( "" );
    assertEquals( false, emailService.isValid( emailConfigOriginal ) );

    emailConfigOriginal.setDefaultFrom( "from" );
    emailConfigOriginal.setAuthorizationCode( "" );
    assertEquals( false, emailService.isValid( emailConfigOriginal ) );

    emailConfigOriginal.setAuthorizationCode( "auth_code" );
    emailConfigOriginal.setRedirectUri( "" );
    assertEquals( false, emailService.isValid( emailConfigOriginal ) );
  }

  @Test
  public void testIsValidEmailConfigNoAuth() {

    final EmailConfiguration emailConfigOriginal =
            new EmailConfiguration( false, false, "test@pentaho.com", "", "smtp.com", 36,
                    EmailConstants.PROTOCOL_SMTP, true, "", "", false, true );
    boolean response = emailService.isValid( emailConfigOriginal );
    assertEquals( true, response );
  }

  @Test
  public void testInvalidEmailConfig() {

    final EmailConfiguration emailConfigOriginal =
            new EmailConfiguration( true, false, "", "", "smtp.com", 36,
                    EmailConstants.PROTOCOL_SMTP, true, "", "", false, true );

    boolean response = emailService.isValid( emailConfigOriginal );
    assertEquals( false, response );
  }

  @Test
  public void testSendEmailTestNoAuth() throws Exception {
    EmailConfiguration emailConfigOriginal =
            new EmailConfiguration( false, false, "test@pentaho.com", "Pentaho Scheduler", "", 25,
                    EmailConstants.PROTOCOL_SMTP, true, "", "", true, true );
    assertEquals( EmailService.TEST_EMAIL_SUCCESS, emailService.sendEmailTest( emailConfigOriginal ) );
    assertEquals( 1, MockMail.size() );
    final Message message = MockMail.get( 0 );
    assertNotNull( message );
    final String content = (String) message.getContent();
    assertNotNull( content );
    assertEquals( true, content.equals( "This is a test message to verify that email is configured properly." ) );
  }

  @Test
  public void testSendEmailGraphApi() throws Exception {
    MockWebServer server = new MockWebServer();
    String filename = "EmailService/EmailAuthenticationResponse1.json";
    String content = readTestFile( filename );

    server.enqueue( new MockResponse()
            .setResponseCode( HttpStatus.SC_OK )
            .setBody( content )
    );

    String urlPathToken = "/common/oauth2/v2.0/token";
    String mockUrlFulToken = server.url( urlPathToken ).toString();

    EmailConfiguration emailConfigOriginal =
            new EmailConfiguration( true, false, "test@pentaho.com", "Pentaho Scheduler", "", 25,
                    EmailConstants.PROTOCOL_GRAPH_API, true, "test", "", true, true );
    emailConfigOriginal.setAuthMechanism( EmailConstants.AUTH_TYPE_XOAUTH2 );
    emailConfigOriginal.setClientId( "cid" );
    emailConfigOriginal.setClientSecret( "secret" );
    emailConfigOriginal.setTokenUrl( mockUrlFulToken  );
    emailConfigOriginal.setScope( "https://graph.microsoft.com/.default" );
    emailConfigOriginal.setGrantType( EmailConstants.GRANT_TYPE_REFRESH_TOKEN );
    emailConfigOriginal.setRefreshToken( "refreshToken" );
    emailService.setEmailConfig( emailConfigOriginal );
    Properties emailProperties = new Properties();
    Session session = Session.getInstance( emailProperties );
    MimeMessage msg = new MimeMessage( session );
    msg.setFrom( new InternetAddress( emailConfigOriginal.getDefaultFrom(), emailConfigOriginal.getFromName() ) );
    msg.setRecipients( Message.RecipientType.TO, InternetAddress.parse( emailConfigOriginal.getDefaultFrom() ) );
    msg.setSubject( "SUBJECT" );
    msg.setText( "EmailService.MESSAGE" );
    msg.setHeader( "X-Mailer", "smtpsend" );
    msg.setSentDate( new Date() );
    try {
      emailService.sendEmail( session, msg );
    } catch ( EmailServiceException e ) {
      //throws Exception as Actual Data not provided to send mail
      assertTrue( e.getMessage().contains( "401 Unauthorized" ) );
    } finally {
      server.shutdown();
    }
  }

  @Test
  public void testSendEmailSmtp() throws Exception {
    KettleEnvironment.init();
    MockWebServer server = new MockWebServer();
    String filename = "EmailService/EmailAuthenticationResponse1.json";
    String content = readTestFile( filename );

    server.enqueue( new MockResponse()
            .setResponseCode( HttpStatus.SC_OK )
            .setBody( content )
    );

    String urlPathToken = "/common/oauth2/v2.0/token";
    String mockUrlFulToken = server.url( urlPathToken ).toString();
    EmailConfiguration emailConfigOriginal =
            new EmailConfiguration( true, false, "test@pentaho.com", "Pentaho Scheduler", "smtp.office365.com", 587,
                    EmailConstants.PROTOCOL_SMTP, true, "PentahoBA_mailTest@Hitachivantara.com", "", true, true );
    emailConfigOriginal.setAuthMechanism( EmailConstants.AUTH_TYPE_XOAUTH2 );

    Properties emailProperties = new Properties();
    emailProperties.setProperty( "mail.smtp.host", emailConfigOriginal.getSmtpHost() );
    emailProperties.setProperty( "mail.smtp.port", ObjectUtils.toString( emailConfigOriginal.getSmtpPort() ) );
    emailProperties.setProperty( "mail.transport.protocol", emailConfigOriginal.getSmtpProtocol() );
    emailProperties.setProperty( "mail.smtp.starttls.enable", ObjectUtils.toString( emailConfigOriginal.isUseStartTls() ) );
    emailProperties.setProperty( "mail.smtp.ssl", ObjectUtils.toString( emailConfigOriginal.isUseSsl() ) );
    emailProperties.setProperty( "mail.debug", ObjectUtils.toString( emailConfigOriginal.isDebug() ) );
    emailConfigOriginal.setClientId( "cid" );
    emailConfigOriginal.setClientSecret( "secret" );
    emailConfigOriginal.setTokenUrl( mockUrlFulToken  );
    emailConfigOriginal.setScope( "https://graph.microsoft.com/.default" );
    emailConfigOriginal.setGrantType( EmailConstants.GRANT_TYPE_AUTH_CODE );
    emailConfigOriginal.setAuthorizationCode( "testCode" );
    emailConfigOriginal.setRedirectUri( "uri" );
    Session session = Session.getInstance( emailProperties );
    MimeMessage msg = new MimeMessage( session );
    msg.setFrom( new InternetAddress( emailConfigOriginal.getDefaultFrom(), emailConfigOriginal.getFromName() ) );
    msg.setRecipients( Message.RecipientType.TO, InternetAddress.parse( emailConfigOriginal.getDefaultFrom() ) );
    msg.setSubject( "SUBJECT" );
    msg.setText( "EmailService.MESSAGE" );
    msg.setHeader( "X-Mailer", "smtpsend" );
    msg.setSentDate( new Date() );
    emailService.setEmailConfig( emailConfigOriginal );
    try {
      emailService.sendEmail( session, msg );
    } catch ( EmailServiceException e ) {
      //throws Exception as actual data not provided to send mail
      assertTrue( e.getMessage().contains( "Authentication unsuccessful" ) );
    } finally {
      server.shutdown();
    }
    EmailConfiguration emailConfiguration = emailService.getEmailConfig();
    assertEquals( "testSomeRefreshToken1", emailConfiguration.getRefreshToken() );
    assertEquals( "refresh_token", emailConfiguration.getGrantType() );
    assertEquals( "", emailConfiguration.getAuthorizationCode() );

  }


  @Test( expected = IllegalArgumentException.class )
  public void testSetEmailConfigFile() {
    emailService.setEmailConfigFile( null );
  }

  @Test
  public void testGetOAuthTokenAuthCode_mockHttpServer() throws Exception {
    MockWebServer server = new MockWebServer();
    String filename = "EmailService/EmailAuthenticationResponse1.json";
    String content = readTestFile( filename );


    server.enqueue( new MockResponse()
            .setResponseCode( HttpStatus.SC_OK )
            .setBody( content )
    );

    String urlPathToken = "/common/oauth2/v2.0/token";
    String mockUrlFulToken = server.url( urlPathToken ).toString();

    EmailConfiguration emailConfigOriginal =
            new EmailConfiguration( true, false, "tes@test.com", "Pentaho Scheduler", "", 25,
                    EmailConstants.PROTOCOL_GRAPH_API, true, "test@test.com", "", true, true );
    emailConfigOriginal.setAuthMechanism( EmailConstants.AUTH_TYPE_XOAUTH2 );
    emailConfigOriginal.setClientId( "cid" );
    emailConfigOriginal.setClientSecret( "secret" );
    emailConfigOriginal.setTokenUrl( mockUrlFulToken  );
    emailConfigOriginal.setScope( "https://graph.microsoft.com/.default" );
    emailConfigOriginal.setGrantType( EmailConstants.GRANT_TYPE_AUTH_CODE );
    emailConfigOriginal.setAuthorizationCode( "testCode" );
    emailConfigOriginal.setRedirectUri( "uri" );

    IEmailAuthenticationResponse actualResult = emailService.getOAuthToken( emailConfigOriginal );

    assertEquals( actualResult.getAccessToken(), "testSomeToken1" );
    assertEquals( actualResult.getRefreshToken(), "testSomeRefreshToken1" );

    RecordedRequest request1 = server.takeRequest();
    assertEquals( urlPathToken, request1.getPath() );
    assertEquals( HttpMethod.POST, request1.getMethod() );

    String requestBody = request1.getBody().readUtf8();
    Map<String, String> mapRequestBody = convertToMap( requestBody );
    assertEquals(  urlEncodeValue( EmailConstants.GRANT_TYPE_AUTH_CODE ), mapRequestBody.get( EmailService.GRANT_TYPE )  );
    assertEquals(  urlEncodeValue( "https://graph.microsoft.com/.default" ), mapRequestBody.get( EmailService.SCOPE ) );
    assertEquals(  urlEncodeValue( "cid" ), mapRequestBody.get( EmailService.CLIENT_ID )  );
    assertEquals(  urlEncodeValue( "secret" ), mapRequestBody.get( EmailService.CLIENT_SECRET )  );
    assertEquals(  urlEncodeValue( "testCode" ), mapRequestBody.get( EmailService.CODE )  );
    assertEquals(  urlEncodeValue( "uri" ), mapRequestBody.get( EmailService.REDIRECT_URI )  );
    assertFalse( mapRequestBody.containsKey( EmailConstants.GRANT_TYPE_REFRESH_TOKEN  ) );

    server.shutdown();

  }

  URL getTestResource( String relativePath ) {
    ClassLoader classLoader = getClass().getClassLoader();
    return classLoader.getResource( relativePath );
  }

  String readTestFile( String filename ) throws Exception {
    byte[] encoded = Files.readAllBytes( Paths.get( getTestResource( filename ).toURI() ) );
    return new String( encoded, Consts.UTF_8 );
  }

  String urlEncodeValue( String value ) throws UnsupportedEncodingException {
    return URLEncoder.encode( value, StandardCharsets.UTF_8.toString() );
  }

  Map<String, String> convertToMap( String input ) {
    return convertToMap( input, "&", "=" );
  }

  Map<String, String> convertToMap( String input, String splitProperty, String splitKeyValue ) {
    Map<String, String> ret = Arrays.stream( input.split( splitProperty ) )
            .map( l -> l.split( splitKeyValue )  )
            .collect( Collectors.toMap(
              m -> m[0], // key
              m -> m[1] // value
            ) );
    return ret;
  }


  @Test
  public void testGetOAuthTokenAuthCode_mockHttpServer_httpException() throws Exception {
    MockWebServer server = new MockWebServer();
    String filename = "EmailService/EmailAuthenticationResponse1.json";
    String content = readTestFile( filename );


    server.enqueue( new MockResponse()
            .setResponseCode( HttpStatus.SC_NOT_FOUND )
            .setBody( content )
    );

    String urlPathToken = "/common/oauth2/v2.0/token";
    String mockUrlFulToken = server.url( urlPathToken ).toString();

    EmailConfiguration emailConfigOriginal =
            new EmailConfiguration( true, false, "tes@test.com", "Pentaho Scheduler", "", 25,
                    EmailConstants.PROTOCOL_GRAPH_API, true, "test@test.com", "", true, true );
    emailConfigOriginal.setAuthMechanism( EmailConstants.AUTH_TYPE_XOAUTH2 );
    emailConfigOriginal.setClientId( "cid" );
    emailConfigOriginal.setClientSecret( "secret" );

    emailConfigOriginal.setTokenUrl( mockUrlFulToken  );
    emailConfigOriginal.setScope( "https://graph.microsoft.com/.default" );
    emailConfigOriginal.setGrantType( EmailConstants.GRANT_TYPE_AUTH_CODE );

    emailConfigOriginal.setAuthorizationCode( "testCode" );
    emailConfigOriginal.setRedirectUri( "uri" );

    try {
      IEmailAuthenticationResponse actualResult = emailService.getOAuthToken( emailConfigOriginal );
    } catch ( HttpException he ) {
      //Throws exception as we have setup the mock http server to get this error.
      assertTrue( he.getMessage().contains( "Unable to get authorization token" ) );
    } finally {
      server.shutdown();
    }

  }

  @Test
  public void testSendEmailTestBasicAuth() throws Exception {
    KettleEnvironment.init();
    EmailConfiguration emailConfigOriginal =
            new EmailConfiguration( true, false, "test@pentaho.com", "Pentaho Scheduler", "smtp.com", 25,
                    EmailConstants.PROTOCOL_SMTP, true, "user", "password", true, true );
    emailConfigOriginal.setAuthMechanism( EmailConstants.AUTH_TYPE_BASIC );

    emailService.sendEmailTest( emailConfigOriginal );
    assertEquals( 1, MockMail.size() );
    final Message message = MockMail.get( 0 );
    assertNotNull( message );
    final String content = (String) message.getContent();
    assertNotNull( content );
    assertEquals( true, content.equals( "This is a test message to verify that email is configured properly." ) );

  }

  @Test
  public  void testEmailServiceSetup() {
    try {
      new EmailService( new File( defaultConfigFile, "cannot.exist" ) );
      fail( "Exception should be thrown when an invalid EmailService is provided" );
    } catch ( IllegalArgumentException success ) {
      //ignore
    }

    IEmailConfiguration emptyConfig = emailService.getEmailConfig();
    assertEquals( true, BLANK_CONFIG.equals( emptyConfig ) );

    emailService.setEmailConfig( new EmailConfiguration() );

    emptyConfig = emailService.getEmailConfig();
    assertEquals( true, BLANK_CONFIG.equals( emptyConfig ) );
  }

}
