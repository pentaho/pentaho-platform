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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.operations.FileOperations;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrAclNodeHelper;

public class SolutionRepositoryVfsFileObject implements FileObject {

  private static IAclNodeHelper testAclHelper;

  private String fileRef;

  private static final IUnifiedRepository repository = PentahoSystem.get( IUnifiedRepository.class, null );

  private FileContent content = null;

  private RepositoryFile repositoryFile = null;

  private IRepositoryContentConverterHandler converterHandler;

  private IAclNodeHelper aclHelper;

  public SolutionRepositoryVfsFileObject( final String fileRef ) {
    super();
    this.fileRef = fileRef;
  }

  public IUnifiedRepository getRepository() {
    return repository;
  }

  public IRepositoryContentConverterHandler getConverterHandler() {
    if ( converterHandler == null ) {
      converterHandler = PentahoSystem.get( IRepositoryContentConverterHandler.class );
    }
    return converterHandler;
  }

  public String getFileRef() {
    return fileRef;
  }

  public FileName getName() {
    initFile();
    FileType fileType = null;
    try {
      fileType = getType();
    } catch ( Exception ex ) {
      fileType = FileType.FOLDER;
    }
    return new SolutionRepositoryFileName( fileRef, fileType );
  }

  /**
   * TODO: This impl always returns null as the URL constructor throws an exception on not finding a handler for 'solution' protocol
   * We should either fix this or remove this method as it is not called by any code
   * @return
   * @throws FileSystemException
   */
  public URL getURL() throws FileSystemException {
    URL url = null;
    try {
      url = new URL( "solution:" + fileRef ); //$NON-NLS-1$
    } catch ( Exception e ) {
      // CHECKSTYLES IGNORE
    }
    return url;
  }

  private void initFile() {
    // decode URL before 'get'
    String fileUrl = fileRef;

    try {
      final Charset urlCharset = Charset.forName( "UTF-8" );
      fileUrl = URLDecoder.decode( fileUrl, urlCharset.name() );
    } catch ( UnsupportedEncodingException e ) {
      fileUrl = fileRef;
    }

    String dsPath = fileUrl;
    if ( fileUrl.matches( "^(/etc/mondrian/)(.*)(/schema.xml)" ) ) {
      dsPath = fileUrl.substring( 0, fileUrl.indexOf( "/schema.xml" ) );
    }

    repositoryFile = getRepository().getFile( fileUrl );
    if ( !getAclHelper().canAccess( getRepository().getFile( dsPath ),  EnumSet.of( RepositoryFilePermission.READ ) ) ) {
      repositoryFile = null;
    }
  }

  public boolean exists() throws FileSystemException {
    initFile();
    return repositoryFile != null;
  }

  public boolean isHidden() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public boolean isReadable() throws FileSystemException {
    // not needed for our usage
    return exists();
  }

  public boolean isWriteable() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public FileType getType() throws FileSystemException {
    return ( ( repositoryFile != null ) && !repositoryFile.isFolder() ) ? FileType.FILE : FileType.FOLDER;
  }

  public FileObject getParent() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  @Override public String getPublicURIString() {
    throw new UnsupportedOperationException();
  }

  public FileSystem getFileSystem() {
    // not needed for our usage
    return null;
  }

  public FileObject[] getChildren() throws FileSystemException {

    List<FileObject> fileList = new ArrayList<FileObject>();
    if ( exists() ) {
      for ( RepositoryFile child : getRepository().getChildren( repositoryFile.getId() ) ) {
        SolutionRepositoryVfsFileObject fileInfo = new SolutionRepositoryVfsFileObject( child.getPath() );
        fileList.add( fileInfo );
      }
    }
    return fileList.toArray( new FileObject[0] );
  }

  public FileObject getChild( final String arg0 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileObject resolveFile( final String arg0, final NameScope arg1 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  @Override public boolean setExecutable( boolean executable, boolean ownerOnly ) throws FileSystemException {
    throw new UnsupportedOperationException();
  }

  @Override public boolean setReadable( boolean readable, boolean ownerOnly ) throws FileSystemException {
    throw new UnsupportedOperationException();
  }

  @Override public boolean setWritable( boolean writable, boolean ownerOnly ) throws FileSystemException {
    throw new UnsupportedOperationException();
  }

  public FileObject resolveFile( final String arg0 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public FileObject[] findFiles( final FileSelector arg0 ) throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public void findFiles( final FileSelector arg0, final boolean arg1, final List arg2 ) throws FileSystemException {
    // not needed for our usage
  }

  public boolean delete() throws FileSystemException {
    // not needed for our usage
    return false;
  }

  public int delete( final FileSelector arg0 ) throws FileSystemException {
    // not needed for our usage
    return 0;
  }

  @Override public int deleteAll() throws FileSystemException {
    throw new UnsupportedOperationException();
  }

  public void createFolder() throws FileSystemException {
    // not needed for our usage

  }

  public void createFile() throws FileSystemException {
    // not needed for our usage

  }

  public void copyFrom( final FileObject arg0, final FileSelector arg1 ) throws FileSystemException {
    // not needed for our usage

  }

  public void moveTo( final FileObject arg0 ) throws FileSystemException {
    // not needed for our usage
  }

  public boolean canRenameTo( final FileObject arg0 ) {
    // not needed for our usage
    return false;
  }

  public FileContent getContent() throws FileSystemException {
    content = new SolutionRepositoryVfsFileContent( this );
    return content;
  }

  public void close() throws FileSystemException {
    if ( content != null ) {
      content.close();
      content = null;
    }
  }

  public void refresh() throws FileSystemException {
    // not needed for our usage
  }

  public boolean isAttached() {
    // not needed for our usage
    return false;
  }

  public boolean isContentOpen() {
    return ( content != null ) && content.isOpen();
  }

  @Override public boolean isExecutable() throws FileSystemException {
    throw new UnsupportedOperationException();
  }

  @Override public boolean isFile() throws FileSystemException {
    return getType().equals( FileType.FILE );
  }

  @Override public boolean isFolder() throws FileSystemException {
    throw new UnsupportedOperationException();
  }

  public FileOperations getFileOperations() throws FileSystemException {
    // not needed for our usage
    return null;
  }

  public InputStream getInputStream() throws UnifiedRepositoryException, FileSystemException {
    InputStream inputStream = null;
    if ( exists() ) {
      String extension = FilenameUtils.getExtension( repositoryFile.getPath() );
      // Try to get the converter for the extension. If there is not converter available then we will
      //assume simple type and will get the data that way
      if ( getConverterHandler() != null ) {
        Converter converter = getConverterHandler().getConverter( extension );
        if ( converter != null ) {
          inputStream = converter.convert( repositoryFile.getId() );
        }
      }
      if ( inputStream == null ) {
        inputStream = getRepository().getDataForRead( repositoryFile.getId(), SimpleRepositoryFileData.class ).getStream();
      }
    }
    return inputStream;
  }

  protected synchronized IAclNodeHelper getAclHelper() {
    if ( testAclHelper != null ) {
      return testAclHelper;
    }
    if ( aclHelper == null ) {
      aclHelper = new JcrAclNodeHelper( getRepository() );
    }
    return aclHelper;
  }

  /**
   * We do not control the lifecycle (creation) of these class instances. Therefore, for testing purposes when we need
   * to override the IAclNodeHelper, this is the method to call.
   *
   * @param helper
   */
  @VisibleForTesting
  public static void setTestAclHelper( IAclNodeHelper helper ) {
    testAclHelper = helper;
  }

  @Override public int compareTo( FileObject file ) {
    return file == null ? 1 : this.getName().getURI().compareTo( file.getName().getURI() );
  }

  @Override public Iterator<FileObject> iterator() {
    throw new UnsupportedOperationException();
  }
}
