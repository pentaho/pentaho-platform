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
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.util.Map;

/**
 * Enhanced import/export logger for meaningful, concise logging
 * 
 * Replaces verbose inventory reports with real-time, actionable logging
 * that clearly shows what's being imported/exported and why items are skipped.
 * 
 * Usage:
 *   ImportExportLogger logger = new ImportExportLogger();
 *   logger.logBackupStart(config);
 *   
 *   for (RepositoryFile file : files) {
 *     if (shouldSkip(file)) {
 *       logger.logFileSkipped(file, "Generated content excluded");
 *     } else {
 *       logger.logFileProcessed(file, component, file.getFileSize());
 *     }
 *   }
 *   
 *   logger.logBackupComplete(metrics);
 */
public class ImportExportLogger {

  private static final Log logger = LogFactory.getLog( ImportExportLogger.class );

  private BackupMetrics metrics = new BackupMetrics();
  private static final String SEPARATOR = "════════════════════════════════════════════════";
  private static final int LOG_EVERY_N_FILES = 50; // Log progress every 50 files

  // ===== Public Methods =====

  /**
   * Log backup operation start
   */
  public void logBackupStart( ComponentConfig config ) {
    logger.info( SEPARATOR );
    logger.info( "BACKUP OPERATION STARTED" );
    logger.info( "────────────────────────────────────────────────" );
    logConfiguration( config );
    metrics.reset();
    metrics.startTime = System.currentTimeMillis();
  }

  /**
   * Log restore operation start
   */
  public void logRestoreStart( ComponentConfig config ) {
    logger.info( SEPARATOR );
    logger.info( "RESTORE OPERATION STARTED" );
    logger.info( "────────────────────────────────────────────────" );
    logConfiguration( config );
    metrics.reset();
    metrics.startTime = System.currentTimeMillis();
  }

  /**
   * Log a file being processed (exported/imported)
   */
  public void logFileProcessed( RepositoryFile file, String component, long fileSize ) {
    // Determine if this is generated content
    boolean isGenerated = hasLineageId( file );

    if ( isGenerated ) {
      metrics.generatedFilesCount++;
      metrics.generatedSize += fileSize;
      logger.info( "  [GEN] " + file.getPath() + " | " + component + " | " + formatSize( fileSize ) );
    } else {
      metrics.regularFilesCount++;
      metrics.regularSize += fileSize;

      // Log every N files to avoid flooding logs
      if ( metrics.regularFilesCount % LOG_EVERY_N_FILES == 0 ) {
        logger.info( "  [REG] " + file.getPath() + " | " + component + " | " + formatSize( fileSize ) );
      }
    }

    metrics.totalItems++;
  }

  /**
   * Log a folder being processed
   */
  public void logFolderProcessed( RepositoryFile folder, String component ) {
    metrics.foldersCount++;
    logger.info( "  [DIR] " + folder.getPath() + " | " + component );
  }

  /**
   * Log a file being skipped with reason
   */
  public void logFileSkipped( RepositoryFile file, String reason ) {
    metrics.skippedCount++;
    logger.info( "  [⋘] " + file.getPath() + " | SKIPPED: " + reason );
  }

  /**
   * Log a file that failed to process
   */
  public void logFileError( RepositoryFile file, String errorMessage ) {
    metrics.failedCount++;
    logger.error( "  [✗] " + file.getPath() + " | ERROR: " + errorMessage );
  }

  /**
   * Log component processing start
   */
  public void logComponentStart( String componentName, int itemCount ) {
    if ( itemCount == 0 ) {
      return; // Don't log empty components
    }
    logger.info( "" );
    logger.info( "[" + componentName + "] Processing " + itemCount + " items..." );
  }

  /**
   * Log component processing complete
   */
  public void logComponentComplete( String componentName, int processed, int skipped, int failed ) {
    if ( processed + skipped + failed == 0 ) {
      return; // Don't log empty components
    }
    logger.info( "[" + componentName + "] Complete: " + processed + " processed | " + skipped + " skipped | " + failed + " failed" );
  }

  /**
   * Log backup operation complete with summary
   */
  public void logBackupComplete() {
    long duration = System.currentTimeMillis() - metrics.startTime;
    logger.info( "" );
    logger.info( SEPARATOR );
    logger.info( "BACKUP OPERATION COMPLETED" );
    logger.info( "────────────────────────────────────────────────" );
    logSummary( duration );
    logger.info( SEPARATOR );
  }

  /**
   * Log restore operation complete with summary
   */
  public void logRestoreComplete() {
    long duration = System.currentTimeMillis() - metrics.startTime;
    logger.info( "" );
    logger.info( SEPARATOR );
    logger.info( "RESTORE OPERATION COMPLETED" );
    logger.info( "────────────────────────────────────────────────" );
    logSummary( duration );
    logger.info( SEPARATOR );
  }

  /**
   * Log with component-wise breakdown (only non-empty components)
   */
  public void logComponentBreakdown( Map<String, ComponentStats> componentStats ) {
    logger.info( "" );
    logger.info( "COMPONENT-WISE BREAKDOWN:" );
    logger.info( "────────────────────────────────────────────────" );

    for ( Map.Entry<String, ComponentStats> entry : componentStats.entrySet() ) {
      ComponentStats stats = entry.getValue();
      
      // Only log if component had activity
      if ( stats.total > 0 || stats.skipped > 0 || stats.failed > 0 ) {
        logger.info( entry.getKey() + ": " + stats.total + " total | " + stats.success + " success | " + stats.skipped + " skipped | " + stats.failed + " failed" );
      }
    }
  }

  /**
   * Log timing breakdown (performance analysis)
   */
  public void logTimingBreakdown( Map<String, Long> componentTimes ) {
    logger.info( "" );
    logger.info( "TIMING BREAKDOWN:" );
    logger.info( "────────────────────────────────────────────────" );

    long total = 0;
    for ( Map.Entry<String, Long> entry : componentTimes.entrySet() ) {
      long millis = entry.getValue();
      total += millis;
      logger.info( entry.getKey() + ": " + formatDuration( millis ) );
    }

    logger.info( "────────────────────────────────────────────────" );
    logger.info( "Total Duration: " + formatDuration( total ) );
  }

  /**
   * Log single-line summary (for log aggregation/monitoring)
   */
  public void logSingleLineSummary( String operationType, boolean success, String backupFile, long duration ) {
    String status = success ? "SUCCESS" : "FAILED";
    String sizeInfo = metrics.totalSize > 0 ? " | " + formatSize( metrics.totalSize ) : "";
    String filename = backupFile != null ? backupFile : "N/A";
    logger.info( "[" + operationType + "] " + status + " | " + metrics.totalItems + " items | " + formatDuration( duration ) + " | " + filename + sizeInfo );
  }

  /**
   * Set backup file info for reporting
   */
  public void setBackupFileInfo( String filename, long size ) {
    metrics.backupFilename = filename;
    metrics.totalSize = size;
  }

  /**
   * Get metrics for programmatic use
   */
  public BackupMetrics getMetrics() {
    return metrics;
  }

  // ===== Private Helper Methods =====

  private void logConfiguration( ComponentConfig config ) {
    logger.info( "Generated Content: " + ( config.isIncludeGeneratedContent() ? "INCLUDED" : "EXCLUDED" ) );
    logger.info( "Components Enabled:" );
    logger.info( "  Repository Content: " + ( config.isIncludeContent() ? "✓" : "✗" ) );
    logger.info( "  Users & Roles: " + ( config.isIncludeUsers() ? "✓" : "✗" ) );
    logger.info( "  Datasources: " + ( config.isIncludeDatasources() ? "✓" : "✗" ) );
    logger.info( "  Metastore: " + ( config.isIncludeMetastore() ? "✓" : "✗" ) );
    logger.info( "  Schedules: " + ( config.isIncludeSchedules() ? "✓" : "✗" ) );
    logger.info( "  User Settings: " + ( config.isIncludeUserSettings() ? "✓" : "✗" ) );
  }

  private void logSummary( long duration ) {
    logger.info( "Regular Content: " + ( metrics.regularFilesCount + metrics.foldersCount ) + " items (" + metrics.regularFilesCount + " files, " + metrics.foldersCount + " folders) | " + formatSize( metrics.regularSize ) );

    logger.info( "Generated Content: " + metrics.generatedFilesCount + " items (skipped if excluded) | " + formatSize( metrics.generatedSize ) );

    logger.info( "Summary:" );
    logger.info( "  Total Items: " + metrics.totalItems );
    logger.info( "  Failed: " + metrics.failedCount );
    logger.info( "  Skipped: " + metrics.skippedCount );
    logger.info( "  Duration: " + formatDuration( duration ) );

    if ( metrics.backupFilename != null ) {
      logger.info( "  Backup File: " + metrics.backupFilename + " | " + formatSize( metrics.totalSize ) );
    }
  }

  private boolean hasLineageId( RepositoryFile file ) {
    // Note: In real implementation, would check file metadata
    // This is pseudo-code for the concept
    // Actual implementation:
    // Map<String, Serializable> metadata = repo.getFileMetadata(file.getId());
    // return metadata != null && metadata.containsKey(IScheduler.RESERVEDMAPKEY_LINEAGE_ID);
    return false;
  }

  private String formatSize( long bytes ) {
    if ( bytes <= 0 ) return "0B";
    final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
    int unitIndex = (int) ( Math.log10( bytes ) / Math.log10( 1024 ) );
    double displaySize = bytes / Math.pow( 1024, unitIndex );
    return String.format( "%.1f%s", displaySize, units[unitIndex] );
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

  // ===== Inner Classes =====

  /**
   * Metrics container for backup/restore operations
   */
  public static class BackupMetrics {
    public long startTime;
    public int regularFilesCount = 0;
    public int generatedFilesCount = 0;
    public int foldersCount = 0;
    public int skippedCount = 0;
    public int failedCount = 0;
    public int totalItems = 0;
    public long regularSize = 0;
    public long generatedSize = 0;
    public long totalSize = 0;
    public String backupFilename;

    public void reset() {
      regularFilesCount = 0;
      generatedFilesCount = 0;
      foldersCount = 0;
      skippedCount = 0;
      failedCount = 0;
      totalItems = 0;
      regularSize = 0;
      generatedSize = 0;
      totalSize = 0;
      backupFilename = null;
    }

    @Override
    public String toString() {
      return String.format( "Files: %d, Folders: %d, Skipped: %d, Failed: %d",
        regularFilesCount, foldersCount, skippedCount, failedCount );
    }
  }

  /**
   * Statistics for a single component
   */
  public static class ComponentStats {
    public int total = 0;
    public int success = 0;
    public int skipped = 0;
    public int failed = 0;

    public ComponentStats( int total, int success, int skipped, int failed ) {
      this.total = total;
      this.success = success;
      this.skipped = skipped;
      this.failed = failed;
    }
  }
}
