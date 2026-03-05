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


package org.pentaho.platform.web.http.context;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.web.hsqldb.HsqlDatabaseStarterBean;
import org.pentaho.platform.web.hsqldb.messages.Messages;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HsqldbStartupListener implements ServletContextListener {

  private static final Log logger = LogFactory.getLog( HsqldbStartupListener.class );

  public void contextDestroyed( ServletContextEvent sce ) {
    ServletContext ctx = sce.getServletContext();
    Object obj = ctx.getAttribute( "hsqldb-starter-bean" ); //$NON-NLS-1$
    if ( obj != null ) {
      logger.debug( "Context listener stopping Embedded HSQLDB" ); //$NON-NLS-1$
      HsqlDatabaseStarterBean starterBean = ( HsqlDatabaseStarterBean ) obj;
      starterBean.stop();
    }
  }

  private Map<String, String> getDatabases( ServletContext ctx ) {
    HashMap<String, String> map = new LinkedHashMap<String, String>();
    String dbs = ctx.getInitParameter( "hsqldb-databases" ); //$NON-NLS-1$
    if ( dbs != null ) {
      String[] dbEntries = dbs.split( "," ); //$NON-NLS-1$
      for ( int i = 0; i < dbEntries.length; i++ ) {
        String[] entry = dbEntries[i].split( "@" ); //$NON-NLS-1$
        if ( ( entry.length != 2 ) || ( StringUtils.isEmpty( entry[0] ) ) || ( StringUtils.isEmpty( entry[1] ) ) ) {
          logger.error(
            Messages.getErrorString( "HsqlDatabaseStartupListener.ERROR_0001_HSQLDB_ENTRY_MALFORMED" ) ); //$NON-NLS-1$
          continue;
        }
        map.put( entry[0], entry[1] );
      }
    }
    return map;
  }

  /**
   * Load data from .script file into an in-memory HSQLDB database
   * For in-memory databases, we connect directly without a server port.
   *
   * @param dbName     The database name (e.g., "sampledata")
   * @param scriptPath The path to the .script file
   */
  private void loadDatabaseFromScript( String dbName, String scriptPath ) {
    File scriptFile = new File( scriptPath );
    if ( !scriptFile.exists() ) {
      logger.warn( String.format( "Script file not found for database %s: %s", dbName, scriptPath ) ); //$NON-NLS-1$
      return;
    }

    String jdbcUrl = String.format( "jdbc:hsqldb:mem:%s", dbName ); //$NON-NLS-1$
    try {
      loadScriptIntoDatabase( dbName, jdbcUrl, scriptFile );
    } catch ( Exception e ) {
      logger.warn( String.format( "Failed to load data for database %s: %s", dbName, e.getMessage() ) ); //$NON-NLS-1$
    }
  }

  /**
   * Executes SQL statements from a script file against a database connection
   *
   * @param dbName     The database name for logging
   * @param jdbcUrl    The JDBC connection URL
   * @param scriptFile The script file to load
   * @throws SQLException if an error occurs during script execution
   */
  private void loadScriptIntoDatabase( String dbName, String jdbcUrl, File scriptFile ) throws SQLException {
    try ( Connection conn = DriverManager.getConnection( jdbcUrl, "sa", "password" ); //$NON-NLS-1$ //$NON-NLS-2$
          Statement stmt = conn.createStatement() ) {
      executeSqlStatements( dbName, scriptFile, stmt );
      logger.info( String.format( "Successfully loaded data for database %s from %s", dbName, scriptFile.getAbsolutePath() ) ); //$NON-NLS-1$
    }
  }

  /**
   * Reads and executes SQL statements from a script file
   *
   * @param dbName     The database name for logging
   * @param scriptFile The script file to read from
   * @param stmt       The Statement to execute SQL with
   * @throws Exception if an error occurs during file reading
   */
  private void executeSqlStatements( String dbName, File scriptFile, Statement stmt ) throws Exception {
    try ( BufferedReader reader = new BufferedReader( new FileReader( scriptFile ) ) ) {
      StringBuilder sqlBuilder = new StringBuilder();
      String line;
      int lineNum = 0;

      while ( ( line = reader.readLine() ) != null ) {
        lineNum++;
        line = line.trim();

        // Skip empty lines and comments
        if ( line.isEmpty() || line.startsWith( "--" ) ) {
          continue;
        }

        sqlBuilder.append( line ).append( "\n" ); //$NON-NLS-1$

        // Execute statement when we hit a semicolon
        if ( line.endsWith( ";" ) ) {
          executeSingleStatement( stmt, sqlBuilder, lineNum, dbName, scriptFile.getAbsolutePath() );
          sqlBuilder = new StringBuilder();
        }
      }
    }
  }

  /**
   * Executes a single SQL statement and logs any errors
   *
   * @param stmt       The Statement to execute
   * @param sqlBuilder The StringBuilder containing the SQL
   * @param lineNum    The line number for error reporting
   * @param dbName     The database name for logging
   * @param scriptPath The script file path for logging
   */
  private void executeSingleStatement( Statement stmt, StringBuilder sqlBuilder, int lineNum, String dbName, String scriptPath ) {
    String sql = sqlBuilder.toString().trim();
    if ( !sql.isEmpty() ) {
      try {
        stmt.execute( sql );
      } catch ( SQLException e ) {
        logger.warn( String.format( "Error executing SQL at line %d in %s for database %s: %s", lineNum, scriptPath, dbName, e.getMessage() ) ); //$NON-NLS-1$
        // Continue processing despite errors
      }
    }
  }

  /**
   * Load all database scripts synchronously before returning from contextInitialized.
   * This ensures data is available when Hibernate and other components initialize.
   */
  private void loadAllDatabaseScripts( ServletContext ctx, Map<String, String> databases ) {
    String initScriptPath = ctx.getInitParameter( "hsqldb-init-script" ); //$NON-NLS-1$
    if ( initScriptPath != null ) {
      for ( Map.Entry<String, String> entry : databases.entrySet() ) {
        String dbName = entry.getKey();
        // Only load in-memory databases (persistent ones already have their data)
        if ( entry.getValue().startsWith( "mem:" ) ) { //$NON-NLS-1$
          // Resolve the script path relative to the web app context
          String webappPath = ctx.getRealPath( "/" ); //$NON-NLS-1$
          String scriptFilePath = null;
          if ( webappPath != null ) {
            // Convert ../../data/hsqldb to absolute path
            File baseDir = new File( webappPath );
            File scriptFile = new File( baseDir, initScriptPath + File.separator + dbName + ".script" ); //$NON-NLS-1$
            scriptFilePath = scriptFile.getAbsolutePath();
          }
          if ( scriptFilePath != null ) {
            logger.debug( String.format( "Loading script for in-memory database %s from %s", dbName, scriptFilePath ) ); //$NON-NLS-1$
            loadDatabaseFromScript( dbName, scriptFilePath );
          } else {
            logger.warn( String.format( "Could not resolve script path for database %s", dbName ) ); //$NON-NLS-1$
          }
        }
      }
    }
  }

  public void contextInitialized( ServletContextEvent sce ) {
    ServletContext ctx = sce.getServletContext();
    logger.debug( "Context listener initializing Embedded HSQLDB" ); //$NON-NLS-1$
    HsqlDatabaseStarterBean starterBean = new HsqlDatabaseStarterBean();

    // Check for port override
    String port = ctx.getInitParameter( "hsqldb-port" ); //$NON-NLS-1$
    Integer portNum = null;
    if ( !StringUtils.isEmpty( port ) ) {
      logger.debug( String.format( "Port override specified: %s", port ) ); //$NON-NLS-1$
      try {
        portNum = Integer.parseInt( port );
        starterBean.setPort( portNum );
      } catch ( NumberFormatException ex ) {
        logger.error( Messages.getErrorString( "HsqldbStartupListener.ERROR_0004_INVALID_PORT", "9001" ) ); //$NON-NLS-1$
        port = null; // force check default port
      }
    }

    Map<String, String> databases = getDatabases( ctx );
    starterBean.setDatabases( databases );

    String sampleDataAllowPortFailover = ctx.getInitParameter( "hsqldb-allow-port-failover" ); //$NON-NLS-1$
    if ( ( sampleDataAllowPortFailover != null ) && ( sampleDataAllowPortFailover.equalsIgnoreCase( "true" ) ) ) { //$NON-NLS-1$
      logger.debug( String.format( "Allow Port Failover specified" ) ); //$NON-NLS-1$
      starterBean.setAllowPortFailover( true );
    }

    if ( starterBean.start() ) {
      ctx.setAttribute( "hsqldb-starter-bean", starterBean ); //$NON-NLS-1$

      // Load database scripts synchronously for in-memory databases before other components initialize
      loadAllDatabaseScripts( ctx, databases );

      logger.info( "HSQLDB data loading completed. Proceeding with application initialization." ); //$NON-NLS-1$
    }

  }


}
