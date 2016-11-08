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

package org.pentaho.platform.api.repository2.unified.data.simple;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * A {@link IRepositoryFileData} that has an input stream, encoding, and optional MIME type.
 * 
 * @author mlowery
 */
public class SimpleRepositoryFileData implements IRepositoryFileData {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final long serialVersionUID = -1571991472814251230L;

  // ~ Instance fields
  // =================================================================================================

  private InputStream stream;

  private String encoding;

  private String mimeType;

  // ~ Constructors
  // ====================================================================================================

  public SimpleRepositoryFileData( final InputStream stream, final String encoding, final String mimeType ) {
    super();
    this.stream = stream;
    this.encoding = encoding;
    this.mimeType = mimeType;
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * Returns a stream for reading the data in this file.
   * 
   * @return stream (may be {@code null})
   * @deprecated Use {@link #getInputStream()} instead. Proguard likes to obfuscate any method named getStream that
   *             returns an InputStream, even if you tell it not to via keep rules.
   */
  @Deprecated
  public InputStream getStream() {
    return stream;
  }

  /**
   * Returns a stream for reading the data in this file.
   * 
   * @return stream (may be {@code null})
   */
  public InputStream getInputStream() {
    return stream;
  }

  /**
   * Returns the character encoding of the bytes in the data stream. May be {@code null} for non-character data.
   * 
   * @return character encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Returns the MIME type of the data in this file.
   * 
   * @return MIME type
   */
  public String getMimeType() {
    return mimeType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.repository2.unified.IRepositoryFileData#getDataSize()
   */
  @Override
  public long getDataSize() {
    try {
      return stream.available();
    } catch ( IOException e ) {
      return 0;
    }
  }

  @Override
  @SuppressWarnings( "nls" )
  public String toString() {
    final int MAX_EXCERPT_LENGTH = 20;

    StringBuilder buf = new StringBuilder();
    buf.append( "SimpleRepositoryFileData[" );

    if ( stream.markSupported() ) {
      stream.mark( Integer.MAX_VALUE );
      buf.append( "stream excerpt=" );
      if ( StringUtils.isNotBlank( encoding ) ) {
        String text = null;
        try {
          text = toString( stream, encoding );
        } catch ( IOException e ) {
          throw new RuntimeException( e );
        }
        buf.append( head( text, MAX_EXCERPT_LENGTH ) );
        buf.append( "," );
        buf.append( "encoding=" );
        buf.append( encoding );
      } else {
        byte[] bytes = null;
        try {
          bytes = toByteArray( stream );
        } catch ( IOException e ) {
          throw new RuntimeException( e );
        }
        buf.append( head( bytes, MAX_EXCERPT_LENGTH ) );
      }

      try {
        stream.reset();
      } catch ( IOException e ) {
        throw new RuntimeException( e );
      }
    } else {
      buf.append( "stream=<unable to show>" );
    }
    buf.append( "," );
    buf.append( "mimeType=" );
    buf.append( mimeType );
    buf.append( "]" );
    return buf.toString();
  }

  /**
   * Returns at most {@code count} characters from {@code str}.
   */
  @SuppressWarnings( "nls" )
  private String head( final String str, final int count ) {
    if ( str.length() > count ) {
      return str.substring( 0, count ) + "...";
    } else {
      return str;
    }
  }

  /**
   * Returns {@code String} representation of array consisting of at most {@code count} bytes from {@code bytes}.
   */
  @SuppressWarnings( "nls" )
  private String head( final byte[] bytes, final int count ) {
    if ( bytes.length > count ) {
      StringBuilder buf = new StringBuilder();
      buf.append( "[" );
      for ( int i = 0; i < count; i++ ) {
        if ( i > 0 ) {
          buf.append( ", " );
        }
        buf.append( bytes[i] );
      }
      buf.append( "..." );
      buf.append( "]" );
      return buf.toString();
    } else {
      return Arrays.toString( bytes );
    }
  }

  /*
   * Copied from IOUtils.
   */
  private static String toString( final InputStream input, final String encoding ) throws IOException {
    final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    StringWriter sw = new StringWriter();
    InputStreamReader in = new InputStreamReader( input, encoding );
    char[] buffer = new char[DEFAULT_BUFFER_SIZE];
    int n = 0;
    while ( -1 != ( n = in.read( buffer ) ) ) {
      sw.write( buffer, 0, n );
    }
    return sw.toString();
  }

  /*
   * Copied from IOUtils.
   */
  private static byte[] toByteArray( final InputStream input ) throws IOException {
    final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    int n = 0;
    while ( -1 != ( n = input.read( buffer ) ) ) {
      output.write( buffer, 0, n );
    }
    return output.toByteArray();
  }

}
