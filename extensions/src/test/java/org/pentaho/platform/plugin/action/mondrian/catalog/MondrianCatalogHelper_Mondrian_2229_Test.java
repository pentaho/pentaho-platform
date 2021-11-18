/*!
 *
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
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action.mondrian.catalog;

import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
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
      .appendCatalogsSection( any( IUnifiedRepository.class ), nullable( String.class ), any( RepositoryFile.class ),
        any( StringBuffer.class ) );

    return helperSpy.generateInMemoryDatasourcesXml( unifiedRepositoryMock );
  }
}
