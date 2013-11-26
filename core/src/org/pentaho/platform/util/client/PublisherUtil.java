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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.util.client;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.util.messages.Messages;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PublisherUtil {

  protected static final Log logger = LogFactory.getLog( PublisherUtil.class );

  public static final int FILE_EXISTS = 1;

  public static final int FILE_ADD_FAILED = 2;

  public static final int FILE_ADD_SUCCESSFUL = 3;

  public static final int FILE_ADD_INVALID_PUBLISH_PASSWORD = 4;

  public static final int FILE_ADD_INVALID_USER_CREDENTIALS = 5;

  /**
   * Publishes a list of files and a datasource to the server with basic authentication to the server
   * 
   * @param publishURL
   *          The URL of the Pentaho server
   * @param publishPath
   *          The path in the solution to place the files
   * @param publishFiles
   *          Array of File objects to post to the server
   * @param dataSource
   *          The datasource to publish to the server
   * @param publishPassword
   *          The publishing password for the server
   * @param serverUserid
   *          The userid to authenticate to the server
   * @param serverPassword
   *          The password to authenticate with the server
   * @param overwrite
   *          Whether the server should overwrite the file if it exists already
   * @return Server response as a string
   */
  public static int publish( final String publishURL, final String publishPath, final File[] publishFiles,
      final String publishPassword, final String serverUserid, final String serverPassword, final boolean overwrite ) {
    return PublisherUtil.publish( publishURL, publishPath, publishFiles, publishPassword, serverUserid, serverPassword,
        overwrite, true );
  }

  /**
   * Publishes a list of files and a datasource to the server with basic authentication to the server
   * 
   * @param publishURL
   *          The URL of the Pentaho server
   * @param publishPath
   *          The path in the solution to place the files
   * @param publishFiles
   *          Array of File objects to post to the server
   * @param dataSource
   *          The datasource to publish to the server
   * @param publishPassword
   *          The publishing password for the server
   * @param serverUserid
   *          The userid to authenticate to the server
   * @param serverPassword
   *          The password to authenticate with the server
   * @param overwrite
   *          Whether the server should overwrite the file if it exists already
   * @param mkdirs
   *          Whether the server should create any missing folders on the publish path
   * @return Server response as a string
   */
  public static int publish( final String publishURL, final String publishPath, final File[] publishFiles,
      final String publishPassword, final String serverUserid, final String serverPassword, final boolean overwrite,
      final boolean mkdirs ) {
    int status = -1;
    System.setProperty( "org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog" ); //$NON-NLS-1$ //$NON-NLS-2$
    System.setProperty( "org.apache.commons.logging.simplelog.showdatetime", "true" ); //$NON-NLS-1$ //$NON-NLS-2$
    System.setProperty( "org.apache.commons.logging.simplelog.log.httpclient.wire.header", "warn" ); //$NON-NLS-1$ //$NON-NLS-2$
    System.setProperty( "org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "warn" ); //$NON-NLS-1$ //$NON-NLS-2$

    String fullURL = null;
    try {
      fullURL = publishURL + "?publishPath=" + URLEncoder.encode( publishPath, "UTF-8" );
    } catch ( UnsupportedEncodingException e ) {
      fullURL = publishURL + "?publishPath=" + publishPath;
    }
    if ( publishPassword == null ) {
      throw new IllegalArgumentException( Messages.getInstance().getErrorString(
          "PUBLISHERUTIL.ERROR_0001_PUBLISH_PASSWORD_REQUIRED" ) ); //$NON-NLS-1$
    }

    fullURL += "&publishKey=" + PublisherUtil.getPasswordKey( publishPassword ); //$NON-NLS-1$
    fullURL += "&overwrite=" + overwrite; //$NON-NLS-1$
    fullURL += "&mkdirs=" + mkdirs; //$NON-NLS-1$

    PostMethod filePost = new PostMethod( fullURL );
    Part[] parts = new Part[publishFiles.length];
    for ( int i = 0; i < publishFiles.length; i++ ) {
      try {
        File file = publishFiles[i];
        FileInputStream in = new FileInputStream( file );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy( in, out );
        String reportNameEncoded = ( URLEncoder.encode( file.getName(), "UTF-8" ) );
        ByteArrayPartSource source = new ByteArrayPartSource( reportNameEncoded, out.toByteArray() );
        parts[i] = new FilePart( reportNameEncoded, source, FilePart.DEFAULT_CONTENT_TYPE, "UTF-8" );
      } catch ( Exception e ) {
        PublisherUtil.logger.error( null, e );
      }
    }
    filePost.setRequestEntity( new MultipartRequestEntity( parts, filePost.getParams() ) );
    HttpClient client = new HttpClient();
    try {
      // If server userid/password was supplied, use basic authentication to
      // authenticate with the server.
      if ( ( serverUserid != null ) && ( serverUserid.length() > 0 ) && ( serverPassword != null )
          && ( serverPassword.length() > 0 ) ) {
        Credentials creds = new UsernamePasswordCredentials( serverUserid, serverPassword );
        client.getState().setCredentials( AuthScope.ANY, creds );
        client.getParams().setAuthenticationPreemptive( true );
      }
      status = client.executeMethod( filePost );

      if ( status == HttpStatus.SC_OK ) {
        String postResult = filePost.getResponseBodyAsString();

        if ( postResult != null ) {
          try {
            return Integer.parseInt( postResult.trim() );
          } catch ( NumberFormatException e ) {
            PublisherUtil.logger.error( null, e );
            return PublisherUtil.FILE_ADD_INVALID_USER_CREDENTIALS;
          }
        }
      } else if ( status == HttpStatus.SC_UNAUTHORIZED ) {
        return PublisherUtil.FILE_ADD_INVALID_USER_CREDENTIALS;
      }
    } catch ( HttpException e ) {
      PublisherUtil.logger.error( null, e );
    } catch ( IOException e ) {
      PublisherUtil.logger.error( null, e );
    }
    // return Messages.getString("REPOSITORYFILEPUBLISHER.USER_PUBLISHER_FAILED"); //$NON-NLS-1$
    return PublisherUtil.FILE_ADD_FAILED;
  }

  /**
   * Utility for getting the MD5 hash from the provided key for sending the publishPassword.
   * 
   * @param passWord
   *          The password to get an MD5 hash of
   * @return zero-padded MD5 hash of the password
   */
  public static final String getPasswordKey( final String passWord ) {
    try {
      MessageDigest md = MessageDigest.getInstance( "MD5" ); //$NON-NLS-1$
      md.reset(); // Reset the algorithm
      md.update( passWord.getBytes() ); // Update the algorithm with the e-mail
      // Update the algorithm with a known "key" for keyed MD5
      // It basically adds the new key to the end and computes
      byte[] digest = md.digest( "P3ntah0Publ1shPa55w0rd".getBytes() ); //$NON-NLS-1$
      StringBuffer buf = new StringBuffer();
      String s;
      for ( byte element : digest ) {
        s = Integer.toHexString( 0xFF & element );
        buf.append( ( s.length() == 1 ) ? "0" : "" ).append( s ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      return buf.toString(); // Return MD5 string
    } catch ( NoSuchAlgorithmException ex ) {
      PublisherUtil.logger.error( null, ex );
      return null;
    }
  }

}
