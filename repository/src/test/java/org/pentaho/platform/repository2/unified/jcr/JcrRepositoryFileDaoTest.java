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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import org.apache.jackrabbit.core.VersionManagerImpl;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoterManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryDefaultAclHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.springframework.dao.DataAccessException;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import java.util.List;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class JcrRepositoryFileDaoTest {

  private JcrRepositoryFileDao dao;

  private IRepositoryAccessVoterManager accessVoterManager;

  private IPentahoSession pentahoSession;
  private CountDownLatch jcrCallbackStarted;
  private Session session;
  private Node node;
  private VersionManagerImpl versionManager;
  private List<ITransformer<IRepositoryFileData>> transformers;

  @Before
  public void setUp() throws RepositoryException {
    jcrCallbackStarted = new CountDownLatch( 1 );
    node = mock( Node.class );
    Node nodeParent = mock( Node.class );
    when( node.getIdentifier() ).thenReturn( "" );
    when( nodeParent.getIdentifier() ).thenReturn( "" );
    when( node.getParent() ).thenReturn( nodeParent );
    when( node.isNodeType( "null:pentahoFile" ) ).thenReturn( true );
    when( node.isNodeType( "null:pentahoVersionable" ) ).thenReturn( true );
    versionManager = mock( VersionManagerImpl.class );
    Workspace workspace = mock( Workspace.class );
    when( workspace.getVersionManager() ).thenReturn( versionManager );
    session = mock( Session.class );
    when( session.getWorkspace() ).thenReturn( workspace );
    when( session.getNodeByIdentifier( nullable( String.class) ) ).thenReturn( node );
    when( session.getItem( nullable( String.class) ) ).thenReturn( node );
    when( node.getPath() ).thenReturn( "/file" );
    VersionHistory versionHistory = mock( VersionHistory.class );
    when( versionManager.getVersionHistory( nullable( String.class ) ) ).thenReturn( versionHistory );
    Version rootVersion = mock( Version.class );
    Version currentVersion = mock( Version.class );
    Node frozenNode = mock( Node.class );
    Node rootNode = mock( Node.class );
    Property lastModified = mock( Property.class );
    when( session.getRootNode() ).thenReturn( rootNode );
    when( versionHistory.getRootVersion() ).thenReturn( rootVersion );
    when( rootVersion.getSuccessors() ).thenReturn( new Version[] { currentVersion } );
    when( currentVersion.getSuccessors() ).thenReturn( new Version[ 0 ] );
    when( versionHistory.getVersion( nullable( String.class ) ) ).thenReturn( currentVersion );
    when( versionHistory.getVersionLabels( currentVersion ) ).thenReturn( new String[ 0 ] );
    when( versionHistory.getVersionableIdentifier() ).thenReturn( "file" );
    when( currentVersion.getName() ).thenReturn( "version-id" );
    when( currentVersion.getCreated() ).thenReturn( Calendar.getInstance() );
    when( currentVersion.getNode( nullable( String.class ) ) ).thenReturn( frozenNode );
    NodeIterator noChildren = mock( NodeIterator.class );
    when( noChildren.hasNext() ).thenReturn( false );
    when( frozenNode.getNodes() ).thenReturn( noChildren );
    when( currentVersion.getFrozenNode() ).thenReturn( frozenNode );
    when( frozenNode.getIdentifier() ).thenReturn( "file" );
    when( frozenNode.getName() ).thenReturn( "file" );
    when( frozenNode.getPath() ).thenReturn( "/file" );
    when( frozenNode.getProperty( org.mockito.ArgumentMatchers.anyString() ) ).thenReturn( lastModified );
    when( node.getProperty( org.mockito.ArgumentMatchers.anyString() ) ).thenReturn( lastModified );
    when( lastModified.getDate() ).thenReturn( Calendar.getInstance() );
    when( frozenNode.isNodeType( "null:pentahoFile" ) ).thenReturn( true );
    when( frozenNode.getNode( org.mockito.ArgumentMatchers.anyString() ) )
      .thenThrow( new PathNotFoundException() );
    when( node.getNode( org.mockito.ArgumentMatchers.anyString() ) )
      .thenThrow( new PathNotFoundException() );
    pentahoSession = mock( IPentahoSession.class );
    PentahoSessionHolder.setSession( pentahoSession );
    IRepositoryVersionManager repositoryVersionManager = mock( IRepositoryVersionManager.class );
    when( repositoryVersionManager.isVersioningEnabled( nullable( String.class) ) ).thenReturn( true );
    PentahoSystem.registerObject( repositoryVersionManager );
    JcrTemplate jcrTemplate = new JcrTemplate() {
      @Override
      public Object execute( JcrCallback callback ) throws DataAccessException {
        jcrCallbackStarted.countDown();
        try {
          return callback.doInJcr( session );
        } catch ( Exception e ) {
          // wrapping exception to comply overriding rules
          throw new RuntimeException( e );
        }
      }
    };
    transformers = new ArrayList<>();
    IPathConversionHelper pathConversionHelper = new DefaultPathConversionHelper();
    IRepositoryFileAclDao aclDao = mock( IRepositoryFileAclDao.class );
    accessVoterManager = mock( IRepositoryAccessVoterManager.class );
    JcrRepositoryFileDao jcrDao = new JcrRepositoryFileDao( jcrTemplate, transformers, null, null,
      pathConversionHelper, aclDao, null, accessVoterManager );
    dao = spy( jcrDao );
  }

  @Test
  public void shouldWaitForCurrentContentReadBeforeUpdatingFile() throws Exception {
    String fileId = "file-content-lock-update-test";
    RepositoryFile file = mock( RepositoryFile.class );
    when( file.getId() ).thenReturn( fileId );
    when( file.isFolder() ).thenReturn( false );
    Lock readLock = JcrRepositoryFileUtils.getFileContentLock( fileId ).readLock();
    Lock ancestorReadLock = JcrRepositoryFileUtils.getFileContentLock( "" ).readLock();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    CountDownLatch updateStarted = new CountDownLatch( 1 );
    boolean readLockReleased = false;
    boolean ancestorReadLockReleased = false;

    readLock.lock();
    ancestorReadLock.lock();
    try {
      Future<?> update = executor.submit( () -> {
        updateStarted.countDown();
        dao.updateFile( file, mock( IRepositoryFileData.class ), null );
      } );
      assertTrue( "File update worker should start", updateStarted.await( 5, TimeUnit.SECONDS ) );
      assertStillBlocked( "File update must wait while content is being read", update );
      readLock.unlock();
      readLockReleased = true;
      assertStillBlocked( "File update must wait while its versionable ancestor is being read", update );
      ancestorReadLock.unlock();
      ancestorReadLockReleased = true;
      update.get( 5, TimeUnit.SECONDS );
    } finally {
      if ( !readLockReleased ) {
        readLock.unlock();
      }
      if ( !ancestorReadLockReleased ) {
        ancestorReadLock.unlock();
      }
      executor.shutdownNow();
    }
  }

  private static void assertStillBlocked( final String message, final Future<?> future ) throws Exception {
    try {
      future.get( 100, TimeUnit.MILLISECONDS );
      fail( message );
    } catch ( TimeoutException expected ) {
      assertFalse( message, future.isDone() );
    }
  }

  @Test
  public void shouldReadContentTypeUnderContentReadLock() throws Exception {
    String fileId = "file-content-lock-type-test";
    CountDownLatch contentTypeLookupStarted = new CountDownLatch( 1 );
    AtomicInteger nodeLookupCount = new AtomicInteger();
    Property contentType = mock( Property.class );
    when( contentType.getString() ).thenReturn( "application/test" );
    when( node.getProperty( org.mockito.ArgumentMatchers.anyString() ) ).thenReturn( contentType );

    Node frozenNode = mock( Node.class );
    Version version = mock( Version.class );
    when( versionManager.getBaseVersion( nullable( String.class ) ) ).thenReturn( version );
    when( version.getNode( nullable( String.class ) ) ).thenReturn( frozenNode );
    doReturn( node ).when( session ).getNodeByIdentifier( org.mockito.ArgumentMatchers.anyString() );
    org.mockito.Mockito.doAnswer( invocation -> {
      if ( nodeLookupCount.incrementAndGet() == 1 ) {
        contentTypeLookupStarted.countDown();
      } else if ( nodeLookupCount.get() == 3 ) {
        throw new ItemNotFoundException();
      }
      return node;
    } ).when( session ).getNodeByIdentifier( org.mockito.ArgumentMatchers.anyString() );

    IRepositoryFileData data = mock( IRepositoryFileData.class );
    ITransformer<IRepositoryFileData> transformer = mock( ITransformer.class );
    when( transformer.canRead( "application/test", IRepositoryFileData.class ) ).thenReturn( true );
    when( transformer.fromContentNode( org.mockito.ArgumentMatchers.any( Session.class ),
        org.mockito.ArgumentMatchers.any( PentahoJcrConstants.class ), org.mockito.ArgumentMatchers.any( Node.class ) ) )
        .thenReturn( data );
    transformers.add( transformer );

    Lock contentWriteLock =
        JcrRepositoryFileUtils.getFileContentLock( fileId ).writeLock();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    boolean writeLockReleased = false;
    contentWriteLock.lock();
    try {
      Future<IRepositoryFileData> read = executor.submit( () -> dao.getData( fileId, null, IRepositoryFileData.class ) );
      assertFalse( "Content type lookup must wait for an active content writer",
          contentTypeLookupStarted.await( 100, TimeUnit.MILLISECONDS ) );
      contentWriteLock.unlock();
      writeLockReleased = true;
      assertTrue( "Content type lookup should continue after the writer finishes",
          contentTypeLookupStarted.await( 5, TimeUnit.SECONDS ) );
      assertSame( data, read.get( 5, TimeUnit.SECONDS ) );
    } finally {
      if ( !writeLockReleased ) {
        contentWriteLock.unlock();
      }
      executor.shutdownNow();
    }
  }

  @Test
  public void shouldWaitForContentWritersBeforeReadingVersionSummary() throws Exception {
    assertReadOperationWaitsForContentWriter( "file-content-lock-version-summary-test",
        () -> dao.getVersionSummary( "file-content-lock-version-summary-test", "version-id" ) );
  }

  @Test
  public void shouldWaitForContentWritersBeforeReadingVersionSummaries() throws Exception {
    assertReadOperationWaitsForContentWriter( "file-content-lock-version-summaries-test",
        () -> dao.getVersionSummaries( "file-content-lock-version-summaries-test" ) );
  }

  @Test
  public void shouldWaitForContentWritersBeforeReadingVersionedFile() throws Exception {
    assertReadOperationWaitsForContentWriter( "file-content-lock-versioned-file-test",
        () -> dao.getFile( "file-content-lock-versioned-file-test", "version-id" ) );
  }

  @Test
  public void shouldWaitForContentReadersBeforeDeletingVersion() throws Exception {
    String fileId = "file-content-lock-delete-version-test";
    doReturn( null ).when( dao ).getFileById( fileId );

    assertOperationWaitsForContentReaders( fileId, () -> dao.deleteFileAtVersion( fileId, "version-id" ) );
  }

  @Test
  public void shouldWaitForContentReadersBeforeRestoringVersion() throws Exception {
    String fileId = "file-content-lock-restore-version-test";

    CountDownLatch restoreStarted = new CountDownLatch( 1 );
    org.mockito.Mockito.doAnswer( invocation -> {
      restoreStarted.countDown();
      return null;
    } ).when( versionManager ).restore( nullable( String.class ), nullable( String.class ), org.mockito.ArgumentMatchers.anyBoolean() );

    Lock contentReadLock = JcrRepositoryFileUtils.getFileContentLock( fileId ).readLock();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    contentReadLock.lock();
    boolean released = false;
    try {
      Future<?> restore = executor.submit( () -> dao.restoreFileAtVersion( fileId, "version-id", null ) );
      assertFalse( "Restore must wait while content is being read", restoreStarted.await( 100, TimeUnit.MILLISECONDS ) );
      contentReadLock.unlock();
      released = true;
      restore.get( 5, TimeUnit.SECONDS );
      assertTrue( restoreStarted.await( 5, TimeUnit.SECONDS ) );
    } finally {
      if ( !released ) {
        contentReadLock.unlock();
      }
      executor.shutdownNow();
    }
  }

  @Test
  public void shouldWaitForReadersOfNodeRemovedByUuidCollisionBeforeRestoring() throws Exception {
    String fileId = "file-content-lock-restore-collision-test";
    String movedFolderId = "moved-folder";
    String movedChildId = "moved-child";
    assertNotSame( JcrRepositoryFileUtils.getFileContentLock( fileId ),
        JcrRepositoryFileUtils.getFileContentLock( movedChildId ) );
    assertNotSame( JcrRepositoryFileUtils.getFileContentLock( movedFolderId ),
        JcrRepositoryFileUtils.getFileContentLock( movedChildId ) );

    Node frozenNode = versionManager.getVersionHistory( "/file" ).getVersion( "version-id" ).getFrozenNode();
    Node frozenChild = mock( Node.class );
    Property frozenUuid = mock( Property.class );
    when( frozenUuid.getString() ).thenReturn( movedFolderId );
    when( frozenChild.hasProperty( "jcr:frozenUuid" ) ).thenReturn( true );
    when( frozenChild.getProperty( "jcr:frozenUuid" ) ).thenReturn( frozenUuid );
    NodeIterator noChildren = mock( NodeIterator.class );
    when( frozenChild.getNodes() ).thenReturn( noChildren );
    NodeIterator frozenChildren = mock( NodeIterator.class );
    when( frozenChildren.hasNext() ).thenReturn( true, true, false );
    when( frozenChildren.nextNode() ).thenReturn( frozenChild );
    when( frozenNode.getNodes() ).thenReturn( frozenChildren );

    Node movedChild = mock( Node.class );
    when( movedChild.isNodeType( "null:pentahoFile" ) ).thenReturn( true );
    when( movedChild.getIdentifier() ).thenReturn( movedChildId );
    NodeIterator movedChildren = mock( NodeIterator.class );
    when( movedChildren.hasNext() ).thenReturn( true, false );
    when( movedChildren.nextNode() ).thenReturn( movedChild );
    Node movedFolder = mock( Node.class );
    when( movedFolder.isNodeType( "null:pentahoFolder" ) ).thenReturn( true );
    when( movedFolder.getIdentifier() ).thenReturn( movedFolderId );
    when( movedFolder.getNodes() ).thenReturn( movedChildren );
    when( session.getNodeByIdentifier( movedFolderId ) ).thenReturn( movedFolder );

    CountDownLatch restoreStarted = new CountDownLatch( 1 );
    org.mockito.Mockito.doAnswer( invocation -> {
      restoreStarted.countDown();
      return null;
    } ).when( versionManager ).restore( nullable( String.class ), nullable( String.class ),
        org.mockito.ArgumentMatchers.anyBoolean() );

    Lock descendantReadLock = JcrRepositoryFileUtils.getFileContentLock( movedChildId ).readLock();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    descendantReadLock.lock();
    boolean released = false;
    try {
      Future<?> restore = executor.submit( () -> dao.restoreFileAtVersion( fileId, "version-id", null ) );
      assertFalse( "Restore must wait for readers of a descendant it may remove",
          restoreStarted.await( 100, TimeUnit.MILLISECONDS ) );
      descendantReadLock.unlock();
      released = true;
      restore.get( 5, TimeUnit.SECONDS );
      assertTrue( restoreStarted.await( 5, TimeUnit.SECONDS ) );
      org.mockito.Mockito.verify( movedFolder, org.mockito.Mockito.times( 1 ) ).getNodes();
    } finally {
      if ( !released ) {
        descendantReadLock.unlock();
      }
      executor.shutdownNow();
    }
  }

  @Test
  public void shouldSerializeUpdatesAndVersionOperationsBehindTreeWriteLease() throws Exception {
    RepositoryFile file = new RepositoryFile.Builder( "f" ).id( "tree-lease-test" ).build();
    IRepositoryFileData content = mock( IRepositoryFileData.class );
    List<Runnable> writers = Arrays.asList(
        () -> dao.updateFile( file, content, null ),
        () -> dao.restoreFileAtVersion( "tree-lease-test", "version-id", null ),
        () -> dao.deleteFileAtVersion( "tree-lease-test", "version-id" ) );

    for ( Runnable writer : writers ) {
      jcrCallbackStarted = new CountDownLatch( 1 );
      ExecutorService executor = Executors.newSingleThreadExecutor();
      JcrRepositoryFileUtils.FileContentLockLease treeLease = JcrRepositoryFileUtils.acquireFileContentTreeWriteLock();
      try {
        Future<?> future = executor.submit( () -> {
          try {
            writer.run();
          } catch ( RuntimeException ignored ) {
            // only lock ordering is under test
          }
        } );
        assertFalse( "Writers must wait for the tree write lease",
            jcrCallbackStarted.await( 100, TimeUnit.MILLISECONDS ) );
        treeLease.close();
        treeLease = null;
        future.get( 5, TimeUnit.SECONDS );
        assertEquals( 0, jcrCallbackStarted.getCount() );
      } finally {
        if ( treeLease != null ) {
          treeLease.close();
        }
        executor.shutdownNow();
      }
    }
  }

  @Test
  public void shouldWaitForContentReadersBeforePermanentlyDeletingFile() throws Exception {
    String fileId = "file-content-lock-permanent-delete-test";
    doReturn( null ).when( dao ).getFileById( fileId );
    when( node.getIdentifier() ).thenReturn( fileId );
    CountDownLatch deleteCallStarted = new CountDownLatch( 1 );
    IDeleteHelper deleteHelper = mock( IDeleteHelper.class );
    org.mockito.Mockito.doAnswer( invocation -> {
      deleteCallStarted.countDown();
      return null;
    } ).when( deleteHelper ).permanentlyDeleteFile( org.mockito.ArgumentMatchers.any( Session.class ),
        org.mockito.ArgumentMatchers.any( PentahoJcrConstants.class ), org.mockito.ArgumentMatchers.eq( fileId ) );
    Field deleteHelperField = JcrRepositoryFileDao.class.getDeclaredField( "deleteHelper" );
    deleteHelperField.setAccessible( true );
    deleteHelperField.set( dao, deleteHelper );

    Lock contentReadLock = JcrRepositoryFileUtils.getFileContentLock( fileId ).readLock();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    boolean readLockReleased = false;
    contentReadLock.lock();
    try {
      Future<?> deletion = executor.submit( () -> dao.permanentlyDeleteFile( fileId, null ) );
      assertFalse( "Permanent deletion must wait before invoking the delete helper",
          deleteCallStarted.await( 100, TimeUnit.MILLISECONDS ) );
      contentReadLock.unlock();
      readLockReleased = true;
        deletion.get( 5, TimeUnit.SECONDS );
      assertTrue( "Permanent deletion should continue after the content read finishes",
          deleteCallStarted.await( 5, TimeUnit.SECONDS ) );
    } finally {
      if ( !readLockReleased ) {
        contentReadLock.unlock();
      }
      executor.shutdownNow();
    }
  }

  @Test
  public void shouldWaitForContentReadersBeforePermanentlyDeletingFolderDescendant() throws Exception {
    String folderId = "file-content-lock-permanent-delete-folder-test";
    String fileId = "file-content-lock-permanent-delete-child-test";
    doReturn( null ).when( dao ).getFileById( folderId );
    when( node.getIdentifier() ).thenReturn( folderId );
    when( node.isNodeType( "null:pentahoFile" ) ).thenReturn( false );
    when( node.isNodeType( "null:pentahoFolder" ) ).thenReturn( true );

    Node child = mock( Node.class );
    when( child.getIdentifier() ).thenReturn( fileId );
    when( child.isNodeType( "null:pentahoFile" ) ).thenReturn( true );
    when( child.isNodeType( "null:pentahoFolder" ) ).thenReturn( false );
    NodeIterator children = mock( NodeIterator.class );
    when( children.hasNext() ).thenReturn( true, false );
    when( children.nextNode() ).thenReturn( child );
    when( node.getNodes() ).thenReturn( children );

    CountDownLatch deleteCallStarted = new CountDownLatch( 1 );
    IDeleteHelper deleteHelper = mock( IDeleteHelper.class );
    org.mockito.Mockito.doAnswer( invocation -> {
      deleteCallStarted.countDown();
      return null;
    } ).when( deleteHelper ).permanentlyDeleteFile( org.mockito.ArgumentMatchers.any( Session.class ),
        org.mockito.ArgumentMatchers.any( PentahoJcrConstants.class ), org.mockito.ArgumentMatchers.eq( folderId ) );
    Field deleteHelperField = JcrRepositoryFileDao.class.getDeclaredField( "deleteHelper" );
    deleteHelperField.setAccessible( true );
    deleteHelperField.set( dao, deleteHelper );

    Lock contentReadLock = JcrRepositoryFileUtils.getFileContentLock( fileId ).readLock();
    Lock folderReadLock = JcrRepositoryFileUtils.getFileContentLock( folderId ).readLock();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    boolean readLockReleased = false;
    boolean folderReadLockReleased = false;
    contentReadLock.lock();
    folderReadLock.lock();
    try {
      Future<?> deletion = executor.submit( () -> dao.permanentlyDeleteFile( folderId, null ) );
      assertFalse( "Folder deletion must wait for active reads of descendant files",
          deleteCallStarted.await( 100, TimeUnit.MILLISECONDS ) );
      contentReadLock.unlock();
      readLockReleased = true;
      assertFalse( "Folder deletion must wait for active reads of folder metadata",
          deleteCallStarted.await( 100, TimeUnit.MILLISECONDS ) );
      folderReadLock.unlock();
      folderReadLockReleased = true;
      deletion.get( 5, TimeUnit.SECONDS );
      assertTrue( "Folder deletion should continue after descendant readers finish",
          deleteCallStarted.await( 5, TimeUnit.SECONDS ) );
    } finally {
      if ( !readLockReleased ) {
        contentReadLock.unlock();
      }
      if ( !folderReadLockReleased ) {
        folderReadLock.unlock();
      }
      executor.shutdownNow();
    }
  }

  private void assertOperationWaitsForContentReaders( String fileId, Runnable operation ) throws Exception {
    Lock contentReadLock = JcrRepositoryFileUtils.getFileContentLock( fileId ).readLock();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    boolean readLockReleased = false;

    contentReadLock.lock();
    try {
      Future<?> mutation = executor.submit( operation );
      assertFalse( "JCR mutation must wait while content is being read",
          jcrCallbackStarted.await( 100, TimeUnit.MILLISECONDS ) );
      contentReadLock.unlock();
      readLockReleased = true;
      assertTrue( "JCR mutation should continue after the content read finishes",
          jcrCallbackStarted.await( 5, TimeUnit.SECONDS ) );
      mutation.get( 5, TimeUnit.SECONDS );
    } finally {
      if ( !readLockReleased ) {
        contentReadLock.unlock();
      }
      executor.shutdownNow();
    }
  }

  private void assertReadOperationWaitsForContentWriter( String fileId, Runnable operation ) throws Exception {
    Lock contentWriteLock = JcrRepositoryFileUtils.getFileContentLock( fileId ).writeLock();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    boolean writeLockReleased = false;

    contentWriteLock.lock();
    try {
      Future<?> read = executor.submit( operation );
      assertFalse( "JCR version read must wait while file content is being updated",
          jcrCallbackStarted.await( 100, TimeUnit.MILLISECONDS ) );
      contentWriteLock.unlock();
      writeLockReleased = true;
      assertTrue( "JCR version read should continue after the content writer finishes",
          jcrCallbackStarted.await( 5, TimeUnit.SECONDS ) );
        read.get( 5, TimeUnit.SECONDS );
    } finally {
      if ( !writeLockReleased ) {
        contentWriteLock.unlock();
      }
      executor.shutdownNow();
    }
  }

  @Test
  public void shouldFailRestoringIntoMissingTenantRootFolder() throws Exception {
    when( session.getItem( nullable( String.class ) ) ).thenThrow( new PathNotFoundException() );

    RepositoryException error = assertThrows( RepositoryException.class,
        () -> invokePrivate( "resolveOrCreateFolder", new Class<?>[] { Session.class, String.class },
            session, "/pentaho/tenant0/home/missing" ) );

    assertEquals( "Tenant root folder not found", error.getMessage() );
  }

  @Test
  public void shouldDenyCreatingMissingFolderWhenParentIsNotWritable() throws Exception {
    when( session.getItem( nullable( String.class ) ) ).thenThrow( new PathNotFoundException() );
    RepositoryFile parent = new RepositoryFile.Builder( "parent" ).id( "parent-id" ).path( "/parent" ).build();
    doReturn( parent ).when( dao ).getFileById( "parent-id" );
    Field aclHandlerField = JcrRepositoryFileDao.class.getDeclaredField( "defaultAclHandler" );
    aclHandlerField.setAccessible( true );
    aclHandlerField.set( dao, mock( IRepositoryDefaultAclHandler.class ) );

    assertThrows( AccessDeniedException.class,
        () -> invokePrivate( "getOrCreateChildFolder",
            new Class<?>[] { Session.class, RepositoryFile.class, String.class }, session, parent, "child" ) );
  }

  private Object invokePrivate( final String name, final Class<?>[] types, final Object... args ) throws Exception {
    Method method = JcrRepositoryFileDao.class.getDeclaredMethod( name, types );
    method.setAccessible( true );
    try {
      return method.invoke( dao, args );
    } catch ( InvocationTargetException e ) {
      throw (Exception) e.getCause();
    }
  }

  @Test
  public void shouldConsultAccessVoterWhenCopyingOrMovingFiles() {
    RepositoryFile filePermitted = mock( RepositoryFile.class );
    RepositoryFile fileNotPermitted = mock( RepositoryFile.class );
    RepositoryFile destinationPermitted = mock( RepositoryFile.class );
    RepositoryFile destinationNotPermitted = mock( RepositoryFile.class );
    doReturn( filePermitted ).when( dao ).getFileById( "/filePermitted" );
    doReturn( fileNotPermitted ).when( dao ).getFileById( "/fileNotPermitted" );
    doReturn( destinationPermitted ).when( dao ).getFile( "/destinationPermitted" );
    doReturn( destinationNotPermitted ).when( dao ).getFile( "/destinationNotPermitted" );
    doReturn( true ).when( accessVoterManager )
      .hasAccess( filePermitted, RepositoryFilePermission.WRITE, null, pentahoSession );
    doReturn( false ).when( accessVoterManager )
      .hasAccess( fileNotPermitted, RepositoryFilePermission.WRITE, null, pentahoSession );
    doReturn( true ).when( accessVoterManager )
      .hasAccess( destinationPermitted, RepositoryFilePermission.WRITE, null, pentahoSession );
    doReturn( false ).when( accessVoterManager )
      .hasAccess( destinationNotPermitted, RepositoryFilePermission.WRITE, null, pentahoSession );

    // should move the file,
    // if the user has write permissions to the source file,
    // and write permissions to the destination folder.
    try {
      dao.moveFile( "/filePermitted", "/destinationPermitted", null );
    } catch ( Throwable e ) {
      fail( e.getMessage() );
    }

    // should NOT move the file and throw an exception,
    // if the user has write permissions to the destination folder,
    // but does not have write permissions to the source file.
    try {
      dao.moveFile( "/fileNotPermitted", "/destinationPermitted", null );
    } catch ( Throwable e ) {
      // unwrap original exception and check it
      if ( !( e instanceof RuntimeException )
        || !( e.getCause() instanceof AccessDeniedException ) ) {
        fail( e.getMessage() );
      }
    }

    // should NOT move the file and throw an exception,
    // if the user has write permissions to the source file,
    // but does not have write permissions to the destination folder.
    try {
      dao.moveFile( "/filePermitted", "/destinationNotPermitted", null );
    } catch ( Throwable e ) {
      // unwrap original exception and check it
      if ( !( e instanceof RuntimeException )
        || !( e.getCause() instanceof AccessDeniedException ) ) {
        fail( e.getMessage() );
      }
    }

    // should NOT move the file and throw an exception,
    // if the user neither has write permissions to the source file,
    // nor has write permissions to the destination folder.
    try {
      dao.moveFile( "/fileNotPermitted", "/destinationNotPermitted", null );
    } catch ( Throwable e ) {
      // unwrap original exception and check it
      if ( !( e instanceof RuntimeException )
        || !( e.getCause() instanceof AccessDeniedException ) ) {
        fail( e.getMessage() );
      }
    }
  }
}
