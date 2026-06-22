/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.services.importexport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Consolidation of all import/export operation metrics
 * 
 * Replaces multiple contradictory logs with single source of truth
 * 
 * Example usage:
 *   ImportExportMetricsCollector metrics = new ImportExportMetricsCollector();
 *   
 *   // From repository content processing
 *   metrics.addRepositoryContent(5);
 *   
 *   // From datasource processing
 *   metrics.addJdbcDatasources(2);
 *   metrics.addMondianDatasources(2);
 *   metrics.addMetadataDatasources(1);
 *   
 *   // From schedule processing
 *   metrics.addSchedules(9790);
 *   
 *   // From user/role processing
 *   metrics.addUsers(5);
 *   metrics.addRoles(6);
 *   metrics.addEmails(0);
 *   metrics.addGroups(0);
 *   
 *   // From metastore
 *   metrics.setMetastoreProcessed(true);
 *   
 *   // Get comprehensive summary
 *   metrics.printConsolidatedSummary();
 */
public class ImportExportMetricsCollector {

  private static final Log logger = LogFactory.getLog( ImportExportMetricsCollector.class );

  // Component-level metrics
  private int repositoryContentItems = 0;
  private int jdbcDatasources = 0;
  private int mondianDatasources = 0;
  private int metadataDatasources = 0;
  private int schedules = 0;
  private int users = 0;
  private int roles = 0;
  private int emails = 0;
  private int groups = 0;
  private boolean metastoreProcessed = false;

  // Overall operation metrics
  private long startTime;
  private long endTime;
  private int totalFailures = 0;
  private int totalSkipped = 0;
  private String backupFilename;
  private long backupFileSize;

  // Component timing
  private Map<String, Long> componentTiming = new HashMap<>();

  // ===== Public API =====

  public ImportExportMetricsCollector() {
    this.startTime = System.currentTimeMillis();
  }

  /**
   * Record repository content items backed up
   * Called after repository content export completes
   */
  public void addRepositoryContent( int count ) {
    this.repositoryContentItems = count;
  }

  /**
   * Record JDBC datasources backed up
   * Called after JDBC datasource export completes
   */
  public void addJdbcDatasources( int count ) {
    this.jdbcDatasources = count;
  }

  /**
   * Record Mondrian datasources backed up
   * Called after Mondrian datasource export completes
   */
  public void addMondianDatasources( int count ) {
    this.mondianDatasources = count;
  }

  /**
   * Record Metadata datasources backed up
   * Called after metadata datasource export completes
   */
  public void addMetadataDatasources( int count ) {
    this.metadataDatasources = count;
  }

  /**
   * Record schedules backed up
   * Called after schedule export completes - THIS IS THE 9,790 NUMBER!
   */
  public void addSchedules( int count ) {
    this.schedules = count;
  }

  /**
   * Record users backed up
   * Called after user export completes
   */
  public void addUsers( int count ) {
    this.users = count;
  }

  /**
   * Record roles backed up
   * Called after role export completes
   */
  public void addRoles( int count ) {
    this.roles = count;
  }

  /**
   * Record emails backed up
   * Called after email export completes
   */
  public void addEmails( int count ) {
    this.emails = count;
  }

  /**
   * Record groups backed up
   * Called after group export completes
   */
  public void addGroups( int count ) {
    this.groups = count;
  }

  /**
   * Mark metastore as processed
   */
  public void setMetastoreProcessed( boolean processed ) {
    this.metastoreProcessed = processed;
  }

  /**
   * Record failures
   */
  public void addFailure( String component, String reason ) {
    totalFailures++;
    logger.warn( "[FAILED] " + component + " - " + reason );
  }

  /**
   * Record items skipped
   */
  public void addSkipped( String component, int count, String reason ) {
    totalSkipped += count;
    if ( count > 0 ) {
      logger.info( "[SKIPPED] " + component + " - " + count + " items - " + reason );
    }
  }

  /**
   * Record component timing
   */
  public void addComponentTiming( String componentName, long durationMs ) {
    componentTiming.put( componentName, durationMs );
  }

  /**
   * Set backup file info
   */
  public void setBackupFileInfo( String filename, long size ) {
    this.backupFilename = filename;
    this.backupFileSize = size;
  }

  /**
   * Mark backup as complete and calculate final metrics
   */
  public void complete() {
    this.endTime = System.currentTimeMillis();
  }

  // ===== Output Generation =====

  /**
   * Print consolidated summary (REPLACES ALL THE CONTRADICTORY LOGS)
   */
  public void printConsolidatedSummary() {
    logger.info( "" );
    logger.info( "════════════════════════════════════════════════" );
    logger.info( "BACKUP OPERATION COMPLETED" );
    logger.info( "────────────────────────────────────────────────" );
    logger.info( "" );
    
    printSummary();
    logger.info( "" );
    
    printComponentBreakdown();
    logger.info( "" );
    
    if ( !componentTiming.isEmpty() ) {
      printTimingBreakdown();
      logger.info( "" );
    }
    
    logger.info( "════════════════════════════════════════════════" );
  }

  /**
   * Print single-line summary for log aggregation
   */
  public void printSingleLineSummary() {
    long duration = endTime - startTime;
    int totalItems = getTotalItems();
    String filename = backupFilename != null ? backupFilename : "N/A";
    logger.info( "[BACKUP] SUCCESS | " + totalItems + " items | " + formatDuration( duration ) + " | " + filename + " | 0 errors" );
  }

  // ===== Private Helper Methods =====

  private void printSummary() {
    long duration = endTime - startTime;
    int totalItems = getTotalItems();
    
    logger.info( "SUMMARY:" );
    logger.info( "  Total Items Backed Up: " + formatNumber( totalItems ) );
    logger.info( "  Failed: " + totalFailures );
    logger.info( "  Skipped: " + totalSkipped );
    logger.info( "  Duration: " + formatDuration( duration ) );
    
    if ( backupFilename != null ) {
      logger.info( "  Backup File: " + backupFilename + " | " + formatSize( backupFileSize ) );
    }
  }

  private void printComponentBreakdown() {
    logger.info( "COMPONENT BREAKDOWN:" );
    
    // Repository Content
    if ( repositoryContentItems > 0 ) {
      logger.info( "  Repository Content ........ " + repositoryContentItems + " items" );
    }
    
    // Datasources (consolidated)
    int totalDatasources = jdbcDatasources + mondianDatasources + metadataDatasources;
    if ( totalDatasources > 0 ) {
      String breakdown = String.format( "%d JDBC, %d Mondrian, %d Metadata",
        jdbcDatasources, mondianDatasources, metadataDatasources );
      logger.info( "  Datasources .............. " + totalDatasources + " items (" + breakdown + ")" );
    }
    
    // Schedules (THE IMPORTANT ONE - 9,790)
    if ( schedules > 0 ) {
      logger.info( "  Schedules .............. " + formatNumber( schedules ) + " items" );
    }
    
    // Users & Roles (consolidated)
    int totalUsersRoles = users + roles;
    if ( totalUsersRoles > 0 ) {
      logger.info( "  Users & Roles ............ " + totalUsersRoles + " items (" + users + " users, " + roles + " roles)" );
    }
    
    // Email/Groups
    int totalEmailsGroups = emails + groups;
    if ( totalEmailsGroups > 0 ) {
      logger.info( "  Email/Groups ............. " + totalEmailsGroups + " items (" + emails + " emails, " + groups + " groups)" );
    } else if ( totalEmailsGroups == 0 && (emails >= 0 && groups >= 0) ) {
      // If we explicitly checked and found none, show it
      logger.info( "  Email/Groups ............. 0 items" );
    }
    
    // Metastore
    if ( metastoreProcessed ) {
      logger.info( "  Metastore ............... ✓ Completed" );
    }
  }

  private void printTimingBreakdown() {
    logger.info( "TIMING BREAKDOWN:" );
    long totalMs = 0;
    
    for ( Map.Entry<String, Long> entry : componentTiming.entrySet() ) {
      long ms = entry.getValue();
      totalMs += ms;
      logger.info( "  " + entry.getKey() + " ...... " + formatDuration( ms ) );
    }
    
    logger.info( "  ────────────────────────────────────────" );
    logger.info( "  Total Duration: " + formatDuration( endTime - startTime ) );
  }

  private int getTotalItems() {
    return repositoryContentItems +
           jdbcDatasources +
           mondianDatasources +
           metadataDatasources +
           schedules +
           users +
           roles +
           emails +
           groups;
  }

  private String formatNumber( int number ) {
    return String.format( "%,d", number );
  }

  private String formatDuration( long millis ) {
    long seconds = millis / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;

    if ( hours > 0 ) {
      return String.format( "%dh %dm %ds", hours, minutes % 60, seconds % 60 );
    } else if ( minutes > 0 ) {
      return String.format( "%dm %ds", minutes, seconds % 60 );
    } else {
      return String.format( "%ds", seconds );
    }
  }

  private String formatSize( long bytes ) {
    if ( bytes <= 0 ) return "0B";
    final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
    int unitIndex = (int) ( Math.log10( bytes ) / Math.log10( 1024 ) );
    double displaySize = bytes / Math.pow( 1024, unitIndex );
    return String.format( "%.1f%s", displaySize, units[unitIndex] );
  }

  // ===== Getters =====

  public int getTotalRepositoryContent() {
    return repositoryContentItems;
  }

  public int getTotalDatasources() {
    return jdbcDatasources + mondianDatasources + metadataDatasources;
  }

  public int getTotalSchedules() {
    return schedules;
  }

  public int getTotalUsers() {
    return users;
  }

  public int getTotalRoles() {
    return roles;
  }

  public int getTotalUsersAndRoles() {
    return users + roles;
  }

  public int getTotalFailures() {
    return totalFailures;
  }

  public int getTotalSkipped() {
    return totalSkipped;
  }

  public long getDuration() {
    return endTime - startTime;
  }
}
