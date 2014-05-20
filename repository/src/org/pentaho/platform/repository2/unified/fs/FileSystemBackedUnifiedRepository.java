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

package org.pentaho.platform.repository2.unified.fs;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.springframework.util.Assert;

public class FileSystemBackedUnifiedRepository implements IUnifiedRepository {
  private FileSystemRepositoryFileDao repositoryFileDao;

  public FileSystemBackedUnifiedRepository( final String baseDir ) {
    this( new FileSystemRepositoryFileDao( baseDir ) );
  }

  public FileSystemBackedUnifiedRepository( final File baseDir ) {
    this( new FileSystemRepositoryFileDao( baseDir ) );
  }

  public FileSystemBackedUnifiedRepository() {
    this( new FileSystemRepositoryFileDao() );
  }

  public FileSystemBackedUnifiedRepository( final FileSystemRepositoryFileDao repositoryFileDao ) {
    this.repositoryFileDao = repositoryFileDao;
  }

  public boolean canUnlockFile( Serializable fileId ) {
    return repositoryFileDao.canUnlockFile( fileId );
  }

  public RepositoryFile createFile( Serializable parentFolderId, RepositoryFile file, IRepositoryFileData data,
      String versionMessage ) {
    return repositoryFileDao.createFile( parentFolderId, file, data, null, versionMessage );
  }

  public RepositoryFile createFile( Serializable parentFolderId, RepositoryFile file, IRepositoryFileData data,
      RepositoryFileAcl acl, String versionMessage ) {
    return repositoryFileDao.createFile( parentFolderId, file, data, acl, versionMessage );
  }

  public RepositoryFile createFolder( Serializable parentFolderId, RepositoryFile file, String versionMessage ) {
    return repositoryFileDao.createFolder( parentFolderId, file, null, versionMessage );
  }

  public RepositoryFile createFolder( Serializable parentFolderId, RepositoryFile file, RepositoryFileAcl acl,
      String versionMessage ) {
    return repositoryFileDao.createFolder( parentFolderId, file, acl, versionMessage );
  }

  public void deleteFile( Serializable fileId, boolean permanent, String versionMessage ) {
    repositoryFileDao.deleteFile( fileId, versionMessage );

  }

  public void deleteFile( Serializable fileId, String versionMessage ) {
    repositoryFileDao.deleteFile( fileId, versionMessage );

  }

  public void deleteFileAtVersion( Serializable fileId, Serializable versionId ) {
    repositoryFileDao.deleteFileAtVersion( fileId, versionId );
  }

  public RepositoryFileAcl getAcl( Serializable fileId ) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public List<RepositoryFile> getChildren( RepositoryRequest repositoryRequest ) {
    return repositoryFileDao.getChildren( repositoryRequest );
  }

  @Deprecated
  public List<RepositoryFile> getChildren( Serializable folderId ) {
    return repositoryFileDao.getChildren( folderId, "", false );
  }

  @Deprecated
  public List<RepositoryFile> getChildren( Serializable folderId, String filter) {
    return repositoryFileDao.getChildren( folderId, filter, false);
  }
  
  @Deprecated
  public List<RepositoryFile> getChildren( Serializable folderId, String filter, Boolean showHiddenFiles ) {
    return repositoryFileDao.getChildren( new RepositoryRequest( folderId.toString(), showHiddenFiles, -1, filter ) );
  }

  public <T extends IRepositoryFileData> T getDataAtVersionForExecute( Serializable fileId, Serializable versionId,
      Class<T> dataClass ) {
    return repositoryFileDao.getData( fileId, versionId, dataClass );
  }

  public <T extends IRepositoryFileData> T getDataAtVersionForRead( Serializable fileId, Serializable versionId,
      Class<T> dataClass ) {
    return repositoryFileDao.getData( fileId, versionId, dataClass );
  }

  public <T extends IRepositoryFileData> T getDataForExecute( Serializable fileId, Class<T> dataClass ) {
    return repositoryFileDao.getData( fileId, null, dataClass );
  }

  public <T extends IRepositoryFileData> T getDataForRead( Serializable fileId, Class<T> dataClass ) {
    return repositoryFileDao.getData( fileId, null, dataClass );
  }

  public List<RepositoryFile> getDeletedFiles( Serializable folderId ) {
    return repositoryFileDao.getDeletedFiles( folderId, null );
  }

  public List<RepositoryFile> getDeletedFiles( Serializable folderId, String filter ) {
    return repositoryFileDao.getDeletedFiles( folderId, filter );
  }

  public List<RepositoryFile> getDeletedFiles() {
    return repositoryFileDao.getDeletedFiles();
  }

  public List<RepositoryFileAce> getEffectiveAces( Serializable fileId ) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<RepositoryFileAce> getEffectiveAces( Serializable fileId, boolean forceEntriesInheriting ) {
    // TODO Auto-generated method stub
    return null;
  }

  public RepositoryFile getFile( String path ) {
    return repositoryFileDao.getFile( path );
  }

  public RepositoryFile getFile( String path, boolean loadLocaleMaps ) {
    return repositoryFileDao.getFile( path, loadLocaleMaps );
  }

  public RepositoryFile getFileAtVersion( Serializable fileId, Serializable versionId ) {
    return repositoryFileDao.getFile( fileId, versionId );
  }

  public RepositoryFile getFileById( Serializable fileId ) {
    return repositoryFileDao.getFile( fileId, null );
  }

  public RepositoryFile getFileById( Serializable fileId, boolean loadLocaleMaps ) {
    return repositoryFileDao.getFileById( fileId, loadLocaleMaps );
  }

  @Override
  public RepositoryFile getFile( String path, IPentahoLocale locale ) {
    return this.repositoryFileDao.getFile( path, locale );
  }

  @Override
  public RepositoryFile getFileById( Serializable fileId, IPentahoLocale locale ) {
    return this.repositoryFileDao.getFileById( fileId, locale );
  }

  @Override
  public RepositoryFile getFile( String path, boolean loadLocaleMaps, IPentahoLocale locale ) {
    return this.repositoryFileDao.getFile( path, loadLocaleMaps, locale );
  }

  @Override
  public RepositoryFile getFileById( Serializable fileId, boolean loadLocaleMaps, IPentahoLocale locale ) {
    return this.repositoryFileDao.getFileById( fileId, loadLocaleMaps, locale );
  }
  
  @Override
  public RepositoryFileTree getTree( RepositoryRequest repositoryRequest ) {
    return repositoryFileDao.getTree( repositoryRequest);
  }

  public RepositoryFileTree getTree( String path, int depth, String filter, boolean showHidden ) {
    return repositoryFileDao.getTree( new RepositoryRequest( path, showHidden, depth, filter ) );
  }

  public List<VersionSummary> getVersionSummaries( Serializable fileId ) {
    return repositoryFileDao.getVersionSummaries( fileId );
  }

  public VersionSummary getVersionSummary( Serializable fileId, Serializable versionId ) {
    return repositoryFileDao.getVersionSummary( fileId, versionId );
  }

  public void setRootDir( File rootDir ) {
    repositoryFileDao.setRootDir( rootDir );
  }

  public boolean hasAccess( String path, EnumSet<RepositoryFilePermission> permissions ) {
    throw new UnsupportedOperationException();
  }

  public void lockFile( Serializable fileId, String message ) {
    repositoryFileDao.lockFile( fileId, message );
  }

  public void moveFile( Serializable fileId, String destAbsPath, String versionMessage ) {
    repositoryFileDao.moveFile( fileId, destAbsPath, versionMessage );
  }

  public void restoreFileAtVersion( Serializable fileId, Serializable versionId, String versionMessage ) {
    repositoryFileDao.restoreFileAtVersion( fileId, versionId, versionMessage );
  }

  public void undeleteFile( Serializable fileId, String versionMessage ) {
    repositoryFileDao.undeleteFile( fileId, versionMessage );
  }

  public void unlockFile( Serializable fileId ) {
    repositoryFileDao.unlockFile( fileId );
  }

  public RepositoryFileAcl updateAcl( RepositoryFileAcl acl ) {
    throw new UnsupportedOperationException();
  }

  public RepositoryFile updateFile( RepositoryFile file, IRepositoryFileData data, String versionMessage ) {
    return repositoryFileDao.updateFile( file, data, versionMessage );
  }

  public List<RepositoryFile> getReferrers( Serializable arg0 ) {
    throw new UnsupportedOperationException();
  }

  public <T extends IRepositoryFileData> List<T> getDataForExecuteInBatch( List<RepositoryFile> files,
      Class<T> dataClass ) {
    throw new UnsupportedOperationException();
  }

  public <T extends IRepositoryFileData> List<T> getDataForReadInBatch( List<RepositoryFile> files,
                                                                        Class<T> dataClass ) {
    Assert.notNull( files );
    List<T> data = new ArrayList<T>( files.size() );
    for ( RepositoryFile f : files ) {
      Assert.notNull( f );
      data.add( repositoryFileDao.getData( f.getId(), f.getVersionId(), dataClass ) );
    }
    return data;
  }

  public List<VersionSummary> getVersionSummaryInBatch( List<RepositoryFile> files ) {
    
    Assert.notNull( files );
    List<VersionSummary> versionSummaryList = new ArrayList<VersionSummary>( files.size() );
    
    for(RepositoryFile file : files){
      versionSummaryList.add( getVersionSummary( file.getId(), file.getVersionId() ) );
    }
    
    return versionSummaryList;
  }

  public void setFileMetadata( final Serializable fileId, Map<String, Serializable> metadataMap ) {
    repositoryFileDao.setFileMetadata( fileId, metadataMap );
  }

  public Map<String, Serializable> getFileMetadata( final Serializable fileId ) {
    return repositoryFileDao.getFileMetadata( fileId );
  }

  public void copyFile( Serializable fileId, String destAbsPath, String versionMessage ) {
    throw new UnsupportedOperationException();
  }

  public List<RepositoryFile> getDeletedFiles( String origParentFolderPath, String filter ) {
    throw new UnsupportedOperationException();
  }

  public List<RepositoryFile> getDeletedFiles( String origParentFolderPath ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Character> getReservedChars() {
    return repositoryFileDao.getReservedChars();
  }

  @Override
  public List<Locale> getAvailableLocalesForFileById( Serializable fileId ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Locale> getAvailableLocalesForFileByPath( String relPath ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Locale> getAvailableLocalesForFile( RepositoryFile repositoryFile ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Properties getLocalePropertiesForFileById( Serializable fileId, String locale ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Properties getLocalePropertiesForFileByPath( String relPath, String locale ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Properties getLocalePropertiesForFile( RepositoryFile repositoryFile, String locale ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLocalePropertiesForFileById( Serializable fileId, String locale, Properties properties ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLocalePropertiesForFileByPath( String relPath, String locale, Properties properties ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLocalePropertiesForFile( RepositoryFile repositoryFile, String locale, Properties properties ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteLocalePropertiesForFile( RepositoryFile repositoryFile, String locale ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositoryFile updateFolder( RepositoryFile folder, String versionMessage ) {
    throw new UnsupportedOperationException();
  }

}
