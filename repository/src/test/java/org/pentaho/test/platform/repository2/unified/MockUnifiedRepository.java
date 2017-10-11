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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.test.platform.repository2.unified;

import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.READ;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.WRITE;
import static org.pentaho.platform.api.repository2.unified.RepositoryFileSid.Type.ROLE;
import static org.pentaho.platform.api.repository2.unified.RepositoryFileSid.Type.USER;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode.DataPropertyType;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.security.userroledao.DefaultTenantedPrincipleNameResolver;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Mock implementation of the {@link IUnifiedRepository} for unit testing.
 * 
 * @author dkincade
 * @author mlowery
 */
@SuppressWarnings( "nls" )
public class MockUnifiedRepository implements IUnifiedRepository {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private IdManager idManager = new IdManager();

  private VersionManager versionManager = new VersionManager();

  private DeleteManager deleteManager = new DeleteManager();

  private LockManager lockManager = new LockManager();

  private ReferralManager referralManager = new ReferralManager();

  private ICurrentUserProvider currentUserProvider = new SpringSecurityCurrentUserProvider();

  private FileRecord root;

  private static ITenantedPrincipleNameResolver userNameUtils = new DefaultTenantedPrincipleNameResolver();

  // ~ Constructors
  // ====================================================================================================

  /**
   * Creates a mock.
   * 
   * @param currentUserProvider
   *          create your own or use {@link SpringSecurityCurrentUserProvider}.
   */
  public MockUnifiedRepository( final ICurrentUserProvider currentUserProvider ) {
    super();
    init();
    this.currentUserProvider = currentUserProvider;
  }

  // ~ Methods
  // =========================================================================================================

  public static RepositoryFileSid everyone() {
    return new RepositoryFileSid( userNameUtils.getPrincipleId( null, "__everyone__" ), ROLE );
  }

  public static RepositoryFileSid root() {
    return new RepositoryFileSid( userNameUtils.getPrincipleId( null, "__root__" ), USER );
  }

  protected void init() {
    RepositoryFile rootFolder =
        new RepositoryFile.Builder( "" ).path( RepositoryFile.SEPARATOR ).folder( true ).build();

    RepositoryFileAcl rootFolderAcl =
        new RepositoryFileAcl.Builder( root() ).entriesInheriting( false ).ace( everyone(), READ ).build();

    root = new FileRecord( rootFolder, rootFolderAcl );
    idManager.register( root );

    RepositoryFile publicFolder =
        new RepositoryFile.Builder( "public" ).path( RepositoryFile.SEPARATOR + "public" ).folder( true ).build();

    RepositoryFileAcl publicFolderAcl =
        new RepositoryFileAcl.Builder( root() ).entriesInheriting( false ).ace( everyone(), READ, WRITE ).build();

    FileRecord pub = new FileRecord( publicFolder, publicFolderAcl );
    root.addChild( pub );
    idManager.register( pub );

    RepositoryFile etcFolder =
        new RepositoryFile.Builder( "etc" ).path( RepositoryFile.SEPARATOR + "etc" ).folder( true ).build();

    RepositoryFileAcl etcFolderAcl = new RepositoryFileAcl.Builder( root() ).entriesInheriting( true ).build();

    FileRecord etc = new FileRecord( etcFolder, etcFolderAcl );
    root.addChild( etc );
    idManager.register( etc );

  }

  @Override
  public RepositoryFile getFile( final String path ) {
    FileRecord r = root.getFileRecord( path );
    if ( r != null ) {
      if ( !hasAccess( r.getFile().getId(), EnumSet.of( READ ) ) ) {
        return null;
      }
      return r.getFile();
    }
    return null;
  }

  @Override
  public RepositoryFileTree getTree( RepositoryRequest repositoryRequest ) {
    return getTree( repositoryRequest.getPath(), repositoryRequest.getDepth(), repositoryRequest.getChildNodeFilter(),
        repositoryRequest.isShowHidden() );
  }

  @Override
  public RepositoryFileTree getTree( final String path, final int depth, final String filter,
                                     final boolean showHidden ) {
    FileRecord r = root.getFileRecord( path );
    RepositoryFile rootFile = r.getFile();
    if ( ( !showHidden && rootFile.isHidden() ) || rootFile.isAclNode() ) {
      return null;
    }
    List<RepositoryFileTree> children;
    if ( depth != 0 ) {
      children = new ArrayList<RepositoryFileTree>();
      if ( rootFile.isFolder() ) {
        List<RepositoryFile> childrenTmp = getChildren( rootFile.getId(), filter );
        for ( RepositoryFile child : childrenTmp ) {
          RepositoryFileTree repositoryFileTree = getTree( child.getPath(), depth - 1, filter, showHidden );
          if ( repositoryFileTree != null ) {
            children.add( repositoryFileTree );
          }
        }
      }
      Collections.sort( children );
    } else {
      children = null;
    }
    return new RepositoryFileTree( rootFile, children );
  }

  @Override
  public RepositoryFile getFileAtVersion( final Serializable fileId, final Serializable versionId ) {
    return versionManager.getFileAtVersion( fileId, versionId ).getFile();
  }

  @Override
  public RepositoryFile getFileById( final Serializable fileId ) {
    if ( idManager.hasId( fileId ) ) {
      if ( !hasAccess( fileId, EnumSet.of( READ ) ) ) {
        return null;
      }
      return idManager.getFileById( fileId ).getFile();
    }

    return null;
  }

  @Override
  public RepositoryFile getFile( final String path, final boolean loadLocaleMaps ) {
    return getFile( path );
  }

  @Override
  public RepositoryFile getFileById( final Serializable fileId, final boolean loadLocaleMaps ) {
    return getFileById( fileId );
  }

  @Override
  public RepositoryFile getFile( String path, IPentahoLocale locale ) {
    return getFile( path );
  }

  @Override
  public RepositoryFile getFileById( Serializable fileId, IPentahoLocale locale ) {
    return getFileById( fileId );
  }

  @Override
  public RepositoryFile getFile( String path, boolean loadLocaleMaps, IPentahoLocale locale ) {
    return getFile( path );
  }

  @Override
  public RepositoryFile getFileById( Serializable fileId, boolean loadLocaleMaps, IPentahoLocale locale ) {
    return getFileById( fileId );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T extends IRepositoryFileData> T getDataForRead( final Serializable fileId, final Class<T> dataClass ) {
    FileRecord r = idManager.getFileById( fileId );
    return (T) r.getData();
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T extends IRepositoryFileData> T getDataAtVersionForRead( final Serializable fileId,
      final Serializable versionId, final Class<T> dataClass ) {
    if ( versionId == null ) {
      return (T) versionManager.getLatestVersion( fileId ).getData();
    }

    return (T) versionManager.getFileAtVersion( fileId, versionId ).getData();
  }

  @Override
  public <T extends IRepositoryFileData> T getDataForExecute( final Serializable fileId, final Class<T> dataClass ) {
    return getDataForRead( fileId, dataClass );
  }

  @Override
  public <T extends IRepositoryFileData> T getDataAtVersionForExecute( final Serializable fileId,
      final Serializable versionId, final Class<T> dataClass ) {
    return getDataAtVersionForRead( fileId, versionId, dataClass );
  }

  @Override
  public <T extends IRepositoryFileData> List<T> getDataForReadInBatch( final List<RepositoryFile> files,
      final Class<T> dataClass ) {
    List<T> datas = new ArrayList<T>();
    for ( RepositoryFile file : files ) {
      if ( file.getVersionId() != null ) {
        datas.add( getDataAtVersionForRead( file.getId(), file.getVersionId(), dataClass ) );
      } else {
        datas.add( getDataForRead( file.getId(), dataClass ) );
      }
    }
    return datas;
  }

  @Override
  public <T extends IRepositoryFileData> List<T> getDataForExecuteInBatch( final List<RepositoryFile> files,
      final Class<T> dataClass ) {
    List<T> datas = new ArrayList<T>();
    for ( RepositoryFile file : files ) {
      if ( file.getVersionId() != null ) {
        datas.add( getDataAtVersionForExecute( file.getId(), file.getVersionId(), dataClass ) );
      } else {
        datas.add( getDataForExecute( file.getId(), dataClass ) );
      }
    }
    return datas;
  }

  @Override
  public RepositoryFile createFile( final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final String versionMessage ) {
    return createFile( parentFolderId, file, data, createDefaultAcl(), versionMessage );
  }

  private RepositoryFileAcl createDefaultAcl() {
    RepositoryFileAcl.Builder builder =
        new RepositoryFileAcl.Builder( userNameUtils.getPrincipleId( new Tenant( "/pentaho", true ),
            currentUserProvider.getUser() ) );
    builder.entriesInheriting( true );
    return builder.build();
  }

  @Override
  public RepositoryFile createFile( final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final RepositoryFileAcl acl, final String versionMessage ) {
    Validate.isTrue( !file.isFolder() );
    if ( !hasAccess( parentFolderId, EnumSet.of( WRITE ) ) ) {
      throw new AccessDeniedException( "access denied" );
    }
    FileRecord parentFolder = idManager.getFileById( parentFolderId );
    RepositoryFile fileFromRepo =
        new RepositoryFile.Builder( file ).path( parentFolder.getPath() + RepositoryFile.SEPARATOR + file.getName() )
            .title( findTitle( file ) ).description( findDesc( file ) ).build();
    RepositoryFileAcl aclFromRepo = new RepositoryFileAcl.Builder( acl ).build();
    FileRecord fileRecord = new FileRecord( fileFromRepo, data, aclFromRepo, new HashMap<String, Serializable>() );
    idManager.register( fileRecord );

    process( fileRecord, null );

    parentFolder.addChild( fileRecord );
    if ( file.isVersioned() ) {
      versionManager.createVersion( fileRecord.getFile().getId(), currentUserProvider.getUser(), versionMessage,
          new Date() );
    }
    return fileRecord.getFile();
  }

  private void process( final FileRecord r, final IRepositoryFileData oldData ) {
    IRepositoryFileData data = r.getData();
    if ( data instanceof SimpleRepositoryFileData ) {
      r.setData( new ReusableSimpleRepositoryFileData( (SimpleRepositoryFileData) data ) );
    } else if ( data instanceof NodeRepositoryFileData ) {
      DataNode node = ( (NodeRepositoryFileData) data ).getNode();
      referralManager.process( r.getFile().getId(), oldData != null ? ( (NodeRepositoryFileData) oldData ).getNode()
          : null, node );
      r.setData( new NodeRepositoryFileData( idManager.process( node ) ) );
    }
  }

  private static String findTitle( final RepositoryFile file ) {
    String title = null;
    if ( file.getLocalePropertiesMap() != null ) {
      Properties properties = file.getLocalePropertiesMap().get( Locale.getDefault().toString() );
      if ( properties == null ) {
        properties = file.getLocalePropertiesMap().get( RepositoryFile.DEFAULT_LOCALE );
        if ( properties != null ) {
          title = properties.getProperty( RepositoryFile.FILE_TITLE );
          if ( StringUtils.isBlank( title ) ) {
            title = properties.getProperty( RepositoryFile.TITLE );
          }
        }
      }
    }

    return title;
  }

  private static String findDesc( final RepositoryFile file ) {
    String desc = null;
    if ( file.getLocalePropertiesMap() != null ) {
      Properties properties = file.getLocalePropertiesMap().get( Locale.getDefault().toString() );
      if ( properties == null ) {
        properties = file.getLocalePropertiesMap().get( RepositoryFile.DEFAULT_LOCALE );
        if ( properties != null ) {
          desc = properties.getProperty( RepositoryFile.FILE_DESCRIPTION );
          if ( StringUtils.isBlank( desc ) ) {
            desc = properties.getProperty( RepositoryFile.DESCRIPTION );
          }
        }
      }
    }

    return desc;
  }

  @Override
  public RepositoryFile createFolder( final Serializable parentFolderId, final RepositoryFile file,
      final String versionMessage ) {
    return createFolder( parentFolderId, file, createDefaultAcl(), versionMessage );
  }

  @Override
  public RepositoryFile createFolder( final Serializable parentFolderId, final RepositoryFile file,
      final RepositoryFileAcl acl, final String versionMessage ) {
    Validate.isTrue( file.isFolder() );
    Validate.isTrue( !file.isVersioned() );
    if ( !hasAccess( parentFolderId, EnumSet.of( WRITE ) ) ) {
      throw new AccessDeniedException( "access denied" );
    }
    FileRecord parentFolder = idManager.getFileById( parentFolderId );
    RepositoryFile fileFromRepo =
        new RepositoryFile.Builder( file ).path(
            parentFolder.getPath()
                + ( parentFolder.getPath().endsWith( RepositoryFile.SEPARATOR ) ? "" : RepositoryFile.SEPARATOR )
                + file.getName() ).title( findTitle( file ) ).description( findDesc( file ) ).build();
    RepositoryFileAcl aclFromRepo = new RepositoryFileAcl.Builder( acl ).build();
    FileRecord fileRecord = new FileRecord( fileFromRepo, null, aclFromRepo, new HashMap<String, Serializable>() );
    idManager.register( fileRecord );
    parentFolder.addChild( fileRecord );
    return fileRecord.getFile();
  }

  @Override
  public List<RepositoryFile> getChildren( RepositoryRequest repositoryRequest ) {
    return getChildren( repositoryRequest.getPath(), repositoryRequest.getChildNodeFilter(), repositoryRequest.isShowHidden() );
  }

  @Override
  public List<RepositoryFile> getChildren( final Serializable folderId ) {
    return getChildren( folderId, null );
  }

  @Override
  public List<RepositoryFile> getChildren( final Serializable folderId, final String filter ) {
    return getChildren( folderId, filter, null );
  }

  @Override
  public List<RepositoryFile> getChildren( final Serializable folderId, final String filter, final Boolean showHiddenFiles ) {
    FileRecord r = idManager.getFileById( folderId );
    List<RepositoryFile> children = new ArrayList<RepositoryFile>();
    for ( FileRecord child : r.getChildren() ) {
      if ( filter != null ) {
        if ( matches( child.getFile().getName(), filter ) ) {
          if ( hasAccess( child.getFile().getId(), EnumSet.of( READ ) ) ) {
            children.add( child.getFile() );
          }
        }
      } else {
        if ( hasAccess( child.getFile().getId(), EnumSet.of( READ ) ) ) {
          children.add( child.getFile() );
        }
      }
    }
    return children;
  }

  private static boolean matches( final String in, final String pattern ) {
    StringBuilder buf = new StringBuilder();
    // build a regex
    String[] patterns = pattern.split( "\\|" );
    for ( int i = 0; i < patterns.length; i++ ) {
      if ( i > 0 ) {
        buf.append( "|" );
      }
      String tmp = patterns[i].trim();
      tmp = tmp.replace( ".", "\\." );
      tmp = tmp.replace( "*", ".*" );
      buf.append( tmp );
    }
    Pattern p = Pattern.compile( buf.toString() );
    Matcher m = p.matcher( in );
    return m.matches();
  }

  @Override
  public RepositoryFile updateFile( final RepositoryFile file, final IRepositoryFileData data,
      final String versionMessage ) {
    Validate.isTrue( !file.isFolder() );
    if ( !hasAccess( file.getId(), EnumSet.of( WRITE ) ) ) {
      throw new AccessDeniedException( "access denied" );
    }
    FileRecord fileRecord = idManager.getFileById( file.getId() );
    fileRecord.setFile( new RepositoryFile.Builder( file ).title( findTitle( file ) ).description( findDesc( file ) )
        .build() );
    IRepositoryFileData oldData = fileRecord.getData();
    fileRecord.setData( data );

    process( fileRecord, oldData );

    if ( file.isVersioned() ) {
      versionManager.createVersion( fileRecord.getFile().getId(), currentUserProvider.getUser(), versionMessage,
          new Date() );
    }
    return fileRecord.getFile();
  }

  @Override
  public void deleteFile( final Serializable fileId, final boolean permanent, final String versionMessage ) {
    FileRecord r = idManager.getFileById( fileId );
    FileRecord parentFolder = r.getParent();
    orphanFile( fileId );
    if ( !permanent ) {
      deleteManager.trash( parentFolder.getPath(), r );
    } else {
      idManager.deregister( fileId );
    }
  }

  private void orphanFile( final Serializable fileId ) {
    FileRecord r = idManager.getFileById( fileId );
    FileRecord parentFolder = r.getParent();
    if ( !hasAccess( parentFolder.getFile().getId(), EnumSet.of( WRITE ) ) ) {
      throw new AccessDeniedException( "access denied" );
    }
    parentFolder.orphan( r.getFile().getName() );
  }

  @Override
  public void deleteFile( final Serializable fileId, final String versionMessage ) {
    deleteFile( fileId, false, versionMessage );
  }

  @Override
  public void moveFile( final Serializable fileId, final String destAbsPath, final String versionMessage ) {
    copyMoveFile( fileId, destAbsPath, true );
  }

  @Override
  public void copyFile( final Serializable fileId, final String destAbsPath, final String versionMessage ) {
    copyMoveFile( fileId, destAbsPath, false );
  }

  private void copyMoveFile( final Serializable fileId, final String destAbsPath, final boolean move ) {
    FileRecord src = idManager.getFileById( fileId );
    FileRecord dest = root.getFileRecord( destAbsPath );
    String newName = src.getFile().getName();
    if ( dest != null ) {
      if ( !dest.getFile().isFolder() ) {
        throw new UnifiedRepositoryException( "file already exists" );
      }
    } else {
      // find parent folder
      String parentPath = StringUtils.substringBeforeLast( destAbsPath, RepositoryFile.SEPARATOR );
      FileRecord parent = root.getFileRecord( parentPath );
      if ( parent == null ) {
        throw new UnifiedRepositoryException( "invalid destination path" );
      }
      dest = parent;
      newName = StringUtils.substringAfterLast( destAbsPath, RepositoryFile.SEPARATOR );
    }
    String newPath =
        dest.getPath()
            + ( dest.getPath().endsWith( RepositoryFile.SEPARATOR ) ? newName : RepositoryFile.SEPARATOR + newName );

    FileRecord newChild =
        new FileRecord( new RepositoryFile.Builder( src.getFile() ).name( newName ).path( newPath ).build(), src
            .getData(), src.getAcl(), src.getMetadata() );
    if ( !move ) {
      idManager.register( newChild );
    }
    dest.addChild( newChild );
    if ( move ) {
      orphanFile( fileId );
    }
  }

  @Override
  public void undeleteFile( final Serializable fileId, final String versionMessage ) {
    FileRecord r = idManager.getFileById( fileId );
    FileRecord parentFolder = r.getParent();
    if ( !hasAccess( parentFolder.getFile().getId(), EnumSet.of( WRITE ) ) ) {
      throw new AccessDeniedException( "access denied" );
    }
    deleteManager.restore( fileId );
  }

  @Override
  public List<RepositoryFile> getDeletedFiles( final String origParentFolderPath ) {
    return getDeletedFiles( origParentFolderPath, null );
  }

  @Override
  public List<RepositoryFile> getDeletedFiles( final String origParentFolderPath, final String filter ) {
    List<RepositoryFile> deletedFiles = new ArrayList<RepositoryFile>();
    List<FileRecord> d = deleteManager.getTrashedFiles( origParentFolderPath, filter );
    for ( FileRecord r : d ) {
      deletedFiles.add( r.getFile() );
    }
    return deletedFiles;
  }

  @Override
  public List<RepositoryFile> getDeletedFiles() {
    List<RepositoryFile> deletedFiles = new ArrayList<RepositoryFile>();
    List<FileRecord> d = deleteManager.getTrashedFiles();
    for ( FileRecord r : d ) {
      deletedFiles.add( r.getFile() );
    }
    return deletedFiles;
  }

  @Override
  public boolean canUnlockFile( final Serializable fileId ) {
    return lockManager.canUnlockFile( fileId );
  }

  @Override
  public void lockFile( final Serializable fileId, final String message ) {
    lockManager.lockFile( fileId, message );
  }

  @Override
  public void unlockFile( final Serializable fileId ) {
    lockManager.unlockFile( fileId );
  }

  @Override
  public RepositoryFileAcl getAcl( final Serializable fileId ) {
    if ( !hasAccess( fileId, EnumSet.of( READ ) ) ) {
      throw new AccessDeniedException( "access denied" );
    }
    FileRecord r = idManager.getFileById( fileId );
    return r.getAcl();
  }

  @Override
  public RepositoryFileAcl updateAcl( final RepositoryFileAcl acl ) {
    if ( !hasAccess( acl.getId(), EnumSet.of( WRITE ) ) ) {
      throw new AccessDeniedException( "access denied" );
    }
    FileRecord r = idManager.getFileById( acl.getId() );
    r.setAcl( acl );
    return acl;
  }

  @Override
  public boolean hasAccess( final String path, final EnumSet<RepositoryFilePermission> permissions ) {
    FileRecord r = root.getFileRecord( path );
    if ( r != null ) {
      return hasAccess( r.getFile().getId(), permissions );
    }
    return false;
  }

  private boolean hasAccess( final Serializable fileId, final EnumSet<RepositoryFilePermission> permissions ) {
    String username = currentUserProvider.getUser();
    List<String> roles = currentUserProvider.getRoles();

    RepositoryFileAcl acl = idManager.getFileById( fileId ).getAcl();
    if ( acl.getOwner().getType() == USER && acl.getOwner().getName().equals( username ) ) {
      return true; // owner can do anything
    }

    List<RepositoryFileAce> aces = internalGetEffectiveAces( fileId );
    for ( RepositoryFileAce ace : aces ) {
      if ( ace.getSid().equals( everyone() ) && ace.getPermissions().containsAll( permissions ) ) {
        return true; // match special everyone role
      } else if ( ace.getSid().getType() == USER && ace.getSid().getName().equals( username )
          && ace.getPermissions().containsAll( permissions ) ) {
        return true; // match on user
      }
      for ( String role : roles ) {
        if ( ace.getSid().getType() == ROLE && ace.getSid().getName().equals( role )
            && ace.getPermissions().containsAll( permissions ) ) {
          return true; // match on role
        }
      }
    }
    return false;
  }

  @Override
  public List<RepositoryFileAce> getEffectiveAces( final Serializable fileId ) {
    if ( !hasAccess( fileId, EnumSet.of( READ ) ) ) {
      throw new AccessDeniedException( "access denied" );
    }
    return internalGetEffectiveAces( fileId );
  }

  private List<RepositoryFileAce> internalGetEffectiveAces( final Serializable fileId ) {
    FileRecord r = idManager.getFileById( fileId );
    if ( r.getParent() == null ) {
      return r.getAcl().getAces();
    } else if ( r.getAcl().isEntriesInheriting() == false ) {
      return r.getAcl().getAces();
    } else {
      return getEffectiveAces( r.getParent().getFile().getId() );
    }
  }

  @Override
  public List<RepositoryFileAce> getEffectiveAces( final Serializable fileId, final boolean forceEntriesInheriting ) {
    FileRecord r = idManager.getFileById( fileId );
    if ( r.getParent() != null ) {
      return getEffectiveAces( r.getParent().getFile().getId() );
    }

    return r.getAcl().getAces();
  }

  @Override
  public VersionSummary getVersionSummary( final Serializable fileId, final Serializable versionId ) {
    FrozenFileRecord r = null;
    if ( versionId == null ) {
      r = versionManager.getLatestVersion( fileId );
    } else {
      r = versionManager.getFileAtVersion( fileId, versionId );
    }
    return new VersionSummary( r.getVersionId(), r.getFile().getId(), false, r.getDate(), r.getAuthor(), r
        .getVersionMessage(), new ArrayList<String>( 0 ) );
  }

  @Override
  public List<VersionSummary> getVersionSummaryInBatch( final List<RepositoryFile> files ) {
    List<VersionSummary> sums = new ArrayList<VersionSummary>();
    for ( RepositoryFile file : files ) {
      if ( file.getVersionId() != null ) {
        sums.add( getVersionSummary( file.getId(), file.getVersionId() ) );
      } else {
        sums.add( getVersionSummary( file.getId(), versionManager.getLatestVersion( file.getId() ).getVersionId() ) );
      }
    }
    return sums;
  }

  @Override
  public List<VersionSummary> getVersionSummaries( final Serializable fileId ) {
    List<VersionSummary> sums = new ArrayList<VersionSummary>();
    List<FrozenFileRecord> records = versionManager.getVersions( fileId );
    for ( FrozenFileRecord record : records ) {
      sums.add( new VersionSummary( record.getVersionId(), record.getFile().getId(), false, record.getDate(), record
          .getAuthor(), record.getVersionMessage(), new ArrayList<String>( 0 ) ) );
    }
    return sums;
  }

  @Override
  public void deleteFileAtVersion( final Serializable fileId, final Serializable versionId ) {
    versionManager.deleteVersion( fileId, versionId );
  }

  @Override
  public void
    restoreFileAtVersion( final Serializable fileId, final Serializable versionId, final String versionMessage ) {
    FrozenFileRecord restored =
        versionManager.restoreVersion( fileId, versionId, currentUserProvider.getUser(), versionMessage, new Date() );
    FileRecord fileRecord = idManager.getFileById( fileId );
    fileRecord.setData( restored.getData() );
    fileRecord.setMetadata( restored.getMetadata() );
    // reset properties that aren't versioned
    RepositoryFile orig = fileRecord.getFile();
    fileRecord.setFile( new RepositoryFile.Builder( restored.getFile() ).locked( orig.isLocked() ).lockDate(
        orig.getLockDate() ).lockMessage( orig.getLockMessage() ).lockOwner( orig.getLockOwner() ).build() );
  }

  @Override
  public List<RepositoryFile> getReferrers( final Serializable fileId ) {
    List<RepositoryFile> files = new ArrayList<RepositoryFile>();
    for ( Serializable refFileId : referralManager.getReferrers( fileId ) ) {
      files.add( idManager.getFileById( refFileId ).getFile() );
    }
    return files;
  }

  @Override
  public void setFileMetadata( final Serializable fileId, final Map<String, Serializable> metadataMap ) {
    // if (!hasAccess(fileId, EnumSet.of(WRITE))) {
    // throw new AccessDeniedException("access denied");
    // }
    FileRecord r = idManager.getFileById( fileId );
    r.setMetadata( metadataMap );
  }

  @Override
  public Map<String, Serializable> getFileMetadata( final Serializable fileId ) {
    if ( !hasAccess( fileId, EnumSet.of( READ ) ) ) {
      throw new AccessDeniedException( "access denied" );
    }
    FileRecord r = idManager.getFileById( fileId );
    return r.getMetadata();
  }

  // ~ Helper classes
  // ==================================================================================================

  @SuppressWarnings( "serial" )
  private class ReusableSimpleRepositoryFileData extends SimpleRepositoryFileData {

    private byte[] bytes;

    public ReusableSimpleRepositoryFileData( final SimpleRepositoryFileData data ) {
      super( null, data.getEncoding(), data.getMimeType() );
      try {
        bytes = IOUtils.toByteArray( data.getStream() );
      } catch ( IOException e ) {
        throw new RuntimeException( e );
      }
    }

    @Override
    public InputStream getStream() {
      return new ByteArrayInputStream( bytes );
    }

  }

  private class FileRecord implements Comparable<FileRecord> {

    public FileRecord( final RepositoryFile file, final IRepositoryFileData data, final RepositoryFileAcl acl,
        final Map<String, Serializable> metadata ) {
      this.file = file;
      setData( data );
      this.acl = acl;
      setMetadata( metadata );
    }

    public FileRecord( final RepositoryFile file, final RepositoryFileAcl acl ) {
      this( file, null, acl, new HashMap<String, Serializable>() );
    }

    public void setFile( final RepositoryFile file ) {
      this.file = file;
    }

    public void setData( final IRepositoryFileData data ) {
      this.data = data;
    }

    public void setAcl( final RepositoryFileAcl acl ) {
      this.acl = acl;
    }

    public void setMetadata( final Map<String, Serializable> metadata ) {
      this.metadata = new HashMap<String, Serializable>( metadata );
    }

    private void setParent( final FileRecord parent ) {
      this.parent = parent;
    }

    private FileRecord getParent() {
      return parent;
    }

    public RepositoryFileAcl getAcl() {
      return acl;
    }

    public String getPath() {
      if ( file.getName().equals( "" ) ) {
        return RepositoryFile.SEPARATOR;
      }
      String parentPath = parent.getPath();
      return parentPath + ( parentPath.endsWith( RepositoryFile.SEPARATOR ) ? "" : RepositoryFile.SEPARATOR )
          + file.getName();
    }

    public FileRecord getFileRecord( final String path ) {
      String normalizedPath = path;
      if ( path.startsWith( RepositoryFile.SEPARATOR ) ) {
        normalizedPath = path.substring( 1 );
      }
      if ( normalizedPath.equals( "" ) ) {
        return this;
      }
      String[] pathSegments = normalizedPath.split( RepositoryFile.SEPARATOR );
      if ( hasChild( pathSegments[0] ) ) {
        return getChild( pathSegments[0] ).getFileRecord(
            1 <= pathSegments.length - 1 ? StringUtils.join(
                Arrays.copyOfRange( pathSegments, 1, pathSegments.length ), RepositoryFile.SEPARATOR ) : "" );
      }
      return null;
    }

    public boolean hasChild( final String name ) {
      return getChild( name ) != null;
    }

    public FileRecord getChild( final String name ) {
      for ( FileRecord child : children ) {
        if ( child.getFile().getName().equals( name ) ) {
          return child;
        }
      }
      return null;
    }

    private RepositoryFile file;

    private IRepositoryFileData data;

    private Map<String, Serializable> metadata;

    public FileRecord parent;

    public List<FileRecord> children = new ArrayList<FileRecord>();

    public RepositoryFileAcl acl;

    public void addChild( final FileRecord fileRecord ) {
      // make sure no name collision
      for ( FileRecord child : children ) {
        if ( child.getFile().getName().equals( fileRecord.getFile().getName() ) ) {
          throw new UnifiedRepositoryException( String.format( "file [%s] already exists", fileRecord.getFile()
              .getName() ) );
        }
      }
      children.add( fileRecord );
      fileRecord.setParent( this );
      Collections.sort( children );
    }

    public List<FileRecord> getChildren() {
      return Collections.unmodifiableList( children );
    }

    public void orphan( final String name ) {
      for ( Iterator<FileRecord> iter = children.iterator(); iter.hasNext(); ) {
        FileRecord r = iter.next();
        if ( r.getFile().getName().equals( name ) ) {
          iter.remove();
          return;
        }
      }
    }

    private RepositoryFile getFile() {
      return file;
    }

    private IRepositoryFileData getData() {
      return data;
    }

    private Map<String, Serializable> getMetadata() {
      return Collections.unmodifiableMap( metadata );
    }

    @Override
    public int compareTo( final FileRecord other ) {
      return this.getFile().getName().compareTo( other.getFile().getName() );
    }

  }

  private class IdManager {

    private Map<Serializable, FileRecord> idMap = new HashMap<Serializable, FileRecord>();

    public boolean hasId( final Serializable fileId ) {
      return idMap.containsKey( fileId );
    }

    public FileRecord getFileById( final Serializable fileId ) {
      FileRecord r = idMap.get( fileId );
      if ( r == null ) {
        throw new UnifiedRepositoryException( String.format( "file id [%s] does not exist", fileId ) );
      }
      return r;
    }

    public void register( final FileRecord fileRecord ) {
      Serializable fileId = UUID.randomUUID().toString();
      fileRecord.setFile( new RepositoryFile.Builder( fileRecord.getFile() ).id( fileId ).build() );
      fileRecord.setAcl( new RepositoryFileAcl.Builder( fileRecord.getAcl() ).id( fileId ).build() );
      idMap.put( fileId, fileRecord );
    }

    public DataNode process( final DataNode node ) {
      node.setId( UUID.randomUUID().toString() );
      for ( DataNode child : node.getNodes() ) {
        process( child );
      }
      return node;
    }

    /**
     * Removes file from this manager. Returns FileRecord mapped to fileId (null if no such fileId).
     */
    public FileRecord deregister( final Serializable fileId ) {
      return idMap.remove( fileId );
    }

  }

  private class VersionManager {

    private Map<Serializable, List<FrozenFileRecord>> versionMap = new HashMap<Serializable, List<FrozenFileRecord>>();

    public FrozenFileRecord getLatestVersion( final Serializable fileId ) {
      List<FrozenFileRecord> history = versionMap.get( fileId );
      if ( history == null ) {
        throw new UnifiedRepositoryException( String.format( "version history for [%s] does not exist", fileId ) );
      }
      return history.get( history.size() - 1 );
    }

    public void createVersion( final Serializable fileId, final String author, final String versionMessage,
        final Date date ) {
      List<FrozenFileRecord> history = versionMap.get( fileId );
      if ( history == null ) {
        history = new ArrayList<FrozenFileRecord>();
        versionMap.put( fileId, history );
      }
      FileRecord fileRecord = idManager.getFileById( fileId );
      fileRecord.setFile( new RepositoryFile.Builder( fileRecord.getFile() ).versionId( history.size() ).build() );
      history.add( new FrozenFileRecord( history.size(), fileRecord.getFile(), fileRecord.getData(), fileRecord
          .getMetadata(), author, versionMessage, date ) );
    }

    public void deleteVersion( final Serializable fileId, final Serializable versionId ) {
      List<FrozenFileRecord> history = versionMap.get( fileId );
      if ( history == null ) {
        throw new UnifiedRepositoryException( String.format( "version history for [%s] does not exist", fileId ) );
      }
      Integer versionNumber = versionNumber( versionId );
      if ( versionNumber >= 0 && versionNumber < history.size() ) {
        FrozenFileRecord r = history.get( versionNumber );
        if ( r == null ) {
          throw new UnifiedRepositoryException( String.format( "version [%s] does not exist", versionId ) );
        }
        history.set( versionNumber, null );
      } else {
        throw new UnifiedRepositoryException( String.format( "unknown version [%s]", fileId ) );
      }
    }

    public FrozenFileRecord restoreVersion( final Serializable fileId, final Serializable versionId,
        final String author, final String versionMessage, final Date date ) {
      List<FrozenFileRecord> history = versionMap.get( fileId );
      if ( history == null ) {
        throw new UnifiedRepositoryException( String.format( "version history for [%s] does not exist", fileId ) );
      }
      Integer versionNumber = versionNumber( versionId );
      if ( versionNumber >= 0 && versionNumber < history.size() ) {
        FrozenFileRecord r = history.get( versionNumber );
        if ( r == null ) {
          throw new UnifiedRepositoryException( String.format( "version [%s] does not exist", versionId ) );
        }
        history.add( new FrozenFileRecord( history.size(), new RepositoryFile.Builder( r.getFile() ).versionId(
            history.size() ).build(), r.getData(), r.getMetadata(), author, versionMessage, date ) );
        return history.get( history.size() - 1 );
      }

      throw new UnifiedRepositoryException( String.format( "unknown version [%s]", fileId ) );
    }

    public List<FrozenFileRecord> getVersions( final Serializable fileId ) {
      List<FrozenFileRecord> history = versionMap.get( fileId );
      if ( history == null ) {
        throw new UnifiedRepositoryException( String.format( "version history for [%s] does not exist", fileId ) );
      }
      List<FrozenFileRecord> cleanedHistory = new ArrayList<FrozenFileRecord>( history );
      for ( Iterator<FrozenFileRecord> iter = cleanedHistory.iterator(); iter.hasNext(); ) {
        FrozenFileRecord r = iter.next();
        if ( r == null ) {
          iter.remove();
        }
      }
      return cleanedHistory;
    }

    public FrozenFileRecord getFileAtVersion( final Serializable fileId, final Serializable versionId ) {
      List<FrozenFileRecord> history = versionMap.get( fileId );
      if ( history == null ) {
        throw new UnifiedRepositoryException( String.format( "version history for [%s] does not exist", fileId ) );
      }
      Integer versionNumber = versionNumber( versionId );
      FrozenFileRecord r = history.get( versionNumber );
      if ( r == null ) {
        throw new UnifiedRepositoryException( String.format( "version [%s] does not exist", versionId ) );
      }
      return r;
    }

    private int versionNumber( final Serializable versionId ) {
      if ( versionId instanceof Integer ) {
        return (Integer) versionId;
      } else if ( versionId instanceof String ) {
        return Integer.parseInt( (String) versionId );
      } else {
        return Integer.parseInt( versionId.toString() );
      }
    }

  }

  public class FrozenFileRecord {

    private Serializable versionId;

    private RepositoryFile file;

    private IRepositoryFileData data;

    private Map<String, Serializable> metadata;

    private String author;

    private String versionMessage;

    private Date date;

    public FrozenFileRecord( final Serializable versionId, final RepositoryFile file, final IRepositoryFileData data,
        final Map<String, Serializable> metadata, final String author, final String versionMessage, final Date date ) {
      super();
      this.versionId = versionId;
      this.file = file;
      this.data = data;
      this.metadata = metadata;
      this.author = author;
      this.versionMessage = versionMessage;
      this.date = date;
    }

    public Serializable getVersionId() {
      return versionId;
    }

    public RepositoryFile getFile() {
      return file;
    }

    public IRepositoryFileData getData() {
      return data;
    }

    public Map<String, Serializable> getMetadata() {
      return metadata;
    }

    private String getAuthor() {
      return author;
    }

    private String getVersionMessage() {
      return versionMessage;
    }

    private Date getDate() {
      return date;
    }

  }

  private class DeleteManager {
    Map<String, Trash> userToTrashMap = new HashMap<String, Trash>();

    private class Trash {
      private Map<String, List<FileRecord>> origPathToFilesMap = new HashMap<String, List<FileRecord>>();

      private Map<Serializable, String> idToOrigPathMap = new HashMap<Serializable, String>();

      private Map<String, List<FileRecord>> getOrigPathToFilesMap() {
        return origPathToFilesMap;
      }

      private Map<Serializable, String> getIdToOrigPathMap() {
        return idToOrigPathMap;
      }
    }

    public void trash( final String origPath, final FileRecord deletedRecord ) {
      Trash trash = userToTrashMap.get( currentUserProvider.getUser() );
      if ( trash == null ) {
        trash = new Trash();
        userToTrashMap.put( currentUserProvider.getUser(), trash );
      }

      RepositoryFile popFile =
          new RepositoryFile.Builder( deletedRecord.getFile() ).originalParentFolderPath( origPath ).deletedDate(
              new Date() ).build();
      deletedRecord.setFile( popFile );

      List<FileRecord> dels = trash.getOrigPathToFilesMap().get( origPath );
      if ( dels == null ) {
        dels = new ArrayList<FileRecord>();
        trash.getOrigPathToFilesMap().put( origPath, dels );
      }
      dels.add( deletedRecord );
      trash.getIdToOrigPathMap().put( deletedRecord.getFile().getId(), origPath );
    }

    public void restore( final Serializable fileId ) {
      Trash trash = userToTrashMap.get( currentUserProvider.getUser() );
      if ( trash == null ) {
        throw new UnifiedRepositoryException( "no trash found for user" );
      }

      String origPath = trash.getIdToOrigPathMap().get( fileId );
      List<FileRecord> dels = trash.getOrigPathToFilesMap().get( origPath );
      FileRecord found = null;
      for ( Iterator<FileRecord> iter = dels.iterator(); iter.hasNext(); ) {
        FileRecord r = iter.next();
        if ( r.getFile().getId().equals( fileId ) ) {
          iter.remove();
          found = r;
        }
      }
      FileRecord parentFolder = root.getFileRecord( origPath );
      parentFolder.addChild( found );
      trash.getIdToOrigPathMap().remove( fileId );

      RepositoryFile popFile =
          new RepositoryFile.Builder( found.getFile() ).originalParentFolderPath( null ).deletedDate( null ).build();
      found.setFile( popFile );
    }

    public List<FileRecord> getTrashedFiles( final String path, final String filter ) {
      Trash trash = userToTrashMap.get( currentUserProvider.getUser() );
      if ( trash == null ) {
        return Collections.emptyList();
      }

      List<FileRecord> filtered = new ArrayList<FileRecord>();
      List<FileRecord> dels = trash.getOrigPathToFilesMap().get( path );
      if ( dels == null ) {
        return Collections.emptyList();
      }
      for ( FileRecord d : dels ) {
        if ( filter != null ) {
          if ( matches( d.getFile().getName(), filter ) ) {
            filtered.add( d );
          }
        } else {
          filtered.add( d );
        }
      }
      return filtered;
    }

    public List<FileRecord> getTrashedFiles() {
      Trash trash = userToTrashMap.get( currentUserProvider.getUser() );
      if ( trash == null ) {
        return Collections.emptyList();
      }

      List<FileRecord> deletedRecords = new ArrayList<FileRecord>();
      for ( List<FileRecord> list : trash.getOrigPathToFilesMap().values() ) {
        deletedRecords.addAll( list );
      }
      Collections.sort( deletedRecords );
      return deletedRecords;
    }

  }

  private class LockManager {

    public boolean canUnlockFile( final Serializable fileId ) {
      return hasAccess( fileId, EnumSet.of( WRITE ) );
    }

    public void lockFile( final Serializable fileId, final String message ) {
      if ( !hasAccess( fileId, EnumSet.of( WRITE ) ) ) {
        throw new UnifiedRepositoryException( "access denied" );
      }
      FileRecord r = idManager.getFileById( fileId );
      if ( r.getFile().getLockOwner() != null ) {
        throw new IllegalStateException( "file is already locked" );
      }
      r.setFile( new RepositoryFile.Builder( r.getFile() ).lockOwner( currentUserProvider.getUser() ).lockDate(
          new Date() ).lockMessage( message ).locked( true ).build() );
    }

    public void unlockFile( final Serializable fileId ) {
      if ( !hasAccess( fileId, EnumSet.of( WRITE ) ) ) {
        throw new UnifiedRepositoryException( "access denied" );
      }
      FileRecord r = idManager.getFileById( fileId );
      if ( r.getFile().getLockOwner() == null ) {
        throw new IllegalStateException( "file is not locked" );
      }
      r.setFile( new RepositoryFile.Builder( r.getFile() ).lockOwner( null ).lockDate( null ).lockMessage( null )
          .locked( false ).build() );
    }

  }

  private class ReferralManager {

    private Map<Serializable, Set<Serializable>> referrersMap = new HashMap<Serializable, Set<Serializable>>();

    public void process( final Serializable fileId, final DataNode oldNode, final DataNode newNode ) {
      if ( oldNode != null ) {
        process( fileId, oldNode, true );
      }
      process( fileId, newNode, false );
    }

    private void process( final Serializable fileId, final DataNode node, final boolean clean ) {
      for ( DataProperty property : node.getProperties() ) {
        if ( property.getType() == DataPropertyType.REF ) {
          Set<Serializable> referrers = referrersMap.get( property.getRef().getId() );
          if ( referrers == null ) {
            referrers = new HashSet<Serializable>();
            referrersMap.put( property.getRef().getId(), referrers );
          }
          if ( clean ) {
            referrers.remove( fileId );
          } else {
            referrers.add( fileId );
          }
        }
      }
      for ( DataNode child : node.getNodes() ) {
        process( fileId, child, clean );
      }
    }

    public Set<Serializable> getReferrers( final Serializable fileId ) {
      Set<Serializable> refs = referrersMap.get( fileId );
      if ( refs == null ) {
        return Collections.emptySet();
      }
      return Collections.unmodifiableSet( refs );
    }

  }

  public static interface ICurrentUserProvider {
    public String getUser();

    public List<String> getRoles();
  }

  public static class SpringSecurityCurrentUserProvider implements ICurrentUserProvider {

    @Override
    public String getUser() {
      return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public List<String> getRoles() {
      Collection<? extends GrantedAuthority> auths = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
      List<String> roles = new ArrayList<String>();
      for ( GrantedAuthority auth : auths ) {
        roles.add( auth.getAuthority() );
      }
      return roles;
    }

  }

  @Override
  public List<Character> getReservedChars() {
    return Collections.emptyList();
  }

  @Override
  public List<Locale> getAvailableLocalesForFileById( Serializable fileId ) {
    return Collections.emptyList();
  }

  @Override
  public List<Locale> getAvailableLocalesForFileByPath( String relPath ) {
    return Collections.emptyList();
  }

  @Override
  public List<Locale> getAvailableLocalesForFile( RepositoryFile repositoryFile ) {
    return Collections.emptyList();
  }

  @Override
  public Properties getLocalePropertiesForFileById( Serializable fileId, String locale ) {
    return new Properties();
  }

  @Override
  public Properties getLocalePropertiesForFileByPath( String relPath, String locale ) {
    return new Properties();
  }

  @Override
  public Properties getLocalePropertiesForFile( RepositoryFile repositoryFile, String locale ) {
    return new Properties();
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
    Validate.isTrue( folder.isFolder() );
    if ( !hasAccess( folder.getId(), EnumSet.of( WRITE ) ) ) {
      throw new AccessDeniedException( "access denied" );
    }
    FileRecord fileRecord = idManager.getFileById( folder.getId() );
    fileRecord.setFile( new RepositoryFile.Builder( folder ).hidden( folder.isHidden() ).title( findTitle( folder ) )
        .description( findDesc( folder ) ).aclNode( folder.isAclNode() ).build() );
    if ( folder.isVersioned() ) {
      versionManager.createVersion( fileRecord.getFile().getId(), currentUserProvider.getUser(), versionMessage,
          new Date() );
    }
    return fileRecord.getFile();
  }

  // public static void main(final String[] args) throws Exception {
  // MockUnifiedRepository repo = new MockUnifiedRepository();
  // t(repo.getFile("/").getId() != null);
  // t(repo.getFile("/").getPath().equals("/"));
  // t(repo.getFile("/public").getId() != null);
  // t(repo.getFile("/public").getPath().equals("/public"));
  // repo.createFile(repo.getFile("/public").getId(), new
  // RepositoryFile.Builder("file.txt").versioned(true).build(),
  // new SimpleRepositoryFileData(new ByteArrayInputStream("hello world".getBytes("UTF-8")), "UTF-8",
  // "text/plain"),
  // "hello world");
  // t(repo.getFile("/public/file.txt").getId() != null);
  // t(repo.getVersionSummaries(repo.getFile("/public/file.txt").getId()).size() == 1);
  //
  // repo.updateFile(repo.getFile("/public/file.txt"), new SimpleRepositoryFileData(new ByteArrayInputStream(
  // "caio world".getBytes("UTF-8")), "UTF-8", "text/plain"), "caio world");
  // t(repo.getVersionSummaries(repo.getFile("/public/file.txt").getId()).size() == 2);
  // List<VersionSummary> sums = repo.getVersionSummaries(repo.getFile("/public/file.txt").getId());
  // t(repo.getFileAtVersion(sums.get(0).getVersionedFileId(), sums.get(0).getId()) != null);
  // t(repo.getFileAtVersion(sums.get(1).getVersionedFileId(), sums.get(1).getId()) != null);
  // SimpleRepositoryFileData d = repo.getDataForRead(repo.getFile("/public/file.txt").getId(),
  // SimpleRepositoryFileData.class);
  // SimpleRepositoryFileData d0 = repo.getDataAtVersionForRead(repo.getFile("/public/file.txt").getId(),
  // sums.get(0)
  // .getId(), SimpleRepositoryFileData.class);
  // SimpleRepositoryFileData d1 = repo.getDataAtVersionForRead(repo.getFile("/public/file.txt").getId(),
  // sums.get(1)
  // .getId(), SimpleRepositoryFileData.class);
  // t(Arrays.equals(IOUtils.toByteArray(d.getStream()), IOUtils.toByteArray(d1.getStream())));
  // t(Arrays.equals(IOUtils.toByteArray(d0.getStream()), "hello world".getBytes("UTF-8")));
  // t(Arrays.equals(IOUtils.toByteArray(d1.getStream()), "caio world".getBytes("UTF-8")));
  //
  // repo.restoreFileAtVersion(repo.getFile("/public/file.txt").getId(), sums.get(0).getId(), null);
  // List<VersionSummary> sums2 = repo.getVersionSummaries(repo.getFile("/public/file.txt").getId());
  // t(sums2.size() == 3);
  // SimpleRepositoryFileData d2 = repo.getDataForRead(repo.getFile("/public/file.txt").getId(),
  // SimpleRepositoryFileData.class);
  // t(Arrays.equals(IOUtils.toByteArray(d2.getStream()), "hello world".getBytes("UTF-8")));
  // repo.deleteFileAtVersion(repo.getFile("/public/file.txt").getId(), sums2.get(2).getId());
  // t(repo.getVersionSummaries(repo.getFile("/public/file.txt").getId()).size() == 2);
  //
  // t(repo.getAcl(repo.getFile("/public/file.txt").getId()) != null);
  // RepositoryFileAcl newAcl = new
  // RepositoryFileAcl.Builder(repo.getAcl(repo.getFile("/public/file.txt").getId()))
  // .ace(new RepositoryFileSid("larry", USER), READ).build();
  // t(repo.updateAcl(newAcl).getId() != null);
  // t(repo.getAcl(repo.getFile("/public/file.txt").getId()).getAces().get(0).getSid()
  // .equals(new RepositoryFileSid("larry", USER)));
  //
  // t(repo.createFolder(repo.getFile("/public").getId(), new
  // RepositoryFile.Builder("testFolder").folder(true).build(),
  // null).getId() != null);
  //
  // t(repo.getChildren(repo.getFile("/public").getId()).size() == 2);
  // t(repo.getChildren(repo.getFile("/public").getId(), "*.txt").size() == 1);
  //
  // RepositoryFileTree tree = repo.getTree("/public", -1, null, true);
  // t(tree.getFile().getName().equals("public"));
  // t(tree.getChildren().size() == 2);
  // t(tree.getChildren().get(1).getFile().getName().equals("testFolder"));
  //
  // repo.copyFile(repo.getFile("/public/file.txt").getId(), "/public/testFolder", null);
  // t(repo.getFile("/public/file.txt") != null);
  // t(repo.getFile("/public/testFolder/file.txt") != null);
  // t(!repo.getFile("/public/file.txt").getId().equals(repo.getFile("/public/testFolder/file.txt").getId()));
  // Serializable testFolderFileId = repo.getFile("/public/testFolder/file.txt").getId();
  // repo.moveFile(testFolderFileId, "/public/file2.txt", null);
  // t(repo.getFile("/public/testFolder/file.txt") == null);
  // t(repo.getFile("/public/file2.txt").getId().equals(testFolderFileId));
  //
  // t(repo.canUnlockFile(repo.getFile("/public/file.txt").getId()));
  // t(!repo.getFile("/public/file.txt").isLocked());
  // repo.lockFile(repo.getFile("/public/file.txt").getId(), "blah");
  // t(repo.getFile("/public/file.txt").isLocked());
  // t(repo.getFile("/public/file.txt").getLockMessage().equals("blah"));
  // repo.unlockFile(repo.getFile("/public/file.txt").getId());
  // t(!repo.getFile("/public/file.txt").isLocked());
  //
  // t(repo.getDeletedFiles().isEmpty());
  // Serializable fileTxtId = repo.getFile("/public/file.txt").getId();
  // repo.deleteFile(repo.getFile("/public/file.txt").getId(), null);
  // t(repo.getDeletedFiles().size() == 1);
  // t(repo.getDeletedFiles("/public").size() == 1);
  // t(repo.getDeletedFiles("/public", "*.txt").size() == 1);
  // repo.undeleteFile(fileTxtId, null);
  // t(repo.getDeletedFiles().isEmpty());
  // repo.deleteFile(repo.getFile("/public/file.txt").getId(), true, null);
  // t(repo.getDeletedFiles().isEmpty());
  // }
  //
  // private static void t(boolean test) {
  // if (!test) {
  // throw new RuntimeException();
  // }
  // }
}
