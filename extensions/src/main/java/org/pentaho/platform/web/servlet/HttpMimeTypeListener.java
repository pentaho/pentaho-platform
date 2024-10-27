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


package org.pentaho.platform.web.servlet;

import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.MimeHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

public class HttpMimeTypeListener implements IMimeTypeListener {

  private HttpServletResponse response;

  private HttpServletRequest request;

  private String title;

  public HttpMimeTypeListener( final HttpServletRequest request, final HttpServletResponse response ) {
    this( request, response, null );
  }

  public HttpMimeTypeListener( final HttpServletRequest request, final HttpServletResponse response,
                               final String title ) {
    this.request = request;
    this.response = response;
    this.title = title;
  }

  public void setName( String name ) {
    title = name;
  }

  public void setMimeType( final String mimeType ) {
    response.setContentType( mimeType );
    response.setCharacterEncoding( LocaleHelper.getSystemEncoding() );
    //
    // Since this is an HTTP thing, allow users to override default behavior by allowing them
    // the mime object as an attachment. Forcing everything non-html to be an attachment
    // means that users can't have their excel reports appear in their IE browser, it makes
    // SVG not work unless you have an external SVG viewer, it makes PDF files no longer
    // be viewable in the browser. Please see PLATFORM-1018.
    // also see: http://support.microsoft.com/kb/260519 for info on content-disposition header
    //
    String forceAttachment = request.getParameter( "forceAttachment" ); //$NON-NLS-1$
    String fileName = title;
    if ( fileName == null ) {
      fileName = "default"; //$NON-NLS-1$
    }
    fileName += MimeHelper.getExtension( mimeType );
    if ( ( forceAttachment != null ) && ( "true".equalsIgnoreCase( forceAttachment ) ) ) { //$NON-NLS-1$
      response.setHeader( "content-disposition", buildContentDispositionValue( fileName, true ) ); //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      response.setHeader( "content-disposition", buildContentDispositionValue( fileName, false ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * Uses RFC2231/5987 encoded UTF-8 to support internationalized file names on modern browsers.
   * 
   * @param fileName
   * @return
   */
  public static String buildContentDispositionValue( String fileName, boolean isAttachment ) {
    try {
      fileName = URLEncoder.encode( fileName, "UTF-8" );
    } catch ( UnsupportedEncodingException e ) {
      // Should never happen
    }
    fileName = fileName.replaceAll( "\\+", "%20" );
    if ( isAttachment ) {
      return "attachment; filename*=UTF-8\'\'" + fileName;
    } else {
      return "inline; filename*=UTF-8\'\'" + fileName;
    }
  }

}
