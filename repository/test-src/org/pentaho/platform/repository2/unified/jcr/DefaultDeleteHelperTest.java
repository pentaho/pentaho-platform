/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2014 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.jcr;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.exception.RepositoryFileDaoFileExistsException;
import org.pentaho.platform.repository2.unified.exception.RepositoryFileDaoReferentialIntegrityException;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.Source;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class DefaultDeleteHelperTest {
  private ILockHelper lockHelper;
  private IPathConversionHelper pathConversionHelper;
  private DefaultDeleteHelper defaultDeleteHelper;
  private Session session;
  private PentahoJcrConstants pentahoJcrConstants;
  private final MicroPlatform mp = new MicroPlatform();

  @Before
  public void setUp() throws Exception {
    final IPentahoSession pentahoSession = mock( IPentahoSession.class );
    when( pentahoSession.getName() ).thenReturn( "test" );
    PentahoSessionHolder.setSession( pentahoSession );

    session = mock( Session.class );
    final Node rootNode = mock( Node.class );
    when( session.getRootNode() ).thenReturn( rootNode );

    pentahoJcrConstants = new PentahoJcrConstants( session );
    pathConversionHelper = mock( IPathConversionHelper.class );
    lockHelper = mock( ILockHelper.class );

    defaultDeleteHelper = new DefaultDeleteHelper( lockHelper, pathConversionHelper );

    final RepositoryFileProxy repositoryFileProxy = mock( RepositoryFileProxy.class );
    final RepositoryFileProxyFactory repositoryFileProxyFactory = mock( RepositoryFileProxyFactory.class );
    when( repositoryFileProxyFactory.getProxy( Matchers.<Node>anyObject(), Matchers.<IPentahoLocale>anyObject() ) )
      .thenReturn( repositoryFileProxy );
    // set file ID to random value to make different files not equal
    when( repositoryFileProxy.getId() ).thenAnswer( new Answer<Object>() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        return String.valueOf( new Random().nextDouble() );
      }
    } );
    when( repositoryFileProxy.getTitle() ).thenReturn( "title" );
    mp.defineInstance( RepositoryFileProxyFactory.class, repositoryFileProxyFactory );
    mp.start();
  }

  @After
  public void tearDown() throws Exception {
    defaultDeleteHelper = null;
    mp.stop();
  }

  @Test
  public void testDeleteFile() throws Exception {
    String fileID = "testFileID";

    final Node nodeDeletedFile = mock( Node.class );

    final Node nodeTrash = mock( Node.class );
    when( nodeTrash.hasNode( anyString() ) ).thenReturn( true );
    when( nodeTrash.getNode( anyString() ) ).thenReturn( nodeDeletedFile );

    final Node nodeUserFolder = mock( Node.class );
    when( nodeUserFolder.hasNode( anyString() ) ).thenReturn( true );
    when( nodeUserFolder.getNode( anyString() ) ).thenReturn( nodeTrash );

    final Node nodeDeletedParent = mock( Node.class );
    when( nodeDeletedParent.getPath() ).thenReturn( "parentPath" );

    final Node nodeToRemove = mock( Node.class );
    when( nodeToRemove.getPath() ).thenReturn( "nodePath" );
    when( nodeToRemove.getParent() ).thenReturn( nodeDeletedParent );

    when( session.getNodeByIdentifier( fileID ) ).thenReturn( nodeToRemove );
    when( session.getItem( anyString() ) ).thenReturn( nodeUserFolder );

    try {
      defaultDeleteHelper.deleteFile( session, pentahoJcrConstants, fileID );

      verify( nodeDeletedFile ).setProperty( eq( pentahoJcrConstants.getPHO_DELETEDDATE() ), any( Calendar.class ) );
      verify( nodeDeletedFile ).setProperty( eq( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ), anyString() );
      verify( nodeDeletedFile ).setProperty( eq( pentahoJcrConstants.getPHO_ORIGNAME() ), anyString() );

      verify( session ).move( eq( nodeToRemove.getPath() ), anyString() );
    } catch ( Exception e ) {
      fail();
    }
  }

  @Test
  public void testGetDeletedFiles() throws Exception {
    final String path1 = "path1";
    final Calendar date1 = Calendar.getInstance();
    final Node deletedNode1 = createDeletedNode( path1, date1 );

    final String path2 = "path2";
    final Calendar date2 = Calendar.getInstance();
    final Node deletedNode2 = createDeletedNode( path2, date2 );

    final NodeIterator nodeIterator = mock( NodeIterator.class );
    when( nodeIterator.hasNext() ).thenReturn( true, true, false ); // 2 nodes in trash
    when( nodeIterator.nextNode() ).thenReturn( deletedNode1, deletedNode2 );

    final Node nodeTrash = mock( Node.class );
    when( nodeTrash.getNodes() ).thenReturn( nodeIterator );

    final Node nodeUserFolder = mock( Node.class );
    when( nodeUserFolder.hasNode( anyString() ) ).thenReturn( true );
    when( nodeUserFolder.getNode( anyString() ) ).thenReturn( nodeTrash );

    final Node nodeDeletedParent = mock( Node.class );
    when( nodeDeletedParent.getPath() ).thenReturn( "parentPath" );

    final Node nodeToRemove = mock( Node.class );
    when( nodeToRemove.getPath() ).thenReturn( "nodePath" );
    when( nodeToRemove.getParent() ).thenReturn( nodeDeletedParent );

    when( session.getItem( anyString() ) ).thenReturn( nodeUserFolder );

    try {
      final List<RepositoryFile> deletedFiles = defaultDeleteHelper.getDeletedFiles( session, pentahoJcrConstants );
      assertNotNull( deletedFiles );
      assertEquals( deletedFiles.size(), 2 );
      assertEquals( deletedFiles.get( 0 ).getOriginalParentFolderPath(), path1 );
      assertEquals( deletedFiles.get( 1 ).getOriginalParentFolderPath(), path2 );
      assertEquals( deletedFiles.get( 0 ).getDeletedDate(), date1.getTime() );
      assertEquals( deletedFiles.get( 1 ).getDeletedDate(), date2.getTime() );
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }
  }

  private Node createDeletedNode( String origParentFolderPath, Calendar date ) throws RepositoryException {
    final Node deletedNodeContent = mock( Node.class );

    final NodeIterator nodeIterator = mock( NodeIterator.class );
    when( nodeIterator.hasNext() ).thenReturn( true, false );
    when( nodeIterator.nextNode() ).thenReturn( deletedNodeContent );

    final Property deletedDate = mock( Property.class );
    when( deletedDate.getDate() ).thenReturn( date );

    final Property origParentFolderPathProperty = mock( Property.class );
    when( origParentFolderPathProperty.getString() ).thenReturn( origParentFolderPath );

    final Node deletedNode = mock( Node.class );
    when( deletedNode.hasProperty( pentahoJcrConstants.getPHO_DELETEDDATE() ) ).thenReturn( true );
    when( deletedNode.getNodes() ).thenReturn( nodeIterator );
    when( deletedNode.hasNodes() ).thenReturn( true );
    when( deletedNode.getIdentifier() ).thenReturn( origParentFolderPath + "_" );
    when( deletedNode.hasProperty( pentahoJcrConstants.getPHO_DELETEDDATE() ) ).thenReturn( true );
    when( deletedNode.getProperty( pentahoJcrConstants.getPHO_DELETEDDATE() ) ).thenReturn( deletedDate );
    when( deletedNode.hasProperty( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ) ).thenReturn( true );
    when( deletedNode.getProperty( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ) ).thenReturn( origParentFolderPathProperty );

    when( deletedNodeContent.getParent() ).thenReturn( deletedNode );

    return deletedNode;
  }

  @Test
  public void testGetDeletedFiles1() throws Exception {
    final String path1 = "path1";
    final Calendar date1 = Calendar.getInstance();
    final Node deletedNode1 = createDeletedNode( path1, date1 );

    final String path2 = "path2";
    final Calendar date2 = Calendar.getInstance();
    final Node deletedNode2 = createDeletedNode( path2, date2 );

    final NodeIterator nodeIterator = mock( NodeIterator.class );
    when( nodeIterator.hasNext() ).thenReturn( true, true, false ); // 2 nodes in trash
    when( nodeIterator.nextNode() ).thenReturn( deletedNode1, deletedNode2 );

    final Node nodeTrash = mock( Node.class );
    when( nodeTrash.getNodes() ).thenReturn( nodeIterator );

    final Node nodeUserFolder = mock( Node.class );
    when( nodeUserFolder.hasNode( anyString() ) ).thenReturn( true );
    when( nodeUserFolder.getNode( anyString() ) ).thenReturn( nodeTrash );
    when( nodeUserFolder.getIdentifier() ).thenReturn( "nodeUserFolderID" );

    final Selector selector = mock( Selector.class );

    final Value value = mock( Value.class );

    final ValueFactory valueFactory = mock( ValueFactory.class );
    when( valueFactory.createValue( anyString() ) ).thenReturn( value );

    final QueryObjectModel queryObjectModel = mock( QueryObjectModel.class );

    final QueryObjectModelFactory qomFactory = mock( QueryObjectModelFactory.class );
    when( qomFactory.createQuery( Matchers.<Source>any(), Matchers.<Constraint>any(), Matchers.<Ordering[]>any(), Matchers.<Column[]>any() ) ).thenReturn( queryObjectModel );
    when( qomFactory.selector( anyString(), anyString() ) ).thenReturn( selector );

    final QueryResult queryResult = mock( QueryResult.class );
    when( queryResult.getNodes() ).thenReturn( nodeIterator );

    final Query query = mock( Query.class );
    when( query.execute() ).thenReturn( queryResult );

    final QueryManager queryManager = mock( QueryManager.class );
    when( queryManager.getQOMFactory() ).thenReturn( qomFactory );
    when( queryManager.createQuery( anyString(), anyString() ) ).thenReturn( query );

    final Workspace workspace = mock( Workspace.class );
    when( workspace.getQueryManager() ).thenReturn( queryManager );

    when( session.getItem( anyString() ) ).thenReturn( nodeUserFolder );
    when( session.getValueFactory() ).thenReturn( valueFactory );
    when( session.getWorkspace() ).thenReturn( workspace );
    when( session.itemExists( anyString() ) ).thenReturn( true );

    try {
      final String someFilter = "someFilter";
      final List<RepositoryFile> deletedFiles = defaultDeleteHelper.getDeletedFiles( session, pentahoJcrConstants, path1, someFilter );
      assertNotNull( deletedFiles );
      assertEquals( deletedFiles.size(), 2 );
      for ( RepositoryFile file : deletedFiles ) {
        if ( file.getOriginalParentFolderPath().equals( path1 ) ) {
          assertEquals( file.getDeletedDate(), date1.getTime() );
        } else if ( file.getOriginalParentFolderPath().equals( path2 ) ) {
          assertEquals( file.getDeletedDate(), date2.getTime() );
        } else {
          fail( "Deleted file doesn't have correct path" );
        }
      }

      verify( valueFactory ).createValue( someFilter );
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testPermanentlyDeleteFile() throws Exception {
    String fileID = "testFileID";

    final Node nodeDeletedFile = mock( Node.class );

    final Node nodeTrash = mock( Node.class );
    when( nodeTrash.hasNode( anyString() ) ).thenReturn( true );
    when( nodeTrash.getNode( anyString() ) ).thenReturn( nodeDeletedFile );

    final Node nodeUserFolder = mock( Node.class );
    when( nodeUserFolder.hasNode( anyString() ) ).thenReturn( true );
    when( nodeUserFolder.getNode( anyString() ) ).thenReturn( nodeTrash );

    final Node nodeDeletedParent = mock( Node.class );
    when( nodeDeletedParent.getPath() ).thenReturn( "parentPath" );

    final PropertyIterator referencesPropertyIterator = mock( PropertyIterator.class );
    when( referencesPropertyIterator.hasNext() ).thenReturn( false );

    final Node nodeToRemove = mock( Node.class );
    when( nodeToRemove.getPath() ).thenReturn( "nodePath" );
    when( nodeToRemove.getParent() ).thenReturn( nodeDeletedParent );
    when( nodeToRemove.getReferences() ).thenReturn( referencesPropertyIterator );
    when( nodeToRemove.isLocked() ).thenReturn( false );

    when( session.getNodeByIdentifier( fileID ) ).thenReturn( nodeToRemove );
    when( session.getItem( anyString() ) ).thenReturn( nodeUserFolder );

    try {
      defaultDeleteHelper.permanentlyDeleteFile( session, pentahoJcrConstants, fileID );

      verify( nodeToRemove ).remove();
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }

    // test locked file
    when( nodeToRemove.isLocked() ).thenReturn( true );

    final Lock lock = mock( Lock.class );

    final LockManager lockManager = mock( LockManager.class );
    when( lockManager.getLock( eq( nodeToRemove.getPath() ) ) ).thenReturn( lock );

    final Workspace workspace = mock( Workspace.class );
    when( workspace.getLockManager() ).thenReturn( lockManager );

    when( session.getWorkspace() ).thenReturn( workspace );
    try {
      defaultDeleteHelper.permanentlyDeleteFile( session, pentahoJcrConstants, fileID );

      verify( lockHelper ).removeLockToken( eq( session ), eq( pentahoJcrConstants ), eq( lock ) );
      verify( nodeToRemove, times( 2 ) ).remove();
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }

    // if remove from trash folder, folder containing the file need to be removed
    final String trashPath = ServerRepositoryPaths.getUserHomeFolderPath( new Tenant( null, true ),
      PentahoSessionHolder.getSession().getName() ) + RepositoryFile.SEPARATOR + ".trash";
    when( nodeToRemove.getPath() ).thenReturn( trashPath );
    try {
      defaultDeleteHelper.permanentlyDeleteFile( session, pentahoJcrConstants, fileID );

      verify( nodeToRemove, times( 2 ) ).remove();
      verify( nodeDeletedParent ).remove();
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }

    //test if there are references to this file
    final Node referenceNode = mock( Node.class );
    when( referenceNode.isNodeType( eq( pentahoJcrConstants.getPHO_NT_PENTAHOHIERARCHYNODE() ) ) ).thenReturn( true );
    when( referenceNode.isNodeType( eq( pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER() ) ) ).thenReturn( false );

    final Property property = mock( Property.class );
    when( property.getParent() ).thenReturn( referenceNode );

    when( referencesPropertyIterator.hasNext() ).thenReturn( true, true, false );
    when( referencesPropertyIterator.nextProperty() ).thenReturn( property );

    try {
      defaultDeleteHelper.permanentlyDeleteFile( session, pentahoJcrConstants, fileID );
    } catch ( RepositoryFileDaoReferentialIntegrityException e1 ) {
      // it's ok
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testUndeleteFile() throws Exception {
    String fileID = "testFileID";

    final Property origParentFolderPathProperty = mock( Property.class );
    when( origParentFolderPathProperty.getString() ).thenReturn( "origParentFolderPath" );

    final Node nodeDeletedParent = mock( Node.class );
    when( nodeDeletedParent.getPath() ).thenReturn( "parentPath" );
    when( nodeDeletedParent.hasProperty( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ) ).thenReturn( true );
    when( nodeDeletedParent.getProperty( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ) ).thenReturn( origParentFolderPathProperty );

    final Node nodeToRemove = mock( Node.class );
    when( nodeToRemove.getPath() ).thenReturn( "nodePath" );
    when( nodeToRemove.getParent() ).thenReturn( nodeDeletedParent );

    when( session.getItem( anyString() ) ).thenReturn( nodeDeletedParent );
    when( session.getNodeByIdentifier( fileID ) ).thenReturn( nodeToRemove );

    try {
      defaultDeleteHelper.undeleteFile( session, pentahoJcrConstants, fileID );

      verify( session ).move( eq( nodeToRemove.getPath() ), anyString() );
      verify( nodeDeletedParent ).remove();
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }

    // must fail if there is something at the original path
    when( session.itemExists( anyString() ) ).thenReturn( true );

    try {
      defaultDeleteHelper.undeleteFile( session, pentahoJcrConstants, fileID );
    } catch ( RepositoryFileDaoFileExistsException e1 ) {
      // it's ok
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testGetOriginalParentFolderPath() throws Exception {
    final String fileID = "testFileID";
    final String origParentFolderPath = "origParentFolderPath";
    final String relToAbs_origParentFolderPath = "relToAbs_origParentFolderPath";

    final Property origParentFolderPathProperty = mock( Property.class );
    when( origParentFolderPathProperty.getString() ).thenReturn( origParentFolderPath );

    final Node parentNode = mock( Node.class );
    when( parentNode.getProperty( eq( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ) ) ).thenReturn( origParentFolderPathProperty );
    when( parentNode.hasProperty( eq( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ) ) ).thenReturn( true );

    final Node node = mock( Node.class );
    when( session.getNodeByIdentifier( eq( fileID ) ) ).thenReturn( node );
    when( node.getParent() ).thenReturn( parentNode );

    when( pathConversionHelper.relToAbs( eq( origParentFolderPath ) ) ).thenReturn( relToAbs_origParentFolderPath );
    try {
      final String originalParentFolderPath = defaultDeleteHelper.getOriginalParentFolderPath( session, pentahoJcrConstants, fileID );

      assertEquals( relToAbs_origParentFolderPath, originalParentFolderPath );
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testVersionHistoryDeleted() throws Exception {
    String fileID = "testFileID";

    final Node nodeToRemove = mock( Node.class );
    when( nodeToRemove.getPath() ).thenReturn( "nodePath" );

    final PropertyIterator referencesPropertyIterator = mock( PropertyIterator.class );
    when( referencesPropertyIterator.hasNext() ).thenReturn( false );

    when( nodeToRemove.getReferences() ).thenReturn( referencesPropertyIterator );

    when( session.getNodeByIdentifier( fileID ) ).thenReturn( nodeToRemove );

    Workspace workspace = mock( Workspace.class );
    when( session.getWorkspace()).thenReturn( workspace );
    VersionManager versionManager = mock( VersionManager.class );
    when( workspace.getVersionManager() ).thenReturn( versionManager );

    VersionHistory history = mock( VersionHistory.class );
    when( versionManager.getVersionHistory( "nodePath" )).thenReturn( history );

    VersionIterator versions = mock( VersionIterator.class );
    when( history.getAllVersions()).thenReturn( versions );
    when( versions.hasNext() ).thenReturn( true, false );
    Version version = mock( Version.class );
    when( versions.next() ).thenReturn( version );
    String value = "I am legend";
    when( version.getName()).thenReturn( value );
    try {
      defaultDeleteHelper.permanentlyDeleteFile( session, pentahoJcrConstants, fileID );

      verify( versionManager).getVersionHistory( nodeToRemove.getPath() );
      verify( nodeToRemove ).remove();
      verify( history ).removeVersion( value );
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }

  }
}
