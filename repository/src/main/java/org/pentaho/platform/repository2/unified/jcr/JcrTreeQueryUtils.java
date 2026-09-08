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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoterManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.unified.TreeNodeFilterSpec;
import org.springframework.util.Assert;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * JCR-SQL2 based implementation of the repository tree retrieval.
 * <p>
 * Unlike {@link JcrRepositoryFileUtils#getTree(Session, PentahoJcrConstants, IPathConversionHelper, ILockHelper,
 * String, RepositoryRequest, IRepositoryAccessVoterManager)}, which walks the node hierarchy and applies a single node
 * name filter to both files and folders, this implementation honors the structured child node filter parsed by
 * {@link TreeNodeFilterSpec} by issuing two queries:
 * <ul>
 * <li>one returning the <code>pentahoFolder</code> nodes matching <code>folderFilter</code>, or every folder below the
 * requested path when no folder filter was given;</li>
 * <li>one returning the <code>pentahoFile</code> nodes matching <code>fileFilter</code>.</li>
 * </ul>
 * The nodes those queries return are the <i>matches</i> of the request, and the tree is assembled around them:
 * <ul>
 * <li>every folder the folder filter selects belongs to the tree, empty ones included, and every file the file
 * filter selects does too: a filter only ever narrows its own kind of node;</li>
 * <li>the folders between a matching folder and the root are materialized, because a query returns a flat result set
 * while the node traversal walks through them, so that a matching folder deeper than a direct child of the requested
 * path stays reachable;</li>
 * <li>a file living in a folder the folder filter rejected is left out, for the tree carries no folder to hold
 * it;</li>
 * <li>{@link RepositoryRequest.FILES_TYPE_FILTER#FOLDERS} leaves the files out of the tree altogether, while the
 * folders always answer to the folder filter alone;</li>
 * <li>the deleted files, which keep their node type below the trash folder of a user home folder, are left out, as
 * the traversal leaves them out by refusing to descend into that internal folder.</li>
 * </ul>
 * This class is JCR specific and must only be used by the JCR repository provider.
 */
public class JcrTreeQueryUtils {
  private static final Log logger = LogFactory.getLog( JcrTreeQueryUtils.class );

  private static final String PATH_SEPARATOR = "/";

  /**
   * the name of the internal folder <code>DefaultDeleteHelper</code> moves the deleted files to
   */
  private static final String TRASH_FOLDER_NAME = ".trash";

  /**
   * The trash folder as a path segment.
   * <p>
   * Its node type is <code>pentahoInternalFolder</code>, which the node traversal refuses to descend into, so the
   * files below it never belong to a tree. A query, on the other hand, returns them, because they keep their
   * <code>pentahoFile</code> type once deleted, hence this check.
   */
  private static final String TRASH_FOLDER_PATH_SEGMENT = PATH_SEPARATOR + TRASH_FOLDER_NAME + PATH_SEPARATOR;

  /**
   * the JCR-SQL2 <code>LIKE</code> wildcards, which the node name filter wildcard is translated to
   */
  private static final String SQL_LIKE_WILDCARD = "%";

  /**
   * the node name filter wildcard, as a regular expression, so that a pattern can be split on it
   */
  private static final String WILDCARD_SPLITTER = Pattern.quote( RepositoryRequest.FILTER_WILDCARD );

  private JcrTreeQueryUtils() {
    // static utility class
  }

  /**
   * Builds the repository tree of the given request with two JCR-SQL2 queries, so that files and folders are filtered
   * independently.
   *
   * @param session              the JCR session
   * @param pentahoJcrConstants  the constants of the session workspace
   * @param pathConversionHelper the relative / absolute path converter
   * @param lockHelper           the lock helper
   * @param absPath              the absolute path of the root of the tree
   * @param repositoryRequest    the request, whose child node filter must carry the structured syntax
   * @param accessVoterManager   the access voter manager; must not be <code>null</code>, exactly as required by
   *                             {@link JcrRepositoryFileUtils#getTree(Session, PentahoJcrConstants,
   *                             IPathConversionHelper, ILockHelper, String, RepositoryRequest,
   *                             IRepositoryAccessVoterManager)}
   * @return the tree, or <code>null</code> when the root is hidden, is an ACL node, or is not readable
   * @throws RepositoryException when the queries cannot be executed
   */
  public static RepositoryFileTree getTreeByQuery( final Session session,
                                                   final PentahoJcrConstants pentahoJcrConstants,
                                                   final IPathConversionHelper pathConversionHelper,
                                                   final ILockHelper lockHelper, final String absPath,
                                                   final RepositoryRequest repositoryRequest,
                                                   final IRepositoryAccessVoterManager accessVoterManager )
    throws RepositoryException {
    String encodedRootPath = JcrStringHelper.pathEncode( absPath );
    Item rootItem = session.getItem( encodedRootPath );

    // items are nodes or properties; this must be a node
    Assert.isTrue( rootItem.isNode(),
      "The specified item must be a node. Ensure the provided path corresponds to a valid node in the repository." );

    Node rootNode = (Node) rootItem;
    RepositoryFile rootFile =
      JcrRepositoryFileUtils.nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper, rootNode );

    // the very same root rejection as the node traversal: hidden, ACL node or unreadable yields no tree at all. The
    // ACL is read without guarding against AccessDeniedException on purpose, so that it propagates just as it does
    // there
    if ( ( !repositoryRequest.isShowHidden() && rootFile.isHidden() ) || rootFile.isAclNode()
      || !accessVoterManager.hasAccess( rootFile, RepositoryFilePermission.READ,
      JcrRepositoryFileAclUtils.getAcl( session, pentahoJcrConstants, rootFile.getId() ),
      PentahoSessionHolder.getSession() ) ) {
      return null;
    }

    // if depth is neither negative ( indicating unlimited depth ) nor positive ( indicating at least one more level to
    // go ), the root carries no children at all
    if ( repositoryRequest.getDepth() == 0 ) {
      return new RepositoryFileTree( rootFile, null );
    }

    TreeQueryContext context =
      new TreeQueryContext( session, pentahoJcrConstants, pathConversionHelper, lockHelper, accessVoterManager,
        repositoryRequest );
    // the path of the node itself, rather than the encoded request path, so that it compares equal to the paths of
    // the nodes the queries return, whatever the encoding did
    context.setRoot( rootNode.getPath(), rootFile );

    // the folders are what the tree is made of, so they are always collected, and only the folder filter narrows
    // them; the folders between a matching folder and the root are materialized along with it
    collectNodes( context, buildFolderQuery( pentahoJcrConstants, encodedRootPath, context.filterSpec ), true );

    // a file, on the other hand, is only attached to a folder the tree already carries: the folder filter is what
    // decides which folders those are
    if ( repositoryRequest.getTypes() != RepositoryRequest.FILES_TYPE_FILTER.FOLDERS ) {
      collectNodes( context, buildFileQuery( pentahoJcrConstants, encodedRootPath, context.filterSpec ), false );
    }

    return toRepositoryFileTree( context.nodesByPath.get( context.rootPath ) );
  }

  // region Query building
  private static String buildFolderQuery( final PentahoJcrConstants pentahoJcrConstants, final String encodedRootPath,
                                          final TreeNodeFilterSpec filterSpec ) {
    return "SELECT * FROM [" + pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER() + "]"
      + " WHERE ISDESCENDANTNODE(" + toPathLiteral( encodedRootPath ) + ")"
      + nameConstraint( filterSpec.getFolderFilter() );
  }

  private static String buildFileQuery( final PentahoJcrConstants pentahoJcrConstants, final String encodedRootPath,
                                        final TreeNodeFilterSpec filterSpec ) {
    return "SELECT * FROM [" + pentahoJcrConstants.getPHO_NT_PENTAHOFILE() + "]"
      + " WHERE ISDESCENDANTNODE(" + toPathLiteral( encodedRootPath ) + ")"
      + nameConstraint( filterSpec.getFileFilter() );
  }

  /**
   * Translates <code>"*.ktr,*.kjb"</code> into
   * <code>" AND ( LOWER(LOCALNAME()) LIKE '%.ktr' OR LOWER(LOCALNAME()) LIKE '%.kjb' )"</code>. A <code>null</code>
   * filter means "every node of this type".
   * <p>
   * Two limitations of the JCR-SQL2 implementation shape this constraint:
   * <ul>
   * <li>the SQL <code>ESCAPE</code> clause is not supported, so the <code>LIKE</code> wildcards of a node name cannot
   * be escaped;</li>
   * <li><code>NAME()</code> only supports an equality comparison without transformation, so a <code>LIKE</code> on it
   * is rejected as soon as it appears in a boolean constraint, whereas <code>LOCALNAME()</code> supports both.</li>
   * </ul>
   * The constraint is therefore a widening one, matching at least every node the filter selects, and the exact
   * matching is done in memory by {@link #matchesNodeFilter(String, String)}.
   */
  private static String nameConstraint( final String nodeFilter ) {
    if ( nodeFilter == null || TreeNodeFilterSpec.DEFAULT_CHILD_NODE_FILTER.equals( nodeFilter )
      // LOCALNAME() drops the namespace prefix, so a pattern carrying its separator is left to the in-memory
      // matching alone rather than risking a false negative. Repository file names cannot contain it anyway
      || nodeFilter.indexOf( ':' ) >= 0 ) {
      return "";
    }

    StringBuilder constraint = new StringBuilder( " AND (" );
    boolean first = true;

    for ( String pattern : nodeFilter.split( TreeNodeFilterSpec.INSIDE_FILTER_TOKEN_SEPARATOR ) ) {
      if ( !first ) {
        constraint.append( " OR " );
      }

      constraint.append( "LOWER(LOCALNAME()) LIKE '" ).append( toLikeLiteral( pattern ) ).append( "'" );
      first = false;
    }

    return constraint.append( ")" ).toString();
  }

  private static String toLikeLiteral( final String pattern ) {
    return pattern.toLowerCase( Locale.ROOT )
      .replace( "'", "''" )
      .replace( RepositoryRequest.FILTER_WILDCARD, SQL_LIKE_WILDCARD );
  }

  /**
   * Turns the given path into the quoted path literal of a JCR-SQL2 statement.
   * <p>
   * The bracketed form, <code>[/a/b]</code>, is a node name and the engine silently matches nothing when a segment
   * of the path carries a space, which repository folder and file names commonly do; the quoted form is parsed as a
   * path and behaves.
   */
  private static String toPathLiteral( final String path ) {
    return "'" + path.replace( "'", "''" ) + "'";
  }
  // endregion Query building

  // region Query execution
  private static void collectNodes( final TreeQueryContext context, final String statement,
                                    final boolean materializeAncestors ) throws RepositoryException {
    Query query = context.session.getWorkspace().getQueryManager().createQuery( statement, Query.JCR_SQL2 );
    NodeIterator nodeIterator = query.execute().getNodes();

    while ( nodeIterator.hasNext() ) {
      Node node = nodeIterator.nextNode();
      RepositoryFile file = accept( context, node );

      if ( file != null ) {
        add( context, node, file, materializeAncestors );
      }
    }
  }

  /**
   * Adds the given match to the tree, below its parent folder.
   * <p>
   * A query returns a flat result set, so the parent may not be there yet: when the caller asks for it, it is
   * materialized, and so on up to the root, exactly as the node traversal walks through the folders a matching
   * folder lives in. Otherwise, and whenever an ancestor must be skipped ( unsupported, hidden, ACL node, system
   * folder or unreadable ), the node is left unlinked, which drops it, and everything below it, from the tree being
   * returned: a file whose folder the folder filter rejected is dropped that way.
   *
   * @return the node added to the tree
   */
  private static TreeNode add( final TreeQueryContext context, final Node node, final RepositoryFile file,
                               final boolean materializeAncestors ) throws RepositoryException {
    String path = normalizePath( file.getPath() );
    TreeNode treeNode = new TreeNode( file );
    context.nodesByPath.put( path, treeNode );

    String parentPath = parentPathOf( path );
    TreeNode parent = parentPath == null ? null : context.nodesByPath.get( parentPath );

    // the root is always there, so a missing parent means the node lives in a folder no query returned
    if ( parent == null && materializeAncestors ) {
      parent = materializeParent( context, node );
    }

    if ( parent != null ) {
      parent.children.add( treeNode );
    }

    return treeNode;
  }

  /**
   * Adds the parent folder of the given node to the tree, when it may belong to it, climbing on from there.
   *
   * @return the parent folder of the given node, <code>null</code> when the branch must be dropped
   */
  private static TreeNode materializeParent( final TreeQueryContext context, final Node node )
    throws RepositoryException {
    Node parentNode = node.getParent();

    if ( !isBelowRoot( context.encodedRootPath, normalizePath( parentNode.getPath() ) ) ) {
      return null;
    }

    RepositoryFile parentFile = acceptAncestor( context, parentNode );

    if ( parentFile == null ) {
      return null;
    }

    return add( context, parentNode, parentFile, true );
  }

  /**
   * Mirrors <code>JcrRepositoryFileUtils.checkNodeForTree</code> together with the root checks its recursive call
   * performs: an unsupported, unreadable, hidden or ACL node is skipped, and so is a node whose ACL cannot be read
   * because access was denied.
   *
   * @return the file of the given node when it belongs to the tree being built, <code>null</code> when it must be
   * skipped
   */
  private static RepositoryFile accept( final TreeQueryContext context, final Node node ) throws RepositoryException {
    RepositoryFile file = acceptNode( context, node );

    if ( file == null || !matchesStructuredFilter( file, context.filterSpec ) ) {
      return null;
    }

    String path = normalizePath( file.getPath() );

    if ( context.nodesByPath.containsKey( path )
      || !isWithinDepth( context.rootPath, path, context.maxDepth )
      || isAccessDenied( context.session, context.pentahoJcrConstants, file, context.accessVoterManager ) ) {
      return null;
    }

    return file;
  }

  /**
   * Same as {@link #accept(TreeQueryContext, Node)} without the filter, the depth and the duplicate checks, none of
   * which apply to a folder that only carries the tree: it did not have to match the filter, it is shallower than the
   * node it was materialized for, and the caller checks whether it is already there.
   *
   * @return the file of the given ancestor folder, <code>null</code> when the branch must be dropped
   */
  private static RepositoryFile acceptAncestor( final TreeQueryContext context, final Node node )
    throws RepositoryException {
    RepositoryFile file = acceptNode( context, node );

    if ( file == null
      || isAccessDenied( context.session, context.pentahoJcrConstants, file, context.accessVoterManager ) ) {
      return null;
    }

    return file;
  }

  /**
   * The checks every node of the tree goes through, whether it matched a query or it was materialized to carry its
   * descendants.
   */
  private static RepositoryFile acceptNode( final TreeQueryContext context, final Node node )
    throws RepositoryException {
    // checked before the file is built, so that a repository full of deleted files costs no more than its paths
    if ( isInTrash( context, node ) ) {
      return null;
    }

    RepositoryFile file =
      JcrRepositoryFileUtils.nodeToFile( context.session, context.pentahoJcrConstants, context.pathConversionHelper,
        context.lockHelper, node );

    if ( !JcrRepositoryFileUtils.isSupportedNodeType( context.pentahoJcrConstants, node ) ) {
      return null;
    }

    // node paths are the encoded ones, hence the comparison against the encoded root path
    if ( !context.repositoryRequest.isIncludeSystemFolders()
      && context.encodedRootPath.equals( normalizePath( node.getParent().getPath() ) )
      && isSystemFolder( context.session, node ) ) {
      return null;
    }

    if ( file == null || file.isAclNode() || ( !context.repositoryRequest.isShowHidden() && file.isHidden() ) ) {
      return null;
    }

    return file;
  }

  /**
   * Applies the structured filter to the given file, which the query constraint only narrowed down. A folder is
   * matched against the folder filter and a file against the file filter, exactly as the two queries intend.
   */
  private static boolean matchesStructuredFilter( final RepositoryFile file, final TreeNodeFilterSpec filterSpec ) {
    return matchesNodeFilter( file.getName(),
      file.isFolder() ? filterSpec.getFolderFilter() : filterSpec.getFileFilter() );
  }

  private static boolean matchesNodeFilter( final String nodeName, final String nodeFilter ) {
    if ( nodeFilter == null || TreeNodeFilterSpec.DEFAULT_CHILD_NODE_FILTER.equals( nodeFilter ) ) {
      return true;
    }

    String lowerName = nodeName.toLowerCase( Locale.ROOT );

    for ( String pattern : nodeFilter.split( TreeNodeFilterSpec.INSIDE_FILTER_TOKEN_SEPARATOR ) ) {
      if ( globToRegex( pattern ).matcher( lowerName ).matches() ) {
        return true;
      }
    }

    return false;
  }

  /**
   * Only {@value RepositoryRequest#FILTER_WILDCARD} is a wildcard, every other character is a literal, exactly as in
   * the node name glob of the traversal based tree.
   */
  private static Pattern globToRegex( final String pattern ) {
    StringBuilder regex = new StringBuilder();

    for ( String literal : pattern.toLowerCase( Locale.ROOT ).split( WILDCARD_SPLITTER, -1 ) ) {
      if ( !regex.isEmpty() ) {
        regex.append( ".*" );
      }

      regex.append( Pattern.quote( literal ) );
    }

    return Pattern.compile( regex.toString() );
  }

  /**
   * Reads the ACL of the given file and votes on it. An {@link AccessDeniedException} raised while reading the ACL
   * means the node must be skipped, exactly as in <code>JcrRepositoryFileUtils.checkNodeForTree</code>; every other
   * {@link RepositoryException} propagates.
   */
  private static boolean isAccessDenied( final Session session, final PentahoJcrConstants pentahoJcrConstants,
                                         final RepositoryFile file,
                                         final IRepositoryAccessVoterManager accessVoterManager )
    throws RepositoryException {
    RepositoryFileAcl acl;

    try {
      acl = JcrRepositoryFileAclUtils.getAcl( session, pentahoJcrConstants, file.getId() );
    } catch ( AccessDeniedException e ) {
      logger.debug( "Access denied while reading the ACL of " + file.getPath(), e );
      return true;
    }

    return !accessVoterManager.hasAccess( file, RepositoryFilePermission.READ, acl,
      PentahoSessionHolder.getSession() );
  }

  private static boolean isSystemFolder( final Session session, final Node node ) throws RepositoryException {
    Map<String, Serializable> fileMeta = JcrRepositoryFileUtils.getFileMetadata( session, node.getIdentifier() );
    return fileMeta.containsKey( IUnifiedRepository.SYSTEM_FOLDER )
      && (Boolean) fileMeta.get( IUnifiedRepository.SYSTEM_FOLDER );
  }

  /**
   * Whether the given node is the trash folder of a user home folder, or one of its descendants, that is, whether it
   * was deleted.
   * <p>
   * Only the part of the path below the root of the tree is looked at, so that the tree of a deleted folder, which a
   * caller may legitimately ask for, still carries its content.
   *
   * @return <code>true</code> when the node was deleted and must be left out of the tree
   */
  private static boolean isInTrash( final TreeQueryContext context, final Node node ) throws RepositoryException {
    String path = node.getPath();

    if ( path.length() <= context.encodedRootPath.length() ) {
      return false;
    }

    String belowRoot = path.substring( context.encodedRootPath.length() );

    return belowRoot.contains( TRASH_FOLDER_PATH_SEGMENT )
      || belowRoot.endsWith( PATH_SEPARATOR + TRASH_FOLDER_NAME );
  }
  // endregion Query execution

  // region Tree assembly
  private static RepositoryFileTree toRepositoryFileTree( final TreeNode node ) {
    List<RepositoryFileTree> children = new ArrayList<>( node.children.size() );

    for ( TreeNode child : node.children ) {
      children.add( toRepositoryFileTree( child ) );
    }

    Collections.sort( children );

    return new RepositoryFileTree( node.file, children );
  }
  // endregion Tree assembly

  // region Path helpers
  private static String normalizePath( final String path ) {
    if ( path == null || path.isEmpty() ) {
      return PATH_SEPARATOR;
    }

    if ( path.length() > 1 && path.endsWith( PATH_SEPARATOR ) ) {
      return path.substring( 0, path.length() - 1 );
    }

    return path;
  }

  private static String parentPathOf( final String path ) {
    int index = path.lastIndexOf( '/' );

    if ( index < 0 ) {
      return null;
    }

    return index == 0 ? PATH_SEPARATOR : path.substring( 0, index );
  }

  private static int depthOf( final String path ) {
    int depth = 0;

    for ( int i = 0; i < path.length(); i++ ) {
      if ( path.charAt( i ) == '/' ) {
        depth++;
      }
    }

    return depth;
  }

  private static boolean isWithinDepth( final String rootPath, final String path, final int maxDepth ) {
    if ( maxDepth < 0 ) {
      return true;
    }

    int rootDepth = PATH_SEPARATOR.equals( rootPath ) ? 0 : depthOf( rootPath );

    return depthOf( path ) - rootDepth <= maxDepth;
  }

  /**
   * @return <code>true</code> when the given path is a strict descendant of the given root path
   */
  private static boolean isBelowRoot( final String rootPath, final String path ) {
    String prefix = PATH_SEPARATOR.equals( rootPath ) ? rootPath : rootPath + PATH_SEPARATOR;

    return path.length() > rootPath.length() && path.startsWith( prefix );
  }
  // endregion Path helpers

  // region Inner classes

  /**
   * Everything the collection of a single query result needs, so that the helpers below do not carry a dozen
   * parameters around.
   */
  private static final class TreeQueryContext {
    private final Session session;
    private final PentahoJcrConstants pentahoJcrConstants;
    private final IPathConversionHelper pathConversionHelper;
    private final ILockHelper lockHelper;
    private final IRepositoryAccessVoterManager accessVoterManager;
    private final RepositoryRequest repositoryRequest;
    private final TreeNodeFilterSpec filterSpec;
    private final int maxDepth;
    private final Map<String, TreeNode> nodesByPath = new HashMap<>();
    private String encodedRootPath;
    private String rootPath;

    private TreeQueryContext( final Session session, final PentahoJcrConstants pentahoJcrConstants,
                              final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper,
                              final IRepositoryAccessVoterManager accessVoterManager,
                              final RepositoryRequest repositoryRequest ) {
      this.session = session;
      this.pentahoJcrConstants = pentahoJcrConstants;
      this.pathConversionHelper = pathConversionHelper;
      this.lockHelper = lockHelper;
      this.accessVoterManager = accessVoterManager;
      this.repositoryRequest = repositoryRequest;
      this.filterSpec = TreeNodeFilterSpec.parse( repositoryRequest.getChildNodeFilter() );
      this.maxDepth = repositoryRequest.getDepth();
    }

    /**
     * @param encodedRootPath the encoded absolute path of the root, which is the form the JCR node paths carry, used
     *                        to spot the direct children of the root exactly as the node traversal does and to stop
     *                        the materialization of the ancestor folders
     * @param rootFile        the file of the root node, which anchors the tree being assembled
     */
    private void setRoot( final String encodedRootPath, final RepositoryFile rootFile ) {
      this.encodedRootPath = normalizePath( encodedRootPath );
      this.rootPath = normalizePath( rootFile.getPath() );
      this.nodesByPath.put( this.rootPath, new TreeNode( rootFile ) );
    }
  }

  private static class TreeNode {
    private final RepositoryFile file;
    private final List<TreeNode> children = new ArrayList<>();

    TreeNode( final RepositoryFile file ) {
      this.file = file;
    }
  }
  // endregion Inner classes
}
