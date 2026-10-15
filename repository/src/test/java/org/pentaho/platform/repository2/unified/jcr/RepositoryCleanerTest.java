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

import org.apache.jackrabbit.core.IPentahoSystemSessionFactory;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.gc.GarbageCollector;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.PropertyIteratorAdapter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class RepositoryCleanerTest {

  private static final String SOLUTION_PATH = "src/test/resources/solution";

  @Test
  public void gc() throws Exception {
    GarbageCollector collector = mock( GarbageCollector.class );

    RepositoryImpl repository = mock( RepositoryImpl.class );
    when( repository.createDataStoreGarbageCollector() ).thenReturn( collector );

    MicroPlatform mp = new MicroPlatform( getSolutionPath() );
    mp.defineInstance( Repository.class, repository );
    mp.defineInstance( "jcrRepository", repository );
    mp.start();

    RepositoryCleaner cleaner = new RepositoryCleaner();
    Session systemSession = mock( Session.class );
    IPentahoSystemSessionFactory sessionFactory = mock( IPentahoSystemSessionFactory.class );
    when( sessionFactory.create( repository ) ).thenReturn( systemSession );
    cleaner.setSystemSessionFactory( sessionFactory );

    try {
      cleaner.gc();
    } finally {
      mp.stop();
    }

    verify( collector, times( 1 ) ).mark();
    verify( collector, times( 1 ) ).sweep();
    verify( collector, times( 1 ) ).close();
    verify( sessionFactory, times( 1 ) ).create( repository );
  }

  protected String getSolutionPath() {
    return SOLUTION_PATH;
  }

  @Test
  public void testFailedMarkRunsDiagnosticsAfterClosingCollector() throws Exception {
    GarbageCollector collector = mock( GarbageCollector.class );
    RepositoryImpl repository = mock( RepositoryImpl.class );
    when( repository.createDataStoreGarbageCollector() ).thenReturn( collector );
    doThrow( new RepositoryException( "mark failed to access a property" ) ).when( collector ).mark();
    Session purgeSession = mock( Session.class );
    Session diagnosticSession = mock( Session.class );
    when( diagnosticSession.getRootNode() ).thenThrow( new RepositoryException( "Cannot read diagnostic root" ) );
    IPentahoSystemSessionFactory sessionFactory = mock( IPentahoSystemSessionFactory.class );
    when( sessionFactory.create( repository ) ).thenReturn( purgeSession, diagnosticSession );
    RepositoryCleaner cleaner = new RepositoryCleaner();
    cleaner.setSystemSessionFactory( sessionFactory );
    MicroPlatform platform = new MicroPlatform( getSolutionPath() );
    platform.defineInstance( Repository.class, repository );
    platform.defineInstance( "jcrRepository", repository );
    platform.start();
    try {
      cleaner.gc();
    } finally {
      platform.stop();
    }
    verify( collector, never() ).sweep();
    InOrder order = inOrder( collector, sessionFactory, diagnosticSession );
    order.verify( collector ).close();
    order.verify( sessionFactory ).create( repository );
    order.verify( diagnosticSession ).getRootNode();
    order.verify( diagnosticSession ).logout();
    verify( diagnosticSession, never() ).save();
  }

  @Test
  public void testFindVersionNodesAndPurgeWhenNodeHasNullNodes() throws Exception {
    GarbageCollector collector = mock( GarbageCollector.class );
    RepositoryImpl repository = mock( RepositoryImpl.class );
    when( repository.createDataStoreGarbageCollector() ).thenReturn( collector );
    MicroPlatform mp = new MicroPlatform( getSolutionPath() );
    mp.defineInstance( Repository.class, repository );
    mp.defineInstance( "jcrRepository", repository );
    mp.start();

    RepositoryCleaner cleaner = new RepositoryCleaner();
    Session systemSession = mock( Session.class );
    IPentahoSystemSessionFactory sessionFactory = mock( IPentahoSystemSessionFactory.class );
    when( sessionFactory.create( any() ) ).thenReturn( systemSession );
    Node parentNode = mock( Node.class );
    when( systemSession.getNode( anyString() ) ).thenReturn( parentNode );
    when( parentNode.getName() ).thenReturn( "" );
    when( parentNode.getNodes() ).thenReturn( null );
    try {
      cleaner.setSystemSessionFactory( sessionFactory );
      cleaner.gc();
    } catch ( Exception e ) {
      Assert.fail();
    } finally {
      mp.stop();
    }

  }

  @Test
  public void testFailedMarkScansAndClosesDedicatedSession() throws Exception {
    checkDiagnosticSessionLifecycle( false );
  }

  @Test
  public void testDiagnosticLogoutFailureDoesNotEscape() throws Exception {
    checkDiagnosticSessionLifecycle( true );
  }

  private void checkDiagnosticSessionLifecycle( boolean logoutFails ) throws Exception {
    GarbageCollector collector = mock( GarbageCollector.class );
    RepositoryImpl repository = mock( RepositoryImpl.class );
    when( repository.createDataStoreGarbageCollector() ).thenReturn( collector );
    doThrow( new RepositoryException( "Mark failed" ) ).when( collector ).mark();
    Session purgeSession = mock( Session.class );
    Session diagnosticSession = mock( Session.class );
    Workspace workspace = mock( Workspace.class );
    when( workspace.getName() ).thenReturn( "default" );
    when( diagnosticSession.getWorkspace() ).thenReturn( workspace );
    Node root = mock( Node.class );
    when( diagnosticSession.getRootNode() ).thenReturn( root );
    when( root.getProperties() ).thenReturn( PropertyIteratorAdapter.EMPTY );
    when( root.getNodes() ).thenReturn( NodeIteratorAdapter.EMPTY );
    if ( logoutFails ) {
      doThrow( new IllegalStateException( "Logout failed" ) ).when( diagnosticSession ).logout();
    }
    IPentahoSystemSessionFactory factory = mock( IPentahoSystemSessionFactory.class );
    when( factory.create( repository ) ).thenReturn( purgeSession, diagnosticSession );

    runCleaner( repository, factory );

    InOrder order = inOrder( collector, root, diagnosticSession );
    order.verify( collector ).close();
    order.verify( root ).getProperties();
    order.verify( root ).getNodes();
    order.verify( diagnosticSession ).logout();
    verify( collector, never() ).sweep();
    verify( factory, times( 2 ) ).create( repository );
    verify( diagnosticSession, never() ).save();
  }

  @Test
  public void testRuntimeMarkFailureAndDiagnosticLoginFailureDoNotSweep() throws Exception {
    GarbageCollector collector = mock( GarbageCollector.class );
    RepositoryImpl repository = mock( RepositoryImpl.class );
    when( repository.createDataStoreGarbageCollector() ).thenReturn( collector );
    doThrow( new IllegalStateException( "Mark failed" ) ).when( collector ).mark();
    IPentahoSystemSessionFactory factory = mock( IPentahoSystemSessionFactory.class );
    Session purgeSession = mock( Session.class );
    when( factory.create( repository ) ).thenReturn( purgeSession )
      .thenThrow( new RepositoryException( "Cannot open diagnostic session" ) );

    runCleaner( repository, factory );

    verify( collector, never() ).sweep();
    verify( collector ).close();
    verify( factory, times( 2 ) ).create( repository );
  }

  @Test
  public void testCollectorCloseFailurePreventsDiagnostics() throws Exception {
    GarbageCollector collector = mock( GarbageCollector.class );
    RepositoryImpl repository = mock( RepositoryImpl.class );
    when( repository.createDataStoreGarbageCollector() ).thenReturn( collector );
    doThrow( new RepositoryException( "Mark failed" ) ).when( collector ).mark();
    doThrow( new IllegalStateException( "Close failed" ) ).when( collector ).close();
    IPentahoSystemSessionFactory factory = mock( IPentahoSystemSessionFactory.class );
    Session purgeSession = mock( Session.class );
    when( factory.create( repository ) ).thenReturn( purgeSession );

    runCleaner( repository, factory );

    verify( collector, never() ).sweep();
    verify( collector ).close();
    verify( factory ).create( repository );
  }

  @Test
  public void testSweepFailureDoesNotTriggerDiagnostics() throws Exception {
    GarbageCollector collector = mock( GarbageCollector.class );
    RepositoryImpl repository = mock( RepositoryImpl.class );
    when( repository.createDataStoreGarbageCollector() ).thenReturn( collector );
    when( collector.sweep() ).thenThrow( new RepositoryException( "Sweep failed" ) );
    IPentahoSystemSessionFactory factory = mock( IPentahoSystemSessionFactory.class );
    Session purgeSession = mock( Session.class );
    when( factory.create( repository ) ).thenReturn( purgeSession );

    runCleaner( repository, factory );

    verify( collector ).mark();
    verify( collector ).close();
    verify( factory ).create( repository );
  }

  @Test
  public void testCollectorCreationFailureDoesNotTriggerDiagnostics() throws Exception {
    RepositoryImpl repository = mock( RepositoryImpl.class );
    when( repository.createDataStoreGarbageCollector() ).thenThrow( new RepositoryException( "No collector" ) );
    IPentahoSystemSessionFactory factory = mock( IPentahoSystemSessionFactory.class );
    Session purgeSession = mock( Session.class );
    when( factory.create( repository ) ).thenReturn( purgeSession );

    runCleaner( repository, factory );

    verify( factory ).create( repository );
  }

  private void runCleaner( RepositoryImpl repository, IPentahoSystemSessionFactory factory ) throws Exception {
    RepositoryCleaner cleaner = new RepositoryCleaner();
    cleaner.setSystemSessionFactory( factory );
    MicroPlatform platform = new MicroPlatform( getSolutionPath() );
    platform.defineInstance( Repository.class, repository );
    platform.defineInstance( "jcrRepository", repository );
    platform.start();
    try {
      cleaner.gc();
    } finally {
      platform.stop();
    }
  }
}
