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



package org.pentaho.platform.web.http.context;

import org.apache.commons.lang3.StringUtils;
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
      
      // Issue CHECKPOINT on all databases to flush all changes to disk before shutdown
      Map<String, String> databases = getDatabases( ctx );
      for ( Map.Entry<String, String> entry : databases.entrySet() ) {
        String dbName = entry.getKey();
        String dbUrl = entry.getValue();
        if ( dbUrl.startsWith( "file:" ) ) { //$NON-NLS-1$
          try {
            String jdbcUrl = String.format( "jdbc:hsqldb:hsql://localhost:9001/%s", dbName ); //$NON-NLS-1$
            try ( Connection conn = DriverManager.getConnection( jdbcUrl, "sa", "" ); //$NON-NLS-1$ //$NON-NLS-2$
                  Statement stmt = conn.createStatement() ) {
              logger.info( String.format( "HsqldbStartupListener: Issuing CHECKPOINT for database %s to flush changes to disk", dbName ) ); //$NON-NLS-1$
              stmt.execute( "CHECKPOINT" ); //$NON-NLS-1$
            }
          } catch ( SQLException e ) {
            logger.warn( String.format( "HsqldbStartupListener: Could not issue CHECKPOINT for database %s: %s", dbName, e.getMessage() ) ); //$NON-NLS-1$
          }
        }
      }
      
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
   * Executes SQL statements from a script file against a database connection
   *
   * @param dbName     The database name for logging
   * @param jdbcUrl    The JDBC connection URL
   * @param scriptFile The script file to load
   * @throws SQLException if an error occurs during script execution
   */
  private void loadScriptIntoDatabase( String dbName, String jdbcUrl, File scriptFile ) throws Exception {
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
   * Gracefully ignores "already exists" and constraint violation errors which occur on subsequent runs
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
        // Gracefully handle common non-fatal errors:
        // - "already exists": schema/user/table already created
        // - "not found": object doesn't exist (expected on first run)
        // - "unique constraint" or "integrity constraint": duplicate data on file-based DB re-reads
        // - "primary key": duplicate key in existing database
        String errorMsg = e.getMessage().toLowerCase();
        if ( errorMsg.contains( "already exists" ) || errorMsg.contains( "not found" )
            || errorMsg.contains( "unique constraint" ) || errorMsg.contains( "integrity constraint" )
            || errorMsg.contains( "primary key" ) ) {
          logger.debug( String.format( "Skipping expected SQL error at line %d in %s for database %s: %s", lineNum, scriptPath, dbName, e.getMessage() ) ); //$NON-NLS-1$
        } else {
          logger.warn( String.format( "Error executing SQL at line %d in %s for database %s: %s", lineNum, scriptPath, dbName, e.getMessage() ) ); //$NON-NLS-1$
        }
        // Continue processing despite errors
      }
    }
  }

  /**
   * Load all database scripts synchronously before returning from contextInitialized.
   * This ensures data is available when Hibernate and other components initialize.
   * For in-memory databases: scripts load every startup (database is recreated).
   * For file-based databases: scripts load on first initialization (checking for "already exists" errors).
   */
  private void loadAllDatabaseScripts( ServletContext ctx, Map<String, String> databases ) {
    String initScriptPath = ctx.getInitParameter( "hsqldb-init-script" ); //$NON-NLS-1$
    logger.info( String.format( "HsqldbStartupListener: initScriptPath=%s, databases=%d", initScriptPath, databases.size() ) ); //$NON-NLS-1$
    if ( initScriptPath != null ) {
      for ( Map.Entry<String, String> entry : databases.entrySet() ) {
        String dbName = entry.getKey();
        String dbUrl = entry.getValue();
        logger.info( String.format( "HsqldbStartupListener: Processing database %s with URL %s", dbName, dbUrl ) ); //$NON-NLS-1$
        
        // Load scripts for BOTH in-memory and file-based databases
        // For in-memory: scripts reload on each startup (database is recreated)
        // For file-based: scripts run but gracefully skip "already exists" errors (preserving state)
        if ( dbUrl.startsWith( "mem:" ) || dbUrl.startsWith( "file:" ) ) { //$NON-NLS-1$ //$NON-NLS-2$
          // Resolve the script path relative to the web app context
          String webappPath = ctx.getRealPath( "/" ); //$NON-NLS-1$
          String scriptFilePath = null;
          if ( webappPath != null ) {
            // Convert ../../data/hsqldb to absolute path
            File baseDir = new File( webappPath );
            File scriptFile = new File( baseDir, initScriptPath + File.separator + dbName + ".script" ); //$NON-NLS-1$
            scriptFilePath = scriptFile.getAbsolutePath();
            logger.info( String.format( "HsqldbStartupListener: webappPath=%s, scriptFile=%s, exists=%b", webappPath, scriptFilePath, scriptFile.exists() ) ); //$NON-NLS-1$
          } else {
            logger.warn( "HsqldbStartupListener: webappPath is null" ); //$NON-NLS-1$
          }
          if ( scriptFilePath != null ) {
            logger.info( String.format( "HsqldbStartupListener: Loading script for database %s from %s", dbName, scriptFilePath ) ); //$NON-NLS-1$
            loadDatabaseFromScriptWithUrl( dbName, scriptFilePath, dbUrl );
          } else {
            logger.warn( String.format( "HsqldbStartupListener: Could not resolve script path for database %s", dbName ) ); //$NON-NLS-1$
          }
        } else {
          logger.warn( String.format( "HsqldbStartupListener: Skipping database %s (URL does not start with mem: or file:)", dbName ) ); //$NON-NLS-1$
        }
      }
    } else {
      logger.warn( "HsqldbStartupListener: hsqldb-init-script parameter not set" ); //$NON-NLS-1$
    }
  }

  /**
   * Load data from .script file into HSQLDB database using the configured URL
   *
   * @param dbName     The database name (e.g., "sampledata")
   * @param scriptPath The path to the .script file
   * @param configUrl  The configured JDBC URL (mem: or file:)
   */
  private void loadDatabaseFromScriptWithUrl( String dbName, String scriptPath, String configUrl ) {
    File scriptFile = new File( scriptPath );
    if ( !scriptFile.exists() ) {
      logger.warn( String.format( "Script file not found for database %s: %s", dbName, scriptPath ) ); //$NON-NLS-1$
      return;
    }

    try {
      loadScriptIntoDatabaseWithUrl( dbName, configUrl, scriptFile );
    } catch ( Exception e ) {
      logger.warn( String.format( "Failed to load data for database %s: %s", dbName, e.getMessage() ) ); //$NON-NLS-1$
    }
  }

  /**
   * Executes SQL statements from a script file against a database connection
   * For in-memory databases, connects directly using the mem: URL
   * For file-based databases, connects through the server port (hsql://localhost:9001/)
   *
   * @param dbName     The database name for logging
   * @param configUrl  The configured URL (mem:name or file:./path format)
   * @param scriptFile The script file to load
   * @throws SQLException if an error occurs during script execution
   */
  private void loadScriptIntoDatabaseWithUrl( String dbName, String configUrl, File scriptFile ) throws Exception {
    // Construct appropriate JDBC URL based on config URL type
    // SA credentials are defined in the script files themselves
    String jdbcUrl;
    
    if ( configUrl.startsWith( "mem:" ) ) {
      // For in-memory: connect directly using mem: URL
      jdbcUrl = "jdbc:hsqldb:" + configUrl; //$NON-NLS-1$
    } else if ( configUrl.startsWith( "file:" ) ) {
      // For file-based: connect through server port
      jdbcUrl = String.format( "jdbc:hsqldb:hsql://localhost:9001/%s", dbName ); //$NON-NLS-1$
    } else {
      throw new IllegalArgumentException( "Unsupported HSQLDB URL format: " + configUrl ); //$NON-NLS-1$
    }
    
    logger.info( String.format( "HsqldbStartupListener: Connecting to database %s using URL %s as user sa", dbName, jdbcUrl ) ); //$NON-NLS-1$
    try ( Connection conn = DriverManager.getConnection( jdbcUrl, "sa", "" ); //$NON-NLS-1$ //$NON-NLS-2$
          Statement stmt = conn.createStatement() ) {
      logger.info( String.format( "HsqldbStartupListener: Successfully connected to %s, loading script %s", dbName, scriptFile.getAbsolutePath() ) ); //$NON-NLS-1$
      executeSqlStatements( dbName, scriptFile, stmt );
      logger.info( String.format( "Successfully loaded data for database %s from %s", dbName, scriptFile.getAbsolutePath() ) ); //$NON-NLS-1$
    } catch ( Exception e ) {
      logger.error( String.format( "HsqldbStartupListener: Failed to connect to %s at %s: %s", dbName, jdbcUrl, e.getMessage() ), e ); //$NON-NLS-1$
      throw e;
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

      // Give the server a moment to fully initialize and bind to the port
      try {
        Thread.sleep( 500 );
      } catch ( InterruptedException e ) {
        Thread.currentThread().interrupt();
      }

      // Load database scripts synchronously for both in-memory and file-based databases before other components initialize
      // SA credentials are defined in the script files themselves
      loadAllDatabaseScripts( ctx, databases );

      logger.info( "HSQLDB data loading completed. Proceeding with application initialization." ); //$NON-NLS-1$
    }

  }


}
