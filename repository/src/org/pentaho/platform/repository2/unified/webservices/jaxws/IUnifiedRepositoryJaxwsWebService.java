/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.webservices.jaxws;

import org.pentaho.platform.repository2.unified.webservices.IUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;

import javax.jws.WebService;
import java.util.List;

@WebService
public interface IUnifiedRepositoryJaxwsWebService extends IUnifiedRepositoryWebService {

  SimpleRepositoryFileDataDto getDataAsBinaryForRead( final String fileId );

  RepositoryFileDto createBinaryFile( final String parentFolderId, final RepositoryFileDto file,
      final SimpleRepositoryFileDataDto simpleJaxWsData, final String versionMessage );

  RepositoryFileDto createBinaryFileWithAcl( final String parentFolderId, final RepositoryFileDto file,
      final SimpleRepositoryFileDataDto simpleJaxWsData, final RepositoryFileAclDto acl, final String versionMessage );

  RepositoryFileDto updateBinaryFile( final RepositoryFileDto file, final SimpleRepositoryFileDataDto simpleJaxWsData,
      final String versionMessage );

  SimpleRepositoryFileDataDto getDataAsBinaryForReadAtVersion( final String fileId, final String versionId );

  List<SimpleRepositoryFileDataDto> getDataAsBinaryForReadInBatch( final List<RepositoryFileDto> files );
}
