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

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    when( systemSession.getNodeByIdentifier( anyString() ) ).thenThrow( new RepositoryException( "err" ) );

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
