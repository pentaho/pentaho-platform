package org.pentaho.platform.plugin.services.importer;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.plugin.services.importexport.ImportSource;
import org.pentaho.platform.plugin.services.importexport.StreamConverter;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository.messages.Messages;
import org.springframework.util.Assert;

/**
 * User: nbaker
 * Date: 5/29/12
 */
public class RepositoryFileImportFileHandler implements IPlatformImportHandler {

  private IUnifiedRepository repository;

  private static final Log log = LogFactory.getLog(RepositoryFileImportFileHandler.class);
  private static final Messages messages = Messages.getInstance();
  private static StreamConverter converter = new StreamConverter();
  
  protected String computeBundlePath(final ImportSource.IRepositoryFileBundle bundle) {
	    String bundlePath = bundle.getPath();
	    bundlePath = RepositoryFilenameUtils.separatorsToRepository(bundlePath);
	    if (bundlePath.startsWith(RepositoryFile.SEPARATOR)) {
	      bundlePath = bundlePath.substring(1);
	    }
	    return bundlePath;
  }
  
  protected static boolean isSystemPath(final String bundlePath) {
	    final String[] split = StringUtils.split(bundlePath, RepositoryFile.SEPARATOR);
	    return isSystemDir(split, 0) || isSystemDir(split, 1);
  }
  
  protected static boolean isSystemDir(final String[] split, final int index) {
	    return (split != null && index < split.length &&
	        (StringUtils.equals(split[index], "system") || StringUtils.equals(split[index], "admin")));
  }
  
  public void importFile(IPlatformImportBundle bnd) throws PlatformImportException {

    if(bnd instanceof RepositoryFileImportBundle == false){
      throw new PlatformImportException("Error importing bundle. RepositoryFileImportBundle expected");
    }
    RepositoryFileImportBundle bundle = (RepositoryFileImportBundle) bnd;
    String repositoryFilePath = RepositoryFilenameUtils.concat(computeBundlePath(bundle), bundle.getName());
    
    // Validate against importing system related artifacts.
    if (isSystemPath(repositoryFilePath)) {
        log.trace("Skipping [" + repositoryFilePath + "], it is in admin / system folders");
        return;
    }
    
    repositoryFilePath = RepositoryFilenameUtils.concat(bnd.getUploadDir(), repositoryFilePath);
    log.trace("Processing [" + repositoryFilePath + "]");

    // Verify if destination already exists in the repository.
    final RepositoryFile file = repository.getFile(repositoryFilePath);
    if (file != null) {
    	// If file exists, overwrite is true and is not a folder then update it.
    	if(bundle.overwriteInRepossitory() && !file.isFolder()) {
    		copyFileToRepository(bundle, repositoryFilePath, file);
    	}
    } else {
        if (bundle.isFolder()) {
          // The file doesn't exist and it is a folder. Create folder.
          log.trace("Creating folder [" + repositoryFilePath + "]");
          final Serializable parentId = getParentId(repositoryFilePath);
          if (bundle.getAcl() != null) {
            repository.createFolder(parentId, bundle.getFile(), bundle.getAcl(), null);
          } else {
            repository.createFolder(parentId, bundle.getFile(), null);
          }
        } else {
          // The file doesn't exist. Create file.
          log.trace("Creating file [" + repositoryFilePath + "]");
          copyFileToRepository(bundle, repositoryFilePath, null);
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
        createFile(bundle, repositoryPath, data);
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
		  							  final String repositoryPath,
                                      final IRepositoryFileData data) {
    final RepositoryFile file = new RepositoryFile.Builder(bundle.getName()).hidden(bundle.isHidden()).title(RepositoryFile.ROOT_LOCALE, getTitle(bundle.getName())).versioned(true).build();
    final Serializable parentId = getParentId(repositoryPath);
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
    final String parentPath = RepositoryFilenameUtils.getFullPathNoEndSeparator(repositoryPath);
    final RepositoryFile parentFile = repository.getFile(parentPath);
    Assert.notNull(parentFile);
    Serializable parentFileId = parentFile.getId();
    Assert.notNull(parentFileId);
    return parentFileId;
  }

  public IUnifiedRepository getRepository() {
    return repository;
  }

  public void setRepository(IUnifiedRepository repository) {
    this.repository = repository;
  }

 

}
