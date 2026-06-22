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

package org.pentaho.platform.plugin.services.exporter;

import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;

/**
 * Facade for metrics collection to be used in exporter operations.
 * Wraps ImportExportMetrics with BACKUP operation type.
 */
public class BackupMetricsCollector {

  private ImportExportMetrics metrics;

  public BackupMetricsCollector() {
    this.metrics = new ImportExportMetrics( ImportExportMetrics.OperationType.BACKUP );
  }

  // ===== Files =====

  public void recordFileSuccess( String fileName, long fileSize ) {
    metrics.recordSuccess( ImportExportMetrics.Category.FILES, fileSize );
  }

  public void recordFileFailure( String fileName, String reason ) {
    metrics.recordFailure( ImportExportMetrics.Category.FILES, fileName, reason );
  }

  public void recordFileSkipped( String fileName, String reason ) {
    metrics.recordSkip( ImportExportMetrics.Category.FILES, fileName, reason );
  }

  // ===== Schedules =====

  public void recordScheduleSuccess( String scheduleName ) {
    metrics.recordSuccess( ImportExportMetrics.Category.SCHEDULES );
  }

  public void recordScheduleFailure( String scheduleName, String reason ) {
    metrics.recordFailure( ImportExportMetrics.Category.SCHEDULES, scheduleName, reason );
  }

  public void recordScheduleSkipped( String scheduleName, String reason ) {
    metrics.recordSkip( ImportExportMetrics.Category.SCHEDULES, scheduleName, reason );
  }

  // ===== Users =====

  public void recordUserSuccess( String userName ) {
    metrics.recordSuccess( ImportExportMetrics.Category.USERS );
  }

  public void recordUserFailure( String userName, String reason ) {
    metrics.recordFailure( ImportExportMetrics.Category.USERS, userName, reason );
  }

  // ===== Roles =====

  public void recordRoleSuccess( String roleName ) {
    metrics.recordSuccess( ImportExportMetrics.Category.ROLES );
  }

  public void recordRoleFailure( String roleName, String reason ) {
    metrics.recordFailure( ImportExportMetrics.Category.ROLES, roleName, reason );
  }

  // ===== Datasources =====

  public void recordDatasourceSuccess( String datasourceName ) {
    metrics.recordSuccess( ImportExportMetrics.Category.DATASOURCES );
  }

  public void recordDatasourceFailure( String datasourceName, String reason ) {
    metrics.recordFailure( ImportExportMetrics.Category.DATASOURCES, datasourceName, reason );
  }

  // ===== Metadata =====

  public void recordMetadataSuccess( String domainId ) {
    metrics.recordSuccess( ImportExportMetrics.Category.METADATA );
  }

  public void recordMetadataFailure( String domainId, String reason ) {
    metrics.recordFailure( ImportExportMetrics.Category.METADATA, domainId, reason );
  }

  // ===== Mondrian =====

  public void recordMondrianSuccess( String catalogName ) {
    metrics.recordSuccess( ImportExportMetrics.Category.MONDRIAN );
  }

  public void recordMondrianFailure( String catalogName, String reason ) {
    metrics.recordFailure( ImportExportMetrics.Category.MONDRIAN, catalogName, reason );
  }

  // ===== Reporting =====

  public String getSummaryReport() {
    return metrics.generateSummaryReport();
  }

  public String getDetailedReport() {
    return metrics.generateDetailedReport();
  }

  public ImportExportMetrics getMetrics() {
    return metrics;
  }

  @Override
  public String toString() {
    return metrics.toString();
  }
}
