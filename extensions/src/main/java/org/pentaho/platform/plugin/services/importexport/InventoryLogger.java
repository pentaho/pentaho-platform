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

import org.pentaho.platform.api.util.IRepositoryExportLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InventoryLogger - Utility class for logging backup inventory details
 * 
 * Handles formatting and logging of inventory information to both
 * the RepositoryExportLogger and standard SLF4J logger.
 */
public class InventoryLogger {

  private static final Logger log = LoggerFactory.getLogger(InventoryLogger.class);

  private IRepositoryExportLogger exportLogger;
  private BackupInventory inventory;
  private boolean detailedLogging;

  public InventoryLogger(IRepositoryExportLogger exportLogger, BackupInventory inventory) {
    this(exportLogger, inventory, true);
  }

  public InventoryLogger(IRepositoryExportLogger exportLogger, BackupInventory inventory, boolean detailedLogging) {
    this.exportLogger = exportLogger;
    this.inventory = inventory;
    this.detailedLogging = detailedLogging;
  }

  /**
   * Log the beginning of a component export
   */
  public void logComponentStart(String componentName, int expectedCount) {
    String message = String.format("Starting backup of %s (%d objects expected)", componentName, expectedCount);
    if (exportLogger != null) {
      exportLogger.info(message);
    }
    log.info(message);
  }

  /**
   * Log successful object backup
   */
  public void logObjectSuccess(String componentKey, String objectName, String objectType) {
    if (detailedLogging) {
      String message = String.format("[%s] Successfully backed up %s: %s", componentKey, objectType, objectName);
      if (exportLogger != null) {
        exportLogger.debug(message);
      }
      log.debug(message);
    }
  }

  /**
   * Log failed object backup
   */
  public void logObjectFailure(String componentKey, String objectName, String objectType, String errorMessage) {
    String message = String.format("[%s] FAILED to back up %s: %s - Error: %s", 
        componentKey, objectType, objectName, errorMessage);
    if (exportLogger != null) {
      exportLogger.warn(message);
    }
    log.warn(message);

    // Record in inventory
    if (inventory != null) {
      inventory.recordFailure(componentKey, objectName, objectType, errorMessage);
    }
  }

  /**
   * Log skipped object backup
   */
  public void logObjectSkipped(String componentKey, String objectName, String objectType, String reason) {
    if (detailedLogging) {
      String message = String.format("[%s] Skipped %s: %s - Reason: %s", 
          componentKey, objectType, objectName, reason);
      if (exportLogger != null) {
        exportLogger.info(message);
      }
      log.info(message);
    }

    // Record in inventory
    if (inventory != null) {
      inventory.recordSkipped(componentKey, objectName, objectType, reason);
    }
  }

  /**
   * Log component completion summary
   */
  public void logComponentComplete(String componentName, String componentKey, int successful, int failed, int skipped) {
    String message = String.format(
        "Completed backup of %s - Success: %d, Failed: %d, Skipped: %d",
        componentName, successful, failed, skipped);
    if (exportLogger != null) {
      exportLogger.info(message);
    }
    log.info(message);
  }

  /**
   * Log overall operation summary
   */
  public void logOperationComplete() {
    if (inventory != null) {
      inventory.finalizeOperation();
      String summaryLine = inventory.getSummaryLine();
      
      if (exportLogger != null) {
        exportLogger.info(summaryLine);
      }
      log.info(summaryLine);

      // Verbose detailed report removed - it added no value and showed all zeros
      // when actual exports were successful. Summary line above is sufficient.
    }
  }

  /**
   * Log a detailed inventory report section
   */
  public void logDetailedInventory(String componentName, BackupInventory.ComponentInventory componentInventory) {
    if (componentInventory != null && detailedLogging) {
      StringBuilder sb = new StringBuilder();
      sb.append("\n");
      sb.append("DETAILED INVENTORY FOR ").append(componentName).append(":\n");
      sb.append("-".repeat(80)).append("\n");
      
      // Success items
      if (componentInventory.getSuccessCount() > 0) {
        sb.append(String.format("Successfully Backed Up (%d items):\n", componentInventory.getSuccessCount()));
        for (BackupInventory.InventoryItem item : componentInventory.getSuccessItems()) {
          sb.append(String.format("  ✓ %s (%s)\n", item.getObjectName(), item.getObjectType()));
        }
        sb.append("\n");
      }

      // Failure items
      if (componentInventory.getFailureCount() > 0) {
        sb.append(String.format("Failed to Back Up (%d items):\n", componentInventory.getFailureCount()));
        for (BackupInventory.InventoryItem item : componentInventory.getFailureItems()) {
          sb.append(String.format("  ✗ %s (%s): %s\n", 
              item.getObjectName(), item.getObjectType(), item.getDetailMessage()));
        }
        sb.append("\n");
      }

      // Skipped items
      if (componentInventory.getSkippedCount() > 0) {
        sb.append(String.format("Skipped (%d items):\n", componentInventory.getSkippedCount()));
        for (BackupInventory.InventoryItem item : componentInventory.getSkippedItems()) {
          sb.append(String.format("  ~ %s (%s): %s\n", 
              item.getObjectName(), item.getObjectType(), item.getDetailMessage()));
        }
        sb.append("\n");
      }

      String report = sb.toString();
      if (exportLogger != null) {
        exportLogger.info(report);
      }
      log.info(report);
    }
  }

  /**
   * Get the current inventory
   */
  public BackupInventory getInventory() {
    return inventory;
  }

  /**
   * Set detailed logging level
   */
  public void setDetailedLogging(boolean detailed) {
    this.detailedLogging = detailed;
  }

  /**
   * Format a count line for logging
   */
  public static String formatCountLine(String componentName, int total, int successful, int failed, int skipped) {
    return String.format(
        "%s - Total: %d | Successful: %d | Failed: %d | Skipped: %d | Success Rate: %.1f%%",
        componentName,
        total,
        successful,
        failed,
        skipped,
        total > 0 ? (100.0 * successful / total) : 0
    );
  }
}
