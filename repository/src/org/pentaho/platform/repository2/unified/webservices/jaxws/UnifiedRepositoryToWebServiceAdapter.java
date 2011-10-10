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
 */
package org.pentaho.platform.repository2.unified.webservices.jaxws;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository2.unified.webservices.IUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.NodeRepositoryFileDataAdapter;
import org.pentaho.platform.repository2.unified.webservices.NodeRepositoryFileDataDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAceAdapter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAdapter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAdapter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileTreeAdapter;
import org.pentaho.platform.repository2.unified.webservices.VersionSummaryAdapter;
import org.pentaho.platform.repository2.unified.webservices.VersionSummaryDto;

/**
 * Converts calls to {@link IUnifiedRepository} into {@link IUnifiedRepositoryWebService}. This is how client code 
 * remains unaware of server code location.
 * 
 * @author mlowery
 */
public class UnifiedRepositoryToWebServiceAdapter implements IUnifiedRepository {

  private IUnifiedRepositoryJaxwsWebService repoWebService;

  private RepositoryFileAdapter repositoryFileAdapter = new RepositoryFileAdapter();

  private RepositoryFileTreeAdapter repositoryFileTreeAdapter = new RepositoryFileTreeAdapter();

  private NodeRepositoryFileDataAdapter nodeRepositoryFileDataAdapter = new NodeRepositoryFileDataAdapter();

  private RepositoryFileAclAdapter repositoryFileAclAdapter = new RepositoryFileAclAdapter();

  private RepositoryFileAclAceAdapter repositoryFileAclAceAdapter = new RepositoryFileAclAceAdapter();

  private VersionSummaryAdapter versionSummaryAdapter = new VersionSummaryAdapter();

  public UnifiedRepositoryToWebServiceAdapter(IUnifiedRepositoryJaxwsWebService repoWebService) {
    super();
    this.repoWebService = repoWebService;
  }

  public RepositoryFile createFile(Serializable parentFolderId, RepositoryFile file, IRepositoryFileData data,
      String versionMessage) {
    if (data instanceof NodeRepositoryFileData) {
      return repositoryFileAdapter.unmarshal(repoWebService.createFile(parentFolderId != null ? parentFolderId
          .toString() : null, repositoryFileAdapter.marshal(file), nodeRepositoryFileDataAdapter
          .marshal((NodeRepositoryFileData) data), versionMessage));
    } else if (data instanceof SimpleRepositoryFileData) {
      SimpleRepositoryFileData simpleData = (SimpleRepositoryFileData) data;
      return repositoryFileAdapter.unmarshal(repoWebService.createBinaryFile(parentFolderId != null ? parentFolderId
          .toString() : null, repositoryFileAdapter.marshal(file), SimpleRepositoryFileDataDto.convert(simpleData),
          versionMessage));
    } else {
      throw new IllegalArgumentException();
    }
  }

  public RepositoryFile createFolder(Serializable parentFolderId, RepositoryFile file, String versionMessage) {
    return repositoryFileAdapter.unmarshal(repoWebService.createFolder(parentFolderId != null ? parentFolderId
        .toString() : null, repositoryFileAdapter.marshal(file), versionMessage));
  }

  public void deleteFile(Serializable fileId, boolean permanent, String versionMessage) {
    repoWebService.deleteFileWithPermanentFlag(fileId != null ? fileId.toString() : null, permanent, versionMessage);
  }

  public void deleteFile(Serializable fileId, String versionMessage) {
    repoWebService.deleteFile(fileId != null ? fileId.toString() : null, versionMessage);
  }

  public void deleteFileAtVersion(Serializable fileId, Serializable versionId) {
    repoWebService.deleteFileAtVersion(fileId != null ? fileId.toString() : null,
        versionId.toString() != null ? versionId.toString() : null);
  }

  public RepositoryFileAcl getAcl(Serializable fileId) {
    return repositoryFileAclAdapter.unmarshal(repoWebService.getAcl(fileId != null ? fileId.toString() : null));
  }

  public List<RepositoryFile> getChildren(Serializable folderId) {
    return unmarshalFiles(repoWebService.getChildren(folderId.toString() != null ? folderId.toString() : null));
  }

  private List<RepositoryFile> unmarshalFiles(List<RepositoryFileDto> dtos) {
    List<RepositoryFile> files = new ArrayList<RepositoryFile>();
    for (RepositoryFileDto dto : dtos) {
      files.add(repositoryFileAdapter.unmarshal(dto));
    }
    return files;
  }

  public List<RepositoryFile> getChildren(Serializable folderId, String filter) {
    return unmarshalFiles(repoWebService.getChildrenWithFilter(
        folderId.toString() != null ? folderId.toString() : null, filter));
  }

  public <T extends IRepositoryFileData> T getDataForExecute(Serializable fileId, Class<T> dataClass) {
    throw new UnsupportedOperationException();
  }

  public <T extends IRepositoryFileData> List<T> getDataForExecuteInBatch(
      final List<RepositoryFile> files, Class<T> dataClass) {
    throw new UnsupportedOperationException();
  }

  public <T extends IRepositoryFileData> T getDataAtVersionForExecute(Serializable fileId, Serializable versionId,
      Class<T> dataClass) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  public <T extends IRepositoryFileData> T getDataForRead(Serializable fileId, Class<T> dataClass) {
    if (dataClass.equals(NodeRepositoryFileData.class)) {
      return (T) nodeRepositoryFileDataAdapter.unmarshal(repoWebService.getDataAsNodeForRead(fileId != null ? fileId
          .toString() : null));
    } else if (dataClass.equals(SimpleRepositoryFileData.class)) {
      SimpleRepositoryFileDataDto simpleJaxWsData = repoWebService.getDataAsBinaryForRead(fileId != null ? fileId
          .toString() : null);
      return (T) SimpleRepositoryFileDataDto.convert(simpleJaxWsData);
    } else {
      throw new IllegalArgumentException();
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends IRepositoryFileData> java.util.List<T> getDataForReadInBatch(final List<RepositoryFile> files, final Class<T> dataClass) {
    List<RepositoryFileDto> fileDtos = new ArrayList<RepositoryFileDto>(files.size());
    for(RepositoryFile file : files) {
      fileDtos.add(repositoryFileAdapter.marshal(file));
    }
    if (dataClass.equals(NodeRepositoryFileData.class)) {
      List<NodeRepositoryFileDataDto> nodeData = repoWebService.getDataAsNodeForReadInBatch(fileDtos);
      List<T> data = new ArrayList<T>(nodeData.size());
      for (NodeRepositoryFileDataDto node : nodeData) {
        data.add((T) nodeRepositoryFileDataAdapter.unmarshal(node));
      }
      return data;
    } else if (dataClass.equals(SimpleRepositoryFileData.class)) {
      List<SimpleRepositoryFileDataDto> nodeData = repoWebService.getDataAsBinaryForReadInBatch(fileDtos);
      List<T> data = new ArrayList<T>(nodeData.size());
      for (SimpleRepositoryFileDataDto node : nodeData) {
        data.add((T) SimpleRepositoryFileDataDto.convert(node));
      }
      return data;
    } else {
      throw new IllegalArgumentException();
    }
  } 

  @SuppressWarnings("unchecked")
  public <T extends IRepositoryFileData> T getDataAtVersionForRead(Serializable fileId, Serializable versionId,
      Class<T> dataClass) {
    if (dataClass.equals(NodeRepositoryFileData.class)) {
      return (T) nodeRepositoryFileDataAdapter.unmarshal(repoWebService.getDataAsNodeForReadAtVersion(
          fileId != null ? fileId.toString() : null, versionId != null ? versionId.toString() : null));
    } else if (dataClass.equals(SimpleRepositoryFileData.class)) {
      SimpleRepositoryFileDataDto simpleJaxWsData = repoWebService.getDataAsBinaryForReadAtVersion(
          fileId != null ? fileId.toString() : null, versionId != null ? versionId.toString() : null);
      return (T) SimpleRepositoryFileDataDto.convert(simpleJaxWsData);
    } else {
      throw new IllegalArgumentException();
    }
  }

  public List<RepositoryFile> getDeletedFiles(final String origParentFolderPath) {
    return unmarshalFiles(repoWebService.getDeletedFilesInFolder(origParentFolderPath));
  }

  public List<RepositoryFile> getDeletedFiles(final String origParentFolderPath, String filter) {
    return unmarshalFiles(repoWebService.getDeletedFilesInFolderWithFilter(origParentFolderPath, filter));
  }

  public List<RepositoryFile> getDeletedFiles() {
    return unmarshalFiles(repoWebService.getDeletedFiles());
  }

  public List<RepositoryFileAce> getEffectiveAces(Serializable fileId) {
    return unmarshalAces(repoWebService.getEffectiveAces(fileId != null ? fileId.toString() : null));
  }

  private List<RepositoryFileAce> unmarshalAces(List<RepositoryFileAclAceDto> dtos) {
    List<RepositoryFileAce> aces = new ArrayList<RepositoryFileAce>();
    for (RepositoryFileAclAceDto dto : dtos) {
      aces.add(repositoryFileAclAceAdapter.unmarshal(dto));
    }
    return aces;
  }

  public List<RepositoryFileAce> getEffectiveAces(Serializable fileId, boolean forceEntriesInheriting) {
    return unmarshalAces(repoWebService.getEffectiveAcesWithForceFlag(fileId != null ? fileId.toString() : null,
        forceEntriesInheriting));
  }

  public RepositoryFile getFile(String path) {
    path = path.replaceAll(";", "/");
    return repositoryFileAdapter.unmarshal(repoWebService.getFile(path));
  }

  public RepositoryFile getFile(String path, boolean loadLocaleMaps) {
    throw new UnsupportedOperationException();
  }

  public RepositoryFile getFileAtVersion(Serializable fileId, Serializable versionId) {
    return repositoryFileAdapter.unmarshal(repoWebService.getFileAtVersion(fileId != null ? fileId.toString() : null,
        versionId != null ? versionId.toString() : null));
  }

  public RepositoryFile getFileById(Serializable fileId) {
    return repositoryFileAdapter.unmarshal(repoWebService.getFileById(fileId != null ? fileId.toString() : null));
  }

  public RepositoryFile getFileById(Serializable fileId, boolean loadLocaleMaps) {
    throw new UnsupportedOperationException();
  }

  public List<VersionSummary> getVersionSummaries(Serializable fileId) {
    return unmarshalVersionSummaries(repoWebService.getVersionSummaries(fileId != null ? fileId.toString() : null));
  }

  private List<VersionSummary> unmarshalVersionSummaries(List<VersionSummaryDto> dtos) {
    List<VersionSummary> versionSummaries = new ArrayList<VersionSummary>();
    for (VersionSummaryDto dto : dtos) {
      versionSummaries.add(versionSummaryAdapter.unmarshal(dto));
    }
    return versionSummaries;
  }

  public VersionSummary getVersionSummary(Serializable fileId, Serializable versionId) {
    return versionSummaryAdapter.unmarshal(repoWebService.getVersionSummary(fileId != null ? fileId.toString() : null,
        versionId != null ? versionId.toString() : null));
  }

  public List<VersionSummary> getVersionSummaryInBatch(final List<RepositoryFile> files) {
    List<RepositoryFileDto> fileDtos = new ArrayList<RepositoryFileDto>(files.size());
    for (RepositoryFile file : files) {
      fileDtos.add(repositoryFileAdapter.marshal(file));
    }
    return unmarshalVersionSummaries(repoWebService.getVersionSummaryInBatch(fileDtos));    
  }

  public boolean hasAccess(String path, EnumSet<RepositoryFilePermission> permissions) {
    return repoWebService.hasAccess(path, RepositoryFileAclAceAdapter.toIntPerms(permissions));
  }

  public void lockFile(Serializable fileId, String message) {
    repoWebService.lockFile(fileId != null ? fileId.toString() : null, message);
  }

  public void moveFile(Serializable fileId, String destAbsPath, String versionMessage) {
    repoWebService.moveFile(fileId != null ? fileId.toString() : null, destAbsPath, versionMessage);
  }
  
  public void copyFile(Serializable fileId, String destAbsPath, String versionMessage) {
    repoWebService.copyFile(fileId != null ? fileId.toString() : null, destAbsPath, versionMessage);
  }

  public void undeleteFile(Serializable fileId, String versionMessage) {
    repoWebService.undeleteFile(fileId != null ? fileId.toString() : null, versionMessage);
  }

  public void unlockFile(Serializable fileId) {
    repoWebService.unlockFile(fileId != null ? fileId.toString() : null);
  }

  public RepositoryFileAcl updateAcl(RepositoryFileAcl acl) {
    return repositoryFileAclAdapter.unmarshal(repoWebService.updateAcl(repositoryFileAclAdapter.marshal(acl)));
  }

  public RepositoryFile updateFile(RepositoryFile file, IRepositoryFileData data, String versionMessage) {
    if (data instanceof NodeRepositoryFileData) {
      return repositoryFileAdapter.unmarshal(repoWebService.updateFile(repositoryFileAdapter.marshal(file),
          nodeRepositoryFileDataAdapter.marshal((NodeRepositoryFileData) data), versionMessage));
    } else if (data instanceof SimpleRepositoryFileData) {
      SimpleRepositoryFileData simpleData = (SimpleRepositoryFileData) data;
      return repositoryFileAdapter.unmarshal(repoWebService.updateBinaryFile(repositoryFileAdapter.marshal(file),
          SimpleRepositoryFileDataDto.convert(simpleData), versionMessage));
    } else {
      throw new IllegalArgumentException();
    }
  }

  public void restoreFileAtVersion(Serializable fileId, Serializable versionId, String versionMessage) {
    repoWebService.restoreFileAtVersion(fileId.toString(), versionId.toString(), versionMessage);
  }

  public boolean canUnlockFile(final Serializable fileId) {
    return repoWebService.canUnlockFile(fileId.toString());
  }

  public RepositoryFileTree getTree(final String path, final int depth, final String filter, final boolean showHidden) {
    return repositoryFileTreeAdapter.unmarshal(repoWebService.getTree(path, depth, filter, showHidden));
  }

  public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final RepositoryFileAcl acl, final String versionMessage) {
    if (data instanceof NodeRepositoryFileData) {
      return repositoryFileAdapter.unmarshal(repoWebService.createFileWithAcl(parentFolderId != null ? parentFolderId
          .toString() : null, repositoryFileAdapter.marshal(file), nodeRepositoryFileDataAdapter
          .marshal((NodeRepositoryFileData) data), repositoryFileAclAdapter.marshal(acl), versionMessage));
    } else if (data instanceof SimpleRepositoryFileData) {
      SimpleRepositoryFileData simpleData = (SimpleRepositoryFileData) data;
      return repositoryFileAdapter.unmarshal(repoWebService.createBinaryFileWithAcl(
          parentFolderId != null ? parentFolderId.toString() : null, repositoryFileAdapter.marshal(file),
          SimpleRepositoryFileDataDto.convert(simpleData), repositoryFileAclAdapter.marshal(acl), versionMessage));
    } else {
      throw new IllegalArgumentException();
    }
  }

  public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file,
      final RepositoryFileAcl acl, final String versionMessage) {
    return repositoryFileAdapter
        .unmarshal(repoWebService.createFolderWithAcl(parentFolderId != null ? parentFolderId.toString() : null,
            repositoryFileAdapter.marshal(file), repositoryFileAclAdapter.marshal(acl), versionMessage));
  }

  public List<RepositoryFile> getReferrers(Serializable fileId) {
    List<RepositoryFile> fileList = new ArrayList<RepositoryFile>();
    
    for( RepositoryFileDto fileDto : repoWebService.getReferrers(fileId != null ? fileId.toString() : null)) {
      fileList.add(repositoryFileAdapter.unmarshal(fileDto));
    }
    return fileList;
  }
  
  public void setFileMetadata(final Serializable fileId, Map<String, Serializable> metadataMap) {
//    repoWebService.setFileMetadata(fileId, (FileMetadataMap) metadataMap);
  }
  
  public Map<String, Serializable> getFileMetadata(final Serializable fileId) {
//    return repoWebService.getFileMetadata(fileId);
    return null;
  }

}
