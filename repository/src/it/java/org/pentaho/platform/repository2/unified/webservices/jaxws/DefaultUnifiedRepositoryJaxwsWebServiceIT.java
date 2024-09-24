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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.webservices.jaxws;

import com.sun.xml.ws.developer.JAXWSProperties;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode.DataPropertyType;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile.Mode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests marshalling, unmarshalling, and {@code UnifiedRepositoryToWebServiceAdapter}. Do not test unified
 * repository logic in this class; just make sure args serialize back and forth correctly and that the adapter is
 * translating to the right calls.
 * 
 * @author mlowery
 */

@RunWith( SpringJUnit4ClassRunner.class )
@SuppressWarnings( "nls" )
public class DefaultUnifiedRepositoryJaxwsWebServiceIT extends DefaultUnifiedRepositoryBase {
  // ~ Static fields/initializers
  // ======================================================================================

  private final Logger logger = LogManager.getLogger( DefaultUnifiedRepositoryJaxwsWebServiceIT.class );

  // ~ Instance fields
  // =================================================================================================



  // ~ Constructors
  // ====================================================================================================

  public DefaultUnifiedRepositoryJaxwsWebServiceIT() throws Exception {
    super();
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();

    IRepositoryVersionManager mockRepositoryVersionManager = mock( IRepositoryVersionManager.class );
    when( mockRepositoryVersionManager.isVersioningEnabled( anyString() ) ).thenReturn( true );
    when( mockRepositoryVersionManager.isVersionCommentEnabled( anyString() ) ).thenReturn( false );
    JcrRepositoryFileUtils.setRepositoryVersionManager( mockRepositoryVersionManager );

    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );

    String address = "http://localhost:9000/repo";

    try {
      Endpoint.publish( address, new DefaultUnifiedRepositoryJaxwsWebService( repo ) );
    } catch ( Throwable th ) {
      //ignore
    }

    Service service =
        Service.create( new URL( "http://localhost:9000/repo?wsdl" ), new QName( "http://www.pentaho.org/ws/1.0",
            "unifiedRepository" ) );

    IUnifiedRepositoryJaxwsWebService repoWebService = service.getPort( IUnifiedRepositoryJaxwsWebService.class );

    // accept cookies to maintain session on server
    ( (BindingProvider) repoWebService ).getRequestContext().put( BindingProvider.SESSION_MAINTAIN_PROPERTY, true );
    // support streaming binary data
    ( (BindingProvider) repoWebService ).getRequestContext().put( JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE,
        8192 );
    SOAPBinding binding = (SOAPBinding) ( (BindingProvider) repoWebService ).getBinding();
    binding.setMTOMEnabled( true );

    repo = new UnifiedRepositoryToWebServiceAdapter( repoWebService );

  }

  @Test
  public void testDummy() {

  }

  @Test
  public void testEverything() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName,
      tenantAuthenticatedRoleName } );
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName,
            tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", new String[]{tenantAdminRoleName} );
    logout();
    login( USERNAME_SUZY, tenantAcme, new String[]{tenantAdminRoleName, tenantAuthenticatedRoleName} );
    logger.info( "getFile" );
    JcrRepositoryDumpToFile dumpToFile =
        new JcrRepositoryDumpToFile( testJcrTemplate, jcrTransactionTemplate, repositoryAdminUsername,
            "c:/build/testrepo_9", Mode.CUSTOM );
    dumpToFile.execute();
    RepositoryFile f = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    assertNotNull( f.getId() );
    assertEquals( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ), f.getPath() );
    assertNotNull( f.getCreatedDate() );
    assertEquals( USERNAME_SUZY, f.getName() );
    assertTrue( f.isFolder() );

    logger.info( "getFileById" );
    assertNotNull( repo.getFileById( f.getId() ) );

    logger.info( "createFolder" );
    RepositoryFile folder1 =
        repo.createFolder( f.getId(), new RepositoryFile.Builder( "folder1" ).folder( true ).build(), null );
    assertNotNull( folder1 );
    assertEquals( "folder1", folder1.getName() );
    assertNotNull( folder1.getId() );

    NodeRepositoryFileData data = makeNodeRepositoryFileData1();
    logger.info( "createFile" );
    RepositoryFile file1 =
        repo.createFile( folder1.getId(), new RepositoryFile.Builder( "file1.whatever" ).versioned( true ).build(),
            data, null );
    assertNotNull( file1 );
    assertNotNull( file1.getId() );

    logger.info( "getDataForRead" );
    NodeRepositoryFileData file1Data = repo.getDataForRead( file1.getId(), NodeRepositoryFileData.class );
    assertNotNull( file1Data );
    assertEquals( "testNode", file1Data.getNode().getName() );
    assertEquals( "hello world", file1Data.getNode().getProperty( "prop1" ).getString() );
    assertEquals( false, file1Data.getNode().getProperty( "prop2" ).getBoolean() );
    assertEquals( DataPropertyType.BOOLEAN, file1Data.getNode().getProperty( "prop2" ).getType() );
    assertEquals( 12L, file1Data.getNode().getProperty( "prop3" ).getLong() );

    logger.info( "createFile (binary)" );
    SimpleRepositoryFileData simpleData =
        new SimpleRepositoryFileData( new ByteArrayInputStream( "Hello World!".getBytes( "UTF-8" ) ), "UTF-8",
            "text/plain" );
    RepositoryFile simpleFile =
        repo.createFile( folder1.getId(), new RepositoryFile.Builder( "file2.whatever" ).versioned( true ).build(),
            simpleData, null );

    Serializable simpleVersion = simpleFile.getVersionId();

    logger.info( "getDataForRead (binary)" );
    SimpleRepositoryFileData simpleFileData = repo.getDataForRead( simpleFile.getId(), SimpleRepositoryFileData.class );
    assertNotNull( simpleFileData );
    assertEquals( "Hello World!", IOUtils.toString( simpleFileData.getInputStream(), simpleFileData.getEncoding() ) );
    assertEquals( "text/plain", simpleFileData.getMimeType() );
    assertEquals( "UTF-8", simpleFileData.getEncoding() );

    logger.info( "updateFile (binary)" );
    simpleData =
        new SimpleRepositoryFileData( new ByteArrayInputStream( "Ciao World!".getBytes( "UTF-8" ) ), "UTF-8",
            "text/plain" );
    simpleFile = repo.updateFile( simpleFile, simpleData, null );
    assertNotNull( simpleFile.getLastModifiedDate() );

    logger.info( "getDataForRead (binary)" );
    simpleFileData = repo.getDataForRead( simpleFile.getId(), SimpleRepositoryFileData.class );
    assertNotNull( simpleFileData );
    assertEquals( "Ciao World!", IOUtils.toString( simpleFileData.getInputStream(), simpleFileData.getEncoding() ) );

    logger.info( "getDataForReadAtVersion (binary)" );
    simpleFileData = repo.getDataAtVersionForRead( simpleFile.getId(), simpleVersion, SimpleRepositoryFileData.class );
    assertNotNull( simpleFileData );
    assertEquals( "Hello World!", IOUtils.toString( simpleFileData.getInputStream(), simpleFileData.getEncoding() ) );

    logger.info( "getChildren" );
    List<RepositoryFile> folder1Children = repo.getChildren( new RepositoryRequest( String.valueOf( folder1.getId() ), true, -1, null ) );
    assertNotNull( folder1Children );
    assertEquals( 2, folder1Children.size() );
    logger.info( "getChildren" );
    List<RepositoryFile> folder1ChildrenFiltered = repo.getChildren( new RepositoryRequest( String.valueOf( folder1.getId() ), true, -1, "*.sample" ) );
    assertNotNull( folder1ChildrenFiltered );
    assertEquals( 0, folder1ChildrenFiltered.size() );
    logger.info( "getDeletedFiles" );
    assertEquals( 0, repo.getDeletedFiles().size() );
    logger.info( "deleteFile" );
    repo.deleteFile( file1.getId(), null );
    logger.info( "getDeletedFiles" );
    assertEquals( 0, repo.getDeletedFiles( folder1.getPath(), "*.sample" ).size() );

    logger.info( "hasAccess" );
    assertFalse( repo.hasAccess( "/pentaho", EnumSet.of( RepositoryFilePermission.WRITE ) ) );

    logger.info( "getEffectiveAces" );
    List<RepositoryFileAce> folder1EffectiveAces = repo.getEffectiveAces( folder1.getId() );
    assertEquals( 1, folder1EffectiveAces.size() );

    logger.info( "getAcl" );
    RepositoryFileAcl folder1Acl = repo.getAcl( folder1.getId() );
    assertEquals( USERNAME_SUZY, folder1Acl.getOwner().getName() );

    logger.info( "updateAcl" );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, PASSWORD, "", null );
    RepositoryFileAcl updatedFolder1Acl =
        repo.updateAcl( new RepositoryFileAcl.Builder( folder1Acl ).entriesInheriting( false ).ace(
            userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ), RepositoryFileSid.Type.USER,
            RepositoryFilePermission.ALL ).build() );
    assertNotNull( updatedFolder1Acl );
    assertEquals( 1, updatedFolder1Acl.getAces().size() );

    logger.info( "lockFile" );
    assertFalse( file1.isLocked() );
    repo.lockFile( file1.getId(), "I locked this file" );
    logger.info( "canUnlockFile" );
    assertTrue( repo.canUnlockFile( file1.getId() ) );
    logger.info( "unlockFile" );
    repo.unlockFile( file1.getId() );

    logger.info( "moveFile" );
    repo.moveFile( file1.getId(), ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) + "/folder1", null );
    logger.info( "copyFile" );
    repo.copyFile( file1.getId(), ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY )
        + "/folder1/fileB.whatever", null );
    RepositoryFile copiedFile = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY )
        + "/folder1/fileB.whatever" );
    copiedFile = repo.updateFile( copiedFile, data, null );

    logger.info( "getVersionSummaries" );
    List<VersionSummary> versionSummaries = repo.getVersionSummaries( file1.getId() );
    assertNotNull( versionSummaries );
    // copy doesn't increase version number
    assertTrue( versionSummaries.size() >= 1 );
    assertEquals( USERNAME_SUZY, versionSummaries.get( 0 ).getAuthor() );

    logger.info( "getVersionSummary" );
    VersionSummary versionSummary = repo.getVersionSummary( file1.getId(), null );
    assertNotNull( versionSummary );
    assertNotNull( versionSummary.getId() );

    logger.info( "getFileAtVersion" );
    RepositoryFile file1AtVersion = repo.getFileAtVersion( file1.getId(), versionSummary.getId() );
    assertNotNull( file1AtVersion );
    assertEquals( versionSummary.getId(), file1AtVersion.getVersionId() );

    logger.info( "getTree" );
    RepositoryFileTree tree = repo.getTree( new RepositoryRequest( ClientRepositoryPaths.getRootFolderPath(), true, -1, null ) );
    assertNotNull( tree.getFile().getId() );

    logger.info( "getDataForReadInBatch" );
    List<NodeRepositoryFileData> result =
        repo.getDataForReadInBatch( Arrays.asList( file1, copiedFile ), NodeRepositoryFileData.class );
    assertEquals( 2, result.size() );

    logger.info( "getVersionSummaryInBatch" );
    List<VersionSummary> vResult = repo.getVersionSummaryInBatch( Arrays.asList( file1, simpleFile ) );
    assertEquals( 2, vResult.size() );

    logger.info( "getReservedChars" );
    assertFalse( repo.getReservedChars().isEmpty() );
  }

  private NodeRepositoryFileData makeNodeRepositoryFileData1() {
    DataNode node = new DataNode( "testNode" );
    node.setProperty( "prop1", "hello world" );
    node.setProperty( "prop2", false );
    node.setProperty( "prop3", 12L );
    return new NodeRepositoryFileData( node );
  }
}
