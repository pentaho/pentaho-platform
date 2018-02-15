/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
