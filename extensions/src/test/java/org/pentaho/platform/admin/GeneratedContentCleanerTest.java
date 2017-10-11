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

package org.pentaho.platform.admin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 10/19/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class GeneratedContentCleanerTest {

  @Mock IUnifiedRepository repo;

  private static final String FILE_ID = "fileId";
  private static final String FOlDER_ID = "folderId";
  private static final String DEFAULT_STRING = "<def>";

  GeneratedContentCleaner generatedContentCleaner;

  @Before
  public void setUp() throws Exception {
    PentahoSystem.registerObject( repo );
    generatedContentCleaner = new GeneratedContentCleaner();
    generatedContentCleaner.setAge( 0 );
  }

  @Test
  public void testExecute_noFilesToDelete() throws Exception {
    final Date fixedDate = new SimpleDateFormat( "d/m/yyyy" ).parse( "1/1/2015" );
    RepositoryFile file =
      new RepositoryFile.Builder( DEFAULT_STRING, FILE_ID ).folder( false ).createdDate( fixedDate ).build();
    RepositoryFileTree tree = new RepositoryFileTree( file, null );
    when( repo.getTree( anyString(), eq( -1 ), anyString(), eq( true ) ) ).thenReturn( tree );

    generatedContentCleaner.execute();
    verify( repo, never() ).deleteFile( any( Serializable.class ), eq( true ), anyString() );
  }

  @Test
  public void testExecute_oldFilesInFolderDeleted() throws Exception {
    final Date fixedDate = new SimpleDateFormat( "d/m/yyyy" ).parse( "1/1/2015" );
    RepositoryFile folder =
      new RepositoryFile.Builder( FOlDER_ID, DEFAULT_STRING ).folder( true ).build();
    RepositoryFile file =
      new RepositoryFile.Builder( FILE_ID, DEFAULT_STRING ).folder( false ).createdDate( fixedDate ).build();

    RepositoryFileTree childRepoFileTree = new RepositoryFileTree( file, null );
    RepositoryFileTree rootRepoFileTree =
      new RepositoryFileTree( folder, Collections.singletonList( childRepoFileTree ) );
    when( repo.getTree( anyString(), eq( -1 ), anyString(), eq( true ) ) ).thenReturn( rootRepoFileTree );

    Map<String, Serializable> values = new HashMap<String, Serializable>();
    values.put( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID, "lineageIdGoesHere" );
    when( repo.getFileMetadata( FILE_ID ) ).thenReturn( values );

    generatedContentCleaner.execute();
    verify( repo ).deleteFile( eq( FILE_ID ), eq( true ), anyString() );
    assertEquals( 0, generatedContentCleaner.getAge() );
  }
}
