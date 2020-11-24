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
 * Copyright (c) 2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources.services;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class FileServiceTest {

  public static final String UTF_8 = StandardCharsets.UTF_8.name();

  private FileService fileService;

  @Before
  public void setUp() throws Exception {
    fileService = spy( FileService.class );
    final ISystemSettings settingsService = mock( ISystemSettings.class );
    when( settingsService.getSystemSetting( eq( "anonymous-authentication/anonymous-user" )  , eq("anonymousUser"))).thenReturn( "anonymousUser");
    PentahoSystem.setSystemSettingsService( settingsService );
    final ISystemConfig systemConfig = mock( ISystemConfig.class );

    final IPentahoObjectFactory objectFactory = mock( IPentahoObjectFactory.class );
    when( objectFactory.objectDefined( eq( ISystemConfig.class ) ) ).thenReturn( true );
    final IPentahoObjectReference pentahoObjectReference = mock( IPentahoObjectReference.class );
    when( pentahoObjectReference.getObject() ).thenReturn( systemConfig );
    try {
      when( objectFactory.getObjectReferences( eq( ISystemConfig.class ), any( IPentahoSession.class ),
          any( Map.class ) ) ).thenReturn( new LinkedList() { {
        add( pentahoObjectReference );
      } } );
    } catch ( ObjectFactoryException e ) {
      e.printStackTrace();
    }
    when( settingsService.getSystemSetting( anyString(), anyString() ) ).thenReturn( "" );
    PentahoSystem.registerObjectFactory( objectFactory );

    PentahoSystem.init();
  }


  @Test
  public void testIsVisible() throws Exception {
    RepositoryFileDto hiddenDto = new RepositoryFileDto();
    hiddenDto.setFolder( true );
    hiddenDto.setHidden( true );
    hiddenDto.setId( "5345345345345345345" );
    hiddenDto.setName( "suzy" );
    RepositoryFileDto visibleDto = new RepositoryFileDto();
    visibleDto.setFolder( true );
    visibleDto.setHidden( false );
    visibleDto.setId( "5345345345345345345" );
    visibleDto.setName( "joe" );
    DefaultUnifiedRepositoryWebService repoWs = mock ( DefaultUnifiedRepositoryWebService.class );

    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( hiddenDto ).when( repoWs ).getFile( "/home/suzy" );
    doReturn( visibleDto ).when( repoWs ).getFile( "/home/joe" );

    assertEquals( fileService.doGetIsVisible( "/home/suzy"), "false" );
    assertEquals( fileService.doGetIsVisible( "/home/joe"), "true" );
  }

  @Test
  public void testGetDefaultLocation_Scenario_ProvidedFolderIsNull() throws Exception {
    DefaultUnifiedRepositoryWebService repoWs = mock ( DefaultUnifiedRepositoryWebService.class );
    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( null ).when( repoWs ).getFile( "/home/joe" );
    assertEquals( fileService.doGetDefaultLocation( "/home/joe"), ClientRepositoryPaths.getRootFolderPath() );
  }

  @Test
  public void testGetDefaultLocation_Scenario_ProvidedFolderIsVisible() throws Exception {
    RepositoryFileDto visibleDto = new RepositoryFileDto();
    visibleDto.setFolder( true );
    visibleDto.setHidden( false );
    visibleDto.setId( "5345345345345345345" );
    visibleDto.setName( "joe" );
    DefaultUnifiedRepositoryWebService repoWs = mock ( DefaultUnifiedRepositoryWebService.class );
    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( visibleDto ).when( repoWs ).getFile( "/home/joe" );
    assertEquals( fileService.doGetDefaultLocation( "/home/joe"), "/home/joe" );
  }

  @Test
  public void testGetDefaultLocation_Scenario__DefaultFolderIsNotProvidedPublicFolderIsVisible() throws Exception {
    RepositoryFileDto hiddenDto = new RepositoryFileDto();
    hiddenDto.setFolder( true );
    hiddenDto.setHidden( true );
    hiddenDto.setId( "5345345345345345345" );
    hiddenDto.setName( "suzy" );
    RepositoryFileDto publicDto = new RepositoryFileDto();
    publicDto.setFolder( true );
    publicDto.setHidden( false );
    publicDto.setId( "5345345345345345345" );
    publicDto.setName( "suzy" );
    DefaultUnifiedRepositoryWebService repoWs = mock ( DefaultUnifiedRepositoryWebService.class );
    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( hiddenDto ).when( repoWs ).getFile( "/home/suzy" );
    when( PentahoSystem.get( ISystemConfig.class ).getProperty( eq( PentahoSystem.DEFAULT_FOLDER_WHEN_HOME_FOLDER_IS_HIDDEN_PROPERTY ) ) ).thenReturn( null );
    doReturn( publicDto ).when( repoWs ).getFile( ClientRepositoryPaths.getPublicFolderPath() );
    assertEquals( fileService.doGetDefaultLocation( "/home/suzy"), ClientRepositoryPaths.getPublicFolderPath() );
  }

  @Test
  public void testGetDefaultLocation_Scenario_DefaultFolderIsNotProvidedPublicFolderIsHidden() throws Exception {
    RepositoryFileDto hiddenDto = new RepositoryFileDto();
    hiddenDto.setFolder( true );
    hiddenDto.setHidden( true );
    hiddenDto.setId( "5345345345345345345" );
    hiddenDto.setName( "suzy" );
    RepositoryFileDto publicDto = new RepositoryFileDto();
    publicDto.setFolder( true );
    publicDto.setHidden( true );
    publicDto.setId( "5345345345345345345" );
    publicDto.setName( "suzy" );
    DefaultUnifiedRepositoryWebService repoWs = mock ( DefaultUnifiedRepositoryWebService.class );
    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( hiddenDto ).when( repoWs ).getFile( "/home/suzy" );
    when( PentahoSystem.get( ISystemConfig.class ).getProperty( eq( PentahoSystem.DEFAULT_FOLDER_WHEN_HOME_FOLDER_IS_HIDDEN_PROPERTY ) ) ).thenReturn( null );
    doReturn( publicDto ).when( repoWs ).getFile( ClientRepositoryPaths.getPublicFolderPath() );
    assertEquals( fileService.doGetDefaultLocation( "/home/suzy"), ClientRepositoryPaths.getRootFolderPath() );
  }

  @Test
  public void testGetDefaultLocation_Scenario_DefaultFolderIsVisible() throws Exception {
    RepositoryFileDto hiddenDto = new RepositoryFileDto();
    hiddenDto.setFolder( true );
    hiddenDto.setHidden( true );
    hiddenDto.setId( "5345345345345345345" );
    hiddenDto.setName( "suzy" );
    RepositoryFileDto defaultDto = new RepositoryFileDto();
    defaultDto.setFolder( true );
    defaultDto.setHidden( false );
    defaultDto.setId( "5345345345345345345" );
    defaultDto.setName( "suzy" );
    DefaultUnifiedRepositoryWebService repoWs = mock ( DefaultUnifiedRepositoryWebService.class );
    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( hiddenDto ).when( repoWs ).getFile( "/home/suzy" );
    when( PentahoSystem.get( ISystemConfig.class ).getProperty( eq( PentahoSystem.DEFAULT_FOLDER_WHEN_HOME_FOLDER_IS_HIDDEN_PROPERTY ) ) ).thenReturn( "/default-folder" );
    doReturn( defaultDto ).when( repoWs ).getFile( "/default-folder" );
    assertEquals( fileService.doGetDefaultLocation( "/home/suzy"), "/default-folder" );
  }

  @Test
  public void testGetDefaultLocation_Scenario_DefaultFolderIsHidden() throws Exception {
    RepositoryFileDto hiddenDto = new RepositoryFileDto();
    hiddenDto.setFolder( true );
    hiddenDto.setHidden( true );
    hiddenDto.setId( "5345345345345345345" );
    hiddenDto.setName( "suzy" );
    RepositoryFileDto defaultDto = new RepositoryFileDto();
    defaultDto.setFolder( true );
    defaultDto.setHidden( true );
    defaultDto.setId( "5345345345345345345" );
    defaultDto.setName( "suzy" );
    DefaultUnifiedRepositoryWebService repoWs = mock ( DefaultUnifiedRepositoryWebService.class );
    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( hiddenDto ).when( repoWs ).getFile( "/home/suzy" );
    when( PentahoSystem.get( ISystemConfig.class ).getProperty( eq( PentahoSystem.DEFAULT_FOLDER_WHEN_HOME_FOLDER_IS_HIDDEN_PROPERTY ) ) ).thenReturn( "/default-folder" );
    doReturn( defaultDto ).when( repoWs ).getFile( "/default-folder" );
    assertEquals( fileService.doGetDefaultLocation( "/home/suzy"), ClientRepositoryPaths.getPublicFolderPath() );
  }

  @Test
  public void testGetDefaultLocation_Scenario_DefaultFolderIsNotAccessible() throws Exception {
    RepositoryFileDto hiddenDto = new RepositoryFileDto();
    hiddenDto.setFolder( true );
    hiddenDto.setHidden( true );
    hiddenDto.setId( "5345345345345345345" );
    hiddenDto.setName( "suzy" );
    DefaultUnifiedRepositoryWebService repoWs = mock ( DefaultUnifiedRepositoryWebService.class );
    doReturn( repoWs ).when( fileService ).getRepoWs();
    doReturn( hiddenDto ).when( repoWs ).getFile( "/home/suzy" );
    when( PentahoSystem.get( ISystemConfig.class ).getProperty( eq( PentahoSystem.DEFAULT_FOLDER_WHEN_HOME_FOLDER_IS_HIDDEN_PROPERTY ) ) ).thenReturn( "/default-folder" );
    doReturn( null ).when( repoWs ).getFile( "/default-folder" );
    assertEquals( fileService.doGetDefaultLocation( "/home/suzy"), ClientRepositoryPaths.getRootFolderPath() );
  }

  private static String encode( String pathControlCharacter ) throws UnsupportedEncodingException {
    return URLEncoder.encode( pathControlCharacter, UTF_8 );
  }
}
