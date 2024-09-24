/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources.services;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.util.RepositoryPathEncoder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * @author Andrey Khayrutdinov
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
  "classpath:/repository-test-override.spring.xml" } )
public class FileService_CreateDir_IT implements ApplicationContextAware {

  @BeforeClass
  public static void init() throws Exception {
    DefaultUnifiedRepositoryBase.setUpClass();
  }

  @AfterClass
  public static void dispose() throws Exception {
    DefaultUnifiedRepositoryBase.tearDownClass();
  }

  @Override
  public void setApplicationContext( final ApplicationContext applicationContext ) throws BeansException {
    testBase.setApplicationContext( applicationContext );
    repository = (IUnifiedRepository) applicationContext.getBean( "unifiedRepository" );
  }

  private final DefaultUnifiedRepositoryBase testBase = new DefaultUnifiedRepositoryBase();
  private IUnifiedRepository repository;

  private FileService service;

  @Before
  public void setUp() throws Exception {
    testBase.setUp();
    testBase.loginAsSysTenantAdmin();

    service = new FileService();
  }

  @After
  public void tearDown() throws Exception {
    testBase.tearDown();
  }


  @Test
  public void doCreateDir_ValidName() throws Exception {
    assertTrue( callCreateDirSafe( "/public/valid-name" ) );
    assertNotNull( repository.getFile( "/public/valid-name" ) );
  }

  @Test
  public void doCreateDir_ValidName_WithDot() throws Exception {
    assertTrue( callCreateDirSafe( "/public/valid.name" ) );
    assertNotNull( repository.getFile( "/public/valid.name" ) );
  }

  @Test( expected = FileService.InvalidNameException.class )
  public void doCreateDir_InvalidName_Dot() throws Exception {
    callCreateDirSafe( "/public/." );
  }

  @Test( expected = FileService.InvalidNameException.class )
  public void doCreateDir_InvalidName_TwoDots() throws Exception {
    callCreateDirSafe( "/public/.." );
  }

  @Test( expected = FileService.InvalidNameException.class )
  public void doCreateDir_InvalidName_NewLine() throws Exception {
    callCreateDirSafe( "/public/pre-\n-post" );
  }

  @Test( expected = FileService.InvalidNameException.class )
  public void doCreateDir_InvalidName_Slash() throws Exception {
    service.doCreateDirSafe( ":public:pre-/-post" );
  }

  @Test( expected = FileService.InvalidNameException.class )
  public void doCreateDir_InvalidName_BackSlash() throws Exception {
    callCreateDirSafe( "/public/pre-\\-post" );
  }

  @Test
  public void doCreateDir_Exists() throws Exception {
    repository.createFolder(
      repository.getFile( "/public" ).getId(),
      new RepositoryFile.Builder( "existing" ).folder( true ).build(),
      null
    );
    assertNotNull( repository.getFile( "/public/existing" ) );

    assertFalse( "When folder exists, false should be returned", callCreateDirSafe( "/public/existing" ) );
  }

  private boolean callCreateDirSafe( String path ) throws Exception {
    String pathId = RepositoryPathEncoder.encodeRepositoryPath( path );
    return service.doCreateDirSafe( pathId );
  }
}
