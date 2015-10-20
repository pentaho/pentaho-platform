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

package org.pentaho.platform.api.repository2.unified;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.pentaho.platform.api.locale.IPentahoLocale;

/**
 * Entry point into the unified repository. The finest grained object that can be read and written to this
 * repository is a {@link RepositoryFile}.
 * 
 * @author mlowery
 */
public interface IUnifiedRepository {
  public static final String SYSTEM_FOLDER = "system_folder"; //$NON-NLS-1$

  /**
   * Gets file. Use this method to test for file existence too.
   * 
   * @param path
   *          path to file
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFile( final String path );

  /**
   * @Deprecated  Construct a RepositoryRequest and use <code>getTree(RepositoryRequest)</code>
   * 
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
   *          node. Filter segments can also filter the results to just Files or Folders by passing in one of the
   *          following: ( FILES | FOLDERS | [default] FILES_FOLDERS )
   * @param showHidden
   *          is a boolean which identify whether to include the hidden files/folders in the list or not
   * @return file or {@code null} if the file does not exist or access is denied
   */
  @Deprecated
  RepositoryFileTree getTree( final String path, final int depth, final String filter, final boolean showHidden );

  /**
   * Gets a tree rooted at path.
   * 
   * @param repositoryRequest
   *          Contains the information necessary to process the request.  See the {@link RepositoryRequest} class.
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFileTree getTree( final RepositoryRequest repositoryRequest );

  /**
   * Gets file as it was at the given version.
   * 
   * @param fileId
   *          file id
   * @param versionId
   *          version id
   * @return file at version
   */
  RepositoryFile getFileAtVersion( final Serializable fileId, final Serializable versionId );

  /**
   * Gets file. Use this method to test for file existence too.
   * 
   * @param fileId
   *          file id
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFileById( final Serializable fileId );

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
  RepositoryFile getFile( final String path, final boolean loadLocaleMaps );

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
  RepositoryFile getFileById( final Serializable fileId, final boolean loadLocaleMaps );

  /**
   * Like {@link #getFile(String, boolean)} except that the maps which are pulled back are slimmed down to only
   * contain the localized strings
   * 
   * @param path
   *          {@link String} of the path to the file
   * @param locale
   *          {@link IPentahoLocale} which the user wishes to have contained in the map
   * @return {@link RepositoryFile}
   */
  RepositoryFile getFile( final String path, final IPentahoLocale locale );

  /**
   * Same as {@link #getFile(String, IPentahoLocale)} except that the {@link Serializable} file id is required
   * 
   * @param fileId
   *          {@link Serializable} file Id of the file
   * @param locale
   *          {@link IPentahoLocale} which the user wishes to have contained in the map
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFileById( final Serializable fileId, final IPentahoLocale locale );

  /**
   * Retrieves a file at its given version
   * 
   * @param path
   *          {@link String} full path to file
   * @param loadLocaleMaps
   *          boolean - to determine whether to load the locale map
   * @param locale
   *          {@link IPentahoLocale} locale to retrieve for {@link RepositoryFile}
   * 
   * @return {@link RepositoryFile}
   */
  RepositoryFile getFile( final String path, final boolean loadLocaleMaps, final IPentahoLocale locale );

  /**
   * Retrieves a file at its given version by its id
   * 
   * @param fileId
   *          {@link Serializable} file id
   * 
   * @param loadLocaleMaps
   *          boolean - to determine whether to load the locale map
   * @param locale
   *          {@link IPentahoLocale} locale to retrieve for {@link RepositoryFile}
   * 
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFileById( final Serializable fileId, final boolean loadLocaleMaps, final IPentahoLocale locale );

  /**
   * Gets data at base version for read.
   * 
   * @param fileId
   *          file id
   * @param dataClass
   *          class that implements {@link IRepositoryFileData}
   * @return data
   */
  <T extends IRepositoryFileData> T getDataForRead( final Serializable fileId, final Class<T> dataClass );

  /**
   * Gets data at given version for read.
   * 
   * @param fileId
   *          file id
   * @param versionId
   *          version id
   * @param dataClass
   *          class that implements {@link IRepositoryFileData}
   * @return data
   */
  <T extends IRepositoryFileData> T getDataAtVersionForRead( final Serializable fileId, final Serializable versionId,
      final Class<T> dataClass );

  /**
   * Gets data at base version for execute.
   * 
   * @param fileId
   *          file id
   * @param dataClass
   *          class that implements {@link IRepositoryFileData}
   * @return data
   */
  <T extends IRepositoryFileData> T getDataForExecute( final Serializable fileId, final Class<T> dataClass );

  /**
   * Gets data at given version for read.
   * 
   * @param fileId
   *          file id
   * @param versionId
   *          version id
   * @param dataClass
   *          class that implements {@link IRepositoryFileData}
   * @return data
   */
  <T extends IRepositoryFileData> T getDataAtVersionForExecute( final Serializable fileId,
      final Serializable versionId, final Class<T> dataClass );

  /**
   * Gets the data for multiple {@link RepositoryFile}s for read. Each {@link RepositoryFile} may or may not
   * contain a version number. If a version number is omitted it is assumed the latest data for the
   * {@link RepositoryFile} is being requested.
   * 
   * @param <T>
   *          Type of {@link IRepositoryFileData}
   * @param files
   *          Repository files to fetch data for. Only {@link RepositoryFile#getId()} and
   *          {@link RepositoryFile#getVersionId()} are used to identify {@link IRepositoryFileData} objects to
   *          return.
   * @param dataClass
   *          class that implements {@link IRepositoryFileData}
   * @return data
   */
  <T extends IRepositoryFileData> java.util.List<T> getDataForReadInBatch( final List<RepositoryFile> files,
      final Class<T> dataClass );

  /**
   * Gets the data for multiple {@link RepositoryFile}s for execute. Each {@link RepositoryFile} may or may not
   * contain a version number. If a version number is omitted it is assumed the latest data for the
   * {@link RepositoryFile} is being requested.
   * 
   * @param <T>
   *          Type of {@link IRepositoryFileData}
   * @param files
   *          Repository files to fetch data for. Only {@link RepositoryFile#getId()} and
   *          {@link RepositoryFile#getVersionId()} are used to identify {@link IRepositoryFileData} objects to
   *          return.
   * @param dataClass
   *          class that implements {@link IRepositoryFileData}
   * @return data
   */
  <T extends IRepositoryFileData> java.util.List<T> getDataForExecuteInBatch( final List<RepositoryFile> files,
      final Class<T> dataClass );

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
  RepositoryFile createFile( final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final String versionMessage );

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
  RepositoryFile createFile( final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final RepositoryFileAcl acl, final String versionMessage );

  /**
   * Creates a folder.
   * 
   * @param parFolderId
   *          parent folder id
   * @param file
   *          file to create
   * @param versionMessage
   *          optional version comment to be applied to parentFolder
   * @return file that is equal to given file except with id populated
   */
  RepositoryFile createFolder( final Serializable parFolderId, final RepositoryFile file, final String versionMessage );

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
  RepositoryFile createFolder( final Serializable parentFolderId, final RepositoryFile file,
      final RepositoryFileAcl acl, final String versionMessage );

  /**
   * Updates a file and/or the data of a file.
   * 
   * @param folder
   *          updated folder (not a file); must have non-null id
   * @param versionMessage
   *          (optional) version comment
   * @return updated folder (possibly with new version number)
   */
  RepositoryFile updateFolder( final RepositoryFile folder, final String versionMessage );

  /**
   * @Deprecated
   * Returns the children of this folder.
   * 
   * @param folderId
   *          id of folder whose children to fetch
   * @return list of children (never {@code null})
   */
  @Deprecated
  List<RepositoryFile> getChildren( final Serializable folderId );

  /**
   * @Deprecated
   * 
   * Returns the children of this folder that match the specified filter.
   * 
   * @param folderId
   *          id of folder whose children to fetch
   * @param filter
   *          filter may be a full name or a partial name with one or more wildcard characters ("*"), or a
   *          disjunction (using the "|" character to represent logical OR) of these
   * @return list of children (never {@code null})
   */
  @Deprecated
  List<RepositoryFile> getChildren( final Serializable folderId, final String filter );

  /**
   * @Deprecated
   * 
   * Returns the children of this folder that match the specified filter.
   * 
   * @param folderId
   *          id of folder whose children to fetch
   * @param filter
   *          filter may be a full name or a partial name with one or more wildcard characters ("*"), or a
   *          disjunction (using the "|" character to represent logical OR) of these
   * @param showHiddenFiles flag determines whether to return the hidden files in he response or not         
   * @return list of children (never {@code null})
   */
  @Deprecated
  List<RepositoryFile> getChildren( final Serializable folderId, final String filter, final Boolean showHiddenFiles );

  /**
   * Returns the children according to the specifications of the RepositoryRequest object
   * 
   * @param repositoryRequest  See {@link RepositoryRequest) class
   * @return list of children (never {@code null})
   */
  List<RepositoryFile> getChildren( RepositoryRequest repositoryRequest );

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
  RepositoryFile updateFile( final RepositoryFile file, final IRepositoryFileData data, final String versionMessage );

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
  void deleteFile( final Serializable fileId, final boolean permanent, final String versionMessage );

  /**
   * Deletes a file in a recoverable manner.
   * 
   * @param fileId
   *          file id
   * @param versionMessage
   *          optional version comment
   */
  void deleteFile( final Serializable fileId, final String versionMessage );

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
  void moveFile( final Serializable fileId, final String destAbsPath, final String versionMessage );

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
  void copyFile( final Serializable fileId, final String destAbsPath, final String versionMessage );

  // ~ Undelete methods
  // ================================================================================================

  /**
   * Recovers a deleted file if it was not permanently deleted. File is recovered to its original folder.
   * 
   * @param fileId
   *          deleted file id
   * @param versionMessage
   *          optional version comment to be applied to original parent folder
   */
  void undeleteFile( final Serializable fileId, final String versionMessage );

  /**
   * Gets all deleted files for the current user in this folder.
   * 
   * @param origParentFolderPath
   *          path to original parent folder
   * @return list of deleted files
   */
  List<RepositoryFile> getDeletedFiles( final String origParentFolderPath );

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
  List<RepositoryFile> getDeletedFiles( final String origParentFolderPath, final String filter );

  /**
   * Gets all deleted files for the current user. This is the "recycle bin" view.
   * 
   * @return list of deleted files
   */
  List<RepositoryFile> getDeletedFiles();

  // ~ Lock methods
  // ====================================================================================================

  /**
   * Returns {@code true} if the current user can unlock the file. This might be a function of access control.
   * 
   * @param fileId
   *          file id
   * @return {@code true} if the current user can unlock the file
   */
  boolean canUnlockFile( final Serializable fileId );

  /**
   * Locks a file.
   * 
   * @param fileId
   *          file id
   * @param lock
   *          message
   */
  void lockFile( final Serializable fileId, final String message );

  /**
   * Unlocks a file.
   * 
   * @param fileId
   *          file id
   */
  void unlockFile( final Serializable fileId );

  // ~ Access read/write methods
  // =======================================================================================

  /**
   * Returns ACL for file.
   * 
   * @param fileId
   *          file id
   * @return access control list
   */
  RepositoryFileAcl getAcl( final Serializable fileId );

  /**
   * Updates an ACL.
   * 
   * @param acl
   *          ACL to set; must have non-null id
   * @return updated ACL as it would be if calling {@link #getAcl(Serializable)}
   */
  RepositoryFileAcl updateAcl( final RepositoryFileAcl acl );

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
  boolean hasAccess( final String path, final EnumSet<RepositoryFilePermission> permissions );

  /**
   * Returns the list of access control entries (ACEs) that will be used to make an access control decision. This
   * method is equivalent to {@code getEffectiveAces(fileId, false)}.
   * 
   * @param fileId
   *          file id
   * @return list of ACEs
   */
  List<RepositoryFileAce> getEffectiveAces( final Serializable fileId );

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
  List<RepositoryFileAce> getEffectiveAces( final Serializable fileId, final boolean forceEntriesInheriting );

  // ~ Version methods
  // =================================================================================================

  /**
   * Returns a version summary for the given file id and version id.
   * 
   * @param fileId
   *          file id
   * @param versionId
   *          version id (if {@code null}, returns the last version)
   * @return version summary
   */
  VersionSummary getVersionSummary( Serializable fileId, Serializable versionId );

  /**
   * Returns a version summary for every {@link RepositoryFile} provided. Each {@link RepositoryFile} may or may
   * not contain a version number. If a version number is omitted it is assumed the latest version for the
   * {@link RepositoryFile} is being requested.
   * 
   * @param files
   *          Repository files to fetch version summaries for. Only {@link RepositoryFile#getId()} and
   *          {@link RepositoryFile#getVersionId()} are used to identify {@link VersionSummary} objects to return.
   * @return version summary for every file provided
   */
  List<VersionSummary> getVersionSummaryInBatch( final List<RepositoryFile> files );

  /**
   * Returns a list of version summary instances. The first version in the list is the root version. The last
   * version in the list is the base version. Branching and merging are not supported so this is a simple list.
   * 
   * @param fileId
   *          file id
   * @return list of version summaries (never {@code null})
   */
  List<VersionSummary> getVersionSummaries( final Serializable fileId );

  /**
   * Permanently deletes a specific version of a file. The version is removed from the version history of the given
   * file.
   * 
   * @param fileId
   *          file id
   * @param versionId
   *          version id (MUST not be null)
   */
  void deleteFileAtVersion( final Serializable fileId, final Serializable versionId );

  /**
   * Makes a file, as it was at the given version, the latest version. Result should be the same as if the user had
   * called {@link #updateFile(RepositoryFile, IRepositoryFileData, String)} with a file and data that matched the
   * state of the file and data at the given version.
   * 
   * @param fileId
   *          file id
   * @param versionId
   *          version id
   * @param versionMessage
   *          optional version comment
   */
  void restoreFileAtVersion( final Serializable fileId, final Serializable versionId, final String versionMessage );

  /**
   * Get a list of {@link RepositoryFile}s that reference the RepositoryFile identified by fileId.
   * 
   * @param fileId
   *          file id
   * @return list of repository files that reference the fileId repository file
   */
  List<RepositoryFile> getReferrers( final Serializable fileId );

  // ~ Metadata methods
  // =================================================================================================

  /**
   * Sets a metadata object for the given fileid
   * 
   * @param fileId
   *          file id
   * @param metadataMap
   *          Map of properties to apply to this file.
   */
  void setFileMetadata( final Serializable fileId, final Map<String, Serializable> metadataMap );

  /**
   * Gets a metadata for the given fileid
   * 
   * @param fileId
   *          file id
   * @return Map<String, Serializable> of all the metadata for this file
   */
  Map<String, Serializable> getFileMetadata( final Serializable fileId );

  /**
   * Returns a list of characters which cannot be used in file/folder names. These characters must be escaped using
   * percent-encoding. Callers may safely cache this value. Note that it is the responsibility of the
   * implementation to guard against illegal permutations of non-reserved characters.
   * 
   * <blockquote> A percent-encoded octet is encoded as a character triplet, consisting of the percent character
   * "%" followed by the two hexadecimal digits representing that octet's numeric value. For example, "%20" is the
   * percent-encoding for the binary octet "00100000" (ABNF: %x20), which in US-ASCII corresponds to the space
   * character (SP). </blockquote>
   * 
   * @return list of reserved characters
   */
  List<Character> getReservedChars();

  List<Locale> getAvailableLocalesForFileById( final Serializable fileId );

  List<Locale> getAvailableLocalesForFileByPath( final String relPath );

  List<Locale> getAvailableLocalesForFile( final RepositoryFile repositoryFile );

  Properties getLocalePropertiesForFileById( final Serializable fileId, final String locale );

  Properties getLocalePropertiesForFileByPath( final String relPath, final String locale );

  Properties getLocalePropertiesForFile( final RepositoryFile repositoryFile, final String locale );

  void setLocalePropertiesForFileById( final Serializable fileId, final String locale, final Properties properties );

  void setLocalePropertiesForFileByPath( final String relPath, final String locale, final Properties properties );

  void setLocalePropertiesForFile( final RepositoryFile repoFile, final String locale, final Properties properties );

  void deleteLocalePropertiesForFile( final RepositoryFile repositoryFile, final String locale );
}
