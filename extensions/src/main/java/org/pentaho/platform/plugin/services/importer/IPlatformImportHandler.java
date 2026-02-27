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

import java.io.IOException;
import java.util.List;

import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;

/**
 * Implementations handle importing content into the repository.
 * 
 * User: nbaker Date: 5/29/12
 */
public interface IPlatformImportHandler {

  /**
   * Import the provided IPlatformImportBundle into the platform.
   * 
   * @param bundle
   * @throws PlatformImportException
   * @throws IOException
   * @throws DomainStorageException
   * @throws DomainAlreadyExistsException
   * @throws DomainIdNullException
   * 
   */
  void importFile( IPlatformImportBundle bundle ) throws PlatformImportException, DomainIdNullException,
    DomainAlreadyExistsException, DomainStorageException, IOException;

  List<IMimeType> getMimeTypes();
}
