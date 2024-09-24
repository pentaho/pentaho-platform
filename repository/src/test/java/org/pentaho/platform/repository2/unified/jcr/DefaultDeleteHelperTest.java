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

import org.apache.jackrabbit.spi.commons.name.NameConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.exception.RepositoryFileDaoFileExistsException;
import org.pentaho.platform.repository2.unified.exception.RepositoryFileDaoReferentialIntegrityException;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.NodeType;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultDeleteHelperTest {
  private ILockHelper lockHelper;
  private IPathConversionHelper pathConversionHelper;
  private DefaultDeleteHelper defaultDeleteHelper;
  private Session session;
  private PentahoJcrConstants pentahoJcrConstants;
  private MicroPlatform mp;
  private static final String SOLUTION_PATH = "src/test/resources/solution";

  @Before
  public void setUp() throws Exception {
    mp = new MicroPlatform( getSolutionPath() );
    IPentahoSession pentahoSession = mock( IPentahoSession.class );
    when( pentahoSession.getName() ).thenReturn( "test" );
    PentahoSessionHolder.setSession( pentahoSession );

    session = mock( Session.class );
    final Node rootNode = mock( Node.class );
    when( session.getRootNode() ).thenReturn( rootNode );

    when( session.getNamespacePrefix( anyString() ) ).thenReturn( "prefix:" );

    pentahoJcrConstants = new PentahoJcrConstants( session );
    pathConversionHelper = mock( IPathConversionHelper.class );
    lockHelper = mock( ILockHelper.class );

    defaultDeleteHelper = new DefaultDeleteHelper( lockHelper, pathConversionHelper );

    final RepositoryFileProxy repositoryFileProxy = mock( RepositoryFileProxy.class );
    final RepositoryFileProxyFactory repositoryFileProxyFactory = mock( RepositoryFileProxyFactory.class );
    when( repositoryFileProxyFactory.getProxy( ArgumentMatchers.<Node>any(), ArgumentMatchers.<IPentahoLocale>any() ) )
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
    when( nodeTrash.hasNode( nullable( String.class ) ) ).thenReturn( true );
    when( nodeTrash.getNode( nullable( String.class ) ) ).thenReturn( nodeDeletedFile );

    final Node nodeUserFolder = mock( Node.class );
    when( nodeUserFolder.hasNode( nullable( String.class ) ) ).thenReturn( true );
    when( nodeUserFolder.getNode( nullable( String.class ) ) ).thenReturn( nodeTrash );

    final Node nodeDeletedParent = mock( Node.class );
    when( nodeDeletedParent.getPath() ).thenReturn( "parentPath" );

    final Node nodeToRemove = mock( Node.class );
    when( nodeToRemove.getPath() ).thenReturn( "nodePath" );
    when( nodeToRemove.getParent() ).thenReturn( nodeDeletedParent );

    when( session.getNodeByIdentifier( fileID ) ).thenReturn( nodeToRemove );
    when( session.getItem( nullable( String.class ) ) ).thenReturn( nodeUserFolder );

    try {
      defaultDeleteHelper.deleteFile( session, pentahoJcrConstants, fileID );

      verify( nodeDeletedFile ).setProperty( eq( pentahoJcrConstants.getPHO_DELETEDDATE() ), any( Calendar.class ) );
      verify( nodeDeletedFile ).setProperty( eq( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ), nullable( String.class ) );
      verify( nodeDeletedFile ).setProperty( eq( pentahoJcrConstants.getPHO_ORIGNAME() ), nullable( String.class ) );

      verify( session ).move( eq( nodeToRemove.getPath() ), nullable( String.class ) );
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
    when( nodeUserFolder.hasNode( nullable( String.class ) ) ).thenReturn( true );
    when( nodeUserFolder.getNode( nullable( String.class ) ) ).thenReturn( nodeTrash );

    when( session.getItem( nullable( String.class ) ) ).thenReturn( nodeUserFolder );

    final List<RepositoryFile> deletedFiles = defaultDeleteHelper.getDeletedFiles( session, pentahoJcrConstants );
    assertNotNull( deletedFiles );
    assertEquals( 2, deletedFiles.size() );
    assertEquals( path1, deletedFiles.get( 0 ).getOriginalParentFolderPath() );
    assertEquals( path2, deletedFiles.get( 1 ).getOriginalParentFolderPath() );
    assertEquals( date1.getTime(), deletedFiles.get( 0 ).getDeletedDate() );
    assertEquals( date2.getTime(), deletedFiles.get( 1 ).getDeletedDate() );
  }

  private Node createDeletedNode( String origParentFolderPath, Calendar date ) throws RepositoryException {
    final Node deletedNodeContent = mock( Node.class );

    final Property deletedDate = mock( Property.class );
    when( deletedDate.getDate() ).thenReturn( date );

    final Property origParentFolderPathProperty = mock( Property.class );
    when( origParentFolderPathProperty.getString() ).thenReturn( origParentFolderPath );

    final Node deletedNode = mock( Node.class );
    when( deletedNode.hasProperty( pentahoJcrConstants.getPHO_DELETEDDATE() ) ).thenReturn( true );

    when( deletedNode.getNodes() ).thenAnswer( invoc -> {
      final NodeIterator nodeIterator = mock( NodeIterator.class );
      when( nodeIterator.hasNext() ).thenReturn( true, false );
      when( nodeIterator.nextNode() ).thenReturn( deletedNodeContent );
      return nodeIterator;
    } );

    when( deletedNode.hasNodes() ).thenReturn( true );
    when( deletedNode.getIdentifier() ).thenReturn( origParentFolderPath + "_" );
    when( deletedNode.hasProperty( pentahoJcrConstants.getPHO_DELETEDDATE() ) ).thenReturn( true );
    when( deletedNode.getProperty( pentahoJcrConstants.getPHO_DELETEDDATE() ) ).thenReturn( deletedDate );
    when( deletedNode.hasProperty( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ) ).thenReturn( true );
    when( deletedNode.getProperty( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ) ).thenReturn(
        origParentFolderPathProperty );

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
    when( nodeUserFolder.hasNode( nullable( String.class ) ) ).thenReturn( true );
    when( nodeUserFolder.getNode( nullable( String.class ) ) ).thenReturn( nodeTrash );
    when( nodeUserFolder.getIdentifier() ).thenReturn( "nodeUserFolderID" );

    final Selector selector = mock( Selector.class );

    final Value value = mock( Value.class );

    final ValueFactory valueFactory = mock( ValueFactory.class );
    when( valueFactory.createValue( nullable( String.class ) ) ).thenReturn( value );

    final QueryObjectModel queryObjectModel = mock( QueryObjectModel.class );

    final QueryObjectModelFactory qomFactory = mock( QueryObjectModelFactory.class );
    when( qomFactory.createQuery( ArgumentMatchers.<Source>any(), ArgumentMatchers.<Constraint>any(), ArgumentMatchers.<Ordering[]>any(),
        ArgumentMatchers.<Column[]>any() ) ).thenReturn( queryObjectModel );
    when( qomFactory.selector( nullable( String.class ), nullable( String.class ) ) ).thenReturn( selector );

    final QueryResult queryResult = mock( QueryResult.class );
    when( queryResult.getNodes() ).thenReturn( nodeIterator );

    final Query query = mock( Query.class );
    when( query.execute() ).thenReturn( queryResult );

    final QueryManager queryManager = mock( QueryManager.class );
    when( queryManager.getQOMFactory() ).thenReturn( qomFactory );
    when( queryManager.createQuery( nullable( String.class ), nullable( String.class ) ) ).thenReturn( query );

    final Workspace workspace = mock( Workspace.class );
    when( workspace.getQueryManager() ).thenReturn( queryManager );

    when( session.getItem( nullable( String.class ) ) ).thenReturn( nodeUserFolder );
    when( session.getValueFactory() ).thenReturn( valueFactory );
    when( session.getWorkspace() ).thenReturn( workspace );
    when( session.itemExists( nullable( String.class ) ) ).thenReturn( true );

    final String someFilter = "someFilter";
    final List<RepositoryFile> deletedFiles =
        defaultDeleteHelper.getDeletedFiles( session, pentahoJcrConstants, path1, someFilter );
    assertNotNull( deletedFiles );
    assertEquals( 2, deletedFiles.size() );
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
  }

  private void setupGetAllDeletedFiles( final String pathUsr, final boolean[] admin )
      throws RepositoryException {

    final String path1 = "path1";
    final Calendar date1 = Calendar.getInstance();
    final Node deletedNode1 = createDeletedNode( path1, date1 );


    final String path2 = "path2";
    final Calendar date2 = Calendar.getInstance();
    final Node deletedNode2 = createDeletedNode( path2, date2 );

    final Node nodeTrash = mock( Node.class );
    when( nodeTrash.getNodes() ).thenAnswer( invoc -> {
      final NodeIterator nodeIterator = mock( NodeIterator.class );
      when( nodeIterator.hasNext() ).thenReturn( true, true, false ); // 2 nodes in trash
      when( nodeIterator.nextNode() ).thenReturn( deletedNode1, deletedNode2 );
      return nodeIterator;
    } );

    final Node nodeOtherFolder = mock( Node.class );
    when( nodeOtherFolder.hasNode( anyString() ) ).thenReturn( true );
    when( nodeOtherFolder.hasNodes(  ) ).thenReturn( true );
    when( nodeOtherFolder.getNode( anyString() ) ).thenReturn( nodeTrash );

    final Calendar dateUsr = Calendar.getInstance();
    final Node deletedNodeUsr = createDeletedNode( pathUsr, dateUsr );
    final Node nodeTrashUsr = mock( Node.class );

    when( nodeTrashUsr.getNodes() ).thenAnswer( invoc -> {
      NodeIterator nodeIteratorUsr = mock( NodeIterator.class );
      when( nodeIteratorUsr.hasNext() ).thenReturn( true, false );
      when( nodeIteratorUsr.nextNode() ).thenReturn( deletedNodeUsr );
      return nodeIteratorUsr;
    } );

    final Node nodeUserFolder = mock( Node.class );
    when( nodeUserFolder.hasNode( anyString() ) ).thenReturn( true );
    when( nodeUserFolder.hasNodes(  ) ).thenReturn( true );
    when( nodeUserFolder.getNode( anyString() ) ).thenReturn( nodeTrashUsr );

    defaultDeleteHelper = new DefaultDeleteHelper( lockHelper, pathConversionHelper ) {
      @Override
      protected boolean isAdmin() {
        return admin[0];
      }
      @Override
      protected List<String> getUserList() {
        return Arrays.asList( "test", "other" );
      }
    };
    when( session.getItem( endsWith( "/other" ) ) ).thenReturn( nodeOtherFolder );
    when( session.getItem( endsWith( "/test" ) ) ).thenReturn( nodeUserFolder );

    final Node nodeHomeFolder = mock( Node.class );
    when( nodeHomeFolder.hasNode( anyString() ) ).thenReturn( true );
    when( nodeHomeFolder.getNodes() ).thenAnswer( invoc -> {
      NodeIterator nodeIteratorHome = mock( NodeIterator.class );
      when( nodeIteratorHome.hasNext() ).thenReturn( true, true, false );
      when( nodeIteratorHome.next() ).thenReturn( nodeOtherFolder, nodeUserFolder );
      return nodeIteratorHome;
    } );

    when( session.getItem( "/pentaho/tenant0/home" ) ).thenReturn( nodeHomeFolder );
  }

  @Test
  public void testGetAllDeletedFiles_NonAdmin() throws RepositoryException {
    final String pathUsr = "pathUser";
    final boolean[] admin = { false };
    setupGetAllDeletedFiles( pathUsr, admin );

    final List<RepositoryFile> deletedFiles = defaultDeleteHelper.getAllDeletedFiles( session, pentahoJcrConstants );
    assertNotNull( deletedFiles );
    assertEquals( 1, deletedFiles.size() );
    assertEquals( pathUsr, deletedFiles.get( 0 ).getOriginalParentFolderPath() );
  }

  @Test
  public void testGetAllDeletedFiles_Admin_Default() throws RepositoryException {
    final String pathUsr = "pathUser";
    final boolean[] admin = {true};
    setupGetAllDeletedFiles( pathUsr, admin );

    final List<RepositoryFile> deletedFiles = defaultDeleteHelper.getAllDeletedFiles( session, pentahoJcrConstants );
    assertNotNull( deletedFiles );
    assertEquals( 3, deletedFiles.size() );
  }

  private void assertAdminAccessAllUsersTrash( boolean adminAccessAllUsersTrash, int expectedFiles ) throws RepositoryException {
    RepositoryFileProxyFactory repositoryFileProxyFactory = PentahoSystem.get( RepositoryFileProxyFactory.class );
    try ( MockedStatic<PentahoSystem> mockedPentahoSystem = mockStatic( PentahoSystem.class ) ) {
      // By mocking PentahoSystem, the value of this property will become null when called,
      // so we need to set it up in the mocked object
      mockedPentahoSystem.when( () -> PentahoSystem.get( RepositoryFileProxyFactory.class ) )
        .thenReturn( repositoryFileProxyFactory );

      mockedPentahoSystem.when(
        () -> PentahoSystem.getSystemSetting( "adminAccessAllUsersTrash", "true" ) )
        .thenReturn( String.valueOf( adminAccessAllUsersTrash ) );
      final List<RepositoryFile> deletedFiles = defaultDeleteHelper.getAllDeletedFiles( session, pentahoJcrConstants );
      assertNotNull( deletedFiles );
      assertEquals( expectedFiles, deletedFiles.size() );
    }
  }

  @Test
  public void testGetAllDeletedFiles_AdminAccessAllUsersTrash() throws RepositoryException {
    final String pathUsr = "pathUser";
    final boolean[] admin = {true};
    setupGetAllDeletedFiles( pathUsr, admin );

    assertAdminAccessAllUsersTrash( true, 3 );
  }

  @Test
  public void testGetAllDeletedFiles_AdminCannotAccessAllUsersTrash() throws RepositoryException {
    final String pathUsr = "pathUser";
    final boolean[] admin = {true};
    setupGetAllDeletedFiles( pathUsr, admin );

    assertAdminAccessAllUsersTrash( false, 1 );
  }

  @Test
  public void testGetAllDeletedFiles_deletedUserHomeFolderNode() throws Exception {

    final String pathUser1 = "pathUser1";
    final Calendar dateUser1 = Calendar.getInstance();
    final Node deletedNodeUser1 = createDeletedNode( pathUser1, dateUser1 );
    final Node nodeTrashUser1 = mock( Node.class );

    when( nodeTrashUser1.getNodes() ).thenAnswer( invoc -> {
      NodeIterator nodeIteratorUser1 = mock( NodeIterator.class );
      when( nodeIteratorUser1.hasNext() ).thenReturn( true, false );
      when( nodeIteratorUser1.nextNode() ).thenReturn( deletedNodeUser1 );
      return nodeIteratorUser1;
    } );

    final Node nodeUser1 = mock( Node.class );
    when( nodeUser1.hasNode( anyString() ) ).thenReturn( true );
    when( nodeUser1.hasNodes(  ) ).thenReturn( true );
    when( nodeUser1.getNode( anyString() ) ).thenReturn( nodeTrashUser1 );

    final String pathUser2 = "pathUser2";
    final Calendar dateUser2 = Calendar.getInstance();
    final Node deletedNodeUser2 = createDeletedNode( pathUser2, dateUser2 );
    final Node nodeTrashUser2 = mock( Node.class );

    when( nodeTrashUser2.getNodes() ).thenAnswer( invoc -> {
      NodeIterator nodeIteratorUser2 = mock( NodeIterator.class );
      when( nodeIteratorUser2.hasNext() ).thenReturn( true, false );
      when( nodeIteratorUser2.nextNode() ).thenReturn( deletedNodeUser2 );
      return nodeIteratorUser2;
    } );

    final Node nodeUser2 = mock( Node.class );
    when( nodeUser2.hasNode( anyString() ) ).thenReturn( true );
    when( nodeUser2.hasNodes(  ) ).thenReturn( true );
    when( nodeUser2.getNode( anyString() ) ).thenReturn( nodeTrashUser2 );


    final String pathUserTest = "test";
    final Calendar dateUserTest = Calendar.getInstance();
    final Node deletedNodeUserTest = createDeletedNode( pathUserTest, dateUserTest );
    final Node nodeTrashUserTest = mock( Node.class );

    when( nodeTrashUserTest.getNodes() ).thenAnswer( invoc -> {
      NodeIterator nodeIteratorUserTest = mock( NodeIterator.class );
      when( nodeIteratorUserTest.hasNext() ).thenReturn( true, false );
      when( nodeIteratorUserTest.nextNode() ).thenReturn( deletedNodeUserTest );
      return nodeIteratorUserTest;
    } );

    final Node nodeUserTest = mock( Node.class );
    when( nodeUserTest.hasNode( anyString() ) ).thenReturn( true );
    when( nodeUserTest.getNode( anyString() ) ).thenReturn( nodeTrashUserTest );
    when( nodeUserTest.hasNodes( ) ).thenReturn( true );

    defaultDeleteHelper = new DefaultDeleteHelper( lockHelper, pathConversionHelper ) {
      @Override
      protected boolean isAdmin() {
        return true;
      }
      @Override
      protected List<String> getUserList() {
        List<String> userList = new ArrayList<>();
        userList.add( "user1" );
        userList.add( "userInvalidPath" );
        userList.add( "user2" );
        return userList;
      }
    };
    when( session.getItem( endsWith( "/userInvalidPath" ) ) ).thenThrow(  new PathNotFoundException() );
    when( session.getItem( endsWith( "/test" ) ) ).thenReturn( nodeUserTest );
    when( session.getItem( endsWith( "/user2" ) ) ).thenReturn( nodeUser2 );
    when( session.getItem( endsWith( "/user1" ) ) ).thenReturn( nodeUser1 );

    final Node nodeHomeFolder = mock( Node.class );
    when( nodeHomeFolder.hasNode( anyString() ) ).thenReturn( true );
    when( nodeHomeFolder.getNodes() ).thenAnswer( invoc -> {
      NodeIterator nodeIteratorHome = mock( NodeIterator.class );
      when( nodeIteratorHome.hasNext() ).thenReturn( true, true, true, false );
      when( nodeIteratorHome.next() ).thenReturn( nodeUserTest, nodeUser2, nodeUser1 );
      return nodeIteratorHome;
    } );

    when( session.getItem( "/pentaho/tenant0/home" ) ).thenReturn( nodeHomeFolder );



    final List<RepositoryFile> deletedFilesAdmin = defaultDeleteHelper.getAllDeletedFiles( session, pentahoJcrConstants );
    assertNotNull( deletedFilesAdmin );
    assertEquals( 3, deletedFilesAdmin.size() );
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
    Workspace workspace = mock( Workspace.class );
    VersionManager versionManager = mock( VersionManager.class );
    when( workspace.getVersionManager() ).thenReturn( versionManager );
    when( session.getWorkspace() ).thenReturn( workspace );
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
    final String trashPath =
        ServerRepositoryPaths.getUserHomeFolderPath( new Tenant( null, true ), PentahoSessionHolder.getSession()
            .getName() ) + RepositoryFile.SEPARATOR + ".trash";
    when( nodeToRemove.getPath() ).thenReturn( trashPath );
    try {
      defaultDeleteHelper.permanentlyDeleteFile( session, pentahoJcrConstants, fileID );

      verify( nodeToRemove, times( 3 ) ).remove();
      verify( nodeDeletedParent ).remove();
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }

    // test if there are references to this file
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
    when( nodeDeletedParent.getProperty( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ) ).thenReturn(
        origParentFolderPathProperty );

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
    when( parentNode.getProperty( eq( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ) ) ).thenReturn(
        origParentFolderPathProperty );
    when( parentNode.hasProperty( eq( pentahoJcrConstants.getPHO_ORIGPARENTFOLDERPATH() ) ) ).thenReturn( true );

    final Node node = mock( Node.class );
    when( session.getNodeByIdentifier( eq( fileID ) ) ).thenReturn( node );
    when( node.getParent() ).thenReturn( parentNode );

    when( pathConversionHelper.relToAbs( eq( origParentFolderPath ) ) ).thenReturn( relToAbs_origParentFolderPath );
    try {
      final String originalParentFolderPath =
          defaultDeleteHelper.getOriginalParentFolderPath( session, pentahoJcrConstants, fileID );

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
    when( nodeToRemove.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ).thenReturn( true );
    when( nodeToRemove.isNodeType( pentahoJcrConstants.getPHO_MIX_VERSIONABLE() ) ).thenReturn( true );
    NodeType nt = mock( NodeType.class );
    when( nt.getName() ).thenReturn( NameConstants.MIX_VERSIONABLE.getLocalName() );
    when( nodeToRemove.getPrimaryNodeType() ).thenReturn( nt );
    when( nodeToRemove.getMixinNodeTypes() ).thenReturn( new NodeType[] {} );
    Property prop1 = mock( Property.class );
    String pho_nt_pentahofile = pentahoJcrConstants.getPHO_NT_PENTAHOFILE();
    when( prop1.toString() ).thenReturn( pho_nt_pentahofile );
    when( prop1.getString() ).thenReturn( pho_nt_pentahofile );
    when( nodeToRemove.getProperty( pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE() ) ).thenReturn( prop1 );
    final PropertyIterator referencesPropertyIterator = mock( PropertyIterator.class );
    when( referencesPropertyIterator.hasNext() ).thenReturn( false );
    when( nodeToRemove.getReferences() ).thenReturn( referencesPropertyIterator );
    when( session.getNodeByIdentifier( fileID ) ).thenReturn( nodeToRemove );

    when( session.getNamespacePrefix( anyString() ) ).thenReturn( "prefix:" );

    Workspace workspace = mock( Workspace.class );
    when( session.getWorkspace() ).thenReturn( workspace );
    VersionManager versionManager = mock( VersionManager.class );
    when( workspace.getVersionManager() ).thenReturn( versionManager );

    VersionHistory history = mock( VersionHistory.class );
    when( versionManager.getVersionHistory( "nodePath" ) ).thenReturn( history );

    VersionIterator versions = mock( VersionIterator.class );
    when( history.getAllVersions() ).thenReturn( versions );
    when( versions.hasNext() ).thenReturn( true, false );
    Version version = mock( Version.class );
    when( versions.next() ).thenReturn( version );
    String value = "Omega Man";
    when( version.getName() ).thenReturn( value );
    try {
      defaultDeleteHelper.permanentlyDeleteFile( session, pentahoJcrConstants, fileID );

      verify( versionManager ).getVersionHistory( nodeToRemove.getPath() );
      verify( nodeToRemove ).remove();
      verify( history ).removeVersion( value );
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }

  }

  @Test
  public void testVersionHistoryDeletedWithParent() throws Exception {

    String fileID = "testFileID";

    final Node nodeToRemove = mock( Node.class );
    when( nodeToRemove.getPath() ).thenReturn( "nodePath" );
    when( nodeToRemove.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ).thenReturn( true );
    NodeType nt = mock( NodeType.class );
    when( nt.getName() ).thenReturn( NameConstants.MIX_VERSIONABLE.getLocalName() );
    when( nodeToRemove.getPrimaryNodeType() ).thenReturn( nt );
    when( nodeToRemove.getMixinNodeTypes() ).thenReturn( new NodeType[] {} );
    when( nodeToRemove.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ).thenReturn( true );
    when( nodeToRemove.isNodeType( pentahoJcrConstants.getPHO_MIX_VERSIONABLE() ) ).thenReturn( true );
    Property prop1 = mock( Property.class );
    String pho_nt_pentahofile = pentahoJcrConstants.getPHO_NT_PENTAHOFILE();
    when( prop1.toString() ).thenReturn( pho_nt_pentahofile );
    when( prop1.getString() ).thenReturn( pho_nt_pentahofile );
    when( nodeToRemove.getProperty( pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE() ) ).thenReturn( prop1 );
    final PropertyIterator referencesPropertyIterator = mock( PropertyIterator.class );
    when( referencesPropertyIterator.hasNext() ).thenReturn( false );
    when( nodeToRemove.getReferences() ).thenReturn( referencesPropertyIterator );
    when( session.getNodeByIdentifier( fileID ) ).thenReturn( nodeToRemove );

    when( session.getNamespacePrefix( anyString() ) ).thenReturn( "prefix:" );

    Workspace workspace = mock( Workspace.class );
    when( session.getWorkspace() ).thenReturn( workspace );
    VersionManager versionManager = mock( VersionManager.class );
    when( workspace.getVersionManager() ).thenReturn( versionManager );

    VersionHistory history = mock( VersionHistory.class );
    when( versionManager.getVersionHistory( "nodePath" ) ).thenReturn( history );

    VersionIterator versions = mock( VersionIterator.class );
    when( history.getAllVersions() ).thenReturn( versions );
    when( versions.hasNext() ).thenReturn( true, false );
    Version version = mock( Version.class );
    when( versions.next() ).thenReturn( version );
    String value = "Omega Man";
    when( version.getName() ).thenReturn( value );

    String parentID = "parent";
    final Node parent = mock( Node.class );
    when( session.getNodeByIdentifier( parentID ) ).thenReturn( parent );
    when( parent.getPath() ).thenReturn( "parentNodePath" );
    when( parent.isNodeType( pentahoJcrConstants.getNT_FROZENNODE() ) ).thenReturn( true );

    Property prop = mock( Property.class );
    String pho_nt_pentahofolder = pentahoJcrConstants.getPHO_NT_PENTAHOFOLDER();
    when( prop.toString() ).thenReturn( pho_nt_pentahofolder );
    when( prop.getString() ).thenReturn( pho_nt_pentahofolder );
    when( parent.getProperty( pentahoJcrConstants.getJCR_FROZENPRIMARYTYPE() ) ).thenReturn( prop );
    when( parent.getReferences() ).thenReturn( referencesPropertyIterator );

    NodeIterator nodeIterator = mock( NodeIterator.class );
    when( nodeIterator.hasNext() ).thenReturn( true, false );
    when( nodeIterator.next() ).thenReturn( nodeToRemove );
    when( parent.getNodes() ).thenReturn( nodeIterator );

    try {
      defaultDeleteHelper.permanentlyDeleteFile( session, pentahoJcrConstants, parentID );

      verify( versionManager ).getVersionHistory( nodeToRemove.getPath() );
      verify( parent ).remove();
      verify( history ).removeVersion( value );
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }
  }

  protected String getSolutionPath() {
    return SOLUTION_PATH;
  }
}
