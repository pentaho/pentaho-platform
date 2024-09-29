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

package org.pentaho.platform.osgi;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.List;

import org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleDataFactoryModuleInitializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

  @Test
  public void testDefaultUsingDisregardClassName() throws Exception {
    provider = new ExceptionBasedClientTypeProvider();
    provider.setTargetClass( ExceptionBasedClientTypeProviderTest.class );
    provider.setDisregardClass( ExceptionBasedClientTypeProviderTest.class );
    ExceptionBasedHelper helper = new ExceptionBasedHelper();
    helper.callme( this );
  }

  @Test
  public void testDefaultDisregardClassNamesContainsKettleDataFactoryModuleInitializer() throws Exception {
    provider = new ExceptionBasedClientTypeProviderForTesting();
    assertTrue( ( (ExceptionBasedClientTypeProviderForTesting) provider ).getDisregardClassNames() != null );
    assertTrue( ( (ExceptionBasedClientTypeProviderForTesting) provider ).getDisregardClassNames().contains(
      KettleDataFactoryModuleInitializer.class.getName() ) );
  }

  @Test
  public void testDefaultTargetClassNamesContainsKettleEnvironments() throws Exception {
    provider = new ExceptionBasedClientTypeProviderForTesting();
    assertTrue( ( (ExceptionBasedClientTypeProviderForTesting) provider ).getTargetClassNames() != null );
    assertTrue( ( (ExceptionBasedClientTypeProviderForTesting) provider ).getTargetClassNames().contains(
      KettleEnvironment.class.getName() ) );
    assertTrue( ( (ExceptionBasedClientTypeProviderForTesting) provider ).getTargetClassNames().contains(
      KettleClientEnvironment.class.getName() ) );
  }

  public void callback() {
    String clientType = provider.getClientType();
    assertEquals( "exceptionbasedhelper", clientType );
  }

  /**
   * Extending ExceptionBasedClientTypeProvider with public getters for the protected attributes
   */
  private class ExceptionBasedClientTypeProviderForTesting extends ExceptionBasedClientTypeProvider {

    public List<String> getTargetClassNames() {
      return targetClassNames;
    }

    public List<String> getDisregardClassNames() {
      return disregardClassNames;
    }
  }
}
