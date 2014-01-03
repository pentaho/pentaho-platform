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

package org.pentaho.platform.repository2.unified;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.VersionSummary;

/**
 * A data access object for reading and writing {@code RepositoryFile} instances. The methods in this interface
 * might closely resemble those in {@link IUnifiedRepository} but this interface is not part of the public Pentaho
 * API and can evolve independently.
 * 
 * @author mlowery
 */
public interface IRepositoryFileDao {

  RepositoryFile getFileByAbsolutePath( final String absPath );

  RepositoryFile getFile( final String relPath );

  @Deprecated
  RepositoryFileTree getTree( final String relPath, final int depth, final String filter, final boolean showHidden );
  
  RepositoryFileTree getTree( RepositoryRequest repositoryRequest );

  RepositoryFile getFile( final String relPath, final boolean loadLocaleMaps );

  RepositoryFile getFileById( final Serializable fileId );

  RepositoryFile getFileById( final Serializable fileId, final boolean loadLocaleMaps );

  RepositoryFile getFile( final String relPath, final IPentahoLocale locale );

  RepositoryFile getFileById( final Serializable fileId, final IPentahoLocale locale );

  RepositoryFile getFile( final String relPath, final boolean loadLocaleMaps, final IPentahoLocale locale );

  RepositoryFile getFileById( final Serializable fileId, final boolean loadLocaleMaps, final IPentahoLocale locale );

  <T extends IRepositoryFileData> T getData( final Serializable fileId, final Serializable versionId,
      final Class<T> dataClass );

  RepositoryFile createFile( final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final RepositoryFileAcl acl, final String versionMessage );

  RepositoryFile createFolder( final Serializable parentFolderId, final RepositoryFile file,
      final RepositoryFileAcl acl, final String versionMessage );

  RepositoryFile updateFolder( final RepositoryFile file, final String versionMessage );

  @Deprecated
  List<RepositoryFile> getChildren( final Serializable folderId, final String filter, final Boolean showHiddenFiles );
  
  List<RepositoryFile> getChildren( RepositoryRequest repositoryRequest );

  RepositoryFile updateFile( final RepositoryFile file, final IRepositoryFileData data, final String versionMessage );

  void deleteFile( final Serializable fileId, final String versionMessage );

  void deleteFileAtVersion( final Serializable fileId, final Serializable versionId );

  void undeleteFile( final Serializable fileId, final String versionMessage );

  void permanentlyDeleteFile( final Serializable fileId, final String versionMessage );

  List<RepositoryFile> getDeletedFiles( final String origParentFolderPath, final String filter );

  List<RepositoryFile> getDeletedFiles();

  boolean canUnlockFile( final Serializable fileId );

  void lockFile( final Serializable fileId, final String message );

  void unlockFile( final Serializable fileId );

  List<VersionSummary> getVersionSummaries( final Serializable fileId );

  VersionSummary getVersionSummary( final Serializable fileId, final Serializable versionId );

  RepositoryFile getFile( final Serializable fileId, final Serializable versionId );

  void moveFile( final Serializable fileId, final String destRelPath, final String versionMessage );

  void copyFile( final Serializable fileId, final String destAbsPath, final String versionMessage );

  void restoreFileAtVersion( final Serializable fileId, final Serializable versionId, final String versionMessage );

  List<RepositoryFile> getReferrers( final Serializable fileId );

  void setFileMetadata( final Serializable fileId, Map<String, Serializable> metadataMap );

  Map<String, Serializable> getFileMetadata( final Serializable fileId );

  List<Character> getReservedChars();

  List<Locale> getAvailableLocalesForFileById( final Serializable fileId );

  List<Locale> getAvailableLocalesForFileByPath( final String relPath );

  List<Locale> getAvailableLocalesForFile( final RepositoryFile repositoryFile );

  Properties getLocalePropertiesForFileById( final Serializable fileId, final String locale );

  Properties getLocalePropertiesForFileByPath( final String relPath, final String locale );

  Properties getLocalePropertiesForFile( final RepositoryFile repositoryFile, final String locale );

  void setLocalePropertiesForFileById( final Serializable fileId, final String locale, final Properties properties );

  void setLocalePropertiesForFileByPath( final String relPath, final String locale, final Properties properties );

  void
  setLocalePropertiesForFile( final RepositoryFile repositoryFile, final String locale, final Properties properties );

  void deleteLocalePropertiesForFile( final RepositoryFile repositoryFile, final String locale );
}
