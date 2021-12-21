package org.pentaho.platform.api.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.StringWriter;
import java.io.Writer;

public class LogUtil {

    public static void addAppender(Appender appender, Logger logger, Level level) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
        Configuration config = ctx.getConfiguration();
        appender.start();
        config.addAppender(appender);
        LoggerConfig loggerConfig = config.getLoggerConfig( logger.getName() );
        loggerConfig.addAppender( appender, level, null );
        ctx.updateLoggers();
    }

    public static void removeAppender(Appender appender, Logger logger) {
        appender.stop();
        LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig( logger.getName() );
        loggerConfig.removeAppender( appender.getName() );
        ctx.updateLoggers();
    }

    public static Appender makeAppender(String name, StringWriter sw, String layout) {
        return WriterAppender.newBuilder()
                .setName(name)
                .setLayout(PatternLayout.newBuilder().withPattern(layout).build())
                .setTarget(sw)
                .build();
    }

    public static Appender makeAppender(String name, Writer writer, Layout layout) {
        return WriterAppender.newBuilder().setName(name).setLayout(layout).setTarget(writer).build();
    }

    public static void setLevel(Logger logger, Level level) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        LoggerConfig loggerConfig = config.getLoggerConfig(logger.getName());
        LoggerConfig specificConfig = loggerConfig;

        // We need a specific configuration for this logger,
        // otherwise we would change the level of all other loggers
        // having the original configuration as parent as well

        if (!loggerConfig.getName().equals(logger.getName())) {
            specificConfig = new LoggerConfig(logger.getName(), level, true);
            specificConfig.setParent(loggerConfig);
            config.addLogger(logger.getName(), specificConfig);
        }
        specificConfig.setLevel(level);
        ctx.updateLoggers();
    }
    public static void setRootLoggerLevel(Level level) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(level);
        ctx.updateLoggers();
    }
}
