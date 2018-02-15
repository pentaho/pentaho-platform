/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;

import junit.framework.TestCase;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IParameterManager;
import org.pentaho.platform.api.engine.IParameterResolver;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.services.actionsequence.ActionParameter;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.util.DateMath;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings( { "all" } )
public class TemplateUtilTest extends TestCase implements IParameterResolver {

  public void testVariable() {

    Properties props = new Properties();
    props.put( "name1", "value1" );

    String template = "{name1}";
    String value = TemplateUtil.applyTemplate( template, props, (IParameterResolver) null );

    assertEquals( value, "value1" );

  }

  public void testPreareVariable() {

    Properties props = new Properties();

    String template = "{PREPARE:name1}";
    String value = TemplateUtil.applyTemplate( template, props, this );

    assertEquals( value, "value1" );

  }

  public void testDateRegexSimple() {

    doCompare( "+1:MS" );
    doCompare( "0:DS" );
    doCompare( "-10:Y" );

  }

  public void testDateRegexCompound() {

    doCompare( "+1:MS -2:DS" );
    doCompare( "0:DS +12:h" );
    doCompare( "-10:Y 0:MS" );
    doCompare( "+1:MS -2:DS" );
    doCompare( "0:DS +12:h" );
    doCompare( "-10:Y 0:MS" );

  }

  public void testDateRegexFormatted() {

    doCompare( "+1:MS;MM,yyyy-dd" );
    doCompare( "0:DS +12:h;yyyy-MM-dd" );
    doCompare( "-10:Y\t0:MS;yyyy-MM-dd" );
    // doCompare( "-10:Y 0:MS;yyyy-MM-dd hh:mm:ss" );

  }

  public void testDateRegexDateMath() {

    doCompare( "DATEMATH('+1:MS')", "+1:MS;yyyy-MM-dd" );
    doCompare( "DATEMATH(\"0:DS +12:h\")", "0:DS +12:h;yyyy-MM-dd" );
    doCompare( "DATEMATH( '-10:Y 0:MS' )", "-10:Y 0:MS;yyyy-MM-dd" );

  }

  public void testDateRegexDateMathFormatted() {

    doCompare( "DATEMATH('+1:MS;MM,yyyy-dd')", "+1:MS;MM,yyyy-dd" );
    doCompare( "DATEMATH(\"0:DS +12:h ; yyyy-MM-dd\")", "0:DS +12:h ; yyyy-MM-dd" );
    doCompare( "DATEMATH( '-10:Y 0:MS ;yyyy-MM-dd hh:mm:ss' )", "-10:Y 0:MS ;yyyy-MM-dd hh:mm:ss" );

  }

  private void doCompare( String exp ) {
    if ( exp.indexOf( ';' ) == -1 ) {
      doCompare( exp, exp + ";yyyy-MM-dd" );
    } else {
      doCompare( exp, exp );
    }
  }

  private void doCompare( String exp, String exp2 ) {

    Properties props = new Properties();
    // props.put( "dummy", exp );

    String template = "{" + exp + "}";
    String ref = DateMath.calculateDateString( null, exp2.replace( '=', ':' ).replace( '_', ' ' ) );
    String value = TemplateUtil.applyTemplate( template, props, (IParameterResolver) null );

    template = "{DATEMATH:var}";
    props.put( "var", exp );
    String value2 = TemplateUtil.applyTemplate( template, props, (IParameterResolver) null );

    assertNotNull( "Date was null", value );
    assertNotNull( "Date was null", value2 );
    assertEquals( "Dates do not match", ref, value );
    assertEquals( "Dates do not match", ref, value2 );

  }

  public int resolveParameter( String template, String parameter, Matcher parameterMatcher, int copyStart,
      StringBuffer results ) {

    if ( parameter.equals( "PREPARE:name1" ) ) {
      results.append( "value1" );
    }
    return template.length();

  }

  public void testTableTemplate() {

    IRuntimeContext mockRuntimeContext = mock( IRuntimeContext.class );
    IPentahoSession mockSession = mock( IPentahoSession.class );
    IParameterManager mockParameterManager = mock( IParameterManager.class );
    IPentahoResultSet mockPentahoResultSet = mock( IPentahoResultSet.class );
    Object[] mockRow = new Object[] { "field0", "field1" };
    when( mockPentahoResultSet.getColumnCount() ).thenReturn( mockRow.length );
    when( mockPentahoResultSet.getDataRow( 0 ) ).thenReturn( mockRow );
    when( mockParameterManager.getCurrentInputNames() ).thenReturn(
        new HashSet<Object>( Arrays.asList( new String[] { "param1" } ) ) );
    when( mockRuntimeContext.getSession() ).thenReturn( mockSession );
    when( mockRuntimeContext.getParameterManager() ).thenReturn( mockParameterManager );
    when( mockRuntimeContext.getInputParameterValue( "param1" ) ).thenReturn( mockPentahoResultSet );

    String template = "table {param1:col:1} and text";
    IParameterResolver resolver = mock( IParameterResolver.class );

    assertEquals( "table field1 and text", TemplateUtil.applyTemplate( template, mockRuntimeContext, resolver ) );

  }

  public void testKeyedTableTemplate() {

    IRuntimeContext mockRuntimeContext = mock( IRuntimeContext.class );
    IPentahoSession mockSession = mock( IPentahoSession.class );
    IParameterManager mockParameterManager = mock( IParameterManager.class );
    IPentahoResultSet mockPentahoResultSet = createMockResultSet();
    IPentahoMetaData mockPentahoMetaData = mock( IPentahoMetaData.class );
    final Set inputNames = new HashSet<Object>( Arrays.asList( new String[] { "param1" } ) );
    when( mockParameterManager.getCurrentInputNames() ).thenReturn( inputNames );
    when( mockRuntimeContext.getSession() ).thenReturn( mockSession );
    when( mockRuntimeContext.getParameterManager() ).thenReturn( mockParameterManager );
    when( mockRuntimeContext.getInputParameterValue( "param1" ) ).thenReturn( mockPentahoResultSet );
    when( mockRuntimeContext.getInputNames() ).thenReturn( inputNames );

    String template = "{param1:keycol:key_Value:valcol:defaultValue}"; // "key_value" is parsed as "key value"

    assertEquals( "field Value", TemplateUtil.applyTemplate( template, mockRuntimeContext ) );

  }

  public void testGetSystemInput() {
    final String USER_NAME = "userName";
    IRuntimeContext mockRuntimeContext = mock( IRuntimeContext.class );
    IPentahoSession mockSession = mock( IPentahoSession.class );
    when( mockRuntimeContext.getSession() ).thenReturn( mockSession );
    when( mockSession.getName() ).thenReturn( USER_NAME );

    assertEquals( USER_NAME, TemplateUtil.getSystemInput( "$user", mockRuntimeContext ) );
  }

  public void testApplyTemplateNameValue() {
    assertEquals( "bland{bar}", TemplateUtil.applyTemplate( "{foo}and{bar}", "foo", "bl" ) );
  }

  public void testApplyTemplateNameValues() {
    // String Array tests
    assertEquals( "{foo}and{bar}", TemplateUtil.applyTemplate( "{foo}and{bar}", "foo", (String[]) null ) );
    assertEquals( "bland{bar}", TemplateUtil.applyTemplate( "{foo}and{bar}", "foo", new String[] { "bl" } ) );
    assertEquals( "bland&brand", TemplateUtil.applyTemplate( "{foo}and", "foo", new String[] { "bl", "br" } ) );
  }

  public void testGetProperty() {
    IRuntimeContext mockRuntimeContext = mock( IRuntimeContext.class );
    IPentahoSession mockSession = mock( IPentahoSession.class );
    IParameterManager mockParameterManager = mock( IParameterManager.class );
    when( mockRuntimeContext.getParameterManager() ).thenReturn( mockParameterManager );
    when( mockRuntimeContext.getSession() ).thenReturn( mockSession );
    // Load up various parameter types to target code sections
    Map<String, IActionParameter> paramMap = new HashMap<String, IActionParameter>();
    IActionParameter param1 = new ActionParameter( "param1", "String", "one", null, "defone" );
    paramMap.put( "param1", param1 );
    Map<String, Object> inputMap = new HashMap<String, Object>();
    inputMap.putAll( paramMap );
    inputMap.put( "param2", "two" );
    inputMap.put( "param3", new TestObject( "three" ) );
    inputMap.put( "param4", new TestObject[] { new TestObject( "four-0" ), new TestObject( "four-1" ) } );
    inputMap.put( "param5", createMockResultSet() );
    // Set up a captor to return the appropriate parameter
    ArgumentCaptor<String> paramNameArgument = ArgumentCaptor.forClass( String.class );
    when( mockRuntimeContext.getInputParameterValue( paramNameArgument.capture() ) ).thenAnswer( new Answer() {
      public Object answer( InvocationOnMock invocation ) {
        return inputMap.get( paramNameArgument.getValue() );
      }
    } );
    when( mockParameterManager.getCurrentInputNames() ).thenReturn( paramMap.keySet() );
    when( mockParameterManager.getAllParameters() ).thenReturn( paramMap );
    when( mockRuntimeContext.getInputNames() ).thenReturn( inputMap.keySet() );

    // Now we can test
    assertEquals( "one", TemplateUtil.applyTemplate( "{param1}", mockRuntimeContext ) ); // action parameter
    assertEquals( "two", TemplateUtil.applyTemplate( "{param2}", mockRuntimeContext ) ); // simple String
    assertEquals( "three", TemplateUtil.applyTemplate( "{param3}", mockRuntimeContext ) ); // single arbitrary object
    assertEquals( "four-0','four-1", TemplateUtil.applyTemplate( "{param4}", mockRuntimeContext ) ); // array of
                                                                                                     // arbitrary objects
    assertEquals( "key Value", TemplateUtil.applyTemplate( "{param5}", mockRuntimeContext ) ); // result set
  }

  private IPentahoResultSet createMockResultSet() {
    final Object[] mockRow = new Object[] { "key Value", "field Value" };
    IPentahoResultSet mockPentahoResultSet = mock( IPentahoResultSet.class );
    IPentahoMetaData mockPentahoMetaData = mock( IPentahoMetaData.class );
    when( mockPentahoResultSet.getColumnCount() ).thenReturn( mockRow.length );
    when( mockPentahoResultSet.getDataRow( 0 ) ).thenReturn( mockRow );
    when( mockPentahoResultSet.getMetaData() ).thenReturn( mockPentahoMetaData );
    when( mockPentahoResultSet.getRowCount() ).thenReturn( 1 );
    when( mockPentahoResultSet.getValueAt( 0, 0 ) ).thenReturn( mockRow[0] );
    when( mockPentahoResultSet.getValueAt( 0, 1 ) ).thenReturn( mockRow[1] );
    when( mockPentahoMetaData.getColumnIndex( "keycol" ) ).thenReturn( 0 );
    when( mockPentahoMetaData.getColumnIndex( "valcol" ) ).thenReturn( 1 );
    return mockPentahoResultSet;
  }

  private class TestObject {
    private String toStringValue;

    public TestObject( String toStringValue ) {
      this.toStringValue = toStringValue;
    }

    public String toString() {
      return toStringValue;
    }
  }

}
