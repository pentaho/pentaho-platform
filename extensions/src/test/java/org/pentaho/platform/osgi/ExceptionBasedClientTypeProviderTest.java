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
