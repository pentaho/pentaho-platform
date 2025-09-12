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


package org.pentaho.platform.admin;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    generatedContentCleaner.setAge( 1000 );
  }

  @After
  public void cleanup() {
    Mockito.validateMockitoUsage();
  }

  @Test
  public void testExecute_noFilesToDelete() throws Exception {
    final Date fixedDate = new SimpleDateFormat( "d/m/yyyy" ).parse( "1/1/2015" );
    RepositoryFile file =
      new RepositoryFile.Builder( DEFAULT_STRING, FILE_ID ).folder( false ).createdDate( fixedDate ).build();
    RepositoryFileTree tree = new RepositoryFileTree( file, null );
    lenient().when( repo.getTree( nullable( String.class ), eq( -1 ), nullable( String.class ), eq( true ) ) ).thenReturn( tree );

    generatedContentCleaner.execute();
    verify( repo, never() ).deleteFile( any( Serializable.class ), eq( true ), nullable( String.class ) );
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
    when( repo.getTree( nullable( String.class ), eq( -1 ), nullable( String.class ), eq( true ) ) ).thenReturn( rootRepoFileTree );

    Map<String, Serializable> values = new HashMap<>();
    values.put( IScheduler.RESERVEDMAPKEY_LINEAGE_ID, "lineageIdGoesHere" );
    when( repo.getFileMetadata( FILE_ID ) ).thenReturn( values );

    generatedContentCleaner.execute();
    verify( repo ).deleteFile( eq( FILE_ID ), eq( true ), nullable( String.class ) );
    assertEquals( 1000, generatedContentCleaner.getAge() );
  }
}
