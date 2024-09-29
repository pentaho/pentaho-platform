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
 * Copyright (c) 2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.api.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Various utility methods for working with log4j2
 *
 */
public class LogUtil {

  /**
   * Adds an appender to a logger creating a LoggerConfig if necessary so that the appender only listens to the
   * specified logger and not parent loggers.
   * 
   * @param appender
   * @param logger
   * @param level
   *          - Set to null if appender should log all events
   */
  public static void addAppender( Appender appender, Logger logger, Level level ) {
    addAppender( appender, logger, level, null );
  }

  public static void addAppender( Appender appender, Logger logger, Level level, Filter filter ) {
    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    appender.start();
    config.addAppender( appender );

    LoggerConfig loggerConfig = config.getLoggerConfig( logger.getName() );
    LoggerConfig specificConfig = loggerConfig;

    if ( !loggerConfig.getName().equals( logger.getName() ) ) {
      // With log4j2, Logger and LoggerConfig are separate objects so if you attach an appender to a
      // Logger, you are actually attaching to the logger's LoggerConfig which could point to a parent logger.
      // For example, assuming a logger "foo.bar" isn't configured but it's parent "foo" is configured. When
      // retrieving the LoggerConfig for "foo.bar", you're going to get a LoggerConfig for "foo". If you attach the
      // appender to "foo" LoggerConfig, then your appender will end up receiving "foo" events (in addition to "foo.bar"
      // events).mvn cle
      specificConfig = new LoggerConfig( logger.getName(), null, true );
      specificConfig.setParent( loggerConfig );
      config.addLogger( logger.getName(), specificConfig );
    }

    specificConfig.addAppender( appender, level, filter );
    ctx.updateLoggers();
  }

  public static void removeAppender( Appender appender, Logger logger ) {
    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig( logger.getName() );
    loggerConfig.removeAppender( appender.getName() );
    ctx.updateLoggers();
    appender.stop();
  }

  public static Appender makeAppender( String name, StringWriter sw, String layout ) {
    return WriterAppender.newBuilder().setName( name )
        .setLayout( PatternLayout.newBuilder().withPattern( layout ).build() ).setTarget( sw ).build();
  }

  public static Appender makeAppender( String name, Writer writer, Layout layout ) {
    return WriterAppender.newBuilder().setName( name ).setLayout( layout ).setTarget( writer ).build();
  }

  public static Appender makeAppender(String name, Writer writer, Filter filter, Layout layout ) {
    return WriterAppender.newBuilder().setName( name ).setFilter(filter).setLayout( layout ).setTarget( writer ).build();
  }
  /**
   * Sets level of a logger creating a logger specific LoggerConfig if necessary.
   * 
   * @param logger
   * @param level
   */
  public static void setLevel( Logger logger, Level level ) {
    Configurator.setLevel( logger.getName(), level );
  }

  public static void setRootLoggerLevel( Level level ) {
    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig( LogManager.ROOT_LOGGER_NAME );
    loggerConfig.setLevel( level );
    ctx.updateLoggers();
  }

  /**
   * Returns true if the specific logger has been configured such as defined in log4j2.xml
   * 
   * @param logger
   * @return
   */
  public static boolean exists( String logger ) {
    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    return config.getLoggers().keySet().contains( logger );
  }

  /**
   * Returns true if appender is attached to logger.
   * 
   * @param logger
   * @param appender
   * @return
   */
  public static boolean isAttached( Logger logger, Appender appender ) {
    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig( logger.getName() );
    return loggerConfig.getAppenders().containsKey( appender.getName() );
  }

  public static Map<String, Appender> getAppenders( Logger logger ) {
    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig( logger.getName() );
    return loggerConfig.getAppenders();
  }
}
