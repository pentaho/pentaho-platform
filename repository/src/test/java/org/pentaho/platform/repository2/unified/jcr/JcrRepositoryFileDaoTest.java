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

import org.apache.jackrabbit.core.VersionManagerImpl;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoterManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.springframework.dao.DataAccessException;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class JcrRepositoryFileDaoTest {

  private JcrRepositoryFileDao dao;

  private IRepositoryAccessVoterManager accessVoterManager;

  private IPentahoSession pentahoSession;

  @Before
  public void setUp() throws RepositoryException {
    Node node = mock( Node.class );
    Node nodeParent = mock( Node.class );
    when( node.getIdentifier() ).thenReturn( "" );
    when( nodeParent.getIdentifier() ).thenReturn( "" );
    when( node.getParent() ).thenReturn( nodeParent );
    when( node.isNodeType( "null:pentahoFile" ) ).thenReturn( true );
    when( node.isNodeType( "null:pentahoVersionable" ) ).thenReturn( true );
    VersionManagerImpl versionManager = mock( VersionManagerImpl.class );
    Workspace workspace = mock( Workspace.class );
    when( workspace.getVersionManager() ).thenReturn( versionManager );
    Session session = mock( Session.class );
    when( session.getWorkspace() ).thenReturn( workspace );
    when( session.getNodeByIdentifier( nullable( String.class) ) ).thenReturn( node );
    when( session.getItem( nullable( String.class) ) ).thenReturn( node );
    pentahoSession = mock( IPentahoSession.class );
    PentahoSessionHolder.setSession( pentahoSession );
    IRepositoryVersionManager repositoryVersionManager = mock( IRepositoryVersionManager.class );
    when( repositoryVersionManager.isVersioningEnabled( nullable( String.class) ) ).thenReturn( true );
    PentahoSystem.registerObject( repositoryVersionManager );
    JcrTemplate jcrTemplate = new JcrTemplate() {
      @Override
      public Object execute( JcrCallback callback ) throws DataAccessException {

        try {
          return callback.doInJcr( session );
        } catch ( Exception e ) {
          // wrapping exception to comply overriding rules
          throw new RuntimeException( e );
        }
      }
    };
    List<ITransformer<IRepositoryFileData>> transformerList = Collections.emptyList();
    IPathConversionHelper pathConversionHelper = new DefaultPathConversionHelper();
    IRepositoryFileAclDao aclDao = mock( IRepositoryFileAclDao.class );
    accessVoterManager = mock( IRepositoryAccessVoterManager.class );
    JcrRepositoryFileDao jcrDao = new JcrRepositoryFileDao( jcrTemplate, transformerList, null, null,
      pathConversionHelper, aclDao, null, accessVoterManager );
    dao = spy( jcrDao );
  }

  @Test
  public void shouldConsultAccessVoterWhenCopyingOrMovingFiles() {
    RepositoryFile filePermitted = mock( RepositoryFile.class );
    RepositoryFile fileNotPermitted = mock( RepositoryFile.class );
    RepositoryFile destinationPermitted = mock( RepositoryFile.class );
    RepositoryFile destinationNotPermitted = mock( RepositoryFile.class );
    doReturn( filePermitted ).when( dao ).getFileById( "/filePermitted" );
    doReturn( fileNotPermitted ).when( dao ).getFileById( "/fileNotPermitted" );
    doReturn( destinationPermitted ).when( dao ).getFile( "/destinationPermitted" );
    doReturn( destinationNotPermitted ).when( dao ).getFile( "/destinationNotPermitted" );
    doReturn( true ).when( accessVoterManager )
      .hasAccess( filePermitted, RepositoryFilePermission.WRITE, null, pentahoSession );
    doReturn( false ).when( accessVoterManager )
      .hasAccess( fileNotPermitted, RepositoryFilePermission.WRITE, null, pentahoSession );
    doReturn( true ).when( accessVoterManager )
      .hasAccess( destinationPermitted, RepositoryFilePermission.WRITE, null, pentahoSession );
    doReturn( false ).when( accessVoterManager )
      .hasAccess( destinationNotPermitted, RepositoryFilePermission.WRITE, null, pentahoSession );

    // should move the file,
    // if the user has write permissions to the source file,
    // and write permissions to the destination folder.
    try {
      dao.moveFile( "/filePermitted", "/destinationPermitted", null );
    } catch ( Throwable e ) {
      fail( e.getMessage() );
    }

    // should NOT move the file and throw an exception,
    // if the user has write permissions to the destination folder,
    // but does not have write permissions to the source file.
    try {
      dao.moveFile( "/fileNotPermitted", "/destinationPermitted", null );
    } catch ( Throwable e ) {
      // unwrap original exception and check it
      if ( !( e instanceof RuntimeException )
        || !( e.getCause() instanceof AccessDeniedException ) ) {
        fail( e.getMessage() );
      }
    }

    // should NOT move the file and throw an exception,
    // if the user has write permissions to the source file,
    // but does not have write permissions to the destination folder.
    try {
      dao.moveFile( "/filePermitted", "/destinationNotPermitted", null );
    } catch ( Throwable e ) {
      // unwrap original exception and check it
      if ( !( e instanceof RuntimeException )
        || !( e.getCause() instanceof AccessDeniedException ) ) {
        fail( e.getMessage() );
      }
    }

    // should NOT move the file and throw an exception,
    // if the user neither has write permissions to the source file,
    // nor has write permissions to the destination folder.
    try {
      dao.moveFile( "/fileNotPermitted", "/destinationNotPermitted", null );
    } catch ( Throwable e ) {
      // unwrap original exception and check it
      if ( !( e instanceof RuntimeException )
        || !( e.getCause() instanceof AccessDeniedException ) ) {
        fail( e.getMessage() );
      }
    }
  }
}
