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

/**
 * Useful when you need to override a single method in a FileContent object
 */
public class DecoratedFileContent implements FileContent {
  private FileContent fileContent;

  public DecoratedFileContent( final FileContent fileContent ) {
    this.fileContent = fileContent;
  }

  @Override public FileObject getFile() {
    return fileContent.getFile();
  }

  @Override public long getSize() throws FileSystemException {
    return fileContent.getSize();
  }

  @Override public long getLastModifiedTime() throws FileSystemException {
    return fileContent.getLastModifiedTime();
  }

  @Override public void setLastModifiedTime( final long l ) throws FileSystemException {
    fileContent.setLastModifiedTime( l );
  }

  @Override public boolean hasAttribute( final String s ) throws FileSystemException {
    return fileContent.hasAttribute( s );
  }

  @Override public Map getAttributes() throws FileSystemException {
    return fileContent.getAttributes();
  }

  @Override public String[] getAttributeNames() throws FileSystemException {
    return fileContent.getAttributeNames();
  }

  @Override public Object getAttribute( final String s ) throws FileSystemException {
    return fileContent.getAttribute( s );
  }

  @Override public void setAttribute( final String s, final Object o ) throws FileSystemException {
    fileContent.setAttribute( s, o );
  }

  @Override public void removeAttribute( final String s ) throws FileSystemException {
    fileContent.removeAttribute( s );
  }

  @Override public Certificate[] getCertificates() throws FileSystemException {
    return fileContent.getCertificates();
  }

  @Override public InputStream getInputStream() throws FileSystemException {
    return fileContent.getInputStream();
  }

  @Override public OutputStream getOutputStream() throws FileSystemException {
    return fileContent.getOutputStream();
  }

  @Override public RandomAccessContent getRandomAccessContent( final RandomAccessMode randomAccessMode )
    throws FileSystemException {
    return fileContent.getRandomAccessContent( randomAccessMode );
  }

  @Override public OutputStream getOutputStream( final boolean b ) throws FileSystemException {
    return fileContent.getOutputStream( b );
  }

  @Override public void close() throws FileSystemException {
    fileContent.close();
  }

  @Override public FileContentInfo getContentInfo() throws FileSystemException {
    return fileContent.getContentInfo();
  }

  @Override public boolean isOpen() {
    return fileContent.isOpen();
  }

  @Override public long write( FileContent fileContent ) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override public long write( FileObject fileObject ) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override public long write( OutputStream outputStream ) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override public long write( OutputStream outputStream, int i ) throws IOException {
    throw new UnsupportedOperationException();
  }
}
