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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.jcr;

import org.apache.jackrabbit.core.VersionManagerImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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

public class JcrRepositoryFileDaoTest {

  private JcrRepositoryFileDao dao;

  private IRepositoryAccessVoterManager accessVoterManager;

  private IPentahoSession pentahoSession;

  @Before
  public void setUp() throws RepositoryException {
    Node node = Mockito.mock( Node.class );
    Node nodeParent = Mockito.mock( Node.class );
    Mockito.when( node.getIdentifier() ).thenReturn( "" );
    Mockito.when( nodeParent.getIdentifier() ).thenReturn( "" );
    Mockito.when( node.getParent() ).thenReturn( nodeParent );
    Mockito.when( node.isNodeType( "null:pentahoFile" ) ).thenReturn( true );
    Mockito.when( node.isNodeType( "null:pentahoVersionable" ) ).thenReturn( true );
    VersionManagerImpl versionManager = Mockito.mock( VersionManagerImpl.class );
    Workspace workspace = Mockito.mock( Workspace.class );
    Mockito.when( workspace.getVersionManager() ).thenReturn( versionManager );
    final Session session = Mockito.mock( Session.class );
    Mockito.when( session.getWorkspace() ).thenReturn( workspace );
    Mockito.when( session.getNodeByIdentifier( Mockito.anyString() ) ).thenReturn( node );
    Mockito.when( session.getItem( Mockito.anyString() ) ).thenReturn( node );
    pentahoSession = Mockito.mock( IPentahoSession.class );
    PentahoSessionHolder.setSession( pentahoSession );
    IRepositoryVersionManager repositoryVersionManager = Mockito.mock( IRepositoryVersionManager.class );
    Mockito.when( repositoryVersionManager.isVersioningEnabled( Mockito.anyString() ) ).thenReturn( true );
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
    IRepositoryFileAclDao aclDao = Mockito.mock( IRepositoryFileAclDao.class );
    accessVoterManager = Mockito.mock( IRepositoryAccessVoterManager.class );
    JcrRepositoryFileDao jcrDao = new JcrRepositoryFileDao( jcrTemplate, transformerList, null, null,
      pathConversionHelper, aclDao, null, accessVoterManager );
    dao = Mockito.spy( jcrDao );
  }

  @Test
  public void shouldConsultAccessVoterWhenCopyingOrMovingFiles() {
    RepositoryFile filePermitted = Mockito.mock( RepositoryFile.class );
    RepositoryFile fileNotPermitted = Mockito.mock( RepositoryFile.class );
    RepositoryFile destinationPermitted = Mockito.mock( RepositoryFile.class );
    RepositoryFile destinationNotPermitted = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( filePermitted ).when( dao ).getFileById( "/filePermitted" );
    Mockito.doReturn( fileNotPermitted ).when( dao ).getFileById( "/fileNotPermitted" );
    Mockito.doReturn( destinationPermitted ).when( dao ).getFile( "/destinationPermitted" );
    Mockito.doReturn( destinationNotPermitted ).when( dao ).getFile( "/destinationNotPermitted" );
    Mockito.doReturn( true ).when( accessVoterManager )
      .hasAccess( filePermitted, RepositoryFilePermission.WRITE, null, pentahoSession );
    Mockito.doReturn( false ).when( accessVoterManager )
      .hasAccess( fileNotPermitted, RepositoryFilePermission.WRITE, null, pentahoSession );
    Mockito.doReturn( true ).when( accessVoterManager )
      .hasAccess( destinationPermitted, RepositoryFilePermission.WRITE, null, pentahoSession );
    Mockito.doReturn( false ).when( accessVoterManager )
      .hasAccess( destinationNotPermitted, RepositoryFilePermission.WRITE, null, pentahoSession );

    // should move the file,
    // if the user has write permissions to the source file,
    // and write permissions to the destination folder.
    try {
      dao.moveFile( "/filePermitted", "/destinationPermitted", null );
    } catch ( Throwable e ) {
      Assert.fail( e.getMessage() );
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
        Assert.fail( e.getMessage() );
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
        Assert.fail( e.getMessage() );
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
        Assert.fail( e.getMessage() );
      }
    }
  }
}
