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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importer;

import junit.framework.Assert;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static org.mockito.Mockito.*;
import static org.pentaho.platform.plugin.services.importer.ArchiveLoader.ZIPS_FILTER;

/**
 * Created with IntelliJ IDEA. User: kwalker Date: 6/20/13 Time: 12:37 PM
 */
public class ArchiveLoaderTest {

  private static final Date LOAD_STAMP = new Date( 123456789 );

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
    IRepositoryImportLogger logger = mock( IRepositoryImportLogger.class );
    when( importer.getRepositoryImportLogger() ).thenReturn( logger );
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
    IRepositoryImportLogger logger = mock( IRepositoryImportLogger.class );
    when( importer.getRepositoryImportLogger() ).thenReturn( logger );
    loader.loadAll( directory, ZIPS_FILTER );
    verify( importer ).importFile( argThat( bundleMatcher( jobsName, inputStream ) ) );
    verify( importer ).importFile( argThat( bundleMatcher( reportsName, inputStream ) ) );
    verify( jobs ).renameTo( argThat( fileMatcher( jobs ) ) );
    verify( reports ).renameTo( argThat( fileMatcher( reports ) ) );
  }

  @Test
  public void testDoesNotBombWhenDirectoryDoesNotExist() throws Exception {
    IPlatformImporter importer = mock( IPlatformImporter.class );
    ArchiveLoader loader = new ArchiveLoader( importer );
    try {
      loader.loadAll( new File( "/fake/path/that/does/not/exist" ), ArchiveLoader.ZIPS_FILTER );
    } catch ( Exception e ) {
      Assert.fail( "Expected no exception but got " + e.getMessage() );
    }
  }

  @Test
  public void testDoesNotBombWhenZeroFilesFound() throws Exception {
    final IPlatformImporter importer = mock( IPlatformImporter.class );
    final ArchiveLoader loader = new ArchiveLoader( importer );
    final File directory = mock( File.class );
    when( directory.listFiles( ZIPS_FILTER ) ).thenReturn( new File[] {} );
    loader.loadAll( directory, ZIPS_FILTER );
  }

  private ArchiveLoader createArchiveLoader( final IPlatformImporter importer, final FileInputStream inputStream ) {
    return new ArchiveLoader( importer, LOAD_STAMP ) {
      @Override
      FileInputStream createInputStream( File file ) throws FileNotFoundException {
        return inputStream;
      }
    };
  }

  private ArgumentMatcher<IPlatformImportBundle> bundleMatcher( final String filename, final InputStream inputStream ) {
    return new ArgumentMatcher<IPlatformImportBundle>() {
      @Override
      public boolean matches( Object argument ) {
        RepositoryFileImportBundle bundle = (RepositoryFileImportBundle) argument;
        try {
          return bundle.getName().equals( filename ) && bundle.getAcl() == null
              && bundle.getInputStream().equals( inputStream ) && bundle.overwriteInRepository() && bundle.isHidden();
        } catch ( IOException e ) {
          return false;
        }
      }
    };
  }

  private Matcher<File> fileMatcher( final File origFile ) {
    return new BaseMatcher<File>() {
      @Override
      public boolean matches( final Object item ) {
        return ( (File) item ).getName().equals( origFile.getName() + ".197001020517" );
      }

      @Override
      public void describeTo( final Description description ) {

      }
    };
  }
}
