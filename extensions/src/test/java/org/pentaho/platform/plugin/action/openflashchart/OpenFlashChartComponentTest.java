/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.plugin.action.openflashchart;

import org.dom4j.Node;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IRuntimeContext;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

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
