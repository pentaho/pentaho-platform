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
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogUtilTest {

  @Test
  void addAppenderWithLevelCapturesInfoAndRestoresThePreviousConfiguration() {
    String loggerName = "org.pentaho.test.dynamic." + UUID.randomUUID();
    Logger logger = LogManager.getLogger( loggerName );
    LoggerContext context = (LoggerContext) LogManager.getContext( false );
    Configuration configuration = context.getConfiguration();
    StringWriter output = new StringWriter();
    Appender appender = LogUtil.makeAppender( loggerName + ".appender", output, "%m" );

    try ( LogUtil.AppenderRegistration ignored =
            LogUtil.addAppenderWithLevel( appender, logger, Level.INFO, null ) ) {
      logger.info( "application_123456789_0001" );

      assertEquals( "application_123456789_0001", output.toString() );
      assertEquals( Level.INFO, configuration.getLoggers().get( loggerName ).getLevel() );
    }

    assertFalse( configuration.getLoggers().containsKey( loggerName ) );
  }

  @Test
  void addAppenderWithLevelRestoresAnExistingLoggerLevel() {
    String loggerName = "org.pentaho.test.existing." + UUID.randomUUID();
    LoggerContext context = (LoggerContext) LogManager.getContext( false );
    Configuration configuration = context.getConfiguration();
    LoggerConfig loggerConfig = new LoggerConfig( loggerName, Level.ERROR, true );
    configuration.addLogger( loggerName, loggerConfig );
    context.updateLoggers();

    Appender appender = LogUtil.makeAppender( loggerName + ".appender", new StringWriter(), "%m" );
    try ( LogUtil.AppenderRegistration ignored =
            LogUtil.addAppenderWithLevel( appender, LogManager.getLogger( loggerName ), Level.INFO, null ) ) {
      assertEquals( Level.INFO, loggerConfig.getLevel() );
      assertFalse( loggerConfig.isAdditive() );
    }

    try {
      assertEquals( Level.ERROR, loggerConfig.getLevel() );
      assertTrue( loggerConfig.isAdditive() );
    } finally {
      configuration.removeLogger( loggerName );
      context.updateLoggers();
    }
  }

  @Test
  void addAppenderWithLevelRestoresAnExistingLoggerOnlyAfterTheLastRegistrationCloses() {
    String loggerName = "org.pentaho.test.overlapping." + UUID.randomUUID();
    LoggerContext context = (LoggerContext) LogManager.getContext( false );
    Configuration configuration = context.getConfiguration();
    LoggerConfig loggerConfig = new LoggerConfig( loggerName, Level.ERROR, true );
    configuration.addLogger( loggerName, loggerConfig );
    context.updateLoggers();

    Appender firstAppender = LogUtil.makeAppender( loggerName + ".first", new StringWriter(), "%m" );
    Appender secondAppender = LogUtil.makeAppender( loggerName + ".second", new StringWriter(), "%m" );
    LogUtil.AppenderRegistration firstRegistration =
      LogUtil.addAppenderWithLevel( firstAppender, LogManager.getLogger( loggerName ), Level.DEBUG, null );
    LogUtil.AppenderRegistration secondRegistration =
      LogUtil.addAppenderWithLevel( secondAppender, LogManager.getLogger( loggerName ), Level.INFO, null );

    try {
      assertEquals( Level.DEBUG, loggerConfig.getLevel() );

      firstRegistration.close();

      assertEquals( Level.INFO, loggerConfig.getLevel() );
      assertFalse( loggerConfig.isAdditive() );

      secondRegistration.close();

      assertEquals( Level.ERROR, loggerConfig.getLevel() );
      assertTrue( loggerConfig.isAdditive() );
    } finally {
      firstRegistration.close();
      secondRegistration.close();
      configuration.removeLogger( loggerName );
      context.updateLoggers();
    }
  }

  @Test
  void addAppenderWithLevelDoesNotPropagateToParentAppenders() {
    String parentLoggerName = "org.pentaho.test.parent." + UUID.randomUUID();
    String loggerName = parentLoggerName + ".child";
    LoggerContext context = (LoggerContext) LogManager.getContext( false );
    Configuration configuration = context.getConfiguration();
    LoggerConfig parentLoggerConfig = new LoggerConfig( parentLoggerName, Level.INFO, true );
    StringWriter parentOutput = new StringWriter();
    Appender parentAppender = LogUtil.makeAppender( parentLoggerName + ".appender", parentOutput, "%m" );
    parentAppender.start();
    configuration.addAppender( parentAppender );
    parentLoggerConfig.addAppender( parentAppender, Level.INFO, null );
    configuration.addLogger( parentLoggerName, parentLoggerConfig );
    context.updateLoggers();

    StringWriter childOutput = new StringWriter();
    Appender childAppender = LogUtil.makeAppender( loggerName + ".appender", childOutput, "%m" );
    Logger logger = LogManager.getLogger( loggerName );

    try ( LogUtil.AppenderRegistration ignored =
            LogUtil.addAppenderWithLevel( childAppender, logger, Level.INFO, null ) ) {
      logger.info( "application_123456789_0002" );

      assertEquals( "application_123456789_0002", childOutput.toString() );
      assertEquals( "", parentOutput.toString() );
      assertFalse( configuration.getLoggers().get( loggerName ).isAdditive() );
    } finally {
      configuration.removeLogger( parentLoggerName );
      configuration.getAppenders().remove( parentAppender.getName() );
      parentAppender.stop();
      context.updateLoggers();
    }

    assertTrue( parentLoggerConfig.isAdditive() );
  }
}