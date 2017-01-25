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

package org.pentaho.platform.engine.services.actionsequence;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IActionSequenceResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class ActionSequenceResourceWrapper implements IPentahoStreamSource {
  private InputStream inputStream;

  private IActionSequenceResource resource;

  public ActionSequenceResourceWrapper( IActionSequenceResource actionResource, InputStream iStream ) {
    super();
    assert ( actionResource != null );
    assert ( iStream != null );
    this.resource = actionResource;
    this.inputStream = iStream;
  }

  public String getContentType() {
    return resource.getMimeType();
  }

  public InputStream getInputStream() throws IOException {
    if ( inputStream != null ) {
      if ( inputStream.markSupported() ) {
        try {
          inputStream.reset();
        } catch ( IOException e ) {
          // do nothing: a mark has not been set on this stream. This is not an error.
          return inputStream;
        }
      }
    }
    return inputStream;
  }

  public String getName() {
    return resource.getName();
  }

  public OutputStream getOutputStream() throws IOException {
    return new ByteArrayOutputStream();
  }
}
