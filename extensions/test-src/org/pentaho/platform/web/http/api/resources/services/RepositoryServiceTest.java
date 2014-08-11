package org.pentaho.platform.web.http.api.resources.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.web.http.api.resources.FileResource;
import org.pentaho.platform.web.http.api.resources.GeneratorStreamingOutput;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;

public class RepositoryServiceTest {

  private static RepositoryService repositoryService;

  @Before
  public void setUp() {
    this.repositoryService = spy( new RepositoryService() );
  }

  @After
  public void tearDown() {
    this.repositoryService = null;
  }

  @Test
  public void testDoExecuteDefault() throws FileNotFoundException, MalformedURLException, URISyntaxException {
    IPluginManager mockPluginManager = mock( IPluginManager.class );
    IContentInfo mockContentInfo = mock( IContentInfo.class );
    List<IPluginOperation> mockOperationsList = new ArrayList<IPluginOperation>();
    IPluginOperation mockPluginOperation = mock( IPluginOperation.class );
    mockOperationsList.add( mockPluginOperation );

    doReturn( mockOperationsList ).when( mockContentInfo ).getOperations();
    doReturn( mockContentInfo ).when( mockPluginManager ).getContentTypeInfo( anyString() );
    doReturn( "RUN" ).when( mockPluginOperation ).getId();
    doReturn( mockPluginManager ).when( repositoryService )
      .getPluginManager( (Class<IPluginManager>) anyObject(), (IPentahoSession) anyObject() );

    URI uri = repositoryService
      .doExecuteDefault( "path:to:file", new StringBuffer( "http://localhost:8080/pentaho/repos/path:to:file/default" ),
        "" );

    verify( mockContentInfo, times( 1 ) ).getOperations();
    verify( mockPluginManager, times( 1 ) ).getContentTypeInfo( anyString() );
    verify( mockPluginOperation, times( 1 ) ).getId();
    verify( repositoryService ).getPluginManager( (Class<IPluginManager>) anyObject(), (IPentahoSession) anyObject() );

    assertTrue( uri.toASCIIString().endsWith( "generatedContent" ) );
  }

  @Test
  public void testGetContentGeneratorStreamingOutput() {
    RepositoryService.CGFactoryInterface mockFactory = mock( RepositoryService.CGFactoryInterface.class );
    IContentGenerator mockGenerator = mock( IContentGenerator.class );

    doReturn( mockGenerator ).when( mockFactory ).create();

    GeneratorStreamingOutput mockOutput = mock( GeneratorStreamingOutput.class );
    doReturn( mockOutput ).when( mockFactory ).getStreamingOutput( mockGenerator );

    doNothing().when( repositoryService ).rsc( anyString(), any() );
    doNothing().when( repositoryService ).rsc( anyString(), any(), any() );

    GeneratorStreamingOutput output = repositoryService.getContentGeneratorStreamingOutput( mockFactory );

    verify( repositoryService, times( 1 ) ).rsc( anyString(), any() );
    verify( repositoryService, times( 1 ) ).rsc( anyString(), any(), any() );
    assertEquals( mockOutput, output );
  }

  @Test
  public void testGetContentGeneratorStreamingOutputException() {
    // Test 1
    RepositoryService.CGFactoryInterface mockFactory = mock( RepositoryService.CGFactoryInterface.class );
    doThrow( new NoSuchBeanDefinitionException( "test", "test" ) ).when( mockFactory ).create();

    doNothing().when( repositoryService ).rsc( anyString(), any() );

    assertNull( repositoryService.getContentGeneratorStreamingOutput( mockFactory ) );
    verify( repositoryService, times( 2 ) ).rsc( anyString(), any() );

    // Test 2
    doReturn( null ).when( mockFactory ).create();

    assertNull( repositoryService.getContentGeneratorStreamingOutput( mockFactory ) );
    verify( repositoryService, times( 4 ) ).rsc( anyString(), any() );
  }

  @Test
  public void testGetUrl() {
    // Test 1
    String url = "url";

    RepositoryFile mockFile = mock( RepositoryFile.class );
    doReturn( url ).when( mockFile ).getName();

    doReturn( url ).when( repositoryService ).extractUrl( mockFile );

    String fullyQualifiedServerUrl = "test";
    doReturn( fullyQualifiedServerUrl ).when( repositoryService ).getFullyQualifiedServerURL();

    String resultUrl = repositoryService.getUrl( mockFile, "generatedContent" );

    assertEquals( fullyQualifiedServerUrl + url, resultUrl );
    verify( mockFile, times( 2 ) ).getName();
    verify( repositoryService, times( 1 ) ).extractUrl( mockFile );
    verify( repositoryService, times( 1 ) ).getFullyQualifiedServerURL();

    // Test 2
    doReturn( "" ).when( mockFile ).getName();

    resultUrl = repositoryService.getUrl( mockFile, "generatedContent" );
    assertNull( resultUrl );
  }

  @Test
  public void testGetRepositoryFileResourcePath() {
    String filePath = "filePath";
    String relPath = "relPath";

    FileResource mockFileResource = mock( FileResource.class );

    doNothing().when( repositoryService ).rsc( anyString(), eq( relPath ), eq( filePath ) );

    String separatorsToRepository = "result";
    doReturn( separatorsToRepository ).when( repositoryService )
      .separatorsToRepository( eq( filePath ), eq( relPath ) );

    String result = repositoryService.getRepositoryFileResourcePath( mockFileResource, filePath, relPath );

    assertEquals( separatorsToRepository, result );
    verify( repositoryService, times( 1 ) ).rsc( anyString(), eq( relPath ), eq( filePath ) );
  }

  @Test
  public void testExtractUrl() throws Exception {
    RepositoryFile mockFile = mock( RepositoryFile.class );

    Serializable mockFileId = mock( Serializable.class );
    doReturn( mockFileId ).when( mockFile ).getId();

    SimpleRepositoryFileData mockFileData = mock( SimpleRepositoryFileData.class );

    IUnifiedRepository mockRepository = mock( IUnifiedRepository.class );
    doReturn( mockRepository ).when( repositoryService ).getIUnifiedRepository();

    doReturn( mockFileData ).when( mockRepository )
      .getDataForRead( eq( mockFileId ), eq( SimpleRepositoryFileData.class ) );

    StringWriter mockStringWriter = mock( StringWriter.class );
    doReturn( mockStringWriter ).when( repositoryService ).getStringWriter();

    String value = "val";
    String props = "url=" + value + "\r";
    doReturn( props ).when( mockStringWriter ).toString();

    InputStream mockInputStream = mock( InputStream.class );
    doReturn( mockInputStream ).when( mockFileData ).getInputStream();

    doNothing().when( repositoryService ).copy( mockInputStream, mockStringWriter );

    String result = repositoryService.extractUrl( mockFile );

    assertEquals( value, result );
    verify( repositoryService, times( 1 ) ).getIUnifiedRepository();
    verify( mockRepository, times( 1 ) ).getDataForRead( eq( mockFileId ), eq( SimpleRepositoryFileData.class ) );
    verify( repositoryService, times( 1 ) ).getStringTokenizer( eq( props ), eq( "\n" ) );
  }

}
