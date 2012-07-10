package org.pentaho.platform.plugin.services.importer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.plugin.services.importexport.StreamConverter;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository.messages.Messages;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: nbaker
 * Date: 5/29/12
 */
public class RepositoryFileImportFileHandler implements IPlatformImportHandler {

  private IUnifiedRepository repository;

  private static final Log log = LogFactory.getLog(RepositoryFileImportFileHandler.class);
  private static final Messages messages = Messages.getInstance();
  private Map<String, Serializable> parentIdCache = new HashMap<String, Serializable>();
  private static StreamConverter converter = new StreamConverter();
  private boolean overwriteFile = false;
  
  public void importFile(IPlatformImportBundle bnd) throws PlatformImportException {

    if(bnd instanceof RepositoryFileImportBundle == false){
      throw new PlatformImportException("Error importing bundle. RepositoryFileImportBundle expected");
    }
    RepositoryFileImportBundle bundle = (RepositoryFileImportBundle) bnd;
    // Make sure we don't try to do anything in a system-defined folder
    final String repositoryFilePath = bundle.getPath();
    log.trace("Processing [" + repositoryFilePath + "]");

    // See if the destination already exists in the repository
    final RepositoryFile file = repository.getFile(repositoryFilePath);
    if (file != null) {
//      if (file.isFolder() != bundle.getFile().isFolder()) {
//        log.warn("Entry already exists in the repository - but it is a " + (file.isFolder() ? "folder" : "file")
//            + " and the entry to be imported is a " + (bundle.getFile().isFolder() ? "folder" : "file"));
//      }

      if (!bundle.isOverwrite()) {
        log.trace("File already exists in repository and overwrite is false - skipping");
      } else if (file.isFolder()) {
        log.trace("Folder already exists - skip");
      } else {
        // It is a file we can overwrite...
        log.trace("Updating...");
        copyFileToRepository(bundle, repositoryFilePath, file);
      }
    }

}

  protected String computeBundlePath(final RepositoryFileImportBundle bundle) {
    String bundlePath = bundle.getPath();
    bundlePath = RepositoryFilenameUtils.separatorsToRepository(bundlePath);
    if (bundlePath.startsWith(RepositoryFile.SEPARATOR)) {
      bundlePath = bundlePath.substring(1);
    }
    return bundlePath;
  }

  /**
   * Copies the file bundle into the repository
   *
   * @param bundle
   * @param repositoryPath
   * @param file
   */
  protected boolean copyFileToRepository(final RepositoryFileImportBundle bundle, final String repositoryPath,
                                         final RepositoryFile file) {
    // Compute the file extension
    final String name = bundle.getName();
    final String ext = RepositoryFilenameUtils.getExtension(name);
    if (StringUtils.isEmpty(ext)) {
      log.debug("Skipping file without extension: " + name);
      return false;
    }

    // Check the mime type
    final String mimeType = bundle.getMimeType();
    if (mimeType == null) {
      log.debug("Skipping file without mime-type: " + name);
      return false;
    }

    // Copy the file into the repository
    try {
      log.trace("copying file to repository: " + name);
      IRepositoryFileData data = converter.convert(bundle.getInputStream(), bundle.getCharset(), mimeType);
      if (null == file) {
        createFile(bundle, data);
      } else {
        repository.updateFile(file, data, bundle.getComment());
      }
      return true;
    } catch (IOException e) {
      log.warn(messages.getString("DefaultImportHandler.WARN_0003_IOEXCEPTION", name), e); // TODO make sure string exists
      return false;
    }
  }

  /**
   * Creates a new file in the repository
   *
   * @param bundle
   * @param data
   */
  protected RepositoryFile createFile(final RepositoryFileImportBundle bundle,
                                      final IRepositoryFileData data) {
    final RepositoryFile file = new RepositoryFile.Builder(bundle.getName()).hidden(bundle.isHidden()).title(RepositoryFile.ROOT_LOCALE, getTitle(bundle.getName())).versioned(true).build();
    final Serializable parentId = getParentId(bundle.getPath());
    final RepositoryFileAcl acl = bundle.getAcl();
    if (null == acl) {
      return repository.createFile(parentId, file, data, bundle.getComment());
    } else {
      return repository.createFile(parentId, file, data, acl, bundle.getComment());
    }
  }

  /**
   * truncate the extension from the file name for the extension
   * @param name
   * @return title
   */
  protected String getTitle(String name) {
    if(name != null && name.length() > 0) {
      return name.substring(0, name.lastIndexOf('.'));
    } else {
      return name;
    }
  }

  /**
   * Returns the Id of the parent folder of the file path provided
   *
   * @param repositoryPath
   * @return
   */
  protected Serializable getParentId(final String repositoryPath) {
    Assert.notNull(repositoryPath);
    Assert.notNull(parentIdCache);

    final String parentPath = RepositoryFilenameUtils.getFullPathNoEndSeparator(repositoryPath);
    Serializable parentFileId = parentIdCache.get(parentPath);
    if (parentFileId == null) {
      final RepositoryFile parentFile = repository.getFile(parentPath);
      Assert.notNull(parentFile);
      parentFileId = parentFile.getId();
      Assert.notNull(parentFileId);
      parentIdCache.put(parentPath, parentFileId);
    }
    return parentFileId;
  }

  public IUnifiedRepository getRepository() {
    return repository;
  }

  public void setRepository(IUnifiedRepository repository) {
    this.repository = repository;
  }

  @Override
  public void importFile(IPlatformImportBundle bundle, boolean overwriteInRepository) throws PlatformImportException,
      DomainIdNullException, DomainAlreadyExistsException, DomainStorageException, IOException {
    // TODO Auto-generated method stub
    
  }

}
