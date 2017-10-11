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
 * Copyright 2006 - 2017 Hitachi Vantara.  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAdapter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.web.http.api.resources.services.FileService;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;
import org.pentaho.platform.web.http.api.resources.utils.RepositoryFileHelper;
import org.pentaho.platform.web.http.messages.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CopyFilesOperation {

  private RepositoryFile destDir;
  private List<String> sourceFileIds;
  private String path;
  private int mode;

  private IUnifiedRepository repository;
  private DefaultUnifiedRepositoryWebService defaultUnifiedRepositoryWebService;

  private static final Log logger = LogFactory.getLog( FileService.class );
  public static final Integer DEFAULT_DEEPNESS = 10;

  public CopyFilesOperation( List<String> sourceFileIds, String destDirPath, int overrideMode ) {
    this( PentahoSystem.get( IUnifiedRepository.class ), new DefaultUnifiedRepositoryWebService(), sourceFileIds,
      destDirPath, overrideMode );
  }

  public CopyFilesOperation( IUnifiedRepository repository,
                             DefaultUnifiedRepositoryWebService defaultUnifiedRepositoryWebService,
                             List<String> sourceFileIds, String destDirPath, int overrideMode ) {

    if ( repository == null ) {
      throw new IllegalArgumentException( "repository cannot be null" );
    }
    this.repository = repository;

    if ( defaultUnifiedRepositoryWebService == null ) {
      throw new IllegalArgumentException( "defaultUnifiedRepositoryWebService  cannot be null" );
    }
    this.defaultUnifiedRepositoryWebService = defaultUnifiedRepositoryWebService;

    if ( sourceFileIds == null ) {
      throw new IllegalArgumentException( "sourceFileIds cannot be null" );
    }

    if ( sourceFileIds.isEmpty() ) {
      throw new IllegalArgumentException( "Nothing to copy, list of files shouldn't be empty" );
    }

    this.sourceFileIds = sourceFileIds;

    if ( destDirPath == null ) {
      throw new IllegalArgumentException( "destDirPath cannot be null" );
    }

    destDir = getRepository().getFile( destDirPath );

    if ( destDir == null ) {
      throw new IllegalArgumentException( "Directory with destPath: " + destDirPath + " doesn't exist" );
    }

    if ( !destDir.isFolder() ) {
      throw new IllegalArgumentException( destDirPath + " should be a folder" );
    }


    this.mode = overrideMode;
    this.path = destDirPath;

  }

  public void execute() {
    for ( String sourceFileId : getSourceFileIds() ) {
      RepositoryFile sourceFile = getRepository().getFileById( sourceFileId );

      if ( sourceFile == null ) {
        logger.warn( "File with id: " + sourceFileId + " is not found" );
        continue;
      }

      if ( mode == FileService.MODE_OVERWRITE ) {
        copyOverrideMode( sourceFile );
      } else if ( mode == FileService.MODE_NO_OVERWRITE ) {
        copyNoOverrideMode( sourceFile );
      } else {
        copyRenameMode( sourceFile );
      }
    }
  }

  private void copyOverrideMode( RepositoryFile file ) {
    if ( sourceAndDestDirAreSame( file.getPath() ) ) {
      return;
    }

    RepositoryFileAcl acl = getRepository().getAcl( file.getId() );

    RepositoryFile destFile = getRepository()
      .getFile( destDir.getPath() + FileUtils.PATH_SEPARATOR + file.getName() );

    if ( destFile == null ) {
      // destFile doesn't exist so we'll create it.
      RepositoryFile duplicateFile =
        new RepositoryFile.Builder( file.getName() ).hidden( file.isHidden() ).versioned(
          file.isVersioned() ).build();
      final RepositoryFile repositoryFile =
        getRepository()
          .createFile( destDir.getId(), duplicateFile, RepositoryFileHelper.getFileData( file ), acl,
            null );
      getRepository()
        .setFileMetadata( repositoryFile.getId(), getRepository().getFileMetadata( file.getId() ) );

      return;
    }

    RepositoryFileDto destFileDto = toFileDto( destFile, null, false );
    destFileDto.setHidden( file.isHidden() );
    destFile = toFile( destFileDto );

    final RepositoryFile repositoryFile = getRepository().updateFile( destFile, RepositoryFileHelper.getFileData( file ), null );
    getRepository().updateAcl( acl );
    getRepository()
      .setFileMetadata( repositoryFile.getId(), getRepository().getFileMetadata( file.getId() ) );
  }

  private void copyNoOverrideMode( RepositoryFile repoFile ) {
    if ( sourceAndDestDirAreSame( repoFile.getPath() ) ) {
      return;
    }

    RepositoryFile destFile =
      getRepository().getFile( destDir.getPath() + FileUtils.PATH_SEPARATOR + repoFile.getName() );

    if ( destFile != null ) {
      // file exists already
      return;
    }

    final RepositoryFile repositoryFile;
    final RepositoryFile duplicateFile;

    if ( repoFile.isFolder() ) {
      duplicateFile = repoFile.clone();
      repositoryFile = getRepository()
        .createFolder( destDir.getId(), duplicateFile, getRepository().getAcl( repoFile.getId() ), null );

      performFolderDeepCopy( repoFile, repositoryFile, DEFAULT_DEEPNESS );
    } else {
      duplicateFile = new RepositoryFile.
        Builder( repoFile.getName() )
        .hidden( repoFile.isHidden() )
        .folder( repoFile.isFolder() )
        .versioned( repoFile.isVersioned() )
        .build();

      repositoryFile = getRepository()
        .createFile( destDir.getId(), duplicateFile, RepositoryFileHelper.getFileData( repoFile ),
          getRepository().getAcl( repoFile.getId() ),
          null );
    }

    getRepository()
      .setFileMetadata( repositoryFile.getId(), getRepository().getFileMetadata( repoFile.getId() ) );
  }

  private void copyRenameMode( RepositoryFile repoFile ) {
    // First try to see if regular name is available
    String repoFileName = repoFile.getName();
    String copyText = "";
    String rootCopyText = "";
    String nameNoExtension = repoFileName;
    String extension = "";
    int indexOfDot = repoFileName.lastIndexOf( '.' );
    if ( !( indexOfDot == -1 ) ) {
      nameNoExtension = repoFileName.substring( 0, indexOfDot );
      extension = repoFileName.substring( indexOfDot );
    }

    RepositoryFileDto
      testFile =
      getRepoWs().getFile( path + FileUtils.PATH_SEPARATOR + nameNoExtension + extension ); //$NON-NLS-1$
    if ( testFile != null ) {
      // Second try COPY_PREFIX, If the name already ends with a COPY_PREFIX don't append twice
      if ( !nameNoExtension
        .endsWith( Messages.getInstance().getString( "FileResource.COPY_PREFIX" ) ) ) { //$NON-NLS-1$
        copyText = rootCopyText = Messages.getInstance().getString( "FileResource.COPY_PREFIX" );
        repoFileName = nameNoExtension + copyText + extension;
        testFile = getRepoWs().getFile( path + FileUtils.PATH_SEPARATOR + repoFileName );
      }
    }

    // Third try COPY_PREFIX + DUPLICATE_INDICATOR
    Integer nameCount = 1;
    while ( testFile != null ) {
      nameCount++;
      copyText =
        rootCopyText + Messages.getInstance().getString( "FileResource.DUPLICATE_INDICATOR", nameCount );
      repoFileName = nameNoExtension + copyText + extension;
      testFile = getRepoWs().getFile( path + FileUtils.PATH_SEPARATOR + repoFileName );
    }
    IRepositoryFileData data = RepositoryFileHelper.getFileData( repoFile );
    RepositoryFileAcl acl = getRepository().getAcl( repoFile.getId() );
    RepositoryFile duplicateFile = null;
    final RepositoryFile repositoryFile;

    if ( repoFile.isFolder() ) {
      // If the title is different than the source file, copy it separately
      if ( !repoFile.getName().equals( repoFile.getTitle() ) ) {
        duplicateFile =
          new RepositoryFile.Builder( repoFileName ).title( RepositoryFile.DEFAULT_LOCALE,
            repoFile.getTitle() + copyText ).hidden( repoFile.isHidden() ).versioned(
            repoFile.isVersioned() ).folder( true ).build();
      } else {
        duplicateFile = new RepositoryFile.Builder( repoFileName ).hidden( repoFile.isHidden() ).folder( true ).build();
      }
      repositoryFile = getRepository()
        .createFolder( destDir.getId(), duplicateFile, acl, null );

      performFolderDeepCopy( repoFile, repositoryFile, DEFAULT_DEEPNESS );
    } else {
      // If the title is different than the source file, copy it separately
      if ( !repoFile.getName().equals( repoFile.getTitle() ) ) {
        duplicateFile =
          new RepositoryFile.Builder( repoFileName ).title( RepositoryFile.DEFAULT_LOCALE,
            repoFile.getTitle() + copyText ).hidden( repoFile.isHidden() ).versioned(
            repoFile.isVersioned() ).build();
      } else {
        duplicateFile = new RepositoryFile.Builder( repoFileName ).hidden( repoFile.isHidden() ).build();
      }

      repositoryFile =
        getRepository().createFile( destDir.getId(), duplicateFile, data, acl, null );
    }
    if ( repositoryFile == null ) {
      throw new UnifiedRepositoryAccessDeniedException( Messages.getInstance().getString(
        "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED_CREATE", destDir.getId() ) );
    }

    getRepository().setFileMetadata( repositoryFile.getId(), getRepository().getFileMetadata( repoFile.getId() ) );
  }

  private boolean sourceAndDestDirAreSame( String path ) {
    String pathWithoutFileName =
      path.substring( 0, path.lastIndexOf( FileUtils.PATH_SEPARATOR ) );

    return pathWithoutFileName.equals( destDir.getPath() );
  }


  /**
   * @param from     folder, from witch we will copy content
   * @param to       folder, in witch we will copy content
   * @param deepness deepness of child entries in each folder
   */
  protected void performFolderDeepCopy( RepositoryFile from, RepositoryFile to, Integer deepness ) {
    if ( from == null || to == null ) {
      throw new IllegalArgumentException( "Folder should not be null" );
    }

    if ( !from.isFolder() ) {
      throw new IllegalArgumentException( from.getPath() + " is not a folder" );
    }

    if ( !to.isFolder() ) {
      throw new IllegalArgumentException( to.getPath() + " is not a folder" );
    }

    if ( deepness == null || deepness < 0 ) {
      deepness = DEFAULT_DEEPNESS;
    }

    List<RepositoryFile> children =
      getRepository().getChildren( createRepoRequest( from, deepness ) );

    for ( RepositoryFile repoFile : children ) {
      if ( repoFile.isFolder() ) {
        RepositoryFile childFolder =
          getRepository().createFolder( to.getId(), repoFile, getRepository().getAcl( repoFile.getId() ), null );
        performFolderDeepCopy( repoFile, childFolder, deepness );
      } else {
        getRepository().createFile( to.getId(), repoFile, RepositoryFileHelper.getFileData( repoFile ), null );
      }
    }
  }

  protected IUnifiedRepository getRepository() {
    if ( repository == null ) {
      repository = PentahoSystem.get( IUnifiedRepository.class );
    }
    return repository;
  }

  protected DefaultUnifiedRepositoryWebService getRepoWs() {
    if ( defaultUnifiedRepositoryWebService == null ) {
      defaultUnifiedRepositoryWebService = new DefaultUnifiedRepositoryWebService();
    }
    return defaultUnifiedRepositoryWebService;
  }

  public List<String> getSourceFileIds() {
    return new ArrayList<>( sourceFileIds );
  }

  /**
   * For testing
   */
  protected RepositoryRequest createRepoRequest( RepositoryFile repoFile, int deepness ) {
    return new RepositoryRequest( String.valueOf( repoFile.getId() ), true, deepness, null );
  }

  /**
   * For testing
   */
  protected RepositoryFileDto toFileDto( RepositoryFile repositoryFile, Set<String> memberSet, boolean exclude ) {
    return RepositoryFileAdapter.toFileDto( repositoryFile, memberSet, exclude );
  }

  /**
   * For testing
   */
  protected RepositoryFile toFile( RepositoryFileDto repositoryFileDto ) {
    return RepositoryFileAdapter.toFile( repositoryFileDto );
  }
}
