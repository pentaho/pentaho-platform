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

import java.io.Serializable;
import java.util.*;

/**
 * BackupInventory - Comprehensive tracking of backup/restore operations
 * 
 * Tracks all objects processed during backup and restore operations including
 * success count, failure count, skipped count, and detailed error information.
 */
public class BackupInventory implements Serializable {

  private static final long serialVersionUID = 1L;

  // Component inventory tracking
  private Map<String, ComponentInventory> componentInventories = new LinkedHashMap<>();

  // Operation metadata
  private long operationStartTime;
  private long operationEndTime;
  private String operationType; // "BACKUP" or "RESTORE"

  // Overall statistics
  private int totalObjectsProcessed = 0;
  private int totalObjectsSuccessful = 0;
  private int totalObjectsFailed = 0;
  private int totalObjectsSkipped = 0;

  // File/Folder statistics (for export operations)
  private int totalFilesExported = 0;
  private int totalFoldersExported = 0;

  // Constructor
  public BackupInventory(String operationType) {
    this.operationType = operationType;
    this.operationStartTime = System.currentTimeMillis();
    initializeComponentInventories();
  }

  /**
   * Initialize inventory for all 7 backup components
   */
  private void initializeComponentInventories() {
    componentInventories.put("CONTENT", new ComponentInventory("CONTENT", "Repository Content"));
    componentInventories.put("USERS", new ComponentInventory("USERS", "Users & Roles"));
    componentInventories.put("DATASOURCES", new ComponentInventory("DATASOURCES", "Datasources"));
    componentInventories.put("METASTORE", new ComponentInventory("METASTORE", "Metastore"));
    componentInventories.put("SCHEDULES", new ComponentInventory("SCHEDULES", "Schedules"));
    componentInventories.put("USER_SETTINGS", new ComponentInventory("USER_SETTINGS", "User Settings"));
    componentInventories.put("MONDRIAN", new ComponentInventory("MONDRIAN", "Mondrian Schemas"));
  }

  /**
   * Track successful object processing
   */
  public void recordSuccess(String componentKey, String objectName, String objectType) {
    ComponentInventory inventory = componentInventories.get(componentKey);
    if (inventory != null) {
      inventory.recordSuccess(objectName, objectType);
      totalObjectsSuccessful++;
      totalObjectsProcessed++;
    }
  }

  /**
   * Track failed object processing
   */
  public void recordFailure(String componentKey, String objectName, String objectType, String errorMessage) {
    ComponentInventory inventory = componentInventories.get(componentKey);
    if (inventory != null) {
      inventory.recordFailure(objectName, objectType, errorMessage);
      totalObjectsFailed++;
      totalObjectsProcessed++;
    }
  }

  /**
   * Track skipped object processing
   */
  public void recordSkipped(String componentKey, String objectName, String objectType, String reason) {
    ComponentInventory inventory = componentInventories.get(componentKey);
    if (inventory != null) {
      inventory.recordSkipped(objectName, objectType, reason);
      totalObjectsSkipped++;
      totalObjectsProcessed++;
    }
  }

  /**
   * Finalize the operation and calculate timings
   */
  public void finalizeOperation() {
    this.operationEndTime = System.currentTimeMillis();
  }

  /**
   * Set file and folder export counts (called from PentahoPlatformExporter)
   */
  public void setExportFileStats(int filesExported, int foldersExported) {
    this.totalFilesExported = filesExported;
    this.totalFoldersExported = foldersExported;
  }

  /**
   * Get total files exported
   */
  public int getTotalFilesExported() {
    return totalFilesExported;
  }

  /**
   * Get total folders exported
   */
  public int getTotalFoldersExported() {
    return totalFoldersExported;
  }

  /**
   * Get total items exported (files + folders)
   */
  public int getTotalItemsExported() {
    return totalFilesExported + totalFoldersExported;
  }

  /**
   * Get detailed inventory report as formatted string
   */
  public String getDetailedReport() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("\n");
    sb.append("=".repeat(80)).append("\n");
    sb.append(String.format("BACKUP INVENTORY REPORT - %s OPERATION\n", operationType));
    sb.append("=".repeat(80)).append("\n");
    sb.append("\n");

    // Overall Statistics
    sb.append("OVERALL STATISTICS:\n");
    sb.append("-".repeat(80)).append("\n");
    sb.append(String.format("  Total Objects Processed:     %d\n", totalObjectsProcessed));
    sb.append(String.format("  Total Objects Successful:    %d (%.1f%%)\n", 
        totalObjectsSuccessful, 
        totalObjectsProcessed > 0 ? (100.0 * totalObjectsSuccessful / totalObjectsProcessed) : 0));
    sb.append(String.format("  Total Objects Failed:        %d (%.1f%%)\n", 
        totalObjectsFailed,
        totalObjectsProcessed > 0 ? (100.0 * totalObjectsFailed / totalObjectsProcessed) : 0));
    sb.append(String.format("  Total Objects Skipped:      %d\n", totalObjectsSkipped));
    
    // File/Folder statistics (if this is an export operation)
    if (totalFilesExported > 0 || totalFoldersExported > 0) {
      sb.append("\nFILE/FOLDER STATISTICS:\n");
      sb.append(String.format("  Total Files Exported:        %d\n", totalFilesExported));
      sb.append(String.format("  Total Folders Exported:      %d\n", totalFoldersExported));
      sb.append(String.format("  Total Items Exported:        %d\n", getTotalItemsExported()));
    }
    
    sb.append(String.format("\n  Operation Duration:         %d ms\n", 
        operationEndTime - operationStartTime));
    sb.append("\n");

    // Component-wise breakdown
    sb.append("COMPONENT-WISE BREAKDOWN:\n");
    sb.append("-".repeat(80)).append("\n");

    for (ComponentInventory inventory : componentInventories.values()) {
      sb.append(inventory.getReport());
    }

    sb.append("\n");
    sb.append("=".repeat(80)).append("\n");
    sb.append("END OF INVENTORY REPORT\n");
    sb.append("=".repeat(80)).append("\n");

    return sb.toString();
  }

  /**
   * Get summary line for logging
   */
  public String getSummaryLine() {
    String baseSummary = String.format(
        "Backup Summary: Total=%d, Successful=%d, Failed=%d, Skipped=%d, Duration=%dms",
        totalObjectsProcessed,
        totalObjectsSuccessful,
        totalObjectsFailed,
        totalObjectsSkipped,
        operationEndTime - operationStartTime
    );
    
    // Add file statistics for export operations
    if (totalFilesExported > 0 || totalFoldersExported > 0) {
      baseSummary += String.format(", FilesExported=%d, FoldersExported=%d, TotalItems=%d",
          totalFilesExported, totalFoldersExported, getTotalItemsExported());
    }
    
    return baseSummary;
  }

  // Getters
  public int getTotalObjectsProcessed() {
    return totalObjectsProcessed;
  }

  public int getTotalObjectsSuccessful() {
    return totalObjectsSuccessful;
  }

  public int getTotalObjectsFailed() {
    return totalObjectsFailed;
  }

  public int getTotalObjectsSkipped() {
    return totalObjectsSkipped;
  }

  public String getOperationType() {
    return operationType;
  }

  public ComponentInventory getComponentInventory(String componentKey) {
    return componentInventories.get(componentKey);
  }

  public Map<String, ComponentInventory> getAllComponentInventories() {
    return new LinkedHashMap<>(componentInventories);
  }

  public long getOperationDuration() {
    return operationEndTime - operationStartTime;
  }

  /**
   * Inner class: ComponentInventory
   * Tracks inventory for a single backup component
   */
  public static class ComponentInventory implements Serializable {
    private static final long serialVersionUID = 1L;

    private String componentKey;
    private String componentName;
    private int successCount = 0;
    private int failureCount = 0;
    private int skippedCount = 0;
    private List<InventoryItem> successItems = new ArrayList<>();
    private List<InventoryItem> failureItems = new ArrayList<>();
    private List<InventoryItem> skippedItems = new ArrayList<>();

    public ComponentInventory(String key, String name) {
      this.componentKey = key;
      this.componentName = name;
    }

    public void recordSuccess(String objectName, String objectType) {
      successCount++;
      successItems.add(new InventoryItem(objectName, objectType, null));
    }

    public void recordFailure(String objectName, String objectType, String errorMessage) {
      failureCount++;
      failureItems.add(new InventoryItem(objectName, objectType, errorMessage));
    }

    public void recordSkipped(String objectName, String objectType, String reason) {
      skippedCount++;
      skippedItems.add(new InventoryItem(objectName, objectType, reason));
    }

    public String getReport() {
      StringBuilder sb = new StringBuilder();
      int total = successCount + failureCount + skippedCount;

      sb.append(String.format("\n%s (%s):\n", componentName, componentKey));
      sb.append(String.format("  Total: %d | Success: %d | Failed: %d | Skipped: %d\n",
          total, successCount, failureCount, skippedCount));

      // Detailed failure listing
      if (!failureItems.isEmpty()) {
        sb.append("  FAILURES:\n");
        for (InventoryItem item : failureItems) {
          sb.append(String.format("    - %s (%s): %s\n", 
              item.objectName, item.objectType, item.detailMessage));
        }
      }

      // Detailed skipped listing
      if (!skippedItems.isEmpty()) {
        sb.append("  SKIPPED:\n");
        for (InventoryItem item : skippedItems) {
          sb.append(String.format("    - %s (%s): %s\n", 
              item.objectName, item.objectType, item.detailMessage));
        }
      }

      return sb.toString();
    }

    // Getters
    public String getComponentKey() {
      return componentKey;
    }

    public String getComponentName() {
      return componentName;
    }

    public int getSuccessCount() {
      return successCount;
    }

    public int getFailureCount() {
      return failureCount;
    }

    public int getSkippedCount() {
      return skippedCount;
    }

    public List<InventoryItem> getSuccessItems() {
      return new ArrayList<>(successItems);
    }

    public List<InventoryItem> getFailureItems() {
      return new ArrayList<>(failureItems);
    }

    public List<InventoryItem> getSkippedItems() {
      return new ArrayList<>(skippedItems);
    }

    public int getTotalCount() {
      return successCount + failureCount + skippedCount;
    }
  }

  /**
   * Inner class: InventoryItem
   * Represents a single item in inventory
   */
  public static class InventoryItem implements Serializable {
    private static final long serialVersionUID = 1L;

    public String objectName;
    public String objectType;
    public String detailMessage;
    public long timestamp;

    public InventoryItem(String objectName, String objectType, String detailMessage) {
      this.objectName = objectName;
      this.objectType = objectType;
      this.detailMessage = detailMessage;
      this.timestamp = System.currentTimeMillis();
    }

    public String getObjectName() {
      return objectName;
    }

    public String getObjectType() {
      return objectType;
    }

    public String getDetailMessage() {
      return detailMessage;
    }

    public long getTimestamp() {
      return timestamp;
    }
  }
}
