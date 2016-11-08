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
package org.pentaho.platform.engine.core.system.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
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
    appender = new TestAppender();
    logger = Logger.getRootLogger();
    logger.addAppender( appender );
    serverStatusProvider = IServerStatusProvider.LOCATOR.getProvider();
  }

  @After
  public void shutdown() {
    logger.removeAppender( appender );
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

    final List<LoggingEvent> log = appender.getLog();
    assertTrue( "log size was " + log.size() + ". Excpected it to be greater than 3.", log.size() > 3 ); // Should be at least 2 messages
    assertEquals( TEST_MESSAGE1, log.get( 0 ).getMessage() );
    assertEquals( TEST_MESSAGE1, log.get( 1 ).getMessage() );
    assertEquals( TEST_MESSAGE2, log.get( 2 ).getMessage() );

    try {
      PeriodicStatusLogger.stop();
      fail( "Attempt to stop unstarted logger did not throw exception" );
    } catch ( IllegalStateException e ) {
      // This is what should happen
    }
  }

  public class TestAppender extends AppenderSkeleton {
    private final List<LoggingEvent> log = new ArrayList<LoggingEvent>();

    @Override
    public boolean requiresLayout() {
      return false;
    }

    @Override
    protected void append( final LoggingEvent loggingEvent ) {
      log.add( loggingEvent );
    }

    @Override
    public void close() {
    }

    public List<LoggingEvent> getLog() {
      return new ArrayList<LoggingEvent>( log );
    }
  }

}
