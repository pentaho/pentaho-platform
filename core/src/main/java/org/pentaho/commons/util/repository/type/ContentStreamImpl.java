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
