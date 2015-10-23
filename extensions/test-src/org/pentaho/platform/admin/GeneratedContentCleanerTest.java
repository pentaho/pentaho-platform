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
  @Mock RepositoryFileTree tree;

  GeneratedContentCleaner generatedContentCleaner;

  @Before
  public void setUp() throws Exception {
    PentahoSystem.registerObject( repo );
    generatedContentCleaner = new GeneratedContentCleaner();
    generatedContentCleaner.setAge( 0 );
  }

  @Test
  public void testExecute_noFilesToDelete() throws Exception {
    when( repo.getTree( anyString(), eq( -1 ), anyString(), eq( false ) ) ).thenReturn( tree );
    RepositoryFile file = mock( RepositoryFile.class );
    when( tree.getFile() ).thenReturn( file );
    when( file.isFolder() ).thenReturn( false );
    when( file.getCreatedDate() ).thenReturn( new Date() );

    generatedContentCleaner.execute();
    verify( repo, never() ).deleteFile( any( Serializable.class ), eq( true ), anyString() );
  }

  @Test
  public void testExecute() throws Exception {
    when( repo.getTree( anyString(), eq( -1 ), anyString(), eq( false ) ) ).thenReturn( tree );
    RepositoryFile file = mock( RepositoryFile.class );
    when( tree.getFile() ).thenReturn( file );
    when( file.isFolder() ).thenReturn( false );
    when( file.getCreatedDate() ).thenReturn( new Date() );
    when( file.getId() ).thenReturn( "fileId" );
    Map<String, Serializable> values = new HashMap<String, Serializable>();
    values.put( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID, "lineageIdGoesHere" );
    when( repo.getFileMetadata( "fileId" ) ).thenReturn( values );

    generatedContentCleaner.execute();
    verify( repo ).deleteFile( eq( "fileId" ), eq( true ), anyString() );
    assertEquals( 0, generatedContentCleaner.getAge() );
  }

}