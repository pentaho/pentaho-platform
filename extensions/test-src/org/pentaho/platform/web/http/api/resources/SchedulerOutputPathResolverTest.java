package org.pentaho.platform.web.http.api.resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by rfellows on 9/23/15.
 */
public class SchedulerOutputPathResolverTest {

  SchedulerOutputPathResolver schedulerOutputPathResolver;

  IUnifiedRepository repo;
  IUserSettingService userSettingService;

  @Before
  public void setUp() throws Exception {
    repo = mock( IUnifiedRepository.class );
    userSettingService = mock( IUserSettingService.class );
    PentahoSystem.registerObject( repo );
    PentahoSystem.registerObject( userSettingService );
  }

  @Test
  public void testResolveOutputFilePath() throws Exception {
    JobScheduleRequest scheduleRequest = new JobScheduleRequest();
    String inputFile = "/home/admin/test.prpt";
    String outputFolder = "/home/admin/output";
    scheduleRequest.setInputFile( inputFile );
    scheduleRequest.setOutputFile( outputFolder );

    RepositoryFile repoFile = mock( RepositoryFile.class );
    when( repo.getFile( outputFolder ) ).thenReturn( repoFile );
    when( repoFile.isFolder() ).thenReturn( true );

    schedulerOutputPathResolver = new SchedulerOutputPathResolver( scheduleRequest );
    String outputFilePath = schedulerOutputPathResolver.resolveOutputFilePath();

    assertEquals( "/home/admin/output/test.*", outputFilePath );
    verify( repo ).getFile( outputFolder );

  }

  @Test
  public void testResolveOutputFilePath_ContainsPatternAlready() throws Exception {
    JobScheduleRequest scheduleRequest = new JobScheduleRequest();
    String inputFile = "/home/admin/test.prpt";
    String outputFolder = "/home/admin/output/test.*";
    scheduleRequest.setInputFile( inputFile );
    scheduleRequest.setOutputFile( outputFolder );

    RepositoryFile repoFile = mock( RepositoryFile.class );
    when( repo.getFile( anyString() ) ).thenReturn( repoFile );
    when( repoFile.isFolder() ).thenReturn( true );

    schedulerOutputPathResolver = new SchedulerOutputPathResolver( scheduleRequest );
    String outputFilePath = schedulerOutputPathResolver.resolveOutputFilePath();

    assertEquals( "/home/admin/output/test.*", outputFilePath );
    verify( repo ).getFile( "/home/admin/output" );
  }

  @Test
  public void testResolveOutputFilePath_Fallback() throws Exception {
    JobScheduleRequest scheduleRequest = new JobScheduleRequest();
    String inputFile = "/home/admin/test.prpt";
    String outputFolder = null;
    scheduleRequest.setInputFile( inputFile );
    scheduleRequest.setOutputFile( outputFolder );

    RepositoryFile repoFile = mock( RepositoryFile.class );
    when( repo.getFile( anyString() ) ).thenReturn( repoFile );
    when( repoFile.isFolder() ).thenReturn( false );

    schedulerOutputPathResolver = spy( new SchedulerOutputPathResolver( scheduleRequest ) );
    doReturn( "/home/admin/setting" ).when( schedulerOutputPathResolver ).getUserSettingOutputPath();
    doReturn( "/system/setting" ).when( schedulerOutputPathResolver ).getSystemSettingOutputPath();
    doReturn( "/home/admin" ).when( schedulerOutputPathResolver ).getUserHomeDirectoryPath();
    doReturn( true ).when( schedulerOutputPathResolver ).isValidOutputPath( "/home/admin/setting" );
    String outputFilePath = schedulerOutputPathResolver.resolveOutputFilePath();

    assertEquals( "/home/admin/setting/test.*", outputFilePath );
  }

  @Test
  public void testResolveOutputFilePath_FallbackFarther() throws Exception {
    JobScheduleRequest scheduleRequest = new JobScheduleRequest();
    String inputFile = "/home/admin/test.prpt";
    String outputFolder = null;
    scheduleRequest.setInputFile( inputFile );
    scheduleRequest.setOutputFile( outputFolder );

    RepositoryFile repoFile = mock( RepositoryFile.class );
    when( repo.getFile( anyString() ) ).thenReturn( repoFile );
    when( repoFile.isFolder() ).thenReturn( false );

    schedulerOutputPathResolver = spy( new SchedulerOutputPathResolver( scheduleRequest ) );
    doReturn( null ).when( schedulerOutputPathResolver ).getUserSettingOutputPath();
    doReturn( null ).when( schedulerOutputPathResolver ).getSystemSettingOutputPath();
    doReturn( "/home/admin" ).when( schedulerOutputPathResolver ).getUserHomeDirectoryPath();
    doReturn( true ).when( schedulerOutputPathResolver ).isValidOutputPath( "/home/admin" );
    String outputFilePath = schedulerOutputPathResolver.resolveOutputFilePath();

    assertEquals( "/home/admin/test.*", outputFilePath );
  }

  @After
  public void tearDown() throws Exception {
    PentahoSystem.clearObjectFactory();
  }
}