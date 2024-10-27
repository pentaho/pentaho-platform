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


package org.pentaho.platform.repository2.unified.webservices.jaxws;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.xml.bind.annotation.XmlMimeType;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class exists for one purpose: to be able to apply the XmlMimeType annotation.
 * 
 * @author mlowery
 */
public class SimpleRepositoryFileDataDto {

  /**
   * There is no getter/setter for dataHandler because JAX-WS will complain about duplicate fields.
   */
  @XmlMimeType( "application/octet-stream" )
  DataHandler dataHandler;

  String encoding;

  String mimeType;

  /**
   * Converts SimpleRepositoryFileData to SimpleRepositoryFileDataDto. Does not use ByteArrayDataSource since that
   * implementation reads the entire stream into a byte array.
   */
  public static SimpleRepositoryFileDataDto convert( final SimpleRepositoryFileData simpleData ) {
    FileOutputStream fout = null;
    boolean foutClosed = false;
    try {
      SimpleRepositoryFileDataDto simpleJaxWsData = new SimpleRepositoryFileDataDto();
      File tmpFile = File.createTempFile( "pentaho-ws", null ); //$NON-NLS-1$
      // TODO mlowery this might not delete files soon enough
      tmpFile.deleteOnExit();
      fout = FileUtils.openOutputStream( tmpFile );
      IOUtils.copy( simpleData.getStream(), fout );
      fout.close();
      foutClosed = true;
      simpleJaxWsData.dataHandler = new DataHandler( new FileDataSource( tmpFile ) );
      simpleJaxWsData.encoding = simpleData.getEncoding();
      simpleJaxWsData.mimeType = simpleData.getMimeType();
      return simpleJaxWsData;
    } catch ( IOException e ) {
      throw new RuntimeException( e );
    } finally {
      try {
        if ( fout != null && !foutClosed ) {
          fout.close();
        }
      } catch ( Exception e ) {
        // CHECKSTYLES IGNORE
      }
    }
  }

  /**
   * Converts SimpleRepositoryFileDataDto to SimpleRepositoryFileData.
   */
  public static SimpleRepositoryFileData convert( final SimpleRepositoryFileDataDto simpleJaxWsData ) {
    FileOutputStream fout = null;
    InputStream in = null;
    DataHandler dh = null;
    boolean foutClosed = false;
    try {
      File tmpFile = File.createTempFile( "pentaho", null ); //$NON-NLS-1$
      // TODO mlowery this might not delete files soon enough
      tmpFile.deleteOnExit();
      fout = FileUtils.openOutputStream( tmpFile );
      // used to cast to com.sun.xml.ws.developer.StreamingDataHandler here but that stopped working
      dh = simpleJaxWsData.dataHandler;
      // used to call dh.readOnce() (instead of dh.getInputStream()) here
      in = dh.getInputStream();
      IOUtils.copy( in, fout );
      fout.close();
      foutClosed = true;
      InputStream fin = new BufferedInputStream( FileUtils.openInputStream( tmpFile ) );
      return new SimpleRepositoryFileData( fin, simpleJaxWsData.encoding, simpleJaxWsData.mimeType );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    } finally {
      try {
        // close the streams
        if ( in != null ) {
          in.close();
        }
        // used to have to call dh.close() on the com.sun.xml.ws.developer.StreamingDataHandler here
        if ( fout != null && !foutClosed ) {
          fout.close();
        }
      } catch ( Exception e ) {
        // CHECKSTYLES IGNORE
      }
    }
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "SimpleRepositoryFileDataDto [dataHandler=" + dataHandler + ", encoding=" + encoding + ", mimeType="
        + mimeType + "]";
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType( String mimeType ) {
    this.mimeType = mimeType;
  }

}
