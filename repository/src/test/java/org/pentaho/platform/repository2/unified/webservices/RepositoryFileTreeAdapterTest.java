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


package org.pentaho.platform.repository2.unified.webservices;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.RepositoryFileProxy;

public class RepositoryFileTreeAdapterTest extends TestCase {

  /**
   * Assert empty list in RepositoryFileTree#children survives full jaxb serialization roundtrip
   */
  @Test
  public void testBIServer7777() throws Exception {

    IUnifiedRepository unifiedRepository = mock( IUnifiedRepository.class );
    PentahoSystem.registerObject( unifiedRepository );
    RepositoryFileAcl acl = new RepositoryFileAcl.Builder( "admin" ).build();
    when( unifiedRepository.getAcl( anyString() ) ).thenReturn( acl );

    IRepositoryVersionManager mockRepositoryVersionManager = mock( IRepositoryVersionManager.class );
    when( mockRepositoryVersionManager.isVersioningEnabled( anyString() ) ).thenReturn( true );
    when( mockRepositoryVersionManager.isVersionCommentEnabled( anyString() ) ).thenReturn( false );
    JcrRepositoryFileUtils.setRepositoryVersionManager( mockRepositoryVersionManager );

    // file tree with empty children
    RepositoryFile empty = new RepositoryFile.Builder( "empty" ).build();
    RepositoryFileTree emptyDir = new RepositoryFileTree( empty, Collections.<RepositoryFileTree>emptyList() );
    RepositoryFile root = new RepositoryFile.Builder( "rootDir" ).build();
    ArrayList<RepositoryFileTree> children = new ArrayList<RepositoryFileTree>( 1 );
    children.add( emptyDir );
    RepositoryFileTree rootDir = new RepositoryFileTree( root, children );
    // to DTO
    RepositoryFileTreeAdapter adapter = new RepositoryFileTreeAdapter();
    RepositoryFileTreeDto dtoThere = adapter.marshal( rootDir );
    assertNotNull( dtoThere.getChildren().get( 0 ).getChildren() );
    // serialize
    final JAXBContext jaxbContext = JAXBContext.newInstance( RepositoryFileTreeDto.class );
    Marshaller marshaller = jaxbContext.createMarshaller();
    StringWriter sw = new StringWriter();
    marshaller.marshal( dtoThere, sw );
    // and bring it back
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    StringReader sr = new StringReader( sw.toString() );
    RepositoryFileTreeDto dtoBackAgain = (RepositoryFileTreeDto) unmarshaller.unmarshal( sr );
    assertNotNull( dtoBackAgain.getChildren().get( 0 ).getChildren() );
    // unmarshall
    RepositoryFileTree rootDir2 = adapter.unmarshal( dtoBackAgain );
    assertNotNull( rootDir2.getChildren().get( 0 ).getChildren() );
    assertEquals( rootDir, rootDir2 );
  }

  @Test
  public void testWhenChildrenIsDeleted() throws Exception {
    // mock RepositoryFile to return null
    RepositoryFile mockFile = mock( RepositoryFileProxy.class );
    when( mockFile.isHidden() ).thenReturn( null );

    // create tree with the mockFile
    RepositoryFileTree nullValueDir = new RepositoryFileTree( mockFile, Collections.<RepositoryFileTree>emptyList() );
    RepositoryFile root = new RepositoryFile.Builder( "rootDir" ).build();
    ArrayList<RepositoryFileTree> children = new ArrayList<RepositoryFileTree>( 1 );
    children.add( nullValueDir );
    RepositoryFileTree rootDir = new RepositoryFileTree( root, children );

    // to DTO
    RepositoryFileTreeAdapter adapter = new RepositoryFileTreeAdapter();
    RepositoryFileTreeDto dtoThere = adapter.marshal( rootDir );

    // as isHidden() returns null, it's expected that null was returned, so root has no children
    assertTrue( dtoThere.getChildren().isEmpty() );
  }

}
