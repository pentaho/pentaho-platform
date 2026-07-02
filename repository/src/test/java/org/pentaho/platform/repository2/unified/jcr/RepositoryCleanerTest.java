/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.repository2.unified.jcr;

import org.apache.jackrabbit.core.IPentahoSystemSessionFactory;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.gc.GarbageCollector;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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
  }

  protected String getSolutionPath() {
    return SOLUTION_PATH;
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
}
