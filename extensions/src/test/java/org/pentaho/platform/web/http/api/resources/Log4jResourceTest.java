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

package org.pentaho.platform.web.http.api.resources;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class Log4jResourceTest {

  private static final String CONFIG = "log4j.xml";
  private Log4jResource target = new Log4jResource();

  @Test
  public void resetLogLevel() throws Exception {
    Logger root = LogManager.getRootLogger();
    Level initialLevel = root.getLevel();
    assertNotEquals( initialLevel, Level.ALL );
    root.setLevel( Level.ALL );
    assertEquals( Level.ALL, root.getLevel() );

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
    Enumeration e = LogManager.getCurrentLoggers();
    while ( e.hasMoreElements() ) {
      Logger logger = (Logger) e.nextElement();
      if ( !Level.DEBUG.equals( logger.getLevel() ) ) {
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
    DOMConfigurator.configure( Loader.getResource( CONFIG ) );
  }

}
