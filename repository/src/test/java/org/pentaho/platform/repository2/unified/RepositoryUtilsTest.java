/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.repository2.unified;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository.SpringSecurityCurrentUserProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Class Description User: dkincade
 */
@SuppressWarnings( "nls" )
public class RepositoryUtilsTest {

  @BeforeClass
  public static void setUpClass() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken( "admin", null, new ArrayList<GrantedAuthority>() ) );
  }

  @AfterClass
  public static void tearDownClass() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void testCreationError() throws Exception {
    try {
      new RepositoryUtils( null );
      fail( "Should not allow creating with no repository" );
    } catch ( Exception success ) {
      //ignored
    }
  }

  @Test
  public void testGetFolder() throws Exception {
    final MockUnifiedRepository repository = new MockUnifiedRepository( new SpringSecurityCurrentUserProvider() );
    final RepositoryUtils repositoryUtils = new RepositoryUtils( repository );

    RepositoryFile test = repositoryUtils.getFolder( "/public/one/two/three", true, true, null );
    assertNotNull( test );
    assertEquals( "The folder name is invalid", "three", test.getName() );
    assertEquals( "The path is invalid", "/public/one/two/three", test.getPath() );
    assertTrue( "The folder should be defined as a folder", test.isFolder() );

    // Make sure it created the parents
    RepositoryFile one = repositoryUtils.getFolder( "/public/one", false, false, null );
    assertNotNull( one );
    RepositoryFile two = repositoryUtils.getFolder( "/public/one/two", false, false, null );
    assertNotNull( two );
  }

  @Test
  public void testGetFile() throws Exception {
    final MockUnifiedRepository repository = new MockUnifiedRepository( new SpringSecurityCurrentUserProvider() );
    final RepositoryUtils repositoryUtils = new RepositoryUtils( repository );

    final SimpleRepositoryFileData data =
        new SimpleRepositoryFileData( new ByteArrayInputStream( "Test".getBytes() ), "UTF-8", "text/plain" );
    RepositoryFile test = repositoryUtils.getFile( "/public/one/two/three.prpt", data, true, true, null );
    assertNotNull( test );
    assertEquals( "The filename is invalid", "three.prpt", test.getName() );
    assertEquals( "The path is invalid", "/public/one/two/three.prpt", test.getPath() );
    assertFalse( "The file should not be defined as a folder", test.isFolder() );

    // Make sure it created the parents
    RepositoryFile one = repositoryUtils.getFolder( "/public/one", false, false, null );
    assertNotNull( one );
    RepositoryFile two = repositoryUtils.getFolder( "/public/one/two", false, false, null );
    assertNotNull( two );
  }
}
