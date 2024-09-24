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
 * Copyright (c) 2018 Hitachi Vantara. All rights reserved.
 *
 */

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
