/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jul 11, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.engine.core.output;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Date;
import java.util.List;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.util.UUIDUtil;

public class SimpleContentItem implements IContentItem {

  private String mimeType;

  private OutputStream outputStream;
  private String simplePath = UUIDUtil.getUUIDAsString();

  public SimpleContentItem() {
    outputStream = null;
  }

  public SimpleContentItem(final OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public void closeOutputStream() {
    if (outputStream != null) {
      try {
        outputStream.close();
      } catch (IOException e) {
        //Do nothing
      }
    }
  }

  public String getId() {
    return null;
  }

  public String getPath() {
    // Ensure non-null output
    return simplePath;
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

  public Reader getReader() throws ContentException {
    return null;
  }

  public OutputStream getOutputStream(final String actionName) throws IOException {
    return outputStream;
  }

  public void setOutputStream(final OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public IPentahoStreamSource getDataSource() {
    // TODO
    return null;
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
  
  public void setName( String name ) {
	  // don't need this
  }
}
