/*!
 *
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
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.jcr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.exception.RepositoryFileDaoFileExistsException;
import org.pentaho.platform.repository2.unified.exception.RepositoryFileDaoReferentialIntegrityException;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link IDeleteHelper}.
 * <p/>
 * <p>
 * If user {@code suzy} in tenant {@code acme} deletes a file with id {@code testFileId} and named {@code testFile}
 * in folder with id {@code testFolderId} and named {@code testFolder} then this implementation upon a
 * non-permanent delete will move the file such that the new absolute path and properties of the "deleted" node
 * will be as follows.
 * </p>
 * <p/>
 * <p>
 * Trash Structure 2
 * </p>
 * Uses JCR XPath queries for iteration and filtering. File ID nodes exist to prevent same-name sibling conflicts.
 * Original parent folder path stored in property. All delete-related properties stored in file ID node to avoid
 * the need to checkout versioned files when they are deleted. Note that use of JCR XPath queries may require
 * enabling features in the JCR implementation.
 *
 * <pre>
 * /pentaho/acme/home/suzy/.trash/pho:testFileId/testFile
 * /pentaho/acme/home/suzy/.trash/pho:testFileId/pho:deletedDate (deleted date property)
 * /pentaho/acme/home/suzy/.trash/pho:testFileId/pho:origName (original filename property)
 * /pentaho/acme/home/suzy/.trash/pho:testFileId/pho:origParentFolderPath (original parent folder path property)
 * </pre>
 * <p/>
 * <p>
 * Trash Structure 1 (aka legacy)
 * </p>
 * Uses node iterators and {@link javax.jcr.Node#getNodes(String)} when filtering. File ID nodes exist to prevent
 * same-name sibling conflicts. Original parent folder path derived from folder ID node name. All delete-related
 * properties stored in file ID node to avoid the need to checkout versioned files when they are deleted.
 *
 * <pre>
 * /pentaho/acme/home/suzy/.trash/pho:testFolderId/pho:testFileId/testFile
 * /pentaho/acme/home/suzy/.trash/pho:testFolderId/pho:testFileId/pho:deletedDate (deleted date property)
 * </pre>
 * <p/>
 * <p>
 * Assumptions:
 * <ul>
 * <li>User home folder and all ancestors are not versioned.</li>
 * <li>Internal folders are never versioned.</li>
 * </ul>
 * </p>
 * <p/>
 * <p>
 * By storing deleted files inside the user's home folder, the user's recycle bin is effectively private. This is
 * desirable because a deleted file with confidential information should not be seen by anyone else except the
 * deleting user.
 * </p>
 *
 * @author mlowery
 */
public class DefaultDeleteHelper implements IDeleteHelper {
  public static final String JCR_ROOT_VERSION = "jcr:rootVersion";
  private static final Log logger = LogFactory.getLog( DefaultDeleteHelper.class );

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  final ILockHelper lockHelper;

  final IPathConversionHelper pathConversionHelper;

  // ~ Constructors
  // ====================================================================================================

  public DefaultDeleteHelper( final ILockHelper lockHelper, final IPathConversionHelper pathConversionHelper ) {
    this.lockHelper = lockHelper;
    this.pathConversionHelper = pathConversionHelper;
  }

  // ~ Methods
  // =========================================================================================================

  private static final String FOLDER_NAME_TRASH = ".trash"; //$NON-NLS-1$

  /**
   * {@inheritDoc}
   */
  public void deleteFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
                          final Serializable fileId ) throws RepositoryException {
    Node fileToDeleteNode = session.getNodeByIdentifier( fileId.toString() );
    // move file to .trash subfolder named with the UUID of the file to delete
    Node trashFileIdNode = getOrCreateTrashFileIdNode( session, pentahoJcrConstants, fileId );
    trashFileIdNode.setProperty( pentahoJcrConstants.getPHO_DELETEDDATE(), Calendar.getInstance() );
    trashFileIdNode.setProperty( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH(), pathConversionHelper
        .absToRel( fileToDeleteNode.getParent().getPath() ) );
    // origName only stored in order to do a jcr:like query later on the node name; fn:name() can only do equals
    trashFileIdNode.setProperty( pentahoJcrConstants.getPHO_ORIGNAME(), fileToDeleteNode.getName() );
    session.move( fileToDeleteNode.getPath(), trashFileIdNode.getPath() + RepositoryFile.SEPARATOR
        + fileToDeleteNode.getName() );
  }

  /**
   * Creates and/or returns an internal folder to store a single deleted file. This folder is uniquely named and thus
   * prevents same-name sibling conflicts.
   *
   * @param fileId id of file to delete
   */
  private Node getOrCreateTrashFileIdNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
                                           final Serializable fileId ) throws RepositoryException {
    final String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS ) + ":";
    final String folderName = fileId.toString(); //$NON-NLS-1$
    Node trashInternalFolderNode = getOrCreateTrashInternalFolderNode( session, pentahoJcrConstants );
    if ( NodeHelper.hasNode( trashInternalFolderNode, prefix, folderName ) ) {
      return NodeHelper.getNode( trashInternalFolderNode, prefix, folderName );
    } else {
      return trashInternalFolderNode.addNode( prefix + folderName, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER() );
    }
  }

  /**
   * Returns an internal folder to store all files deleted from a given folder. Provides fast access when searching
   * for files deleted from a given folder.
   */
  private Node legacyGetTrashFolderIdNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
                                           final String origParentFolderPath ) throws RepositoryException {

    // get folder id
    String folderId = null;
    if ( session.itemExists( origParentFolderPath ) ) {
      folderId = ( (Node) session.getItem( origParentFolderPath ) ).getIdentifier();
    } else {
      throw new RuntimeException(
          Messages.getInstance().getString( "DefaultDeleteHelper.ERROR_0001_PATH_NOT_FOUND" ) ); //$NON-NLS-1$
    }

    final String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS ) + ":";
    Node trashInternalFolderNode = getOrCreateTrashInternalFolderNode( session, pentahoJcrConstants );
    if ( NodeHelper.hasNode( trashInternalFolderNode, prefix, folderId ) ) {
      return NodeHelper.getNode( trashInternalFolderNode, prefix, folderId );
    } else {
      // if Trash Structure 1 (legacy) doesn't exist, no need to create it now
      return null;
    }
  }

  /**
   * Creates and/or returns an internal folder called {@code .trash} located just below the user's home folder.
   */
  private Node getOrCreateTrashInternalFolderNode(
      final Session session, final PentahoJcrConstants pentahoJcrConstants )
    throws RepositoryException {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    String tenantId = (String) pentahoSession.getAttribute( IPentahoSession.TENANT_ID_KEY );
    Tenant tenant = new Tenant( tenantId, true );
    String userName = pentahoSession.getName();
    Node userHomeFolderNode =
        (Node) session.getItem( ServerRepositoryPaths.getUserHomeFolderPath( tenant, JcrStringHelper.fileNameEncode( userName ) ) );
    if ( userHomeFolderNode.hasNode( FOLDER_NAME_TRASH ) ) {
      return userHomeFolderNode.getNode( FOLDER_NAME_TRASH );
    } else {
      return userHomeFolderNode.addNode( FOLDER_NAME_TRASH, pentahoJcrConstants.getPHO_NT_INTERNALFOLDER() );
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getDeletedFiles( final Session session, final PentahoJcrConstants pentahoJcrConstants,
                                               final String origParentFolderPath, final String filter )
      throws RepositoryException {
    Node trashNode = getOrCreateTrashInternalFolderNode( session, pentahoJcrConstants );

    // query Trash Structure 2
    QueryObjectModelFactory fac = session.getWorkspace().getQueryManager().getQOMFactory();
    final String selectorName = "selector"; //$NON-NLS-1$

    // selector
    final Selector selector = fac.selector( "nt:base", selectorName ); //$NON-NLS-1$
    // constraint1
    Constraint origParentFolderPathConstraint =
        fac.comparison( fac.propertyValue( selectorName, pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ),
            QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, fac.literal( session.getValueFactory().createValue(
                origParentFolderPath ) ) );
    // constraint2
    Constraint origNameConstraint = null;
    if ( StringUtils.hasLength( filter ) ) {
      String convertedFilter = filter.replace( '*', '%' );
      origNameConstraint =
          fac.comparison( fac.propertyValue( selectorName, pentahoJcrConstants.getPHO_ORIGNAME() ),
              QueryObjectModelConstants.JCR_OPERATOR_LIKE, fac.literal( session.getValueFactory().createValue(
                  convertedFilter ) ) );
    }
    // constraint3
    Constraint descendantNodeConstraint = fac.descendantNode( selectorName, trashNode.getPath() );
    // AND together constraints
    Constraint allConstraints = fac.and( descendantNodeConstraint, origParentFolderPathConstraint );
    if ( StringUtils.hasLength( filter ) ) {
      allConstraints = fac.and( allConstraints, origNameConstraint );
    }
    Query query = fac.createQuery( selector, allConstraints, null, null );
    QueryResult result =
        session.getWorkspace().getQueryManager().createQuery( query.getStatement(), Query.JCR_JQOM ).execute();

    NodeIterator nodeIter = result.getNodes();
    List<RepositoryFile> deletedFiles = new ArrayList<RepositoryFile>();
    String user = PentahoSessionHolder.getSession().getName();

    while ( nodeIter.hasNext() ) {
      Node trashFileIdNode = nodeIter.nextNode();
      if ( trashFileIdNode.hasNodes() ) {
        // since the nodes returned by the query are the trash file ID nodes, need to getNodes().nextNode() to get
        // first
        // (and only) child
        deletedFiles.add(
          nodeToDeletedFile( session, pentahoJcrConstants, trashFileIdNode.getNodes().nextNode(), user ) );
      } else {
        throw new RuntimeException(
            Messages.getInstance().getString( "DefaultDeleteHelper.ERROR_0002_NOT_CLEAN" ) ); //$NON-NLS-1$
      }
    }

    // now we need to handle legacy trash since legacy trashed files don't have origParentFolderPath property

    Set<RepositoryFile> mergedDeletedFiles = new HashSet<RepositoryFile>();
    mergedDeletedFiles.addAll( deletedFiles );
    mergedDeletedFiles.addAll( legacyGetDeletedFiles( session, pentahoJcrConstants, pathConversionHelper
        .relToAbs( origParentFolderPath ), filter ) );

    List<RepositoryFile> mergedList = new ArrayList<RepositoryFile>( mergedDeletedFiles );
    Collections.sort( mergedList );
    return mergedList;
  }

  private List<RepositoryFile> legacyGetDeletedFiles( final Session session,
                                                      final PentahoJcrConstants pentahoJcrConstants,
                                                      final String origParentFolderPath, final String filter )
      throws RepositoryException {
    List<RepositoryFile> deletedFiles = new ArrayList<RepositoryFile>();
    Node trashFolderIdNode = legacyGetTrashFolderIdNode( session, pentahoJcrConstants, origParentFolderPath );
    if ( trashFolderIdNode == null ) {
      return Collections.emptyList();
    }
    NodeIterator nodes = trashFolderIdNode.getNodes();
    while ( nodes.hasNext() ) {
      Node trashFileIdNode = nodes.nextNode();
      NodeIterator trashFileIdNodes = null;
      if ( filter != null ) {
        trashFileIdNodes = trashFileIdNode.getNodes( filter );
      } else {
        trashFileIdNodes = trashFileIdNode.getNodes();
      }
      if ( trashFileIdNodes.hasNext() ) {
        deletedFiles.add( nodeToDeletedFile( session, pentahoJcrConstants, trashFileIdNodes.nextNode(), null ) );
      }
    }
    return deletedFiles;
  }

  /**
   * {@inheritDoc}
   */
  public List<RepositoryFile> getDeletedFiles( final Session session, final PentahoJcrConstants pentahoJcrConstants )
      throws RepositoryException {
    String user = PentahoSessionHolder.getSession().getName();
    Node trashNode = getOrCreateTrashInternalFolderNode( session, pentahoJcrConstants );
    return getDeletedFiles( session, pentahoJcrConstants, trashNode, user );
  }

  protected boolean isAdmin() {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    return policy.isAllowed( AdministerSecurityAction.NAME );
  }

  private List<RepositoryFile> getDeletedFiles( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      Node trashNode, String user )
    throws RepositoryException {
    if ( trashNode == null ) {
      return Collections.emptyList();
    }

    List<RepositoryFile> deletedFiles = new ArrayList<RepositoryFile>();

    NodeIterator nodes = trashNode.getNodes();
    while ( nodes.hasNext() ) {
      Node trashFileNode = nodes.nextNode();

      // since the nodes returned are the trash file ID nodes,
      // we need to getNodes().nextNode() to get its first (and only) child

      if ( trashFileNode != null && trashFileNode.hasProperty( pentahoJcrConstants.getPHO_DELETEDDATE() ) ) {

        NodeIterator trashFileNodeIterator = trashFileNode.getNodes();

        if ( trashFileNodeIterator.hasNext() ) {
          deletedFiles.add( nodeToDeletedFile( session, pentahoJcrConstants, trashFileNodeIterator.nextNode(), user ) );
        }
      }
    }
    Collections.sort( deletedFiles );
    return deletedFiles;
  }

  public List<RepositoryFile> getAllDeletedFiles( final Session session,
      final PentahoJcrConstants pentahoJcrConstants )
    throws RepositoryException {

    if ( isAdmin() ) {
      List<RepositoryFile> deletedFiles = new ArrayList<>();
      ITenant tenant = JcrTenantUtils.getTenant();
      for ( String user : getUserList() ) {
        String homePath = getHomePath( tenant, user );
        try {
          Node userHomeFolderNode = (Node) session.getItem( homePath );
          if ( userHomeFolderNode.hasNode( FOLDER_NAME_TRASH ) ) {
            Node trashNode = userHomeFolderNode.getNode( FOLDER_NAME_TRASH );
            List<RepositoryFile> deletedForUser = getDeletedFiles( session, pentahoJcrConstants, trashNode, user );
            deletedFiles.addAll( deletedForUser );
          }
        } catch ( PathNotFoundException e ) {
          logger.warn( MessageFormat.format(
            Messages.getInstance()
              .getString( "DefaultDeleteHelper.PATH_NOT_FOUND_EXCEPTION" ), homePath, user ), e );
        }
      }
      Collections.sort( deletedFiles );
      return deletedFiles;
    }
    return getDeletedFiles( session, pentahoJcrConstants );
  }

  protected String getHomePath( ITenant tenant, String user ) {
    return ServerRepositoryPaths.getUserHomeFolderPath( tenant, JcrStringHelper.fileNameEncode( user ) );
  }

  protected List<String> getUserList() {
    IUserRoleDao userRoleDao = PentahoSystem.get( IUserRoleDao.class );
    List<IPentahoUser> iusers = userRoleDao.getUsers();
    return iusers.stream().map( user -> user.getUsername() ).collect( Collectors.toList() );
  }

  private RepositoryFile nodeToDeletedFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
                                            final Node trashFileNode, String owner ) throws RepositoryException {
    // each fileId node has at most one child that is the deleted file
    RepositoryFile deletedFile =
        JcrRepositoryFileUtils.nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper,
            trashFileNode );
    Date deletedDate = getDeletedDate( trashFileNode, pentahoJcrConstants );
    String originalParentFolderPath = getOriginalParentFolderPath( session, pentahoJcrConstants, trashFileNode, true );
    return new RepositoryFile.Builder( deletedFile ).deletedDate( deletedDate ).originalParentFolderPath(
        originalParentFolderPath ).creatorId( owner ).build();
  }

  //returns encoded path
  private String getOriginalParentFolderPath( final Session session, final PentahoJcrConstants pentahoJcrConstants,
                                              final Node trashFileNode, final boolean relative )
      throws RepositoryException {
    if ( trashFileNode.getParent().hasProperty( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ) ) {
      String relPath =
          trashFileNode.getParent().getProperty( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ).getString();
      return relative ? relPath : pathConversionHelper.relToAbs( relPath );
    } else {
      // legacy mode
      final String prefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS );
      String originalParentFolderId = trashFileNode.getParent().getParent().getName().substring( prefix.length() + 1 );
      String absPath = session.getNodeByIdentifier( originalParentFolderId ).getPath();
      return relative ? pathConversionHelper.absToRel( absPath ) : absPath;
    }
  }

  private Date getDeletedDate( final Node trashFileNode, final PentahoJcrConstants pentahoJcrConstants )
      throws RepositoryException {
    if ( trashFileNode.getParent().hasProperty( pentahoJcrConstants.getPHO_DELETEDDATE() ) ) {
      return trashFileNode.getParent().getProperty( pentahoJcrConstants.getPHO_DELETEDDATE() ).getDate().getTime();
    } else {
      // legacy mode
      return trashFileNode.getParent().getProperty( pentahoJcrConstants.getPHO_DELETEDDATE() ).getDate().getTime();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void permanentlyDeleteFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
                                     final Serializable fileId ) throws RepositoryException {
    Assert.notNull( fileId );
    Node fileNode = session.getNodeByIdentifier( fileId.toString() );
    // guard against using a file retrieved from a more lenient session inside a more strict session
    Assert.notNull( fileNode );

    // see if anything is referencing this node; if yes, then we cannot delete it as a
    // ReferentialIntegrityException
    // will result
    Set<RepositoryFile> referrers = new HashSet<RepositoryFile>();
    PropertyIterator refIter = fileNode.getReferences();
    if ( refIter.hasNext() ) {
      while ( refIter.hasNext() ) {
        // for each referrer property, march up the tree until we find the file node to which the property belongs
        RepositoryFile referrer = getReferrerFile( session, pentahoJcrConstants, refIter.nextProperty() );
        if ( referrer != null ) {
          referrers.add( referrer );
        }
      }
      if ( !referrers.isEmpty() ) {
        RepositoryFile referee =
            JcrRepositoryFileUtils
                .nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper, fileNode );
        throw new RepositoryFileDaoReferentialIntegrityException( referee, referrers );
      }
    }

    // technically, the node can be deleted while it is locked; however, we want to avoid an orphaned lock token;
    // delete
    // it first
    if ( fileNode.isLocked() ) {
      Lock lock = session.getWorkspace().getLockManager().getLock( fileNode.getPath() );
      // don't need lock token anymore
      lockHelper.removeLockToken( session, pentahoJcrConstants, lock );
    }

    // if this file was non-permanently deleted, delete its containing folder too
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    String tenantId = (String) pentahoSession.getAttribute( IPentahoSession.TENANT_ID_KEY );
    String trashFolder =
        ServerRepositoryPaths.getUserHomeFolderPath( new Tenant( tenantId, true ), PentahoSessionHolder.getSession()
            .getName() )
            + RepositoryFile.SEPARATOR + FOLDER_NAME_TRASH;
    Node parent = fileNode.getParent();


    purgeHistory( fileNode, session, pentahoJcrConstants );

    if ( fileNode.getPath().startsWith( trashFolder ) ) {
      // Remove the file and then the wrapper foler
      fileNode.remove();
      parent.remove();
    } else {
      fileNode.remove();
    }
  }

  private void purgeHistory( Node fileNode, Session session, PentahoJcrConstants pentahoJcrConstants )
      throws RepositoryException {
    // Delete all previous versions of this node
    VersionManager versionManager = session.getWorkspace().getVersionManager();
    if ( JcrRepositoryFileUtils.isPentahoFolder( pentahoJcrConstants, fileNode ) ) {
      // go down to children
      NodeIterator nodes = fileNode.getNodes();
      while ( nodes.hasNext() ) {
        Node next = (Node) nodes.next();
        purgeHistory( next, session, pentahoJcrConstants );
      }
    } else if ( JcrRepositoryFileUtils.isPentahoFile( pentahoJcrConstants, fileNode )
        && fileNode.isNodeType( pentahoJcrConstants.getPHO_MIX_VERSIONABLE() ) ) {
      VersionHistory versionHistory = versionManager.getVersionHistory( fileNode.getPath() );

      VersionIterator allVersions = versionHistory.getAllVersions();
      while ( allVersions.hasNext() ) {
        Version next = (Version) allVersions.next();
        String name = next.getName();
        // Root version cannot be deleted, the remove below will take care of that.
        if ( !JCR_ROOT_VERSION.equals( name ) ) {
          versionHistory.removeVersion( name );
        }
      }
    }

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
    } else {
      return JcrRepositoryFileUtils.nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper,
          currentNode );
    }
  }

  /**
   * {@inheritDoc}
   */
  public void undeleteFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
                            final Serializable fileId ) throws RepositoryException {
    Node fileToUndeleteNode = session.getNodeByIdentifier( fileId.toString() );
    String trashFileIdNodePath = fileToUndeleteNode.getParent().getPath();
    String origParentFolderPath =
        getOriginalParentFolderPath( session, pentahoJcrConstants, fileToUndeleteNode, false );

    String absDestPath = origParentFolderPath + RepositoryFile.SEPARATOR + fileToUndeleteNode.getName();

    if ( session.itemExists( absDestPath ) ) {
      RepositoryFile file =
          JcrRepositoryFileUtils.nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper,
              (Node) session.getItem( absDestPath ) );
      throw new RepositoryFileDaoFileExistsException( file );
    }

    session.move( fileToUndeleteNode.getPath(), absDestPath );
    session.getItem( trashFileIdNodePath ).remove();
  }

  /**
   * {@inheritDoc}
   */
  public String getOriginalParentFolderPath( final Session session, final PentahoJcrConstants pentahoJcrConstants,
                                             final Serializable fileId ) throws RepositoryException {
    return JcrStringHelper.pathDecode(
        getOriginalParentFolderPath( session, pentahoJcrConstants, session.getNodeByIdentifier( fileId.toString() ),
            false ) );
  }
}
