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


package org.pentaho.platform.plugin.services.importer;

import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;

import java.util.Map;

/**
 * Implementations handle importing the given content into the Hitachi Vantara System.
 * <p/>
 * User: nbaker Date: 6/18/12
 */
public interface IPlatformImporter {
  /**
   * Import the given IPlatformImportBundle into the system.
   *
   * @param bundle
   * @throws PlatformImportException
   */
  void importFile( IPlatformImportBundle bundle ) throws PlatformImportException;

  IRepositoryImportLogger getRepositoryImportLogger();

  /**
   * Add a new IPlatformImportHandler to process the MimeTypes given by that import handler.  An
   * Import Handler registers what MimeTypes it processes via the getMimeTypes() method.  This
   * IPlatformImporter then adds the MimeTypes to its master list, and, adds the extensions handled
   * by the mimetypes to the IPlatformImportResolver managed by this class.
   */
  void addHandler( IPlatformImportHandler platformImportHandler );

  /**
   * Returns the platform importer's map of registered handlers
   */
  Map<String, IPlatformImportHandler> getHandlers();
}
