/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services;

import junit.framework.TestCase;
import org.pentaho.platform.api.engine.IParameterResolver;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.util.DateMath;

import java.util.Properties;
import java.util.regex.Matcher;

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

}
