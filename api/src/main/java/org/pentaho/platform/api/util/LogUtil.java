package org.pentaho.platform.api.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.StringWriter;
import java.io.Writer;

public class LogUtil {

  /**
   * Adds an appender to a logger creating a LoggerConfig if necessary
   * so that the appender only applies to the logger and not parent loggers.
   * 
   * @param appender
   * @param logger
   * @param level
   */
  public static void addAppender( Appender appender, Logger logger, Level level ) {
    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    appender.start();
    config.addAppender( appender );

    LoggerConfig loggerConfig = config.getLoggerConfig( logger.getName() );
    LoggerConfig specificConfig = loggerConfig;

    if ( !loggerConfig.getName().equals( logger.getName() ) ) {
      specificConfig = new LoggerConfig( logger.getName(), null, true );
      specificConfig.setParent( loggerConfig );
      config.addLogger( logger.getName(), specificConfig );
    }

    specificConfig.addAppender( appender, level, null );
    ctx.updateLoggers();
  }

  public static void removeAppender( Appender appender, Logger logger ) {
    appender.stop();
    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig( logger.getName() );
    loggerConfig.removeAppender( appender.getName() );
    ctx.updateLoggers();
  }

  public static Appender makeAppender( String name, StringWriter sw, String layout ) {
    return WriterAppender.newBuilder().setName( name )
        .setLayout( PatternLayout.newBuilder().withPattern( layout ).build() ).setTarget( sw ).build();
  }

  public static Appender makeAppender( String name, Writer writer, Layout layout ) {
    return WriterAppender.newBuilder().setName( name ).setLayout( layout ).setTarget( writer ).build();
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
   * Returns true if the specific logger has been configured such as defined
   * in log4j2.xml
   * 
   * @param logger
   * @return
   */
  public static boolean exists( String logger ) {
    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    return config.getLoggers().keySet().contains( logger );
  }
}
