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


package org.pentaho.platform.plugin.services.importexport;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.StringLayout;
import org.pentaho.platform.api.util.IRepositoryExportLogger;

import java.io.OutputStream;

/**
 * {@inherit}
 *
 * @author TKafalas
 */
public class Log4JRepositoryExportLogger implements IRepositoryExportLogger {

  private ThreadLocal<Log4JRepositoryExportLog> repositoryExportLog = new ThreadLocal<Log4JRepositoryExportLog>();

  public Log4JRepositoryExportLogger() {
  }

  public void remove() {
    repositoryExportLog.remove();
  }

  public void startJob( OutputStream outputStream, Level logLevel, StringLayout layout ) {
    repositoryExportLog.set( new Log4JRepositoryExportLog( outputStream, logLevel, layout ) );
  }

  public void startJob( OutputStream outputStream, Level logLevel ) {
    repositoryExportLog.set( new Log4JRepositoryExportLog( outputStream, logLevel ) );
  }

  public void endJob() {
    getLog4JRepositoryImportLog().endJob();
  }

  public void info( String s ) {
    getLogger().info( s );
  }

  public void error( String s ) {
    getLogger().error( s );
  }

  public void debug( String s ) {
    getLogger().debug( s );
  }

  public void warn( String s ) {
    getLogger().debug( s );
  }

  @Override
  public void error( Exception e ) {
    getLogger().error( e.getMessage(), e );

  }

  private Log4JRepositoryExportLog getLog4JRepositoryImportLog() {
    Log4JRepositoryExportLog currentLog = repositoryExportLog.get();
    if ( currentLog == null ) {
      throw new IllegalStateException( "No job started for current Thread" );
    }
    return currentLog;
  }

  private Logger getLogger() {
    return getLog4JRepositoryImportLog().getLogger();
  }

  public boolean hasLogger() {
    return ( repositoryExportLog.get() == null ) ? false : true;
  }

  @Override
  public void debug( Object arg0 ) {
    getLogger().debug( arg0 );
  }

  @Override
  public void debug( Object arg0, Throwable arg1 ) {
    getLogger().debug( arg0, arg1 );
  }

  @Override
  public void error( Object arg0 ) {
    getLogger().error( arg0 );
  }

  @Override
  public void error( Object arg0, Throwable arg1 ) {
    getLogger().error( arg0, arg1 );

  }

  @Override
  public void fatal( Object arg0 ) {
    getLogger().fatal( arg0 );

  }

  @Override
  public void fatal( Object arg0, Throwable arg1 ) {
    getLogger().fatal( arg0, arg1 );

  }

  @Override
  public void info( Object arg0 ) {
    getLogger().info( arg0 );

  }

  @Override
  public void info( Object arg0, Throwable arg1 ) {
    getLogger().info( arg0, arg1 );

  }

  @Override
  public boolean isDebugEnabled() {
    return getLogger().isDebugEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return Level.ERROR.isMoreSpecificThan( getLogger().getLevel() );
  }

  @Override
  public boolean isFatalEnabled() {
    return Level.FATAL.isMoreSpecificThan( getLogger().getLevel() );
  }

  @Override
  public boolean isInfoEnabled() {
    return getLogger().isInfoEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return getLogger().isTraceEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return Level.WARN.isMoreSpecificThan( getLogger().getLevel() );
  }

  @Override
  public void trace( Object arg0 ) {
    getLogger().trace( arg0 );
  }

  @Override
  public void trace( Object arg0, Throwable arg1 ) {
    getLogger().trace( arg0, arg1 );
  }

  @Override
  public void warn( Object arg0 ) {
    getLogger().warn( arg0 );
  }

  @Override
  public void warn( Object arg0, Throwable arg1 ) {
    getLogger().warn( arg0, arg1 );
  }

}
