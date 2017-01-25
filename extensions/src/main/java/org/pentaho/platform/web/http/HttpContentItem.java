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

package org.pentaho.platform.web.http;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.IContentItem;

import java.io.InputStream;
import java.io.OutputStream;

public class HttpContentItem implements IContentItem {

  private String mimeType;

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
