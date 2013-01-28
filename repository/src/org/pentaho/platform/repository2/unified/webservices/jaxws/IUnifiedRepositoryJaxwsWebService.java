package org.pentaho.platform.repository2.unified.webservices.jaxws;

import java.util.List;
import javax.jws.WebService;

import org.pentaho.platform.repository2.unified.webservices.IUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;

@WebService
public interface IUnifiedRepositoryJaxwsWebService extends IUnifiedRepositoryWebService {

  SimpleRepositoryFileDataDto getDataAsBinaryForRead(final String fileId);

  RepositoryFileDto createBinaryFile(final String parentFolderId, final RepositoryFileDto file,
      final SimpleRepositoryFileDataDto simpleJaxWsData, final String versionMessage);

  RepositoryFileDto createBinaryFileWithAcl(final String parentFolderId, final RepositoryFileDto file,
      final SimpleRepositoryFileDataDto simpleJaxWsData, final RepositoryFileAclDto acl, final String versionMessage);

  RepositoryFileDto updateBinaryFile(final RepositoryFileDto file, final SimpleRepositoryFileDataDto simpleJaxWsData,
      final String versionMessage);

  SimpleRepositoryFileDataDto getDataAsBinaryForReadAtVersion(final String fileId, final String versionId);

  List<SimpleRepositoryFileDataDto> getDataAsBinaryForReadInBatch(final List<RepositoryFileDto> files);
}
