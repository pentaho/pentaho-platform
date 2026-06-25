/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.plugin.services.metadata;

import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;

import java.io.InputStream;

public interface IPentahoMondrianDomainRepositoryImporter {

  public void storeDomain( final InputStream inputStream, final String domainId, final boolean overwrite )
    throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException;;

}
