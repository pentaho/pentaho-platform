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


package org.pentaho.platform.engine.core.output;

import org.pentaho.platform.api.engine.IContentListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class BufferedContentItem extends SimpleContentItem {

  private ByteArrayOutputStream outputStream;

  private InputStream inputStream;

  private IContentListener listener;

  protected String name;

  public BufferedContentItem( final IContentListener listener ) {
    super();
    this.listener = listener;
    outputStream = new ByteArrayOutputStream();
    inputStream = null;
    setOutputStream( outputStream );
  }

  @Override
  public void closeOutputStream() {
    inputStream = new ByteArrayInputStream( outputStream.toByteArray() );
    if ( listener != null ) {
      listener.close();
    }
  }

  @Override
  public InputStream getInputStream() {
    return inputStream;
  }

  @Override
  public void setMimeType( final String mimeType ) {
    super.setMimeType( mimeType );
    if ( listener != null ) {
      listener.setMimeType( mimeType );
    }
  }

  @Override
  public void setName( String name ) {
    this.name = name;
  }

}
