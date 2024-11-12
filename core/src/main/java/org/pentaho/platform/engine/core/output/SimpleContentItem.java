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
