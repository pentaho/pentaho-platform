package org.pentaho.mantle.client;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileTreeDto;

public class RepositoryFileUtils {

  public static RepositoryFileTree convertToRepositoryFileTree(RepositoryFileTreeDto tree) {
  RepositoryFileTree fileTree = new RepositoryFileTree();
  List<RepositoryFileTree> fileList = new ArrayList<RepositoryFileTree>();
  RepositoryFile file = convertToRepositoryFile((RepositoryFileDto)tree.getFile());
  fileTree.setFile(file);
  for (RepositoryFileTreeDto treeItem : tree.getChildren()) {
    fileList.add(convertToRepositoryFileTree((RepositoryFileTreeDto)treeItem));
  }
  fileTree.setChildren(fileList);
  return fileTree;
}

public static RepositoryFile convertToRepositoryFile(RepositoryFileDto file) {
  RepositoryFile repositoryFile = new RepositoryFile();
  repositoryFile.setCreatedDate(file.getCreatedDate());
  repositoryFile.setDeletedDate(file.getDeletedDate());
  repositoryFile.setDescription(file.getDescription());
  //fileChooserFile.setDescriptionMap(file.getDescriptionMap());
  repositoryFile.setFolder(file.isFolder());
  repositoryFile.setHidden(file.isHidden());
  repositoryFile.setId(file.getId());
  repositoryFile.setLastModifiedDate(file.getLastModifiedDate());
  repositoryFile.setLocale(file.getLocale());
  repositoryFile.setLockDate(file.getLockDate());
  repositoryFile.setLocked(file.isLocked());
  repositoryFile.setLockMessage(file.getLockMessage());
  repositoryFile.setLockOwner(file.getLockOwner());
  repositoryFile.setName(file.getName());
  repositoryFile.setOriginalParentFolderId(file.getOriginalParentFolderPath());
  repositoryFile.setOriginalParentFolderPath(file.getOriginalParentFolderPath());
  repositoryFile.setOwner(file.getOwner());
  repositoryFile.setPath(file.getPath());
  repositoryFile.setTitle(file.getTitle());
  //fileChooserFile.setTitleMap(file.getTitleMap());
  //fileChooserFile.setVersioned(file.isVersioned());
  repositoryFile.setVersionId(file.getVersionId());
  return repositoryFile;
}


public static RepositoryFileTreeDto convertFromRepositoryFileTree(RepositoryFileTree tree) {
  RepositoryFileTreeDto fileTreeDto = new RepositoryFileTreeDto();
  List<RepositoryFileTreeDto> fileList = new ArrayList<RepositoryFileTreeDto>();
  RepositoryFileDto file = convertFromRepositoryFile(tree.getFile());
  fileTreeDto.setFile(file);
  for (RepositoryFileTree treeItem : tree.getChildren()) {
    fileList.add(convertFromRepositoryFileTree(treeItem));
  }
  fileTreeDto.setChildren(fileList);
  return fileTreeDto;
}

public static RepositoryFileDto convertFromRepositoryFile(RepositoryFile file) {
  RepositoryFileDto repositoryFile = new RepositoryFileDto();
  repositoryFile.setCreatedDate(file.getCreatedDate());
  repositoryFile.setDeletedDate(file.getDeletedDate());
  repositoryFile.setDescription(file.getDescription());
  //fileChooserFile.setDescriptionMap(file.getDescriptionMap());
  repositoryFile.setFolder(file.isFolder());
  repositoryFile.setHidden(file.isHidden());
  repositoryFile.setId(file.getId());
  repositoryFile.setLastModifiedDate(file.getLastModifiedDate());
  repositoryFile.setLocale(file.getLocale());
  repositoryFile.setLockDate(file.getLockDate());
  repositoryFile.setLocked(file.isLocked());
  repositoryFile.setLockMessage(file.getLockMessage());
  repositoryFile.setLockOwner(file.getLockOwner());
  repositoryFile.setName(file.getName());
  repositoryFile.setOriginalParentFolderPath(file.getOriginalParentFolderPath());
  repositoryFile.setOriginalParentFolderPath(file.getOriginalParentFolderPath());
  repositoryFile.setOwner(file.getOwner());
  repositoryFile.setPath(file.getPath());
  repositoryFile.setTitle(file.getTitle());
  //fileChooserFile.setTitleMap(file.getTitleMap());
  //fileChooserFile.setVersioned(file.isVersioned());
  repositoryFile.setVersionId(file.getVersionId());
  return repositoryFile;
}
}
