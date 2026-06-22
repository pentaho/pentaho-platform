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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.util.IRepositoryExportLogger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * BackupInventoryTest - Tests for comprehensive inventory tracking
 */
public class BackupInventoryTest {

  private BackupInventory inventory;
  private IRepositoryExportLogger mockLogger;

  @Before
  public void setUp() {
    inventory = new BackupInventory("BACKUP");
    mockLogger = mock(IRepositoryExportLogger.class);
  }

  @Test
  public void testInventoryCreation() {
    assertNotNull(inventory);
    assertEquals("BACKUP", inventory.getOperationType());
    assertEquals(0, inventory.getTotalObjectsProcessed());
    assertEquals(0, inventory.getTotalObjectsSuccessful());
  }

  @Test
  public void testRecordSuccess() {
    inventory.recordSuccess("DATASOURCES", "SalesDB", "DATASOURCE");
    inventory.recordSuccess("DATASOURCES", "AnalyticsDB", "DATASOURCE");

    assertEquals(2, inventory.getTotalObjectsProcessed());
    assertEquals(2, inventory.getTotalObjectsSuccessful());
    assertEquals(0, inventory.getTotalObjectsFailed());

    BackupInventory.ComponentInventory dsInventory = inventory.getComponentInventory("DATASOURCES");
    assertEquals(2, dsInventory.getSuccessCount());
    assertEquals(2, dsInventory.getSuccessItems().size());
  }

  @Test
  public void testRecordFailure() {
    inventory.recordFailure("DATASOURCES", "BadDB", "DATASOURCE", "Connection timeout");

    assertEquals(1, inventory.getTotalObjectsProcessed());
    assertEquals(0, inventory.getTotalObjectsSuccessful());
    assertEquals(1, inventory.getTotalObjectsFailed());

    BackupInventory.ComponentInventory dsInventory = inventory.getComponentInventory("DATASOURCES");
    assertEquals(1, dsInventory.getFailureCount());
    assertEquals(1, dsInventory.getFailureItems().size());
    assertEquals("Connection timeout", dsInventory.getFailureItems().get(0).getDetailMessage());
  }

  @Test
  public void testRecordSkipped() {
    inventory.recordSkipped("DATASOURCES", "TempDB", "DATASOURCE", "Filtered out");

    assertEquals(1, inventory.getTotalObjectsProcessed());
    assertEquals(0, inventory.getTotalObjectsSuccessful());
    assertEquals(0, inventory.getTotalObjectsFailed());
    assertEquals(1, inventory.getTotalObjectsSkipped());

    BackupInventory.ComponentInventory dsInventory = inventory.getComponentInventory("DATASOURCES");
    assertEquals(1, dsInventory.getSkippedCount());
  }

  @Test
  public void testMixedOperations() {
    // Record 10 datasources
    for (int i = 0; i < 8; i++) {
      inventory.recordSuccess("DATASOURCES", "DB" + i, "DATASOURCE");
    }
    inventory.recordFailure("DATASOURCES", "BadDB", "DATASOURCE", "Error");
    inventory.recordSkipped("DATASOURCES", "TempDB", "DATASOURCE", "Filter");

    // Record users
    inventory.recordSuccess("USERS", "admin", "USER");
    inventory.recordSuccess("USERS", "user1", "USER");

    // Record Mondrian
    inventory.recordSuccess("MONDRIAN", "SalesAnalysis", "CATALOG");

    assertEquals(13, inventory.getTotalObjectsProcessed());
    assertEquals(11, inventory.getTotalObjectsSuccessful());
    assertEquals(1, inventory.getTotalObjectsFailed());
    assertEquals(1, inventory.getTotalObjectsSkipped());
  }

  @Test
  public void testComponentInventorySummary() {
    inventory.recordSuccess("USERS", "admin", "USER");
    inventory.recordSuccess("USERS", "user1", "USER");
    inventory.recordFailure("USERS", "guest", "USER", "Permission denied");

    BackupInventory.ComponentInventory usersInventory = inventory.getComponentInventory("USERS");
    assertEquals("USERS", usersInventory.getComponentKey());
    assertEquals("Users & Roles", usersInventory.getComponentName());
    assertEquals(2, usersInventory.getSuccessCount());
    assertEquals(1, usersInventory.getFailureCount());
    assertEquals(3, usersInventory.getTotalCount());
  }

  @Test
  public void testDetailedReport() {
    // Create a realistic backup scenario
    inventory.recordSuccess("DATASOURCES", "SalesDB", "DATASOURCE");
    inventory.recordSuccess("DATASOURCES", "AnalyticsDB", "DATASOURCE");
    inventory.recordFailure("DATASOURCES", "ErrorDB", "DATASOURCE", "Connection failed");
    
    inventory.recordSuccess("USERS", "admin", "USER");
    inventory.recordSuccess("USERS", "user1", "USER");
    
    inventory.recordSuccess("MONDRIAN", "Sales", "CATALOG");
    inventory.recordSkipped("MONDRIAN", "Temp", "CATALOG", "Temporary catalog");

    inventory.recordSuccess("METASTORE", "Repository Metastore", "METASTORE");

    inventory.finalizeOperation();

    String report = inventory.getDetailedReport();
    assertNotNull(report);
    assertTrue(report.contains("BACKUP INVENTORY REPORT"));
    assertTrue(report.contains("Total Objects Processed:     9"));
    assertTrue(report.contains("Total Objects Successful:    7"));
    assertTrue(report.contains("Total Objects Failed:        1"));
    assertTrue(report.contains("Total Objects Skipped:       1"));
    assertTrue(report.contains("DATASOURCES"));
    assertTrue(report.contains("USERS"));
    assertTrue(report.contains("MONDRIAN"));
  }

  @Test
  public void testSummaryLine() {
    for (int i = 0; i < 10; i++) {
      inventory.recordSuccess("DATASOURCES", "DB" + i, "DATASOURCE");
    }
    inventory.recordFailure("DATASOURCES", "BadDB", "DATASOURCE", "Error");
    inventory.finalizeOperation();

    String summary = inventory.getSummaryLine();
    assertNotNull(summary);
    assertTrue(summary.contains("Total=11"));
    assertTrue(summary.contains("Successful=10"));
    assertTrue(summary.contains("Failed=1"));
  }

  @Test
  public void testOperationDuration() {
    long startTime = System.currentTimeMillis();
    
    inventory.recordSuccess("DATASOURCES", "DB1", "DATASOURCE");
    inventory.recordSuccess("DATASOURCES", "DB2", "DATASOURCE");
    
    // Simulate some delay
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // ignore
    }
    
    inventory.finalizeOperation();
    
    long duration = inventory.getOperationDuration();
    assertTrue(duration >= 100);
  }

  @Test
  public void testInventoryItemDetails() {
    inventory.recordSuccess("DATASOURCES", "SalesDB", "DATASOURCE");
    
    BackupInventory.ComponentInventory dsInventory = inventory.getComponentInventory("DATASOURCES");
    BackupInventory.InventoryItem item = dsInventory.getSuccessItems().get(0);
    
    assertEquals("SalesDB", item.getObjectName());
    assertEquals("DATASOURCE", item.getObjectType());
    assertNull(item.getDetailMessage());
    assertTrue(item.getTimestamp() > 0);
  }

  @Test
  public void testMultipleComponentsTracking() {
    // Ensure all 7 components are initialized and trackable
    String[] components = {"CONTENT", "USERS", "DATASOURCES", "METASTORE", "SCHEDULES", "USER_SETTINGS", "MONDRIAN"};
    
    for (String component : components) {
      BackupInventory.ComponentInventory comp = inventory.getComponentInventory(component);
      assertNotNull("Component " + component + " not initialized", comp);
    }
  }

  @Test
  public void testInventoryItemTracking() {
    inventory.recordSuccess("DATASOURCES", "DB1", "DATASOURCE");
    inventory.recordSuccess("DATASOURCES", "DB2", "DATASOURCE");
    inventory.recordFailure("DATASOURCES", "DB3", "DATASOURCE", "Connection error");

    BackupInventory.ComponentInventory dsInventory = inventory.getComponentInventory("DATASOURCES");
    
    // Verify success items
    assertEquals(2, dsInventory.getSuccessItems().size());
    assertEquals("DB1", dsInventory.getSuccessItems().get(0).getObjectName());
    assertEquals("DB2", dsInventory.getSuccessItems().get(1).getObjectName());
    
    // Verify failure items
    assertEquals(1, dsInventory.getFailureItems().size());
    assertEquals("DB3", dsInventory.getFailureItems().get(0).getObjectName());
    assertEquals("Connection error", dsInventory.getFailureItems().get(0).getDetailMessage());
  }
}
