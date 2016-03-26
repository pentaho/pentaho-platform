/*
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
 * Copyright 2016 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.osgi;

import org.junit.Test;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import static org.junit.Assert.assertEquals;

/**
 * Created by nbaker on 3/25/16.
 */
public class ExceptionBasedClientTypeProviderTest {

  private ExceptionBasedClientTypeProvider provider;

  @Test
  public void testGetClientType() throws Exception {
    provider = new ExceptionBasedClientTypeProvider();
    provider.setTargetClass( ExceptionBasedClientTypeProviderTest.class );
    ExceptionBasedHelper helper = new ExceptionBasedHelper();
    helper.callme( this );
  }

  @Test
  /**
   * In this case the stacktrace does not contain the target class, "default" should return
   */
  public void testDefault() throws Exception {
    provider = new ExceptionBasedClientTypeProvider();
    provider.setTargetClass( PentahoSystem.class );
    String clientType = provider.getClientType();
    assertEquals( "default", clientType );
  }

  public void callback() {
    String clientType = provider.getClientType();
    assertEquals( "exceptionbasedhelper", clientType );
  }
}
