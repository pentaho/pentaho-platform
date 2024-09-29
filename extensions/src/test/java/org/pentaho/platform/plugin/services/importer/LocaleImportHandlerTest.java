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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class LocaleImportHandlerTest {

  private static final String DEFAULT_ENCODING = "UTF-8";
  private static final String FILE_BUNDLE_PATH = "/pentaho-solutions/my-test/";

  private static MockedStatic<PentahoSystem> pentahoSystemMock;
  private IUnifiedRepository mockUnifiedRepository;

  @BeforeClass
  public static void beforeAll() {
    pentahoSystemMock = mockStatic( PentahoSystem.class );
  }

  @AfterClass
  public static void afterAll() {
    pentahoSystemMock.close();
  }

  PentahoPlatformImporter importer;
  LocaleFilesProcessor localeFilesProcessor;
  LocaleImportHandler localeImportHandler;

  @Before
  public void setUp() throws Exception {
    mockUnifiedRepository = mock( IUnifiedRepository.class );
    pentahoSystemMock.when( () -> PentahoSystem.get( IUnifiedRepository.class ) ).thenReturn( mockUnifiedRepository );

    NameBaseMimeResolver nameResolver = new NameBaseMimeResolver();
    PentahoSystem.registerObject( nameResolver );

    IRepositoryContentConverterHandler converterHandler =
        new DefaultRepositoryContentConverterHandler( new HashMap<>() );

    List<IMimeType> localeMimeList = new ArrayList<>();
    localeMimeList.add( new MimeType( "text/locale", "locale" ) );

    nameResolver.addMimeType( new MimeType( "text/prptMimeType", "prpt" ) );
    nameResolver.addMimeType( new MimeType( "text/xactionMimeType", "xaction" ) );

    MimeType mimeType = new MimeType( "text/xml", "xml" );
    mimeType.setHidden( true );
    nameResolver.addMimeType( mimeType );

    mimeType = new MimeType( "image/png", "png" );
    mimeType.setHidden( true );
    nameResolver.addMimeType( mimeType );

    pentahoSystemMock.when( () -> PentahoSystem.get( IPlatformMimeResolver.class ) ).thenReturn( nameResolver );

    List<String> allowedArtifacts = new ArrayList<>();
    allowedArtifacts.add( "xaction" );
    allowedArtifacts.add( "url" );
    allowedArtifacts.add( "properties" );

    LocaleImportHandler localeImportHandler = new LocaleImportHandler( localeMimeList, allowedArtifacts );
    LocaleImportHandler spylocaleImportHandler = spy( localeImportHandler );
    Log log = mock( Log.class );
    doReturn( log ).when( spylocaleImportHandler ).getLogger();

    List<IPlatformImportHandler> handlers = new ArrayList<>();
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
  public void testImportDefaultPropertiesFiles() {

    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "someFile.properties" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "someFile.xaction" );
    when( mockUnifiedRepository.getFile( nullable( String.class ) ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( nullable( Integer.class ) ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository ).getFile( nullable( String.class ) );
    verify( mockUnifiedRepository ).getChildren( nullable( Integer.class ) );

    assertNotNull( localeParent );
  }

  @Test
  public void testImportLocalizedPropertiesFiles_fr() {

    LocaleHelper.setThreadLocaleBase( new Locale( "fr" ) );

    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "someFile_fr.properties" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "someFile.xaction" );
    when( mockUnifiedRepository.getFile( nullable( String.class ) ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( nullable( Integer.class ) ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository ).getFile( nullable( String.class ) );
    verify( mockUnifiedRepository ).getChildren( nullable( Integer.class ) );

    assertNotNull( localeParent );
  }

  @Test
  public void testImportLocalizedPropertiesFiles_en_us() {

    LocaleHelper.setThreadLocaleBase( new Locale( "en_US" ) );

    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "someFile_en_US.properties" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "someFile.xaction" );
    when( mockUnifiedRepository.getFile( nullable( String.class ) ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( nullable( Integer.class ) ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository ).getFile( nullable( String.class ) );
    verify( mockUnifiedRepository ).getChildren( nullable( Integer.class ) );

    assertNotNull( localeParent );
  }

  @Test
  public void testImportLocalizedPropertiesFiles_en_gb() {
    LocaleHelper.setThreadLocaleBase( new Locale( "en_GB" ) );

    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "someFile_en_GB.properties" );

    String propertiesContent =
      "description=Some Description\n"
        + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "someFile.xaction" );
    when( mockUnifiedRepository.getFile( nullable( String.class ) ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>();
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( nullable( Integer.class ) ) ).thenReturn( localeFolderChildren );

    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository ).getFile( nullable( String.class ) );
    verify( mockUnifiedRepository ).getChildren( nullable( Integer.class ) );

    assertNotNull( localeParent );
  }

  @Test
  public void testImportDefaultWithFileExtensionPropertiesFiles() {

    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "someFile.xaction.properties" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "someFile.xaction" );
    when( mockUnifiedRepository.getFile( nullable( String.class ) ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( nullable( Integer.class ) ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository ).getFile( nullable( String.class ) );
    verify( mockUnifiedRepository ).getChildren( nullable( Integer.class ) );

    assertNotNull( localeParent );
  }

  @Test
  public void testImportLocalizedWithFileExtensionPropertiesFiles_fr() {

    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "some_File.xaction_fr.locale" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "some_File.xaction" );
    when( mockUnifiedRepository.getFile( nullable( String.class ) ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( nullable( Integer.class ) ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository ).getFile( nullable( String.class ) );
    verify( mockUnifiedRepository ).getChildren( nullable( Integer.class ) );

    assertNotNull( localeParent );
  }

  @Test
  public void testImportLocalizedWithFileExtensionPropertiesFiles_en_us() {

    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "someFile.xaction_en_US.locale" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "someFile.xaction" );
    when( mockUnifiedRepository.getFile( nullable( String.class ) ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( nullable( Integer.class ) ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository ).getFile( nullable( String.class ) );
    verify( mockUnifiedRepository ).getChildren( nullable( Integer.class ) );

    assertNotNull( localeParent );
  }

  @Test
  public void testImportLocalizedWithFileExtensionPropertiesFiles_en_gb() {

    LocaleHelper.setThreadLocaleBase( new Locale( "en_GB" ) );

    RepositoryFileImportBundle mockLocale = mock( RepositoryFileImportBundle.class );
    Properties mockProperties = mock( Properties.class );

    when( mockLocale.getName() ).thenReturn( "Some File Name" );
    when( mockLocale.getFile() ).thenReturn( mock( RepositoryFile.class ) );
    when( mockLocale.getFile().getName() ).thenReturn( "someFile.xaction_en_GB.properties" );

    String propertiesContent =
            "description=Some Description\n"
                    + "title=Some Title";
    RepositoryFileImportBundle importBundle = createBundle( propertiesContent, "someFile.xaction" );
    when( mockUnifiedRepository.getFile( nullable( String.class ) ) ).thenReturn( importBundle.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle.getFile() );
    when( mockUnifiedRepository.getChildren( nullable( Integer.class ) ) ).thenReturn( localeFolderChildren );


    RepositoryFile localeParent = localeImportHandler.getLocaleParent( mockLocale, mockProperties );

    verify( mockUnifiedRepository ).getFile( nullable( String.class ) );
    verify( mockUnifiedRepository ).getChildren( nullable( Integer.class ) );

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

      verify( unifiedRepository ).getFile( nullable( String.class ) );
      verify( unifiedRepository, never() ).getChildren( nullable( Integer.class ) );
      verify( unifiedRepository ).setLocalePropertiesForFile( any( RepositoryFile.class ), nullable( String.class ),
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

      verify( unifiedRepository ).getFile( nullable( String.class ) );
      verify( unifiedRepository ).getChildren( nullable( Integer.class ) );
      verify( unifiedRepository, never() ).setLocalePropertiesForFile( any( RepositoryFile.class ), nullable( String.class ),
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

      verify( unifiedRepository ).getFile( nullable( String.class ) );
      verify( unifiedRepository ).getChildren( nullable( Integer.class ) );
      verify( unifiedRepository, never() ).setLocalePropertiesForFile( any( RepositoryFile.class ), nullable( String.class ),
          any( Properties.class ) );
    } catch ( PlatformImportException e ) {
      fail( e.getMessage() );
    }

  }

  @Test
  public void testImportLocaleFolderChild() {
    Properties mockProperties = mock( Properties.class );

    String someFile1 = "someFile.xaction";
    String someFile2 = "someFile_rf.xaction";
    String someFile3 = "someFile_rf.prpt";
    String propertyFile = "someFile_rf.xaction.properties";

    String propertiesContent =
      "description=Some Description\n"
        + "title=Some Title";
    RepositoryFileImportBundle importBundle1 = createBundle( propertiesContent, someFile1 );
    when( mockUnifiedRepository.getFile( FILE_BUNDLE_PATH + someFile1 ) ).thenReturn( importBundle1.getFile() );
    RepositoryFileImportBundle importBundle2 = createBundle( propertiesContent, someFile2 );
    when( mockUnifiedRepository.getFile( FILE_BUNDLE_PATH + someFile2 ) ).thenReturn( importBundle2.getFile() );
    RepositoryFileImportBundle importBundle3 = createBundle( propertiesContent, someFile3 );
    when( mockUnifiedRepository.getFile( FILE_BUNDLE_PATH + someFile3 ) ).thenReturn( importBundle3.getFile() );
    RepositoryFileImportBundle importProperties = createBundle( propertiesContent, propertyFile );
    when( mockUnifiedRepository.getFile( FILE_BUNDLE_PATH + propertyFile ) ).thenReturn( importProperties.getFile() );

    List<RepositoryFile> localeFolderChildren = new ArrayList<>( );
    localeFolderChildren.add( importBundle1.getFile() );
    localeFolderChildren.add( importBundle2.getFile() );
    localeFolderChildren.add( importBundle3.getFile() );
    when( mockUnifiedRepository.getChildren( nullable( Integer.class ) ) ).thenReturn( localeFolderChildren );

    RepositoryFile localeParent1 = localeImportHandler.getLocaleParent( importBundle1, mockProperties );
    RepositoryFile localeParent2 = localeImportHandler.getLocaleParent( importBundle2, mockProperties );
    RepositoryFile localeParent3 = localeImportHandler.getLocaleParent( importBundle3, mockProperties );
    RepositoryFile localeParent4 = localeImportHandler.getLocaleParent( importProperties, mockProperties );

    verify( mockUnifiedRepository, times( 4 ) ).getFile( nullable( String.class ) );
    verify( mockUnifiedRepository, times( 4 ) ).getChildren( nullable( Integer.class ) );

    assertNotNull( localeParent1 );
    assertNotNull( localeParent2 );
    assertNotNull( localeParent3 );
    assertNotNull( localeParent4 );

    assertEquals( localeParent1.getName(), someFile1 );
    assertEquals( localeParent2.getName(), someFile2 );
    assertEquals( localeParent3.getName(), someFile3 );
    assertEquals( localeParent4.getName(), someFile2 );
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
    bundleBuilder.path( FILE_BUNDLE_PATH + fileName );
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
  public void loadPropertiesByXmlWrongFormatTest() {
    RepositoryFileImportBundle repFileBundleMock = mock( RepositoryFileImportBundle.class );
    RepositoryFile repFileMock = mock( RepositoryFile.class );
    IPlatformImporter platformImporterMock = mock( IPlatformImporter.class );
    when( repFileBundleMock.getFile() ).thenReturn( repFileMock );
    when( repFileMock.getName() ).thenReturn( "index.xml" );
    pentahoSystemMock.when( () -> PentahoSystem.get( IPlatformImporter.class ) ).thenReturn( platformImporterMock );
    assertEquals( localeImportHandler.loadPropertiesByXml( repFileBundleMock ).size(), 0 );
  }

  @Test
  public void loadPropertiesByXmlInputStreamExceptionTest() throws  Exception {
    RepositoryFileImportBundle repFileBundleMock = mock( RepositoryFileImportBundle.class );
    RepositoryFile repFileMock = mock( RepositoryFile.class );
    IPlatformImporter platformImporterMock = mock( IPlatformImporter.class );
    IRepositoryImportLogger logMock = mock( IRepositoryImportLogger.class );
    when( repFileBundleMock.getFile() ).thenReturn( repFileMock );
    when( repFileMock.getName() ).thenReturn( "index.xml" );
    pentahoSystemMock.when( () -> PentahoSystem.get( IPlatformImporter.class ) ).thenReturn( platformImporterMock );
    when( platformImporterMock.getRepositoryImportLogger() ).thenReturn( logMock );
    when( logMock.hasLogger() ).thenReturn( true );
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
