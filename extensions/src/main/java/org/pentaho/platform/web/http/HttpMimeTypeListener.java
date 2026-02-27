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

import org.pentaho.platform.api.engine.IMimeTypeListener;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Deprecated
public class HttpMimeTypeListener implements IMimeTypeListener {

  private org.pentaho.platform.web.servlet.HttpMimeTypeListener mimeTypeListener;

  @Deprecated
  public HttpMimeTypeListener( final HttpServletRequest request, final HttpServletResponse response ) {
    mimeTypeListener = new org.pentaho.platform.web.servlet.HttpMimeTypeListener( request, response );
  }

  @Deprecated
  public HttpMimeTypeListener( final HttpServletRequest request, final HttpServletResponse response,
                               final String title ) {
    mimeTypeListener = new org.pentaho.platform.web.servlet.HttpMimeTypeListener( request, response, title );
  }

  @Deprecated
  public void setName( String name ) {
    mimeTypeListener.setName( name );
  }

  @Deprecated
  public void setMimeType( final String mimeType ) {
    mimeTypeListener.setMimeType( mimeType );
  }

}
