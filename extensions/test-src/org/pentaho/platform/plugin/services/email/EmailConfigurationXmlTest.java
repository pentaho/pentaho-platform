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
import org.dom4j.Document;

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

    // Construction from String

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

    // Construction from file

    final File emailConfigFile = getSampleEmailConfigFile( "testdata/email_config_1.xml" );
    emailConfiguration = new EmailConfigurationXml( emailConfigFile );
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

  }

  public void testGetDocument() throws Exception {

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

    // Load from a file
    final File emailConfigFile = getSampleEmailConfigFile( "testdata/email_config_2.xml" );
    final EmailConfigurationXml tempEmailConfiguration = new EmailConfigurationXml( emailConfigFile );
    final Document document = tempEmailConfiguration.getDocument();
    emailConfiguration = new EmailConfigurationXml( document );
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
