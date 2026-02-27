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

public interface ContentStream {

  public int getLength();

  public void setLength( int length );

  public String getMimeType();

  public void setMimeType( String mimeType );

  public String getFilename();

  public void setFilename( String filename );

  public String getUri();

  public void setUri( String uri );

  public String getStream();

  public void setStream( String stream );

  public void base64EncodeBytes( byte[] bytes );

  public byte[] decodeBase64Stream();

}
