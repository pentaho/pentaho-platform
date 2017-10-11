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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid.Type;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.core.mimetype.MimeType;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.test.platform.engine.core.MicroPlatform;

public class ZipExportProcessorTest {

  private static final Locale LOCALE_DEFAULT = Locale.ENGLISH;

  private final MimeType MIME_PRPT = new MimeType( "text/prptMimeType", "prpt" );

  private static File tempDir;

  private DefaultExportHandler exportHandler;

  private IUnifiedRepository repo;
  private MicroPlatform microPlatform;
  private StandaloneSession exportSession;
  private Converter defaultConverter;

  @BeforeClass
  public static void beforeClass() throws IOException {
    tempDir = File.createTempFile( "test", null );
    tempDir.delete();
    tempDir.mkdir();
    FileUtils.forceDeleteOnExit( tempDir );
  }

  @AfterClass
  public static void afterClass() throws IOException {
    FileUtils.forceDelete( tempDir );
  }

  @Before
  public void init() throws IOException, PlatformInitializationException, DomainIdNullException,
    DomainAlreadyExistsException, DomainStorageException {

    List<Locale> availableLocales = java.util.Collections.singletonList( LOCALE_DEFAULT );
    Properties localePropertries = new Properties();
    localePropertries.setProperty( "name1", "value1" );

    final RepositoryFile file0 = new RepositoryFile.Builder( "" ).path( "/" ).id( "/" ).folder( true ).build();
    final RepositoryFile file1 =
        new RepositoryFile.Builder( "home" ).path( "/home/" ).id( "/home/" ).folder( true ).build();
    final RepositoryFile file2 =
        new RepositoryFile.Builder( "test user" ).path( "/home/test user/" ).id( "/home/test user/" ).folder( true )
            .build();
    final RepositoryFile file3 =
        new RepositoryFile.Builder( "two words" ).path( "/home/test user/two words/" )
            .id( "/home/test user/two words/" ).folder( true ).build();
    final RepositoryFile fileX =
        new RepositoryFile.Builder( "eval (+)%.prpt" ).path( "/home/test user/two words/eval (+)%.prpt" ).id(
            "/home/test user/two words/eval (+)%.prpt" ).folder( false ).build();
    final RepositoryFile[] repoFiles = new RepositoryFile[] { file0, file1, file2, file3, fileX };
    final Map<Serializable, RepositoryFile> repoFilesMap = new HashMap<Serializable, RepositoryFile>();
    for ( RepositoryFile f : repoFiles ) {
      repoFilesMap.put( f.getId(), f );
    }

    repo = mock( IUnifiedRepository.class );
    final Answer<RepositoryFile> answerRepoGetFile = new Answer<RepositoryFile>() {
      @Override
      public RepositoryFile answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        final Object fileId = args[0];
        return getRepoFile( repoFilesMap, fileId );
      }

    };
    Mockito.doAnswer( answerRepoGetFile ).when( repo ).getFile( anyString() );
    Mockito.doAnswer( answerRepoGetFile ).when( repo ).getFile( anyString(), Mockito.anyBoolean() );
    Mockito.doAnswer( answerRepoGetFile ).when( repo ).getFile( anyString(), Mockito.anyBoolean(),
        any( IPentahoLocale.class ) );
    Mockito.doAnswer( answerRepoGetFile ).when( repo ).getFile( anyString(), any( IPentahoLocale.class ) );
    Mockito.doAnswer( answerRepoGetFile ).when( repo ).getFileById( any( Serializable.class ) );
    Mockito.doAnswer( answerRepoGetFile ).when( repo ).getFileById( any( Serializable.class ), Mockito.anyBoolean() );
    Mockito.doAnswer( answerRepoGetFile ).when( repo ).getFileById( any( Serializable.class ), Mockito.anyBoolean(),
        any( IPentahoLocale.class ) );
    Mockito.doAnswer( answerRepoGetFile ).when( repo ).getFileById( any( Serializable.class ),
        any( IPentahoLocale.class ) );

    Answer<List<RepositoryFile>> answerRepoGetChildren = new Answer<List<RepositoryFile>>() {

      @Override
      public List<RepositoryFile> answer( InvocationOnMock invocation ) throws Throwable {
        // returns the following item from <repoFiles>
        RepositoryRequest r = (RepositoryRequest) invocation.getArguments()[0];
        String path = r.getPath();
        RepositoryFile thisFile = getRepoFile( repoFilesMap, path );
        if ( thisFile == null || !thisFile.isFolder() ) {
          return Collections.emptyList();
        }
        for ( int i = 0, n = repoFiles.length - 1; i < n; i++ ) {
          RepositoryFile iFile = repoFiles[i];
          if ( iFile == thisFile || iFile.getId().equals( thisFile.getId() ) ) {
            return Collections.singletonList( repoFiles[i + 1] );
          }
        }
        return Collections.emptyList();
      }

    };
    Mockito.doAnswer( answerRepoGetChildren ).when( repo ).getChildren( any( RepositoryRequest.class ) );
    doReturn( availableLocales ).when( repo ).getAvailableLocalesForFile( Mockito.any( RepositoryFile.class ) );
    doReturn( availableLocales ).when( repo ).getAvailableLocalesForFileById( Mockito.any( Serializable.class ) );
    doReturn( availableLocales ).when( repo ).getAvailableLocalesForFileByPath( Mockito.any( String.class ) );

    doReturn( localePropertries ).when( repo ).getLocalePropertiesForFileById( Mockito.any( File.class ),
        Mockito.anyString() );

    RepositoryFileSid sid = mock( RepositoryFileSid.class );
    doReturn( "testUser" ).when( sid ).getName();
    doReturn( Type.USER ).when( sid ).getType();
    final RepositoryFileAcl mockAcl = mock( RepositoryFileAcl.class );
    doReturn( sid ).when( mockAcl ).getOwner();
    doReturn( mockAcl ).when( repo ).getAcl( any( Serializable.class ) );

    Answer<IRepositoryFileData> answerGetDataForRead = new Answer<IRepositoryFileData>() {
      @Override
      public IRepositoryFileData answer( InvocationOnMock invocation ) throws Throwable {
        Serializable id = (Serializable) invocation.getArguments()[0];
        RepositoryFile file = getRepoFile( repoFilesMap, id );
        if ( !file.isFolder() ) {
          return new SimpleRepositoryFileData( new ByteArrayInputStream( new byte[0] ), "UTF-8", MIME_PRPT.getName() );
        }
        return null;
      }
    };
    doAnswer( answerGetDataForRead ).when( repo ).getDataForRead( Mockito.any( Serializable.class ),
        Mockito.any( Class.class ) );

    exportHandler = new DefaultExportHandler();
    defaultConverter = new StreamConverter( repo );
    PentahoSystem.clearObjectFactory();
    microPlatform = new MicroPlatform( getSolutionPath() );
    microPlatform.defineInstance( IUnifiedRepository.class, repo );
    // microPlatform.defineInstance( IPlatformMimeResolver.class, mimeResolver );
    microPlatform.defineInstance( ISolutionEngine.class, Mockito.mock( SolutionEngine.class ) );
    microPlatform.defineInstance( IDatasourceMgmtService.class, Mockito.mock( IDatasourceMgmtService.class ) );

    microPlatform.start();

    exportSession = new StandaloneSession();
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );
    PentahoSessionHolder.setSession( exportSession );
  }

  private String getSolutionPath() {
    return tempDir.getPath();
  }

  @After
  public void cleanup() throws Exception {
    PentahoSessionHolder.removeSession();
    if ( microPlatform != null ) {
      microPlatform.stop();
    }
  }

  private RepositoryFile getRepoFile( final Map<Serializable, RepositoryFile> repoFilesMap, final Object fileId ) {
    RepositoryFile result = repoFilesMap.get( fileId );
    if ( result == null && fileId instanceof String ) {
      String sFileId = (String) fileId;
      if ( sFileId.endsWith( "/" ) || sFileId.endsWith( "\\" ) ) {
        sFileId = sFileId.substring( 0, sFileId.length() - 1 );
      } else {
        sFileId = sFileId + "/";
      }
      result = repoFilesMap.get( sFileId );
      if ( !result.isFolder() ) {
        result = null;
      }
    }
    return result;
  }

  @Test
  public void testGetFixedZipEntryName() {
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    final String fileName = "eval (+)%.prpt";
    final String filePath = "/home/test user/two words/eval (+)%.prpt";
    String processorBasePath = "/home/test user";
    ZipExportProcessor zipMF = new ZipExportProcessor( processorBasePath, repo, true );
    ZipExportProcessor zipNoMF = new ZipExportProcessor( processorBasePath, repo, false );

    String basePath = "/home/test user";
    String simpleZipEntryName = "two words/eval (+)%.prpt";
    String encodedZipEntryName = "two+words/eval+%28%2B%29%25.prpt";

    RepositoryFile file = null;

    file = new RepositoryFile.Builder( fileName ).path( filePath ).folder( false ).build();
    assertEquals( simpleZipEntryName, zipNoMF.getFixedZipEntryName( file, basePath ) );
    assertEquals( encodedZipEntryName, zipMF.getFixedZipEntryName( file, basePath ) );

    file = new RepositoryFile.Builder( fileName ).path( filePath ).folder( true ).build();
    assertEquals( simpleZipEntryName + "/", zipNoMF.getFixedZipEntryName( file, basePath ) );
    assertEquals( encodedZipEntryName + "/", zipMF.getFixedZipEntryName( file, basePath ) );

    basePath = "/";
    simpleZipEntryName = "home/test user/two words/eval (+)%.prpt";
    encodedZipEntryName = "home/test+user/two+words/eval+%28%2B%29%25.prpt";

    file = new RepositoryFile.Builder( fileName ).path( filePath ).folder( false ).build();
    assertEquals( simpleZipEntryName, zipNoMF.getFixedZipEntryName( file, basePath ) );
    assertEquals( encodedZipEntryName, zipMF.getFixedZipEntryName( file, basePath ) );

    file = new RepositoryFile.Builder( fileName ).path( filePath ).folder( true ).build();
    assertEquals( simpleZipEntryName + "/", zipNoMF.getFixedZipEntryName( file, basePath ) );
    assertEquals( encodedZipEntryName + "/", zipMF.getFixedZipEntryName( file, basePath ) );

    basePath = "";
    simpleZipEntryName = "home/test user/two words/eval (+)%.prpt";
    encodedZipEntryName = "home/test+user/two+words/eval+%28%2B%29%25.prpt";

    file = new RepositoryFile.Builder( fileName ).path( filePath ).folder( false ).build();
    assertEquals( simpleZipEntryName, zipNoMF.getFixedZipEntryName( file, basePath ) );
    assertEquals( encodedZipEntryName, zipMF.getFixedZipEntryName( file, basePath ) );

    file = new RepositoryFile.Builder( fileName ).path( filePath ).folder( true ).build();
    assertEquals( simpleZipEntryName + "/", zipNoMF.getFixedZipEntryName( file, basePath ) );
    assertEquals( encodedZipEntryName + "/", zipMF.getFixedZipEntryName( file, basePath ) );
  }

  @Test
  public void testPerformExport_withoutManifest() throws Exception {
    String expFolderPath = "/home/test user/two words/";
    ZipExportProcessor zipNoMF = new ZipExportProcessor( expFolderPath, repo, false );

    exportHandler.setConverters( assignConverterForExt( defaultConverter, "prpt" ) );

    zipNoMF.addExportHandler( exportHandler );
    RepositoryFile expFolder = repo.getFile( expFolderPath );
    assertNotNull( expFolder );

    File result = zipNoMF.performExport( repo.getFile( expFolderPath ) );

    Set<String> zipEntriesFiles = extractZipEntries( result );
    final String[] expectedEntries =
        new String[] { "two words/eval (+)%.prpt", "two words/eval (+)%.prpt_en.locale", "two words/index_en.locale" };
    for ( String e : expectedEntries ) {
      assertTrue( "expected entry: [" + e + "]", zipEntriesFiles.contains( e ) );
    }
    assertEquals( "entries count", expectedEntries.length, zipEntriesFiles.size() );
  }

  @Test
  public void testPerformExport_withManifest() throws Exception {
    String expFolderPath = "/home/test user/two words/";
    ZipExportProcessor zipMF = new ZipExportProcessor( expFolderPath, repo, true );

    exportHandler.setConverters( assignConverterForExt( defaultConverter, "prpt" ) );
    zipMF.addExportHandler( exportHandler );

    RepositoryFile expFolder = repo.getFile( expFolderPath );
    assertNotNull( expFolder );

    File result = zipMF.performExport( repo.getFile( expFolderPath ) );

    Set<String> zipEntriesFiles = extractZipEntries( result );
    final String[] expectedEntries =
        new String[] { "two+words/eval+%28%2B%29%25.prpt", "two+words/eval+%28%2B%29%25.prpt_en.locale",
          "two+words/index_en.locale", "exportManifest.xml" };
    for ( String e : expectedEntries ) {
      assertTrue( "expected entry: [" + e + "]", zipEntriesFiles.contains( e ) );
    }
    assertEquals( "entries count", expectedEntries.length, zipEntriesFiles.size() );
  }

  private Map<String, Converter> assignConverterForExt( Converter conv, String... exts ) {
    final Map<String, Converter> converters = new HashMap<String, Converter>();
    for ( String ext : exts ) {
      converters.put( ext, conv );
    }
    return converters;
  }

  private Set<String> extractZipEntries( File zipFile ) throws IOException {
    Set<String> result = new HashSet<String>();
    FileInputStream fis = null;
    ZipInputStream zis = null;
    try {
      fis = new FileInputStream( zipFile );
      zis = new ZipInputStream( fis );
      for ( ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry() ) {
        if ( !entry.isDirectory() ) {
          final String entityName = entry.getName().replaceAll( "\\\\", "/" );
          result.add( entityName );
        }
      }
    } finally {
      if ( fis != null ) {
        fis.close();
      }
    }
    return result;
  }

}
