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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive metrics collector for backup and restore operations.
 * Tracks what was processed, what succeeded, what failed, and what was skipped.
 */
public class ImportExportMetrics {

  // Backup/Restore operation type
  public enum OperationType {
    BACKUP, RESTORE
  }

  // Category types
  public enum Category {
    FILES, SCHEDULES, USERS, ROLES, DATASOURCES, METADATA, MONDRIAN, METASTORE
  }

  private OperationType operationType;
  private long operationStartTime;
  private long operationEndTime;

  // Success tracking
  private Map<Category, Integer> successCount = new HashMap<>();
  private Map<Category, Long> successSize = new HashMap<>();

  // Failure tracking
  private Map<Category, Integer> failureCount = new HashMap<>();
  private Map<Category, List<String>> failureDetails = new HashMap<>();

  // Skip tracking
  private Map<Category, Integer> skipCount = new HashMap<>();
  private Map<Category, List<String>> skipReasons = new HashMap<>();

  // Total tracking
  private Map<Category, Integer> totalCount = new HashMap<>();

  // Overall stats
  private int totalFiles = 0;
  private long totalSize = 0;
  private long startTime;

  public ImportExportMetrics( OperationType operationType ) {
    this.operationType = operationType;
    this.operationStartTime = System.currentTimeMillis();
    this.startTime = System.currentTimeMillis();

    // Initialize maps for all categories
    for ( Category cat : Category.values() ) {
      successCount.put( cat, 0 );
      successSize.put( cat, 0L );
      failureCount.put( cat, 0 );
      failureDetails.put( cat, new ArrayList<>() );
      skipCount.put( cat, 0 );
      skipReasons.put( cat, new ArrayList<>() );
      totalCount.put( cat, 0 );
    }
  }

  // ===== Success Tracking =====

  public void recordSuccess( Category category ) {
    recordSuccess( category, 0 );
  }

  public void recordSuccess( Category category, long size ) {
    successCount.put( category, successCount.get( category ) + 1 );
    successSize.put( category, successSize.get( category ) + size );
    totalCount.put( category, totalCount.get( category ) + 1 );
  }

  // ===== Failure Tracking =====

  public void recordFailure( Category category, String itemName, String reason ) {
    failureCount.put( category, failureCount.get( category ) + 1 );
    failureDetails.get( category ).add( itemName + " - " + reason );
    totalCount.put( category, totalCount.get( category ) + 1 );
  }

  public void recordFailure( Category category, String itemName, Exception e ) {
    recordFailure( category, itemName, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName() );
  }

  // ===== Skip Tracking =====

  public void recordSkip( Category category, String itemName, String reason ) {
    skipCount.put( category, skipCount.get( category ) + 1 );
    skipReasons.get( category ).add( itemName + " - " + reason );
    totalCount.put( category, totalCount.get( category ) + 1 );
  }

  // ===== Getters =====

  public int getSuccessCount( Category category ) {
    return successCount.get( category );
  }

  public long getSuccessSize( Category category ) {
    return successSize.get( category );
  }

  public int getFailureCount( Category category ) {
    return failureCount.get( category );
  }

  public List<String> getFailureDetails( Category category ) {
    return failureDetails.get( category );
  }

  public int getSkipCount( Category category ) {
    return skipCount.get( category );
  }

  public List<String> getSkipReasons( Category category ) {
    return skipReasons.get( category );
  }

  public int getTotalCount( Category category ) {
    return totalCount.get( category );
  }

  public long getOperationDuration() {
    return operationEndTime > 0 ? operationEndTime - operationStartTime : System.currentTimeMillis() - operationStartTime;
  }

  // ===== Reporting =====

  /**
   * Generate a comprehensive summary report of the operation
   */
  public String generateSummaryReport() {
    this.operationEndTime = System.currentTimeMillis();
    StringBuilder report = new StringBuilder();

    report.append( "\n" );
    report.append( "================================================================================\n" );
    report.append( "                    " ).append( operationType.name() ).append( " OPERATION SUMMARY\n" );
    report.append( "================================================================================\n" );
    report.append( "Duration: " ).append( formatDuration( getOperationDuration() ) ).append( "\n" );
    report.append( "\n" );

    // Summary by category
    report.append( "SUMMARY BY CATEGORY:\n" );
    report.append( "--------------------\n" );

    int totalSuccess = 0;
    int totalFailure = 0;
    int totalSkipped = 0;

    for ( Category category : Category.values() ) {
      int success = getSuccessCount( category );
      int failure = getFailureCount( category );
      int skipped = getSkipCount( category );
      int total = getTotalCount( category );

      if ( total > 0 ) {
        totalSuccess += success;
        totalFailure += failure;
        totalSkipped += skipped;

        report.append( "\n" ).append( category.name() ).append( ":\n" );
        report.append( "  Total:    " ).append( total ).append( "\n" );
        report.append( "  ✓ Success: " ).append( success );
        if ( category == Category.FILES && getSuccessSize( category ) > 0 ) {
          report.append( " (" ).append( formatSize( getSuccessSize( category ) ) ).append( ")" );
        }
        report.append( "\n" );
        if ( failure > 0 ) {
          report.append( "  ✗ Failed:  " ).append( failure ).append( "\n" );
        }
        if ( skipped > 0 ) {
          report.append( "  ⊘ Skipped: " ).append( skipped ).append( "\n" );
        }
      }
    }

    report.append( "\n" );
    report.append( "OVERALL TOTALS:\n" );
    report.append( "  Total Processed: " ).append( totalSuccess + totalFailure + totalSkipped ).append( "\n" );
    report.append( "  ✓ Successful:    " ).append( totalSuccess ).append( "\n" );
    if ( totalFailure > 0 ) {
      report.append( "  ✗ Failed:        " ).append( totalFailure ).append( "\n" );
    }
    if ( totalSkipped > 0 ) {
      report.append( "  ⊘ Skipped:       " ).append( totalSkipped ).append( "\n" );
    }

    report.append( "\n" );
    report.append( "================================================================================\n" );

    return report.toString();
  }

  /**
   * Generate a detailed report including all failures and skips
   */
  public String generateDetailedReport() {
    StringBuilder report = new StringBuilder();
    report.append( generateSummaryReport() );

    report.append( "\nDETAILED REPORT:\n" );
    report.append( "================================================================================\n" );

    // Failures
    boolean hasFailures = false;
    for ( Category category : Category.values() ) {
      if ( !getFailureDetails( category ).isEmpty() ) {
        if ( !hasFailures ) {
          report.append( "\nFAILURES:\n" );
          report.append( "----------\n" );
          hasFailures = true;
        }
        report.append( "\n" ).append( category.name() ).append( " Failures:\n" );
        for ( String detail : getFailureDetails( category ) ) {
          report.append( "  • " ).append( detail ).append( "\n" );
        }
      }
    }

    // Skipped
    boolean hasSkips = false;
    for ( Category category : Category.values() ) {
      if ( !getSkipReasons( category ).isEmpty() ) {
        if ( !hasSkips ) {
          report.append( "\nSKIPPED ITEMS:\n" );
          report.append( "----------\n" );
          hasSkips = true;
        }
        report.append( "\n" ).append( category.name() ).append( " Skipped:\n" );
        for ( String reason : getSkipReasons( category ) ) {
          report.append( "  • " ).append( reason ).append( "\n" );
        }
      }
    }

    if ( !hasFailures ) {
      report.append( "\nNo failures to report.\n" );
    }
    if ( !hasSkips ) {
      report.append( "\nNo skipped items to report.\n" );
    }

    report.append( "\n" );
    report.append( "================================================================================\n" );

    return report.toString();
  }

  // ===== Utility Methods =====

  private String formatDuration( long millis ) {
    long seconds = millis / 1000;
    long minutes = seconds / 60;
    long remainingSeconds = seconds % 60;

    if ( minutes > 0 ) {
      return minutes + "m " + remainingSeconds + "s";
    }
    return remainingSeconds + "s";
  }

  private String formatSize( long bytes ) {
    if ( bytes <= 0 ) return "0 B";
    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
    int digitGroups = (int) ( Math.log10( bytes ) / Math.log10( 1024 ) );
    return String.format( "%.1f %s", bytes / Math.pow( 1024, digitGroups ), units[digitGroups] );
  }

  @Override
  public String toString() {
    return generateSummaryReport();
  }
}
