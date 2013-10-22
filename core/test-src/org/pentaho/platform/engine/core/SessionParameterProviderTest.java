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

package org.pentaho.platform.engine.core;

import junit.framework.TestCase;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;

@SuppressWarnings( { "all" } )
public class SessionParameterProviderTest extends TestCase {

  public void test1() {

    StandaloneSession session = new StandaloneSession( "test name" );

    PentahoSessionParameterProvider params = new PentahoSessionParameterProvider( session );

    session.setAttribute( "param1", "value1" );

    assertEquals( "Wrong param value", "test name", params.getParameter( "name" ) );
    assertEquals( "Wrong param value", "value1", params.getParameter( "param1" ) );
    assertEquals( "Wrong param value", "value1", params.getStringParameter( "param1", null ) );
    assertEquals( "Wrong param value", null, params.getStringParameter( "bogus", null ) );

  }

}
