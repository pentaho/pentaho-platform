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

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.List;

@WebService(
    endpointInterface = "org.pentaho.platform.repository2.unified.webservices.jaxws.IUnifiedRepositoryJaxwsWebService",
    serviceName = "unifiedRepository", portName = "unifiedRepositoryPort",
    targetNamespace = "http://www.pentaho.org/ws/1.0" )
public class DefaultUnifiedRepositoryJaxwsWebService extends DefaultUnifiedRepositoryWebService implements
    IUnifiedRepositoryJaxwsWebService {

  /**
   * Used by Metro.
   */
  public DefaultUnifiedRepositoryJaxwsWebService() {
    super();
  }

  /**
   * Used in unit test.
   */
  public DefaultUnifiedRepositoryJaxwsWebService( final IUnifiedRepository repo ) {
    super( repo );
  }

  public RepositoryFileDto createBinaryFileWithAcl( final String parentFolderId, final RepositoryFileDto file,
      final SimpleRepositoryFileDataDto simpleJaxWsData, final RepositoryFileAclDto acl, final String versionMessage ) {
    validateEtcWriteAccess( parentFolderId );
    return repositoryFileAdapter.marshal( repo.createFile( parentFolderId, repositoryFileAdapter.unmarshal( file ),
        SimpleRepositoryFileDataDto.convert( simpleJaxWsData ), repositoryFileAclAdapter.unmarshal( acl ),
        versionMessage ) );
  }

  public RepositoryFileDto createBinaryFile( final String parentFolderId, final RepositoryFileDto file,
      final SimpleRepositoryFileDataDto simpleJaxWsData, final String versionMessage ) {
    validateEtcWriteAccess( parentFolderId );
    return repositoryFileAdapter.marshal( repo.createFile( parentFolderId, repositoryFileAdapter.unmarshal( file ),
        SimpleRepositoryFileDataDto.convert( simpleJaxWsData ), versionMessage ) );
  }

  public SimpleRepositoryFileDataDto getDataAsBinaryForRead( final String fileId ) {
    SimpleRepositoryFileData simpleData = repo.getDataForRead( fileId, SimpleRepositoryFileData.class );
    return SimpleRepositoryFileDataDto.convert( simpleData );
  }

  public SimpleRepositoryFileDataDto getDataAsBinaryForReadAtVersion( final String fileId, final String versionId ) {
    SimpleRepositoryFileData simpleData =
        repo.getDataAtVersionForRead( fileId, versionId, SimpleRepositoryFileData.class );
    return SimpleRepositoryFileDataDto.convert( simpleData );
  }

  public RepositoryFileDto updateBinaryFile( final RepositoryFileDto file,
      final SimpleRepositoryFileDataDto simpleJaxWsData, final String versionMessage ) {
    return repositoryFileAdapter.marshal( repo.updateFile( repositoryFileAdapter.unmarshal( file ),
        SimpleRepositoryFileDataDto.convert( simpleJaxWsData ), versionMessage ) );
  }

  public List<SimpleRepositoryFileDataDto> getDataAsBinaryForReadInBatch( final List<RepositoryFileDto> files ) {
    List<SimpleRepositoryFileDataDto> data = new ArrayList<SimpleRepositoryFileDataDto>( files.size() );
    for ( RepositoryFileDto f : files ) {
      if ( f.getVersionId() == null ) {
        data.add( SimpleRepositoryFileDataDto
            .convert( repo.getDataForRead( f.getId(), SimpleRepositoryFileData.class ) ) );
      } else {
        data.add( SimpleRepositoryFileDataDto.convert( repo.getDataAtVersionForExecute( f.getId(), f.getVersionId(),
            SimpleRepositoryFileData.class ) ) );
      }
    }
    return data;
  }
}
