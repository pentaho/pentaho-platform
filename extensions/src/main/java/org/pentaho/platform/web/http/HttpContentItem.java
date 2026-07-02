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


package org.pentaho.platform.web.http;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.IContentItem;

import java.io.InputStream;
import java.io.OutputStream;

public class HttpContentItem implements IContentItem {

  private String mimeType;

  private String name;

  private OutputStream outputStream;

  private HttpOutputHandler outputHandler;

  private IMimeTypeListener mimeTypeListener = null;

  public HttpContentItem( final OutputStream outputStream, final HttpOutputHandler outputHandler ) {
    this.outputStream = outputStream;
    this.outputHandler = outputHandler;
  }

  public void closeOutputStream() {
    // nothing to do here
  }

  public String getPath() {
    return null;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType( final String mimeType ) {
    this.mimeType = mimeType;
    outputHandler.setMimeType( mimeType );
    if ( mimeTypeListener != null ) {
      mimeTypeListener.setMimeType( mimeType );
    }
  }

  public void setName( String name ) {
    this.name = name;
    outputHandler.setName( name );
  }

  public InputStream getInputStream() throws ContentException {
    return null;
  }

  public IPentahoStreamSource getDataSource() {
    // TODO
    return null;
  }

  public OutputStream getOutputStream( final String actionName ) {
    if ( mimeType == null ) {
      setMimeType( "text/html" ); //$NON-NLS-1$
    }
    return outputStream;
  }

  public void setOutputStream( final OutputStream outputStream ) {
    this.outputStream = outputStream;
  }

  public IMimeTypeListener getMimeTypeListener() {
    return mimeTypeListener;
  }

  public void setMimeTypeListener( final IMimeTypeListener mimeTypeListener ) {
    this.mimeTypeListener = mimeTypeListener;
  }

}
