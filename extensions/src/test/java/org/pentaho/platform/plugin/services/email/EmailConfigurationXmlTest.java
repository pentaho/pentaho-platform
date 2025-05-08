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


package org.pentaho.platform.plugin.services.email;

import junit.framework.TestCase;
import org.dom4j.Document;
import org.pentaho.di.core.KettleEnvironment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Test class for the {@link }EmailConfigurationXml} class
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class EmailConfigurationXmlTest extends TestCase {
  public void testConstruction() throws Exception {
    // Invalid parameters to constructor
    try {
      new EmailConfigurationXml( (File) null );
      fail();
    } catch ( IllegalArgumentException success ) {
      // ignore
    }

    try {
      new EmailConfigurationXml( (String) null );
      fail();
    } catch ( Exception success ) {
      // ignore
    }

    try {
      new EmailConfigurationXml( (Document) null );
      fail();
    } catch ( Exception success ) {
      // ignore
    }
  }

  public void testEmailConfigFileAsString() throws Exception {
    // Construction from String
    KettleEnvironment.init();
    final String emailConfig = getSampleEmailConfigFileAsString( "testdata/email_config_1.xml" );
    EmailConfigurationXml emailConfiguration = new EmailConfigurationXml( emailConfig );
    assertEquals( "mailserver.sampleserver.com", emailConfiguration.getSmtpHost() );
    assertEquals( 25, emailConfiguration.getSmtpPort().intValue() );
    assertEquals( "smtp", emailConfiguration.getSmtpProtocol() );
    assertEquals( true, emailConfiguration.isUseStartTls() );
    assertEquals( true, emailConfiguration.isAuthenticate() );
    assertEquals( true, emailConfiguration.isUseSsl() );
    assertEquals( true, emailConfiguration.isDebug() );
    assertEquals( true, emailConfiguration.isSmtpQuitWait() );
    assertEquals( "sampleuser@sampleserver.com", emailConfiguration.getDefaultFrom() );
    assertEquals( "sampleuser", emailConfiguration.getUserId() );
    assertEquals( "samplepassword", emailConfiguration.getPassword() );
    assertEquals( "basic", emailConfiguration.getAuthMechanism() );
    assertEquals( "samplecid", emailConfiguration.getClientId() );
    assertEquals( "samplesecret", emailConfiguration.getClientSecret() );
    assertEquals( "sampleurl", emailConfiguration.getTokenUrl() );
    assertEquals( "samplescope", emailConfiguration.getScope() );
    assertEquals( "samplegranttype", emailConfiguration.getGrantType() );
    assertEquals( "samplerefreshtoken", emailConfiguration.getRefreshToken() );
    assertEquals( "samplecode", emailConfiguration.getAuthorizationCode() );
    assertEquals( "sampleredirect", emailConfiguration.getRedirectUri() );
  }

  public void testEmailConfigFile() throws Exception {
    // Construction from file
    KettleEnvironment.init();
    final File emailConfigFile = getSampleEmailConfigFile( "testdata/email_config_1.xml" );
    EmailConfigurationXml emailConfiguration = new EmailConfigurationXml( emailConfigFile );
    assertEquals( "mailserver.sampleserver.com", emailConfiguration.getSmtpHost() );
    assertEquals( 25, emailConfiguration.getSmtpPort().intValue() );
    assertEquals( "smtp", emailConfiguration.getSmtpProtocol() );
    assertEquals( true, emailConfiguration.isUseStartTls() );
    assertEquals( true, emailConfiguration.isAuthenticate() );
    assertEquals( true, emailConfiguration.isUseSsl() );
    assertEquals( true, emailConfiguration.isDebug() );
    assertEquals( true, emailConfiguration.isSmtpQuitWait() );
    assertEquals( "sampleuser@sampleserver.com", emailConfiguration.getDefaultFrom() );
    assertEquals( "sampleuser", emailConfiguration.getUserId() );
    assertEquals( "samplepassword", emailConfiguration.getPassword() );
    assertEquals( "basic", emailConfiguration.getAuthMechanism() );
    assertEquals( "samplecid", emailConfiguration.getClientId() );
    assertEquals( "samplesecret", emailConfiguration.getClientSecret() );
    assertEquals( "sampleurl", emailConfiguration.getTokenUrl() );
    assertEquals( "samplescope", emailConfiguration.getScope() );
    assertEquals( "samplegranttype", emailConfiguration.getGrantType() );
    assertEquals( "samplerefreshtoken", emailConfiguration.getRefreshToken() );
    assertEquals( "samplecode", emailConfiguration.getAuthorizationCode() );
    assertEquals( "sampleredirect", emailConfiguration.getRedirectUri() );

  }

  public void testGetBlankDocument() throws Exception {

    KettleEnvironment.init();
    // Empty configuration
    final EmailConfiguration blankEmailConfig = new EmailConfiguration();
    final Document blankDocument = EmailConfigurationXml.getDocument( blankEmailConfig );
    EmailConfigurationXml emailConfiguration = new EmailConfigurationXml( blankDocument );
    assertEquals( "", emailConfiguration.getSmtpHost() );
    assertEquals( Integer.MIN_VALUE, emailConfiguration.getSmtpPort().intValue() );
    assertEquals( "", emailConfiguration.getSmtpProtocol() );
    assertEquals( false, emailConfiguration.isUseStartTls() );
    assertEquals( false, emailConfiguration.isAuthenticate() );
    assertEquals( false, emailConfiguration.isUseSsl() );
    assertEquals( false, emailConfiguration.isDebug() );
    assertEquals( false, emailConfiguration.isSmtpQuitWait() );
    assertEquals( "", emailConfiguration.getDefaultFrom() );
    assertEquals( "", emailConfiguration.getUserId() );
    assertEquals( "", emailConfiguration.getPassword() );
    assertEquals( "", emailConfiguration.getAuthMechanism() );
    assertEquals( "", emailConfiguration.getClientId() );
    assertEquals( "", emailConfiguration.getClientSecret() );
    assertEquals( "", emailConfiguration.getTokenUrl() );
    assertEquals( "", emailConfiguration.getScope() );
    assertEquals( "", emailConfiguration.getGrantType() );
    assertEquals( "", emailConfiguration.getRefreshToken() );
    assertEquals( "", emailConfiguration.getAuthorizationCode() );
    assertEquals( "", emailConfiguration.getRedirectUri() );
  }

  public void testGetDocument() throws Exception {
    // Load from a file
    final File emailConfigFile = getSampleEmailConfigFile( "testdata/email_config_2.xml" );
    final EmailConfigurationXml tempEmailConfiguration = new EmailConfigurationXml( emailConfigFile );
    final Document document = tempEmailConfiguration.getDocument();
    EmailConfigurationXml emailConfiguration = new EmailConfigurationXml( document );
    assertEquals( "mailserver.sampleserver.com", emailConfiguration.getSmtpHost() );
    assertEquals( 99, emailConfiguration.getSmtpPort().intValue() );
    assertEquals( "smtp", emailConfiguration.getSmtpProtocol() );
    assertEquals( false, emailConfiguration.isUseStartTls() );
    assertEquals( true, emailConfiguration.isAuthenticate() );
    assertEquals( false, emailConfiguration.isUseSsl() );
    assertEquals( false, emailConfiguration.isDebug() );
    assertEquals( false, emailConfiguration.isSmtpQuitWait() );
    assertEquals( "", emailConfiguration.getDefaultFrom() );
    assertEquals( "sampleuser", emailConfiguration.getUserId() );
    assertEquals( "", emailConfiguration.getPassword() );
    assertEquals( "", emailConfiguration.getAuthMechanism() );
    assertEquals( "", emailConfiguration.getClientId() );
    assertEquals( "", emailConfiguration.getClientSecret() );
    assertEquals( "", emailConfiguration.getTokenUrl() );
    assertEquals( "", emailConfiguration.getScope() );
    assertEquals( "", emailConfiguration.getGrantType() );
    assertEquals( "", emailConfiguration.getRefreshToken() );
    assertEquals( "", emailConfiguration.getAuthorizationCode() );
    assertEquals( "", emailConfiguration.getRedirectUri() );
  }

  public void testPasswordEncryption() throws Exception {
    final File emailConfigFile = getSampleEmailConfigFile( "testdata/email_config_1.xml" );
    EmailConfigurationXml emailConfiguration = new EmailConfigurationXml( emailConfigFile );
    Document xmlDoc = emailConfiguration.getDocument();
    //Verify password is stored in xml in encrypted form
    String xmlString = xmlDoc.asXML();
    assertTrue( xmlString.contains( "password>Encrypted 2be98dca907d79e81bb18bd63c99dbdde" ) );
    //and that it comes back decrypted
    assertEquals( "samplepassword", emailConfiguration.getPassword() );
  }

  private static final File getSampleEmailConfigFile( final String filename ) {
    return new File( filename );
  }

  private static final String getSampleEmailConfigFileAsString( final String filename ) throws IOException {
    BufferedInputStream f = null;
    try {
      final File configFile = getSampleEmailConfigFile( filename );
      final long size = configFile.length();
      byte[] buffer = new byte[(int) ( size > 0 ? size : 0 )];
      f = new BufferedInputStream( new FileInputStream( configFile ) );
      f.read( buffer );
      return new String( buffer );
    } finally {
      if ( f != null ) {
        f.close();
      }
    }
  }
}
