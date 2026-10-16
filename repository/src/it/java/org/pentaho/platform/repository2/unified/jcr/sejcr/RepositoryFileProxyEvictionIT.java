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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Test;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.repository2.unified.jcr.RepositoryFileProxy;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.TestPrincipalProvider;

/**
 * Verifies, against a real Jackrabbit repository, that a lazily loaded {@link RepositoryFileProxy} keeps working
 * when the cached session it reads from is evicted while a lazy read is in progress. The proxy outlives the callback
 * that created it, so its reads are not protected by any template lease unless the proxy takes one itself.
 */
public class RepositoryFileProxyEvictionIT extends DefaultUnifiedRepositoryBase {

  private static final int STRESS_WORKERS = 8;
  private static final int STRESS_ITERATIONS = 150;

  /**
   * Evicts the session at the first repository access of every lazy read, which is the deterministic form of the
   * cleanup thread closing the session between the proxy's liveness check and its use. Each read must still return
   * the real value instead of logging a repository error and returning nothing.
   */
  @Test
  public void lazyReadReturnsRealValueWhenItsSessionIsEvictedDuringTheRead() {
    loginAsRepositoryAdmin();
    String nodeName = "lazy-read-eviction-" + UUID.randomUUID();
    createFileNode( nodeName );
    try {
      assertLazyRead( nodeName, "name", proxy -> proxy.getName(), nodeName );
      assertLazyRead( nodeName, "created date", proxy -> proxy.getCreatedDate() != null, true );
      assertLazyRead( nodeName, "folder flag", proxy -> proxy.isFolder(), false );
      assertLazyRead( nodeName, "id", proxy -> proxy.getId() != null, true );
    } finally {
      removeFileNode( nodeName );
    }
  }

  /**
   * Reads proxies from many threads while another thread keeps evicting the cache. Every lazy read must return the
   * real value; any repository error swallowed by the proxy shows up here as a missing value.
   */
  @Test
  public void lazyReadsStayCorrectWhileTheCacheIsEvictedConcurrently() throws Exception {
    loginAsRepositoryAdmin();
    String nodeName = "lazy-read-stress-" + UUID.randomUUID();
    createFileNode( nodeName );
    GuavaCachePoolPentahoJcrSessionFactory ownedCache = newOwnedCache();
    PentahoJcrTemplate template = newAdminTemplate( ownedCache );
    ConcurrentLinkedQueue<String> failures = new ConcurrentLinkedQueue<>();
    AtomicBoolean running = new AtomicBoolean( true );
    ExecutorService executor = Executors.newFixedThreadPool( STRESS_WORKERS + 1 );

    try {
      Future<?> evictor = executor.submit( () -> evictUntilStopped( ownedCache, running ) );
      List<Future<?>> workers = new ArrayList<>();
      for ( int worker = 0; worker < STRESS_WORKERS; worker++ ) {
        workers.add( executor.submit( () -> readLazily( template, nodeName, failures ) ) );
      }
      for ( Future<?> worker : workers ) {
        worker.get( 120, TimeUnit.SECONDS );
      }
      running.set( false );
      evictor.get( 10, TimeUnit.SECONDS );
      assertTrue( "Lazy reads must not fail while sessions are evicted: " + failures, failures.isEmpty() );
    } finally {
      running.set( false );
      executor.shutdownNow();
      assertTrue( "Workers must terminate", executor.awaitTermination( 30, TimeUnit.SECONDS ) );
      ownedCache.invalidateCachedSessions();
      removeFileNode( nodeName );
    }
  }

  /** Creates a cache of its own, so the test can evict it without touching the shared fixture's cache. */
  private GuavaCachePoolPentahoJcrSessionFactory newOwnedCache() {
    CredentialsStrategySessionFactory configuredFactory =
      (CredentialsStrategySessionFactory) jcrTemplate.getSessionFactory();
    return new GuavaCachePoolPentahoJcrSessionFactory(
      configuredFactory.getRepository(), configuredFactory.getWorkspaceName() );
  }

  /** Creates a template that reads as the repository administrator through the given cache. */
  private PentahoJcrTemplate newAdminTemplate( GuavaCachePoolPentahoJcrSessionFactory cache ) throws Exception {
    CredentialsStrategySessionFactory configuredFactory =
      (CredentialsStrategySessionFactory) jcrTemplate.getSessionFactory();
    CredentialsStrategySessionFactory factory = new CredentialsStrategySessionFactory(
      configuredFactory.getRepository(), configuredFactory.getWorkspaceName(),
      TestPrincipalProvider.adminCredentialsStrategy, null );
    factory.setSessionFactory( cache );
    factory.afterPropertiesSet();
    PentahoJcrTemplate template = new PentahoJcrTemplate();
    template.setSessionFactory( factory );
    template.setAllowCreate( true );
    return template;
  }

  /** Keeps evicting the whole cache until told to stop. */
  private static void evictUntilStopped( GuavaCachePoolPentahoJcrSessionFactory cache, AtomicBoolean running ) {
    while ( running.get() ) {
      cache.invalidateCachedSessions();
      LockSupport.parkNanos( TimeUnit.MICROSECONDS.toNanos( 200 ) );
    }
  }

  /** Creates proxies in a callback, then resolves them lazily after it, recording every wrong or missing value. */
  private static void readLazily( PentahoJcrTemplate template, String nodeName,
                                  ConcurrentLinkedQueue<String> failures ) {
    for ( int i = 0; i < STRESS_ITERATIONS; i++ ) {
      RepositoryFileProxy proxy = (RepositoryFileProxy) template.execute(
        session -> new RepositoryFileProxy( session.getRootNode().getNode( nodeName ), template, null ), true );
      recordWrongValues( proxy, nodeName, failures );
    }
  }

  /** Resolves the proxy's lazy values and records each one that is wrong or missing. */
  private static void recordWrongValues( RepositoryFileProxy proxy, String nodeName,
                                         ConcurrentLinkedQueue<String> failures ) {
    try {
      addIf( !nodeName.equals( proxy.getName() ), "name was wrong", failures );
      addIf( proxy.getCreatedDate() == null, "created date was missing", failures );
      addIf( proxy.getId() == null, "id was missing", failures );
    } catch ( RuntimeException e ) {
      failures.add( e.toString() );
    }
  }

  private static void addIf( boolean failed, String message, ConcurrentLinkedQueue<String> failures ) {
    if ( failed ) {
      failures.add( message );
    }
  }

  /**
   * Builds a fresh proxy over a live cached session whose node evicts that session on its first repository access,
   * then checks the value the proxy resolves lazily after its creating callback has finished.
   */
  private <T> void assertLazyRead( String nodeName, String description,
                                   Function<RepositoryFileProxy, T> read, T expected ) {
    AtomicBoolean armed = new AtomicBoolean();
    RepositoryFileProxy proxy = (RepositoryFileProxy) jcrTemplate.execute( session -> {
      Node real = session.getRootNode().getNode( nodeName );
      CachedJcrSession holder = (CachedJcrSession) session.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE );
      assertNotNull( "The test needs a lifecycle-tracked cached session", holder );
      Node evicting = (Node) Proxy.newProxyInstance( Node.class.getClassLoader(), new Class<?>[] { Node.class },
        ( target, method, args ) -> {
          if ( !"getSession".equals( method.getName() ) && armed.compareAndSet( true, false ) ) {
            holder.retire();
          }
          try {
            return method.invoke( real, args );
          } catch ( InvocationTargetException e ) {
            throw e.getTargetException();
          }
        } );
      return new RepositoryFileProxy( evicting, jcrTemplate, null );
    }, true );
    armed.set( true );

    assertEquals( "Lazy " + description + " must survive eviction of its session", expected, read.apply( proxy ) );
    assertFalse( "The eviction must have happened during the read", armed.get() );
  }

  /** Creates a minimal file node through an administrator transaction. */
  private void createFileNode( String nodeName ) {
    jcrTransactionTemplate.execute( transaction -> jcrTemplate.execute( session -> {
      Node file = session.getRootNode().addNode( nodeName, "nt:file" );
      Node content = file.addNode( "jcr:content", "nt:resource" );
      content.setProperty( "jcr:mimeType", "application/octet-stream" );
      content.setProperty( "jcr:lastModified", Calendar.getInstance() );
      Binary binary = session.getValueFactory().createBinary( new ByteArrayInputStream( new byte[ 16 ] ) );
      try {
        content.setProperty( "jcr:data", binary );
        session.save();
      } finally {
        binary.dispose();
      }
      return null;
    }, true ) );
  }

  /** Removes the fixture node through an administrator transaction. */
  private void removeFileNode( String nodeName ) {
    jcrTransactionTemplate.execute( transaction -> jcrTemplate.execute( session -> {
      if ( session.getRootNode().hasNode( nodeName ) ) {
        session.getRootNode().getNode( nodeName ).remove();
        session.save();
      }
      return null;
    }, true ) );
  }
}
