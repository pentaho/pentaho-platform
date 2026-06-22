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

package org.pentaho.platform.plugin.services.exporter.helper;

import org.pentaho.platform.api.importexport.ExportException;
import org.pentaho.platform.api.importexport.IExportHelper;
import org.pentaho.platform.plugin.services.exporter.PentahoPlatformExporter;

/**
 * Abstract base class for export helpers that have configurable execution.
 * Subclasses can override shouldExecute() to implement their own configuration logic.
 * This enforces the pattern where each helper decides whether to execute based on configuration.
 */
public abstract class AbstractConfigurableExportHelper implements IExportHelper {
  protected PentahoPlatformExporter exporter;

  public AbstractConfigurableExportHelper( PentahoPlatformExporter exporter ) {
    this.exporter = exporter;
  }

  /**
   * Determine if this helper should execute based on component configuration.
   * Subclasses override this to check their specific configuration.
   * 
   * @return true if the helper should execute, false otherwise
   */
  protected abstract boolean shouldExecute();

  /**
   * Get the reason this helper is being skipped (for logging).
   * Only used when shouldExecute() returns false.
   * 
   * @return description of why execution is being skipped
   */
  protected abstract String getSkipReason();

  /**
   * Template method that checks configuration before delegating to the actual export logic.
   * Final implementations call performExport() to do the actual work.
   */
  @Override
  public final void doExport( Object exportArg ) throws ExportException {
    if ( !shouldExecute() ) {
      exporter.getRepositoryExportLogger().debug( 
        "Skipping " + getName() + " export (" + getSkipReason() + ")" 
      );
      return;
    }

    try {
      performExport( exportArg );
    } catch ( ExportException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new ExportException( "Error in " + getName() + ": " + e.getMessage(), e );
    }
  }

  /**
   * Subclasses implement this to perform the actual export logic.
   * Called only if shouldExecute() returns true.
   * 
   * @param exportArg the exporter object (PentahoPlatformExporter)
   * @throws Exception if export fails
   */
  protected abstract void performExport( Object exportArg ) throws ExportException;
}
