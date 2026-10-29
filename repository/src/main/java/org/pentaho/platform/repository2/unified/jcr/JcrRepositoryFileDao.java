/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.repository2.unified.jcr;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.TreeNodeFilterSpec;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * CRUD operations against JCR. Note that there is no access control in this class (implicit or explicit).
 *
 * @author mlowery
 */
public class JcrRepositoryFileDao implements IRepositoryFileDao {
  private static final Log logger = LogFactory.getLog( JcrRepositoryFileDao.class );

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================
  private JcrTemplate jcrTemplate;

  private List<ITransformer<IRepositoryFileData>> transformers;

  private ILockHelper lockHelper;

  private IDeleteHelper deleteHelper;

  private IPathConversionHelper pathConversionHelper;

  private IRepositoryFileAclDao aclDao;

  private IRepositoryDefaultAclHandler defaultAclHandler;

  private IRepositoryAccessVoterManager accessVoterManager;

  // ~ Constructors
  // ====================================================================================================

  public JcrRepositoryFileDao( final JcrTemplate jcrTemplate,
      final List<ITransformer<IRepositoryFileData>> transformers, final ILockHelper lockHelper,
      final IDeleteHelper deleteHelper, final IPathConversionHelper pathConversionHelper,
      final IRepositoryFileAclDao aclDao, final IRepositoryDefaultAclHandler defaultAclHandler ) {
    super();
    Assert.notNull( jcrTemplate, "JCR Template must not be null" );
    Assert.notNull( transformers, "Transformers list must not be null" );
    this.jcrTemplate = jcrTemplate;
    this.transformers = transformers;
    this.lockHelper = lockHelper;
    this.deleteHelper = deleteHelper;
    this.pathConversionHelper = pathConversionHelper;
    this.aclDao = aclDao;
    this.defaultAclHandler = defaultAclHandler;
  }

  public JcrRepositoryFileDao( final JcrTemplate jcrTemplate,
      final List<ITransformer<IRepositoryFileData>> transformers, final ILockHelper lockHelper,
      final IDeleteHelper deleteHelper, final IPathConversionHelper pathConversionHelper,
      final IRepositoryFileAclDao aclDao, final IRepositoryDefaultAclHandler defaultAclHandler,
      final IRepositoryAccessVoterManager accessVoterManager ) {
    this( jcrTemplate, transformers, lockHelper, deleteHelper, pathConversionHelper, aclDao, defaultAclHandler );
    this.accessVoterManager = accessVoterManager;
  }

  // ~ Methods
  // =========================================================================================================

  private RepositoryFile internalCreateFolder( final Session session, final Serializable parentFolderId,
      final RepositoryFile folder, final RepositoryFileAcl acl, final String versionMessage )
    throws RepositoryException {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    // Get repository file info and acl info of parent
    if ( parentFolderId != null ) {
      RepositoryFile parentRepositoryFile = getFileById( parentFolderId );
      if ( parentRepositoryFile != null ) {
        RepositoryFileAcl parentAcl = aclDao.getAcl( parentRepositoryFile.getId() );
        // Invoke accessVoterManager to see if we have access to perform this operation
        if ( !accessVoterManager.hasAccess( parentRepositoryFile, RepositoryFilePermission.WRITE, parentAcl,
            PentahoSessionHolder.getSession() ) ) {
          return null;
        }
      }
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

  private RepositoryFile internalCreateFile( final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData content, final RepositoryFileAcl acl, final String versionMessage ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    /*
     * PPP-3049: Changed the Assert.notNull(content) to code that creates a file with a single blank when the assert
     * WOULD have been triggered.
     */
    Assert.notNull( file, "File must not be null" );
    Assert.isTrue( !file.isFolder(), "File must not be a folder" );

    if ( !hasWriteAccessToParent( parentFolderId ) ) {
      return null;
    }
    // Assert.notNull(content);
    DataNode emptyDataNode = new DataNode( file.getName() );
    emptyDataNode.setProperty( " ", "content" ); //$NON-NLS-1$ //$NON-NLS-2$
    final IRepositoryFileData emptyContent = new NodeRepositoryFileData( emptyDataNode );

    JcrRepositoryFileUtils.FileContentLockLease treeWriteLock =
        JcrRepositoryFileUtils.acquireFileContentTreeWriteLock();
    try {
      return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
        @Override
        public Object doInJcr( final Session session ) throws RepositoryException, IOException {
          return createFileInSession( session, parentFolderId, file, content == null ? emptyContent : content, acl,
              versionMessage );
        }
      } );
    } finally {
      treeWriteLock.close();
    }
  }

  private boolean hasWriteAccessToParent( final Serializable parentFolderId ) {
    if ( parentFolderId == null ) {
      return true;
    }
    RepositoryFile parentRepositoryFile = getFileById( parentFolderId );
    if ( parentRepositoryFile == null ) {
      return true;
    }
    RepositoryFileAcl parentAcl = aclDao.getAcl( parentRepositoryFile.getId() );
    return accessVoterManager.hasAccess( parentRepositoryFile, RepositoryFilePermission.WRITE, parentAcl,
        PentahoSessionHolder.getSession() );
  }

  private RepositoryFile createFileInSession( final Session session, final Serializable parentFolderId,
      final RepositoryFile file, final IRepositoryFileData content, final RepositoryFileAcl acl,
      final String versionMessage ) throws RepositoryException, IOException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    List<Serializable> lockIds = new ArrayList<>();
    if ( parentFolderId != null ) {
      lockIds.addAll( JcrRepositoryFileUtils.getFileContentWriteLockIds( session, pentahoJcrConstants,
          parentFolderId ) );
    }
    RepositoryFile[] createdFile = new RepositoryFile[ 1 ];
    JcrRepositoryFileUtils.withFileContentWriteLocks( lockIds, () -> {
      JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants,
          parentFolderId );
      Node fileNode = JcrRepositoryFileUtils.createFileNode( session, pentahoJcrConstants, parentFolderId, file,
          content, findTransformerForWrite( content.getClass() ) );
      String path = JcrRepositoryFileUtils.getAbsolutePath( session, pentahoJcrConstants, fileNode );
      RepositoryFile tmpFile = new RepositoryFile.Builder( file ).path( path ).build();
      aclDao.createAcl( fileNode.getIdentifier(), acl == null ? defaultAclHandler.createDefaultAcl( tmpFile ) : acl );
      session.save();
      if ( file.isVersioned() ) {
        JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary( session, pentahoJcrConstants, fileNode,
            versionMessage, file.getCreatedDate(), false );
      }
      JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, parentFolderId,
          Messages.getInstance().getString( "JcrRepositoryFileDao.USER_0002_VER_COMMENT_ADD_FILE", file.getName(),
              ( parentFolderId == null ? "root" : parentFolderId.toString() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
      createdFile[ 0 ] = JcrRepositoryFileUtils.nodeToFile( session, pentahoJcrConstants, pathConversionHelper,
          lockHelper, fileNode );
    } );
    return createdFile[ 0 ];
  }

  private RepositoryFile internalUpdateFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final RepositoryFile file, final IRepositoryFileData content, final String versionMessage )
    throws RepositoryException {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    Assert.notNull( file, "File must not be null" );
    Assert.isTrue( !file.isFolder(), "File must not be a folder" );
    Assert.notNull( content, "Content must not be null" );
    // Get repository file info and acl info of parent
    RepositoryFileAcl acl = aclDao.getAcl( file.getId() );
    // Invoke accessVoterManager to see if we have access to perform this operation
    if ( !accessVoterManager.hasAccess( file, RepositoryFilePermission.WRITE, acl, PentahoSessionHolder.getSession() ) ) {
      return null;
    }
    lockHelper.addLockTokenToSessionIfNecessary( session, pentahoJcrConstants, file.getId() );
    JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, file.getId() );
    JcrRepositoryFileUtils.updateFileNode( session, pentahoJcrConstants, file, content,
        findTransformerForWrite( content.getClass() ) );
    session.save();
    JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, file.getId(),
        versionMessage, file.getCreatedDate() != null ? file.getCreatedDate() : new java.util.Date(), true );
    lockHelper.removeLockTokenFromSessionIfNecessary( session, pentahoJcrConstants, file.getId() );
    return JcrRepositoryFileUtils.nodeIdToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper, file
        .getId() );
  }

  private RepositoryFile internalUpdateFolder( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final RepositoryFile folder, final String versionMessage ) throws RepositoryException {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    Assert.notNull( folder, "Folder must not be null" );
    Assert.isTrue( folder.isFolder(), "Folder must be a directory" );
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

  protected ITransformer<IRepositoryFileData> findTransformerForWrite(
    Class<? extends IRepositoryFileData> clazz ) {

    for ( ITransformer<IRepositoryFileData> transformer : transformers ) {
      if ( transformer.canWrite( clazz ) ) {
        return transformer;
      }
    }
    throw new IllegalArgumentException( Messages.getInstance().getString(
      "JcrRepositoryFileDao.ERROR_0001_NO_TRANSFORMER" ) ); //$NON-NLS-1$
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile createFile( final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData content, final RepositoryFileAcl acl, final String versionMessage ) {
    return internalCreateFile( parentFolderId, file, content, acl, versionMessage );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile createFolder( final Serializable parentFolderId, final RepositoryFile folder,
      final RepositoryFileAcl acl, final String versionMessage ) {
    Assert.notNull( folder, "Folder must not be null" );
    Assert.isTrue( folder.isFolder(), "Folder must be a directory" );

    JcrRepositoryFileUtils.FileContentLockLease treeWriteLock =
        JcrRepositoryFileUtils.acquireFileContentTreeWriteLock();
    try {
      return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        List<Serializable> lockIds = new ArrayList<>();
        if ( parentFolderId != null ) {
          lockIds.addAll( JcrRepositoryFileUtils.getFileContentWriteLockIds( session, pentahoJcrConstants,
              parentFolderId ) );
        }
        RepositoryFile[] createdFolder = new RepositoryFile[ 1 ];
        JcrRepositoryFileUtils.withFileContentWriteLocks( lockIds, () ->
          createdFolder[ 0 ] = internalCreateFolder( session, parentFolderId, folder, acl, versionMessage ) );
        return createdFolder[ 0 ];
      }
      } );
    } finally {
      treeWriteLock.close();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile getFileById( final Serializable fileId ) {
    return internalGetFileById( fileId, false, null );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile getFileById( final Serializable fileId, final boolean loadMaps ) {
    return internalGetFileById( fileId, loadMaps, null );
  }

  @Override
  public RepositoryFile getFile( final String relPath, final IPentahoLocale locale ) {
    return getFile( relPath, false, locale );
  }

  @Override
  public RepositoryFile getFileById( Serializable fileId, IPentahoLocale locale ) {
    return internalGetFileById( fileId, false, locale );
  }

  @Override
  public RepositoryFile getFile( final String relPath, final boolean loadLocaleMaps, final IPentahoLocale locale ) {
    Assert.hasText( relPath, "Relative path must not be null or empty" );
    Assert.isTrue( relPath.startsWith( RepositoryFile.SEPARATOR ), "Relative path must start with a separator" );

    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        String absPath = pathConversionHelper.relToAbs( relPath );
        return internalGetFile( session, absPath, loadLocaleMaps, locale );
      }
    } );
  }

  @Override
  public RepositoryFile getFileById( Serializable fileId, boolean loadLocaleMaps, IPentahoLocale locale ) {
    return internalGetFileById( fileId, loadLocaleMaps, locale );
  }

  private RepositoryFile internalGetFileById( final Serializable fileId, final boolean loadMaps,
      final IPentahoLocale locale ) {
    Assert.notNull( fileId, "File ID must not be null" );
    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        Node fileNode;
        try {
          fileNode = session.getNodeByIdentifier( fileId.toString() );
        } catch ( ItemNotFoundException e ) {
          logger.info( "Couldn't find file by id: " + fileId );
          fileNode = null;
        }
        RepositoryFile file =
            fileNode != null ? JcrRepositoryFileUtils.nodeToFile( session, pentahoJcrConstants, pathConversionHelper,
                lockHelper, fileNode, loadMaps, locale ) : null;
        if ( file != null ) {
          RepositoryFileAcl acl = aclDao.getAcl( file.getId() );
          // Invoke accessVoterManager to see if we have access to perform this operation
          if ( !accessVoterManager.hasAccess( file, RepositoryFilePermission.READ, acl, PentahoSessionHolder
              .getSession() ) ) {
            return null;
          }
        }
        return file;
      }
    } );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile getFile( final String relPath ) {
    return getFile( relPath, false, null );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile getFileByAbsolutePath( final String absPath ) {
    Assert.hasText( absPath, "Absolute path must not be null or empty" );
    Assert.isTrue( absPath.startsWith( RepositoryFile.SEPARATOR ), "Absolute path must start with a separator" );
    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        return internalGetFile( session, absPath, false, null );
      }
    } );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile getFile( final String relPath, final boolean loadMaps ) {
    Assert.hasText( relPath, "Relative path must not be null or empty" );
    Assert.isTrue( relPath.startsWith( RepositoryFile.SEPARATOR ), "Relative path must start with a separator" );
    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        String absPath = pathConversionHelper.relToAbs( relPath );
        return internalGetFile( session, absPath, loadMaps, null );
      }
    } );
  }

  private RepositoryFile internalGetFile( final Session session, final String absPath, final boolean loadMaps,
      final IPentahoLocale locale ) throws RepositoryException {

    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    Item fileNode;
    try {
      fileNode = session.getItem( JcrStringHelper.pathEncode( absPath ) );
      // items are nodes or properties; this must be a node
      Assert.isTrue( fileNode.isNode(), "The item at the specified path must be a node, but it is not." );
    } catch ( PathNotFoundException e ) {
      fileNode = null;
    }
    RepositoryFile file =
        fileNode != null ? JcrRepositoryFileUtils.nodeToFile( session, pentahoJcrConstants, pathConversionHelper,
            lockHelper, (Node) fileNode, loadMaps, locale ) : null;
    if ( file != null ) {
      RepositoryFileAcl acl = aclDao.getAcl( file.getId() );
      // Invoke accessVoterManager to see if we have access to perform this operation
      if ( !accessVoterManager.hasAccess( file, RepositoryFilePermission.READ, acl, PentahoSessionHolder.getSession() ) ) {
        return null;
      }
    }
    return file;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public <T extends IRepositoryFileData> T getData( final Serializable fileId, final Serializable versionId,
      final Class<T> contentClass ) {
    Assert.notNull( fileId, "File ID must not be null" );
    return (T) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        java.util.concurrent.locks.Lock contentReadLock =
            JcrRepositoryFileUtils.getFileContentLock( fileId ).readLock();
        contentReadLock.lock();
        IRepositoryFileData data;
        try {
          data = JcrRepositoryFileUtils.getContent( session, pentahoJcrConstants, fileId, versionId,
              findTransformerForRead(
                  JcrRepositoryFileUtils.getFileContentType( session, pentahoJcrConstants, fileId, versionId ),
                  contentClass ) );
        } finally {
          contentReadLock.unlock();
        }
        if ( fileId != null ) {
          RepositoryFile file = internalGetFileById( fileId, false, null );
          if ( file != null ) {
            RepositoryFileAcl acl = aclDao.getAcl( fileId );
            // Invoke accessVoterManager to see if we have access to perform this operation
            if ( !accessVoterManager.hasAccess( file, RepositoryFilePermission.READ, acl, PentahoSessionHolder
                .getSession() ) ) {
              return null;
            }
          }
        }
        return data;
      }
    } );

  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public List<RepositoryFile> getChildren( final RepositoryRequest repositoryRequest ) {
    Assert.notNull( repositoryRequest.getPath(), "Repository request path must not be null" );
    // the structured filter only applies to tree requests; a children request degrades it to "everything"
    TreeNodeFilterSpec.applyFallback( repositoryRequest );

    return (List<RepositoryFile>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        return JcrRepositoryFileUtils.getChildren( session, pentahoJcrConstants, pathConversionHelper, lockHelper,
            repositoryRequest );
      }
    } );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public List<RepositoryFile> getChildren( final Serializable folderId, final String filter,
      final Boolean showHiddenFiles ) {
    Assert.notNull( folderId, "Folder ID must not be null" );
    return (List<RepositoryFile>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        return JcrRepositoryFileUtils.getChildren( session, pentahoJcrConstants, pathConversionHelper, lockHelper,
            folderId, filter, showHiddenFiles );
      }
    } );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile updateFile( final RepositoryFile file, final IRepositoryFileData content,
      final String versionMessage ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    Assert.notNull( file,"File must not be null" );
    Assert.isTrue( !file.isFolder(), "The provided file must not be a folder" );
    JcrRepositoryFileUtils.FileContentLockLease treeWriteLock =
        JcrRepositoryFileUtils.acquireFileContentTreeWriteLock();
    try {
      return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
        @Override
        public Object doInJcr( final Session session ) throws RepositoryException, IOException {
          PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
          List<Serializable> fileIds = JcrRepositoryFileUtils.getFileContentWriteLockIds( session,
              pentahoJcrConstants, file.getId() );
          RepositoryFile[] updatedFile = new RepositoryFile[ 1 ];
          JcrRepositoryFileUtils.withFileContentWriteLocks( fileIds, () ->
            updatedFile[ 0 ] = internalUpdateFile( session, pentahoJcrConstants, file, content, versionMessage ) );
          return updatedFile[ 0 ];
        }
      } );
    } finally {
      treeWriteLock.close();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void lockFile( final Serializable fileId, final String message ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    Assert.notNull( fileId, "File ID must not be null" );
    jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        lockHelper.lockFile( session, pentahoJcrConstants, fileId, message );
        return null;
      }
    } );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unlockFile( final Serializable fileId ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    Assert.notNull( fileId, "File ID must not be null" );
    jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        lockHelper.unlockFile( session, pentahoJcrConstants, fileId );
        return null;
      }
    } );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public List<VersionSummary> getVersionSummaries( final Serializable fileId ) {
    Assert.notNull( fileId, "File ID must not be null" );
    java.util.concurrent.locks.Lock contentReadLock =
        JcrRepositoryFileUtils.getFileContentLock( fileId ).readLock();
    contentReadLock.lock();
    try {
      return (List<VersionSummary>) jcrTemplate.execute( new JcrCallback() {
        @Override
        public Object doInJcr( final Session session ) throws RepositoryException, IOException {
          PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
          return JcrRepositoryFileUtils.getVersionSummaries( session, pentahoJcrConstants, fileId, true );
        }
      } );
    } finally {
      contentReadLock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFile getFile( final Serializable fileId, final Serializable versionId ) {
    Assert.notNull( fileId, "File ID must not be null" );
    Assert.notNull( versionId, "Version ID must not be null" );
    java.util.concurrent.locks.Lock contentReadLock =
        JcrRepositoryFileUtils.getFileContentLock( fileId ).readLock();
    contentReadLock.lock();
    try {
      return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
        @Override
        public Object doInJcr( final Session session ) throws RepositoryException, IOException {
          PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
          return JcrRepositoryFileUtils.getFileAtVersion( session, pentahoJcrConstants, pathConversionHelper,
              lockHelper, fileId, versionId );
        }
      } );
    } finally {
      contentReadLock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteFile( final Serializable fileId, final String versionMessage ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    Assert.notNull( fileId, "File ID must not be null" );
    JcrRepositoryFileUtils.FileContentLockLease treeWriteLock =
        JcrRepositoryFileUtils.acquireFileContentTreeWriteLock();
    try {
      jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        RepositoryFile fileToBeDeleted = getFileById( fileId );
        // Get repository file info and acl info of parent
        if ( fileToBeDeleted != null ) {
          RepositoryFileAcl toBeDeletedFileAcl = aclDao.getAcl( fileToBeDeleted.getId() );
          // Invoke accessVoterManager to see if we have access to perform this operation
          if ( !accessVoterManager.hasAccess( fileToBeDeleted, RepositoryFilePermission.DELETE, toBeDeletedFileAcl,
                  PentahoSessionHolder.getSession() ) ) {
            return null;
          }

          List<RepositoryFilePermission> perms = new ArrayList<RepositoryFilePermission>();
          perms.add( RepositoryFilePermission.DELETE );
          if ( !aclDao.hasAccess( fileToBeDeleted.getPath(), EnumSet.copyOf( perms ) ) ) {
            throw new AccessDeniedException( Messages.getInstance().getString(
                    "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED_DELETE", fileId ) );
          }
        }
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        Serializable parentFolderId = JcrRepositoryFileUtils.getParentId( session, fileId );
        List<Serializable> lockIds = new ArrayList<>();
        collectPentahoFileIds( pentahoJcrConstants, session.getNodeByIdentifier( fileId.toString() ), lockIds );
        lockIds.addAll( JcrRepositoryFileUtils.getFileContentWriteLockIds( session, pentahoJcrConstants, fileId ) );
        if ( parentFolderId != null ) {
          lockIds.addAll( JcrRepositoryFileUtils.getFileContentWriteLockIds( session, pentahoJcrConstants,
            parentFolderId ) );
        }
        JcrRepositoryFileUtils.withFileContentWriteLocks( lockIds, () -> {
          JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants,
            parentFolderId );
          deleteHelper.deleteFile( session, pentahoJcrConstants, fileId );
          session.save();
          JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants,
            parentFolderId, versionMessage );
        } );
        return null;
      }
      } );
    } finally {
      treeWriteLock.close();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteFileAtVersion( final Serializable fileId, final Serializable versionId ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    Assert.notNull( fileId, "File ID must not be null" );
    Assert.notNull( versionId, "Version ID must not be null" );
    JcrRepositoryFileUtils.FileContentLockLease treeWriteLock =
      JcrRepositoryFileUtils.acquireFileContentTreeWriteLock();
    JcrRepositoryFileUtils.FileContentLockLease contentWriteLock =
      JcrRepositoryFileUtils.acquireFileContentWriteLock( fileId );
    try {
      jcrTemplate.execute( new JcrCallback() {
        @Override
        public Object doInJcr( final Session session ) throws RepositoryException, IOException {
          RepositoryFile fileToBeDeleted = getFileById( fileId );
          // Get repository file info and acl info of parent
          if ( fileToBeDeleted != null ) {
            RepositoryFileAcl toBeDeletedFileAcl = aclDao.getAcl( fileToBeDeleted.getId() );
            // Invoke accessVoterManager to see if we have access to perform this operation
            if ( !accessVoterManager.hasAccess( fileToBeDeleted, RepositoryFilePermission.DELETE, toBeDeletedFileAcl,
                PentahoSessionHolder.getSession() ) ) {
              return null;
            }
          }
          Node fileToDeleteNode = session.getNodeByIdentifier( fileId.toString() );
          session.getWorkspace().getVersionManager().getVersionHistory( fileToDeleteNode.getPath() ).removeVersion(
              versionId.toString() );
          session.save();
          return null;
        }
      } );
    } finally {
      contentWriteLock.close();
      treeWriteLock.close();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public List<RepositoryFile> getDeletedFiles( final String origParentFolderPath, final String filter ) {
    Assert.hasLength( origParentFolderPath, "Original parent folder path must not be null or empty" );
    return (List<RepositoryFile>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        return deleteHelper.getDeletedFiles( session, pentahoJcrConstants, origParentFolderPath, filter );
      }
    } );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public List<RepositoryFile> getDeletedFiles() {
    return (List<RepositoryFile>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        return deleteHelper.getDeletedFiles( session, pentahoJcrConstants );
      }
    } );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public List<RepositoryFile> getAllDeletedFiles() {
    return (List<RepositoryFile>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        return deleteHelper.getAllDeletedFiles( session, pentahoJcrConstants );
      }
    } );
  }

  /**
   * {@inheritDoc}
   * <p/>
   * <p>
   * No checkout needed as .trash is not versioned.
   * </p>
   */
  @Override
  public void permanentlyDeleteFile( final Serializable fileId, final String versionMessage ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    Assert.notNull( fileId, "File ID must not be null" );
    JcrRepositoryFileUtils.FileContentLockLease treeWriteLock =
        JcrRepositoryFileUtils.acquireFileContentTreeWriteLock();
    try {
      jcrTemplate.execute( new JcrCallback() {
        @Override
        public Object doInJcr( final Session session ) throws RepositoryException, IOException {
          if ( hasDeleteAccess( fileId ) ) {
            permanentlyDeleteInSession( session, fileId );
          }
          return null;
        }
      } );
    } finally {
      treeWriteLock.close();
    }
  }

  private boolean hasDeleteAccess( final Serializable fileId ) {
    RepositoryFile fileToBeDeleted = getFileById( fileId );
    if ( fileToBeDeleted == null ) {
      return true;
    }
    RepositoryFileAcl toBeDeletedFileAcl = aclDao.getAcl( fileToBeDeleted.getId() );
    return accessVoterManager.hasAccess( fileToBeDeleted, RepositoryFilePermission.DELETE, toBeDeletedFileAcl,
        PentahoSessionHolder.getSession() );
  }

  private void permanentlyDeleteInSession( final Session session, final Serializable fileId )
    throws RepositoryException, IOException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    List<Serializable> fileIds = new ArrayList<>();
    collectPentahoFileIds( pentahoJcrConstants, session.getNodeByIdentifier( fileId.toString() ), fileIds );
    if ( fileIds.isEmpty() ) {
      fileIds.add( fileId );
    }
    JcrRepositoryFileUtils.withFileContentWriteLocks( fileIds, () -> {
      deleteHelper.permanentlyDeleteFile( session, pentahoJcrConstants, fileId );
      session.save();
    } );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void undeleteFile( final Serializable fileId, final String versionMessage ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    Assert.notNull( fileId, "File ID must not be null" );
    JcrRepositoryFileUtils.FileContentLockLease treeWriteLock =
        JcrRepositoryFileUtils.acquireFileContentTreeWriteLock();
    try {
      jcrTemplate.execute( new JcrCallback() {
        @Override
        public Object doInJcr( final Session session ) throws RepositoryException, IOException {
          undeleteInSession( session, fileId, versionMessage );
          return null;
        }
      } );
    } finally {
      treeWriteLock.close();
    }
  }

  private void undeleteInSession( final Session session, final Serializable fileId, final String versionMessage )
    throws RepositoryException, IOException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    String absOrigParentFolderPath = deleteHelper.getOriginalParentFolderPath( session, pentahoJcrConstants, fileId );
    RepositoryFile file = getFileById( fileId );
    RepositoryFileAcl acl = aclDao.getAcl( fileId );
    if ( !accessVoterManager.hasAccess( file, RepositoryFilePermission.WRITE, acl, PentahoSessionHolder
        .getSession() ) ) {
      return;
    }
    final Serializable restoredParentFolderId = resolveOrCreateFolder( session, absOrigParentFolderPath );
    List<Serializable> lockIds = new ArrayList<>();
    collectPentahoFileIds( pentahoJcrConstants, session.getNodeByIdentifier( fileId.toString() ), lockIds );
    lockIds.addAll( JcrRepositoryFileUtils.getFileContentWriteLockIds( session, pentahoJcrConstants, fileId ) );
    if ( restoredParentFolderId != null ) {
      lockIds.addAll( JcrRepositoryFileUtils.getFileContentWriteLockIds( session, pentahoJcrConstants,
          restoredParentFolderId ) );
    }
    JcrRepositoryFileUtils.withFileContentWriteLocks( lockIds, () -> {
      JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants,
          restoredParentFolderId );
      deleteHelper.undeleteFile( session, pentahoJcrConstants, fileId );
      session.save();
      JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants,
          restoredParentFolderId, versionMessage );
    } );
  }

  // The original parent folder path may no longer exist, so missing segments are created.
  private Serializable resolveOrCreateFolder( final Session session, final String absFolderPath )
    throws RepositoryException {
    if ( session.itemExists( JcrStringHelper.pathEncode( absFolderPath ) ) ) {
      return ( (Node) session.getItem( JcrStringHelper.pathEncode( absFolderPath ) ) ).getIdentifier();
    }
    String[] segments = pathConversionHelper.absToRel( absFolderPath ).split( RepositoryFile.SEPARATOR );
    RepositoryFile lastParentFolder =
        internalGetFile( session, ServerRepositoryPaths.getTenantRootFolderPath(), false, null );
    if ( lastParentFolder == null ) {
      throw new RepositoryException( "Tenant root folder not found" );
    }
    for ( String segment : segments ) {
      if ( StringUtils.hasLength( segment ) ) {
        lastParentFolder = getOrCreateChildFolder( session, lastParentFolder, segment );
      }
    }
    return lastParentFolder.getId();
  }

  private RepositoryFile getOrCreateChildFolder( final Session session, final RepositoryFile parent,
      final String segment ) throws RepositoryException {
    RepositoryFile existing =
        internalGetFile( session, pathConversionHelper
            .relToAbs( ( parent.getPath().equals( RepositoryFile.SEPARATOR )
                ? "" : parent.getPath() ) + RepositoryFile.SEPARATOR + segment ), false, null ); //$NON-NLS-1$
    if ( existing != null ) {
      return existing;
    }
    RepositoryFile created = internalCreateFolder( session, parent.getId(), new RepositoryFile.Builder( segment )
        .folder( true ).build(), defaultAclHandler.createDefaultAcl( parent ), null );
    if ( created == null ) {
      throw new AccessDeniedException( Messages.getInstance().getString(
          "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED_CREATE", parent.getId() ) );
    }
    return created;
  }

  private void internalCopyOrMove( final Serializable fileId, final String destRelPath, final String versionMessage,
      final boolean copy ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    Assert.notNull( fileId, "File ID must not be null" );
    JcrRepositoryFileUtils.FileContentLockLease treeWriteLock =
        JcrRepositoryFileUtils.acquireFileContentTreeWriteLock();
    try {
      jcrTemplate.execute( new JcrCallback() {
        @Override
        public Object doInJcr( final Session session ) throws RepositoryException, IOException {
          copyOrMoveInSession( session, fileId, destRelPath, versionMessage, copy );
          return null;
        }
      } );
    } finally {
      treeWriteLock.close();
    }
  }

  private void copyOrMoveInSession( final Session session, final Serializable fileId, final String destRelPath,
      final String versionMessage, final boolean copy ) throws RepositoryException, IOException {
    RepositoryFile file = getFileById( fileId );
    checkCopyOrMoveAccess( file, fileId, destRelPath, copy );

    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    String destAbsPath = pathConversionHelper.relToAbs( destRelPath );
    String cleanDestAbsPath = destAbsPath;
    if ( cleanDestAbsPath.endsWith( RepositoryFile.SEPARATOR ) ) {
      cleanDestAbsPath.substring( 0, cleanDestAbsPath.length() - 1 );
    }
    Node srcFileNode = session.getNodeByIdentifier( fileId.toString() );
    Serializable srcParentFolderId = JcrRepositoryFileUtils.getParentId( session, fileId );
    Node destFileNode = getNodeIfExists( session, cleanDestAbsPath );
    final boolean appendFileName =
        destFileNode != null && JcrRepositoryFileUtils.isPentahoFolder( pentahoJcrConstants, destFileNode );
    final Node destParentFolderNode =
        resolveDestParentFolder( session, pentahoJcrConstants, srcFileNode, destFileNode, cleanDestAbsPath );

    List<Serializable> lockIds = new ArrayList<>();
    collectPentahoFileIds( pentahoJcrConstants, srcFileNode, lockIds );
    if ( srcParentFolderId != null ) {
      lockIds.addAll( JcrRepositoryFileUtils.getFileContentWriteLockIds( session, pentahoJcrConstants,
          srcParentFolderId ) );
    }
    lockIds.addAll( JcrRepositoryFileUtils.getFileContentWriteLockIds( session, pentahoJcrConstants,
        destParentFolderNode.getIdentifier() ) );
    JcrRepositoryFileUtils.withFileContentWriteLocks( lockIds, () -> {
      if ( !copy ) {
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants,
            srcParentFolderId );
      }
      JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary( session, pentahoJcrConstants,
          destParentFolderNode );
      copyOrMoveNode( session, srcFileNode.getPath(),
          encodedDestPath( srcFileNode, cleanDestAbsPath, appendFileName ), cleanDestAbsPath, file, copy );
      JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary( session, pentahoJcrConstants,
          destParentFolderNode, versionMessage );
      if ( !copy && !destParentFolderNode.getIdentifier().equals( srcParentFolderId.toString() ) ) {
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants,
            srcParentFolderId, versionMessage );
      }
      session.save();
    } );
  }

  // When moving, the user must be able to remove the file from its location; the destination folder must be writable.
  private void checkCopyOrMoveAccess( final RepositoryFile file, final Serializable fileId, final String destRelPath,
      final boolean copy ) throws AccessDeniedException {
    if ( !copy ) {
      RepositoryFileAcl acl = aclDao.getAcl( fileId );
      if ( !accessVoterManager.hasAccess( file, RepositoryFilePermission.WRITE, acl,
          PentahoSessionHolder.getSession() ) ) {
        throw new AccessDeniedException( Messages.getInstance().getString(
          "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED_DELETE", fileId ) );
      }
    }
    RepositoryFile destFolder = getFile( destRelPath );
    if ( destFolder != null ) {
      RepositoryFileAcl destFolderAcl = aclDao.getAcl( destFolder.getId() );
      if ( !accessVoterManager.hasAccess( destFolder, RepositoryFilePermission.WRITE, destFolderAcl,
          PentahoSessionHolder.getSession() ) ) {
        throw new AccessDeniedException( Messages.getInstance().getString(
          "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED_CREATE", destFolder.getId() ) );
      }
    }
  }

  private Node getNodeIfExists( final Session session, final String absPath ) throws RepositoryException {
    try {
      return (Node) session.getItem( JcrStringHelper.pathEncode( absPath ) );
    } catch ( PathNotFoundException e ) {
      return null;
    }
  }

  private Node resolveDestParentFolder( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node srcFileNode, final Node destFileNode, final String cleanDestAbsPath ) throws RepositoryException {
    if ( destFileNode == null ) {
      // destination doesn't exist; go up one level to a folder that does exist
      Node parent = getNodeIfExists( session, parentPath( cleanDestAbsPath ) );
      Assert.isTrue( parent != null, Messages.getInstance()
          .getString( "JcrRepositoryFileDao.ERROR_0004_PARENT_MUST_EXIST" ) ); //$NON-NLS-1$
      Assert.isTrue( JcrRepositoryFileUtils.isPentahoFolder( pentahoJcrConstants, parent ), Messages
          .getInstance().getString( "JcrRepositoryFileDao.ERROR_0005_PARENT_MUST_BE_FOLDER" ) ); //$NON-NLS-1$
      return parent;
    }
    // make sure it's a file or folder
    Assert.isTrue( JcrRepositoryFileUtils.isSupportedNodeType( pentahoJcrConstants, destFileNode ), "" );
    // existing item; make sure src is not a folder if dest is a file
    Assert.isTrue(
        !( JcrRepositoryFileUtils.isPentahoFolder( pentahoJcrConstants, srcFileNode ) && JcrRepositoryFileUtils
            .isPentahoFile( pentahoJcrConstants, destFileNode ) ), Messages.getInstance().getString(
            "JcrRepositoryFileDao.ERROR_0002_CANNOT_OVERWRITE_FILE_WITH_FOLDER" ) ); //$NON-NLS-1$
    if ( JcrRepositoryFileUtils.isPentahoFolder( pentahoJcrConstants, destFileNode ) ) {
      // existing item; caller is not renaming file, only moving it
      return destFileNode;
    }
    return (Node) session.getItem( JcrStringHelper.pathEncode( parentPath( cleanDestAbsPath ) ) );
  }

  private String parentPath( final String absPath ) {
    int lastSlashIndex = absPath.lastIndexOf( RepositoryFile.SEPARATOR );
    Assert.isTrue( lastSlashIndex > 1, Messages.getInstance().getString(
        "JcrRepositoryFileDao.ERROR_0003_ILLEGAL_DEST_PATH" ) ); //$NON-NLS-1$
    return absPath.substring( 0, lastSlashIndex );
  }

  private String encodedDestPath( final Node srcFileNode, final String cleanDestAbsPath,
      final boolean appendFileName ) throws RepositoryException {
    if ( !appendFileName ) {
      return JcrStringHelper.pathEncode( cleanDestAbsPath );
    }
    final String fileName = srcFileNode.getName();
    if ( JcrStringHelper.isEncoded( fileName ) ) {
      return JcrStringHelper.pathEncode( cleanDestAbsPath ) + RepositoryFile.SEPARATOR + fileName;
    }
    return JcrStringHelper.pathEncode( cleanDestAbsPath + RepositoryFile.SEPARATOR + fileName );
  }

  private void copyOrMoveNode( final Session session, final String srcPath, final String destPath,
      final String cleanDestAbsPath, final RepositoryFile file, final boolean copy ) throws RepositoryException {
    try {
      if ( copy ) {
        session.getWorkspace().copy( srcPath, destPath );
      } else {
        session.getWorkspace().move( srcPath, destPath );
      }
    } catch ( ItemExistsException iae ) {
      throw new UnifiedRepositoryException( ( file.isFolder() ? "Folder " : "File " ) + "with path ["
          + cleanDestAbsPath + "] already exists in the repository" );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void moveFile( final Serializable fileId, final String destRelPath, final String versionMessage ) {
    internalCopyOrMove( fileId, destRelPath, versionMessage, false );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void copyFile( final Serializable fileId, final String destRelPath, final String versionMessage ) {
    internalCopyOrMove( fileId, destRelPath, versionMessage, true );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VersionSummary getVersionSummary( final Serializable fileId, final Serializable versionId ) {
    Assert.notNull( fileId, "File ID must not be null" );
    java.util.concurrent.locks.Lock contentReadLock =
        JcrRepositoryFileUtils.getFileContentLock( fileId ).readLock();
    contentReadLock.lock();
    try {
      return (VersionSummary) jcrTemplate.execute( new JcrCallback() {
        @Override
        public Object doInJcr( final Session session ) throws RepositoryException, IOException {
          PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
          return JcrRepositoryFileUtils.getVersionSummary( session, pentahoJcrConstants, fileId, versionId );
        }
      } );
    } finally {
      contentReadLock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void restoreFileAtVersion( final Serializable fileId, final Serializable versionId,
                                    final String versionMessage ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException(
        Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }
    Assert.notNull( fileId, "File ID must not be null" );
    Assert.notNull( versionId, "Version ID must not be null" );
    JcrRepositoryFileUtils.FileContentLockLease treeWriteLock =
      JcrRepositoryFileUtils.acquireFileContentTreeWriteLock();
    try {
      jcrTemplate.execute( new JcrCallback() {
        @Override
        public Object doInJcr( final Session session ) throws RepositoryException, IOException {
          PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
          Node fileNode = session.getNodeByIdentifier( fileId.toString() );
          // Both the current and the restored subtree can change membership during a restore.
          Set<Serializable> lockIds = new LinkedHashSet<>();
          collectPentahoFileIdsOnce( pentahoJcrConstants, fileNode, lockIds );
          lockIds.add( fileId );
          collectFrozenFileIds( session, pentahoJcrConstants, session.getWorkspace().getVersionManager()
            .getVersionHistory( fileNode.getPath() ).getVersion( versionId.toString() ).getFrozenNode(), lockIds );
          JcrRepositoryFileUtils.withFileContentWriteLocks( lockIds, () ->
            session.getWorkspace().getVersionManager().restore( fileNode.getPath(), versionId.toString(), true ) );
          return null;
        }
      } );
    } finally {
      treeWriteLock.close();
    }
  }

  private void collectFrozenFileIds( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node frozenNode, final Set<Serializable> fileIds ) throws RepositoryException {
    NodeIterator children = frozenNode.getNodes();
    while ( children.hasNext() ) {
      Node child = children.nextNode();
      if ( child.hasProperty( "jcr:frozenUuid" ) ) {
        String frozenUuid = child.getProperty( "jcr:frozenUuid" ).getString();
        // removeExisting=true deletes a same-UUID node wherever it now lives, along with its descendants.
        try {
          collectPentahoFileIdsOnce( pentahoJcrConstants, session.getNodeByIdentifier( frozenUuid ), fileIds );
        } catch ( ItemNotFoundException e ) {
          // not present in the workspace, nothing to remove
        }
        fileIds.add( frozenUuid );
      }
      collectFrozenFileIds( session, pentahoJcrConstants, child, fileIds );
    }
  }

  // Skips folders already collected so overlapping live subtrees are walked once.
  private void collectPentahoFileIdsOnce( final PentahoJcrConstants pentahoJcrConstants, final Node node,
      final Set<Serializable> fileIds ) throws RepositoryException {
    if ( JcrRepositoryFileUtils.isPentahoFile( pentahoJcrConstants, node ) ) {
      fileIds.add( node.getIdentifier() );
    } else if ( JcrRepositoryFileUtils.isPentahoFolder( pentahoJcrConstants, node ) && fileIds.add( node.getIdentifier() ) ) {
      NodeIterator children = node.getNodes();
      while ( children.hasNext() ) {
        collectPentahoFileIdsOnce( pentahoJcrConstants, children.nextNode(), fileIds );
      }
    }
  }

  private void collectPentahoFileIds( final PentahoJcrConstants pentahoJcrConstants, final Node node,
      final List<Serializable> fileIds ) throws RepositoryException {
    if ( JcrRepositoryFileUtils.isPentahoFile( pentahoJcrConstants, node ) ) {
      fileIds.add( node.getIdentifier() );
    } else if ( JcrRepositoryFileUtils.isPentahoFolder( pentahoJcrConstants, node ) ) {
      fileIds.add( node.getIdentifier() );
      NodeIterator children = node.getNodes();
      while ( children.hasNext() ) {
        collectPentahoFileIds( pentahoJcrConstants, children.nextNode(), fileIds );
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canUnlockFile( final Serializable fileId ) {
    return (Boolean) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        Node fileNode = session.getNodeByIdentifier( fileId.toString() );
        Lock lock = session.getWorkspace().getLockManager().getLock( fileNode.getPath() );
        return lockHelper.canUnlock( session, pentahoJcrConstants, lock );
      }
    } );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryFileTree getTree( final RepositoryRequest repositoryRequest ) {
    Assert.hasText( repositoryRequest.getPath(), "Repository request path must not be null or empty" );

    // the structured child node filter filters files and folders independently, which the node traversal below
    // cannot do; it is served by a JCR-SQL2 query instead
    final TreeNodeFilterSpec filterSpec = TreeNodeFilterSpec.parse( repositoryRequest.getChildNodeFilter() );

    return (RepositoryFileTree) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        String absPath = pathConversionHelper.relToAbs( repositoryRequest.getPath() );

        if ( filterSpec.isStructured() ) {
          return JcrTreeQueryUtils.getTreeByQuery( session, pentahoJcrConstants, pathConversionHelper, lockHelper,
              absPath, repositoryRequest, accessVoterManager );
        }

        return JcrRepositoryFileUtils.getTree( session, pentahoJcrConstants, pathConversionHelper, lockHelper, absPath,
            repositoryRequest, accessVoterManager );
      }
    } );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Deprecated
  public RepositoryFileTree getTree( final String relPath, final int depth, final String filter,
      final boolean showHidden ) {
    Assert.hasText( relPath, "Relative path must not be null or empty" );
    final RepositoryRequest repositoryRequest = new RepositoryRequest( relPath, showHidden, depth, filter );
    return (RepositoryFileTree) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        String absPath = pathConversionHelper.relToAbs( relPath );
        return JcrRepositoryFileUtils.getTree( session, pentahoJcrConstants, pathConversionHelper, lockHelper, absPath,
            repositoryRequest, accessVoterManager );
      }
    } );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public List<RepositoryFile> getReferrers( final Serializable fileId ) {
    if ( fileId == null ) {
      return new ArrayList<RepositoryFile>();
    }

    return (List<RepositoryFile>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );

        Node fileNode = session.getNodeByIdentifier( fileId.toString() );
        // guard against using a file retrieved from a more lenient session inside a more strict session
        Assert.notNull( fileNode, "File node must not be null. Ensure the file ID is valid and accessible." );

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
        return Arrays.asList( referrers.toArray() );
      }
    } );
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

  @Override
  public void setFileMetadata( final Serializable fileId, final Map<String, Serializable> metadataMap ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }
    Assert.notNull( fileId, "File Id must not be null" );
    jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        JcrRepositoryFileUtils.setFileMetadata( session, fileId, metadataMap );
        return null;
      }
    } );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public Map<String, Serializable> getFileMetadata( final Serializable fileId ) {
    Assert.notNull( fileId, "File ID must not be null" );
    return (Map<String, Serializable>) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( Session session ) throws IOException, RepositoryException {
        return JcrRepositoryFileUtils.getFileMetadata( session, fileId );
      }
    } );
  }

  @Override
  public List<Character> getReservedChars() {
    return JcrRepositoryFileUtils.getReservedChars();
  }

  public IRepositoryDefaultAclHandler getDefaultAclHandler() {
    return defaultAclHandler;
  }

  public void setDefaultAclHandler( IRepositoryDefaultAclHandler defaultAclHandler ) {
    this.defaultAclHandler = defaultAclHandler;
  }

  @Override
  public List<Locale> getAvailableLocalesForFileById( Serializable fileId ) {
    RepositoryFile repositoryFile = getFileById( fileId, true );
    return getAvailableLocalesForFile( repositoryFile );
  }

  @Override
  public List<Locale> getAvailableLocalesForFileByPath( String relPath ) {
    RepositoryFile repositoryFile = getFileById( relPath, true );
    return getAvailableLocalesForFile( repositoryFile );
  }

  @Override
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

  @Override
  public Properties getLocalePropertiesForFileById( Serializable fileId, String locale ) {
    RepositoryFile repositoryFile = getFileById( fileId, true );
    return getLocalePropertiesForFile( repositoryFile, locale );
  }

  @Override
  public Properties getLocalePropertiesForFileByPath( String relPath, String locale ) {

    RepositoryFile repositoryFile = getFileById( relPath, true );
    return getLocalePropertiesForFile( repositoryFile, locale );
  }

  @Override
  public Properties getLocalePropertiesForFile( RepositoryFile repositoryFile, String locale ) {
    if ( org.apache.commons.lang3.StringUtils.isBlank( locale ) ) {
      locale = RepositoryFile.DEFAULT_LOCALE;
    }
    if ( repositoryFile != null && repositoryFile.getLocalePropertiesMap() != null ) {
      Properties properties = repositoryFile.getLocalePropertiesMap().get( locale );
      return properties;
    }

    return null;
  }

  @Override
  public void setLocalePropertiesForFileById( Serializable fileId, String locale, Properties properties ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    RepositoryFile repositoryFile = getFileById( fileId, true );
    setLocalePropertiesForFile( repositoryFile, locale, properties );
  }

  @Override
  public void setLocalePropertiesForFileByPath( String relPath, String locale, Properties properties ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    RepositoryFile repositoryFile = getFileById( relPath, true );
    setLocalePropertiesForFile( repositoryFile, locale, properties );
  }

  @Override
  public void setLocalePropertiesForFile( final RepositoryFile repositoryFile, final String locale,
      final Properties properties ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    Assert.notNull( repositoryFile, "Repository file must not be null" );
    Assert.notNull( locale, "Locale must not be null" );
    Assert.notNull( properties, "Properties must not be null" );
    jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        String versionMessage =
            Messages.getInstance().getString( "JcrRepositoryFileDao.LOCALE_0001_UPDATE_PROPERTIES",
                repositoryFile.getId() );
        lockHelper.addLockTokenToSessionIfNecessary( session, pentahoJcrConstants, repositoryFile.getId() );
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, repositoryFile
            .getId() );
        JcrRepositoryFileUtils.updateFileLocaleProperties( session, repositoryFile.getId(), locale, properties );
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, repositoryFile
            .getId(), versionMessage );
        lockHelper.removeLockTokenFromSessionIfNecessary( session, pentahoJcrConstants, repositoryFile.getId() );
        return null;
      }
    } );
  }

  @Override
  public void deleteLocalePropertiesForFile( final RepositoryFile repositoryFile, final String locale ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    Assert.notNull( repositoryFile, "Repository file must not be null" );
    Assert.notNull( locale, "Locale must not be null" );
    jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        String versionMessage =
            Messages.getInstance().getString( "JcrRepositoryFileDao.LOCALE_0002_DELETE_PROPERTIES",
                repositoryFile.getId() );
        lockHelper.addLockTokenToSessionIfNecessary( session, pentahoJcrConstants, repositoryFile.getId() );
        JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, repositoryFile
            .getId() );
        JcrRepositoryFileUtils.deleteFileLocaleProperties( session, repositoryFile.getId(), locale );
        session.save();
        JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, repositoryFile
            .getId(), versionMessage );
        lockHelper.removeLockTokenFromSessionIfNecessary( session, pentahoJcrConstants, repositoryFile.getId() );
        return null;
      }
    } );
  }

  @Override
  public RepositoryFile updateFolder( final RepositoryFile file, final String versionMessage ) {
    if ( isKioskEnabled() ) {
      throw new RuntimeException( Messages.getInstance().getString( "JcrRepositoryFileDao.ERROR_0006_ACCESS_DENIED" ) ); //$NON-NLS-1$
    }

    Assert.notNull( file, "File must not be null" );
    Assert.isTrue( file.isFolder(), "The provided file must be a folder" );
    return (RepositoryFile) jcrTemplate.execute( new JcrCallback() {
      @Override
      public Object doInJcr( final Session session ) throws RepositoryException, IOException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        return internalUpdateFolder( session, pentahoJcrConstants, file, versionMessage );
      }
    } );
  }

  private String extractNameFromPath( String path ) {
    int startIndex = path.lastIndexOf( RepositoryFile.SEPARATOR );
    if ( startIndex >= 0 ) {
      int endIndex = path.indexOf( '.', startIndex );
      if ( endIndex > startIndex ) {
        return path.substring( startIndex + 1, endIndex );
      }
    }
    return null;
  }

  private boolean isKioskEnabled() {
    if ( PentahoSystem.getInitializedOK() ) {
      return "true".equals( PentahoSystem.getSystemSetting( "kiosk-mode", "false" ) );
    } else {
      return false;
    }
  }
}
