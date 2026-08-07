/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.api.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogUtilTest {

  @Test
  void removeAppenderUnregistersAppenderFromConfiguration() {
    String loggerName = "org.pentaho.test.remove." + UUID.randomUUID();
    Logger logger = LogManager.getLogger( loggerName );
    LoggerContext context = (LoggerContext) LogManager.getContext( false );
    Configuration configuration = context.getConfiguration();
    Appender appender = LogUtil.makeAppender( loggerName + ".appender", new StringWriter(), "%m" );

    LogUtil.addAppender( appender, logger, Level.INFO );
    assertTrue( configuration.getAppenders().containsKey( appender.getName() ) );

    LogUtil.removeAppender( appender, logger );

    assertFalse( configuration.getAppenders().containsKey( appender.getName() ) );
  }

  @Test
  void removeAppenderStopsOnlyAfterLastLoggerDetaches() {
    String loggerName = "org.pentaho.test.remove.shared." + UUID.randomUUID();
    Logger firstLogger = LogManager.getLogger( loggerName + ".first" );
    Logger secondLogger = LogManager.getLogger( loggerName + ".second" );
    LoggerContext context = (LoggerContext) LogManager.getContext( false );
    Configuration configuration = context.getConfiguration();
    Appender appender = LogUtil.makeAppender( loggerName + ".appender", new StringWriter(), "%m" );

    LogUtil.addAppender( appender, firstLogger, Level.INFO );
    LogUtil.addAppender( appender, secondLogger, Level.INFO );

    LogUtil.removeAppender( appender, firstLogger );

    assertTrue( configuration.getAppenders().containsKey( appender.getName() ) );
    assertTrue( configuration.getLoggerConfig( secondLogger.getName() ).getAppenders()
      .containsKey( appender.getName() ) );

    LogUtil.removeAppender( appender, secondLogger );

    assertFalse( configuration.getAppenders().containsKey( appender.getName() ) );
  }
}
