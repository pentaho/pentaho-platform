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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

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
