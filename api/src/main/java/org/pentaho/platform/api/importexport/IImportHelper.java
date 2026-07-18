/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
package org.pentaho.platform.api.importexport;

import org.apache.commons.logging.Log;

public interface IImportHelper {

  public interface ImportContext {
    Log getLogger();
    boolean isPerformingRestore();
    boolean isOverwriteFile();
  }

  void doImport( ImportContext context ) throws ImportException;

  String getName();
}
