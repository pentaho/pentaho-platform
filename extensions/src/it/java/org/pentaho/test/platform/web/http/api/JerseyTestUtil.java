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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.web.http.api;

import com.sun.jersey.api.client.ClientResponse;
import junit.framework.AssertionFailedError;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.zip.ZipInputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@SuppressWarnings( "nls" )
public class JerseyTestUtil {

  protected static void assertResponse( ClientResponse response, ClientResponse.Status expectedStatus ) {
    assertResponse( response, expectedStatus, null );
  }

  public static void assertResponse( ClientResponse response, ClientResponse.Status expectedStatus,
      String expectedContentType ) {
    try {
      assertEquals( expectedStatus, response.getClientResponseStatus() );
    } catch ( AssertionFailedError e ) {
      throw new AssertionFailedError( "Response status incorrect: " + e.getMessage() );
    }

    if ( expectedContentType != null ) {
      try {
        assertContentType( response.getType(), expectedContentType );
      } catch ( AssertionFailedError e ) {
        throw new AssertionFailedError( "Response media type incorrect: " + e.getMessage() );
      }
    }
  }

  private static void assertContentType( MediaType type, String expectedContentType ) {
    assertNotNull( type );
    if ( expectedContentType.indexOf( ';' ) == -1 ) {
      // no parameters, just a mime-type
      assertEquals( expectedContentType, type.toString() );
    } else {
      // strictly speaking, the regex below is not correct, as semicolon can be a part of value
      // and should be quoted; see: https://www.w3.org/Protocols/rfc1341/4_Content-Type.html
      // however, in tests, there are no such cases
      String[] parts = expectedContentType.split( ";" );
      String typeSubtype = parts[ 0 ];
      assertEquals( "Mime Type should match", typeSubtype, type.getType() + '/' + type.getSubtype() );
      assertEquals( "Amount of parameters should match", parts.length - 1, type.getParameters().size() );

      for ( int i = 1; i < parts.length; i++ ) {
        String[] pair = parts[ i ].split( "=" );
        String expectedKey = pair[ 0 ];
        String expectedValue = pair[ 1 ];
        String actualValue = type.getParameters().get( expectedKey );
        assertEquals( expectedKey, actualValue, expectedValue );
      }
    }
  }

  public static void assertResponseIsZip( ClientResponse response ) {
    ZipInputStream zis = new ZipInputStream( response.getEntityInputStream() );
    byte[] singleByte = new byte[1];
    try {
      zis.read( singleByte );
    } catch ( IOException e ) {
      throw new AssertionFailedError( "Response entity is not a zip archive: " + e.getMessage() );
    }
  }
}
