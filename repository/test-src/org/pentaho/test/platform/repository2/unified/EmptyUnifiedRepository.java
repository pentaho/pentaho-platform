/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.repository2.unified;

import java.io.Serializable;
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

/**
 * Creates a base implementation of IUnifiedRepository (all methods do default things like {@code return null}) so
 * that unit tests can use simple mock implementations that extend this class and only override what is needed vs.
 * implementing all the methods just to use 2 of them
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class EmptyUnifiedRepository implements IUnifiedRepository {
  @Override
  public RepositoryFile getFile( final String path ) {
    return null;
  }

  @Override
  public RepositoryFileTree getTree( final String path, final int depth, final String filter,
                                     final boolean showHidden ) {
    return null;
  }

  @Override
  public RepositoryFile getFileAtVersion( final Serializable fileId, final Serializable versionId ) {
    return null;
  }

  @Override
  public RepositoryFile getFileById( final Serializable fileId ) {
    return null;
  }

  @Override
  public RepositoryFile getFile( final String path, final boolean loadLocaleMaps ) {
    return null;
  }

  @Override
  public RepositoryFile getFileById( final Serializable fileId, final boolean loadLocaleMaps ) {
    return null;
  }

  @Override
  public <T extends IRepositoryFileData> T getDataForRead( final Serializable fileId, final Class<T> dataClass ) {
    return null;
  }

  @Override
  public <T extends IRepositoryFileData> T getDataAtVersionForRead( final Serializable fileId,
      final Serializable versionId, final Class<T> dataClass ) {
    return null;
  }

  @Override
  public <T extends IRepositoryFileData> T getDataForExecute( final Serializable fileId, final Class<T> dataClass ) {
    return null;
  }

  @Override
  public <T extends IRepositoryFileData> T getDataAtVersionForExecute( final Serializable fileId,
      final Serializable versionId, final Class<T> dataClass ) {
    return null;
  }

  @Override
  public <T extends IRepositoryFileData> List<T> getDataForReadInBatch( final List<RepositoryFile> files,
      final Class<T> dataClass ) {
    return null;
  }

  @Override
  public <T extends IRepositoryFileData> List<T> getDataForExecuteInBatch( final List<RepositoryFile> files,
      final Class<T> dataClass ) {
    return null;
  }

  @Override
  public RepositoryFile createFile( final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final String versionMessage ) {
    return null;
  }

  @Override
  public RepositoryFile createFile( final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final RepositoryFileAcl acl, final String versionMessage ) {
    return null;
  }

  @Override
  public RepositoryFile createFolder( final Serializable parentFolderId, final RepositoryFile file,
      final String versionMessage ) {
    return null;
  }

  @Override
  public RepositoryFile createFolder( final Serializable parentFolderId, final RepositoryFile file,
      final RepositoryFileAcl acl, final String versionMessage ) {
    return null;
  }

  @Override
  public List<RepositoryFile> getChildren( final Serializable folderId ) {
    return null;
  }

  @Override
  public List<RepositoryFile> getChildren( final Serializable folderId, final String filter ) {
    return null;
  }
  
  @Override
  public List<RepositoryFile> getChildren( final Serializable folderId, final String filter, final Boolean showHiddenFiles ) {
    return null;
  }

  @Override
  public RepositoryFile updateFile( final RepositoryFile file, final IRepositoryFileData data,
      final String versionMessage ) {
    return null;
  }

  @Override
  public void deleteFile( final Serializable fileId, final boolean permanent, final String versionMessage ) {
  }

  @Override
  public void deleteFile( final Serializable fileId, final String versionMessage ) {
  }

  @Override
  public void moveFile( final Serializable fileId, final String destAbsPath, final String versionMessage ) {
  }

  @Override
  public void copyFile( final Serializable fileId, final String destAbsPath, final String versionMessage ) {
  }

  @Override
  public void undeleteFile( final Serializable fileId, final String versionMessage ) {
  }

  @Override
  public List<RepositoryFile> getDeletedFiles( final String origParentFolderPath ) {
    return null;
  }

  @Override
  public List<RepositoryFile> getDeletedFiles( final String origParentFolderPath, final String filter ) {
    return null;
  }

  @Override
  public List<RepositoryFile> getDeletedFiles() {
    return null;
  }

  @Override
  public boolean canUnlockFile( final Serializable fileId ) {
    return false;
  }

  @Override
  public void lockFile( final Serializable fileId, final String message ) {
  }

  @Override
  public void unlockFile( final Serializable fileId ) {
  }

  @Override
  public RepositoryFileAcl getAcl( final Serializable fileId ) {
    return null;
  }

  @Override
  public RepositoryFileAcl updateAcl( final RepositoryFileAcl acl ) {
    return null;
  }

  @Override
  public boolean hasAccess( final String path, final EnumSet<RepositoryFilePermission> permissions ) {
    return false;
  }

  @Override
  public List<RepositoryFileAce> getEffectiveAces( final Serializable fileId ) {
    return null;
  }

  @Override
  public List<RepositoryFileAce> getEffectiveAces( final Serializable fileId, final boolean forceEntriesInheriting ) {
    return null;
  }

  @Override
  public VersionSummary getVersionSummary( final Serializable fileId, final Serializable versionId ) {
    return null;
  }

  @Override
  public List<VersionSummary> getVersionSummaryInBatch( final List<RepositoryFile> files ) {
    return null;
  }

  @Override
  public List<VersionSummary> getVersionSummaries( final Serializable fileId ) {
    return null;
  }

  @Override
  public void deleteFileAtVersion( final Serializable fileId, final Serializable versionId ) {
  }

  @Override
  public void
  restoreFileAtVersion( final Serializable fileId, final Serializable versionId, final String versionMessage ) {
  }

  @Override
  public List<RepositoryFile> getReferrers( final Serializable fileId ) {
    return null;
  }

  @Override
  public void setFileMetadata( final Serializable fileId, final Map<String, Serializable> metadataMap ) {
  }

  @Override
  public Map<String, Serializable> getFileMetadata( final Serializable fileId ) {
    return null;
  }

  @Override
  public List<Character> getReservedChars() {
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositoryFile getFile( String path, IPentahoLocale locale ) {
    return null;
  }

  @Override
  public RepositoryFile getFileById( Serializable fileId, IPentahoLocale locale ) {
    return null;
  }

  @Override
  public RepositoryFile getFile( String path, boolean loadLocaleMaps, IPentahoLocale locale ) {
    return null;
  }

  @Override
  public RepositoryFile getFileById( Serializable fileId, boolean loadLocaleMaps, IPentahoLocale locale ) {
    return null;
  }

  @Override
  public List<Locale> getAvailableLocalesForFileById( Serializable fileId ) {
    return null;
  }

  @Override
  public List<Locale> getAvailableLocalesForFileByPath( String relPath ) {
    return null;
  }

  @Override
  public List<Locale> getAvailableLocalesForFile( RepositoryFile repositoryFile ) {
    return null;
  }

  @Override
  public Properties getLocalePropertiesForFileById( Serializable fileId, String locale ) {
    return null;
  }

  @Override
  public Properties getLocalePropertiesForFileByPath( String relPath, String locale ) {
    return null;
  }

  @Override
  public Properties getLocalePropertiesForFile( RepositoryFile repositoryFile, String locale ) {
    return null;
  }

  @Override
  public void setLocalePropertiesForFileById( Serializable fileId, String locale, Properties properties ) {
  }

  @Override
  public void setLocalePropertiesForFileByPath( String relPath, String locale, Properties properties ) {
  }

  @Override
  public void setLocalePropertiesForFile( RepositoryFile repositoryFile, String locale, Properties properties ) {
  }

  @Override
  public void deleteLocalePropertiesForFile( RepositoryFile repositoryFile, String locale ) {
  }

  @Override
  public RepositoryFile updateFolder( RepositoryFile folder, String versionMessage ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFileTree getTree( RepositoryRequest repositoryRequest ) {
    return null;
  }

  @Override
  public List<RepositoryFile> getChildren( RepositoryRequest repositoryRequest ) {
    return null;
  }
}
