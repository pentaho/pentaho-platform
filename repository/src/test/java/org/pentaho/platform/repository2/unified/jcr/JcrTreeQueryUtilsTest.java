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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoterManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.repository2.unified.TreeNodeFilterSpec;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests of {@link JcrTreeQueryUtils}, the JCR-SQL2 based tree retrieval.
 */
public class JcrTreeQueryUtilsTest {
  private static final String FOLDER_NODE_TYPE = "pho_nt:pentahoFolder";
  private static final String FILE_NODE_TYPE = "pho_nt:pentahoFile";
  private static final String ROOT_PATH = "/home/admin";

  /**
   * the folder the deleted files of the root are moved to, named exactly as <code>DefaultDeleteHelper</code> does
   */
  private static final String TRASH_PATH = ROOT_PATH + "/.trash/pho:d78ca35b-852b-4f89-8d4f-61486d3ca1c3";
  private static final String KTR_PATTERN = RepositoryRequest.FILTER_WILDCARD + ".KTR";
  private static final String KJB_PATTERN = RepositoryRequest.FILTER_WILDCARD + ".kjb";
  private static final String FOLDER_PATTERN = "sales*";
  private static final String MARKETING_FOLDER_PATTERN = "marketing";
  private static final String ESCAPED_PATTERN = "a_b%c*.ktr";

  private Session session;
  private QueryManager queryManager;
  private PentahoJcrConstants pentahoJcrConstants;
  private IPathConversionHelper pathConversionHelper;
  private ILockHelper lockHelper;

  /**
   * the voter manager used by the tests that do not care about access control; it grants everything
   */
  private IRepositoryAccessVoterManager accessVoterManager;

  private MockedStatic<JcrStringHelper> jcrStringHelper;
  private MockedStatic<JcrRepositoryFileUtils> jcrRepositoryFileUtils;
  private MockedStatic<JcrRepositoryFileAclUtils> jcrRepositoryFileAclUtils;

  /**
   * the file each mocked node is turned into by {@code JcrRepositoryFileUtils.nodeToFile}
   */
  private final Map<Node, RepositoryFile> filesByNode = new HashMap<>();

  /**
   * the nodes each query returns, keyed by the node type appearing in the statement
   */
  private final Map<String, List<Node>> nodesByNodeType = new HashMap<>();

  /**
   * every mocked node, keyed by its path, so that a node and its parents are mocked only once
   */
  private final Map<String, Node> nodesByPath = new HashMap<>();

  private final List<String> statements = new ArrayList<>();

  @Before
  public void setUp() throws Exception {
    session = mock( Session.class );
    Workspace workspace = mock( Workspace.class );
    queryManager = mock( QueryManager.class );
    when( session.getWorkspace() ).thenReturn( workspace );
    when( workspace.getQueryManager() ).thenReturn( queryManager );

    pentahoJcrConstants = mock( PentahoJcrConstants.class );
    when( pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER() ).thenReturn( FOLDER_NODE_TYPE );
    when( pentahoJcrConstants.getPHO_NT_PENTAHOFILE() ).thenReturn( FILE_NODE_TYPE );

    pathConversionHelper = mock( IPathConversionHelper.class );
    lockHelper = mock( ILockHelper.class );

    accessVoterManager = mock( IRepositoryAccessVoterManager.class );
    when( accessVoterManager.hasAccess( any(), any(), any(), any() ) ).thenReturn( true );

    nodesByNodeType.put( FOLDER_NODE_TYPE, new ArrayList<>() );
    nodesByNodeType.put( FILE_NODE_TYPE, new ArrayList<>() );

    when( queryManager.createQuery( anyString(), eq( Query.JCR_SQL2 ) ) ).thenAnswer( invocation -> {
      String statement = invocation.getArgument( 0 );
      statements.add( statement );

      String nodeType = statement.contains( FOLDER_NODE_TYPE ) ? FOLDER_NODE_TYPE : FILE_NODE_TYPE;

      return queryReturning( nodesByNodeType.get( nodeType ) );
    } );

    jcrStringHelper = mockStatic( JcrStringHelper.class );
    jcrStringHelper.when( () -> JcrStringHelper.pathEncode( anyString() ) )
      .thenAnswer( invocation -> invocation.getArgument( 0 ) );

    jcrRepositoryFileUtils = mockStatic( JcrRepositoryFileUtils.class );
    jcrRepositoryFileUtils.when( () -> JcrRepositoryFileUtils.isSupportedNodeType( any(), any() ) ).thenReturn( true );
    jcrRepositoryFileUtils.when( () -> JcrRepositoryFileUtils.nodeToFile( any(), any(), any(), any(), any() ) )
      .thenAnswer( invocation -> filesByNode.get( invocation.<Node>getArgument( 4 ) ) );
    jcrRepositoryFileUtils.when( () -> JcrRepositoryFileUtils.getFileMetadata( any(), any() ) )
      .thenReturn( Collections.<String, Serializable>emptyMap() );

    RepositoryFileAcl acl = mock( RepositoryFileAcl.class );
    jcrRepositoryFileAclUtils = mockStatic( JcrRepositoryFileAclUtils.class );
    jcrRepositoryFileAclUtils.when( () -> JcrRepositoryFileAclUtils.getAcl( any(), any(), any() ) ).thenReturn( acl );
  }

  @After
  public void tearDown() {
    jcrStringHelper.close();
    jcrRepositoryFileUtils.close();
    jcrRepositoryFileAclUtils.close();
  }

  // region Tree assembly
  @Test
  public void testBuildsTreeOfFoldersAndFiles() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFolders( folder( ROOT_PATH + "/sales" ) );
    givenFiles( file( ROOT_PATH + "/sales/report.prpt" ), file( ROOT_PATH + "/summary.ktr" ) );

    RepositoryFileTree tree = getTree( request( null ) );

    assertNotNull( tree );
    assertEquals( ROOT_PATH, tree.getFile().getPath() );
    assertEquals( 2, tree.getChildren().size() );
    // children are sorted by file name: sales ( folder ) then summary.ktr
    assertEquals( "sales", tree.getChildren().get( 0 ).getFile().getName() );
    assertEquals( "summary.ktr", tree.getChildren().get( 1 ).getFile().getName() );
    assertEquals( 1, tree.getChildren().get( 0 ).getChildren().size() );
    assertEquals( "report.prpt", tree.getChildren().get( 0 ).getChildren().get( 0 ).getFile().getName() );
  }

  /**
   * the folder filter is the only thing that decides which folders the tree carries, so a file living in a folder it
   * rejected has no folder to hang from and goes with it
   */
  @Test
  public void testDropsFileWhoseFolderTheFolderFilterRejected() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFiles( file( ROOT_PATH + "/sales/report.prpt" ), file( ROOT_PATH + "/summary.ktr" ) );

    RepositoryFileTree tree = getTree( request( TreeNodeFilterSpec.FOLDER_FILTER_TOKEN + MARKETING_FOLDER_PATTERN ) );

    assertNotNull( tree );
    assertEquals( 1, tree.getChildren().size() );
    assertEquals( "summary.ktr", tree.getChildren().get( 0 ).getFile().getName() );
  }

  /**
   * a file filter narrows the files alone: every folder still belongs to the tree, empty ones included
   */
  @Test
  public void testFileFilterKeepsEveryFolder() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFolders( folder( ROOT_PATH + "/test" ) );
    givenFiles( file( ROOT_PATH + "/sales.prpt" ), file( ROOT_PATH + "/data.json" ) );

    RepositoryFileTree tree = getTree( request( TreeNodeFilterSpec.FILE_FILTER_TOKEN + "*.prpt" ) );

    assertNotNull( tree );
    assertEquals( 2, tree.getChildren().size() );
    assertEquals( "sales.prpt", tree.getChildren().get( 0 ).getFile().getName() );
    assertEquals( "test", tree.getChildren().get( 1 ).getFile().getName() );
    assertTrue( tree.getChildren().get( 1 ).getChildren().isEmpty() );

    // both queries are issued: the folders are never left to the file filter
    verify( queryManager, times( 2 ) ).createQuery( anyString(), eq( Query.JCR_SQL2 ) );
  }

  /**
   * with FILES the files are what the caller is after, and the folders keep answering to the folder filter alone
   */
  @Test
  public void testFilesTypeFilterLeavesTheFoldersToTheFolderFilter() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFolders( folder( ROOT_PATH + "/marketing" ), folder( ROOT_PATH + "/sales" ) );
    givenFiles( file( ROOT_PATH + "/sales/report.prpt" ) );

    RepositoryRequest repositoryRequest = request( TreeNodeFilterSpec.FILE_FILTER_TOKEN + "*.prpt" );
    repositoryRequest.setTypes( RepositoryRequest.FILES_TYPE_FILTER.FILES );

    RepositoryFileTree tree = getTree( repositoryRequest );

    assertNotNull( tree );
    assertEquals( 2, tree.getChildren().size() );
    assertEquals( "marketing", tree.getChildren().get( 0 ).getFile().getName() );
    assertEquals( "sales", tree.getChildren().get( 1 ).getFile().getName() );
    assertEquals( "report.prpt", tree.getChildren().get( 1 ).getChildren().get( 0 ).getFile().getName() );
  }

  @Test
  public void testHonorsDepth() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFolders( folder( ROOT_PATH + "/sales" ), folder( ROOT_PATH + "/sales/2026" ) );
    givenFiles( file( ROOT_PATH + "/sales/2026/report.prpt" ) );

    RepositoryRequest repositoryRequest = request( null );
    repositoryRequest.setDepth( 1 );

    RepositoryFileTree tree = getTree( repositoryRequest );

    assertNotNull( tree );
    assertEquals( 1, tree.getChildren().size() );
    assertEquals( "sales", tree.getChildren().get( 0 ).getFile().getName() );
    assertTrue( tree.getChildren().get( 0 ).getChildren().isEmpty() );
  }

  @Test
  public void testDepthZeroReturnsRootOnlyWithoutQuerying() throws Exception {
    givenRoot( folder( ROOT_PATH ) );

    RepositoryRequest repositoryRequest = request( null );
    repositoryRequest.setDepth( 0 );

    RepositoryFileTree tree = getTree( repositoryRequest );

    assertNotNull( tree );
    assertNull( tree.getChildren() );
    verify( queryManager, never() ).createQuery( anyString(), anyString() );
  }

  @Test
  public void testFoldersTypeFilterSkipsTheFileQuery() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFolders( folder( ROOT_PATH + "/sales" ) );
    givenFiles( file( ROOT_PATH + "/summary.ktr" ) );

    RepositoryRequest repositoryRequest = request( null );
    repositoryRequest.setTypes( RepositoryRequest.FILES_TYPE_FILTER.FOLDERS );

    RepositoryFileTree tree = getTree( repositoryRequest );

    assertNotNull( tree );
    assertEquals( 1, tree.getChildren().size() );
    assertEquals( "sales", tree.getChildren().get( 0 ).getFile().getName() );
    verify( queryManager, times( 1 ) ).createQuery( anyString(), eq( Query.JCR_SQL2 ) );
  }

  /**
   * with FOLDERS the files are out of the request, so a file filter cannot empty the tree: every folder is kept.
   * This is a deliberate deviation from the traversal, which returns nothing at all because a folder name hardly
   * ever matches a file name filter
   */
  @Test
  public void testFoldersTypeFilterKeepsEveryFolderEvenWhenOnlyFilesAreFiltered() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFolders( folder( ROOT_PATH + "/empty" ) );
    givenFiles( file( ROOT_PATH + "/summary.prpt" ) );

    RepositoryRequest repositoryRequest = request( TreeNodeFilterSpec.FILE_FILTER_TOKEN + KTR_PATTERN );
    repositoryRequest.setTypes( RepositoryRequest.FILES_TYPE_FILTER.FOLDERS );

    RepositoryFileTree tree = getTree( repositoryRequest );

    assertNotNull( tree );
    assertEquals( 1, tree.getChildren().size() );
    assertEquals( "empty", tree.getChildren().get( 0 ).getFile().getName() );
    verify( queryManager, times( 1 ) ).createQuery( anyString(), eq( Query.JCR_SQL2 ) );
  }

  /**
   * a folder matching the folder filter deeper than a direct child of the root is only reachable when the folders
   * between it and the root are materialized, which is what the node traversal walks through
   */
  @Test
  public void testMaterializesTheFoldersBetweenAMatchingFolderAndTheRoot() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    // only the matching folder is returned by the folder query; "2026" never matches "sales*"
    givenFolders( folder( ROOT_PATH + "/2026/sales" ) );
    givenFiles( file( ROOT_PATH + "/2026/sales/report.prpt" ) );

    RepositoryFileTree tree = getTree( request( TreeNodeFilterSpec.FOLDER_FILTER_TOKEN + FOLDER_PATTERN ) );

    assertNotNull( tree );
    assertEquals( 1, tree.getChildren().size() );

    RepositoryFileTree year = tree.getChildren().get( 0 );
    assertEquals( "2026", year.getFile().getName() );
    assertEquals( 1, year.getChildren().size() );

    RepositoryFileTree sales = year.getChildren().get( 0 );
    assertEquals( "sales", sales.getFile().getName() );
    assertEquals( 1, sales.getChildren().size() );
    assertEquals( "report.prpt", sales.getChildren().get( 0 ).getFile().getName() );
  }

  /**
   * a materialized folder is a folder of the tree like any other, so the files matching the file filter below it
   * belong to the tree as well
   */
  @Test
  public void testFilesAreAttachedToMaterializedFolders() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFolders( folder( ROOT_PATH + "/2026/sales" ) );
    givenFiles( file( ROOT_PATH + "/2026/summary.ktr" ), file( ROOT_PATH + "/2026/summary.prpt" ) );

    RepositoryFileTree tree = getTree( request(
      TreeNodeFilterSpec.FOLDER_FILTER_TOKEN + FOLDER_PATTERN + TreeNodeFilterSpec.FILTER_TOKEN_SEPARATOR
        + TreeNodeFilterSpec.FILE_FILTER_TOKEN + KTR_PATTERN ) );

    assertNotNull( tree );
    assertEquals( 1, tree.getChildren().size() );

    RepositoryFileTree year = tree.getChildren().get( 0 );
    assertEquals( "2026", year.getFile().getName() );
    assertEquals( 2, year.getChildren().size() );
    assertEquals( "sales", year.getChildren().get( 0 ).getFile().getName() );
    assertEquals( "summary.ktr", year.getChildren().get( 1 ).getFile().getName() );
  }

  /**
   * a folder the traversal would refuse to descend into drops the branch, whether it matched the folder filter or it
   * was materialized to carry a matching descendant
   */
  @Test
  public void testDropsBranchWhoseMaterializedFolderIsHidden() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFolders( folder( ROOT_PATH + "/2026/sales" ) );
    filesByNode.put( nodeAt( ROOT_PATH + "/2026" ), hidden( folder( ROOT_PATH + "/2026" ) ) );

    RepositoryFileTree tree = getTree( request( TreeNodeFilterSpec.FOLDER_FILTER_TOKEN + FOLDER_PATTERN ) );

    assertNotNull( tree );
    assertTrue( tree.getChildren().isEmpty() );
  }

  /**
   * a query returns its nodes in no particular order, so a folder may be materialized before the query returns it,
   * which must not duplicate it nor detach what was already attached to it
   */
  @Test
  public void testFolderReturnedAfterItWasMaterializedIsNotDuplicated() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    // the child comes first, so "2026" is materialized before the query returns it
    givenFolders( folder( ROOT_PATH + "/2026/sales" ), folder( ROOT_PATH + "/2026" ) );
    givenFiles( file( ROOT_PATH + "/2026/summary.ktr" ) );

    RepositoryFileTree tree = getTree( request( null ) );

    assertNotNull( tree );
    assertEquals( 1, tree.getChildren().size() );

    RepositoryFileTree year = tree.getChildren().get( 0 );
    assertEquals( "2026", year.getFile().getName() );
    assertEquals( 2, year.getChildren().size() );
    assertEquals( "sales", year.getChildren().get( 0 ).getFile().getName() );
    assertEquals( "summary.ktr", year.getChildren().get( 1 ).getFile().getName() );
  }

  /**
   * the depth is counted from the root, so a folder deeper than the requested depth is left out together with the
   * folders that would only be materialized to carry it
   */
  @Test
  public void testDoesNotMaterializeFoldersBeyondTheRequestedDepth() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFolders( folder( ROOT_PATH + "/2026/sales" ) );

    RepositoryRequest repositoryRequest = request( TreeNodeFilterSpec.FOLDER_FILTER_TOKEN + FOLDER_PATTERN );
    repositoryRequest.setDepth( 1 );

    RepositoryFileTree tree = getTree( repositoryRequest );

    assertNotNull( tree );
    assertTrue( tree.getChildren().isEmpty() );
  }
  // endregion Tree assembly

  // region Node filtering
  @Test
  public void testSkipsHiddenAndAclNodesUnlessHiddenAreRequested() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFiles( hidden( file( ROOT_PATH + "/hidden.ktr" ) ), aclNode( file( ROOT_PATH + "/.acl" ) ),
      file( ROOT_PATH + "/visible.ktr" ) );

    RepositoryFileTree tree = getTree( request( null ) );

    assertNotNull( tree );
    assertEquals( 1, tree.getChildren().size() );
    assertEquals( "visible.ktr", tree.getChildren().get( 0 ).getFile().getName() );
  }

  @Test
  public void testShowHiddenIncludesHiddenNodes() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFiles( hidden( file( ROOT_PATH + "/hidden.ktr" ) ) );

    RepositoryRequest repositoryRequest = request( null );
    repositoryRequest.setShowHidden( true );

    RepositoryFileTree tree = getTree( repositoryRequest );

    assertNotNull( tree );
    assertEquals( 1, tree.getChildren().size() );
  }

  @Test
  public void testHiddenRootReturnsNull() throws Exception {
    givenRoot( hidden( folder( ROOT_PATH ) ) );

    assertNull( getTree( request( null ) ) );
  }

  /**
   * the node traversal asserts the item is a node, so this one must fail the very same way
   */
  @Test( expected = IllegalArgumentException.class )
  public void testNonNodeRootIsRejected() throws Exception {
    Item item = mock( Item.class );
    when( item.isNode() ).thenReturn( false );
    when( session.getItem( ROOT_PATH ) ).thenReturn( item );

    getTree( request( null ) );
  }

  /**
   * a missing path surfaces as the {@link PathNotFoundException} raised by the session, never as a <code>null</code>
   * tree
   */
  @Test( expected = PathNotFoundException.class )
  public void testMissingRootPropagatesPathNotFound() throws Exception {
    when( session.getItem( ROOT_PATH ) ).thenThrow( new PathNotFoundException( ROOT_PATH ) );

    getTree( request( null ) );
  }

  @Test
  public void testSkipsUnsupportedNodeTypes() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    Node unsupported = givenFiles( file( ROOT_PATH + "/summary.ktr" ) ).get( 0 );
    jcrRepositoryFileUtils.when( () -> JcrRepositoryFileUtils.isSupportedNodeType( any(), eq( unsupported ) ) )
      .thenReturn( false );

    RepositoryFileTree tree = getTree( request( null ) );

    assertNotNull( tree );
    assertTrue( tree.getChildren().isEmpty() );
  }

  @Test
  public void testSkipsSystemFoldersDirectlyBelowTheRoot() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    List<Node> folders = givenFolders( folder( ROOT_PATH + "/system" ) );
    Node systemNode = folders.get( 0 );
    when( systemNode.getIdentifier() ).thenReturn( "system-id" );

    Map<String, Serializable> metadata = new HashMap<>();
    metadata.put( IUnifiedRepository.SYSTEM_FOLDER, Boolean.TRUE );
    jcrRepositoryFileUtils.when( () -> JcrRepositoryFileUtils.getFileMetadata( any(), eq( "system-id" ) ) )
      .thenReturn( metadata );

    assertTrue( getTree( requestWithoutSystemFolders() ).getChildren().isEmpty() );

    // the default request includes them
    assertEquals( 1, getTree( request( null ) ).getChildren().size() );
  }

  private static RepositoryRequest requestWithoutSystemFolders() {
    RepositoryRequest repositoryRequest = request( null );
    repositoryRequest.setIncludeSystemFolders( false );
    return repositoryRequest;
  }

  /**
   * the deleted files keep their node type below the trash folder, so the file query returns them; they are no part
   * of any tree, exactly as the traversal never descends into that internal folder
   */
  @Test
  public void testSkipsTheDeletedFilesOfTheTrashFolder() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFolders( folder( TRASH_PATH + "/New Folder" ) );
    givenFiles( file( TRASH_PATH + "/New Folder/report.prpt" ), file( TRASH_PATH + "/deleted.ktr" ),
      file( ROOT_PATH + "/summary.ktr" ) );

    RepositoryFileTree tree = getTree( request( null ) );

    assertNotNull( tree );
    assertEquals( 1, tree.getChildren().size() );
    assertEquals( "summary.ktr", tree.getChildren().get( 0 ).getFile().getName() );
  }

  /**
   * only the part of the path below the root is looked at, so the tree of a deleted folder still carries its content
   */
  @Test
  public void testTreeRootedInsideTheTrashCarriesItsContent() throws Exception {
    String trashedFolderPath = TRASH_PATH + "/New Folder";
    Node rootNode = nodeAt( trashedFolderPath );
    filesByNode.put( rootNode, folder( trashedFolderPath ) );
    when( session.getItem( trashedFolderPath ) ).thenReturn( rootNode );

    givenFiles( file( trashedFolderPath + "/report.prpt" ) );

    // the path of the request is not the one the tree is rooted at, which the caller passes on its own
    RepositoryFileTree tree = JcrTreeQueryUtils.getTreeByQuery( session, pentahoJcrConstants, pathConversionHelper,
      lockHelper, trashedFolderPath, request( null ), accessVoterManager );

    assertNotNull( tree );
    assertEquals( 1, tree.getChildren().size() );
    assertEquals( "report.prpt", tree.getChildren().get( 0 ).getFile().getName() );
  }

  @Test
  public void testSkipsNodesTheAccessVoterDenies() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFiles( file( ROOT_PATH + "/denied.ktr" ), file( ROOT_PATH + "/allowed.ktr" ) );

    when( accessVoterManager.hasAccess( any(), eq( RepositoryFilePermission.READ ), any(), any() ) ).thenAnswer(
      invocation -> !"denied.ktr".equals( invocation.<RepositoryFile>getArgument( 0 ).getName() ) );

    RepositoryFileTree tree = getTree( request( null ) );

    assertNotNull( tree );
    assertEquals( 1, tree.getChildren().size() );
    assertEquals( "allowed.ktr", tree.getChildren().get( 0 ).getFile().getName() );
  }

  @Test
  public void testDeniedRootReturnsNull() throws Exception {
    givenRoot( folder( ROOT_PATH ) );

    when( accessVoterManager.hasAccess( any(), any(), any(), any() ) ).thenReturn( false );

    assertNull( getTree( request( null ) ) );
  }

  /**
   * an ACL that cannot be read because access was denied only skips that node, exactly as the node traversal does
   */
  @Test
  public void testSkipsNodesWhoseAclAccessIsDenied() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    RepositoryFile denied = file( ROOT_PATH + "/denied.ktr" );
    givenFiles( denied, file( ROOT_PATH + "/allowed.ktr" ) );

    jcrRepositoryFileAclUtils.when( () -> JcrRepositoryFileAclUtils.getAcl( any(), any(), eq( denied.getId() ) ) )
      .thenThrow( new AccessDeniedException( "denied" ) );

    RepositoryFileTree tree = getTree( request( null ) );

    assertNotNull( tree );
    assertEquals( 1, tree.getChildren().size() );
    assertEquals( "allowed.ktr", tree.getChildren().get( 0 ).getFile().getName() );
  }

  /**
   * every other repository failure while reading an ACL propagates instead of silently pruning the node
   */
  @Test( expected = RepositoryException.class )
  public void testPropagatesAclRepositoryFailures() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFiles( file( ROOT_PATH + "/summary.ktr" ) );

    jcrRepositoryFileAclUtils.when( () -> JcrRepositoryFileAclUtils.getAcl( any(), any(), any() ) )
      .thenThrow( new RepositoryException( "boom" ) );

    getTree( request( null ) );
  }
  // endregion Node filtering

  // region Statement building
  @Test
  public void testQueriesAreScopedToTheRootPathAndUnfilteredByDefault() throws Exception {
    givenRoot( folder( ROOT_PATH ) );

    getTree( request( null ) );

    assertEquals( 2, statements.size() );
    // the path is quoted, not bracketed: a bracketed path carrying a space matches nothing
    assertEquals( "SELECT * FROM [" + FOLDER_NODE_TYPE + "] WHERE ISDESCENDANTNODE('" + ROOT_PATH + "')",
      statements.get( 0 ) );
    assertEquals( "SELECT * FROM [" + FILE_NODE_TYPE + "] WHERE ISDESCENDANTNODE('" + ROOT_PATH + "')",
      statements.get( 1 ) );
  }

  @Test
  public void testStructuredFilterIsTranslatedIntoNameConstraints() throws Exception {
    givenRoot( folder( ROOT_PATH ) );

    getTree(
      request( TreeNodeFilterSpec.FILE_FILTER_TOKEN + KTR_PATTERN + TreeNodeFilterSpec.INSIDE_FILTER_TOKEN_SEPARATOR
        + KJB_PATTERN + TreeNodeFilterSpec.FILTER_TOKEN_SEPARATOR + TreeNodeFilterSpec.FOLDER_FILTER_TOKEN
        + FOLDER_PATTERN ) );

    assertTrue( statements.get( 0 ).endsWith( " AND (LOWER(LOCALNAME()) LIKE 'sales%')" ) );
    assertTrue( statements.get( 1 )
      .endsWith( " AND (LOWER(LOCALNAME()) LIKE '%.ktr' OR LOWER(LOCALNAME()) LIKE '%.kjb')" ) );
  }

  /**
   * the JCR-SQL2 implementation supports neither the <code>ESCAPE</code> clause nor a <code>LIKE</code> on
   * <code>NAME()</code>, so the constraint may only widen the result set, which the in-memory matching then narrows
   */
  @Test
  public void testLikeConstraintWidensAndExactNamesAreMatchedInMemory() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFiles( file( ROOT_PATH + "/aXbYc1.ktr" ), file( ROOT_PATH + "/a_b%c1.ktr" ) );

    RepositoryFileTree tree = getTree( request( TreeNodeFilterSpec.FILE_FILTER_TOKEN + ESCAPED_PATTERN ) );

    assertTrue( statements.get( 1 ).endsWith( " AND (LOWER(LOCALNAME()) LIKE 'a_b%c%.ktr')" ) );
    assertFalse( statements.get( 1 ).contains( "ESCAPE" ) );
    assertNotNull( tree );
    assertEquals( 1, tree.getChildren().size() );
    assertEquals( "a_b%c1.ktr", tree.getChildren().get( 0 ).getFile().getName() );
  }

  /**
   * <code>LOCALNAME()</code> drops the namespace prefix, so a pattern carrying the separator is not pushed down and
   * is answered by the in-memory matching alone
   */
  @Test
  public void testPatternWithNamespaceSeparatorIsNotPushedDown() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFiles( file( ROOT_PATH + "/summary.ktr" ) );

    RepositoryFileTree tree = getTree( request( TreeNodeFilterSpec.FILE_FILTER_TOKEN + "pho:*" ) );

    assertTrue( statements.get( 1 ).endsWith( "')" ) );
    assertNotNull( tree );
    assertTrue( tree.getChildren().isEmpty() );
  }

  /**
   * the file filter must not prune folders, and the folder filter must not prune files
   */
  @Test
  public void testEachFilterOnlyAppliesToItsOwnNodeType() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFolders( folder( ROOT_PATH + "/sales" ), folder( ROOT_PATH + "/marketing" ) );
    givenFiles( file( ROOT_PATH + "/summary.ktr" ), file( ROOT_PATH + "/summary.prpt" ) );

    RepositoryFileTree tree = getTree( request(
      TreeNodeFilterSpec.FILE_FILTER_TOKEN + KTR_PATTERN + TreeNodeFilterSpec.FILTER_TOKEN_SEPARATOR
        + TreeNodeFilterSpec.FOLDER_FILTER_TOKEN + FOLDER_PATTERN ) );

    assertNotNull( tree );
    assertEquals( 2, tree.getChildren().size() );
    assertEquals( "sales", tree.getChildren().get( 0 ).getFile().getName() );
    assertEquals( "summary.ktr", tree.getChildren().get( 1 ).getFile().getName() );
  }

  /**
   * a wildcard folder filter selects every folder, which is what a missing folder filter does as well
   */
  @Test
  public void testWildcardFolderFilterKeepsTheWholeFolderStructure() throws Exception {
    givenRoot( folder( ROOT_PATH ) );
    givenFolders( folder( ROOT_PATH + "/test" ) );
    givenFiles( file( ROOT_PATH + "/sales.prpt" ), file( ROOT_PATH + "/data.json" ) );

    RepositoryFileTree tree = getTree( request(
      TreeNodeFilterSpec.FILE_FILTER_TOKEN + "*.prpt" + TreeNodeFilterSpec.INSIDE_FILTER_TOKEN_SEPARATOR
        + "*.xanalyzer" + TreeNodeFilterSpec.FILTER_TOKEN_SEPARATOR + TreeNodeFilterSpec.FOLDER_FILTER_TOKEN
        + RepositoryRequest.FILTER_WILDCARD ) );

    assertNotNull( tree );
    assertEquals( 2, tree.getChildren().size() );
    assertEquals( "sales.prpt", tree.getChildren().get( 0 ).getFile().getName() );
    assertEquals( "test", tree.getChildren().get( 1 ).getFile().getName() );
    // the wildcard is not pushed down, the folder query returns every folder below the root
    assertTrue( statements.get( 0 ).endsWith( "')" ) );
  }


  @Test
  public void testLegacyFilterAddsNoNameConstraint() throws Exception {
    givenRoot( folder( ROOT_PATH ) );

    getTree( request( RepositoryRequest.FILTER_WILDCARD + ".ktr" ) );

    assertTrue( statements.get( 0 ).endsWith( "')" ) );
    assertTrue( statements.get( 1 ).endsWith( "')" ) );
  }
  // endregion Statement building

  // region Fixture helpers
  private RepositoryFileTree getTree( final RepositoryRequest repositoryRequest ) throws RepositoryException {
    return JcrTreeQueryUtils.getTreeByQuery( session, pentahoJcrConstants, pathConversionHelper, lockHelper, ROOT_PATH,
      repositoryRequest, accessVoterManager );
  }

  private static RepositoryRequest request( final String childNodeFilter ) {
    RepositoryRequest repositoryRequest = new RepositoryRequest( ROOT_PATH, false, -1, childNodeFilter );
    repositoryRequest.setDepth( -1 );
    return repositoryRequest;
  }

  private void givenRoot( final RepositoryFile rootFile ) throws RepositoryException {
    Node rootNode = mock( Node.class );
    when( rootNode.isNode() ).thenReturn( true );
    when( rootNode.getPath() ).thenReturn( ROOT_PATH );
    when( session.getItem( ROOT_PATH ) ).thenReturn( rootNode );
    nodesByPath.put( ROOT_PATH, rootNode );
    filesByNode.put( rootNode, rootFile );
  }

  private List<Node> givenFolders( final RepositoryFile... folders ) throws RepositoryException {
    return givenNodes( FOLDER_NODE_TYPE, folders );
  }

  private List<Node> givenFiles( final RepositoryFile... files ) throws RepositoryException {
    return givenNodes( FILE_NODE_TYPE, files );
  }

  private List<Node> givenNodes( final String nodeType, final RepositoryFile... files ) throws RepositoryException {
    List<Node> nodes = new ArrayList<>();

    for ( RepositoryFile file : files ) {
      Node node = nodeAt( file.getPath() );
      filesByNode.put( node, file );
      nodes.add( node );
    }

    nodesByNodeType.get( nodeType ).addAll( nodes );

    return nodes;
  }

  /**
   * @return the mocked node of the given path, creating the whole chain of parent nodes up to the root when needed,
   * so that the ancestor folders can be walked exactly as they are in a real repository
   */
  private Node nodeAt( final String path ) throws RepositoryException {
    Node existing = nodesByPath.get( path );

    if ( existing != null ) {
      return existing;
    }

    Node node = mock( Node.class );
    when( node.isNode() ).thenReturn( true );
    when( node.getPath() ).thenReturn( path );
    nodesByPath.put( path, node );

    String parentPath = parentPathOf( path );

    if ( !parentPath.isEmpty() ) {
      Node parent = nodeAt( parentPath );
      when( node.getParent() ).thenReturn( parent );
      // an ancestor that no query returns is still turned into a file, exactly as the materialization does
      filesByNode.putIfAbsent( parent, folder( parentPath ) );
    }

    return node;
  }

  private static String parentPathOf( final String path ) {
    return path.substring( 0, path.lastIndexOf( '/' ) );
  }

  private static Query queryReturning( final List<Node> nodes ) throws RepositoryException {
    Iterator<Node> iterator = new ArrayList<>( nodes ).iterator();

    NodeIterator nodeIterator = mock( NodeIterator.class );
    when( nodeIterator.hasNext() ).thenAnswer( invocation -> iterator.hasNext() );
    when( nodeIterator.nextNode() ).thenAnswer( invocation -> iterator.next() );

    QueryResult queryResult = mock( QueryResult.class );
    when( queryResult.getNodes() ).thenReturn( nodeIterator );

    Query query = mock( Query.class );
    when( query.execute() ).thenReturn( queryResult );

    return query;
  }

  private static RepositoryFile folder( final String path ) {
    return newFile( path, true );
  }

  private static RepositoryFile file( final String path ) {
    return newFile( path, false );
  }

  private static RepositoryFile newFile( final String path, final boolean folder ) {
    String name = path.substring( path.lastIndexOf( '/' ) + 1 );
    return new RepositoryFile.Builder( path, name ).path( path ).folder( folder ).build();
  }

  private static RepositoryFile hidden( final RepositoryFile file ) {
    return new RepositoryFile.Builder( file ).hidden( true ).build();
  }

  private static RepositoryFile aclNode( final RepositoryFile file ) {
    return new RepositoryFile.Builder( file ).aclNode( true ).build();
  }
  // endregion Fixture helpers
}
