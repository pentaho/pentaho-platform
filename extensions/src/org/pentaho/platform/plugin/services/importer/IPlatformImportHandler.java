/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importer;

import java.io.IOException;
import java.util.List;

import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.plugin.services.importer.mimeType.MimeType;

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

  List<MimeType> getMimeTypes();
}
