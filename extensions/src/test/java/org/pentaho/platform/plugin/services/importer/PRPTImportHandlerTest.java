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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importer;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.reporting.libraries.docbundle.DocumentMetaData;
import org.pentaho.reporting.libraries.docbundle.ODFMetaAttributeNames;


@RunWith( MockitoJUnitRunner.class )
public class PRPTImportHandlerTest {

  private static final String SAMPLE_USER_PATH = "/home/user/";

  private static final String SAMPLE_SLASH_PATH = "/";

  private static final String SAMPLE_BACKSLASH_PATH = "\\";

  private static final String SAMPLE_STREAM = "stream";

  private static final String SAMPLE_NAME = "name";

  private static MockedStatic<PentahoSystem> pentahoSystemMock;

  private RepositoryFileImportBundle bundle;

  private PRPTImportHandler handler;

  @BeforeClass
  public static void beforeAll() {
    pentahoSystemMock = mockStatic( PentahoSystem.class );
    pentahoSystemMock.when( () -> PentahoSystem.get( IUnifiedRepository.class ) ).thenReturn( mock( IUnifiedRepository.class ) );
    pentahoSystemMock.when( () -> PentahoSystem.get( IPlatformMimeResolver.class ) ).thenReturn( mock( IPlatformMimeResolver.class ) );
  }

  @AfterClass
  public static void afterAll() {
    pentahoSystemMock.close();
  }

  @Before
  public void setUp() throws Exception {
    InputStream stream = new ByteArrayInputStream( SAMPLE_STREAM.getBytes() );
    bundle = mock( RepositoryFileImportBundle.class );
    when( bundle.getName() ).thenReturn( SAMPLE_NAME );
    when( bundle.getInputStream() ).thenReturn( stream );

    Log logger = mock( Log.class );
    ImportSession session = mock( ImportSession.class );
    doReturn( logger ).when( session ).getLogger();
    IUnifiedRepository repository = mock( IUnifiedRepository.class );

    List<IMimeType> mimeTypes = Collections.singletonList( mock( IMimeType.class ) );
    handler = spy( new PRPTImportHandler( mimeTypes ) );
    doReturn( session ).when( handler ).getImportSession();
    handler.setRepository( repository );
  }

  @Test
  public void testImportFile_validPath() throws Exception {
    when( bundle.getPath() ).thenReturn( SAMPLE_USER_PATH );
    DocumentMetaData metadata = mock( DocumentMetaData.class );
    doReturn( metadata ).when( handler ).extractMetaData( any( byte[].class ) );
    handler.importFile( bundle );
  }

  @Test
  public void testImportFile_slashPath() throws Exception {
    when( bundle.getPath() ).thenReturn( SAMPLE_SLASH_PATH );
    DocumentMetaData metadata = mock( DocumentMetaData.class );
    doReturn( metadata ).when( handler ).extractMetaData( any( byte[].class ) );
    handler.importFile( bundle );
  }

  @Test
  public void testImportFile_backSlashPath() throws Exception {
    when( bundle.getPath() ).thenReturn( SAMPLE_BACKSLASH_PATH );
    DocumentMetaData metadata = mock( DocumentMetaData.class );
    doReturn( metadata ).when( handler ).extractMetaData( any( byte[].class ) );
    handler.importFile( bundle );
  }

  //we should import file if we have description
  @Test
  public void testImportFile_metadataReturnOnlyDescription() throws Exception {
    IPlatformImporter importer = mock( IPlatformImporter.class );
    pentahoSystemMock.when( () -> PentahoSystem.get( IPlatformImporter.class ) ).thenReturn( importer );
    when( bundle.getPath() ).thenReturn( SAMPLE_BACKSLASH_PATH );
    DocumentMetaData metadata = mock( DocumentMetaData.class );
    when( metadata.getBundleAttribute( nullable( String.class ), eq( ODFMetaAttributeNames.DublinCore.DESCRIPTION ) ) )
        .thenReturn( ODFMetaAttributeNames.DublinCore.DESCRIPTION );
    doReturn( metadata ).when( handler ).extractMetaData( any( byte[].class ) );
    handler.importFile( bundle );
    verify( importer ).importFile( any( IPlatformImportBundle.class ) );
  }

  //we should import file if we have title
  @Test
  public void testImportFile_metadataReturnOnlyTitle() throws Exception {
    IPlatformImporter importer = mock( IPlatformImporter.class );
    pentahoSystemMock.when( () -> PentahoSystem.get( IPlatformImporter.class ) ).thenReturn( importer );
    when( bundle.getPath() ).thenReturn( SAMPLE_BACKSLASH_PATH );
    DocumentMetaData metadata = mock( DocumentMetaData.class );
    when( metadata.getBundleAttribute( nullable( String.class ), eq( ODFMetaAttributeNames.DublinCore.TITLE ) ) )
      .thenReturn( ODFMetaAttributeNames.DublinCore.TITLE );
    doReturn( metadata ).when( handler ).extractMetaData( any( byte[].class ) );
    handler.importFile( bundle );
    verify( importer ).importFile( any( IPlatformImportBundle.class ) );
  }

  //we should import file if we have description and title
  @Test
  public void testImportFile_metadataIsCorrect() throws Exception {
    IPlatformImporter importer = mock( IPlatformImporter.class );
    pentahoSystemMock.when( () -> PentahoSystem.get( IPlatformImporter.class ) ).thenReturn( importer );
    when( bundle.getPath() ).thenReturn( SAMPLE_BACKSLASH_PATH );
    DocumentMetaData metadata = mock( DocumentMetaData.class );
    when( metadata.getBundleAttribute( nullable( String.class ), eq( ODFMetaAttributeNames.DublinCore.DESCRIPTION ) ) )
      .thenReturn( ODFMetaAttributeNames.DublinCore.DESCRIPTION );
    when( metadata.getBundleAttribute( nullable( String.class ), eq( ODFMetaAttributeNames.DublinCore.TITLE ) ) )
      .thenReturn( ODFMetaAttributeNames.DublinCore.TITLE );
    doReturn( metadata ).when( handler ).extractMetaData( any( byte[].class ) );
    handler.importFile( bundle );
    verify( importer ).importFile( any( IPlatformImportBundle.class ) );
  }

  @Test( expected = PlatformImportException.class )
  public void testImportFile_ErrorReceivingMetadata() throws Exception {
    when( bundle.getPath() ).thenReturn( SAMPLE_USER_PATH );
    handler.importFile( bundle );
  }

}
