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
import org.springframework.util.Assert;

/**
 * A wrapper around a repository that will throw an exception if a call is made to any method which would modify
 * the content or structure of the repository.
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class UnmodifiableRepository implements IUnifiedRepository {
  private IUnifiedRepository repository;

  /**
   * Creates an instance of the repository which wraps the repository passed
   * 
   * @param mockUnifiedRepository
   */
  public UnmodifiableRepository( final IUnifiedRepository repository ) {
    Assert.notNull( repository );
    this.repository = repository;
  }

  /**
   * Gets file. Use this method to test for file existence too.
   * 
   * @param path
   *          path to file
   * @return file or {@code null} if the file does not exist or access is denied
   */
  @Override
  public RepositoryFile getFile( final String path ) {
    return repository.getFile( path );
  }
   
  @Override
  public RepositoryFileTree getTree( RepositoryRequest repositoryRequest ) {
    return getTree(repositoryRequest.getPath(), repositoryRequest.getDepth(), repositoryRequest.getChildNodeFilter(), repositoryRequest.isShowHidden() );
  }

  /**
   * Gets a tree rooted at path.
   * 
   * @param path
   *          path to file
   * @param depth
   *          0 fetches just file at path; positive integer n fetches node at path plus n levels of children;
   *          negative integer fetches all children
   * @param filter
   *          filter may be a full name or a partial name with one or more wildcard characters ("*"), or a
   *          disjunction (using the "|" character to represent logical OR) of these; filter does not apply to root
   *          node
   * @param showHidden
   *          is a boolean which identify whether to include the hidden files/folders in the list or not
   * @return file or {@code null} if the file does not exist or access is denied
   */
  @Override
  public RepositoryFileTree getTree( final String path, final int depth, final String filter,
                                     final boolean showHidden ) {
    return repository.getTree( path, depth, filter, showHidden );
  }

  /**
   * Gets file as it was at the given version.
   * 
   * @param fileId
   *          file id
   * @param versionId
   *          version id
   * @return file at version
   */
  @Override
  public RepositoryFile getFileAtVersion( final Serializable fileId, final Serializable versionId ) {
    return repository.getFileAtVersion( fileId, versionId );
  }

  /**
   * Gets file. Use this method to test for file existence too.
   * 
   * @param fileId
   *          file id
   * @return file or {@code null} if the file does not exist or access is denied
   */
  @Override
  public RepositoryFile getFileById( final Serializable fileId ) {
    return repository.getFileById( fileId );
  }

  /**
   * Same as {@link #getFile(String)} except that if {@code loadMaps} is {@code true}, the maps for localized
   * strings will be loaded as well. (Normally these are not loaded.) Use {@code true} in editing tools that can
   * show the maps for editing purposes.
   * 
   * @param path
   *          path to file
   * @param loadLocaleMaps
   *          {@code true} to load localized string maps
   * @return file or {@code null} if the file does not exist or access is denied
   */
  @Override
  public RepositoryFile getFile( final String path, final boolean loadLocaleMaps ) {
    return repository.getFile( path, loadLocaleMaps );
  }

  /**
   * Same as {@link #getFile(String)} except that if {@code loadMaps} is {@code true}, the maps for localized
   * strings will be loaded as well. (Normally these are not loaded.) Use {@code true} in editing tools that can
   * show the maps for editing purposes.
   * 
   * @param fileId
   *          file id
   * @param loadLocaleMaps
   *          {@code true} to load localized string maps
   * @return file or {@code null} if the file does not exist or access is denied
   */
  @Override
  public RepositoryFile getFileById( final Serializable fileId, final boolean loadLocaleMaps ) {
    return repository.getFileById( fileId, loadLocaleMaps );
  }

  @Override
  public RepositoryFile getFile( String path, IPentahoLocale locale ) {
    return this.repository.getFile( path, locale );
  }

  @Override
  public RepositoryFile getFileById( Serializable fileId, IPentahoLocale locale ) {
    return this.repository.getFileById( fileId, locale );
  }

  @Override
  public RepositoryFile getFile( String path, boolean loadLocaleMaps, IPentahoLocale locale ) {
    return this.repository.getFile( path, loadLocaleMaps, locale );
  }

  @Override
  public RepositoryFile getFileById( Serializable fileId, boolean loadLocaleMaps, IPentahoLocale locale ) {
    return this.repository.getFileById( fileId, loadLocaleMaps, locale );
  }

  /**
   * Gets data at base version for read.
   * 
   * @param fileId
   *          file id
   * @param dataClass
   *          class that implements {@link org.pentaho.platform.api.repository2.unified.IRepositoryFileData}
   * @return data
   */
  @Override
  public <T extends IRepositoryFileData> T getDataForRead( final Serializable fileId, final Class<T> dataClass ) {
    return repository.getDataForRead( fileId, dataClass );
  }

  /**
   * Gets data at given version for read.
   * 
   * @param fileId
   *          file id
   * @param versionId
   *          version id
   * @param dataClass
   *          class that implements {@link org.pentaho.platform.api.repository2.unified.IRepositoryFileData}
   * @return data
   */
  @Override
  public <T extends IRepositoryFileData> T getDataAtVersionForRead( final Serializable fileId,
      final Serializable versionId, final Class<T> dataClass ) {
    return repository.getDataAtVersionForRead( fileId, versionId, dataClass );
  }

  /**
   * Gets data at base version for execute.
   * 
   * @param fileId
   *          file id
   * @param dataClass
   *          class that implements {@link org.pentaho.platform.api.repository2.unified.IRepositoryFileData}
   * @return data
   */
  @Override
  public <T extends IRepositoryFileData> T getDataForExecute( final Serializable fileId, final Class<T> dataClass ) {
    return repository.getDataForExecute( fileId, dataClass );
  }

  /**
   * Gets data at given version for read.
   * 
   * @param fileId
   *          file id
   * @param versionId
   *          version id
   * @param dataClass
   *          class that implements {@link org.pentaho.platform.api.repository2.unified.IRepositoryFileData}
   * @return data
   */
  @Override
  public <T extends IRepositoryFileData> T getDataAtVersionForExecute( final Serializable fileId,
      final Serializable versionId, final Class<T> dataClass ) {
    return repository.getDataAtVersionForExecute( fileId, versionId, dataClass );
  }

  /**
   * Gets the data for multiple {@link org.pentaho.platform.api.repository2.unified.RepositoryFile}s for read. Each
   * {@link org.pentaho.platform.api.repository2.unified.RepositoryFile} may or may not contain a version number.
   * If a version number is omitted it is assumed the latest data for the
   * {@link org.pentaho.platform.api.repository2.unified.RepositoryFile} is being requested.
   * 
   * @param <T>
   *          Type of {@link org.pentaho.platform.api.repository2.unified.IRepositoryFileData}
   * @param files
   *          Repository files to fetch data for. Only
   *          {@link org.pentaho.platform.api.repository2.unified.RepositoryFile#getId()} and
   *          {@link org.pentaho.platform.api.repository2.unified.RepositoryFile#getVersionId()} are used to
   *          identify {@link org.pentaho.platform.api.repository2.unified.IRepositoryFileData} objects to return.
   * @param dataClass
   *          class that implements {@link org.pentaho.platform.api.repository2.unified.IRepositoryFileData}
   * @return data
   */
  @Override
  public <T extends IRepositoryFileData> List<T> getDataForReadInBatch( final List<RepositoryFile> files,
      final Class<T> dataClass ) {
    return repository.getDataForReadInBatch( files, dataClass );
  }

  /**
   * Gets the data for multiple {@link org.pentaho.platform.api.repository2.unified.RepositoryFile}s for execute.
   * Each {@link org.pentaho.platform.api.repository2.unified.RepositoryFile} may or may not contain a version
   * number. If a version number is omitted it is assumed the latest data for the
   * {@link org.pentaho.platform.api.repository2.unified.RepositoryFile} is being requested.
   * 
   * @param <T>
   *          Type of {@link org.pentaho.platform.api.repository2.unified.IRepositoryFileData}
   * @param files
   *          Repository files to fetch data for. Only
   *          {@link org.pentaho.platform.api.repository2.unified.RepositoryFile#getId()} and
   *          {@link org.pentaho.platform.api.repository2.unified.RepositoryFile#getVersionId()} are used to
   *          identify {@link org.pentaho.platform.api.repository2.unified.IRepositoryFileData} objects to return.
   * @param dataClass
   *          class that implements {@link org.pentaho.platform.api.repository2.unified.IRepositoryFileData}
   * @return data
   */
  @Override
  public <T extends IRepositoryFileData> List<T> getDataForExecuteInBatch( final List<RepositoryFile> files,
      final Class<T> dataClass ) {
    return repository.getDataForExecuteInBatch( files, dataClass );
  }

  /**
   * Creates a file.
   * 
   * @param parentFolderId
   *          parent folder id
   * @param file
   *          file to create
   * @param data
   *          file data
   * @param versionMessage
   *          optional version comment to be applied to parentFolder
   * @return file that is equal to given file except with id populated
   */
  @Override
  public RepositoryFile createFile( final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final String versionMessage ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Creates a file.
   * 
   * @param parentFolderId
   *          parent folder id
   * @param file
   *          file to create
   * @param data
   *          file data
   * @param acl
   *          file acl
   * @param versionMessage
   *          optional version comment to be applied to parentFolder
   * @return file that is equal to given file except with id populated
   */
  @Override
  public RepositoryFile createFile( final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final RepositoryFileAcl acl, final String versionMessage ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Creates a folder.
   * 
   * @param parentFolderId
   *          parent folder id
   * @param file
   *          file to create
   * @param versionMessage
   *          optional version comment to be applied to parentFolder
   * @return file that is equal to given file except with id populated
   */
  @Override
  public RepositoryFile createFolder( final Serializable parentFolderId, final RepositoryFile file,
      final String versionMessage ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Creates a folder.
   * 
   * @param parentFolderId
   *          parent folder id
   * @param file
   *          file to create
   * @param acl
   *          file acl
   * @param versionMessage
   *          optional version comment to be applied to parentFolder
   * @return file that is equal to given file except with id populated
   */
  @Override
  public RepositoryFile createFolder( final Serializable parentFolderId, final RepositoryFile file,
      final RepositoryFileAcl acl, final String versionMessage ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Returns the children of this folder.
   * 
   * @param folderId
   *          id of folder whose children to fetch
   * @return list of children (never {@code null})
   */
  @Override
  public List<RepositoryFile> getChildren( final Serializable folderId ) {
    return repository.getChildren( folderId );
  }

  @Override
  public List<RepositoryFile> getChildren( RepositoryRequest repositoryRequest ) {
    return getChildren( repositoryRequest.getPath(), repositoryRequest.getChildNodeFilter(), repositoryRequest.isShowHidden() );
  }
  
  /**
   * Returns the children of this folder that match the specified filter.
   * 
   * @param folderId
   *          id of folder whose children to fetch
   * @param filter
   *          filter may be a full name or a partial name with one or more wildcard characters ("*"), or a
   *          disjunction (using the "|" character to represent logical OR) of these
   * @return list of children (never {@code null})
   */
  @Override
  public List<RepositoryFile> getChildren( final Serializable folderId, final String filter ) {
    return repository.getChildren( folderId, filter );
  }
  
  /**
   * Returns the children of this folder that match the specified filter.
   * 
   * @param folderId
   *          id of folder whose children to fetch
   * @param filter
   *          filter may be a full name or a partial name with one or more wildcard characters ("*"), or a
   *          disjunction (using the "|" character to represent logical OR) of these
   * @return list of children (never {@code null})
   */
  @Override
  public List<RepositoryFile> getChildren( final Serializable folderId, final String filter, Boolean showHiddenFiles ) {
    return repository.getChildren( folderId, filter, showHiddenFiles );
  }

  /**
   * Updates a file and/or the data of a file.
   * 
   * @param file
   *          updated file (not a folder); must have non-null id
   * @param data
   *          updated data
   * @param versionMessage
   *          (optional) version comment
   * @return updated file (possibly with new version number)
   */
  @Override
  public RepositoryFile updateFile( final RepositoryFile file, final IRepositoryFileData data,
      final String versionMessage ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Deletes a file.
   * 
   * @param fileId
   *          file id
   * @param permanent
   *          if {@code true}, once file is deleted, it cannot be undeleted
   * @param versionMessage
   *          optional version comment
   */
  @Override
  public void deleteFile( final Serializable fileId, final boolean permanent, final String versionMessage ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Deletes a file in a recoverable manner.
   * 
   * @param fileId
   *          file id
   * @param versionMessage
   *          optional version comment
   */
  @Override
  public void deleteFile( final Serializable fileId, final String versionMessage ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Moves and/or renames file. Folders are recursive. Throws exception on collision (merging does not occur).
   * 
   * @param fileId
   *          id of file or folder to move and/or rename
   * @param destAbsPath
   *          path to destination; if only moving then destAbsPath will be an existing folder
   * @param versionMessage
   *          optional version comment to be applied to source and destination parent folders
   */
  @Override
  public void moveFile( final Serializable fileId, final String destAbsPath, final String versionMessage ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Copies file. Folders are recursive. Throws exception on collision (merging does not occur).
   * 
   * @param fileId
   *          id of file or folder to copy
   * @param destAbsPath
   *          path to destination; if only copying (without name change) then destAbsPath will be an existing
   *          folder
   * @param versionMessage
   *          optional version comment to be applied to destination parent folder
   */
  @Override
  public void copyFile( final Serializable fileId, final String destAbsPath, final String versionMessage ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Recovers a deleted file if it was not permanently deleted. File is recovered to its original folder.
   * 
   * @param fileId
   *          deleted file id
   * @param versionMessage
   *          optional version comment to be applied to original parent folder
   */
  @Override
  public void undeleteFile( final Serializable fileId, final String versionMessage ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Gets all deleted files for the current user in this folder.
   * 
   * @param origParentFolderPath
   *          path to original parent folder
   * @return list of deleted files
   */
  @Override
  public List<RepositoryFile> getDeletedFiles( final String origParentFolderPath ) {
    return repository.getDeletedFiles( origParentFolderPath );
  }

  /**
   * Gets all deleted files for the current user in this folder.
   * 
   * @param origParentFolderPath
   *          path to original parent folder
   * @param filter
   *          filter may be a full name or a partial name with one or more wildcard characters ("*"), or a
   *          disjunction (using the "|" character to represent logical OR) of these
   * @return list of deleted files
   */
  @Override
  public List<RepositoryFile> getDeletedFiles( final String origParentFolderPath, final String filter ) {
    return repository.getDeletedFiles( origParentFolderPath, filter );
  }

  /**
   * Gets all deleted files for the current user. This is the "recycle bin" view.
   * 
   * @return list of deleted files
   */
  @Override
  public List<RepositoryFile> getDeletedFiles() {
    return repository.getDeletedFiles();
  }

  /**
   * Returns {@code true} if the current user can unlock the file. This might be a function of access control.
   * 
   * @param fileId
   *          file id
   * @return {@code true} if the current user can unlock the file
   */
  @Override
  public boolean canUnlockFile( final Serializable fileId ) {
    return repository.canUnlockFile( fileId );
  }

  /**
   * Locks a file.
   * 
   * @param fileId
   *          file id
   * @param lock
   *          message
   */
  @Override
  public void lockFile( final Serializable fileId, final String message ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Unlocks a file.
   * 
   * @param fileId
   *          file id
   */
  @Override
  public void unlockFile( final Serializable fileId ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Returns ACL for file.
   * 
   * @param fileId
   *          file id
   * @return access control list
   */
  @Override
  public RepositoryFileAcl getAcl( final Serializable fileId ) {
    return repository.getAcl( fileId );
  }

  /**
   * Updates an ACL.
   * 
   * @param acl
   *          ACL to set; must have non-null id
   * @return updated ACL as it would be if calling {@link #getAcl(java.io.Serializable)}
   */
  @Override
  public RepositoryFileAcl updateAcl( final RepositoryFileAcl acl ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Returns {@code true} if user has all permissions given. Note that {@code false} is returned when the path does
   * not exist.
   * 
   * @param path
   *          path to file or folder
   * @param permissions
   *          permissions to check
   * @return {@code true} if user has all permissions given
   */
  @Override
  public boolean hasAccess( final String path, final EnumSet<RepositoryFilePermission> permissions ) {
    return repository.hasAccess( path, permissions );
  }

  /**
   * Returns the list of access control entries (ACEs) that will be used to make an access control decision. This
   * method is equivalent to {@code getEffectiveAces(fileId, false)}.
   * 
   * @param fileId
   *          file id
   * @return list of ACEs
   */
  @Override
  public List<RepositoryFileAce> getEffectiveAces( final Serializable fileId ) {
    return repository.getEffectiveAces( fileId );
  }

  /**
   * Returns the list of access control entries (ACEs) that will be used to make an access control decision. This
   * method is equivalent to {@code getEffectiveAces(get_parent_id(fileId))}. Note that {@code get_parent_id} is
   * not a real method.
   * 
   * @param fileId
   *          file id
   * @param forceEntriesInheriting
   *          {@code true} to treat ACL as if {@code isEntriesInheriting} was true; this avoids having the caller
   *          fetch the parent of ACL belonging to file with {@code fileId}; no change is persisted to the ACL
   * @return list of ACEs
   */
  @Override
  public List<RepositoryFileAce> getEffectiveAces( final Serializable fileId, final boolean forceEntriesInheriting ) {
    return repository.getEffectiveAces( fileId, forceEntriesInheriting );
  }

  /**
   * Returns a version summary for the given file id and version id.
   * 
   * @param fileId
   *          file id
   * @param versionId
   *          version id (if {@code null}, returns the last version)
   * @return version summary
   */
  @Override
  public VersionSummary getVersionSummary( final Serializable fileId, final Serializable versionId ) {
    return repository.getVersionSummary( fileId, versionId );
  }

  /**
   * Returns a version summary for every {@link org.pentaho.platform.api.repository2.unified.RepositoryFile}
   * provided. Each {@link org.pentaho.platform.api.repository2.unified.RepositoryFile} may or may not contain a
   * version number. If a version number is omitted it is assumed the latest version for the
   * {@link org.pentaho.platform.api.repository2.unified.RepositoryFile} is being requested.
   * 
   * @param files
   *          Repository files to fetch version summaries for. Only
   *          {@link org.pentaho.platform.api.repository2.unified.RepositoryFile#getId()} and
   *          {@link org.pentaho.platform.api.repository2.unified.RepositoryFile#getVersionId()} are used to
   *          identify {@link org.pentaho.platform.api.repository2.unified.VersionSummary} objects to return.
   * @return version summary for every file provided
   */
  @Override
  public List<VersionSummary> getVersionSummaryInBatch( final List<RepositoryFile> files ) {
    return repository.getVersionSummaryInBatch( files );
  }

  /**
   * Returns a list of version summary instances. The first version in the list is the root version. The last
   * version in the list is the base version. Branching and merging are not supported so this is a simple list.
   * 
   * @param fileId
   *          file id
   * @return list of version summaries (never {@code null})
   */
  @Override
  public List<VersionSummary> getVersionSummaries( final Serializable fileId ) {
    return repository.getVersionSummaries( fileId );
  }

  /**
   * Permanently deletes a specific version of a file. The version is removed from the version history of the given
   * file.
   * 
   * @param fileId
   *          file id
   * @param versionId
   *          version id (MUST not be null)
   */
  @Override
  public void deleteFileAtVersion( final Serializable fileId, final Serializable versionId ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Makes a file, as it was at the given version, the latest version. Result should be the same as if the user had
   * called
   * {@link #updateFile(org.pentaho.platform.api.repository2.unified.RepositoryFile, org.pentaho.
   * platform.api.repository2.unified.IRepositoryFileData, String)}
   * with a file and data that matched the state of the file and data at the given version.
   * 
   * @param fileId
   *          file id
   * @param versionId
   *          version id
   * @param versionMessage
   *          optional version comment
   */
  @Override
  public void
  restoreFileAtVersion( final Serializable fileId, final Serializable versionId, final String versionMessage ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Get a list of {@link org.pentaho.platform.api.repository2.unified.RepositoryFile}s that reference the
   * RepositoryFile identified by fileId.
   * 
   * @param fileId
   *          file id
   * @return list of repository files that reference the fileId repository file
   */
  @Override
  public List<RepositoryFile> getReferrers( final Serializable fileId ) {
    return repository.getReferrers( fileId );
  }

  /**
   * Sets a metadata object for the given fileid
   * 
   * @param fileId
   *          file id
   * @param metadataMap
   *          Map of properties to apply to this file.
   */
  @Override
  public void setFileMetadata( final Serializable fileId, final Map<String, Serializable> metadataMap ) {
    throw new UnmodifiableRepositoryException();
  }

  /**
   * Gets a metadata for the given fileid
   * 
   * @param fileId
   *          file id
   * @return Map<String, Serializable> of all the metadata for this file
   */
  @Override
  public Map<String, Serializable> getFileMetadata( final Serializable fileId ) {
    return repository.getFileMetadata( fileId );
  }

  /**
   * Exception which indicates an operation is trying to modify the content of this unmodifiable repository
   */
  private class UnmodifiableRepositoryException extends RuntimeException {
  }

  @Override
  public List<Character> getReservedChars() {
    return repository.getReservedChars();
  }

  @Override
  public List<Locale> getAvailableLocalesForFileById( Serializable fileId ) {
    Assert.notNull( fileId );
    return repository.getAvailableLocalesForFileById( fileId );
  }

  @Override
  public List<Locale> getAvailableLocalesForFileByPath( String relPath ) {
    Assert.notNull( relPath );
    return repository.getAvailableLocalesForFileByPath( relPath );
  }

  @Override
  public List<Locale> getAvailableLocalesForFile( RepositoryFile repositoryFile ) {
    Assert.notNull( repositoryFile );
    return repository.getAvailableLocalesForFile( repositoryFile );
  }

  @Override
  public Properties getLocalePropertiesForFileById( Serializable fileId, String locale ) {
    Assert.notNull( fileId );
    Assert.notNull( locale );
    return repository.getLocalePropertiesForFileById( fileId, locale );
  }

  @Override
  public Properties getLocalePropertiesForFileByPath( String relPath, String locale ) {
    Assert.notNull( relPath );
    Assert.notNull( locale );
    return repository.getLocalePropertiesForFileByPath( relPath, locale );
  }

  @Override
  public Properties getLocalePropertiesForFile( RepositoryFile repositoryFile, String locale ) {
    Assert.notNull( repositoryFile );
    Assert.notNull( locale );
    return repository.getLocalePropertiesForFile( repositoryFile, locale );
  }

  @Override
  public void setLocalePropertiesForFileById( Serializable fileId, String locale, Properties properties ) {
    Assert.notNull( fileId );
    Assert.notNull( locale );
    Assert.notNull( properties );
    repository.setLocalePropertiesForFileById( fileId, locale, properties );
  }

  @Override
  public void setLocalePropertiesForFileByPath( String relPath, String locale, Properties properties ) {
    Assert.notNull( relPath );
    Assert.notNull( locale );
    Assert.notNull( properties );
    repository.setLocalePropertiesForFileByPath( relPath, locale, properties );
  }

  @Override
  public void setLocalePropertiesForFile( RepositoryFile repositoryFile, String locale, Properties properties ) {
    Assert.notNull( repositoryFile );
    Assert.notNull( locale );
    Assert.notNull( properties );
    repository.setLocalePropertiesForFile( repositoryFile, locale, properties );
  }

  @Override
  public void deleteLocalePropertiesForFile( RepositoryFile repositoryFile, String locale ) {
    Assert.notNull( repositoryFile );
    Assert.notNull( locale );
    repository.deleteLocalePropertiesForFile( repositoryFile, locale );
  }

  @Override
  public RepositoryFile updateFolder( RepositoryFile folder, String versionMessage ) {
    Assert.notNull( folder );
    Assert.isTrue( folder.isFolder() );
    return repository.updateFolder( folder, versionMessage );
  }

}
