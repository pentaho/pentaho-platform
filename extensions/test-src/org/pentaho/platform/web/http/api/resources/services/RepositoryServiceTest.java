package org.pentaho.platform.web.http.api.resources.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginOperation;

public class RepositoryServiceTest {

  private static RepositoryService repositoryService;
  
  @Before
  public void setUp() throws Exception {
    repositoryService = spy( new RepositoryService() );
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
    doReturn( mockPluginManager).when( repositoryService ).getPluginManager( ( Class<IPluginManager> )anyObject(), ( IPentahoSession )anyObject() );
    
    URI uri = repositoryService.doExecuteDefault( "path:to:file", new StringBuffer("http://localhost:8080/pentaho/repos/path:to:file/default"), "" );
    
    verify( mockContentInfo, times( 1 ) ).getOperations();
    verify( mockPluginManager, times( 1 ) ).getContentTypeInfo( anyString() );
    verify( mockPluginOperation, times( 1 ) ).getId();
    verify( repositoryService ).getPluginManager(  ( Class<IPluginManager> )anyObject(), ( IPentahoSession )anyObject() );
    
    assertTrue( uri.toASCIIString().endsWith( "generatedContent" ) );
  }

}
