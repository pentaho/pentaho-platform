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

package org.apache.jackrabbit.core;

import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.gc.GarbageCollector;
import org.junit.Test;
import org.pentaho.platform.repository2.unified.jcr.RepositoryCleaner;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by nbaker on 10/6/15.
 */
public class RepositoryGCOrphanTest {

  private static final String SOLUTION_PATH = "src/test/resources/solution";

  @Test
  public void testOrphanNodePurge() throws Exception {
    GarbageCollector collector = mock( GarbageCollector.class );

    RepositoryImpl repository = mock( RepositoryImpl.class );

    RepositoryContext context = mock( RepositoryContext.class );
    when( repository.getRepositoryContext() ).thenReturn( context );
    RepositoryImpl.WorkspaceInfo workspaceInfo = mock( RepositoryImpl.WorkspaceInfo.class );
    when( repository.getWorkspaceInfo( "default" ) ).thenReturn( workspaceInfo );
    WorkspaceConfig config = mock( WorkspaceConfig.class );
    when( workspaceInfo.getConfig() ).thenReturn( config );

    when( repository.createDataStoreGarbageCollector() ).thenReturn( collector );

    RepositoryCleaner cleaner = new RepositoryCleaner();
    Session systemSession = mock( Session.class );
    IPentahoSystemSessionFactory sessionFactory = mock( IPentahoSystemSessionFactory.class );
    when( sessionFactory.create( repository ) ).thenReturn( systemSession );
    cleaner.setSystemSessionFactory( sessionFactory );

    Node rootNode = mock( Node.class );
    when( systemSession.getNode( "/jcr:system/jcr:versionStorage" ) ).thenReturn( rootNode );
    when( rootNode.getName() ).thenReturn( "jcr:frozenNode" );

    Property uuid = mock( Property.class );
    when( rootNode.getProperty( "jcr:frozenUuid" ) ).thenReturn( uuid );
    when( rootNode.hasProperty( "jcr:frozenUuid" ) ).thenReturn( true );

    Value value = mock( Value.class );
    when( uuid.getValue() ).thenReturn( value );
    when( uuid.getString() ).thenReturn( "Foo" );

    when( systemSession.getNodeByIdentifier( any() ) ).thenThrow( new RepositoryException( "err" ) );

    Version parent = mock( Version.class );
    VersionHistory grandParent = mock( VersionHistory.class );
    when( rootNode.getParent() ).thenReturn( parent );
    when( parent.getParent() ).thenReturn( grandParent );

    when( parent.getName() ).thenReturn( "Bar" );

    NodeIterator nodes = mock( NodeIterator.class );
    when( rootNode.getNodes() ).thenReturn( nodes );
    when( nodes.hasNext() ).thenReturn( false );

    MicroPlatform mp = new MicroPlatform( getSolutionPath() );
    mp.defineInstance( Repository.class, repository );
    mp.defineInstance( "jcrRepository", repository );
    mp.start();

    try {
      cleaner.gc();
    } finally {
      mp.stop();
    }
    verify( grandParent, times( 1 ) ).removeVersion( "Bar" );
  }

  protected String getSolutionPath() {
    return SOLUTION_PATH;
  }
}
