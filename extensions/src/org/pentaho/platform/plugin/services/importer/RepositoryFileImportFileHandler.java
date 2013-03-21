package org.pentaho.platform.plugin.services.importer;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IRepositoryDefaultAclHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.Converter;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository.messages.Messages;
import org.springframework.util.Assert;

/**
 * User: nbaker
 * Date: 5/29/12
 */
public class RepositoryFileImportFileHandler implements IPlatformImportHandler {

  private IUnifiedRepository repository;
  private ThreadLocal<Log> log = new ThreadLocal<Log>();
  //private static final Log log = LogFactory.getLog(RepositoryFileImportFileHandler.class);
 
  private static final Messages messages = Messages.getInstance();

  private Map<String, Converter> converters;
  IRepositoryDefaultAclHandler defaultAclHandler;
  
  public Log getLogger() {
    if (log.get() == null) {
        log.set((PentahoSystem.get(IPlatformImporter.class).getRepositoryImportLogger() != null &&
            PentahoSystem.get(IPlatformImporter.class).getRepositoryImportLogger().hasLogger())
            ? PentahoSystem.get(IPlatformImporter.class).getRepositoryImportLogger()
                : LogFactory.getLog(RepositoryFileImportFileHandler.class));
    }
    return log.get();
  }

  public void importFile(IPlatformImportBundle bnd) throws PlatformImportException {
    if (bnd instanceof RepositoryFileImportBundle == false) {
      throw new PlatformImportException("Error importing bundle. RepositoryFileImportBundle expected");
    }
    RepositoryFileImportBundle bundle = (RepositoryFileImportBundle) bnd;
    String repositoryFilePath = RepositoryFilenameUtils.concat(bundle.getPath(), bundle.getName());
    getLogger().trace("Processing [" + repositoryFilePath + "]");

    // Verify if destination already exists in the repository.
    final RepositoryFile file = repository.getFile(repositoryFilePath);
    if (file != null) {
      if (bundle.overwriteInRepossitory()) {
        // If file exists, overwrite is true and is not a folder then update it.
        if (!file.isFolder()) {
          copyFileToRepository(bundle, repositoryFilePath, file);
        } else {
          // The folder exists. Possible ACL changes.
          getLogger().trace("Existing folder [" + repositoryFilePath + "]");
          if (bundle.getAcl() != null) {
            updateAclFromBundle(false, bundle, file);
          }
        }
      } else {
        getLogger().trace("Not importing existing file [" + repositoryFilePath + "]");
      }
    } else {
      if (bundle.isFolder()) {
        // The file doesn't exist and it is a folder. Create folder.
        getLogger().trace("Creating folder [" + repositoryFilePath + "]");
        final Serializable parentId = getParentId(repositoryFilePath);
        if (bundle.getAcl() != null) {
          RepositoryFile repositoryFile = repository.createFolder(parentId, bundle.getFile(), bundle.getAcl(), null);
          updateAclFromBundle(true, bundle, repositoryFile);
        } else {
          repository.createFolder(parentId, bundle.getFile(), null);
        }
      } else {
        // The file doesn't exist. Create file.
        getLogger().trace("Creating file [" + repositoryFilePath + "]");
        copyFileToRepository(bundle, repositoryFilePath, null);
      }
    }
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
    Log log = getLogger();
    // Compute the file extension
    final String name = bundle.getName();
    final String ext = RepositoryFilenameUtils.getExtension(name);
    if (StringUtils.isEmpty(ext)) {
      getLogger().debug("Skipping file without extension: " + name);
      return false;
    }

    // Check the mime type
    final String mimeType = bundle.getMimeType();
    if (mimeType == null) {
      getLogger().debug("Skipping file without mime-type: " + name);
      return false;
    }

    // Copy the file into the repository
    try {
      getLogger().trace("copying file to repository: " + name);

      Converter converter = converters.get(ext);
      if (converter == null) {
        getLogger().debug("Skipping file without converter: " + name);
        return false;
      }

      IRepositoryFileData data = converter.convert(bundle.getInputStream(), bundle.getCharset(), mimeType);
      if (null == file) {
        RepositoryFile repositoryFile = createFile(bundle, repositoryPath, data);
          updateAclFromBundle(true, bundle, repositoryFile);
      } else {
        RepositoryFile repositoryFile = repository.updateFile(file, data, bundle.getComment());
          updateAclFromBundle(false, bundle, repositoryFile);
      }

      return true;
    } catch (IOException e) {
      getLogger().warn(messages.getString("DefaultImportHandler.WARN_0003_IOEXCEPTION", name), e); // TODO make sure string exists
      return false;
    }
  }

  //Create a formal RepositoryFileAcl object from the one in the manifest.
  private void updateAclFromBundle(boolean newFile, RepositoryFileImportBundle bundle, RepositoryFile repositoryFile) {
    getLogger().debug("File " + (newFile ? "is new": "already exists"));
    if (bundle.getAcl() != null && (bundle.isApplyAclSettings() || !bundle.isRetainOwnership())) {
      RepositoryFileAcl manifestAcl = bundle.getAcl();
      RepositoryFileAcl originalAcl = repository.getAcl(repositoryFile.getId());

      //Determine who will own this file
      RepositoryFileSid newOwner;
      if (bundle.isRetainOwnership()) {
        if (newFile) {
          getLogger().debug("Getting Owner from Session");
          newOwner = new RepositoryFileSid(PentahoSessionHolder.getSession().getName(), RepositoryFileSid.Type.USER);
        } else {
          getLogger().debug("Getting Owner from existing file");
          newOwner = originalAcl.getOwner();
        }
      } else {
        getLogger().debug("Getting Owner from Manifest");
        newOwner = manifestAcl.getOwner();
      }
      
      //Determine the Aces we will use for this file
      RepositoryFileAcl useAclForPermissions; //The ACL we will use the permissions from
      if (bundle.isApplyAclSettings() && (bundle.isOverwriteAclSettings() || newFile)){
        getLogger().debug("Getting permissions from Manifest");
        useAclForPermissions = manifestAcl;
      } else {
        if (newFile) {
          getLogger().debug("Getting permissions from Default settings");
          useAclForPermissions = getDefaultAcl(repositoryFile);
        } else {
          getLogger().debug("Getting permissions from existing file");
          useAclForPermissions = originalAcl;
        }
      }

      //Make the new Acl if it has changed from the orignal
      if (!newOwner.equals(originalAcl.getOwner()) || !useAclForPermissions.equals(originalAcl)) {
        RepositoryFileAcl updatedAcl = new RepositoryFileAcl(repositoryFile.getId(), newOwner,
            useAclForPermissions.isEntriesInheriting(), useAclForPermissions.getAces());
        repository.updateAcl(updatedAcl);
      }
    }
  }
  
  private RepositoryFileAcl getDefaultAcl(RepositoryFile repositoryFile) {
    // ToDo: call default Acl creator when implemented.  For now just return
    // whatever is stored
    //return repository.getAcl(repositoryFile.getId());
    return defaultAclHandler.createDefaultAcl(repositoryFile.clone());
  }

  /**
   * Creates a new file in the repository
   *
   * @param bundle
   * @param data
   */
  protected RepositoryFile createFile(final RepositoryFileImportBundle bundle, final String repositoryPath,
      final IRepositoryFileData data) {
    final RepositoryFile file = new RepositoryFile.Builder(bundle.getName()).hidden(bundle.isHidden())
        .title(RepositoryFile.DEFAULT_LOCALE, getTitle(bundle.getName())).versioned(true).build();
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
    if (name != null && name.length() > 0) {
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

  public void setConverters(Map<String, Converter> converters) {
    this.converters = converters;
  }
  
  public void setDefaultAclHandler(IRepositoryDefaultAclHandler defaultAclHandler) {
    this.defaultAclHandler = defaultAclHandler;
  }
}
