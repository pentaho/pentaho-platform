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


package org.pentaho.platform.plugin.action.pentahometadata;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.query.model.Parameter;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MetadataQueryComponentTest {

  private Parameter parameter;
  private Map<String, Object> inputs;
  private Object value;
  private MetadataQueryComponent metadataQueryComponent;

  @Before
  public void initializeVariables() {
    parameter = new Parameter( "test", DataType.STRING, "defaultValue" );
    inputs = new HashMap<>( );
    metadataQueryComponent = new MetadataQueryComponent();
  }

  @Test
  public void getParameterValueStringArrayNull() throws Exception {
    inputs.put( "test", new String[] { } );
    metadataQueryComponent.setInputs( inputs );
    assertEquals( true, metadataQueryComponent.inputs.get( parameter.getName() ) instanceof String[] );
    assertEquals( 0, ( (String[]) metadataQueryComponent.inputs.get( parameter.getName() ) ).length );
    value = metadataQueryComponent.getParameterValue( parameter );
    assertEquals( null, value );
  }

  @Test
  public void getParameterValueStringArrayNotNull() throws Exception {
    inputs.put( "test", new String[] { "newValue" } );
    metadataQueryComponent.setInputs( inputs );
    assertEquals( true, metadataQueryComponent.inputs.get( parameter.getName() ) instanceof String[] );
    assertNotEquals( 0, ( (String[]) metadataQueryComponent.inputs.get( parameter.getName() ) ).length );
    value = metadataQueryComponent.getParameterValue( parameter );
    assertEquals( "newValue", ( (String[]) value )[0] );
  }

  @Test
  public void getParameterValueStringNull() throws Exception {
    inputs.put( "test", new String() );
    metadataQueryComponent.setInputs( inputs );
    assertEquals( true, metadataQueryComponent.inputs.get( parameter.getName() ) instanceof String );
    assertEquals( 0, ( (String) metadataQueryComponent.inputs.get( parameter.getName() ) ).length() );
    Object value = metadataQueryComponent.getParameterValue( parameter );
    assertEquals( null, value );
  }

  @Test
  public void getParameterValueStringNotNull() throws Exception {
    inputs.put( "test", "newValue" );
    metadataQueryComponent.setInputs( inputs );
    assertEquals( true, metadataQueryComponent.inputs.get( parameter.getName() ) instanceof String );
    assertNotEquals( 0, ( (String) metadataQueryComponent.inputs.get( parameter.getName() ) ).length() );
    Object value = metadataQueryComponent.getParameterValue( parameter );
    assertEquals( "newValue", value.toString() );
  }

}
