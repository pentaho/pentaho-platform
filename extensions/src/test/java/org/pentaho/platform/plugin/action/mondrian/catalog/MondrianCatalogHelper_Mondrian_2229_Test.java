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


package org.pentaho.platform.plugin.action.mondrian.catalog;

import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
      .appendCatalogsSection( any( IUnifiedRepository.class ), any( RepositoryFile.class ),
        any( StringBuffer.class ) );

    return helperSpy.generateInMemoryDatasourcesXml( unifiedRepositoryMock );
  }
}
