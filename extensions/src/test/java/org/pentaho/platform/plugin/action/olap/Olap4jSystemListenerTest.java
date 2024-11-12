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


package org.pentaho.platform.plugin.action.olap;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.util.KettlePasswordService;
import org.pentaho.platform.util.PasswordHelper;

import java.util.Arrays;
import java.util.Properties;

public class Olap4jSystemListenerTest {
  @Test
  public void testStartupRegistersAndRemovesOlapConnections() throws Exception {
    final IOlapService mockOlapService = Mockito.mock( IOlapService.class );
    final IPentahoSession mockSession = Mockito.mock( IPentahoSession.class );
    Olap4jSystemListener listener = new Olap4jSystemListener() {
      @Override IOlapService getOlapService( IPentahoSession session ) {
        assertSame( mockSession, session );
        return mockOlapService;
      }

      @Override PasswordHelper getPasswordHelper() {
        return new PasswordHelper( new KettlePasswordService() );
      }
    };
    final IPentahoSession oSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession( mockSession );
    try {
      Properties properties1 = makeProperties(
        "aName", "idk", "jdbc:mongolap:host=remote;user=admin;Password=ENC:Encrypted 2be98afc86aa7f2e4cb79ce71da9fa6d4;port=1234",
        "aUser", "ENC:Encrypted 2be98afc86aa7f2859b18bd63c99dbdde" );
      Properties properties2 = makeProperties(
        "bName", "istilldk", "jdbc:mongolap:host=remoteb;user=admin;password=admin;port=1234", "bUser", "bPassword" );
      listener.setOlap4jConnectionList( Arrays.asList( properties1, properties2 ) );
      listener.setOlap4jConnectionRemoveList( Arrays.asList( "defunctConnection", "worthless" ) );
      listener.startup( mockSession );
      Mockito.verify( mockOlapService )
        .addOlap4jCatalog(
          "aName", "idk", "jdbc:mongolap:host=remote; user=admin; Password=admin; port=1234", "aUser", "aPassword",
          new Properties(), true, mockSession );
      Mockito.verify( mockOlapService )
        .addOlap4jCatalog(
          "bName", "istilldk", "jdbc:mongolap:host=remoteb; user=admin; password=admin; port=1234", "bUser",
          "bPassword", new Properties(), true, mockSession );
      Mockito.verify( mockOlapService ).removeCatalog( "defunctConnection", mockSession );
      Mockito.verify( mockOlapService ).removeCatalog( "worthless", mockSession );
    } finally {
      PentahoSessionHolder.setSession( oSession );
    }

  }

  @Test
  public void testExceptionsDoNotStopSystemLoad() throws Exception {
    final IOlapService mockOlapService = Mockito.mock( IOlapService.class );
    final IPentahoSession mockSession = Mockito.mock( IPentahoSession.class );
    Olap4jSystemListener listener = new Olap4jSystemListener() {
      @Override IOlapService getOlapService( IPentahoSession session ) {
        assertSame( mockSession, session );
        return mockOlapService;
      }

      @Override PasswordHelper getPasswordHelper() {
        return new PasswordHelper( new KettlePasswordService() );
      }
    };
    Properties properties1 = makeProperties( "aName", "idk", "jdbc:mongolap:host=remote", "aUser", "aPassword" );
    listener.setOlap4jConnectionList( Arrays.asList( properties1 ) );
    listener.setOlap4jConnectionRemoveList( Arrays.asList( "defunctConnection" ) );
    Mockito.doThrow( new RuntimeException( "something terrible happened" ) )
      .when( mockOlapService )
      .addOlap4jCatalog(
        "aName", "idk", "jdbc:mongolap:host=remote", "aUser", "aPassword", new Properties(), true, mockSession );
    Mockito.doThrow( new RuntimeException( "something amazing happend" ) )
      .when( mockOlapService )
      .removeCatalog( "defunctConnection", mockSession );
    assertTrue( listener.startup( mockSession ) );
  }

  private Properties makeProperties(
    String name, String className, String connectString, String user, String password )
  {
    Properties properties = new Properties();
    properties.setProperty( "name", name );
    properties.setProperty( "className", className );
    properties.setProperty( "connectString", connectString );
    properties.setProperty( "user", user );
    properties.setProperty( "password", password );
    return properties;
  }
}
