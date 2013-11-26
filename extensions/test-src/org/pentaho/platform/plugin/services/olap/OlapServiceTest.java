/*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 *
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.platform.plugin.services.olap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.plugin.action.olap.IOlapServiceException;
import org.pentaho.platform.plugin.action.olap.impl.OlapServiceImpl;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;

public class OlapServiceTest extends TestCase {

  private IUnifiedRepository repository;

  @Override
  protected void setUp() throws Exception {

    PentahoSessionHolder.setSession(new StandaloneSession( "admin" ) );

    repository = new MockUnifiedRepository( new MockUserProvider() );

    repository.createFolder(
      repository.getFile( "/" ).getId(),
      new RepositoryFile.Builder( "etc" ).folder( true ).build(),
      "initialization" );

    RepositoryFile mondrianFolder =
      repository.createFolder(
        repository.getFile( "/etc" ).getId(),
        new RepositoryFile.Builder( "mondrian" ).folder( true ).build(),
        "initialization" );
    RepositoryFile olapFolder =
      repository.createFolder(
        repository.getFile( "/etc" ).getId(),
        new RepositoryFile.Builder( "olap-servers" ).folder( true ).build(),
        "initialization" );

    // Make sure the repository is setup correctly
    assertNotNull( mondrianFolder );
    assertNotNull( mondrianFolder.getId() );
    assertNotNull( olapFolder );
    assertNotNull( olapFolder.getId() );

    final Map<String, Serializable> fileMetadata =
      repository.getFileMetadata(mondrianFolder.getId() );
    assertNotNull( fileMetadata );
    assertEquals( 0, fileMetadata.size() );
  }

  public void testDoImport() throws Exception {

    final IOlapService olapService = new OlapServiceImpl( repository );
    try {
      olapService.addHostedCatalog( null, null, null, true, null );
      fail( "Null data should throw exception" );
    } catch ( IOlapServiceException success ) {
    }

    final InputStream is =
      new FileInputStream(
        new File( "test-src/solution/security/steelwheels.mondrian.xml" ) );

    olapService.addHostedCatalog(
      "test-server",
      "Provider=mondrian;DataSource=SampleData",
      is,
      true,
      new StandaloneSession( "admin" ) );
    assertNotNull( repository.getFile( "/etc/mondrian/test-server" ) );
  }

  private class MockUserProvider implements MockUnifiedRepository.ICurrentUserProvider {
    @Override
    public String getUser() {
      return "admin";
    }

    @Override
    public List<String> getRoles() {
      return Arrays.asList( "Administrator" );
    }
  }
}
