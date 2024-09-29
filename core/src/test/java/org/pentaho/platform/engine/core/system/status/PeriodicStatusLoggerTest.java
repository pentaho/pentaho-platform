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

package org.pentaho.platform.engine.core.system.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IServerStatusProvider;

/**
 * 
 * @author tkafalas
 *
 */
public class PeriodicStatusLoggerTest {
  private final String TEST_MESSAGE1 = "This is a test";
  private final String TEST_MESSAGE2 = "another message";
  private TestAppender appender;
  private static Logger logger;
  IServerStatusProvider serverStatusProvider;

  @Before
  public void setup() {
    logger = LogManager.getRootLogger();
    appender = new TestAppender();
    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    appender.start();
    config.addAppender( appender );
    LoggerConfig loggerConfig = config.getLoggerConfig( logger.getName() );
    loggerConfig.addAppender( appender, Level.DEBUG, null );
    ctx.updateLoggers();
    serverStatusProvider = IServerStatusProvider.LOCATOR.getProvider();
  }

  @After
  public void shutdown() {
    appender.stop();
    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig( logger.getName() );
    loggerConfig.removeAppender( appender.getName() );
    ctx.updateLoggers();
  }

  @Test
  public void test() throws Exception {
    serverStatusProvider.setStatusMessages( new String[] { TEST_MESSAGE1 } );
    PeriodicStatusLogger.setCycleTime( 500 ); // We want a fast test
    assertEquals( 500, PeriodicStatusLogger.getCycleTime() );
    serverStatusProvider.setStatus( IServerStatusProvider.ServerStatus.STARTING );
    PeriodicStatusLogger.start();
    assertEquals( TEST_MESSAGE1, PeriodicStatusLogger.getStatusMessages()[0] );
    assertEquals( IServerStatusProvider.ServerStatus.STARTING, PeriodicStatusLogger.getServerStatus() );
    Thread.sleep( 600 );
    assertEquals( 2, appender.getLog().size() );
    serverStatusProvider.setStatusMessages( new String[] { TEST_MESSAGE2 } );
    Thread.sleep( 600 );
    try {
      PeriodicStatusLogger.start();
      fail( "Attempt to start status logger twice did not throw exception" );
    } catch ( IllegalStateException e ) {
      // This is what should happen
    }
    PeriodicStatusLogger.stop();

    final List<String> log = appender.getLog();
    assertTrue( "log size was " + log.size() + ". Excpected it to be greater than 3.", log.size() > 3 ); // Should be at
                                                                                                         // least 2
                                                                                                         // messages
    assertEquals( TEST_MESSAGE1, log.get( 0 ) );
    assertEquals( TEST_MESSAGE1, log.get( 1 ) );
    assertEquals( TEST_MESSAGE2, log.get( 2 ) );

    try {
      PeriodicStatusLogger.stop();
      fail( "Attempt to stop unstarted logger did not throw exception" );
    } catch ( IllegalStateException e ) {
      // This is what should happen
    }
  }

  public class TestAppender extends AbstractAppender  {
    private final List<String> log = new ArrayList<>();

    protected TestAppender() {
      super("TestAppender", null, PatternLayout.createDefaultLayout(), true, Property.EMPTY_ARRAY);
    }

    @Override
    public void append( final LogEvent loggingEvent ) {
      log.add( loggingEvent.getMessage().getFormattedMessage() );
    }

    public List<String> getLog() {
      return log;
    }
  }

}
