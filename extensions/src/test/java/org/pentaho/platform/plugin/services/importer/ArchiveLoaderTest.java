/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.plugin.services.importer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.importexport.ImportSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.plugin.services.importer.ArchiveLoader.ZIPS_FILTER;

/**
 * Created with IntelliJ IDEA. User: kwalker Date: 6/20/13 Time: 12:37 PM
 */
@RunWith( MockitoJUnitRunner.class )
public class ArchiveLoaderTest {

  private static final Date LOAD_STAMP = new Date( 123456789 );
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( ".yyyyMMddHHmm" );
  private static final String TIMESTAMP =  DATE_FORMAT.format( LOAD_STAMP );

  @Test
  public void testWillImportAllZipsInADirectory() throws Exception {
    final IPlatformImporter importer = mock( IPlatformImporter.class );
    final FileInputStream inputStream = mock( FileInputStream.class );
    final ArchiveLoader loader = createArchiveLoader( importer, inputStream );
    final File directory = mock( File.class );
    final File jobs = mock( File.class );
    String jobsName = "jobs.zip";
    when( jobs.getName() ).thenReturn( jobsName );
    when( jobs.getPath() ).thenReturn( "/root/path/" + jobsName );
    final File reports = mock( File.class );
    String reportsName = "reports.zip";
    when( reports.getName() ).thenReturn( reportsName );
    when( reports.getPath() ).thenReturn( "/root/path/" + reportsName );
    when( directory.listFiles( ZIPS_FILTER ) ).thenReturn( new File[] { jobs, reports } );
    loader.loadAll( directory, ZIPS_FILTER );
    verify( importer ).importFile( argThat( bundleMatcher( jobsName, inputStream ) ) );
    verify( jobs ).renameTo( argThat( fileMatcher( jobs ) ) );
    verify( importer ).importFile( argThat( bundleMatcher( reportsName, inputStream ) ) );
    verify( reports ).renameTo( argThat( fileMatcher( reports ) ) );
  }

  @Test
  public void testWillContinueToLoadOnException() throws Exception {
    final IPlatformImporter importer = mock( IPlatformImporter.class );
    final FileInputStream inputStream = mock( FileInputStream.class );
    final ArchiveLoader loader = createArchiveLoader( importer, inputStream );
    final File directory = mock( File.class );
    final File jobs = mock( File.class );
    String jobsName = "jobs.zip";
    when( jobs.getName() ).thenReturn( jobsName );
    final File reports = mock( File.class );
    when( jobs.getPath() ).thenReturn( "/root/path/" + jobsName );
    String reportsName = "reports.zip";
    when( reports.getName() ).thenReturn( reportsName );
    when( reports.getPath() ).thenReturn( "/root/path/" + reportsName );
    when( directory.listFiles( ZIPS_FILTER ) ).thenReturn( new File[] { jobs, reports } );
    Exception exception = new RuntimeException( "exception thrown on purpose from testWillContinueToLoadOnException" );
    doThrow( exception ).when( importer ).importFile( argThat( bundleMatcher( jobsName, inputStream ) ) );
    loader.loadAll( directory, ZIPS_FILTER );
    verify( importer ).importFile( argThat( bundleMatcher( jobsName, inputStream ) ) );
    verify( importer ).importFile( argThat( bundleMatcher( reportsName, inputStream ) ) );
    verify( jobs ).renameTo( argThat( fileMatcher( jobs ) ) );
    verify( reports ).renameTo( argThat( fileMatcher( reports ) ) );
  }

  @Test
  public void testDoesNotBombWhenDirectoryDoesNotExist() {
    IPlatformImporter importer = mock( IPlatformImporter.class );
    ArchiveLoader loader = new ArchiveLoader( importer );
    try {
      loader.loadAll( new File( "/fake/path/that/does/not/exist" ), ArchiveLoader.ZIPS_FILTER );
    } catch ( Exception e ) {
      Assert.fail( "Expected no exception but got " + e.getMessage() );
    }
  }

  @Test
  public void testDoesNotBombWhenZeroFilesFound() {
    final IPlatformImporter importer = mock( IPlatformImporter.class );
    final ArchiveLoader loader = new ArchiveLoader( importer );
    final File directory = mock( File.class );
    when( directory.listFiles( ZIPS_FILTER ) ).thenReturn( new File[] {} );
    loader.loadAll( directory, ZIPS_FILTER );
  }

  @Test
  public void testLoadAllClearSession() {
    try ( MockedStatic<ImportSession> importSessionMock = mockStatic( ImportSession.class ) ) {
      ImportSession importSession = mock( ImportSession.class );
      importSessionMock.when( ImportSession::getSession ).thenReturn( importSession );
      IPlatformImporter importer = mock( IPlatformImporter.class );
      ArchiveLoader loader = new ArchiveLoader( importer );
      File directoryMock = mock( File.class );
      File file1Mock = mock( File.class );
      File file2Mock = mock( File.class );
      when( directoryMock.listFiles( ZIPS_FILTER ) ).thenReturn( new File[]{file1Mock, file2Mock} );
      loader.loadAll( directoryMock, ArchiveLoader.ZIPS_FILTER );
      importSessionMock.verify( ImportSession::getSession );
      ImportSession.clearSession();
    }
  }

  private ArchiveLoader createArchiveLoader( final IPlatformImporter importer, final FileInputStream inputStream ) {
    return new ArchiveLoader( importer, LOAD_STAMP ) {
      @Override
      FileInputStream createInputStream( File file ) {
        return inputStream;
      }
    };
  }

  private ArgumentMatcher<IPlatformImportBundle> bundleMatcher( final String filename, final InputStream inputStream ) {
    return argument -> {
      RepositoryFileImportBundle bundle = (RepositoryFileImportBundle) argument;
      try {
        return bundle.getName().equals( filename ) && bundle.getAcl() == null
            && bundle.getInputStream().equals( inputStream ) && bundle.overwriteInRepository() && bundle.isHidden();
      } catch ( IOException e ) {
        return false;
      }
    };
  }

  private ArgumentMatcher<File> fileMatcher( final File origFile ) {
    return item -> ( item ).getName().equals( origFile.getName() + TIMESTAMP );
  }
}
