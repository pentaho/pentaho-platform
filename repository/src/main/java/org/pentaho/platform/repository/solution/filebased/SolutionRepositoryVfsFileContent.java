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


package org.pentaho.platform.repository.solution.filebased;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.Map;

public class SolutionRepositoryVfsFileContent implements FileContent {

  private SolutionRepositoryVfsFileObject fileObject;

  private InputStream inputStream = null;

  private boolean isOpen = false;

  public SolutionRepositoryVfsFileContent( final SolutionRepositoryVfsFileObject fileObject ) {
    super();
    this.fileObject = fileObject;
  }

  public FileObject getFile() {
    return fileObject;
  }

  public long getSize() throws FileSystemException {
    // not needed for our usage
    return 0;
  }

  public long getLastModifiedTime() throws FileSystemException {
    // not needed for our usage
    return 0;
  }

  public void setLastModifiedTime( final long arg0 ) throws FileSystemException {
    // not needed for our usage

  }

  public boolean hasAttribute( final String attrName ) {
    return false;
  }

  public void removeAttribute( final String attrName ) {
  }

  public Map getAttributes() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public String[] getAttributeNames() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public Object getAttribute( final String arg0 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public void setAttribute( final String arg0, final Object arg1 ) throws FileSystemException {
    // not needed for our usage

  }

  public Certificate[] getCertificates() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public InputStream getInputStream() throws FileSystemException {
    inputStream = fileObject.getInputStream();
    isOpen = true;
    return inputStream;
  }

  public OutputStream getOutputStream() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public RandomAccessContent getRandomAccessContent( final RandomAccessMode arg0 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public OutputStream getOutputStream( final boolean arg0 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public void close() throws FileSystemException {

    if ( !isOpen ) {
      return;
    }
    if ( inputStream != null ) {
      try {
        inputStream.close();
      } catch ( Exception e ) {
        // not much we can do here
      }
    }
    isOpen = false;
    fileObject.close();
  }

  public FileContentInfo getContentInfo() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public boolean isOpen() {
    // not needed for our usage
    return isOpen;
  }

  @Override public long write( FileContent output ) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override public long write( FileObject file ) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override public long write( OutputStream output ) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override public long write( OutputStream output, int bufferSize ) throws IOException {
    throw new UnsupportedOperationException();
  }

}
