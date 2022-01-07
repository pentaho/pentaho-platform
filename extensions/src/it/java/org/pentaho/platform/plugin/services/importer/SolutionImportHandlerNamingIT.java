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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.plugin.services.importer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
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
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.core.mimetype.MimeType;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.importexport.Log4JRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.StreamConverter;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.utils.TestResourceLocation;

/**
 * 
 * Checks that SolutionImportHandler processes file names correctly
 *
 */
public class SolutionImportHandlerNamingIT extends Assert {

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/SolutionImportHandlerNamingTest";
  private static final String REPO_PATH = "/repo";
  private static final String IMPORT_REPO_DIR = "/home/testuser";

  private static final String DEFAULT_ENCODING = "UTF-8";

  private static final String SRC_ROOT = "/exported";
  private static final String SRC_EXPORTMANIFEST = SRC_ROOT + "/exportManifest.xml";
  private static final String SRC_CONTENT_FILE = SRC_ROOT + "/content.prpt";
  private static final String ZIPENTRY_EXPORTMANIFEST = "exportManifest.xml";
  private static final String ZIPENTRY_CONTENT_FILE = "two+words%2525/eval+%28%2B%29%2525.prpt";
  private static final String ZIPENTRY_CONTENT_FILE_NOMANIFEST = "two words%25/eval (+)%25.prpt";

  private final IMimeType MIME_SOLUTION = new MimeType( "application/vnd.pentaho.solution-repository", "zip" );
  private final IMimeType MIME_PRPT = new MimeType( "text/prptMimeType", "prpt" );
  private final IMimeType MIME_XML = new MimeType( "text/xml", "xml" );

  private static File tempDir;
  private static File solutionDir;

  private SolutionImportHandler solutionImportHandler;

  private File repoRoot;
  private FileSystemBackedUnifiedRepository repo;
  private MicroPlatform microPlatform;
  private AnswerTest<RepositoryFile> answer;

  // private PentahoPlatformImporter importer;

  @BeforeClass
  public static void beforeClass() throws IOException {
    tempDir = File.createTempFile( "test", null );
    tempDir.delete();
    tempDir.mkdir();
    FileUtils.forceDeleteOnExit( tempDir );

    solutionDir = new File( getSolutionPath() );
    assertTrue( solutionDir.exists() );
    assertTrue( solutionDir.isDirectory() );
  }

  @AfterClass
  public static void afterClass() throws IOException {
    FileUtils.forceDelete( tempDir );
  }

  @Before
  public void init() throws IOException, PlatformInitializationException, PlatformImportException,
    DomainIdNullException, DomainAlreadyExistsException, DomainStorageException {
    // repository
    File repoDir = new File( tempDir.getAbsolutePath() + REPO_PATH );
    FileUtils.forceMkdir( repoDir );
    FileUtils.cleanDirectory( repoDir );
    repoRoot = repoDir;
    repo = new FileSystemBackedUnifiedRepository();
    repo = Mockito.spy( repo );
    repo.setRootDir( repoRoot );

    // mimeResolver
    final Converter defaultConverter = new StreamConverter();

    final List<IMimeType> solutionMimeList = java.util.Collections.singletonList( MIME_SOLUTION );
    final List<IMimeType> contentMimeList = java.util.Arrays.asList( new IMimeType[] { MIME_PRPT, MIME_XML } );
    final List<IMimeType> allMimeTypes = new ArrayList<IMimeType>( solutionMimeList.size() + contentMimeList.size() );
    {
      allMimeTypes.addAll( solutionMimeList );
      allMimeTypes.addAll( contentMimeList );
      for ( IMimeType mimeType : allMimeTypes ) {
        mimeType.setConverter( defaultConverter );
      }
    }

    final IPlatformMimeResolver mimeResolver = new NameBaseMimeResolver();
    for ( IMimeType mimeType : allMimeTypes ) {
      mimeResolver.addMimeType( mimeType );
    }

    // platform, import handlers
    PentahoSystem.clearObjectFactory();
    microPlatform = new MicroPlatform( getSolutionPath() );
    microPlatform.defineInstance( IUnifiedRepository.class, repo );
    microPlatform.defineInstance( IPlatformMimeResolver.class, mimeResolver );
    microPlatform.defineInstance( ISolutionEngine.class, Mockito.mock( SolutionEngine.class ) );
    microPlatform.defineInstance( IDatasourceMgmtService.class, Mockito.mock( IDatasourceMgmtService.class ) );

    IRepositoryContentConverterHandler converterHandler =
        new DefaultRepositoryContentConverterHandler( new HashMap<String, Converter>() );

    RepositoryFileImportFileHandler contentImportFileHandler = new RepositoryFileImportFileHandler( contentMimeList );
    contentImportFileHandler.setRepository( repo );
    solutionImportHandler = new SolutionImportHandler( solutionMimeList );

    List<IPlatformImportHandler> handlers = new ArrayList<IPlatformImportHandler>();
    handlers.add( contentImportFileHandler );
    handlers.add( solutionImportHandler );

    PentahoPlatformImporter importer = new PentahoPlatformImporter( handlers, converterHandler );
    importer.setDefaultHandler( contentImportFileHandler );
    importer.setRepositoryImportLogger( new Log4JRepositoryImportLogger() );

    microPlatform.defineInstance( IPlatformImporter.class, importer );

    microPlatform.start();
  }

  @After
  public void cleanup() throws PluginBeanException, PlatformInitializationException, IOException {
    microPlatform.stop();
    FileUtils.cleanDirectory( repoRoot );
  }

  private static String getSolutionPath() {
    return SOLUTION_PATH;
  }

  private void assertFileContentEquals( File expected, RepositoryFile actual ) throws IOException {
    FileInputStream inExp = null;
    InputStream inAct = null;
    try {
      inExp = new FileInputStream( expected );
      try {
        inAct = repo.getDataForRead( actual.getId(), SimpleRepositoryFileData.class ).getInputStream();
        final String msg = "src{ " + expected.getName() + " } == repo{ " + actual.getName() + " }";
        assertTrue( msg, IOUtils.contentEquals( inExp, inAct ) );
      } finally {
        if ( inAct != null ) {
          inAct.close();
        }
      }
    } finally {
      if ( inExp != null ) {
        inExp.close();
      }
    }
  }

  /**
   * Import has to be launched in a separate thread because importSession is thread-local.
   * 
   * @param solutionImportHandler
   * @param bundle
   * @throws InterruptedException
   */
  private void runImport( final SolutionImportHandler solutionImportHandler, final IPlatformImportBundle bundle )
    throws InterruptedException {

    Thread thr = new Thread() {
      @Override
      public void run() {
        try {
          solutionImportHandler.importFile( bundle );
        } catch ( Error | RuntimeException e ) {
          e.printStackTrace();
          throw e;
        } catch ( Exception e ) {
          e.printStackTrace();
          throw new RuntimeException( e );
        }
      }
    };
    thr.start();
    thr.join( 0L );
  }

  @Test
  public void testImportFileWithoutManifest() throws PlatformImportException, DomainIdNullException,
    DomainAlreadyExistsException, DomainStorageException, IOException, PlatformInitializationException,
    InterruptedException {

    ByteArrayInputStream solutionInputStream = null;

    final File srcContentFile = new File( solutionDir + SRC_CONTENT_FILE );

    ByteArrayOutputStream tmpOS = null;
    try {
      tmpOS = new ByteArrayOutputStream();

      ZipOutputStream zipOS = new ZipOutputStream( tmpOS );

      zipOS.putNextEntry( new ZipEntry( ZIPENTRY_CONTENT_FILE_NOMANIFEST ) );
      FileUtils.copyFile( srcContentFile, zipOS );
      zipOS.closeEntry();

      zipOS.close();
      solutionInputStream = new ByteArrayInputStream( tmpOS.toByteArray() );
    } finally {
      if ( tmpOS != null ) {
        tmpOS.close();
      }
    }

    final IPlatformImportBundle bundle = buildBundle( solutionInputStream );

    runImport( solutionImportHandler, bundle );

    { // files with correct names do exist
      RepositoryFile dir = repo.getFile( IMPORT_REPO_DIR + "/two words%25" );
      assertNotNull( dir );
      assertTrue( dir.isFolder() );
      RepositoryFile file = repo.getFile( IMPORT_REPO_DIR + "/two words%25/eval (+)%25.prpt" );
      assertNotNull( file );
      assertFalse( file.isFolder() );
      assertFileContentEquals( srcContentFile, file );
    }
    { // files with incorrect names do not exist
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/two+words%2525" ) );
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/two+words%25" ) );
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/two+words%" ) );
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/two words%2525" ) );
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/two words%" ) );
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/two words%25/eval+%28%2B%29%2525.prpt" ) );
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/two words%25/eval (+)%.prpt" ) );
    }
  }

  @Test
  public void testImportFileWithManifest() throws PlatformImportException, DomainIdNullException,
    DomainAlreadyExistsException, DomainStorageException, IOException, PlatformInitializationException,
    InterruptedException {

    ZipBuilder zipBuilder = new ZipBuilder();
    zipBuilder.addFile( solutionDir + SRC_EXPORTMANIFEST, ZIPENTRY_EXPORTMANIFEST );
    zipBuilder.addFile( solutionDir + SRC_CONTENT_FILE, ZIPENTRY_CONTENT_FILE );
    ByteArrayOutputStream out = zipBuilder.build();
    ByteArrayInputStream solutionInputStream = new ByteArrayInputStream( out.toByteArray() );

    final IPlatformImportBundle bundle = buildBundle( solutionInputStream );

    runImport( solutionImportHandler, bundle );

    { // files with correct names do exist
      RepositoryFile dir = repo.getFile( IMPORT_REPO_DIR + "/two words%25" );
      assertNotNull( dir );
      assertTrue( dir.isFolder() );
      RepositoryFile file = repo.getFile( IMPORT_REPO_DIR + "/two words%25/eval (+)%25.prpt" );
      assertNotNull( file );
      assertFalse( file.isFolder() );
      assertFileContentEquals( new File( solutionDir + SRC_CONTENT_FILE ), file );
    }
    { // exportManifest.xml does not exist
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/exportManifest.xml" ) );
    }
    { // files with incorrect names do not exist
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/two+words%2525" ) );
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/two+words%25" ) );
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/two+words%" ) );
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/two words%2525" ) );
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/two words%" ) );
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/two words%25/eval+%28%2B%29%2525.prpt" ) );
      assertNull( repo.getFile( IMPORT_REPO_DIR + "/two words%25/eval (+)%.prpt" ) );
    }
  }

  private static class ZipBuilder {

    private ByteArrayOutputStream out = new ByteArrayOutputStream();
    private ZipOutputStream zip = new ZipOutputStream( out );

    public void addFile( String filePath, String fileName ) throws IOException {
      final File file = new File( filePath );
      assertTrue( file.getAbsolutePath() + " didn't found.", file.exists() );
      zip.putNextEntry( new ZipEntry( fileName ) );
      FileUtils.copyFile( file, zip );
      zip.closeEntry();
    }

    public ByteArrayOutputStream build() throws IOException {
      zip.close();
      out.close();
      return out;
    }
  }

  private IPlatformImportBundle buildBundle( InputStream solutionInputStream ) {
    final RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
    bundleBuilder.input( solutionInputStream );
    bundleBuilder.charSet( DEFAULT_ENCODING );
    bundleBuilder.hidden( RepositoryFile.HIDDEN_BY_DEFAULT );
    bundleBuilder.schedulable( RepositoryFile.SCHEDULABLE_BY_DEFAULT );
    bundleBuilder.path( IMPORT_REPO_DIR );
    bundleBuilder.overwriteFile( false );
    bundleBuilder.applyAclSettings( false );
    bundleBuilder.overwriteAclSettings( false );
    bundleBuilder.retainOwnership( true );
    bundleBuilder.name( "testSrc.zip" );
    bundleBuilder.mime( "application/vnd.pentaho.solution-repository" );
    return bundleBuilder.build();
  }

  @Test
  public void testImportHiddenFileByManifest() throws IOException, InterruptedException {
    repo.deleteFile( SRC_CONTENT_FILE, "" );

    answer = new AnswerTest<RepositoryFile>() {
      @Override
      public RepositoryFile answer( InvocationOnMock invocation ) throws Throwable {
        obj = invocation.getArgument( 1 );
        return null;
      }
    };

    Mockito.doAnswer( answer ).when( repo ).createFile( Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any() );

    // isHidden="true" isSchedulable="true"
    RepositoryFile file = importFile( "/manifestFileHidden.xml" );
    assertTrue( file.isHidden() );
    assertTrue( file.isSchedulable() );

    // isHidden="false" isSchedulable="false"
    file = importFile( "/manifestFileNotHidden.xml" );
    assertFalse( file.isHidden() );
    assertFalse( file.isSchedulable() );

    // nothing
    file = importFile( "/manifestHiddenByDefault.xml" );
    assertEquals( file.isHidden(), RepositoryFile.HIDDEN_BY_DEFAULT );
    assertEquals( file.isSchedulable(), RepositoryFile.SCHEDULABLE_BY_DEFAULT );
  }

  private RepositoryFile importFile( String manifest ) throws IOException,
    InterruptedException {
    ZipBuilder zipBuilder = new ZipBuilder();
    zipBuilder.addFile( solutionDir + SRC_ROOT + manifest, "exportManifest.xml" );
    zipBuilder.addFile( solutionDir + SRC_CONTENT_FILE, SRC_CONTENT_FILE );
    ByteArrayOutputStream out = zipBuilder.build();
    ByteArrayInputStream solutionInputStream = new ByteArrayInputStream( out.toByteArray() );

    IPlatformImportBundle bundle = buildBundle( solutionInputStream );

    runImport( solutionImportHandler, bundle );

    return answer != null ? answer.getObj() : null;
  }

  private abstract static class AnswerTest<T> implements Answer<T> {

    protected T obj;

    public T getObj() {
      return obj;
    }
  }
}
