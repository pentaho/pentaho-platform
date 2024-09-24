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

package org.pentaho.platform.plugin.action.openflashchart;

import org.dom4j.Node;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IRuntimeContext;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 11/2/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class OpenFlashChartComponentTest {
  OpenFlashChartComponent flashChartComponent;

  @Mock IPentahoResultSet resultSet;
  @Mock IPentahoResultSet memoryResultSet;
  @Mock IRuntimeContext runtimeContext;
  @Mock Set inputNames;
  @Mock Node componentDefinitionNode;
  @Mock Node chartAttributesNode;

  @Before
  public void setUp() throws Exception {
    flashChartComponent = new OpenFlashChartComponent();
    flashChartComponent.setRuntimeContext( runtimeContext );
    when( runtimeContext.getInputNames() ).thenReturn( inputNames );
    when( inputNames.contains( any() ) ).thenReturn( true );
  }

  @Test
  public void testExecuteAction() throws Exception {
    OpenFlashChartComponent fcc = spy( flashChartComponent );

    doReturn( resultSet ).when( fcc ).getInputValue( "chart-data" );
    when( resultSet.isScrollable() ).thenReturn( false );
    when( resultSet.memoryCopy() ).thenReturn( memoryResultSet );

    doReturn( "20" ).when( fcc ).getInputStringValue( "width" );
    doReturn( "30" ).when( fcc ).getInputStringValue( "height" );
    doReturn( "http://localhost:8080/openflashchart" ).when( fcc ).getInputStringValue( "ofc_url" );
    doReturn( "open-flash-chart-full-embedded-font.swf" ).when( fcc ).getInputStringValue( "ofc_lib_name" );
    doReturn( null ).when( fcc ).getInputStringValue( "chart-attributes" );

    doReturn( componentDefinitionNode ).when( fcc ).getComponentDefinition( true );
    when( componentDefinitionNode.selectSingleNode( "chart-attributes" ) ).thenReturn( chartAttributesNode );

    doReturn( "" ).when( fcc ).generateChartJson( memoryResultSet, chartAttributesNode, false );


    boolean action = fcc.executeAction();
    assertTrue( action );

    verify( resultSet ).close();
  }

  @Test
  public void testValidateAction_noChartData() throws Exception {
    OpenFlashChartComponent fcc = spy( flashChartComponent );
    doReturn( false ).when( fcc ).isDefinedInput( "chart-data" );

    assertFalse( fcc.validateAction() );
    verify( fcc ).inputMissingError( "chart-data" );
  }

  @Test
  public void testValidateAction_noChartAttributes() throws Exception {
    OpenFlashChartComponent fcc = spy( flashChartComponent );
    doReturn( true ).when( fcc ).isDefinedInput( "chart-data" );
    doReturn( false ).when( fcc ).isDefinedInput( "chart-attributes" );

    assertFalse( fcc.validateAction() );
    verify( fcc ).inputMissingError( "chart-attributes" );
  }

  @Test
  public void testValidateAction() throws Exception {
    OpenFlashChartComponent fcc = spy( flashChartComponent );
    doReturn( true ).when( fcc ).isDefinedInput( "chart-data" );
    doReturn( false ).when( fcc ).isDefinedInput( "chart-attributes" );
    doReturn( true ).when( fcc ).isDefinedResource( "chart-attributes" );

    assertTrue( fcc.validateAction() );
  }

  @Test
  public void testValidateSystemSettings() throws Exception {
    assertTrue( flashChartComponent.validateSystemSettings() );
  }

  @Test
  public void testInit() throws Exception {
    assertTrue( flashChartComponent.init() );
  }

  @Test
  public void testDone() throws Exception {
    //  no-op, calling it for code coverage
    flashChartComponent.done();
  }
}
