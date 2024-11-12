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


package org.pentaho.platform.plugin.services.metadata;

import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;

import java.io.InputStream;

/**
 * This interface is a temporary solution created to keep backwards compatibility prior to 6.0<br><b>Note: This
 * interface will be removed in 6.0</b>
 * @author Andrey Khayrutdinov
 */
public interface IAclAwarePentahoMetadataDomainRepositoryImporter extends IPentahoMetadataDomainRepositoryImporter {

  void storeDomain( InputStream inputStream, String domainId, boolean overwrite, RepositoryFileAcl acl )
    throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException;

  void setAclFor( String domainId, RepositoryFileAcl acl );

  RepositoryFileAcl getAclFor( String domainId );

  boolean hasAccessFor( String domainId );
}
