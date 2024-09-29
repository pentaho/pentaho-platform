/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
