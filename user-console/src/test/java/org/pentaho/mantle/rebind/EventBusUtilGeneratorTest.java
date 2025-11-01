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


package org.pentaho.mantle.rebind;

import bsh.Interpreter;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JType;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class EventBusUtilGeneratorTest {

  @Test
  public void addHandlerParamJson_ReturnsValidEmptyJson_WhenNoGettersWereFound() throws Exception {
    JClassType type = mock( JClassType.class );
    when( type.getMethods() ).thenReturn( new JMethod[ 0 ] );

    EventBusUtilGenerator generator = new EventBusUtilGenerator();
    String s = generator.addHandlerParamJson( type );
    assertEquals( s, "\"{}\"" );
    assertJsonIsValid( s, Collections.emptyMap() );
  }

  @Test
  public void addHandlerParamJson_ReturnsValidEmptyJson_WhenTwoSimpleGettersWereFound() throws Exception {
    JType stringType = mockType( "string" );
    JField stringField = mockField( stringType, "stringField" );
    JMethod getStringField = mockMethod( stringField );

    JType intType = mockType( "int" );
    JField intField = mockField( intType, "intField" );
    JMethod getIntField = mockMethod( intField );

    JClassType type = mock( JClassType.class );
    when( type.getField( stringField.getName() ) ).thenReturn( stringField );
    when( type.getField( intField.getName() ) ).thenReturn( intField );
    when( type.getMethods() ).thenReturn( new JMethod[] { getStringField, getIntField } );

    EventBusUtilGenerator generator = new EventBusUtilGenerator();
    String s = generator.addHandlerParamJson( type );

    Map<String, Object> expectedContent = new HashMap<>( 4 );
    expectedContent.put( stringField.getName(), "stringValue" );
    expectedContent.put( intField.getName(), 1 );

    assertJsonIsValid( s, expectedContent );
  }

  private JType mockType( String name ) {
    JType type = mock( JType.class );
    when( type.getSimpleSourceName() ).thenReturn( name );
    return type;
  }

  private JField mockField( JType type, String name ) {
    JField field = mock( JField.class );
    when( field.getType() ).thenReturn( type );
    when( field.getName() ).thenReturn( name );
    return field;
  }

  private JMethod mockMethod(JField field) {
    JType type = field.getType();
    String name = "get" + StringUtils.capitalize( field.getName() );

    JMethod method = mock( JMethod.class );
    when( method.isPublic() ).thenReturn( true );
    when( method.getReturnType() ).thenReturn( type );
    when( method.getName() ).thenReturn( name );
    return method;
  }

  private void assertJsonIsValid( String json, Map<?, ?> expectedContent ) throws Exception {
    DummyEvent event = new DummyEvent();
    event.intField = 1;
    event.stringField = "stringValue";

    Interpreter interpreter = new Interpreter();
    interpreter.setStrictJava( true );
    interpreter.set( "event", event );
    json = interpreter.eval( json ).toString();

    JSONObject o = (JSONObject) new JSONParser().parse( json );
    for ( Map.Entry<?, ?> entry : expectedContent.entrySet() ) {
      // toString() to avoid messing with Integer vs Long boxing
      assertEquals( o.get( entry.getKey() ).toString(), entry.getValue().toString() );
    }
  }

  @SuppressWarnings( "unused" )
  public static class DummyEvent {
    public String stringField;
    public int intField;

    public String getStringField() {
      return stringField;
    }

    public int getIntField() {
      return intField;
    }
  }
}
