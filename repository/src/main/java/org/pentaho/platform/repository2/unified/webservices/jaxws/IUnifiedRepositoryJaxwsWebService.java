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


package org.pentaho.platform.repository2.unified.webservices.jaxws;

import org.pentaho.platform.repository2.unified.webservices.IUnifiedRepositoryWebService;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;

import jakarta.jws.WebService;
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
