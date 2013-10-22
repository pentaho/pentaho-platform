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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core.output;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.util.UUIDUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SimpleContentItem implements IContentItem {

  private String mimeType;

  private OutputStream outputStream;
  private String simplePath = UUIDUtil.getUUIDAsString();

  public SimpleContentItem() {
    outputStream = null;
  }

  public SimpleContentItem( final OutputStream outputStream ) {
    this.outputStream = outputStream;
  }

  public void closeOutputStream() {
    if ( outputStream != null ) {
      try {
        outputStream.close();
      } catch ( IOException e ) {
        // Do nothing
      }
    }
  }

  public String getPath() {
    // Ensure non-null output
    return simplePath;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType( final String mimeType ) {
    this.mimeType = mimeType;
  }

  public InputStream getInputStream() throws ContentException {
    return null;
  }

  public OutputStream getOutputStream( final String actionName ) throws IOException {
    return outputStream;
  }

  public void setOutputStream( final OutputStream outputStream ) {
    this.outputStream = outputStream;
  }

  public void setName( String name ) {
    // don't need this
  }

  public IPentahoStreamSource getDataSource() {
    // TODO
    return null;
  }
}
