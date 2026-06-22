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


package org.pentaho.platform.api.importexport;


public interface IExportHelper {

  public void doExport( Object exportArg ) throws ExportException;

  public String getName();

  /**
   * Determines if this helper should execute for the given backup profile.
   * Allows helpers to be profile-aware without the framework needing to know about them.
   *
   * @param componentOverrides the backup component configuration (null = full backup)
   *                           Implementation receives BackupComponentConfig from plugin layer
   * @return true if this helper should execute, false to skip
   */
  default boolean shouldExecute( Object componentOverrides ) {
    // Default: always execute (for backward compatibility with existing helpers)
    return true;
  }
}
