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

package org.pentaho.platform.uifoundation.chart;

import mockit.Deencapsulation;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.util.logging.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@RunWith( JMockit.class )
public class ChartHelperTest extends Assert {

  @Test
  @SuppressWarnings( "deprecation" )
  public void deprecateWarningTest() {

    final Class<?> deprecatedClass = ChartHelper.class;
    final Method[] methods = deprecatedClass.getDeclaredMethods();
    final List<Method> notPrivateMethods = new ArrayList<Method>( methods.length );

    for ( Method m : methods ) {
      if ( !Modifier.isPrivate( m.getModifiers() ) ) {
        notPrivateMethods.add( m );
      }
    }

    new NonStrictExpectations( Logger.class ) {
      {
        Logger.warn( deprecatedClass, withSubstring( "deprecated" ) );
        result = new Exception();
        times = notPrivateMethods.size();
      }
    };

    for ( Method m : notPrivateMethods ) {
      try {
        Deencapsulation.invoke( deprecatedClass, m.getName(), (Object[]) m.getParameterTypes() );
        fail();
      } catch ( Exception e ) {
        System.out.println( MessageFormat.format( "Method {0}.{1}(..) sucessfully deprecated", deprecatedClass
            .getCanonicalName(), m.getName() ) );
      }
    }

  }
}
