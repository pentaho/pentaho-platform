/*
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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Jul 11, 2005 
 * @author James Dixon
 * 
 */
package org.pentaho.platform.web.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Date;
import java.util.List;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.IContentItem;


public class HttpContentItem implements IContentItem {

  private String mimeType;

  private OutputStream outputStream;

  private HttpOutputHandler outputHandler;

  private IMimeTypeListener mimeTypeListener = null;

  public HttpContentItem(final OutputStream outputStream, final HttpOutputHandler outputHandler) {
    this.outputStream = outputStream;
    this.outputHandler = outputHandler;
  }

  public void closeOutputStream() {
    // nothing to do here
  }

  public String getId() {
    return null;
  }

  public String getPath() {
    return null;
  }

  public String getName() {
    return null;
  }

  public String getTitle() {
    return null;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(final String mimeType) {
    this.mimeType = mimeType;
    outputHandler.setMimeType(mimeType);
    if (mimeTypeListener != null) {
      mimeTypeListener.setMimeType(mimeType);
    }
  }
  
  public void setName( String name ) {
	  
  }

  public String getUrl() {
    return null;
  }

  public List getFileVersions() {
    return null;
  }

  public void removeAllVersions() {
  }

  public void removeVersion(final String fileId) {
  }

  public InputStream getInputStream() throws ContentException {
    return null;
  }

  public IPentahoStreamSource getDataSource() {
    // TODO
    return null;
  }

  public Reader getReader() throws ContentException {
    return null;
  }

  public OutputStream getOutputStream(final String actionName) {
    if (mimeType == null) {
      setMimeType("text/html"); //$NON-NLS-1$
    }
    return outputStream;
  }

  public void setOutputStream(final OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public String getActionName() {
    return null;
  }

  public String getFileId() {
    return null;
  }

  public long getFileSize() {
    return 0;
  }

  public Date getFileDateTime() {
    return null;
  }

  public void makeTransient() {
    // NOOP
  }

  public IMimeTypeListener getMimeTypeListener() {
    return mimeTypeListener;
  }

  public void setMimeTypeListener(final IMimeTypeListener mimeTypeListener) {
    this.mimeTypeListener = mimeTypeListener;
  }

}
