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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.jackrabbit.core.VersionManagerImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoterManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;


/**
 * @author Tatsiana_Kasiankova
 */
@RunWith( MockitoJUnitRunner.class )
public class JcrRepositoryFileUtilsTest {
  /**
   *
   */
  private static final String VERSION_NAME_TEST = "Version Name Test";
  /**
   *
   */
  private static final String UNSUPPORTED_REPOSITORY_OPERATION_EXCEPTION_TEST_MESSAGE =
    "Unsupported Repository Operation Exception Test Message";
  /**
   *
   */
  private static final String REPOSITORY_EXCEPTION_TEST_MESSAGE = "Repository Exception Test Message";
  private PentahoJcrConstants pJcrConstMock = mock( PentahoJcrConstants.class );
  private Node nodeMock = mock( Node.class );
  private VersionManager vmanagerMock = mock( VersionManager.class );
  private Workspace workspaceMock = mock( Workspace.class );
  private Session sessionMock = mock( Session.class );
  private PentahoSystem pentahoSystemMock = mock( PentahoSystem.class );
  private IRepositoryVersionManager repositoryVersionManagerMockTrue = mock( IRepositoryVersionManager.class );
  private IRepositoryVersionManager repositoryVersionManagerMockFalse = mock( IRepositoryVersionManager.class );

  @Before
  public void setUp() throws UnsupportedRepositoryOperationException, RepositoryException {
    when( workspaceMock.getVersionManager() ).thenReturn( vmanagerMock );
    when( sessionMock.getWorkspace() ).thenReturn( workspaceMock );
    when( repositoryVersionManagerMockTrue.isVersioningEnabled( nullable( String.class ) ) ).thenReturn( true );
    when( repositoryVersionManagerMockFalse.isVersioningEnabled( nullable( String.class ) ) ).thenReturn( false );
  }

  /**
   * This test covers the change for avoiding NPE from Jackrabbit: see http://jira.pentaho.com/browse/BACKLOG-175
   *
   * @throws UnsupportedRepositoryOperationException
   * @throws RepositoryException
   */
  @Test
  public void testNoNPEThrows_WhenNPEGettingFromContentRepository() throws Exception {
    when( vmanagerMock.getBaseVersion( nullable( String.class ) ) ).thenThrow( new NullPointerException() );
    try {
      String versionId = JcrRepositoryFileUtils.getVersionId( sessionMock, pJcrConstMock, nodeMock );
      assertNull( versionId );
    } catch ( NullPointerException e ) {
      fail( "The exception shold not be thrown: " + e );
    }

  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testRepositoryExceptionThrows() throws Exception {
    when( vmanagerMock.getBaseVersion( nullable( String.class ) ) ).thenThrow(
      new RepositoryException( REPOSITORY_EXCEPTION_TEST_MESSAGE ) );
    exception.expect( RepositoryException.class );
    exception.expectMessage( REPOSITORY_EXCEPTION_TEST_MESSAGE );
    JcrRepositoryFileUtils.getVersionId( sessionMock, pJcrConstMock, nodeMock );
  }

  @Test
  public void testUnsupportedRepositoryOperationExceptionThrows() throws Exception {
    when( workspaceMock.getVersionManager() ).thenThrow(
      new UnsupportedRepositoryOperationException( UNSUPPORTED_REPOSITORY_OPERATION_EXCEPTION_TEST_MESSAGE ) );
    exception.expect( UnsupportedRepositoryOperationException.class );
    exception.expectMessage( UNSUPPORTED_REPOSITORY_OPERATION_EXCEPTION_TEST_MESSAGE );
    JcrRepositoryFileUtils.getVersionId( sessionMock, pJcrConstMock, nodeMock );
  }

  @Test
  public void testVersionIdIsNull_WhenJCRReturnsNull() throws Exception {
    when( vmanagerMock.getBaseVersion( nullable( String.class ) ) ).thenReturn( null );
    try {
      String versionId = JcrRepositoryFileUtils.getVersionId( sessionMock, pJcrConstMock, nodeMock );
      assertNull( versionId );
    } catch ( Exception e ) {
      fail( "No exception shold be here but it is: " + e );
    }
  }

  @Test
  public void testVersionId_Success() throws Exception {
    Version version = mock( Version.class );
    when( version.getName() ).thenReturn( VERSION_NAME_TEST );
    when( vmanagerMock.getBaseVersion( nullable( String.class ) ) ).thenReturn( version );
    try {
      String versionId = JcrRepositoryFileUtils.getVersionId( sessionMock, pJcrConstMock, nodeMock );
      assertTrue( VERSION_NAME_TEST.equals( versionId ) );
    } catch ( Exception e ) {
      fail( "No exception shold be here but it is: " + e );
    }
  }

  @Test
  public void testCheckinNearestVersionableNodeIfNecessary() throws Exception {
    String versionMessage = "Version message";
    String username = "admin";

    Date curDate = new Date();

    Node mockNode = mock( Node.class );
    Node parentNode = mock( Node.class );
    IPentahoSession pentahoSession = mock( IPentahoSession.class );
    when( pentahoSession.getName() ).thenReturn( username );

    JcrRepositoryFileUtils.setRepositoryVersionManager( repositoryVersionManagerMockTrue );

    VersionManagerImpl versionManager = mock( VersionManagerImpl.class );
    when( workspaceMock.getVersionManager() ).thenReturn( versionManager );

    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( sessionMock );

    when( mockNode.isNodeType( pentahoJcrConstants.getPHO_MIX_VERSIONABLE() ) ).thenReturn( Boolean.TRUE );
    when( mockNode.getIdentifier() ).thenReturn( "file-id" );

    PentahoSessionHolder.setSession( pentahoSession );

    JcrRepositoryFileUtils
      .checkinNearestVersionableNodeIfNecessary( sessionMock, pentahoJcrConstants, mockNode, versionMessage, curDate,
        true );

    Calendar cal = Calendar.getInstance();
    if ( curDate != null ) {
      cal.setTime( curDate );
    } else {
      cal.setTime( new Date() );
    }

    // validate checkin was called
    verify( versionManager ).checkin( mockNode.getPath(), cal );
  }

  @Test
  public void testCheckinNearestVersionableNodeIfNecessaryWithoutVersioning() throws Exception {
    String versionMessage = "Version message";
    String username = "admin";
    String mockNodeId = "12345";
    String versionName = "versionName";
    String versionableIdentifier = "versionableIdentifier";

    Date curDate = new Date();

    Calendar cal = Calendar.getInstance();
    if ( curDate != null ) {
      cal.setTime( curDate );
    } else {
      cal.setTime( new Date() );
    }

    // disable versioning
    JcrRepositoryFileUtils.setRepositoryVersionManager( repositoryVersionManagerMockFalse );

    Node mockNode = mock( Node.class );
    Node parentNode = mock( Node.class );
    Node versionNode = mock( Node.class );
    Version mockVersion = mock( Version.class );
    Version mockVersion2 = mock( Version.class );
    VersionManagerImpl versionManager = mock( VersionManagerImpl.class );
    VersionHistory mockVersionHistory = mock( VersionHistory.class );
    IPentahoSession pentahoSession = mock( IPentahoSession.class );

    when( pentahoSession.getName() ).thenReturn( username );
    when( sessionMock.getNodeByIdentifier( nullable( String.class ) ) ).thenReturn( mockNode );

    String[] mockVersionLabels = { "label1" };
    when( mockVersionHistory.getVersionLabels( mockVersion ) ).thenReturn( mockVersionLabels );
    when( mockVersionHistory.getVersionableIdentifier() ).thenReturn( versionableIdentifier );

    Version[] mockVersionsList = { mockVersion, mockVersion2 };
    Version[] mockVersionsList2 = { mockVersion };
    when( mockVersion.getSuccessors() ).thenReturn( mockVersionsList, mockVersionsList2, null );
    when( mockVersion.getNode( any() ) ).thenReturn( versionNode );
    when( mockVersion.getName() ).thenReturn( versionName );
    when( mockVersion.getCreated() ).thenReturn( cal );

    when( versionManager.getVersionHistory( mockNode.getPath() ) ).thenReturn( mockVersionHistory );
    when( workspaceMock.getVersionManager() ).thenReturn( versionManager );
    when( mockVersionHistory.getRootVersion() ).thenReturn( mockVersion );

    when( mockNode.getIdentifier() ).thenReturn( mockNodeId );

    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( sessionMock );

    when( mockNode.isNodeType( pentahoJcrConstants.getPHO_MIX_VERSIONABLE() ) ).thenReturn( Boolean.TRUE );

    PentahoSessionHolder.setSession( pentahoSession );

    JcrRepositoryFileUtils
      .checkinNearestVersionableNodeIfNecessary( sessionMock, pentahoJcrConstants, mockNode, versionMessage, curDate,
        true );

    // verify checkin was called
    verify( versionManager ).checkin( mockNode.getPath(), cal );

    // verify version is deleted
    verify( mockVersionHistory ).removeVersion( any() );
  }

  @Test
  public void shouldWaitForCurrentVersionReadBeforePruningPreviousVersion() throws Exception {
    String fileId = "file-id";
    IPentahoSession previousSession = PentahoSessionHolder.getSession();
    IRepositoryVersionManager previousVersionManager = JcrRepositoryFileUtils.getRepositoryVersionManager();
    IPentahoSession testSession = mock( IPentahoSession.class );
    when( testSession.getName() ).thenReturn( "admin" );
    PentahoSessionHolder.setSession( testSession );
    Node fileNode = mock( Node.class );
    Node frozenNode = mock( Node.class );
    Version version = mock( Version.class );
    VersionManagerImpl versionManager = mock( VersionManagerImpl.class );
    VersionHistory versionHistory = mock( VersionHistory.class );
    Session readSession = mock( Session.class );
    Workspace readWorkspace = mock( Workspace.class );
    when( pJcrConstMock.getJCR_FROZENNODE() ).thenReturn( "jcr:frozenNode" );
    when( pJcrConstMock.getPHO_MIX_VERSIONABLE() ).thenReturn( "mix:versionable" );
    when( readSession.getNodeByIdentifier( fileId ) ).thenReturn( fileNode );
    when( readSession.getWorkspace() ).thenReturn( readWorkspace );
    when( readWorkspace.getVersionManager() ).thenReturn( versionManager );
    when( versionManager.getBaseVersion( any() ) ).thenReturn( version );
    when( version.getNode( "jcr:frozenNode" ) ).thenReturn( frozenNode );
    when( sessionMock.getNodeByIdentifier( fileId ) ).thenReturn( nodeMock );
    when( nodeMock.isNodeType( "mix:versionable" ) ).thenReturn( true );
    when( nodeMock.getPath() ).thenReturn( "/file" );
    when( nodeMock.getIdentifier() ).thenReturn( fileId );
    when( workspaceMock.getVersionManager() ).thenReturn( versionManager );
    when( versionManager.getVersionHistory( "/file" ) ).thenReturn( versionHistory );

    CountDownLatch readEnteredTransformer = new CountDownLatch( 1 );
    CountDownLatch releaseRead = new CountDownLatch( 1 );
    CountDownLatch writerStarted = new CountDownLatch( 1 );
    ExecutorService executor = Executors.newFixedThreadPool( 2 );
    ITransformer<IRepositoryFileData> transformer = mock( ITransformer.class );
    doAnswer( invocation -> {
      readEnteredTransformer.countDown();
      assertTrue( "Timed out waiting to release the content read", releaseRead.await( 5, TimeUnit.SECONDS ) );
      return null;
    } ).when( transformer ).fromContentNode( any(), any(), any() );

    VersionSummary previousVersion = mock( VersionSummary.class );
    VersionSummary olderVersion = mock( VersionSummary.class );
    when( olderVersion.getId() ).thenReturn( "v1" );

    try ( MockedStatic<JcrRepositoryFileUtils> utilities = mockStatic( JcrRepositoryFileUtils.class,
        org.mockito.Mockito.CALLS_REAL_METHODS ) ) {
      utilities.when( () -> JcrRepositoryFileUtils.isVersioned( readSession, pJcrConstMock, fileNode ) )
        .thenReturn( true );
      utilities.when( () -> JcrRepositoryFileUtils.isPentahoFolder( pJcrConstMock, frozenNode ) ).thenReturn( false );
      utilities.when( () -> JcrRepositoryFileUtils.getVersionSummaries( sessionMock, pJcrConstMock, fileId, true ) )
        .thenReturn( java.util.Arrays.asList( olderVersion, previousVersion ) );
      JcrRepositoryFileUtils.setRepositoryVersionManager( repositoryVersionManagerMockFalse );

      Future<IRepositoryFileData> read = executor.submit( () -> JcrRepositoryFileUtils.getContent( readSession,
        pJcrConstMock, fileId, null, transformer ) );
      assertTrue( "Content read did not reach the transformer",
        readEnteredTransformer.await( 5, TimeUnit.SECONDS ) );

      Future<?> write = executor.submit( () -> {
        writerStarted.countDown();
        try ( MockedStatic<JcrRepositoryFileUtils> writerUtilities = mockStatic( JcrRepositoryFileUtils.class,
            org.mockito.Mockito.CALLS_REAL_METHODS ) ) {
          writerUtilities.when( () -> JcrRepositoryFileUtils.getVersionSummaries( sessionMock, pJcrConstMock, fileId,
              true ) ).thenReturn( java.util.Arrays.asList( olderVersion, previousVersion ) );
          JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary( sessionMock, pJcrConstMock, nodeMock,
            "update", new Date(), false );
          return null;
        }
      } );
      assertTrue( "Writer did not start", writerStarted.await( 5, TimeUnit.SECONDS ) );
      try {
        write.get( 200, TimeUnit.MILLISECONDS );
        fail( "Version pruning completed while its content was being read" );
      } catch ( TimeoutException expected ) {
        verify( versionManager, never() ).checkin( any(), any() );
      }

      releaseRead.countDown();
      read.get( 5, TimeUnit.SECONDS );
      write.get( 5, TimeUnit.SECONDS );
      verify( versionManager ).checkin( any(), any() );
      verify( versionHistory ).removeVersion( "v1" );
    } finally {
      releaseRead.countDown();
      executor.shutdownNow();
      JcrRepositoryFileUtils.setRepositoryVersionManager( previousVersionManager );
      if ( previousSession == null ) {
        PentahoSessionHolder.removeSession();
      } else {
        PentahoSessionHolder.setSession( previousSession );
      }
    }
  }

  @Test
  public void testCheckNodeForTree() {
    List<RepositoryFileTree> children = new ArrayList<>();
    IPathConversionHelper pathConversionHelper = new DefaultPathConversionHelper();
    ILockHelper lockHelperMock = mock( ILockHelper.class );
    IRepositoryAccessVoterManager repositoryAccessVoterManagerMock = mock( IRepositoryAccessVoterManager.class );
    MutableBoolean foundFiltered = new MutableBoolean();
    RepositoryFile fileMock = mock( RepositoryFile.class );

    when( fileMock.getId() ).thenReturn( 1 );
    try ( MockedStatic<JcrRepositoryFileUtils> jcrRepositoryFileUtils = mockStatic( JcrRepositoryFileUtils.class ) ) {
      jcrRepositoryFileUtils.when( () -> JcrRepositoryFileUtils.checkNodeForTree( nodeMock, children, sessionMock,
        pJcrConstMock, pathConversionHelper, "childNodeFilter", lockHelperMock, 0, false,
        repositoryAccessVoterManagerMock, RepositoryRequest.FILES_TYPE_FILTER.FOLDERS, foundFiltered,
        true, false, "/" ) ).thenCallRealMethod();
      jcrRepositoryFileUtils.when( () -> JcrRepositoryFileUtils.nodeToFile( sessionMock, pJcrConstMock,
        pathConversionHelper, lockHelperMock, nodeMock ) ).thenReturn( fileMock );
      jcrRepositoryFileUtils.when( () -> JcrRepositoryFileUtils.isSupportedNodeType( pJcrConstMock, nodeMock ) )
        .thenReturn( true );

      try ( MockedStatic<JcrRepositoryFileAclUtils> jcrRepositoryFileAclUtils = mockStatic( JcrRepositoryFileAclUtils.class ) ) {
        jcrRepositoryFileAclUtils.when( () -> JcrRepositoryFileAclUtils.getAcl( sessionMock, pJcrConstMock, 1 ) )
          .thenThrow( new AccessDeniedException() );

        try {
          JcrRepositoryFileUtils.checkNodeForTree( nodeMock, children, sessionMock, pJcrConstMock, pathConversionHelper,
            "childNodeFilter", lockHelperMock, 0, false, repositoryAccessVoterManagerMock,
            RepositoryRequest.FILES_TYPE_FILTER.FOLDERS, foundFiltered, true, false, "/" );
        } catch ( Exception e ) {
          fail();
        }
      }
    }
  }
}
