/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



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
import java.util.HashMap;
import java.util.Map;

/**
 * Various utility methods for working with log4j2
 *
 */
public class LogUtil {

  private static final Map<LoggerConfigKey, ManagedLoggerConfig> MANAGED_LOGGER_CONFIGS = new HashMap<>();

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

  /**
   * Adds an appender to a logger and temporarily enables the supplied log level. Closing the returned registration
   * removes the appender and restores the logger's previous configuration.
   *
   * @param appender
   * @param logger
   * @param level
   * @param filter
   * @return a registration that must be closed when the appender is no longer needed
   */
  public static AppenderRegistration addAppenderWithLevel( Appender appender, Logger logger, Level level,
                                                            Filter filter ) {
    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    LoggerConfigKey key = new LoggerConfigKey( ctx, logger.getName() );

    synchronized ( MANAGED_LOGGER_CONFIGS ) {
      ManagedLoggerConfig managedLoggerConfig = MANAGED_LOGGER_CONFIGS.get( key );
      boolean newManagedLoggerConfig = managedLoggerConfig == null;
      if ( managedLoggerConfig == null ) {
        LoggerConfig loggerConfig = config.getLoggers().get( logger.getName() );
        boolean created = loggerConfig == null;
        Level previousLevel = null;
        boolean previousAdditivity = false;
        if ( created ) {
          LoggerConfig parentConfig = config.getLoggerConfig( logger.getName() );
          loggerConfig = new LoggerConfig( logger.getName(), level, false );
          loggerConfig.setParent( parentConfig );
          config.addLogger( logger.getName(), loggerConfig );
        } else {
          previousLevel = loggerConfig.getLevel();
          previousAdditivity = loggerConfig.isAdditive();
        }
        managedLoggerConfig = new ManagedLoggerConfig( config, loggerConfig, created, previousLevel,
          previousAdditivity );
      }

      try {
        managedLoggerConfig.addAppender( appender, level, filter );
        ctx.updateLoggers();
        if ( newManagedLoggerConfig ) {
          MANAGED_LOGGER_CONFIGS.put( key, managedLoggerConfig );
        }
      } catch ( RuntimeException e ) {
        managedLoggerConfig.removeAppender( appender );
        if ( newManagedLoggerConfig ) {
          managedLoggerConfig.restore( key.loggerName );
        }
        ctx.updateLoggers();
        throw e;
      }
    }
    return new AppenderRegistration( key, appender );
  }

  public static final class AppenderRegistration implements AutoCloseable {
    private final LoggerConfigKey key;
    private final Appender appender;
    private boolean closed;

    private AppenderRegistration( LoggerConfigKey key, Appender appender ) {
      this.key = key;
      this.appender = appender;
    }

    @Override
    public void close() {
      synchronized ( MANAGED_LOGGER_CONFIGS ) {
        if ( closed ) {
          return;
        }
        closed = true;

        ManagedLoggerConfig managedLoggerConfig = MANAGED_LOGGER_CONFIGS.get( key );
        if ( managedLoggerConfig != null ) {
          managedLoggerConfig.removeAppender( appender );
          if ( managedLoggerConfig.isEmpty() ) {
            managedLoggerConfig.restore( key.loggerName );
            MANAGED_LOGGER_CONFIGS.remove( key );
          }
          key.context.updateLoggers();
        }
      }
    }
  }

  private static final class ManagedLoggerConfig {
    private final Configuration configuration;
    private final LoggerConfig loggerConfig;
    private final boolean created;
    private final Level previousLevel;
    private final boolean previousAdditivity;
    private final Map<String, Appender> appenders = new HashMap<>();
    private final Map<String, Level> appenderLevels = new HashMap<>();

    private ManagedLoggerConfig( Configuration configuration, LoggerConfig loggerConfig, boolean created,
                                 Level previousLevel, boolean previousAdditivity ) {
      this.configuration = configuration;
      this.loggerConfig = loggerConfig;
      this.created = created;
      this.previousLevel = previousLevel;
      this.previousAdditivity = previousAdditivity;
    }

    private void addAppender( Appender appender, Level level, Filter filter ) {
      appender.start();
      configuration.addAppender( appender );
      loggerConfig.setAdditive( false );
      loggerConfig.addAppender( appender, level, filter );
      appenders.put( appender.getName(), appender );
      appenderLevels.put( appender.getName(), level );
      loggerConfig.setLevel( getEffectiveLevel() );
    }

    private void removeAppender( Appender appender ) {
      loggerConfig.removeAppender( appender.getName() );
      configuration.getAppenders().remove( appender.getName() );
      appenders.remove( appender.getName() );
      appenderLevels.remove( appender.getName() );
      if ( !appenders.isEmpty() ) {
        loggerConfig.setLevel( getEffectiveLevel() );
      }
      appender.stop();
    }

    private Level getEffectiveLevel() {
      Level effectiveLevel = created ? null : previousLevel;
      for ( Level appenderLevel : appenderLevels.values() ) {
        effectiveLevel = getMostVerboseLevel( effectiveLevel, appenderLevel );
      }
      return effectiveLevel;
    }

    private Level getMostVerboseLevel( Level first, Level second ) {
      if ( first == null ) {
        return second;
      }
      if ( second == null ) {
        return first;
      }
      return first.intLevel() >= second.intLevel() ? first : second;
    }

    private boolean isEmpty() {
      return appenders.isEmpty();
    }

    private void restore( String loggerName ) {
      if ( created ) {
        configuration.removeLogger( loggerName );
      } else {
        loggerConfig.setLevel( previousLevel );
        loggerConfig.setAdditive( previousAdditivity );
      }
    }
  }

  private static final class LoggerConfigKey {
    private final LoggerContext context;
    private final String loggerName;

    private LoggerConfigKey( LoggerContext context, String loggerName ) {
      this.context = context;
      this.loggerName = loggerName;
    }

    @Override
    public boolean equals( Object object ) {
      if ( this == object ) {
        return true;
      }
      if ( !( object instanceof LoggerConfigKey ) ) {
        return false;
      }
      LoggerConfigKey other = (LoggerConfigKey) object;
      return context == other.context && loggerName.equals( other.loggerName );
    }

    @Override
    public int hashCode() {
      return 31 * System.identityHashCode( context ) + loggerName.hashCode();
    }
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
