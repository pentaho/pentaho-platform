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

package org.pentaho.commons.util.repository.type;

import org.apache.commons.codec.binary.Base64;

public class ContentStreamImpl implements ContentStream {

  private String mimeType;

  private String filename;

  private String uri;

  private String stream;

  public int getLength() {
    if ( stream == null ) {
      return 0;
    }
    return stream.length();
  }

  public void setLength( int length ) {
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType( String mimeType ) {
    this.mimeType = mimeType;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public String getUri() {
    return uri;
  }

  public void setUri( String uri ) {
    this.uri = uri;
  }

  public String getStream() {
    return stream;
  }

  public void setStream( String stream ) {
    this.stream = stream;
  }

  public void base64EncodeBytes( byte[] bytes ) {
    stream = new String( Base64.encodeBase64( bytes ) );
  }

  public byte[] decodeBase64Stream() {
    if ( stream == null ) {
      return null;
    }
    return Base64.decodeBase64( stream.getBytes() );
  }

}
