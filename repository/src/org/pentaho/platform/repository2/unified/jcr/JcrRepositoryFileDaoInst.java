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

package org.pentaho.platform.repository2.unified.jcr;

import org.apache.commons.lang.ArrayUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoterManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryDefaultAclHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;

/**
 * CRUD operations against JCR. Note that there is no access control in this class (implicit or explicit).
 * 
 * @author mlowery
 */
public class JcrRepositoryFileDaoInst {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================
  private List<ITransformer<IRepositoryFileData>> transformers;

  private ILockHelper lockHelper;

  private IDeleteHelper deleteHelper;

  private IPathConversionHelper pathConversionHelper;

  private IRepositoryFileAclDao aclDao;

  private IRepositoryDefaultAclHandler defaultAclHandler;

  private IRepositoryAccessVoterManager accessVoterManager;

  private String repositoryAdminUsername;

  // ~ Constructors
  // ====================================================================================================

  public JcrRepositoryFileDaoInst( final List<ITransformer<IRepositoryFileData>> transformers,
      final ILockHelper lockHelper, final IDeleteHelper deleteHelper, final IPathConversionHelper pathConversionHelper,
      final IRepositoryFileAclDao aclDao, final IRepositoryDefaultAclHandler defaultAclHandler,
      String repositoryAdminUsername ) {
    super();
    Assert.notNull( transformers );
    this.transformers = transformers;
    this.lockHelper = lockHelper;
    this.deleteHelper = deleteHelper;
    this.pathConversionHelper = pathConversionHelper;
    this.aclDao = aclDao;
    this.defaultAclHandler = defaultAclHandler;
    this.repositoryAdminUsername = repositoryAdminUsername;
  }

  public JcrRepositoryFileDaoInst( final List<ITransformer<IRepositoryFileData>> transformers,
      final ILockHelper lockHelper, final IDeleteHelper deleteHelper, final IPathConversionHelper pathConversionHelper,
      final IRepositoryFileAclDao aclDao, final IRepositoryDefaultAclHandler defaultAclHandler,
      final IRepositoryAccessVoterManager accessVoterManager, String repositoryAdminUsername ) {
    this( transformers, lockHelper, deleteHelper, pathConversionHelper, aclDao, defaultAclHandler,
        repositoryAdminUsername );
    this.accessVoterManager = accessVoterManager;
  }

  public void setDefaultAclHandler( IRepositoryDefaultAclHandler defaultAclHandler ) {
    this.defaultAclHandler = defaultAclHandler;
  }

  public VersionSummary getVersionSummary( final Session session, final Serializable fileId,
      final Serializable versionId ) throws RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    return JcrRepositoryFileUtils.getVersionSummary( session, pentahoJcrConstants, fileId, versionId );
  }

  public RepositoryFile internalCreateFolder( final Session session, final Serializable parentFolderId,
      final RepositoryFile folder, final RepositoryFileAcl acl, final String versionMessage )
    throws RepositoryException {
    if ( !hasAccess( session, parentFolderId, RepositoryFilePermission.WRITE ) ) {
          return null;
        }
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, parentFolderId );
    Node folderNode = JcrRepositoryFileUtils.createFolderNode( session, pentahoJcrConstants, parentFolderId, folder );
    // create a temporary folder object with correct path for default acl purposes.
    String path = JcrRepositoryFileUtils.getAbsolutePath( session, pentahoJcrConstants, folderNode );
    RepositoryFile tmpFolder = new RepositoryFile.Builder( folder ).path( path ).build();
    // we must create the acl during checkout
    JcrRepositoryFileAclUtils.createAcl( session, pentahoJcrConstants, folderNode.getIdentifier(), acl == null
        ? defaultAclHandler.createDefaultAcl( tmpFolder ) : acl );
    session.save();
    if ( folder.isVersioned() ) {
      JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary( session, pentahoJcrConstants, folderNode,
          versionMessage );
    }
    JcrRepositoryFileUtils
        .checkinNearestVersionableFileIfNecessary(
            session,
            pentahoJcrConstants,
            parentFolderId,
            Messages
                .getInstance()
                .getString(
                    "JcrRepositoryFileDao.USER_0001_VER_COMMENT_ADD_FOLDER", folder.getName(), ( parentFolderId == null ? "root" : parentFolderId.toString() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
    return JcrRepositoryFileUtils.nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper,
        folderNode );
  }

  public RepositoryFile internalCreateFile( Session session, final Serializable parentFolderId,
      final RepositoryFile file, final IRepositoryFileData content, final RepositoryFileAcl acl,
      final String versionMessage ) throws RepositoryException {
    if ( !hasAccess( session, parentFolderId, RepositoryFilePermission.WRITE ) ) {
          return null;
        }
    // Assert.notNull(content);
    DataNode emptyDataNode = new DataNode( file.getName() );
    emptyDataNode.setProperty( " ", "content" ); //$NON-NLS-1$ //$NON-NLS-2$
    final IRepositoryFileData emptyContent = new NodeRepositoryFileData( emptyDataNode );

        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, parentFolderId );
        Node fileNode =
            JcrRepositoryFileUtils.createFileNode( session, pentahoJcrConstants, parentFolderId, file, content == null
                ? emptyContent : content, findTransformerForWrite( content == null ? emptyContent.getClass() : content
                .getClass() ) );
        // create a tmp file with correct path for default acl creation purposes.
        String path = JcrRepositoryFileUtils.getAbsolutePath( session, pentahoJcrConstants, fileNode );
        RepositoryFile tmpFile = new RepositoryFile.Builder( file ).path( path ).build();
        // we must create the acl during checkout
        aclDao.createAcl( fileNode.getIdentifier(), acl == null ? defaultAclHandler.createDefaultAcl( tmpFile ) : acl );
        session.save();
        if ( file.isVersioned() ) {
          JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary( session, pentahoJcrConstants, fileNode,
              versionMessage, file.getCreatedDate(), false );

        }
        JcrRepositoryFileUtils
            .checkinNearestVersionableFileIfNecessary(
                session,
                pentahoJcrConstants,
                parentFolderId,
                Messages
                    .getInstance()
                    .getString(
                        "JcrRepositoryFileDao.USER_0002_VER_COMMENT_ADD_FILE", file.getName(), ( parentFolderId == null ? "root" : parentFolderId.toString() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
    return JcrRepositoryFileUtils.nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper, fileNode );
      }

  public RepositoryFile internalUpdateFile( final Session session, final RepositoryFile file,
      final IRepositoryFileData content, final String versionMessage ) throws RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    if ( !hasAccess( file, RepositoryFilePermission.WRITE ) ) {
      return null;
    }
    lockHelper.addLockTokenToSessionIfNecessary( session, pentahoJcrConstants, file.getId() );
    JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, file.getId() );
    JcrRepositoryFileUtils.updateFileNode( session, pentahoJcrConstants, file, content,
        findTransformerForWrite( content.getClass() ) );
    session.save();
    JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, file.getId(),
        versionMessage, new java.util.Date(), true );
    lockHelper.removeLockTokenFromSessionIfNecessary( session, pentahoJcrConstants, file.getId() );
    return JcrRepositoryFileUtils.nodeIdToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper, file
        .getId() );
  }

  public RepositoryFile internalUpdateFolder( final Session session, final RepositoryFile folder,
      final String versionMessage ) throws RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    lockHelper.addLockTokenToSessionIfNecessary( session, pentahoJcrConstants, folder.getId() );
    JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, folder.getId() );
    JcrRepositoryFileUtils.updateFolderNode( session, pentahoJcrConstants, folder );
    session.save();
    JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, folder.getId(),
        versionMessage );
    lockHelper.removeLockTokenFromSessionIfNecessary( session, pentahoJcrConstants, folder.getId() );
    return JcrRepositoryFileUtils.nodeIdToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper, folder
        .getId() );
  }

  public void undeleteFile( final Session session, final Serializable fileId, final String versionMessage )
    throws RepositoryException, IOException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    String absOrigParentFolderPath = deleteHelper.getOriginalParentFolderPath( session, pentahoJcrConstants, fileId );
    Serializable origParentFolderId = null;
    if ( !hasAccess( session, fileId, RepositoryFilePermission.WRITE ) ) {
      return;
      }
    // original parent folder path may no longer exist!
    if ( session.itemExists( JcrStringHelper.pathEncode( absOrigParentFolderPath ) ) ) {
      origParentFolderId =
          ( (Node) session.getItem( JcrStringHelper.pathEncode( absOrigParentFolderPath ) ) ).getIdentifier();
    } else {
      // go through each of the segments of the original parent folder path, creating as necessary
      String[] segments = pathConversionHelper.absToRel( absOrigParentFolderPath ).split( RepositoryFile.SEPARATOR );
      RepositoryFile lastParentFolder =
          internalGetFile( session, ServerRepositoryPaths.getTenantRootFolderPath(), false, null );
      for ( String segment : segments ) {
        if ( StringUtils.hasLength( segment ) ) {
          RepositoryFile tmp =
              internalGetFile( session,
                  pathConversionHelper.relToAbs( ( lastParentFolder.getPath().equals( RepositoryFile.SEPARATOR )
                      ? "" : lastParentFolder.getPath() ) + RepositoryFile.SEPARATOR + segment ), false, null ); //$NON-NLS-1$
          if ( tmp == null ) {
            lastParentFolder =
                internalCreateFolder( session, lastParentFolder.getId(), new RepositoryFile.Builder( segment ).folder(
                    true ).build(), defaultAclHandler.createDefaultAcl( lastParentFolder ), null );
          } else {
            lastParentFolder = tmp;
    }
  }
      }
      origParentFolderId = lastParentFolder.getId();
    }
    JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, origParentFolderId );
    deleteHelper.undeleteFile( session, pentahoJcrConstants, fileId );
    session.save();
    JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, origParentFolderId,
        versionMessage );
  }

  public void deleteFile( final Session session, final Serializable fileId, final String versionMessage )
    throws RepositoryException {
    if ( !hasAccess( session, fileId, RepositoryFilePermission.DELETE ) ) {
      return;
  }
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    Serializable parentFolderId = JcrRepositoryFileUtils.getParentId( session, fileId );
    JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, parentFolderId );
    deleteHelper.deleteFile( session, pentahoJcrConstants, fileId );
    session.save();
    JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, parentFolderId,
        versionMessage );
      }

  public void deleteFileAtVersion( final Session session, final Serializable fileId, final Serializable versionId )
    throws RepositoryException {
    if ( !hasAccess( session, fileId, RepositoryFilePermission.DELETE ) ) {
      return;
  }
    Node fileToDeleteNode = session.getNodeByIdentifier( fileId.toString() );
    session.getWorkspace().getVersionManager().getVersionHistory( fileToDeleteNode.getPath() ).removeVersion(
        versionId.toString() );
    session.save();
  }

  public List<RepositoryFile> getDeletedFiles( final Session session, final String origParentFolderPath,
      final String filter ) throws RepositoryException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    return deleteHelper.getDeletedFiles( session, pentahoJcrConstants, origParentFolderPath, filter );
          }

  public void permanentlyDeleteFile( final Session session, final Serializable fileId, final String versionMessage )
    throws RepositoryException {
    if ( !hasAccess( session, fileId, RepositoryFilePermission.DELETE ) ) {
      return;
  }
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    deleteHelper.permanentlyDeleteFile( session, pentahoJcrConstants, fileId );
    session.save();
    }

  public List<RepositoryFile> getChildren( final Session session, final RepositoryRequest repositoryRequest )
    throws RepositoryException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        return JcrRepositoryFileUtils.getChildren( session, pentahoJcrConstants, pathConversionHelper, lockHelper,
            repositoryRequest );
      }

  @SuppressWarnings( "deprecation" )
  public List<RepositoryFile> getChildren( final Session session, final Serializable folderId, final String filter,
      final Boolean showHiddenFiles ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        return JcrRepositoryFileUtils.getChildren( session, pentahoJcrConstants, pathConversionHelper, lockHelper,
            folderId, filter, showHiddenFiles );
      }

  public <T extends IRepositoryFileData> IRepositoryFileData getData( final Session session, final Serializable fileId,
      final Serializable versionId, final Class<T> contentClass ) throws RepositoryException {
    if ( !hasAccess( session, fileId, RepositoryFilePermission.READ ) ) {
      return null;
    }
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    return JcrRepositoryFileUtils.getContent( session, pentahoJcrConstants, fileId, versionId, findTransformerForRead(
        JcrRepositoryFileUtils.getFileContentType( session, pentahoJcrConstants, fileId, versionId ), contentClass ) );
      }

  public List<RepositoryFile> getDeletedFiles( final Session session ) throws RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    return deleteHelper.getDeletedFiles( session, pentahoJcrConstants );
    }

  protected ITransformer<IRepositoryFileData> findTransformerForRead( final String contentType,
      final Class<? extends IRepositoryFileData> clazz ) {
    for ( ITransformer<IRepositoryFileData> transformer : transformers ) {
      if ( transformer.canRead( contentType, clazz ) ) {
        return transformer;
      }
  }
    throw new IllegalArgumentException( Messages.getInstance().getString(
        "JcrRepositoryFileDao.ERROR_0001_NO_TRANSFORMER" ) ); //$NON-NLS-1$
  }

  protected ITransformer<IRepositoryFileData>
    findTransformerForWrite( final Class<? extends IRepositoryFileData> clazz ) {
    for ( ITransformer<IRepositoryFileData> transformer : transformers ) {
      if ( transformer.canWrite( clazz ) ) {
        return transformer;
    }
    }
    throw new IllegalArgumentException( Messages.getInstance().getString(
        "JcrRepositoryFileDao.ERROR_0001_NO_TRANSFORMER" ) ); //$NON-NLS-1$
  }

  public RepositoryFile checkAndGetFileById( Session session, final Serializable fileId, final boolean loadMaps,
      final IPentahoLocale locale ) throws RepositoryException {
    RepositoryFile file = internalGetFileById( session, fileId, loadMaps, locale );
    if ( file != null ) {
      if ( !hasAccess( file, RepositoryFilePermission.READ ) ) {
        return null;
      }
  }
    return file;
      }

  private RepositoryFile internalGetFileById( Session session, final Serializable fileId, final boolean loadMaps,
      final IPentahoLocale locale ) throws RepositoryException {
    Assert.notNull( fileId );
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    RepositoryFile file =
        fileNode != null ? JcrRepositoryFileUtils.nodeToFile( session, pentahoJcrConstants, pathConversionHelper,
            lockHelper, fileNode, loadMaps, locale ) : null;
    return file;
      }

  public RepositoryFile getFileByRelPath( final Session session, final String relPath, final boolean loadMaps,
      final IPentahoLocale locale ) throws RepositoryException {
    String absPath = pathConversionHelper.relToAbs( relPath );
    return internalGetFile( session, absPath, loadMaps, locale );
    }

  public RepositoryFile internalGetFile( final Session session, final String absPath, final boolean loadMaps,
      final IPentahoLocale locale ) throws RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    try {
      Item fileNode = session.getItem( JcrStringHelper.pathEncode( absPath ) );
      if ( fileNode == null ) {
            return null;
          }
      // items are nodes or properties; this must be a node
      Assert.isTrue( fileNode.isNode() );
      RepositoryFile file =
          JcrRepositoryFileUtils.nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper,
              (Node) fileNode, loadMaps, locale );
      if ( file == null || !hasAccess( file, RepositoryFilePermission.READ ) ) {
        return null;
        }
      return file;
    } catch ( PathNotFoundException e ) {
        return null;
      }
  }

  private boolean isUser() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    String name = pentahoSession.getName();
    return name != null && !name.equals( repositoryAdminUsername );
    }

  private boolean hasAccess( Session session, Serializable fileId, RepositoryFilePermission... permissions )
    throws RepositoryException {
    if ( !isUser() ) {
      return true;
          }
    return hasUserAccess( internalGetFileById( session, fileId, false, null ), permissions );
        }

  private boolean hasAccess( RepositoryFile file, RepositoryFilePermission... permissions ) {
    if ( !isUser() ) {
      return true;
      }
    return hasUserAccess( file, permissions );
  }

  private boolean hasUserAccess( RepositoryFile file, RepositoryFilePermission... permissions ) {
    if ( ArrayUtils.isEmpty( permissions ) ) {
      return false;
      }

    RepositoryFileAcl acl = aclDao.getAcl( file.getId() );

          // Invoke accessVoterManager to see if we have access to perform this operation
    for ( RepositoryFilePermission permission : permissions ) {
      if ( !accessVoterManager.hasAccess( file, permission, acl, PentahoSessionHolder.getSession() ) ) {
        return false;
          }
        }
    return true;
      }

  public void internalCopyOrMove( Session session, final RepositoryFile file, final String destRelPath,
      final String versionMessage, final boolean copy ) throws RepositoryException, IOException {
    if ( !hasAccess( file, RepositoryFilePermission.WRITE ) ) {
      return;
    }
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        String destAbsPath = pathConversionHelper.relToAbs( destRelPath );
        String cleanDestAbsPath = destAbsPath;
        if ( cleanDestAbsPath.endsWith( RepositoryFile.SEPARATOR ) ) {
          cleanDestAbsPath.substring( 0, cleanDestAbsPath.length() - 1 );
        }
    Node srcFileNode = session.getNodeByIdentifier( file.getId().toString() );
    Serializable srcParentFolderId = JcrRepositoryFileUtils.getParentId( session, file.getId() );
        boolean appendFileName = false;
        boolean destExists = true;
        Node destFileNode = null;
        Node destParentFolderNode = null;
        try {
          destFileNode = (Node) session.getItem( JcrStringHelper.pathEncode( cleanDestAbsPath ) );
        } catch ( PathNotFoundException e ) {
          destExists = false;
        }
        if ( destExists ) {
          // make sure it's a file or folder
          Assert.isTrue( JcrRepositoryFileUtils.isSupportedNodeType( pentahoJcrConstants, destFileNode ) );
          // existing item; make sure src is not a folder if dest is a file
          Assert.isTrue(
              !( JcrRepositoryFileUtils.isPentahoFolder( pentahoJcrConstants, srcFileNode ) && JcrRepositoryFileUtils
                  .isPentahoFile( pentahoJcrConstants, destFileNode ) ), Messages.getInstance().getString(
                  "JcrRepositoryFileDao.ERROR_0002_CANNOT_OVERWRITE_FILE_WITH_FOLDER" ) ); //$NON-NLS-1$
          if ( JcrRepositoryFileUtils.isPentahoFolder( pentahoJcrConstants, destFileNode ) ) {
            // existing item; caller is not renaming file, only moving it
            appendFileName = true;
            destParentFolderNode = destFileNode;
          } else {
            // get parent of existing dest item
            int lastSlashIndex = cleanDestAbsPath.lastIndexOf( RepositoryFile.SEPARATOR );
            Assert.isTrue( lastSlashIndex > 1, Messages.getInstance().getString(
                "JcrRepositoryFileDao.ERROR_0003_ILLEGAL_DEST_PATH" ) ); //$NON-NLS-1$
            String absPathToDestParentFolder = cleanDestAbsPath.substring( 0, lastSlashIndex );
            destParentFolderNode = (Node) session.getItem( JcrStringHelper.pathEncode( absPathToDestParentFolder ) );
          }
        } else {
          // destination doesn't exist; go up one level to a folder that does exist
          int lastSlashIndex = cleanDestAbsPath.lastIndexOf( RepositoryFile.SEPARATOR );
          Assert.isTrue( lastSlashIndex > 1, Messages.getInstance().getString(
              "JcrRepositoryFileDao.ERROR_0003_ILLEGAL_DEST_PATH" ) ); //$NON-NLS-1$
          String absPathToDestParentFolder = cleanDestAbsPath.substring( 0, lastSlashIndex );
          // Not need to check the name if we encoded it
          // JcrRepositoryFileUtils.checkName( cleanDestAbsPath.substring( lastSlashIndex + 1 ) );
          try {
        destParentFolderNode = (Node) session.getItem( JcrStringHelper.pathEncode( absPathToDestParentFolder ) );
          } catch ( PathNotFoundException e1 ) {
        Assert.isTrue( false, Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0004_PARENT_MUST_EXIST" ) ); //$NON-NLS-1$
          }
          Assert.isTrue( JcrRepositoryFileUtils.isPentahoFolder( pentahoJcrConstants, destParentFolderNode ), Messages
              .getInstance().getString( "JcrRepositoryFileDao.ERROR_0005_PARENT_MUST_BE_FOLDER" ) ); //$NON-NLS-1$
        }
        if ( !copy ) {
      JcrRepositoryFileUtils
          .checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, srcParentFolderId );
        }
        JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary( session, pentahoJcrConstants,
            destParentFolderNode );
        String finalEncodedSrcAbsPath = srcFileNode.getPath();
    String finalDestAbsPath =
        appendFileName && !file.isFolder() ? cleanDestAbsPath + RepositoryFile.SEPARATOR + srcFileNode.getName()
            : cleanDestAbsPath;
        try {
          if ( copy ) {
            session.getWorkspace().copy( finalEncodedSrcAbsPath, JcrStringHelper.pathEncode( finalDestAbsPath ) );
          } else {
            session.getWorkspace().move( finalEncodedSrcAbsPath, JcrStringHelper.pathEncode( finalDestAbsPath ) );
          }
        } catch ( ItemExistsException iae ) {
          throw new UnifiedRepositoryException( ( file.isFolder() ? "Folder " : "File " ) + "with path ["
              + cleanDestAbsPath + "] already exists in the repository" );
        }

        JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary( session, pentahoJcrConstants,
            destParentFolderNode, versionMessage );
        // if it's a move within the same folder, then the next checkin is unnecessary
        if ( !copy && !destParentFolderNode.getIdentifier().equals( srcParentFolderId.toString() ) ) {
      JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, srcParentFolderId,
          versionMessage );
        }
        session.save();
      }

  protected RepositoryFile getReferrerFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Property referrerProperty ) throws RepositoryException {
    Node currentNode = referrerProperty.getParent();
    while ( !currentNode.isNodeType( pentahoJcrConstants.getPHO_NT_PENTAHOHIERARCHYNODE() ) ) {
      currentNode = currentNode.getParent();
  }
    // if folder, then referrer is a lock token record (under the user's home folder) which will be cleaned up by
    // lockHelper; ignore it
    if ( currentNode.isNodeType( pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER() ) ) {
        return null;
      }

    return JcrRepositoryFileUtils.nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper,
        currentNode );
      }

  public List<RepositoryFile> getReferrers( Session session, final Serializable fileId ) throws ItemNotFoundException,
    RepositoryException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );

        Node fileNode = session.getNodeByIdentifier( fileId.toString() );
        // guard against using a file retrieved from a more lenient session inside a more strict session
        Assert.notNull( fileNode );

        Set<RepositoryFile> referrers = new HashSet<RepositoryFile>();
        PropertyIterator refIter = fileNode.getReferences();
        if ( refIter.hasNext() ) {
          while ( refIter.hasNext() ) {
            // for each referrer property, march up the tree until we find the file node to which the property
            // belongs
            RepositoryFile referrer = getReferrerFile( session, pentahoJcrConstants, refIter.nextProperty() );
            if ( referrer != null ) {
              referrers.add( referrer );
            }
          }
        }
        session.save();
    return new ArrayList<RepositoryFile>( referrers );
      }

  public RepositoryFileTree getTree( Session session, final RepositoryRequest repositoryRequest )
    throws RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    String absPath = pathConversionHelper.relToAbs( repositoryRequest.getPath() );
    return JcrRepositoryFileUtils.getTree( session, pentahoJcrConstants, pathConversionHelper, lockHelper, absPath,
        repositoryRequest, accessVoterManager );
    }

  public boolean canUnlockFile( final Session session, final Serializable fileId ) throws ItemNotFoundException,
    RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    Lock lock = session.getWorkspace().getLockManager().getLock( fileNode.getPath() );
    return lockHelper.canUnlock( session, pentahoJcrConstants, lock );
  }

  public void restoreFileAtVersion( final Session session, final Serializable fileId, final Serializable versionId,
      final String versionMessage ) throws ItemNotFoundException, RepositoryException {
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    session.getWorkspace().getVersionManager().restore( fileNode.getPath(), versionId.toString(), true );
    }

  public List<Locale> getAvailableLocalesForFile( RepositoryFile repositoryFile ) {
    List<Locale> localeList = new ArrayList<Locale>();
    if ( repositoryFile != null && repositoryFile.getLocalePropertiesMap() != null ) {
      for ( String localeName : repositoryFile.getLocalePropertiesMap().keySet() ) {
        String[] localePieces = localeName.split( "_" );
        String language = localePieces[0];
        String country = ( localePieces.length > 1 ) ? localePieces[1] : "";
        String variant = ( localePieces.length > 2 ) ? localePieces[2] : "";
        Locale locale = new Locale( language, country, variant );
        localeList.add( locale );
      }
    }
    return localeList;
  }

  public Properties getLocalePropertiesForFile( RepositoryFile repositoryFile, String locale ) {
    if ( org.apache.commons.lang.StringUtils.isBlank( locale ) ) {
      locale = RepositoryFile.DEFAULT_LOCALE;
    }
    if ( repositoryFile != null && repositoryFile.getLocalePropertiesMap() != null ) {
      Properties properties = repositoryFile.getLocalePropertiesMap().get( locale );
      return properties;
    }
    return null;
  }

  public void setLocalePropertiesForFile( final Session session, final RepositoryFile repositoryFile,
      final String locale, final Properties properties ) throws RepositoryException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        String versionMessage =
        Messages.getInstance().getString( "JcrRepositoryFileDao.LOCALE_0001_UPDATE_PROPERTIES", repositoryFile.getId() );
        lockHelper.addLockTokenToSessionIfNecessary( session, pentahoJcrConstants, repositoryFile.getId() );
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, repositoryFile
            .getId() );
        JcrRepositoryFileUtils.updateFileLocaleProperties( session, repositoryFile.getId(), locale, properties );
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, repositoryFile
            .getId(), versionMessage );
        lockHelper.removeLockTokenFromSessionIfNecessary( session, pentahoJcrConstants, repositoryFile.getId() );
      }

  public void deleteLocalePropertiesForFile( final Session session, final RepositoryFile repositoryFile,
      final String locale ) throws RepositoryException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        String versionMessage =
        Messages.getInstance().getString( "JcrRepositoryFileDao.LOCALE_0002_DELETE_PROPERTIES", repositoryFile.getId() );
        lockHelper.addLockTokenToSessionIfNecessary( session, pentahoJcrConstants, repositoryFile.getId() );
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, repositoryFile
            .getId() );
        JcrRepositoryFileUtils.deleteFileLocaleProperties( session, repositoryFile.getId(), locale );
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, repositoryFile
            .getId(), versionMessage );
        lockHelper.removeLockTokenFromSessionIfNecessary( session, pentahoJcrConstants, repositoryFile.getId() );
      }

  public void lockFile( final Session session, final Serializable fileId, final String message )
    throws RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    lockHelper.lockFile( session, pentahoJcrConstants, fileId, message );
    }

  public void unlockFile( final Session session, final Serializable fileId ) throws RepositoryException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    lockHelper.unlockFile( session, pentahoJcrConstants, fileId );
      }

  public List<VersionSummary> getVersionSummaries( final Session session, final Serializable fileId )
    throws RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    return JcrRepositoryFileUtils.getVersionSummaries( session, pentahoJcrConstants, fileId, true );
      }

  public RepositoryFile getFile( final Session session, final Serializable fileId, final Serializable versionId )
    throws RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    return JcrRepositoryFileUtils.getFileAtVersion( session, pentahoJcrConstants, pathConversionHelper, lockHelper,
        fileId, versionId );
    }
  }
