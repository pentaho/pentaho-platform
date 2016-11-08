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
package org.pentaho.platform.plugin.services.importer;

import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.reporting.libraries.docbundle.DocumentMetaData;
import org.pentaho.reporting.libraries.docbundle.ODFMetaAttributeNames;

public class PRPTImportHandlerTest {

  private static final String SAMPLE_USER_PATH = "/home/user/";

  private static final String SAMPLE_SLASH_PATH = "/";

  private static final String SAMPLE_BACKSLASH_PATH = "\\";

  private static final String SAMPLE_STREAM = "stream";

  private static final String SAMPLE_NAME = "name";

  private IPentahoObjectFactory pentahoObjectFactory;

  private RepositoryFileImportBundle bundle;

  private PRPTImportHandler handler;

  private static final IPlatformImporter importer = mock( IPlatformImporter.class );

  @Before
  public void setUp() throws ObjectFactoryException, IOException {
    final IPlatformMimeResolver resolver = mock( IPlatformMimeResolver.class );

    pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    when( pentahoObjectFactory.objectDefined( anyString() ) ).thenReturn( true );
    when( pentahoObjectFactory.get( eq( IPlatformMimeResolver.class ), anyString(), any( IPentahoSession.class ) ) )
        .thenAnswer( new Answer<Object>() {
          @Override
          public Object answer( InvocationOnMock invocation ) throws Throwable {
            return resolver;
          }
        } );
    when( pentahoObjectFactory.get( eq( IPlatformImporter.class ), anyString(), any( IPentahoSession.class ) ) )
      .thenAnswer( new Answer<Object>() {
        @Override
        public Object answer( InvocationOnMock invocation ) throws Throwable {
          return importer;
        }
      } );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );

    InputStream stream = new ByteArrayInputStream( SAMPLE_STREAM.getBytes() );
    bundle = mock( RepositoryFileImportBundle.class );
    when( bundle.getName() ).thenReturn( SAMPLE_NAME );
    when( bundle.getInputStream() ).thenReturn( stream );

    Log logger = mock( Log.class );
    ImportSession session = mock( ImportSession.class );
    doReturn( logger ).when( session ).getLogger();
    IUnifiedRepository repository = mock( IUnifiedRepository.class );

    List<IMimeType> mimeTypes = Arrays.asList( mock( IMimeType.class ) );
    handler = spy( new PRPTImportHandler( mimeTypes ) );
    doReturn( session ).when( handler ).getImportSession();
    handler.setRepository( repository );
  }

  @Test
  public void testImportFile_validPath() throws PlatformImportException, IOException {
    when( bundle.getPath() ).thenReturn( SAMPLE_USER_PATH );
    DocumentMetaData metadata = mock( DocumentMetaData.class );
    doReturn( metadata ).when( handler ).extractMetaData( any( byte[].class ) );
    handler.importFile( bundle );
  }

  @Test
  public void testImportFile_slashPath() throws PlatformImportException, IOException {
    when( bundle.getPath() ).thenReturn( SAMPLE_SLASH_PATH );
    DocumentMetaData metadata = mock( DocumentMetaData.class );
    doReturn( metadata ).when( handler ).extractMetaData( any( byte[].class ) );
    handler.importFile( bundle );
  }

  @Test
  public void testImportFile_backSlashPath() throws PlatformImportException, IOException {
    when( bundle.getPath() ).thenReturn( SAMPLE_BACKSLASH_PATH );
    DocumentMetaData metadata = mock( DocumentMetaData.class );
    doReturn( metadata ).when( handler ).extractMetaData( any( byte[].class ) );
    handler.importFile( bundle );
  }

  //we should import file if we have description
  @Test
  public void testImportFile_metadataReturnOnlyDescription() throws PlatformImportException, IOException {
    when( bundle.getPath() ).thenReturn( SAMPLE_BACKSLASH_PATH );
    DocumentMetaData metadata = mock( DocumentMetaData.class );
    when( metadata.getBundleAttribute( anyString(), eq( ODFMetaAttributeNames.DublinCore.DESCRIPTION ) ) )
        .thenReturn( ODFMetaAttributeNames.DublinCore.DESCRIPTION );
    doReturn( metadata ).when( handler ).extractMetaData( any( byte[].class ) );
    handler.importFile( bundle );
    verify( importer, atLeastOnce() ).importFile( any( IPlatformImportBundle.class ) );
  }

  //we should import file if we have title
  @Test
  public void testImportFile_metadataReturnOnlyTitle() throws PlatformImportException, IOException {
    when( bundle.getPath() ).thenReturn( SAMPLE_BACKSLASH_PATH );
    DocumentMetaData metadata = mock( DocumentMetaData.class );
    when( metadata.getBundleAttribute( anyString(), eq( ODFMetaAttributeNames.DublinCore.TITLE ) ) )
      .thenReturn( ODFMetaAttributeNames.DublinCore.TITLE );
    doReturn( metadata ).when( handler ).extractMetaData( any( byte[].class ) );
    handler.importFile( bundle );
    verify( importer, atLeastOnce() ).importFile( any( IPlatformImportBundle.class ) );
  }

  //we should import file if we have description and title
  @Test
  public void testImportFile_metadataIsCorrect() throws PlatformImportException, IOException {
    when( bundle.getPath() ).thenReturn( SAMPLE_BACKSLASH_PATH );
    DocumentMetaData metadata = mock( DocumentMetaData.class );
    when( metadata.getBundleAttribute( anyString(), eq( ODFMetaAttributeNames.DublinCore.DESCRIPTION ) ) )
      .thenReturn( ODFMetaAttributeNames.DublinCore.DESCRIPTION );
    when( metadata.getBundleAttribute( anyString(), eq( ODFMetaAttributeNames.DublinCore.TITLE ) ) )
      .thenReturn( ODFMetaAttributeNames.DublinCore.TITLE );
    doReturn( metadata ).when( handler ).extractMetaData( any( byte[].class ) );
    handler.importFile( bundle );
    verify( importer, atLeastOnce() ).importFile( any( IPlatformImportBundle.class ) );
  }

  @Test( expected = PlatformImportException.class )
  public void testImportFile_ErrorRecivingMetadata() throws PlatformImportException, IOException {
    when( bundle.getPath() ).thenReturn( SAMPLE_USER_PATH );
    handler.importFile( bundle );
  }

}
