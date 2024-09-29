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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action.xmla;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class XMLABaseComponentTest {

  private boolean runMethodDetermineProvider( String param ) {
    XMLABaseComponent mock = mock( XMLABaseComponent.class );

    try {
      Method method = XMLABaseComponent.class.getDeclaredMethod( "determineProvider", String.class );
      method.setAccessible( true );
      method.invoke( mock, param );
    } catch ( Exception e ) {
      return false;
    }
    return true;
  }

  @Test
  public void testCase1() throws Exception {
    Assert.assertTrue( "error during method invocation", runMethodDetermineProvider( "PROVIDER=MONDRIAN" ) );
  }

  @Test
  public void testCase2() throws Exception {
    Locale.setDefault( Locale.US );
    Assert.assertTrue( "error during method invocation", runMethodDetermineProvider( "provider=mondrian" ) );
  }

  @Test
  public void testCase3() throws Exception {
    Locale.setDefault( new Locale( "tr" ) );
    Assert.assertTrue( "error during method invocation", runMethodDetermineProvider( "provider=mondrian" ) );
  }

}
