/*!
 *
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
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.request;

import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Rowell Belen
 * <p>
 * see http://stackoverflow.com/questions/10210645/http-servlet-request-lose-params-from-post-body-after-read-it-once
 * @deprecated Please use {@link com.hitachivantara.security.web.impl.service.util.MultiReadHttpServletRequestWrapper}
 * instead. It supports multi-part requests, including the new {@link HttpServletRequest#getPart(String)} and
 * {@link HttpServletRequest#getParts()} methods.
 */
@Deprecated
public class MultiReadHttpServletRequest extends HttpServletRequestWrapper {
  private ByteArrayOutputStream cachedBytes;

  public MultiReadHttpServletRequest( HttpServletRequest request ) {
    super( request );
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    if ( cachedBytes == null ) {
      cacheInputStream();
    }

    return new CachedServletInputStream();
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader( new InputStreamReader( getInputStream() ) );
  }

  private void cacheInputStream() throws IOException {
    cachedBytes = new ByteArrayOutputStream();
    IOUtils.copy( super.getInputStream(), cachedBytes );
  }

  /* An inputstream which reads the cached request body */
  public class CachedServletInputStream extends ServletInputStream {
    private ByteArrayInputStream input;
    private ReadListener readListener;
    boolean isFinished;

    public CachedServletInputStream() {
      /* create a new input stream from the cached request body */
      input = new ByteArrayInputStream( cachedBytes.toByteArray() );
      this.readListener = null;
      this.isFinished = false;
    }

    @Override public boolean isFinished() {
      return isFinished;
    }

    @Override public boolean isReady() {
      return !isFinished();
    }

    @Override public void setReadListener( ReadListener readListener ) {
      if ( null == readListener ) {
        throw new NullPointerException();
      }
      this.readListener = readListener;
      if ( !isFinished() ) {
        try {
          readListener.onDataAvailable();
        } catch ( IOException e ) {
          readListener.onError( e );
        }
      } else {
        readListenerDone( readListener );
      }
    }

    @Override
    public int read() throws IOException {
      int val = input.read();
      if ( val == -1 ) {
        this.isFinished = true;
        readListenerDone( readListener );
      }
      return val;
    }

    private void readListenerDone( ReadListener readListener ) {
      try {
        if ( null != readListener ) {
          readListener.onAllDataRead();
        }
      } catch ( IOException e ) {
        readListener.onError( e );
      }
    }
  }
}
