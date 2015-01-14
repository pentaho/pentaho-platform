package org.pentaho.platform.plugin.action.mondrian.catalog;

import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

/**
 * Thee tests related to MONDRIAN-2229 issue
 */
public class MondrianCatalogHelper_Mondrian_2229_Test {

  @Test
  public void testGenerateInMemoryDatasourcesXml_DataSourceNameProviderUsingLegacyDbName() throws Exception {
    String result = prepareResultForMondrian2229Tests( true );
    assertThat( result, containsString( "<DataSourceName>Provider=Mondrian</DataSourceName>" ) );
  }

  @Test
  public void testGenerateInMemoryDatasourcesXml_DataSourceNameProviderNotUsingLegacyDbName() throws Exception {
    String result = prepareResultForMondrian2229Tests( false );
    assertThat( result, containsString( "<DataSourceName>Pentaho Mondrian</DataSourceName>" ) );
  }

  @Test
  public void testGenerateInMemoryDatasourcesXml_DataSourceInfoProvider() throws Exception {
    String result = prepareResultForMondrian2229Tests( true );
    assertThat( result, containsString( "<DataSourceInfo>Provider=Mondrian</DataSourceInfo>" ) );
  }

  private String prepareResultForMondrian2229Tests( boolean isUseLegacyDbName ) {
    MondrianCatalogHelper helper = new MondrianCatalogHelper( isUseLegacyDbName );
    MondrianCatalogHelper helperSpy = spy( helper );

    IUnifiedRepository unifiedRepositoryMock = mock( IUnifiedRepository.class );
    RepositoryFile repositoryFileMock = mock( RepositoryFile.class );

    when( unifiedRepositoryMock.getFile( any( String.class ) ) ).thenReturn( repositoryFileMock );

    String contextPathStub = "Stub";
    doReturn( contextPathStub ).when( helperSpy ).contextPathFromRequestContextHolder();

    doNothing().when( helperSpy )
      .appendCatalogsSection( any( IUnifiedRepository.class ), anyString(), any( RepositoryFile.class ),
        any( StringBuffer.class ) );

    return helperSpy.generateInMemoryDatasourcesXml( unifiedRepositoryMock );
  }
}
