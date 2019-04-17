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
 * Copyright (c) 2002-2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mimetype.MimeType;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.Log4JRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle;
import org.pentaho.platform.util.XmlTestConstants;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith( PowerMockRunner.class )
@PrepareForTest( PentahoSystem.class )
public class LocaleImportHandlerTest {

  private static final String DEFAULT_ENCODING = "UTF-8";

  PentahoPlatformImporter importer;
  LocaleFilesProcessor localeFilesProcessor;
  LocaleImportHandler localeImportHandler;

  @Before
  public void setUp() throws Exception {

    NameBaseMimeResolver nameResolver = new NameBaseMimeResolver();
    PentahoSystem.registerObject( nameResolver );

    IRepositoryContentConverterHandler converterHandler =
        new DefaultRepositoryContentConverterHandler( new HashMap<String, Converter>() );

    List<IMimeType> localeMimeList = new ArrayList<IMimeType>();
    localeMimeList.add( new MimeType( "text/locale", "locale" ) );

    nameResolver.addMimeType( new MimeType( "text/prptMimeType", "prpt" ) );
    nameResolver.addMimeType( new MimeType( "text/xactionMimeType", "xaction" ) );

    MimeType mimeType = new MimeType( "text/xml", "xml" );
    mimeType.setHidden( true );
    nameResolver.addMimeType( mimeType );

    mimeType = new MimeType( "image/png", "png" );
    mimeType.setHidden( true );
    nameResolver.addMimeType( mimeType );

    List<String> allowedArtifacts = new ArrayList<String>();
    allowedArtifacts.add( "xaction" );
    allowedArtifacts.add( "url" );
    allowedArtifacts.add( "properties" );

    LocaleImportHandler localeImportHandler = new LocaleImportHandler( localeMimeList, allowedArtifacts );
    LocaleImportHandler spylocaleImportHandler = spy( localeImportHandler );
    Log log = mock( Log.class );
    doReturn( log ).when( spylocaleImportHandler ).getLogger();

    List<IPlatformImportHandler> handlers = new ArrayList<IPlatformImportHandler>();
    handlers.add( spylocaleImportHandler );

    importer = new PentahoPlatformImporter( handlers, converterHandler );
    importer.setRepositoryImportLogger( new Log4JRepositoryImportLogger() );

    localeFilesProcessor = new LocaleFilesProcessor();

    this.localeImportHandler = new LocaleImportHandler( localeMimeList, allowedArtifacts );
  }

  @Test
  public void testImportLocaleFiles() throws Exception {
    StringBuffer localeContent = new StringBuffer();
    localeContent.append( "name=Test" );
    localeContent.append( "\n" );
    localeContent.append( "description=Test description" );

    assertTrue( processIsLocalFile( "test.properties", localeContent ) );
    assertFalse( processIsLocalFile( "test.bla", localeContent ) );

    localeContent = new StringBuffer( "bla bla" );
    assertFalse( processIsLocalFile( "test.properties", localeContent ) );

    localeFilesProcessor.processLocaleFiles( importer );
  }

  @Test
  public void testImportDefaultPropertiesFiles() throws Exception {

    IUnifiedRepository mockUnifiedRepository = mock( IUnifiedRepository.class );
    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );
    setInternalState( localeImportHandler, "unifiedRepository", mockUnifiedRepository );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "someFile.properties" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "someFile.xaction" );
    when( mockUnifiedRepository.getFile( anyString() ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( anyInt() ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository, times( 1 ) ).getFile( anyString() );
    verify( mockUnifiedRepository, times( 1 ) ).getChildren( anyInt() );

    assertNotNull( localeParent );
  }

  @Test
  public void testImportLocalizedPropertiesFiles_fr() throws Exception {

    LocaleHelper.setLocale( new Locale( "fr" ) );

    IUnifiedRepository mockUnifiedRepository = mock( IUnifiedRepository.class );
    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );
    setInternalState( localeImportHandler, "unifiedRepository", mockUnifiedRepository );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "someFile_fr.properties" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "someFile.xaction" );
    when( mockUnifiedRepository.getFile( anyString() ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( anyInt() ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository, times( 1 ) ).getFile( anyString() );
    verify( mockUnifiedRepository, times( 1 ) ).getChildren( anyInt() );

    assertNotNull( localeParent );
  }

  @Test
  public void testImportLocalizedPropertiesFiles_en_us() throws Exception {

    LocaleHelper.setLocale( new Locale( "en_US" ) );

    IUnifiedRepository mockUnifiedRepository = mock( IUnifiedRepository.class );
    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );
    setInternalState( localeImportHandler, "unifiedRepository", mockUnifiedRepository );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "someFile_en_US.properties" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "someFile.xaction" );
    when( mockUnifiedRepository.getFile( anyString() ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( anyInt() ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository, times( 1 ) ).getFile( anyString() );
    verify( mockUnifiedRepository, times( 1 ) ).getChildren( anyInt() );

    assertNotNull( localeParent );
  }

  @Test
  public void testImportLocalizedPropertiesFiles_en_gb() throws Exception {

    LocaleHelper.setLocale( new Locale( "en_GB" ) );

    IUnifiedRepository mockUnifiedRepository = mock( IUnifiedRepository.class );
    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );
    setInternalState( localeImportHandler, "unifiedRepository", mockUnifiedRepository );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "someFile_en_GB.properties" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "someFile.xaction" );
    when( mockUnifiedRepository.getFile( anyString() ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( anyInt() ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository, times( 1 ) ).getFile( anyString() );
    verify( mockUnifiedRepository, times( 1 ) ).getChildren( anyInt() );

    assertNotNull( localeParent );
  }

  @Test
  public void testImportDefaultWithFileExtensionPropertiesFiles() throws Exception {

    IUnifiedRepository mockUnifiedRepository = mock( IUnifiedRepository.class );
    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );
    setInternalState( localeImportHandler, "unifiedRepository", mockUnifiedRepository );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "someFile.xaction.properties" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "someFile.xaction" );
    when( mockUnifiedRepository.getFile( anyString() ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( anyInt() ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository, times( 1 ) ).getFile( anyString() );
    verify( mockUnifiedRepository, times( 1 ) ).getChildren( anyInt() );

    assertNotNull( localeParent );
  }

  @Test
  public void testImportLocalizedWithFileExtensionPropertiesFiles_fr() throws Exception {

    IUnifiedRepository mockUnifiedRepository = mock( IUnifiedRepository.class );
    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );
    setInternalState( localeImportHandler, "unifiedRepository", mockUnifiedRepository );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "some_File.xaction_fr.locale" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "some_File.xaction" );
    when( mockUnifiedRepository.getFile( anyString() ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( anyInt() ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository, times( 1 ) ).getFile( anyString() );
    verify( mockUnifiedRepository, times( 1 ) ).getChildren( anyInt() );

    assertNotNull( localeParent );
  }

  @Test
  public void testImportLocalizedWithFileExtensionPropertiesFiles_en_us() throws Exception {

    IUnifiedRepository mockUnifiedRepository = mock( IUnifiedRepository.class );
    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );
    setInternalState( localeImportHandler, "unifiedRepository", mockUnifiedRepository );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "someFile.xaction_en_US.locale" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "someFile.xaction" );
    when( mockUnifiedRepository.getFile( anyString() ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( anyInt() ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository, times( 1 ) ).getFile( anyString() );
    verify( mockUnifiedRepository, times( 1 ) ).getChildren( anyInt() );

    assertNotNull( localeParent );
  }

  @Test
  public void testImportLocalizedWithFileExtensionPropertiesFiles_en_gb() throws Exception {

    LocaleHelper.setLocale( new Locale( "en_GB" ) );

    IUnifiedRepository mockUnifiedRepository = mock( IUnifiedRepository.class );
    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );
    setInternalState( localeImportHandler, "unifiedRepository", mockUnifiedRepository );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "someFile.xaction_en_GB.properties" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "someFile.xaction" );
    when( mockUnifiedRepository.getFile( anyString() ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( anyInt() ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository, times( 1 ) ).getFile( anyString() );
    verify( mockUnifiedRepository, times( 1 ) ).getChildren( anyInt() );

    assertNotNull( localeParent );
  }

  @Test
  public void testValidImportIndexLocaleFile() {
    String localeContent =
        "<index><name>My name</name><description>My descript</description><icon>samples.png</icon><visible>true</visible><display-type>icons</display-type></index>";
    RepositoryFileImportBundle importBundle = createBundle( localeContent, "index.xml" );

    IUnifiedRepository unifiedRepository = initLocaleHandler( importBundle );

    try {
      importer.importFile( importBundle );

      verify( unifiedRepository, times( 1 ) ).getFile( anyString() );
      verify( unifiedRepository, never() ).getChildren( anyInt() );
      verify( unifiedRepository, times( 1 ) ).setLocalePropertiesForFile( any( RepositoryFile.class ), anyString(),
          any( Properties.class ) );
    } catch ( PlatformImportException e ) {
      fail( e.getMessage() );
    }

  }

  @Test
  public void testInValidImportIndexLocaleFile() {
    String localeContent =
        "<index><name>%name</name><description>%description</description><icon>samples.png</icon><visible>true</visible><display-type>icons</display-type></index>";
    RepositoryFileImportBundle importBundle = createBundle( localeContent, "index.xml" );

    IUnifiedRepository unifiedRepository = initLocaleHandler( importBundle );

    try {
      importer.importFile( importBundle );

      verify( unifiedRepository, times( 1 ) ).getFile( anyString() );
      verify( unifiedRepository, times( 1 ) ).getChildren( anyInt() );
      verify( unifiedRepository, never() ).setLocalePropertiesForFile( any( RepositoryFile.class ), anyString(),
          any( Properties.class ) );
    } catch ( PlatformImportException e ) {
      fail( e.getMessage() );
    }

  }

  @Test
  public void testImportNotLocaleFile() {
    String localeContent = "<index></display-type></index>";
    RepositoryFileImportBundle importBundle = createBundle( localeContent, "test.xml" );

    IUnifiedRepository unifiedRepository = initLocaleHandler( importBundle );

    try {
      importer.importFile( importBundle );

      verify( unifiedRepository, times( 1 ) ).getFile( anyString() );
      verify( unifiedRepository, times( 1 ) ).getChildren( anyInt() );
      verify( unifiedRepository, never() ).setLocalePropertiesForFile( any( RepositoryFile.class ), anyString(),
          any( Properties.class ) );
    } catch ( PlatformImportException e ) {
      fail( e.getMessage() );
    }

  }

  private IUnifiedRepository initLocaleHandler( RepositoryFileImportBundle importBundle ) {
    IUnifiedRepository unifiedRepository = mock( IUnifiedRepository.class );
    when( unifiedRepository.getFile( importBundle.getPath() ) ).thenReturn( importBundle.getFile() );
    LocaleImportHandler localeHandler = (LocaleImportHandler) importer.getHandlers().get( importBundle.getMimeType() );
    ReflectionTestUtils.setField( localeHandler, "unifiedRepository", unifiedRepository );
    return unifiedRepository;
  }

  private RepositoryFileImportBundle createBundle( String localeContent, String fileName ) {
    InputStream in = new ByteArrayInputStream( localeContent.getBytes() );

    RepositoryFile repoFile = new RepositoryFile.Builder( fileName ).build();

    RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
    bundleBuilder.path( "/pentaho-solutions/my-test/" + fileName );
    bundleBuilder.mime( "text/locale" );
    bundleBuilder.input( in );
    bundleBuilder.charSet( DEFAULT_ENCODING );
    bundleBuilder.overwriteFile( true );
    bundleBuilder.applyAclSettings( true );
    bundleBuilder.overwriteAclSettings( true );
    bundleBuilder.retainOwnership( false );
    bundleBuilder.name( fileName );
    bundleBuilder.file( repoFile );

    RepositoryFileImportBundle importBundle = bundleBuilder.build();
    return importBundle;
  }

  private boolean processIsLocalFile( String fileName, StringBuffer localeContent ) throws Exception {
    File tmpFile = new File( "test" );
    tmpFile.createNewFile();
    tmpFile.deleteOnExit();

    RepositoryFile file = new RepositoryFile.Builder( fileName ).build();
    RepositoryFileBundle repoFileBundle =
        new RepositoryFileBundle( file, null, StringUtils.EMPTY, tmpFile, DEFAULT_ENCODING, null );
    return localeFilesProcessor.isLocaleFile( repoFileBundle, "/", localeContent.toString().getBytes() );
  }

  @Test( timeout = 2000, expected = SAXException.class )
  public void shouldNotFailAndReturnNullWhenMaliciousXmlIsGiven() throws IOException, ParserConfigurationException, SAXException {
    LocaleImportHandler lih = new LocaleImportHandler( Collections.emptyList(), null );

    lih.getLocalBundleDocument( new StringBufferInputStream( XmlTestConstants.MALICIOUS_XML ) );
    fail();
  }

  @Test
  public void shouldNotFailAndReturnNotNullWhenLegalXmlIsGiven() throws Exception {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<slave_config>"
      + "</slave_config>";
    LocaleImportHandler lih = new LocaleImportHandler( Collections.emptyList(), null );

    assertNotNull( lih.getLocalBundleDocument( new StringBufferInputStream( xml ) ) );
  }

  @Test
  public void loadPropertiesByXmlTest() throws Exception {
    RepositoryFileImportBundle repFileBundleMock = mock( RepositoryFileImportBundle.class );
    RepositoryFile repFileMock = mock( RepositoryFile.class );
    when( repFileBundleMock.getFile() ).thenReturn( repFileMock );
    when( repFileMock.getName() ).thenReturn( "index.xml" );
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<index>"
      + "<name>the name</name>"
      + "<description>the description</description>"
      + "</index>";
    when( repFileBundleMock.getInputStream() ).thenReturn( new ByteArrayInputStream( xml.getBytes() ) );
    assertEquals( localeImportHandler.loadPropertiesByXml( repFileBundleMock ).size(), 2 );
  }

  @Test
  public void loadPropertiesByXmlReferenceToVariableInNameTest() throws Exception {
    RepositoryFileImportBundle repFileBundleMock = mock( RepositoryFileImportBundle.class );
    RepositoryFile repFileMock = mock( RepositoryFile.class );
    when( repFileBundleMock.getFile() ).thenReturn( repFileMock );
    when( repFileMock.getName() ).thenReturn( "index.xml" );
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<index>"
      + "<name>%name</name>"
      + "<description>the description</description>"
      + "</index>";
    when( repFileBundleMock.getInputStream() ).thenReturn( new ByteArrayInputStream( xml.getBytes() ) );
    assertEquals( localeImportHandler.loadPropertiesByXml( repFileBundleMock ).size(), 1 );
  }

  @Test
  public void loadPropertiesByXmlReferenceToVariableInDescriptionTest() throws Exception {
    RepositoryFileImportBundle repFileBundleMock = mock( RepositoryFileImportBundle.class );
    RepositoryFile repFileMock = mock( RepositoryFile.class );
    when( repFileBundleMock.getFile() ).thenReturn( repFileMock );
    when( repFileMock.getName() ).thenReturn( "index.xml" );
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<index>"
      + "<name>the name</name>"
      + "<description>%description</description>"
      + "</index>";
    when( repFileBundleMock.getInputStream() ).thenReturn( new ByteArrayInputStream( xml.getBytes() ) );
    assertEquals( localeImportHandler.loadPropertiesByXml( repFileBundleMock ).size(), 1 );
  }

  @Test
  public void loadPropertiesByXmlReferenceToVariablesTest() throws Exception {
    RepositoryFileImportBundle repFileBundleMock = mock( RepositoryFileImportBundle.class );
    RepositoryFile repFileMock = mock( RepositoryFile.class );
    when( repFileBundleMock.getFile() ).thenReturn( repFileMock );
    when( repFileMock.getName() ).thenReturn( "index.xml" );
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<index>"
      + "<name>%name</name>"
      + "<description>%description</description>"
      + "</index>";
    when( repFileBundleMock.getInputStream() ).thenReturn( new ByteArrayInputStream( xml.getBytes() ) );
    assertEquals( localeImportHandler.loadPropertiesByXml( repFileBundleMock ).size(), 0 );
  }

  @Test
  public void loadPropertiesByXmlWrongFormatTest() throws Exception {
    RepositoryFileImportBundle repFileBundleMock = mock( RepositoryFileImportBundle.class );
    RepositoryFile repFileMock = mock( RepositoryFile.class );
    IPlatformImporter platformImporterMock = mock( IPlatformImporter.class );
    IRepositoryImportLogger logMock = mock( IRepositoryImportLogger.class );
    mockStatic( PentahoSystem.class );
    when( repFileBundleMock.getFile() ).thenReturn( repFileMock );
    when( repFileMock.getName() ).thenReturn( "index.xml" );
    when( PentahoSystem.get( IPlatformImporter.class ) ).thenReturn( platformImporterMock );
    when( platformImporterMock.getRepositoryImportLogger() ).thenReturn( logMock );
    when( logMock.hasLogger() ).thenReturn( true );
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<name>the name</name>"
      + "<description>the description</description>";
    when( repFileBundleMock.getInputStream() ).thenReturn( new ByteArrayInputStream( xml.getBytes() ) );
    assertEquals( localeImportHandler.loadPropertiesByXml( repFileBundleMock ).size(), 0 );
  }

  @Test
  public void loadPropertiesByXmlInputStreamExceptionTest() throws  Exception {
    RepositoryFileImportBundle repFileBundleMock = mock( RepositoryFileImportBundle.class );
    RepositoryFile repFileMock = mock( RepositoryFile.class );
    IPlatformImporter platformImporterMock = mock( IPlatformImporter.class );
    IRepositoryImportLogger logMock = mock( IRepositoryImportLogger.class );
    mockStatic( PentahoSystem.class );
    when( repFileBundleMock.getFile() ).thenReturn( repFileMock );
    when( repFileMock.getName() ).thenReturn( "index.xml" );
    when( PentahoSystem.get( IPlatformImporter.class ) ).thenReturn( platformImporterMock );
    when( platformImporterMock.getRepositoryImportLogger() ).thenReturn( logMock );
    when( logMock.hasLogger() ).thenReturn( true );
    /*String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<index>"
      + "<name></name>"
      + "<description></description>"
      + "</index>";*/
    when( repFileBundleMock.getInputStream() ).thenThrow( new IOException( "" ) );

    assertEquals( localeImportHandler.loadPropertiesByXml( repFileBundleMock ).size(), 0 );
  }

  @Test
  public void loadPropertiesByXmlWrongTypeTest() {
    RepositoryFileImportBundle repFileBundleMock = mock( RepositoryFileImportBundle.class );
    RepositoryFile repFileMock = mock( RepositoryFile.class );
    when( repFileBundleMock.getFile() ).thenReturn( repFileMock );
    when( repFileMock.getName() ).thenReturn( "fileName" );
    assertEquals( localeImportHandler.loadPropertiesByXml( repFileBundleMock ).size(), 0 );
  }
}
