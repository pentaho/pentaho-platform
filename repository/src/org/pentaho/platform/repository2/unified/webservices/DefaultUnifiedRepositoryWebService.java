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

package org.pentaho.platform.repository2.unified.webservices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.jws.WebService;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.locale.PentahoLocale;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;

/**
 * Implementation of {@link IUnifiedRepositoryWebService} that delegates to an {@link IUnifiedRepository} instance.
 * 
 * @author mlowery
 */
@WebService( endpointInterface = "org.pentaho.platform.repository2.unified.webservices.IUnifiedRepositoryWebService",
    serviceName = "unifiedRepository", portName = "unifiedRepositoryPort",
    targetNamespace = "http://www.pentaho.org/ws/1.0" )
public class DefaultUnifiedRepositoryWebService implements IUnifiedRepositoryWebService {

  protected IUnifiedRepository repo;

  protected RepositoryFileAdapter repositoryFileAdapter = new RepositoryFileAdapter();

  protected NodeRepositoryFileDataAdapter nodeRepositoryFileDataAdapter = new NodeRepositoryFileDataAdapter();

  protected RepositoryFileAclAdapter repositoryFileAclAdapter = new RepositoryFileAclAdapter();

  protected RepositoryFileAclAceAdapter repositoryFileAclAceAdapter = new RepositoryFileAclAceAdapter();

  protected VersionSummaryAdapter versionSummaryAdapter = new VersionSummaryAdapter();

  protected RepositoryFileTreeAdapter repositoryFileTreeAdapter;

  // ~ Constructors
  // ====================================================================================================

  /**
   * No-arg constructor for when in Pentaho BI Server.
   */
  public DefaultUnifiedRepositoryWebService() {
    super();
    // repo = new MockUnifiedRepository();
    repo = PentahoSystem.get( IUnifiedRepository.class );
    if ( repo == null ) {
      throw new IllegalStateException();
    }
  }

  public DefaultUnifiedRepositoryWebService( final IUnifiedRepository repo ) {
    super();
    this.repo = repo;
  }

  @Override
  public List<RepositoryFileDto> getChildrenFromRequest( RepositoryRequest repositoryRequest ) {
    return marshalFiles( repo.getChildren( repositoryRequest ), repositoryRequest );
  }

  @Deprecated
  public List<RepositoryFileDto> getChildren( String folderId ) {
    return getChildrenWithFilter( folderId, null );
  }

  @Deprecated
  public List<RepositoryFileDto> getChildrenWithFilter( String folderId, String filter ) {
    return getChildrenWithFilterAndHidden( folderId, filter, false );
  }

  @Deprecated
  public List<RepositoryFileDto>
    getChildrenWithFilterAndHidden( String folderId, String filter, Boolean showHiddenFiles ) {
    return marshalFiles( repo.getChildren( new RepositoryRequest( folderId, showHiddenFiles, 0, filter ) ) );
  }

  public NodeRepositoryFileDataDto getDataAsNodeForRead( final String fileId ) {
    NodeRepositoryFileData fileData = repo.getDataForRead( fileId, NodeRepositoryFileData.class );
    return fileData != null ? nodeRepositoryFileDataAdapter.marshal( fileData ) : null;
  }

  public List<NodeRepositoryFileDataDto> getDataAsNodeForReadInBatch( final List<RepositoryFileDto> files ) {
    List<NodeRepositoryFileDataDto> data = new ArrayList<NodeRepositoryFileDataDto>( files.size() );
    for ( RepositoryFileDto f : files ) {
      if ( f.getVersionId() == null ) {
        data.add( nodeRepositoryFileDataAdapter
            .marshal( repo.getDataForRead( f.getId(), NodeRepositoryFileData.class ) ) );
      } else {
        data.add( nodeRepositoryFileDataAdapter.marshal( repo.getDataAtVersionForRead( f.getId(), f.getVersionId(),
            NodeRepositoryFileData.class ) ) );
      }
    }
    return data;
  }

  public RepositoryFileDto getFile( String path ) {
    validateEtcReadAccess( path );
    RepositoryFile file = repo.getFile( path );
    return file != null ? repositoryFileAdapter.marshal( file ) : null;
  }

  public RepositoryFileDto getFileById( String fileId ) {
    RepositoryFile file = repo.getFileById( fileId );
    return file != null ? repositoryFileAdapter.marshal( file ) : null;
  }

  @Override
  public RepositoryFileDto getFile( String path, boolean loadLocaleMaps, PentahoLocale locale ) {
    RepositoryFile file = this.repo.getFile( path, loadLocaleMaps, locale );
    return file != null ? this.repositoryFileAdapter.marshal( file ) : null;
  }

  @Override
  public RepositoryFileDto getFileById( String fileId, boolean loadLocaleMaps, PentahoLocale locale ) {
    RepositoryFile file = this.repo.getFileById( fileId, loadLocaleMaps, locale );
    return file != null ? this.repositoryFileAdapter.marshal( file ) : null;
  }

  public RepositoryFileTreeDto getTree( final String path, final int depth, final String filter,
      final boolean showHidden ) {

    RepositoryRequest repositoryRequest = new RepositoryRequest( path, showHidden, depth, filter );
    return getTreeFromRequest( repositoryRequest );
  }

  public RepositoryFileTreeDto getTreeFromRequest( final RepositoryRequest repositoryRequest ) {
    // RepositoryFileTree tree = repo.getTree( path, depth, filter, showHidden );

    RepositoryFileTree tree = repo.getTree( repositoryRequest );

    // Filter system folders from non-admin users.
    // PDI uses this web-service and system folders must be returned to admin repository database connections.
    List<RepositoryFileTree> files = new ArrayList<RepositoryFileTree>();
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    boolean isAdmin = policy.isAllowed( AdministerSecurityAction.NAME );
    for ( RepositoryFileTree file : tree.getChildren() ) {
      Map<String, Serializable> fileMeta = repo.getFileMetadata( file.getFile().getId() );
      boolean isSystemFolder =
          fileMeta.containsKey( IUnifiedRepository.SYSTEM_FOLDER ) ? (Boolean) fileMeta
              .get( IUnifiedRepository.SYSTEM_FOLDER ) : false;
      if ( !isAdmin && isSystemFolder ) {
        continue;
      }
      files.add( file );
    }
    tree = new RepositoryFileTree( tree.getFile(), files );
    if ( tree == null ) {
      return null;
    }

    return new RepositoryFileTreeAdapter( repositoryRequest ).marshal( tree );
  }

  private List<RepositoryFileDto> marshalFiles( List<RepositoryFile> files ) {
    ArrayList<RepositoryFileDto> fileDtos = new ArrayList<RepositoryFileDto>();
    for ( RepositoryFile file : files ) {
      fileDtos.add( repositoryFileAdapter.marshal( file ) );
    }
    return fileDtos;
  }

  private List<RepositoryFileDto> marshalFiles( List<RepositoryFile> files, RepositoryRequest repositoryRequest ) {
    ArrayList<RepositoryFileDto> fileDtos = new ArrayList<RepositoryFileDto>();
    RepositoryFileAdapter filteringRepositoryFileAdapter = new RepositoryFileAdapter( repositoryRequest );
    for ( RepositoryFile file : files ) {
      fileDtos.add( filteringRepositoryFileAdapter.marshal( file ) );
    }
    return fileDtos;
  }

  private List<RepositoryFileAclAceDto> marshalAces( List<RepositoryFileAce> aces ) {
    ArrayList<RepositoryFileAclAceDto> aceDtos = new ArrayList<RepositoryFileAclAceDto>();
    for ( RepositoryFileAce ace : aces ) {
      aceDtos.add( repositoryFileAclAceAdapter.marshal( ace ) );
    }
    return aceDtos;
  }

  public RepositoryFileDto createFolder( String parentFolderId, RepositoryFileDto file, String versionMessage ) {
    RepositoryFile newFile =
        repo.createFolder( parentFolderId, repositoryFileAdapter.unmarshal( file ), versionMessage );
    return newFile != null ? repositoryFileAdapter.marshal( newFile ) : null;
  }

  public RepositoryFileDto createFolderWithAcl( String parentFolderId, RepositoryFileDto file,
      RepositoryFileAclDto acl, String versionMessage ) {
    RepositoryFile newFile =
        repo.createFolder( parentFolderId, repositoryFileAdapter.unmarshal( file ), repositoryFileAclAdapter
            .unmarshal( acl ), versionMessage );
    return newFile != null ? repositoryFileAdapter.marshal( newFile ) : null;
  }

  public void deleteFile( String fileId, String versionMessage ) {
    repo.deleteFile( fileId, versionMessage );
  }

  public void deleteFileAtVersion( String fileId, String versionId ) {
    repo.deleteFileAtVersion( fileId, versionId );
  }

  public void deleteFileWithPermanentFlag( String fileId, boolean permanent, String versionMessage ) {
    repo.deleteFile( fileId, permanent, versionMessage );
  }

  public List<RepositoryFileDto> getDeletedFiles() {
    return marshalFiles( repo.getDeletedFiles() );
  }

  public List<RepositoryFileDto> getDeletedFilesInFolder( String folderPath ) {
    return marshalFiles( repo.getDeletedFiles( folderPath ) );
  }

  public List<RepositoryFileDto> getDeletedFilesInFolderWithFilter( String folderPath, String filter ) {
    return marshalFiles( repo.getDeletedFiles( folderPath, filter ) );
  }

  public void lockFile( String fileId, String message ) {
    repo.lockFile( fileId, message );
  }

  public void moveFile( String fileId, String destAbsPath, String versionMessage ) {
    repo.moveFile( fileId, destAbsPath, versionMessage );
  }

  public void copyFile( String fileId, String destAbsPath, String versionMessage ) {
    repo.copyFile( fileId, destAbsPath, versionMessage );
  }

  public void undeleteFile( String fileId, String versionMessage ) {
    repo.undeleteFile( fileId, versionMessage );
  }

  public void unlockFile( String fileId ) {
    repo.unlockFile( fileId );
  }

  public RepositoryFileDto createFile( String parentFolderId, RepositoryFileDto file, NodeRepositoryFileDataDto data,
      String versionMessage ) {
    validateEtcWriteAccess( parentFolderId );
    return repositoryFileAdapter.marshal( repo.createFile( parentFolderId, repositoryFileAdapter.unmarshal( file ),
        nodeRepositoryFileDataAdapter.unmarshal( data ), versionMessage ) );
  }

  public RepositoryFileDto createFileWithAcl( String parentFolderId, RepositoryFileDto file,
      NodeRepositoryFileDataDto data, RepositoryFileAclDto acl, String versionMessage ) {
    validateEtcWriteAccess( parentFolderId );
    return repositoryFileAdapter.marshal( repo.createFile( parentFolderId, repositoryFileAdapter.unmarshal( file ),
        nodeRepositoryFileDataAdapter.unmarshal( data ), repositoryFileAclAdapter.unmarshal( acl ), versionMessage ) );
  }

  public RepositoryFileDto updateFile( RepositoryFileDto file, NodeRepositoryFileDataDto data, String versionMessage ) {
    return repositoryFileAdapter.marshal( repo.updateFile( repositoryFileAdapter.unmarshal( file ),
        nodeRepositoryFileDataAdapter.unmarshal( data ), versionMessage ) );
  }

  public boolean canUnlockFile( String fileId ) {
    return repo.canUnlockFile( fileId );
  }

  public RepositoryFileAclDto getAcl( String fileId ) {
    if( repo == null ){
      // many tests do not have a repo setup.
      return null;
    }
    return repositoryFileAclAdapter.marshal( repo.getAcl( fileId ) );
  }

  public NodeRepositoryFileDataDto getDataAsNodeForReadAtVersion( String fileId, String versionId ) {
    return nodeRepositoryFileDataAdapter.marshal( repo.getDataAtVersionForRead( fileId, versionId,
        NodeRepositoryFileData.class ) );
  }

  public List<RepositoryFileAclAceDto> getEffectiveAces( String fileId ) {
    return marshalAces( repo.getEffectiveAces( fileId ) );
  }

  public List<RepositoryFileAclAceDto> getEffectiveAcesWithForceFlag( String fileId, boolean forceEntriesInheriting ) {
    return marshalAces( repo.getEffectiveAces( fileId, forceEntriesInheriting ) );
  }

  public RepositoryFileDto getFileAtVersion( String fileId, String versionId ) {
    return repositoryFileAdapter.marshal( repo.getFileAtVersion( fileId, versionId ) );
  }

  public void restoreFileAtVersion( String fileId, String versionId, String versionMessage ) {
    repo.restoreFileAtVersion( fileId, versionId, versionMessage );
  }

  public RepositoryFileAclDto updateAcl( RepositoryFileAclDto acl ) {
    return repositoryFileAclAdapter.marshal( repo.updateAcl( repositoryFileAclAdapter.unmarshal( acl ) ) );
  }

  public List<VersionSummaryDto> getVersionSummaries( String fileId ) {
    return marshalVersionSummaries( repo.getVersionSummaries( fileId ) );
  }

  private List<VersionSummaryDto> marshalVersionSummaries( List<VersionSummary> versionSummaries ) {
    List<VersionSummaryDto> versionSummaryDtos = new ArrayList<VersionSummaryDto>();
    for ( VersionSummary versionSummary : versionSummaries ) {
      versionSummaryDtos.add( versionSummaryAdapter.marshal( versionSummary ) );
    }
    return versionSummaryDtos;
  }

  public VersionSummaryDto getVersionSummary( String fileId, String versionId ) {
    return versionSummaryAdapter.marshal( repo.getVersionSummary( fileId, versionId ) );
  }

  public List<VersionSummaryDto> getVersionSummaryInBatch( final List<RepositoryFileDto> files ) {
    List<VersionSummaryDto> versions = new ArrayList<VersionSummaryDto>( files.size() );
    for ( RepositoryFileDto file : files ) {
      versions.add( versionSummaryAdapter.marshal( repo.getVersionSummary( file.getId(), file.getVersionId() ) ) );
    }
    return versions;
  }

  public boolean hasAccess( String path, List<Integer> permissions ) {
    return repo.hasAccess( path, RepositoryFileAclAceAdapter.toPerms( permissions ) );
  }

  public List<RepositoryFileDto> getReferrers( String fileId ) {
    List<RepositoryFileDto> fileList = new ArrayList<RepositoryFileDto>();

    for ( RepositoryFile file : repo.getReferrers( fileId ) ) {
      fileList.add( repositoryFileAdapter.marshal( file ) );
    }
    return fileList;
  }

  @Override
  public void setFileMetadata( final String fileId, final List<StringKeyStringValueDto> fileMetadataMap ) {
    Map<String, Serializable> metadataMap = new HashMap<String, Serializable>( fileMetadataMap.size() );
    for ( final StringKeyStringValueDto dto : fileMetadataMap ) {
      metadataMap.put( dto.getKey(), dto.getValue() );
    }
    repo.setFileMetadata( fileId, metadataMap );
  }

  @Override
  public List<StringKeyStringValueDto> getFileMetadata( final String fileId ) {
    final Map<String, Serializable> metadataMap = repo.getFileMetadata( fileId );
    final List<StringKeyStringValueDto> fileMetadataMap = new ArrayList<StringKeyStringValueDto>( metadataMap.size() );
    for ( final String key : metadataMap.keySet() ) {
      fileMetadataMap.add( new StringKeyStringValueDto( key, metadataMap.get( key ).toString() ) );
    }
    return fileMetadataMap;
  }

  @Override
  public List<Character> getReservedChars() {
    return repo.getReservedChars();
  }

  protected void validateEtcWriteAccess( String parentFolderId ) {
    RepositoryFile etcFolder = repo.getFile( "/etc" );
    if ( etcFolder != null ) {
      String etcFolderId = etcFolder.getId().toString();
      if ( etcFolderId.equals( parentFolderId ) ) {
        throw new RuntimeException( "This service is not allowed to access the ETC folder in JCR." );
      }
    }
  }

  protected void validateEtcReadAccess( String path ) {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    boolean isAdmin = policy.isAllowed( AdministerSecurityAction.NAME );
    if ( !isAdmin && path.startsWith( "/etc" ) ) {
      throw new RuntimeException( "This user is not allowed to access the ETC folder in JCR." );
    }
  }

  @Override
  public List<PentahoLocale> getAvailableLocalesForFileById( String fileId ) {
    List<PentahoLocale> pentahoLocales = new ArrayList<PentahoLocale>();
    List<Locale> locales = repo.getAvailableLocalesForFileById( fileId );
    if ( locales != null && !locales.isEmpty() ) {
      for ( Locale locale : locales ) {
        pentahoLocales.add( new PentahoLocale( locale ) );
      }
    }
    return pentahoLocales;
  }

  @Override
  public PropertiesWrapper getLocalePropertiesForFileById( String fileId, String locale ) {
    return new PropertiesWrapper( repo.getLocalePropertiesForFileById( fileId, locale ) );
  }

  @Override
  public void setLocalePropertiesForFileByFileId( String fileId, String locale, Properties properties ) {
    repo.setLocalePropertiesForFileById( fileId, locale, properties );
  }

  @Override
  public void deleteLocalePropertiesForFile( String fileId, String locale ) {
    repo.deleteLocalePropertiesForFile( repo.getFileById( fileId ), locale );
  }

  @Override
  public RepositoryFileDto updateFolder( RepositoryFileDto folder, String versionMessage ) {
    // TODO Auto-generated method stub
    return repositoryFileAdapter
        .marshal( repo.updateFolder( repositoryFileAdapter.unmarshal( folder ), versionMessage ) );
  }

}
