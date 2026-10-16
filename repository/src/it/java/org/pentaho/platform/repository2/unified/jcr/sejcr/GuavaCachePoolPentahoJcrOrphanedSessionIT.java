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



package org.pentaho.platform.repository2.unified.jcr.sejcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import javax.jcr.Session;
import javax.jcr.Binary;
import javax.jcr.Node;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.jcr.RepositoryFileProxy;
import org.pentaho.platform.repository2.unified.jcr.transform.SimpleRepositoryFileDataTransformer;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Verifies cached-session lifecycle behavior against the real unified repository. Scenarios cover eviction during
 * callbacks, Spring transaction synchronization and suspension, nested template use, write completion, stale sessions,
 * and resources that escape a callback. Unlike the mock-based unit tests, these cases exercise Jackrabbit session and
 * repository behavior end to end.
 */
public class GuavaCachePoolPentahoJcrOrphanedSessionIT extends DefaultUnifiedRepositoryBase {

  /** Verifies SUPPORTS read-only synchronization retains and reuses an evicted session across sequential callbacks. */
  @Test
  public void sequentialCallbacksInReadOnlySynchronizationReuseLiveSession() {
    loginAsSysTenantAdmin();
    CredentialsStrategySessionFactory credentialsFactory =
      (CredentialsStrategySessionFactory) jcrTemplate.getSessionFactory();
    GuavaCachePoolPentahoJcrSessionFactory cacheFactory =
      (GuavaCachePoolPentahoJcrSessionFactory) credentialsFactory.getSessionFactory();
    TransactionTemplate readOnly = new TransactionTemplate( jcrTransactionTemplate.getTransactionManager() );
    readOnly.setPropagationBehavior( TransactionDefinition.PROPAGATION_SUPPORTS );
    readOnly.setReadOnly( true );
    AtomicReference<Session> boundSession = new AtomicReference<>();

    try {
      readOnly.execute( transaction -> {
        jcrTemplate.execute( first -> {
          boundSession.set( first );
          assertTrue( "Read-only SUPPORTS must bind the session for the entire operation",
            TransactionSynchronizationManager.hasResource( credentialsFactory ) );
          cacheFactory.invalidateCachedSessions();
          assertTrue( "The callback must retain its session during eviction", first.isLive() );
          return null;
        }, true );

        assertTrue( "Eviction must not close a session that Spring still owns between callbacks",
          boundSession.get().isLive() );
        jcrTemplate.execute( second -> {
          assertSame( "Sequential callbacks must reuse the read-only bound session", boundSession.get(), second );
          assertEquals( "The bound session must support real JCR reads after eviction", "",
            second.getRootNode().getName() );
          return null;
        }, true );
        return null;
      } );
      assertFalse( "Spring must unbind the session when the operation completes",
        TransactionSynchronizationManager.hasResource( credentialsFactory ) );
    } finally {
      Session session = boundSession.get();
      if ( session != null && session.isLive() ) {
        session.logout();
      }
    }
  }

  /** Verifies returned lazy-file and binary-stream resources remain usable after their source session is closed. */
  @Test
  public void escapedLazyFileAndBinaryStreamRemainUsableAfterSessionLogout() throws Exception {
    loginAsRepositoryAdmin();
    CredentialsStrategySessionFactory credentialsFactory =
      (CredentialsStrategySessionFactory) jcrTemplate.getSessionFactory();
    GuavaCachePoolPentahoJcrSessionFactory cacheFactory =
      (GuavaCachePoolPentahoJcrSessionFactory) credentialsFactory.getSessionFactory();
    String nodeName = "escaped-session-resource-" + UUID.randomUUID();
    byte[] payload = new byte[ 1_048_576 ];
    for ( int i = 0; i < payload.length; i++ ) {
      payload[ i ] = (byte) ( i * 31 + i / 128 );
    }
    AtomicReference<Session> sourceSession = new AtomicReference<>();
    AtomicReference<RepositoryFileProxy> lazyFile = new AtomicReference<>();
    AtomicReference<String> nodeId = new AtomicReference<>();
    AtomicReference<InputStream> stream = new AtomicReference<>();

    try {
      jcrTransactionTemplate.execute( transaction -> jcrTemplate.execute( session -> {
        Node file = session.getRootNode().addNode( nodeName, "nt:file" );
        Node content = file.addNode( "jcr:content", "nt:resource" );
        content.setProperty( "jcr:mimeType", "application/octet-stream" );
        content.setProperty( "jcr:lastModified", java.util.Calendar.getInstance() );
        Binary binary = session.getValueFactory().createBinary( new ByteArrayInputStream( payload ) );
        try {
          content.setProperty( "jcr:data", binary );
          session.save();
        } finally {
          binary.dispose();
        }
        return null;
      }, true ) );

      jcrTemplate.execute( session -> {
        sourceSession.set( session );
        Node file = session.getRootNode().getNode( nodeName );
        nodeId.set( file.getIdentifier() );
        lazyFile.set( new RepositoryFileProxy( file, jcrTemplate, null ) );
        SimpleRepositoryFileData data = new SimpleRepositoryFileDataTransformer()
          .fromContentNode( session, new PentahoJcrConstants( session ), file );
        stream.set( data.getInputStream() );
        cacheFactory.invalidateCachedSessions();
        return null;
      }, true );

      // The fix closes naturally here. Close the orphan explicitly on baseline to check the same escaped-resource
      // contract on both versions, without relying on the vulnerability to keep a stream or lazy file usable.
      if ( sourceSession.get().isLive() ) {
        sourceSession.get().logout();
      }
      assertFalse( sourceSession.get().isLive() );
      assertEquals( "A lazy repository file must reload its node after session disposal", nodeName, lazyFile.get().getName() );
      assertEquals( nodeId.get(), lazyFile.get().getId() );
      assertArrayEquals( "Returned binary data must remain readable independently of the closed source session",
        payload, stream.get().readAllBytes() );
    } finally {
      try {
        if ( stream.get() != null ) {
          stream.get().close();
        }
      } finally {
        removeTestNode( nodeName );
        Session session = sourceSession.get();
        if ( session != null && session.isLive() ) {
          session.logout();
        }
      }
    }
  }

  /** Removes the named fixture node through an administrator transaction when test cleanup is required. */
  private void removeTestNode( String nodeName ) {
    jcrTransactionTemplate.execute( transaction -> jcrTemplate.execute( session -> {
      if ( session.getRootNode().hasNode( nodeName ) ) {
        session.getRootNode().getNode( nodeName ).remove();
        session.save();
      }
      return null;
    }, true ) );
  }

  /**
   * Runs real JCR callbacks on multiple workers while the cache is repeatedly invalidated between barrier phases.
   * Every callback must retain a live, thread-owned session, and every evicted session must be closed after its
   * callback finishes.
   */
  @Test
  public void realConcurrentCallbacksSurviveEvictionAndReleaseEverySession() throws Exception {
    CredentialsStrategySessionFactory configuredFactory =
      (CredentialsStrategySessionFactory) jcrTemplate.getSessionFactory();
    GuavaCachePoolPentahoJcrSessionFactory ownedCache = new GuavaCachePoolPentahoJcrSessionFactory(
      configuredFactory.getRepository(), configuredFactory.getWorkspaceName() );
    CredentialsStrategySessionFactory ownedFactory = new CredentialsStrategySessionFactory(
      configuredFactory.getRepository(), configuredFactory.getWorkspaceName(),
      TestPrincipalProvider.adminCredentialsStrategy, null );
    ownedFactory.setSessionFactory( ownedCache );
    ownedFactory.afterPropertiesSet();
    PentahoJcrTemplate template = new PentahoJcrTemplate();
    template.setSessionFactory( ownedFactory );
    template.setAllowCreate( true );
    int workers = 12;
    int rounds = 16;
    CyclicBarrier barrier = new CyclicBarrier( workers + 1 );
    ExecutorService executor = Executors.newFixedThreadPool( workers );
    List<Future<?>> futures = new ArrayList<>();
    ConcurrentLinkedQueue<Session> sessions = new ConcurrentLinkedQueue<>();

    try {
      for ( int worker = 0; worker < workers; worker++ ) {
        futures.add( executor.submit( () -> {
          for ( int round = 0; round < rounds; round++ ) {
            template.execute( session -> {
              sessions.add( session );
              awaitEvictionBarrier( barrier );
              awaitEvictionBarrier( barrier );
              assertTrue( "Real Jackrabbit sessions must stay live inside concurrent callbacks", session.isLive() );
              assertEquals( "", session.getRootNode().getName() );
              return null;
            }, true );
          }
        } ) );
      }
      for ( int round = 0; round < rounds; round++ ) {
        barrier.await( 20, TimeUnit.SECONDS );
        ownedCache.invalidateCachedSessions();
        barrier.await( 20, TimeUnit.SECONDS );
      }
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos( 30 );
      for ( Future<?> future : futures ) {
        future.get( Math.max( 1, deadline - System.nanoTime() ), TimeUnit.NANOSECONDS );
      }
      assertEquals( workers * rounds, sessions.size() );
      for ( Session session : sessions ) {
        awaitSessionClosed( session, "Every evicted real session must close after its callback" );
      }
    } finally {
      futures.forEach( future -> future.cancel( true ) );
      executor.shutdownNow();
      assertTrue( "Real JCR workers must terminate", executor.awaitTermination( 30, TimeUnit.SECONDS ) );
      ownedCache.invalidateCachedSessions();
      for ( Session session : sessions ) {
        if ( session.isLive() ) {
          session.logout();
        }
      }
    }
  }

  /**
   * Synchronizes a worker with the test thread around a cache invalidation phase.
   *
   * @param barrier barrier shared by workers and the coordinating test thread
   */
  private void awaitEvictionBarrier( CyclicBarrier barrier ) {
    try {
      barrier.await( 20, TimeUnit.SECONDS );
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
      throw new AssertionError( "Real JCR worker interrupted", e );
    } catch ( java.util.concurrent.BrokenBarrierException | java.util.concurrent.TimeoutException e ) {
      throw new AssertionError( "Timed out coordinating real JCR eviction", e );
    }
  }

  /** Verifies a suspended outer transaction retains its session while a REQUIRES_NEW transaction is evicted. */
  @Test
  public void suspendedTransactionKeepsRetiredSessionUntilOuterCompletion() {
    loginAsRepositoryAdmin();
    CredentialsStrategySessionFactory credentialsFactory =
      (CredentialsStrategySessionFactory) jcrTemplate.getSessionFactory();
    GuavaCachePoolPentahoJcrSessionFactory cacheFactory =
      (GuavaCachePoolPentahoJcrSessionFactory) credentialsFactory.getSessionFactory();
    TransactionTemplate requiresNew = new TransactionTemplate( jcrTransactionTemplate.getTransactionManager() );
    requiresNew.setPropagationBehavior( TransactionDefinition.PROPAGATION_REQUIRES_NEW );
    AtomicReference<Session> outerSession = new AtomicReference<>();
    AtomicReference<Session> innerSession = new AtomicReference<>();

    try {
      jcrTransactionTemplate.execute( outerTransaction -> {
        jcrTemplate.execute( session -> {
          outerSession.set( session );
          cacheFactory.invalidateCachedSessions();
          return null;
        }, true );
        assertTrue( "The outer transaction must retain its evicted session", outerSession.get().isLive() );

        requiresNew.execute( innerTransaction -> {
          jcrTemplate.execute( session -> {
            innerSession.set( session );
            assertNotSame( "REQUIRES_NEW must use a different session from the suspended transaction",
              outerSession.get(), session );
            cacheFactory.invalidateCachedSessions();
            assertTrue( "Eviction must not close the suspended outer transaction", outerSession.get().isLive() );
            assertEquals( "", session.getRootNode().getName() );
            return null;
          }, true );
          return null;
        } );

        awaitSessionClosed( innerSession.get(), "The inner transaction must dispose its evicted session" );
        assertTrue( "Completing the inner transaction must not release the suspended outer lease",
          outerSession.get().isLive() );
        jcrTemplate.execute( session -> {
          assertSame( "Resumption must restore the outer transaction's original session", outerSession.get(), session );
          assertEquals( "", session.getRootNode().getName() );
          return null;
        }, true );
        return null;
      } );
      awaitSessionClosed( outerSession.get(), "The outer transaction must dispose its session only after completing" );
      assertFalse( "Transaction completion must leave no JCR binding",
        TransactionSynchronizationManager.hasResource( credentialsFactory ) );
    } finally {
      cacheFactory.invalidateCachedSessions();
      for ( Session session : new Session[] { innerSession.get(), outerSession.get() } ) {
        if ( session != null && session.isLive() ) {
          session.logout();
        }
      }
    }
  }

  /**
   * Verifies the repository Spring configuration wires the Pentaho transaction manager to the session cache, which is
   * what lets the cache log in directly while a transaction is being created.
   */
  @Test
  public void springConfigurationWiresTheTransactionManagerToTheSessionCache() {
    CredentialsStrategySessionFactory credentialsFactory =
      (CredentialsStrategySessionFactory) jcrTemplate.getSessionFactory();
    GuavaCachePoolPentahoJcrSessionFactory cacheFactory =
      (GuavaCachePoolPentahoJcrSessionFactory) credentialsFactory.getSessionFactory();

    Object transactionManager = jcrTransactionTemplate.getTransactionManager();

    assertTrue( "The transaction manager must report transaction creation",
      transactionManager instanceof PentahoTransactionManager );
    assertSame( "The session cache must use the same transaction manager", transactionManager,
      ReflectionTestUtils.getField( cacheFactory, "transactionManager" ) );
  }

  /**
   * Verifies a {@code REQUIRES_NEW} transaction gets its own session rather than the cached session of the
   * transaction it suspended: sharing would let the inner commit save the outer transaction's unsaved changes, so
   * those changes must survive neither the inner transaction nor the outer rollback.
   */
  @Test
  public void requiresNewTransactionDoesNotShareOrCommitTheSuspendedTransactionsChanges() {
    loginAsRepositoryAdmin();
    TransactionTemplate requiresNew = new TransactionTemplate( jcrTransactionTemplate.getTransactionManager() );
    requiresNew.setPropagationBehavior( TransactionDefinition.PROPAGATION_REQUIRES_NEW );
    String nodeName = "suspended-transaction-" + UUID.randomUUID();

    try {
      jcrTransactionTemplate.execute( outerTransaction -> {
        jcrTemplate.execute( session -> {
          session.getRootNode().addNode( nodeName );
          return null;
        }, true );

        requiresNew.execute( innerTransaction -> jcrTemplate.execute( session -> {
          assertFalse( "REQUIRES_NEW must not see the suspended transaction's unsaved changes",
            session.hasPendingChanges() );
          return null;
        }, true ) );

        outerTransaction.setRollbackOnly();
        return null;
      } );

      jcrTemplate.execute( session -> {
        assertFalse( "The rolled-back outer transaction's node must not have been committed by the inner one",
          session.getRootNode().hasNode( nodeName ) );
        return null;
      }, true );
    } finally {
      removeTestNode( nodeName );
    }
  }

  /** Verifies an evicted write session remains usable until its enclosing transaction commits. */
  @Test
  public void evictedWriteSessionRemainsLiveUntilCommit() {
    verifyEvictedWriteSession( false );
  }

  /** Verifies an evicted write session remains usable until its enclosing transaction rolls back. */
  @Test
  public void evictedWriteSessionRemainsLiveUntilRollback() {
    verifyEvictedWriteSession( true );
  }

  /**
   * Exercises transaction-completion cleanup for an evicted write session and checks the committed repository state.
   *
   * @param rollback {@code true} to mark the transaction rollback-only; otherwise the write is committed
   */
  private void verifyEvictedWriteSession( boolean rollback ) {
    // Root-level test nodes require the repository administrator, not the tenant administrator.
    loginAsRepositoryAdmin();
    CredentialsStrategySessionFactory credentialsFactory =
      (CredentialsStrategySessionFactory) jcrTemplate.getSessionFactory();
    GuavaCachePoolPentahoJcrSessionFactory cacheFactory =
      (GuavaCachePoolPentahoJcrSessionFactory) credentialsFactory.getSessionFactory();
    String nodeName = "session-lifecycle-" + UUID.randomUUID();
    AtomicReference<Session> writeSession = new AtomicReference<>();

    try {
      jcrTransactionTemplate.execute( transaction -> {
        jcrTemplate.execute( session -> {
          writeSession.set( session );
          session.getRootNode().addNode( nodeName ).setProperty( "value", "saved" );
          session.save();
          cacheFactory.invalidateCachedSessions();
          assertTrue( "Eviction must keep the write callback's session live", session.isLive() );
          return null;
        }, true );
        assertTrue( "Spring must retain the evicted XA session until commit or rollback", writeSession.get().isLive() );
        if ( rollback ) {
          transaction.setRollbackOnly();
        }
        return null;
      } );
      awaitSessionClosed( writeSession.get(), "The write session must be closed after transaction completion" );
      jcrTemplate.execute( session -> {
        assertEquals( "Eviction must not change commit/rollback semantics", !rollback, session.getRootNode().hasNode( nodeName ) );
        return null;
      }, true );
    } finally {
      jcrTransactionTemplate.execute( transaction -> jcrTemplate.execute( session -> {
        if ( session.getRootNode().hasNode( nodeName ) ) {
          session.getRootNode().getNode( nodeName ).remove();
          session.save();
        }
        return null;
      }, true ) );
      Session session = writeSession.get();
      if ( session != null && session.isLive() ) {
        session.logout();
      }
    }
  }

  /** Verifies a session evicted during a successful callback closes after the template releases its final lease. */
  @Test
  public void evictedLiveSessionIsClosedAfterTemplateCompletes() {
    loginAsSysTenantAdmin();
    AtomicReference<Session> evictedSession = new AtomicReference<>();
    SessionFactory configuredFactory = jcrTemplate.getSessionFactory();
    CredentialsStrategySessionFactory credentialsFactory = (CredentialsStrategySessionFactory) configuredFactory;
    GuavaCachePoolPentahoJcrSessionFactory cacheFactory =
      (GuavaCachePoolPentahoJcrSessionFactory) credentialsFactory.getSessionFactory();

    try {
      jcrTemplate.execute( session -> {
        evictedSession.set( session );
        Object usageCountAttribute = session.getAttribute( GuavaCachePoolPentahoJcrSessionFactory.USAGE_COUNT );
        assertTrue( "The real cached session should be protected during the callback",
          usageCountAttribute instanceof AtomicInteger && ( (AtomicInteger) usageCountAttribute ).get() > 0 );

        // Exercise Guava's real removal listener while the Spring JCR template is using this session.
        cacheFactory.invalidateCachedSessions();
        assertTrue( "Eviction must defer logout while the callback is active", session.isLive() );
        return null;
      }, true );

      Session session = evictedSession.get();
      assertNotNull( "The callback should have received a repository session", session );
      assertEquals( "The template should release its usage count after the callback", 0,
        ( (AtomicInteger) session.getAttribute( GuavaCachePoolPentahoJcrSessionFactory.USAGE_COUNT ) ).get() );
      awaitSessionClosed( session, "An evicted session must be logged out after its final use" );
    } finally {
      Session session = evictedSession.get();
      if ( session != null && session.isLive() ) {
        // Keep cleanup deterministic if an assertion fails before the deferred logout occurs.
        session.logout();
      }
    }
  }

  /** Verifies callback failure still releases the final lease and closes an evicted session. */
  @Test
  public void evictedLiveSessionIsClosedWhenCallbackFails() {
    loginAsSysTenantAdmin();
    AtomicReference<Session> evictedSession = new AtomicReference<>();
    SessionFactory configuredFactory = jcrTemplate.getSessionFactory();
    CredentialsStrategySessionFactory credentialsFactory = (CredentialsStrategySessionFactory) configuredFactory;
    GuavaCachePoolPentahoJcrSessionFactory cacheFactory =
      (GuavaCachePoolPentahoJcrSessionFactory) credentialsFactory.getSessionFactory();

    try {
      try {
        jcrTemplate.execute( session -> {
          evictedSession.set( session );
          cacheFactory.invalidateCachedSessions();
          assertTrue( "Eviction must defer logout while the callback is active", session.isLive() );
          throw new IllegalStateException( "Expected callback failure" );
        }, true );
        fail( "The callback exception should propagate from the JCR template" );
      } catch ( RuntimeException expected ) {
        assertNotNull( expected );
      }

      Session session = evictedSession.get();
      assertNotNull( "The callback should have received a repository session", session );
      assertEquals( "The template should release its usage count after callback failure", 0,
        ( (AtomicInteger) session.getAttribute( GuavaCachePoolPentahoJcrSessionFactory.USAGE_COUNT ) ).get() );
      awaitSessionClosed( session,
        "An evicted session must be logged out after its final use, even when the callback fails" );
    } finally {
      Session session = evictedSession.get();
      if ( session != null && session.isLive() ) {
        // Preserve teardown if an assertion fails before the deferred logout occurs.
        session.logout();
      }
    }
  }

  /** Verifies an externally logged-out cache entry is replaced on the next template execution. */
  @Test
  public void externallyClosedCachedSessionIsReplacedOnNextTemplateUse() {
    loginAsSysTenantAdmin();
    AtomicReference<Session> staleSession = new AtomicReference<>();
    AtomicReference<Session> replacementSession = new AtomicReference<>();

    jcrTemplate.execute( session -> {
      staleSession.set( session );
      return null;
    }, true );

    try {
      staleSession.get().logout();
      jcrTemplate.execute( session -> {
        replacementSession.set( session );
        assertTrue( "The template should replace an externally closed cached session", session.isLive() );
        return null;
      }, true );

      assertNotNull( "The first template execution should have acquired a session", staleSession.get() );
      assertNotNull( "The next template execution should acquire a replacement session", replacementSession.get() );
      assertNotSame( "A closed cached session must not be reused", staleSession.get(), replacementSession.get() );
    } finally {
      Session session = replacementSession.get();
      if ( session != null && session.isLive() ) {
        session.logout();
      }
    }
  }

  /** Verifies nested callbacks reuse an evicted but Spring-bound session until outer completion releases it. */
  @Test
  public void nestedTemplateExecutionKeepsEvictedSessionUntilOuterCallbackCompletes() {
    loginAsSysTenantAdmin();
    AtomicReference<Session> outerSession = new AtomicReference<>();
    AtomicReference<Session> innerSession = new AtomicReference<>();
    SessionFactory configuredFactory = jcrTemplate.getSessionFactory();
    CredentialsStrategySessionFactory credentialsFactory = (CredentialsStrategySessionFactory) configuredFactory;
    GuavaCachePoolPentahoJcrSessionFactory cacheFactory =
      (GuavaCachePoolPentahoJcrSessionFactory) credentialsFactory.getSessionFactory();
    TransactionTemplate readOnly = new TransactionTemplate( jcrTransactionTemplate.getTransactionManager() );
    readOnly.setPropagationBehavior( TransactionDefinition.PROPAGATION_SUPPORTS );
    readOnly.setReadOnly( true );

    try {
      readOnly.execute( transaction -> {
        jcrTemplate.execute( outer -> {
          outerSession.set( outer );
          cacheFactory.invalidateCachedSessions();
          jcrTemplate.execute( inner -> {
            innerSession.set( inner );
            assertSame( "A nested JCR template execution should reuse the Spring-bound session", outer, inner );
            assertTrue( "The nested operation should enter with the JCR session bound",
              TransactionSynchronizationManager.hasResource( configuredFactory ) );
            assertTrue( "A nested callback may reuse a retired but still-bound session", inner.isLive() );
            assertEquals( "", inner.getRootNode().getName() );
            return null;
          }, true );

          assertTrue( "The outer callback must retain its session after nested execution", outer.isLive() );
          return null;
        }, true );
        assertTrue( "Spring still owns the session after the outer callback", outerSession.get().isLive() );
        return null;
      } );

      assertNotNull( "Outer callback should have received a session", outerSession.get() );
      assertNotNull( "Nested callback should have received a session", innerSession.get() );
      awaitSessionClosed( outerSession.get(),
        "Logout should occur after nested uses and their Spring synchronization release the session" );
    } finally {
      Session session = outerSession.get();
      if ( session != null && session.isLive() ) {
        session.logout();
      }
    }
  }

  /**
   * Waits for the asynchronous cache-removal cleanup to close a real repository session.
   *
   * @param session session expected to be closed by the removal listener or the final lease release
   * @param message assertion message describing the lifecycle expectation
   */
  private void awaitSessionClosed( Session session, String message ) {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos( 10 );
    while ( session.isLive() && System.nanoTime() < deadline ) {
      LockSupport.parkNanos( TimeUnit.MILLISECONDS.toNanos( 10 ) );
    }
    assertFalse( message, session.isLive() );
  }

}