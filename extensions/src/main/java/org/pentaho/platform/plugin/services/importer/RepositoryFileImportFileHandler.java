/*!
 *
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
 *
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.ConverterException;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IRepositoryDefaultAclHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileExtraMetaData;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifestFormatException;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.web.http.api.resources.services.FileService;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: nbaker Date: 5/29/12
 */
public class RepositoryFileImportFileHandler implements IPlatformImportHandler {

  private static final Messages messages = Messages.getInstance();

  private IUnifiedRepository repository;

  private SolutionFileImportHelper solutionHelper = new SolutionFileImportHelper();
  private HashMap<String, IMimeType> mimeTypeMap = new HashMap<>();

  private List<String> knownExtensions;

  public RepositoryFileImportFileHandler( List<IMimeType> mimeTypes ) {
    for ( IMimeType mimeType : mimeTypes ) {
      this.mimeTypeMap.put( mimeType.getName(), mimeType );
    }
  }

  IRepositoryDefaultAclHandler defaultAclHandler;

  public Log getLogger() {
    return getImportSession().getLogger();
  }

  public ImportSession getImportSession() {
    return ImportSession.getSession();
  }

  @Override
  public void importFile( IPlatformImportBundle bnd ) throws PlatformImportException {
    if ( bnd instanceof RepositoryFileImportBundle == false ) {
      throw new PlatformImportException( "Error importing bundle. RepositoryFileImportBundle expected" );
    }
    RepositoryFileImportBundle bundle = (RepositoryFileImportBundle) bnd;
    if ( bundle.isSchedulable() == null ) {
      bundle.setSchedulable( RepositoryFile.SCHEDULABLE_BY_DEFAULT );
    }
    String repositoryFilePath = RepositoryFilenameUtils.concat( bundle.getPath(), bundle.getName() );
    getLogger().trace( messages.getString( "RepositoryFileImportFileHandler.ProcessingFile", repositoryFilePath ) );

    // Check if the name is valid
    validateName( bundle );

    // Verify if destination already exists in the repository.
    RepositoryFile file = repository.getFile( repositoryFilePath );
    if ( file != null ) {
      if ( file.isFolder() && getImportSession().getFoldersCreatedImplicitly().contains( repositoryFilePath ) ) {
        getLogger().trace( messages.getString(
          "RepositoryFileImportFileHandler.SkippingImplicitlyCreatedFolder", repositoryFilePath ) );
      } else {
        if ( bundle.overwriteInRepossitory() ) {
          // If file exists, overwrite is true and is not a folder then update it.
          if ( !file.isFolder() ) {
            file = finalAdjustFile( bundle, file );
            copyFileToRepository( bundle, repositoryFilePath, file );
          } else {
            // The folder exists. Possible ACL changes.
            getLogger().trace( messages.getString( "RepositoryFileImportFileHandler.ExistingFolder", repositoryFilePath ) );
            file = finalAdjustFolder( bundle, file.getId() );
            repository.updateFolder( file, null );
            if ( bundle.getAcl() != null ) {
              updateAclFromBundle( false, bundle, file );
            }
            if ( bundle.getExtraMetaData() != null && bundle.getExtraMetaData().getExtraMetaData().size() > 0 ) {
              updateExtraMetaDataFromBundle( false, bundle, file );
            }
          }
        } else {
          if ( getImportSession().getIsNotRunningImport() ) {
            throw new PlatformImportException( messages.getString( "DefaultImportHandler.ERROR_0009_OVERWRITE_CONTENT",
                repositoryFilePath ), PlatformImportException.PUBLISH_CONTENT_EXISTS_ERROR );
          } else {
            getLogger().trace( messages.getString( "RepositoryFileImportFileHandler.SkippingExistingFile",
              repositoryFilePath ) );
            ImportSession importSession = ImportSession.getSession();
            importSession.getSkippedFiles().add( repositoryFilePath );
          }
        }
      }
    } else {
      if ( bundle.isFolder() ) {
        // The file doesn't exist and it is a folder. Create folder.
        getLogger().trace( messages.getString( "RepositoryFileImportFileHandler.CreatingFolder", repositoryFilePath ) );
        final Serializable parentId = getParentId( repositoryFilePath );

        bundle.setFile( bundle.getFile() );
        RepositoryFile repoFile = finalAdjustFolder( bundle, null );
        if ( bundle.getAcl() != null ) {
          repoFile = repository.createFolder( parentId, repoFile, bundle.getAcl(), null );
          updateAclFromBundle( true, bundle, repoFile );
          if ( bundle.getExtraMetaData() != null && bundle.getExtraMetaData().getExtraMetaData().size() > 0 ) {
            updateExtraMetaDataFromBundle( true, bundle, file );
          }
        } else {
          repository.createFolder( parentId, repoFile, null );
        }
      } else {
        // The file doesn't exist. Create file.
        getLogger().trace( messages.getString( "RepositoryFileImportFileHandler.CreatingFile", repositoryFilePath ) );
        copyFileToRepository( bundle, repositoryFilePath, null );
      }
    }
  }

  private void validateName( RepositoryFileImportBundle bundle ) throws PlatformImportException {
    if ( null != bundle ) {
      FileService fileService = new FileService();
      String name = bundle.getName();
      if ( bundle.isFolder() ) {
        if ( !fileService.isValidFolderName( name ) ) {
          throw new PlatformImportException( messages.getString( "DefaultImportHandler.ERROR_0012_INVALID_FOLDER_NAME",
            name ), PlatformImportException.PUBLISH_PROHIBITED_SYMBOLS_ERROR );
        }
      } else {
        if ( !fileService.isValidFileName( name ) ) {
          throw new PlatformImportException( messages.getString( "DefaultImportHandler.ERROR_0011_INVALID_FILE_NAME",
            name ), PlatformImportException.PUBLISH_PROHIBITED_SYMBOLS_ERROR );
        }
      }
    }
  }

  private RepositoryFile finalAdjustFolder( RepositoryFileImportBundle bundle, Serializable id ) {
    RepositoryFile.Builder builder = new RepositoryFile.Builder( bundle.getFile() ).hidden( bundle.isHidden() );
    if ( id != null ) {
      builder.id( id );
    }
    return builder.build();
  }

  private RepositoryFile finalAdjustFile( RepositoryFileImportBundle bundle, RepositoryFile file ) {
    return new RepositoryFile.Builder( file ).hidden( isHiddenBundle( bundle ) ).schedulable( bundle.isSchedulable() )
        .build();
  }

  private boolean isHiddenBundle( RepositoryFileImportBundle bundle ) {
    if ( bundle.isHidden() != null ) {
      return bundle.isHidden();
    }
    if ( solutionHelper.isInHiddenList( bundle.getName() ) ) {
      return true;
    }
    return RepositoryFile.HIDDEN_BY_DEFAULT;
  }

  /**
   * Copies the file bundle into the repository
   * 
   * @param bundle
   * @param repositoryPath
   * @param file
   */
  protected boolean copyFileToRepository( final RepositoryFileImportBundle bundle, final String repositoryPath,
      final RepositoryFile file ) throws PlatformImportException {
    // Compute the file extension
    final String name = bundle.getName();
    final String ext = RepositoryFilenameUtils.getExtension( name );
    if ( StringUtils.isEmpty( ext ) ) {
      getLogger().debug( messages.getString( "RepositoryFileImportFileHandler.SkippingFileWithoutExtension", name ) );
      return false;
    }

    // Check the mime type
    final String mimeType = bundle.getMimeType();
    if ( mimeType == null ) {
      getLogger().debug( messages.getString( "RepositoryFileImportFileHandler.SkippingFileWithoutMimeType", name ) );
      return false;
    }

    // Copy the file into the repository
    try {
      getLogger().trace( messages.getString( "RepositoryFileImportFileHandler.CopyingFile", name ) );

      if ( getMimeTypeMap().get( mimeType ) == null ) {
        getLogger().debug( messages.getString( "RepositoryFileImportFileHandler.SkippingFileWithUnknownMimeType", name, mimeType ) );
      }
      Converter converter = getMimeTypeMap().get( mimeType ).getConverter();
      if ( converter == null ) {
        getLogger().debug( messages.getString( "RepositoryFileImportFileHandler.SkippingFileWithoutConverter", name ) );
        return false;
      }

      RepositoryFile repositoryFile;

      IRepositoryFileData data = null;

      try {
        data = converter.convert( bundle.getInputStream(), bundle.getCharSet(), mimeType );
      } catch ( ConverterException e ) {
        throw e;
      }
      if ( null == file ) {
        repositoryFile = createFile( bundle, repositoryPath, data );
        if ( repositoryFile != null ) {
          updateAclFromBundle( true, bundle, repositoryFile );
          updateExtraMetaDataFromBundleHelper( bundle, repositoryFile, true );
        }
      } else {
        repositoryFile = updateFile( bundle, file, data );
        updateAclFromBundle( false, bundle, repositoryFile );
        updateExtraMetaDataFromBundleHelper( bundle, repositoryFile, false );
      }

      converter.convertPostRepoSave( repositoryFile );

      if ( repositoryFile != null ) {
        getImportSession().addImportedRepositoryFile( repositoryFile );
      }

      return true;
    } catch ( IOException e ) {
      getLogger().warn( messages.getString( "DefaultImportHandler.WARN_0003_IOEXCEPTION", name ), e ); // TODO make sure
      // string exists
      return false;
    }
  }

  private void updateExtraMetaDataFromBundleHelper( RepositoryFileImportBundle bundle,
                                                    RepositoryFile file, boolean isNew ) {
    if ( bundle.getExtraMetaData() != null && bundle.getExtraMetaData().getExtraMetaData().size() > 0 ) {
      updateExtraMetaDataFromBundle( isNew, bundle, file );
    }
  }

  /**
   * Create a formal <code>RepositoryFileAcl</code> object for import.
   * 
   * @param newFile
   *          Whether the file is being newly created or was pre-existing
   * @param bundle
   *          The RepositoryImportBundle (which contains the effective manifest Acl)
   * @param repositoryFile
   *          The <code>RepositoryFile</code> of the target file
   */
  private void updateAclFromBundle( boolean newFile, RepositoryFileImportBundle bundle, RepositoryFile repositoryFile ) {
    updateAcl( newFile, repositoryFile, bundle.getAcl() );
  }

  /**
   * Create a formal <code>RepositoryFileAcl</code> object for import.
   * 
   * @param newFile
   *          Whether the file is being newly created or was pre-existing
   * @param repositoryFileAcl
   *          The effect Acl as defined in the manifest)
   * @param repositoryFile
   *          The <code>RepositoryFile</code> of the target file
   */
  private void updateAcl( boolean newFile, RepositoryFile repositoryFile, RepositoryFileAcl repositoryFileAcl ) {
    if ( newFile ) {
      getLogger().debug( messages.getString( "RepositoryFileImportFileHandler.FileIsNew" ) );
    } else {
      getLogger().debug( messages.getString( "RepositoryFileImportFileHandler.FileExists" ) );
    }
    if ( repositoryFileAcl != null
        && ( getImportSession().isApplyAclSettings() || !getImportSession().isRetainOwnership() ) ) {
      RepositoryFileAcl manifestAcl = repositoryFileAcl;
      RepositoryFileAcl originalAcl = repository.getAcl( repositoryFile.getId() );

      // Determine who will own this file
      RepositoryFileSid newOwner;
      if ( getImportSession().isRetainOwnership() ) {
        if ( newFile ) {
          getLogger().debug( messages.getString( "RepositoryFileImportFileHandler.OwnerFromSession" ) );
          newOwner = getDefaultAcl( repositoryFile ).getOwner();
        } else {
          getLogger().debug( messages.getString( "RepositoryFileImportFileHandler.OwnerFromExistingFile" ) );
          newOwner = originalAcl.getOwner();
        }
      } else {
        getLogger().debug( messages.getString( "RepositoryFileImportFileHandler.OwnerFromManifest" ) );
        newOwner = manifestAcl.getOwner();
      }

      // Determine the Aces we will use for this file
      RepositoryFileAcl useAclForPermissions; // The ACL we will use the permissions from
      if ( getImportSession().isApplyAclSettings() && ( getImportSession().isOverwriteAclSettings() || newFile ) ) {
        getLogger().debug( messages.getString( "RepositoryFileImportFileHandler.PermissionsFromManifest" ) );
        useAclForPermissions = manifestAcl;
      } else {
        if ( newFile ) {
          getLogger().debug( messages.getString( "RepositoryFileImportFileHandler.PermissionsFromSettings" ) );
          useAclForPermissions = getDefaultAcl( repositoryFile );
        } else {
          getLogger().debug( messages.getString( "RepositoryFileImportFileHandler.PermissionsFromExistingFile" ) );
          useAclForPermissions = originalAcl;
        }
      }

      // Make the new Acl if it has changed from the orignal
      if ( !newOwner.equals( originalAcl.getOwner() ) || !useAclForPermissions.equals( originalAcl ) ) {
        RepositoryFileAcl updatedAcl =
            new RepositoryFileAcl( repositoryFile.getId(), newOwner, useAclForPermissions.isEntriesInheriting(),
                useAclForPermissions.getAces() );
        repository.updateAcl( updatedAcl );
      }
    }
  }

  private RepositoryFileAcl getDefaultAcl( RepositoryFile repositoryFile ) {
    // ToDo: call default Acl creator when implemented. For now just return
    // whatever is stored
    // return repository.getAcl(repositoryFile.getId());
    return defaultAclHandler.createDefaultAcl( repositoryFile.clone() );
  }

  /**
   * Create a formal <code>RepositoryFileExtraMetaData</code> object for import.
   *
   * @param newFile
   *          Whether the file is being newly created or was pre-existing
   * @param bundle
   *          The RepositoryImportBundle (which contains the effective manifest extraMetaData)
   * @param repositoryFile
   *          The <code>RepositoryFile</code> of the target file
   */
  private void updateExtraMetaDataFromBundle( boolean newFile, RepositoryFileImportBundle bundle, RepositoryFile repositoryFile ) {
    updateExtraMetaData( newFile, repositoryFile, bundle.getExtraMetaData() );
  }

  /**
   * Create a formal <code>RepositoryFileExtraMetaData</code> object for import.
   *
   * @param newFile
   *          Whether the file is being newly created or was pre-existing
   * @param repositoryFileExtraMetaData
   *          The effect extraMetaData as defined in the manifest
   * @param repositoryFile
   *          The <code>RepositoryFile</code> of the target file
   */
  private void updateExtraMetaData( boolean newFile, RepositoryFile repositoryFile, RepositoryFileExtraMetaData repositoryFileExtraMetaData ) {
    if ( repositoryFileExtraMetaData != null && !repositoryFileExtraMetaData.getExtraMetaData().isEmpty() ) {
      if ( newFile ) {
        getLogger().debug( messages.getString( "RepositoryFileImportFileHandler.ExtraMetaDataToNewFile" ) );
      } else {
        getLogger().debug( messages.getString( "RepositoryFileImportFileHandler.ExtraMetaDataToExistingFile" ) );
      }
      RepositoryFileExtraMetaData manifestExtraMetaData = repositoryFileExtraMetaData;
      repository.setFileMetadata( repositoryFile.getId(), manifestExtraMetaData.getExtraMetaData() );
    }
  }

  /**
   * Creates a new file in the repository
   * 
   * @param bundle
   * @param data
   */
  protected RepositoryFile createFile( final RepositoryFileImportBundle bundle, final String repositoryPath,
      final IRepositoryFileData data ) throws PlatformImportException {
    if ( solutionHelper.isInApprovedExtensionList( repositoryPath ) ) {
      final RepositoryFile file =
          new RepositoryFile.Builder( bundle.getName() ).hidden( isHiddenBundle( bundle ) ).schedulable( bundle
              .isSchedulable() ).title(
              RepositoryFile.DEFAULT_LOCALE,
              getTitle( bundle.getTitle() != null ? bundle.getTitle() : bundle.getName() ) ).versioned( true ).build();
      final Serializable parentId = checkAndCreatePath( repositoryPath, getImportSession().getCurrentManifestKey() );

      final RepositoryFileAcl acl = bundle.getAcl();
      if ( null == acl ) {
        return repository.createFile( parentId, file, data, bundle.getComment() );
      } else {
        return repository.createFile( parentId, file, data, acl, bundle.getComment() );
      }
    } else {
      getLogger().trace( messages.getString( "RepositoryFileImportFileHandler.ExtensionNotApproved", repositoryPath ) );
      return null;
    }
  }

  /**
   * Updates a file in the repository
   * 
   */
  protected RepositoryFile updateFile( final RepositoryFileImportBundle bundle, final RepositoryFile file,
      final IRepositoryFileData data ) throws PlatformImportException {
    return repository.updateFile( file, data, bundle.getComment() );
  }

  /**
   * Check path for existance. If path does not exist create folders as necessary to satisfy the path. When done return
   * the Id of the path received.
   * 
   * @param repositoryPath
   * @return
   */
  private Serializable checkAndCreatePath( String repositoryPath, String manifestKey ) throws PlatformImportException {
    if ( getParentId( repositoryPath ) == null ) {
      String parentPath = RepositoryFilenameUtils.getFullPathNoEndSeparator( repositoryPath );
      String parentManifestKey = RepositoryFilenameUtils.getFullPathNoEndSeparator( manifestKey );
      if ( !getImportSession().getFoldersCreatedImplicitly().contains( parentPath ) ) {
        RepositoryFile parentFile = repository.getFile( parentPath );
        if ( parentFile == null ) {
          checkAndCreatePath( parentPath, parentManifestKey );
          try {
            parentFile = createFolderJustInTime( parentPath, parentManifestKey );
          } catch ( Exception e ) {
            throw new PlatformImportException( messages.getString(
                "DefaultImportHandler.ERROR_0010_JUST_IN_TIME_FOLDER_CREATION", repositoryPath ) );
          }
        }
        Serializable parentFileId = parentFile.getId();
        Assert.notNull( parentFileId );
      }
    }
    return getParentId( repositoryPath );
  }

  /**
   * truncate the extension from the file name for the extension only if it is file with known extension
   * 
   * @param name
   * @return title
   */
  protected String getTitle( String name ) {
    if ( !StringUtils.isEmpty( name ) ) {
      int dotIndex = name.lastIndexOf( '.' );
      if ( dotIndex != -1 ) {
        String extension = name.substring( dotIndex + 1 );
        if ( knownExtensions != null && knownExtensions.contains( extension ) ) {
          return name.substring( 0, dotIndex );
        }
      }
    }
    return name;
  }

  /**
   * Returns the Id of the parent folder of the file path provided
   * 
   * @param repositoryPath
   * @return
   */
  protected Serializable getParentId( final String repositoryPath ) {
    Assert.notNull( repositoryPath );
    final String parentPath = RepositoryFilenameUtils.getFullPathNoEndSeparator( repositoryPath );
    final RepositoryFile parentFile = repository.getFile( parentPath );
    if ( parentFile == null ) {
      return null;
    }
    Serializable parentFileId = parentFile.getId();
    Assert.notNull( parentFileId );
    return parentFileId;
  }

  public IUnifiedRepository getRepository() {
    return repository;
  }

  public void setRepository( IUnifiedRepository repository ) {
    this.repository = repository;
  }

  public void setDefaultAclHandler( IRepositoryDefaultAclHandler defaultAclHandler ) {
    this.defaultAclHandler = defaultAclHandler;
  }

  public RepositoryFile createFolderJustInTime( String folderPath, String manifestKey ) throws PlatformImportException,
    DomainIdNullException, DomainAlreadyExistsException, DomainStorageException, IOException {
    // The file doesn't exist and it is a folder. Create folder.
    getLogger().trace( messages.getString( "RepositoryFileImportFileHandler.CreatingImpliedFolder", folderPath ) );
    final Serializable parentId = getParentId( folderPath );
    Assert.notNull( parentId );
    boolean isHidden;
    if ( getImportSession().isFileHidden( manifestKey ) == null ) {
      isHidden = false;
    } else {
      isHidden = getImportSession().isFileHidden( manifestKey );
    }
    RepositoryFile.Builder builder =
        new RepositoryFile.Builder( RepositoryFilenameUtils.getName( folderPath ) ).path(
            RepositoryFilenameUtils.getPath( folderPath ) ).folder( true ).hidden( isHidden );
    RepositoryFile repoFile = builder.build();
    RepositoryFileAcl repoAcl = getImportSession().processAclForFile( manifestKey );
    if ( repoAcl != null ) {
      repoFile = repository.createFolder( parentId, repoFile, repoAcl, null );
      RepositoryFileAcl repositoryFileAcl = null;
      try {
        repositoryFileAcl =
            getImportSession().getManifest().getExportManifestEntity( manifestKey ).getRepositoryFileAcl();
      } catch ( NullPointerException e ) {
        // If npe then manifest entry is not defined which is likely so just ignore
      } catch ( ExportManifestFormatException e ) {
        // Same goes here
      }
      updateAcl( true, repoFile, repositoryFileAcl );
    } else {
      repoFile = repository.createFolder( parentId, repoFile, null );
    }
    getImportSession().getFoldersCreatedImplicitly().add( folderPath );
    return repoFile;
  }

  @Override
  public List<IMimeType> getMimeTypes() {
    return new ArrayList<IMimeType>( mimeTypeMap.values() );
  }

  public Map<String, IMimeType> getMimeTypeMap() {
    return mimeTypeMap;
  }

  public void setKnownExtensions( List<String> knownExtensions ) {
    this.knownExtensions = knownExtensions;
  }

  public List<String> getKnownExtensions() {
    return knownExtensions;
  }
}
