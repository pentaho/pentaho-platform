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


package org.pentaho.platform.util;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.logging.SimpleLogger;

public class LogTest extends TestCase {

  public void testLogger() {

    Logger.setLogLevel( ILogger.DEBUG );
    int logLevel = Logger.getLogLevel();
    String logLevelName = Logger.getLogLevelName( logLevel );
    int logLevelWithParam = Logger.getLogLevel( logLevelName );
    Assert.assertEquals( logLevelWithParam, logLevel );

    Logger.setLogLevel( ILogger.ERROR );
    logLevel = Logger.getLogLevel();
    logLevelWithParam = Logger.getLogLevel( "ERROR" ); //$NON-NLS-1$
    logLevelName = Logger.getLogLevelName( logLevel );
    Assert.assertEquals( logLevelWithParam, logLevel );

    Logger.setLogLevel( ILogger.FATAL );
    logLevel = Logger.getLogLevel();
    logLevelWithParam = Logger.getLogLevel( "FATAL" ); //$NON-NLS-1$
    logLevelName = Logger.getLogLevelName( logLevel );
    Assert.assertEquals( logLevelWithParam, logLevel );

    Logger.setLogLevel( ILogger.INFO );
    logLevel = Logger.getLogLevel();
    logLevelWithParam = Logger.getLogLevel( "INFO" ); //$NON-NLS-1$
    logLevelName = Logger.getLogLevelName( logLevel );
    Assert.assertEquals( logLevelWithParam, logLevel );

    Logger.setLogLevel( ILogger.TRACE );
    logLevel = Logger.getLogLevel();
    logLevelWithParam = Logger.getLogLevel( "TRACE" ); //$NON-NLS-1$
    logLevelName = Logger.getLogLevelName( logLevel );
    Assert.assertEquals( logLevelWithParam, logLevel );

    Logger.setLogLevel( ILogger.WARN );
    logLevel = Logger.getLogLevel();
    logLevelWithParam = Logger.getLogLevel( "WARN" ); //$NON-NLS-1$
    logLevelName = Logger.getLogLevelName( logLevel );
    Assert.assertEquals( logLevelWithParam, logLevel );

    Logger.setLogLevel( ILogger.ERROR );
    logLevel = Logger.getLogLevel();
    logLevelWithParam = Logger.getLogLevel( "UNKNOWN" ); //$NON-NLS-1$ - this will return the deafult value of ERROR
    logLevelName = Logger.getLogLevelName( logLevel );
    Assert.assertEquals( logLevelWithParam, logLevel );

    Logger.warn( this.getClass(), "This is a warning with class as an object" + ILogger.WARN ); //$NON-NLS-1$
    Logger.warn( "LogTest", "This is a warning with class as a string" + ILogger.WARN ); //$NON-NLS-1$ //$NON-NLS-2$
    Logger.warn( this.getClass(), "This is a warning with class as an object" + +ILogger.WARN, new Throwable() ); //$NON-NLS-1$
    Logger.warn( "LogTest", "This is a warning with class as a string" + +ILogger.WARN, new Throwable() ); //$NON-NLS-1$ //$NON-NLS-2$

    Logger.debug( this.getClass(), "This is a debug with class as an object" + +ILogger.DEBUG ); //$NON-NLS-1$
    Logger.debug( "LogTest", "This is a debug with class as a string" + ILogger.DEBUG ); //$NON-NLS-1$ //$NON-NLS-2$
    Logger.debug( this.getClass(), "This is a debug with class as an object" + +ILogger.DEBUG, new Throwable() ); //$NON-NLS-1$
    Logger.debug( "LogTest", "This is a debug with class as a string" + ILogger.DEBUG, new Throwable() ); //$NON-NLS-1$ //$NON-NLS-2$

    Logger.error( this.getClass(), "This is a error with class as an object" + ILogger.ERROR ); //$NON-NLS-1$
    Logger.error( "LogTest", "This is a error with class as a string" + ILogger.ERROR ); //$NON-NLS-1$ //$NON-NLS-2$
    Logger.error( this.getClass(), "This is a error with class as an object" + ILogger.ERROR, new Throwable() ); //$NON-NLS-1$
    Logger.error( "LogTest", "This is a error with class as a string" + ILogger.ERROR, new Throwable() ); //$NON-NLS-1$ //$NON-NLS-2$

    Logger.fatal( this.getClass(), "This is a fatal with class as an object" + ILogger.FATAL ); //$NON-NLS-1$
    Logger.fatal( "LogTest", "This is a fatal with class as a string" + ILogger.FATAL ); //$NON-NLS-1$ //$NON-NLS-2$
    Logger.fatal( this.getClass(), "This is a fatal with class as an object" + ILogger.FATAL, new Throwable() ); //$NON-NLS-1$
    Logger.fatal( "LogTest", "This is a fatal with class as a string" + ILogger.FATAL, new Throwable() ); //$NON-NLS-1$ //$NON-NLS-2$

    Logger.info( this.getClass(), "This is an info with class as an object" + ILogger.INFO ); //$NON-NLS-1$ 
    Logger.info( "LogTest", "This is an info with class as a string" + ILogger.INFO ); //$NON-NLS-1$ //$NON-NLS-2$
    Logger.info( this.getClass(), "This is an info with class as an object" + ILogger.INFO, new Throwable() ); //$NON-NLS-1$
    Logger.info( "LogTest", "This is an info with class as a string" + ILogger.INFO, new Throwable() ); //$NON-NLS-1$ //$NON-NLS-2$

    Logger.trace( this.getClass(), "This is an info with class as an object" + ILogger.TRACE, new Throwable() ); //$NON-NLS-1$
    Logger.trace( "LogTest", "This is an info with class as a string" + ILogger.TRACE, new Throwable() ); //$NON-NLS-1$ //$NON-NLS-2$

    Assert.assertTrue( true );
  }

  public void testSimpleLogger() {

    SimpleLogger logger = new SimpleLogger( LogTest.class );

    logger.setLoggingLevel( ILogger.WARN );
    int logLevel = logger.getLoggingLevel();
    String logLevelName = logger.getLogLevelName( logLevel );
    int logLevelWithParam = logger.getLogLevel( logLevelName );
    Assert.assertEquals( logLevelWithParam, ILogger.WARN );

    logger.warn( "This is a warning with class as an object" + ILogger.WARN ); //$NON-NLS-1$
    logger.warn( "This is a warning with class as an object" + +ILogger.WARN, new Throwable() ); //$NON-NLS-1$

    logger.debug( "This is a debug with class as an object" + ILogger.DEBUG ); //$NON-NLS-1$
    logger.debug( "This is a debug with class as an object" + ILogger.DEBUG, new Throwable() ); //$NON-NLS-1$

    logger.error( "This is a error with class as an object" + ILogger.ERROR ); //$NON-NLS-1$
    logger.error( "This is a error with class as an object" + ILogger.ERROR, new Throwable() ); //$NON-NLS-1$

    logger.fatal( "This is a fatal with class as an object" + ILogger.FATAL ); //$NON-NLS-1$
    logger.fatal( "This is a fatal with class as an object" + ILogger.FATAL, new Throwable() ); //$NON-NLS-1$

    logger.info( "This is an info with class as an object" + ILogger.INFO ); //$NON-NLS-1$ 
    logger.info( "This is an info with class as an object" + ILogger.INFO, new Throwable() ); //$NON-NLS-1$

    logger.trace( "This is a trace with class as an object" + ILogger.TRACE, new Throwable() ); //$NON-NLS-1$
    logger.trace( "This is a trace with class as a string" + ILogger.TRACE, new Throwable() ); //$NON-NLS-1$ 
    logger.trace( "This is a trace test" ); //$NON-NLS-1$

    Assert.assertTrue( true );
  }

}
