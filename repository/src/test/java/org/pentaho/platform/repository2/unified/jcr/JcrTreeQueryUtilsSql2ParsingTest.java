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

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoterManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.repository2.unified.TreeNodeFilterSpec;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Verifies that the statements built by {@link JcrTreeQueryUtils} are accepted by a real JCR-SQL2 engine.
 * <p>
 * The reported failure was
 *
 * <pre>
 * javax.jcr.query.InvalidQueryException: Query:
 * SELECT * FROM [pho_nt:pentahoFile] WHERE ISDESCENDANTNODE([/pentaho/tenant0/home/admin])
 *   AND (LOWER(NAME()) LIKE '%.xanalyzer' ESCAPE(*)'\' OR LOWER(NAME()) LIKE '%.prpt' ESCAPE '\'); expected: )
 * </pre>
 * <p>
 * Unlike {@link JcrTreeQueryUtilsTest}, which asserts the statement text against a mocked query manager, this test
 * hands the statements to a real Jackrabbit query manager, so it also covers what only the execution reveals.
 */
public class JcrTreeQueryUtilsSql2ParsingTest {
  private static final String REPO_CONFIG_FILE = "/jackrabbit/repository-in-memory.xml";
  private static final String TEST_REPOSITORY_LOCATION = "test-jcr-sql2_";

  /**
   * the path of the reported failure
   */
  private static final String ROOT_PATH = "/pentaho/tenant0/home/admin";

  private static final String FILE_NODE_TYPE = "nt:file";
  private static final String FOLDER_NODE_TYPE = "nt:folder";

  /**
   * the filter of the reported failure
   */
  private static final String REPORTED_FILTER =
    TreeNodeFilterSpec.FILE_FILTER_TOKEN + "*.xanalyzer" + TreeNodeFilterSpec.INSIDE_FILTER_TOKEN_SEPARATOR + "*.prpt";

  private static final String DESCENDANT_FILES =
    "SELECT * FROM [" + FILE_NODE_TYPE + "] WHERE ISDESCENDANTNODE('" + ROOT_PATH + "')";

  /**
   * the statement the reported failure carried, which must remain invalid, so that this test keeps proving the engine
   * rejects the <code>ESCAPE</code> clause instead of silently accepting anything
   */
  private static final String STATEMENT_WITH_ESCAPE_CLAUSE = DESCENDANT_FILES
    + " AND (LOWER(LOCALNAME()) LIKE '%.xanalyzer' ESCAPE '\\' OR LOWER(LOCALNAME()) LIKE '%.prpt' ESCAPE '\\')";

  /**
   * a <code>LIKE</code> on <code>NAME()</code>, which parses but is rejected when the engine builds the query, so
   * that this test also keeps proving the second limitation the constraint works around
   */
  private static final String STATEMENT_WITH_NODE_NAME = DESCENDANT_FILES
    + " AND (LOWER(NAME()) LIKE '%.xanalyzer' OR LOWER(NAME()) LIKE '%.prpt')";

  private static TransientRepository repository;
  private static Session session;

  private PentahoJcrConstants pentahoJcrConstants;
  private IRepositoryAccessVoterManager accessVoterManager;

  private MockedStatic<JcrRepositoryFileUtils> jcrRepositoryFileUtils;
  private MockedStatic<JcrRepositoryFileAclUtils> jcrRepositoryFileAclUtils;

  @BeforeClass
  public static void startRepository() throws Exception {
    String home = Files.createTempDirectory( TEST_REPOSITORY_LOCATION ).toAbsolutePath().toString();

    try ( InputStream configStream = JcrTreeQueryUtilsSql2ParsingTest.class.getResourceAsStream( REPO_CONFIG_FILE ) ) {
      assertNotNull( "Missing test repository config " + REPO_CONFIG_FILE, configStream );
      repository = new TransientRepository( RepositoryConfig.create( configStream, home ) );
    }

    session = repository.login( new SimpleCredentials( "admin", "admin".toCharArray() ) );

    // the queried subtree must exist, so that the engine is exercised exactly as it is at runtime
    Node root = session.getRootNode();

    for ( String name : ROOT_PATH.substring( 1 ).split( "/" ) ) {
      root = root.hasNode( name ) ? root.getNode( name ) : root.addNode( name, FOLDER_NODE_TYPE );
    }

    Node sales = root.addNode( "sales", FOLDER_NODE_TYPE );
    root.addNode( "marketing", FOLDER_NODE_TYPE );

    // only a_b.prpt really matches the "a_b.prpt" filter; axb.prpt matches the query constraint alone, because '_'
    // is a LIKE wildcard that cannot be escaped
    addFile( sales, "a_b.prpt" );
    addFile( sales, "axb.prpt" );

    // 100%25.prpt is the percent encoded form of a file named 100%.prpt
    for ( String name : new String[] { "dashboard.xanalyzer", "report.prpt", "notes.txt", "100%25.prpt" } ) {
      addFile( root, name );
    }

    session.save();
  }

  @AfterClass
  public static void shutDownRepository() throws Exception {
    String homeDir = repository.getHomeDir();

    session.logout();
    repository.shutdown();

    FileUtils.deleteDirectory( new File( homeDir ) );

    session = null;
    repository = null;
  }

  @Before
  public void setUp() {
    pentahoJcrConstants = mock( PentahoJcrConstants.class );
    // the real node types are used, so that the engine resolves them and only the name constraint is under test
    when( pentahoJcrConstants.getPHO_NT_PENTAHOFILE() ).thenReturn( FILE_NODE_TYPE );
    when( pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER() ).thenReturn( FOLDER_NODE_TYPE );

    accessVoterManager = mock( IRepositoryAccessVoterManager.class );
    when( accessVoterManager.hasAccess( any(), any(), any(), any() ) ).thenReturn( true );

    jcrRepositoryFileUtils = mockStatic( JcrRepositoryFileUtils.class );
    jcrRepositoryFileUtils.when( () -> JcrRepositoryFileUtils.isSupportedNodeType( any(), any() ) ).thenReturn( true );
    jcrRepositoryFileUtils.when( () -> JcrRepositoryFileUtils.getFileMetadata( any(), any() ) )
      .thenReturn( Collections.<String, Serializable>emptyMap() );
    jcrRepositoryFileUtils.when( () -> JcrRepositoryFileUtils.nodeToFile( any(), any(), any(), any(), any() ) )
      .thenAnswer( invocation -> toFile( invocation.getArgument( 4 ) ) );

    RepositoryFileAcl acl = mock( RepositoryFileAcl.class );
    jcrRepositoryFileAclUtils = mockStatic( JcrRepositoryFileAclUtils.class );
    jcrRepositoryFileAclUtils.when( () -> JcrRepositoryFileAclUtils.getAcl( any(), any(), any() ) ).thenReturn( acl );
  }

  @After
  public void tearDown() {
    jcrRepositoryFileUtils.close();
    jcrRepositoryFileAclUtils.close();
  }

  /**
   * the reported request, end to end: its statements must reach the engine and the tree must carry the matching
   * nodes only
   */
  @Test
  public void testReportedRequestIsAcceptedByTheEngine() throws Exception {
    // no folder filter, so every folder below the root belongs to the tree, empty ones included, and notes.txt is
    // the only file dropped
    assertEquals( List.of( "100%25.prpt", "dashboard.xanalyzer", "marketing", "report.prpt", "sales" ),
      childNamesOfTree( REPORTED_FILTER ) );
  }

  /**
   * both queries carry a multi pattern constraint, which is where the <code>NAME()</code> limitation used to surface
   */
  @Test
  public void testFileAndFolderFiltersAreBothExecutedByTheEngine() throws Exception {
    String filter = REPORTED_FILTER + TreeNodeFilterSpec.FILTER_TOKEN_SEPARATOR
      + TreeNodeFilterSpec.FOLDER_FILTER_TOKEN + "sales" + TreeNodeFilterSpec.INSIDE_FILTER_TOKEN_SEPARATOR
      + "finance*";

    // marketing is dropped by the folder filter, notes.txt by the file filter
    assertEquals( List.of( "100%25.prpt", "dashboard.xanalyzer", "report.prpt", "sales" ),
      childNamesOfTree( filter ) );
  }

  /**
   * the constraint can only widen, so the in-memory matching is what makes the result exact: without it the tree
   * would carry axb.prpt too
   */
  @Test
  public void testInMemoryMatchingNarrowsTheWideningConstraint() throws Exception {
    Query query = session.getWorkspace().getQueryManager()
      .createQuery( DESCENDANT_FILES + " AND (LOWER(LOCALNAME()) LIKE 'a_b.prpt')", Query.JCR_SQL2 );

    assertEquals( 2, query.execute().getNodes().getSize() );

    assertEquals( List.of( "a_b.prpt" ),
      childNamesOfTree( TreeNodeFilterSpec.FILE_FILTER_TOKEN + "a_b.prpt", ROOT_PATH + "/sales" ) );
  }

  /**
   * a node name may be percent encoded while the filter carries the decoded form, so the constraint must keep
   * matching at least every node the in-memory matching would accept
   */
  @Test
  public void testEncodedNodeNameIsStillMatchedByTheWideningConstraint() throws Exception {
    Query query = session.getWorkspace().getQueryManager()
      .createQuery( DESCENDANT_FILES + " AND (LOWER(LOCALNAME()) LIKE '100%.prpt')", Query.JCR_SQL2 );

    assertEquals( 1, query.execute().getNodes().getSize() );
  }

  /**
   * the very statement of the reported failure, which the engine must keep rejecting
   */
  @Test
  public void testStatementWithEscapeClauseIsRejectedByTheEngine() throws Exception {
    try {
      session.getWorkspace().getQueryManager().createQuery( STATEMENT_WITH_ESCAPE_CLAUSE, Query.JCR_SQL2 );
      fail( "the engine was expected to reject the ESCAPE clause" );
    } catch ( InvalidQueryException e ) {
      assertNotNull( e.getMessage() );
    }
  }

  /**
   * a <code>LIKE</code> on <code>NAME()</code> parses, so only the execution reveals it is not supported
   */
  @Test( expected = UnsupportedRepositoryOperationException.class )
  public void testStatementWithNodeNameLikeIsRejectedByTheEngine() throws Exception {
    session.getWorkspace().getQueryManager().createQuery( STATEMENT_WITH_NODE_NAME, Query.JCR_SQL2 ).execute()
      .getNodes();
  }

  /**
   * a folder matching the folder filter deeper than a direct child of the root is only reachable when the folders
   * between it and the root are materialized, which no query returns
   */
  @Test
  public void testNestedMatchingFolderIsReachedThroughItsMaterializedParents() throws Exception {
    Node deepRoot = session.getRootNode().addNode( "deep", FOLDER_NODE_TYPE );
    Node sales = deepRoot.addNode( "2026", FOLDER_NODE_TYPE ).addNode( "sales", FOLDER_NODE_TYPE );
    addFile( sales, "report.prpt" );
    session.save();

    try {
      RepositoryFileTree tree = treeOf( TreeNodeFilterSpec.FOLDER_FILTER_TOKEN + "sales", "/deep" );

      assertEquals( 1, tree.getChildren().size() );

      RepositoryFileTree year = tree.getChildren().get( 0 );
      assertEquals( "2026", year.getFile().getName() );
      assertEquals( 1, year.getChildren().size() );

      RepositoryFileTree salesTree = year.getChildren().get( 0 );
      assertEquals( "sales", salesTree.getFile().getName() );
      assertEquals( List.of( "report.prpt" ), childNamesOf( salesTree ) );
    } finally {
      deepRoot.remove();
      session.save();
    }
  }

  /**
   * the deleted files keep their node type below the trash folder, so the file query returns them; they are no part
   * of any tree, exactly as the traversal never descends into that internal folder
   */
  @Test
  public void testDeletedFilesOfTheTrashFolderAreLeftOut() throws Exception {
    Node deepRoot = session.getRootNode().addNode( "deep", FOLDER_NODE_TYPE );
    // the pho namespace of the real trash folder id is not registered here, and the check does not look at it
    Node trashedFolder = deepRoot.addNode( ".trash", FOLDER_NODE_TYPE ).addNode( "d78ca35b", FOLDER_NODE_TYPE )
      .addNode( "New Folder", FOLDER_NODE_TYPE );
    addFile( trashedFolder, "deleted.prpt" );
    addFile( deepRoot, "kept.prpt" );
    session.save();

    try {
      assertEquals( List.of( "kept.prpt" ),
        childNamesOfTree( TreeNodeFilterSpec.FILE_FILTER_TOKEN + "*.prpt", "/deep" ) );

      // the tree of the deleted folder itself still carries its content
      assertEquals( List.of( "deleted.prpt" ), childNamesOfTree( TreeNodeFilterSpec.FILE_FILTER_TOKEN + "*.prpt",
        "/deep/.trash/d78ca35b/New Folder" ) );
    } finally {
      deepRoot.remove();
      session.save();
    }
  }

  /**
   * a wildcard folder filter makes every folder a match, which brings the whole folder structure back next to the
   * narrowed set of files: the empty marketing folder belongs to the tree again
   */
  @Test
  public void testWildcardFolderFilterKeepsTheWholeFolderStructure() throws Exception {
    String filter = REPORTED_FILTER + TreeNodeFilterSpec.FILTER_TOKEN_SEPARATOR
      + TreeNodeFilterSpec.FOLDER_FILTER_TOKEN + RepositoryRequest.FILTER_WILDCARD;

    assertEquals( List.of( "100%25.prpt", "dashboard.xanalyzer", "marketing", "report.prpt", "sales" ),
      childNamesOfTree( filter ) );
  }

  /**
   * the bracketed path of the JCR-SQL2 statement is a node name, and the engine silently matches nothing when a
   * segment carries a space, which repository names commonly do; the quoted path the statements carry behaves
   */
  @Test
  public void testRootPathCarryingASpaceIsMatchedByTheEngine() throws Exception {
    Node spaced = session.getRootNode().addNode( "My Reports", FOLDER_NODE_TYPE );
    addFile( spaced, "report.prpt" );
    session.save();

    try {
      Query bracketed = session.getWorkspace().getQueryManager().createQuery(
        "SELECT * FROM [" + FILE_NODE_TYPE + "] WHERE ISDESCENDANTNODE([/My Reports])", Query.JCR_SQL2 );
      assertEquals( 0, bracketed.execute().getNodes().getSize() );

      assertEquals( List.of( "report.prpt" ),
        childNamesOfTree( TreeNodeFilterSpec.FILE_FILTER_TOKEN + "*.prpt", "/My Reports" ) );
    } finally {
      spaced.remove();
      session.save();
    }
  }

  // region Fixture helpers

  /**
   * @return the sorted names of the children of the tree the given filter builds, rooted at {@link #ROOT_PATH}
   */
  private List<String> childNamesOfTree( final String childNodeFilter ) throws RepositoryException {
    return childNamesOfTree( childNodeFilter, ROOT_PATH );
  }

  /**
   * @return the sorted names of the children of the tree the given filter builds
   */
  private List<String> childNamesOfTree( final String childNodeFilter, final String rootPath )
    throws RepositoryException {
    return childNamesOf( treeOf( childNodeFilter, rootPath ) );
  }

  private static List<String> childNamesOf( final RepositoryFileTree tree ) {
    List<String> names = new ArrayList<>();

    for ( RepositoryFileTree child : tree.getChildren() ) {
      names.add( child.getFile().getName() );
    }

    Collections.sort( names );

    return names;
  }

  /**
   * @return the tree the given filter builds, never <code>null</code>
   */
  private RepositoryFileTree treeOf( final String childNodeFilter, final String rootPath ) throws RepositoryException {
    RepositoryRequest repositoryRequest = new RepositoryRequest( rootPath, true, -1, childNodeFilter );
    repositoryRequest.setDepth( -1 );

    RepositoryFileTree tree = JcrTreeQueryUtils.getTreeByQuery( session, pentahoJcrConstants,
      mock( IPathConversionHelper.class ), mock( ILockHelper.class ), rootPath, repositoryRequest,
      accessVoterManager );

    assertNotNull( tree );

    return tree;
  }


  private static void addFile( final Node parent, final String name ) throws RepositoryException {
    Node content = parent.addNode( name, FILE_NODE_TYPE ).addNode( "jcr:content", "nt:resource" );
    content.setProperty( "jcr:data",
      session.getValueFactory().createBinary( new ByteArrayInputStream( "x".getBytes( StandardCharsets.UTF_8 ) ) ) );
  }

  private static RepositoryFile toFile( final Node node ) throws RepositoryException {
    String path = node.getPath();
    String name = path.substring( path.lastIndexOf( '/' ) + 1 );

    return new RepositoryFile.Builder( path, name ).path( path ).folder( node.isNodeType( FOLDER_NODE_TYPE ) ).build();
  }
  // endregion Fixture helpers
}
