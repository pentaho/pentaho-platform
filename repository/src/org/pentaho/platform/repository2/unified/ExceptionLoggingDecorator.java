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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.repository2.messages.Messages;
import org.springframework.util.Assert;

/**
 * Decorates another {@code IUnifiedRepository} instance and logs exceptions if they occur. Also, a new non-chained
 * exception is thrown. (The root cause will not leave this class.)
 * 
 * @author mlowery
 */
public class ExceptionLoggingDecorator implements IUnifiedRepository {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog( ExceptionLoggingDecorator.class );

  // ~ Instance fields
  // =================================================================================================

  private final IUnifiedRepository delegatee;

  private final Map<String, ExceptionConverter> exceptionConverterMap;

  // ~ Constructors
  // ====================================================================================================

  public ExceptionLoggingDecorator( final IUnifiedRepository delegatee,
      final Map<String, ExceptionConverter> exceptionConverterMap ) {
    super();
    Assert.notNull( delegatee );
    this.delegatee = delegatee;
    this.exceptionConverterMap = exceptionConverterMap;
  }

  // ~ Methods
  // =========================================================================================================

  public boolean canUnlockFile( final Serializable fileId ) {
    return callLogThrow( new Callable<Boolean>() {
      public Boolean call() throws Exception {
        return delegatee.canUnlockFile( fileId );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.canUnlockFile", fileId ) ); //$NON-NLS-1$
  }

  public RepositoryFile createFile( final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final String versionMessage ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.createFile( parentFolderId, file, data, versionMessage );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.createFile", file.getName() ) ); //$NON-NLS-1$
  }

  public RepositoryFile createFolder( final Serializable parentFolderId, final RepositoryFile file,
      final String versionMessage ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.createFolder( parentFolderId, file, versionMessage );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.createFolder", file.getName() ) ); //$NON-NLS-1$
  }

  public void deleteFile( final Serializable fileId, final boolean permanent, final String versionMessage ) {
    callLogThrow( new Callable<Void>() {
      public Void call() throws Exception {
        delegatee.deleteFile( fileId, permanent, versionMessage );
        return null;
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.deleteFile", fileId ) ); //$NON-NLS-1$
  }

  public void deleteFile( final Serializable fileId, final String versionMessage ) {
    callLogThrow( new Callable<Void>() {
      public Void call() throws Exception {
        delegatee.deleteFile( fileId, versionMessage );
        return null;
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.deleteFile", fileId ) ); //$NON-NLS-1$
  }

  public void deleteFileAtVersion( final Serializable fileId, final Serializable versionId ) {
    callLogThrow( new Callable<Void>() {
      public Void call() throws Exception {
        delegatee.deleteFileAtVersion( fileId, versionId );
        return null;
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.deleteFileAtVersion", fileId, versionId ) ); //$NON-NLS-1$
  }

  public RepositoryFileAcl getAcl( final Serializable fileId ) {
    return callLogThrow( new Callable<RepositoryFileAcl>() {
      public RepositoryFileAcl call() throws Exception {
        return delegatee.getAcl( fileId );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getAcl", fileId ) ); //$NON-NLS-1$
  }
  
  @Override
  public List<RepositoryFile> getChildren( final RepositoryRequest repositoryRequest ) {
    return callLogThrow( new Callable<List<RepositoryFile>>() {
      public List<RepositoryFile> call() throws Exception {
        return delegatee.getChildren( repositoryRequest );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getChildren", repositoryRequest.getPath() ) ); //$NON-NLS-1$
  }

  @Deprecated
  public List<RepositoryFile> getChildren( final Serializable folderId ) {
    return getChildren( folderId, "", false);
  }

  @Deprecated
  public List<RepositoryFile> getChildren( final Serializable folderId, final String filter) {
    return getChildren( folderId, filter, false);
  }

  @Deprecated
  public List<RepositoryFile> getChildren( final Serializable folderId, final String filter, final Boolean showHiddenFiles ) {
    return getChildren( new RepositoryRequest(folderId.toString(), showHiddenFiles, -1, filter ) );
  }

  public <T extends IRepositoryFileData> T getDataAtVersionForExecute( final Serializable fileId,
      final Serializable versionId, final Class<T> dataClass ) {
    return callLogThrow( new Callable<T>() {
      public T call() throws Exception {
        return delegatee.getDataAtVersionForExecute( fileId, versionId, dataClass );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getDataAtVersion", fileId, versionId ) ); //$NON-NLS-1$
  }

  public <T extends IRepositoryFileData> T getDataAtVersionForRead( final Serializable fileId,
      final Serializable versionId, final Class<T> dataClass ) {
    return callLogThrow( new Callable<T>() {
      public T call() throws Exception {
        return delegatee.getDataAtVersionForRead( fileId, versionId, dataClass );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getDataAtVersion", fileId, versionId ) ); //$NON-NLS-1$
  }

  public <T extends IRepositoryFileData> T getDataForExecute( final Serializable fileId, final Class<T> dataClass ) {
    return callLogThrow( new Callable<T>() {
      public T call() throws Exception {
        return delegatee.getDataForExecute( fileId, dataClass );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getData", fileId ) ); //$NON-NLS-1$
  }

  public <T extends IRepositoryFileData> java.util.List<T> getDataForExecuteInBatch( final List<RepositoryFile> files,
      final Class<T> dataClass ) {
    return callLogThrow( new Callable<List<T>>() {
      public List<T> call() throws Exception {
        return delegatee.getDataForReadInBatch( files, dataClass );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getDataInBatch" ) ); //$NON-NLS-1$
  }

  public <T extends IRepositoryFileData> T getDataForRead( final Serializable fileId, final Class<T> dataClass ) {
    return callLogThrow( new Callable<T>() {
      public T call() throws Exception {
        return delegatee.getDataForRead( fileId, dataClass );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getData", fileId ) ); //$NON-NLS-1$
  }

  public <T extends IRepositoryFileData> List<T> getDataForReadInBatch( final List<RepositoryFile> files,
      final Class<T> dataClass ) {
    return callLogThrow( new Callable<List<T>>() {
      public List<T> call() throws Exception {
        return delegatee.getDataForReadInBatch( files, dataClass );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getDataInBatch" ) ); //$NON-NLS-1$
  }

  public List<RepositoryFile> getDeletedFiles( final String origParentFolderPath ) {
    return callLogThrow( new Callable<List<RepositoryFile>>() {
      public List<RepositoryFile> call() throws Exception {
        return delegatee.getDeletedFiles( origParentFolderPath );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getDeletedFilesInFolder", origParentFolderPath ) ); //$NON-NLS-1$
  }

  public List<RepositoryFile> getDeletedFiles( final String origParentFolderPath, final String filter ) {
    return callLogThrow( new Callable<List<RepositoryFile>>() {
      public List<RepositoryFile> call() throws Exception {
        return delegatee.getDeletedFiles( origParentFolderPath, filter );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getDeletedFilesInFolder", origParentFolderPath ) ); //$NON-NLS-1$
  }

  public List<RepositoryFile> getDeletedFiles() {
    return callLogThrow( new Callable<List<RepositoryFile>>() {
      public List<RepositoryFile> call() throws Exception {
        return delegatee.getDeletedFiles();
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getDeletedFiles" ) ); //$NON-NLS-1$
  }

  public List<RepositoryFileAce> getEffectiveAces( final Serializable fileId ) {
    return callLogThrow( new Callable<List<RepositoryFileAce>>() {
      public List<RepositoryFileAce> call() throws Exception {
        return delegatee.getEffectiveAces( fileId );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getEffectiveAces", fileId ) ); //$NON-NLS-1$
  }

  public List<RepositoryFileAce> getEffectiveAces( final Serializable fileId, final boolean forceEntriesInheriting ) {
    return callLogThrow( new Callable<List<RepositoryFileAce>>() {
      public List<RepositoryFileAce> call() throws Exception {
        return delegatee.getEffectiveAces( fileId, forceEntriesInheriting );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getEffectiveAces", fileId ) ); //$NON-NLS-1$
  }

  public RepositoryFile getFile( final String path ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.getFile( path );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getFile", path ) ); //$NON-NLS-1$
  }

  public RepositoryFile getFile( final String path, final boolean loadLocaleMaps ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.getFile( path, loadLocaleMaps );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getFile", path ) ); //$NON-NLS-1$
  }

  public RepositoryFile getFileAtVersion( final Serializable fileId, final Serializable versionId ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.getFileAtVersion( fileId, versionId );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getFileAtVersion", fileId, versionId ) ); //$NON-NLS-1$
  }

  public RepositoryFile getFileById( final Serializable fileId ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.getFileById( fileId );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getFileById", fileId ) ); //$NON-NLS-1$
  }

  public RepositoryFile getFileById( final Serializable fileId, final boolean loadLocaleMaps ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.getFileById( fileId, loadLocaleMaps );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getFileById", fileId ) ); //$NON-NLS-1$
  }

  @Override
  public RepositoryFile getFile( final String path, final IPentahoLocale locale ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.getFile( path, locale );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getFile", path ) ); //$NON-NLS-1$
  }

  @Override
  public RepositoryFile getFileById( final Serializable fileId, final IPentahoLocale locale ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.getFileById( fileId, locale );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getFileById", fileId ) ); //$NON-NLS-1$
  }

  @Override
  public RepositoryFile getFile( final String path, final boolean loadLocaleMaps, final IPentahoLocale locale ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.getFile( path, loadLocaleMaps, locale );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getFile", path ) ); //$NON-NLS-1$
  }

  @Override
  public RepositoryFile getFileById( final Serializable fileId, final boolean loadLocaleMaps,
      final IPentahoLocale locale ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.getFileById( fileId, loadLocaleMaps, locale );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getFileById", fileId ) ); //$NON-NLS-1$
  }

  public List<VersionSummary> getVersionSummaries( final Serializable fileId ) {
    return callLogThrow( new Callable<List<VersionSummary>>() {
      public List<VersionSummary> call() throws Exception {
        return delegatee.getVersionSummaries( fileId );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getVersionSummaries", fileId ) ); //$NON-NLS-1$
  }

  public VersionSummary getVersionSummary( final Serializable fileId, final Serializable versionId ) {
    return callLogThrow( new Callable<VersionSummary>() {
      public VersionSummary call() throws Exception {
        return delegatee.getVersionSummary( fileId, versionId );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getVersionSummary", fileId, versionId ) ); //$NON-NLS-1$
  }

  public List<VersionSummary> getVersionSummaryInBatch( final List<RepositoryFile> files ) {
    return callLogThrow( new Callable<List<VersionSummary>>() {
      public List<VersionSummary> call() throws Exception {
        return delegatee.getVersionSummaryInBatch( files );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getVersionSummaryInBatch" ) ); //$NON-NLS-1$
  }

  public boolean hasAccess( final String path, final EnumSet<RepositoryFilePermission> permissions ) {
    return callLogThrow( new Callable<Boolean>() {
      public Boolean call() throws Exception {
        return delegatee.hasAccess( path, permissions );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.hasAccess", path ) ); //$NON-NLS-1$
  }

  public void lockFile( final Serializable fileId, final String message ) {
    callLogThrow( new Callable<Void>() {
      public Void call() throws Exception {
        delegatee.lockFile( fileId, message );
        return null;
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.lockFile", fileId ) ); //$NON-NLS-1$
  }

  public void moveFile( final Serializable fileId, final String destAbsPath, final String versionMessage ) {
    callLogThrow( new Callable<Void>() {
      public Void call() throws Exception {
        delegatee.moveFile( fileId, destAbsPath, versionMessage );
        return null;
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.moveFile", fileId, destAbsPath ) ); //$NON-NLS-1$
  }

  public void copyFile( final Serializable fileId, final String destAbsPath, final String versionMessage ) {
    callLogThrow( new Callable<Void>() {
      public Void call() throws Exception {
        delegatee.copyFile( fileId, destAbsPath, versionMessage );
        return null;
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.copyFile", fileId, destAbsPath ) ); //$NON-NLS-1$
  }

  public void
  restoreFileAtVersion( final Serializable fileId, final Serializable versionId, final String versionMessage ) {
    callLogThrow( new Callable<Void>() {
      public Void call() throws Exception {
        delegatee.restoreFileAtVersion( fileId, versionId, versionMessage );
        return null;
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.restoreFileAtVersion", fileId, versionId ) ); //$NON-NLS-1$
  }

  public void undeleteFile( final Serializable fileId, final String versionMessage ) {
    callLogThrow( new Callable<Void>() {
      public Void call() throws Exception {
        delegatee.undeleteFile( fileId, versionMessage );
        return null;
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.undeleteFile", fileId ) ); //$NON-NLS-1$
  }

  public void unlockFile( final Serializable fileId ) {
    callLogThrow( new Callable<Void>() {
      public Void call() throws Exception {
        delegatee.unlockFile( fileId );
        return null;
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.unlockFile", fileId ) ); //$NON-NLS-1$
  }

  public RepositoryFileAcl updateAcl( final RepositoryFileAcl acl ) {
    return callLogThrow( new Callable<RepositoryFileAcl>() {
      public RepositoryFileAcl call() throws Exception {
        return delegatee.updateAcl( acl );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.updateAcl", acl != null ? acl.getId() : null ) ); //$NON-NLS-1$
  }

  public RepositoryFile updateFile( final RepositoryFile file, final IRepositoryFileData data,
      final String versionMessage ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.updateFile( file, data, versionMessage );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.updateFile", file != null ? file.getId() : null ) ); //$NON-NLS-1$
  }
  

  @Override
  public RepositoryFileTree getTree( final RepositoryRequest repositoryRequest ) {
    return callLogThrow( new Callable<RepositoryFileTree>() {
      public RepositoryFileTree call() throws Exception {
        return delegatee.getTree( repositoryRequest );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getTree", repositoryRequest.getPath() ) ); //$NON-NLS-1$
  }

  @Deprecated
  public RepositoryFileTree getTree( final String path, final int depth,
                                     final String filter, final boolean showHidden ) {
    
    return getTree( new RepositoryRequest( path, showHidden, depth, filter ) );
  }

  public RepositoryFile createFile( final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final RepositoryFileAcl acl, final String versionMessage ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.createFile( parentFolderId, file, data, acl, versionMessage );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.createFile", file.getName() ) ); //$NON-NLS-1$
  }

  public RepositoryFile createFolder( final Serializable parentFolderId, final RepositoryFile file,
      final RepositoryFileAcl acl, final String versionMessage ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.createFolder( parentFolderId, file, acl, versionMessage );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.createFolder", file.getName() ) ); //$NON-NLS-1$
  }

  /**
   * Calls the Callable and returns the value it returns. If an exception occurs, it is logged and a new
   * non-chained exception is thrown.
   * 
   * @param <T>
   *          return type
   * @param callable
   *          code to call
   * @param message
   *          verbose description of operation
   * @return return value of Callable
   */
  private <T> T callLogThrow( final Callable<T> callable, final String message ) {
    try {
      return callable.call();
    } catch ( Exception e ) {
      // generate reference #
      String refNum = UUID.randomUUID().toString();
      if ( logger.isDebugEnabled() ) {
        logger.debug( Messages.getInstance().getString( "ExceptionLoggingDecorator.referenceNumber", refNum ), e ); //$NON-NLS-1$
      }

      // list all exceptions in stack
      @SuppressWarnings( "unchecked" )
      List<Throwable> throwablesInStack = ExceptionUtils.getThrowableList( e );
      // reverse them so most specific exception (root cause) comes first
      Collections.reverse( throwablesInStack );

      for ( Throwable t : throwablesInStack ) {
        String className = t.getClass().getName();
        if ( exceptionConverterMap.containsKey( className ) ) {
          throw exceptionConverterMap.get( className ).convertException( (Exception) t, message, refNum );
        }

      }

      // no converter; throw general exception
      throw new UnifiedRepositoryException( Messages.getInstance().getString(
          "ExceptionLoggingDecorator.generalException", message, refNum ) ); //$NON-NLS-1$

    }
  }

  public List<RepositoryFile> getReferrers( final Serializable fileId ) {
    return callLogThrow( new Callable<List<RepositoryFile>>() {
      public List<RepositoryFile> call() throws Exception {
        return delegatee.getReferrers( fileId );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getReferrers", fileId ) ); //$NON-NLS-1$
  }

  public void setFileMetadata( final Serializable fileId, final Map<String, Serializable> metadataMap ) {
    callLogThrow( new Callable<Void>() {
      public Void call() throws Exception {
        delegatee.setFileMetadata( fileId, metadataMap );
        return null;
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.setFileMetadata", fileId, metadataMap ) ); //$NON-NLS-1$
  }

  public Map<String, Serializable> getFileMetadata( final Serializable fileId ) {
    return callLogThrow( new Callable<Map<String, Serializable>>() {
      public Map<String, Serializable> call() throws Exception {
        return delegatee.getFileMetadata( fileId );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getFileMetadata", fileId ) ); //$NON-NLS-1$
  }

  public List<Character> getReservedChars() {
    return callLogThrow( new Callable<List<Character>>() {
      public List<Character> call() throws Exception {
        return delegatee.getReservedChars();
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getReservedChars" ) ); //$NON-NLS-1$
  }

  /**
   * Converts an exception before throwing to callers.
   */
  public static interface ExceptionConverter {

    /**
     * Converts the exception.
     * 
     * @param exception
     *          exception
     * @param activityMessage
     *          message describing activity in progress when exception occurred
     * @param refNum
     *          reference number generated on server for this exception
     * @return converted exception
     */
    UnifiedRepositoryException convertException( final Exception exception, final String activityMessage,
        final String refNum );

  }

  @Override
  public List<Locale> getAvailableLocalesForFileById( final Serializable fileId ) {
    return callLogThrow( new Callable<List<Locale>>() {
      public List<Locale> call() throws Exception {
        return delegatee.getAvailableLocalesForFileById( fileId );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getAvailableLocalesForFile", fileId ) ); //$NON-NLS-1$
  }

  @Override
  public List<Locale> getAvailableLocalesForFileByPath( final String relPath ) {
    return callLogThrow( new Callable<List<Locale>>() {
      public List<Locale> call() throws Exception {
        return delegatee.getAvailableLocalesForFileByPath( relPath );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getAvailableLocalesForFile", relPath ) ); //$NON-NLS-1$
  }

  @Override
  public List<Locale> getAvailableLocalesForFile( final RepositoryFile repositoryFile ) {
    return callLogThrow( new Callable<List<Locale>>() {
      public List<Locale> call() throws Exception {
        return delegatee.getAvailableLocalesForFile( repositoryFile );
      }
    }, Messages.getInstance()
        .getString( "ExceptionLoggingDecorator.getAvailableLocalesForFile", repositoryFile.getId() ) ); //$NON-NLS-1$
  }

  @Override
  public Properties getLocalePropertiesForFileById( final Serializable fileId, final String locale ) {
    return callLogThrow( new Callable<Properties>() {
      public Properties call() throws Exception {
        return delegatee.getLocalePropertiesForFileById( fileId, locale );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getLocalePropertiesForFile", fileId ) ); //$NON-NLS-1$
  }

  @Override
  public Properties getLocalePropertiesForFileByPath( final String relPath, final String locale ) {
    return callLogThrow( new Callable<Properties>() {
      public Properties call() throws Exception {
        return delegatee.getLocalePropertiesForFileByPath( relPath, locale );
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.getLocalePropertiesForFile", relPath ) ); //$NON-NLS-1$
  }

  @Override
  public Properties getLocalePropertiesForFile( final RepositoryFile repositoryFile, final String locale ) {
    return callLogThrow( new Callable<Properties>() {
      public Properties call() throws Exception {
        return delegatee.getLocalePropertiesForFile( repositoryFile, locale );
      }
    }, Messages.getInstance()
        .getString( "ExceptionLoggingDecorator.getLocalePropertiesForFile", repositoryFile.getId() ) ); //$NON-NLS-1$
  }

  @Override
  public void setLocalePropertiesForFileById( final Serializable fileId, final String locale,
      final Properties properties ) {
    callLogThrow( new Callable<Void>() {
      public Void call() throws Exception {
        delegatee.setLocalePropertiesForFileById( fileId, locale, properties );
        return null;
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.setLocalePropertiesForFile", fileId ) ); //$NON-NLS-1$
  }

  @Override
  public void setLocalePropertiesForFileByPath( final String relPath, final String locale,
                                                final Properties properties ) {
    callLogThrow( new Callable<Void>() {
      public Void call() throws Exception {
        delegatee.setLocalePropertiesForFileByPath( relPath, locale, properties );
        return null;
      }
    }, Messages.getInstance().getString( "ExceptionLoggingDecorator.setLocalePropertiesForFile", relPath ) ); //$NON-NLS-1$
  }

  @Override
  public void setLocalePropertiesForFile( final RepositoryFile repositoryFile, final String locale,
      final Properties properties ) {
    callLogThrow( new Callable<Void>() {
      public Void call() throws Exception {
        delegatee.setLocalePropertiesForFile( repositoryFile, locale, properties );
        return null;
      }
    }, Messages.getInstance()
        .getString( "ExceptionLoggingDecorator.setLocalePropertiesForFile", repositoryFile.getId() ) ); //$NON-NLS-1$
  }

  @Override
  public void deleteLocalePropertiesForFile( final RepositoryFile repositoryFile, final String locale ) {
    callLogThrow( new Callable<Void>() {
      public Void call() throws Exception {
        delegatee.deleteLocalePropertiesForFile( repositoryFile, locale );
        return null;
      }
    }, Messages.getInstance().getString(
        "ExceptionLoggingDecorator.deleteLocalePropertiesForFile", repositoryFile.getId() ) ); //$NON-NLS-1$
  }

  @Override
  public RepositoryFile updateFolder( final RepositoryFile folder, final String versionMessage ) {
    return callLogThrow( new Callable<RepositoryFile>() {
      public RepositoryFile call() throws Exception {
        return delegatee.updateFolder( folder, versionMessage );
      }
    }, Messages.getInstance()
        .getString( "ExceptionLoggingDecorator.updateFile", folder != null ? folder.getId() : null ) ); //$NON-NLS-1$
  }

}
