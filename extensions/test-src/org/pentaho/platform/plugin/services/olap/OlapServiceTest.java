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
import java.util.Properties;

import junit.framework.TestCase;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.plugin.action.olap.impl.OlapServiceImpl;
import org.pentaho.platform.plugin.services.cache.CacheManager;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.util.Base64PasswordService;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.plugin.UserRoleMapperTest.TestUserRoleListService;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.*;

public class OlapServiceTest extends TestCase {

  private IUnifiedRepository repository;
  private MicroPlatform booter;

  @Override
  protected void setUp() throws Exception {

    PentahoSessionHolder.setSession(new StandaloneSession( "admin" ) );

    repository = mock( IUnifiedRepository.class );

    booter = new MicroPlatform( "test-src/solution" );
    booter.define( IPasswordService.class, Base64PasswordService.class, Scope.GLOBAL );
    booter.define( IDatabaseConnection.class, DatabaseConnection.class, Scope.GLOBAL );
    booter.define( IDatabaseDialectService.class, DatabaseDialectService.class, Scope.GLOBAL );
    booter.define( ICacheManager.class, CacheManager.class, Scope.GLOBAL );
    booter.define( IUserRoleListService.class, TestUserRoleListService.class, Scope.GLOBAL );
    booter.defineInstance( IUnifiedRepository.class, repository );
    booter.define( ISolutionEngine.class, SolutionEngine.class, Scope.GLOBAL );
    booter.define( IOlapService.class, OlapServiceImpl.class, Scope.GLOBAL );
    booter.setSettingsProvider( new SystemSettings() );
    booter.start();
  }

  public void testImportHosted() throws Exception {

    // Stub /etc/mondrian
    final String mondrianFolderPath =
      ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "mondrian";
    stubGetFolder( repository, mondrianFolderPath );
    stubGetChildren( repository, mondrianFolderPath ); // return no children

    final String testFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "test-server";
    stubGetFileDoesNotExist( repository, testFolderPath );
    stubCreateFolder( repository, testFolderPath );
    final String metadataPath = testFolderPath + RepositoryFile.SEPARATOR + "metadata";
    stubCreateFile( repository, metadataPath );

    final IPentahoSession session = new StandaloneSession( "admin" );
    final IOlapService olapService = PentahoSystem.get( IOlapService.class );

    final InputStream is =
      new FileInputStream(
        new File( "test-src/solution/security/steelwheels.mondrian.xml" ) );

    olapService.addHostedCatalog(
      "test-server",
      "Provider=mondrian;DataSource=SampleData",
      is,
      true,
      session );

    verify( repository ).createFile(
      eq( makeIdObject( testFolderPath ) ),
      argThat( isLikeFile( makeFileObject( metadataPath ) ) ),
      argThat( hasData(
        pathPropertyPair( "/catalog/definition", "mondrian:/" + "test-server" ),
        pathPropertyPair( "/catalog/datasourceInfo", "Provider=mondrian;DataSource=SampleData" )
      ) ), anyString() );

    verify( repository ).createFile( eq( makeIdObject( testFolderPath ) ),
      argThat( isLikeFile( makeFileObject( testFolderPath + RepositoryFile.SEPARATOR + "schema.xml" ) ) ),
      any( IRepositoryFileData.class ), anyString() );
  }

  public void testImportOlap4j() throws Exception {

    // Stub /etc/mondrian
    final String olapFolderPath =
      ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "olap-servers";
    stubGetFolder( repository, olapFolderPath );
    stubGetChildren( repository, olapFolderPath ); // return no children

    final String testFolderPath = olapFolderPath + RepositoryFile.SEPARATOR + "test-server-2";
    stubGetFileDoesNotExist( repository, testFolderPath );
    stubCreateFolder( repository, testFolderPath );
    final String metadataPath = testFolderPath + RepositoryFile.SEPARATOR + "metadata";
    stubCreateFile( repository, metadataPath );

    final IPentahoSession session = new StandaloneSession( "admin" );
    final IOlapService olapService = PentahoSystem.get( IOlapService.class );

    olapService.addOlap4jCatalog(
      "test-server-2",
      "class-name",
      "url",
      "user",
      "password",
      new Properties(),
      true,
      session );

    verify( repository ).createFile(
      eq( makeIdObject( testFolderPath ) ),
      argThat( isLikeFile( makeFileObject( metadataPath ) ) ),
      argThat( hasData(
        pathPropertyPair( "/name", "test-server-2" ),
        pathPropertyPair( "/className", "class-name" ),
        pathPropertyPair( "/url", "url" ),
        pathPropertyPair( "/password", "password" )
      ) ), anyString() );
  }
}
