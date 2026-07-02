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


package org.pentaho.platform.web.http.api.resources;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.core.util.Loader;
//import org.apache.logging.log4j.core.xml.DOMConfigurator;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.util.LogUtil;

import jakarta.ws.rs.core.Response;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class Log4jResourceTest {

  private static final String CONFIG = "log4j.xml";
  private Log4jResource target = new Log4jResource();

  @Test
  public void resetLogLevel() throws Exception {

    Level initialLevel = LogManager.getRootLogger().getLevel();
    assertNotEquals( initialLevel, Level.ALL );
    LogUtil.setLevel(LogManager.getLogger(), Level.ALL);
    assertEquals( Level.ALL, LogManager.getLogger().getLevel() );

    Response res = target.reloadConfiguration();
    assertNotEquals( Level.ALL,  LogManager.getRootLogger().getLevel() );

    assertEquals( "Done", res.getEntity().toString() );
  }

  @Test
  public void updateLogLevel() throws Exception {
    Response res = target.updateLogLevel( null, null );
    assertEquals( 304, res.getStatus() );

    res = target.updateLogLevel( "debug", null );
    int notDebug = 0;
    LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
    for ( Logger l : logContext.getLoggers() ) {
      if ( !Level.DEBUG.equals( l.getLevel() ) ) {
        notDebug++;
      }
    }
    assertEquals( 0, notDebug );
    assertEquals( 200, res.getStatus() );
    assertEquals( "Log level updated.", res.getEntity().toString() );

    Level original = LogManager.getLogger( "org.pentaho" ).getLevel();
    assertNotEquals( Level.ALL, original );
    res = target.updateLogLevel( "ALL", "org.pentaho" );
    assertEquals( Level.ALL,  LogManager.getLogger( "org.pentaho" ).getLevel() );
    assertEquals( 200, res.getStatus() );
    assertEquals( "Setting log level for: 'org.pentaho' to be: ALL", res.getEntity().toString() );

    res = target.updateLogLevel( "ALL", "foo.bar" );
    assertEquals( 304, res.getStatus() );

    res = target.updateLogLevel( "ALL", "<script>alert('XSS')</script>" );
    assertEquals( 304, res.getStatus() );
    assertEquals( "[\"Category: '&lt;script&gt;alert(&#39;XSS&#39;)&lt;/script&gt;' not found, log level not modified.\"]",
            res.getMetadata().values().toArray()[0].toString() );
  }

  @Before
  public void readFromConfig() {
    //DOMConfigurator.configure( Loader.getResource( CONFIG ) );
  }

}
