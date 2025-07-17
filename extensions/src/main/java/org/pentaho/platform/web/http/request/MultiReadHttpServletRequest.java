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


package org.pentaho.platform.web.http.request;

import org.apache.commons.io.IOUtils;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
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
