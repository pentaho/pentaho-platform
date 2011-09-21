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
package org.pentaho.platform.repository2.unified.importexport;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFile.Builder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.importexport.ImportSource.IRepositoryFileBundle;
import org.springframework.util.Assert;

/**
 * The workhouse that completes the import with the help of several collaborating objects.
 * 
 * @author mlowery
 */
public class Importer {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(Importer.class);
  private Set<String> executableTypes;
  private IPluginManager pluginManager;
  
  // ~ Instance fields =================================================================================================

  private IUnifiedRepository unifiedRepository;

  /**
   * Converter map with keys being file extensions (without leading period).
   */
  private Map<String, Converter> converters;

  // ~ Constructors ====================================================================================================

  public Importer(final IUnifiedRepository unifiedRepository, final Map<String, Converter> converters) {
    super();
    Assert.notNull(unifiedRepository);
    Assert.notNull(converters);
    this.unifiedRepository = unifiedRepository;
    this.converters = converters;
    pluginManager = PentahoSystem.get(IPluginManager.class, null);
  }

  // ~ Methods =========================================================================================================
  public void doImport(final ImportSource importSource, String destFolderPath, final String versionMessage) throws IOException {
    doImport(importSource, destFolderPath, versionMessage, true);
  }
  
  public void doImport(final ImportSource importSource, String destFolderPath, final String versionMessage, final boolean overwrite) throws IOException {
    executableTypes = new HashSet<String>();
    if (pluginManager != null && pluginManager.getContentTypes() != null) {
      executableTypes.addAll(pluginManager.getContentTypes());
    }
    executableTypes.add("xaction"); // Add non-plugin types //$NON-NLS-1$
    executableTypes.add("url"); //$NON-NLS-1$

    destFolderPath = FilenameUtils.separatorsToUnix(destFolderPath);  // Make sure we're working with unix type paths
    if (!destFolderPath.endsWith("/")) {
      destFolderPath += "/";
    }
    long beginTimeMillis = System.currentTimeMillis();
    logger.debug("import begin");

    Map<String, Serializable> parentIdCache = new HashMap<String, Serializable>();

    logger.debug("all paths relative to " + destFolderPath);

    int totalFileCount = 0;

    int importedFileCount = 0;

    for (IRepositoryFileBundle bundle : importSource.getFiles()) {
      totalFileCount += 1;
      String bundlePath = bundle.getPath();
      bundlePath = FilenameUtils.separatorsToUnix(bundlePath);
      if (bundlePath.startsWith("/")) {
        bundlePath = bundlePath.substring(1);
      }
      String repoFilePath = destFolderPath + bundlePath + bundle.getFile().getName();
      RepositoryFile file = unifiedRepository.getFile(repoFilePath);
      String ext = getExtension(bundle.getFile().getName());
      if (file != null) { // We're updating not creating
        if (!overwrite) { // If we're not allowed to overwrite then just continue
          continue;
        }
        if (file.isFolder()) {  // The folder is already created so we just go to the next file
          continue;
        }
        IRepositoryFileData data = null;
        try {
          ext = getExtension(bundle.getFile().getName());
          if (ext == null) {
            logger.warn(Messages.getInstance().getString("Importer.WARN_0001_NO_EXT", bundlePath)); //$NON-NLS-1$
            continue;
          }
          Converter converter = converters.get(ext);
          data = converter.convert(bundle.getInputStream(), bundle.getCharset(), bundle.getMimeType());
        } catch (IOException e) {
          logger
              .warn(Messages.getInstance().getString("Importer.WARN_0003_IOEXCEPTION", bundle.getFile().getName()), e); //$NON-NLS-1$
          continue;
        }
        unifiedRepository.updateFile(file, data, versionMessage);
      } else if (bundle.getFile().isFolder()) { // file doesn't exist so lets create it.
        logger.debug("creating folder " + bundlePath);
        if (bundle.getAcl() != null) {
          unifiedRepository.createFolder(getParentId(destFolderPath, bundlePath, parentIdCache),
              bundle.getFile(), bundle.getAcl(), versionMessage);
        } else {
          unifiedRepository.createFolder(getParentId(destFolderPath, bundlePath, parentIdCache),
              bundle.getFile(), versionMessage);
        }
        importedFileCount += 1;
      } else {
        ext = getExtension(bundle.getFile().getName());
        if (ext == null) {
          logger.warn(Messages.getInstance().getString("Importer.WARN_0001_NO_EXT", bundlePath) + bundle.getFile().getName()); //$NON-NLS-1$
          continue;
        }
        Converter converter = converters.get(ext);
        if (converter == null) {
          logger.warn(Messages.getInstance().getString("Importer.WARN_0002_NO_CONVERTER", bundlePath + bundle.getFile().getName())); //$NON-NLS-1$
          continue;
        }
        if (bundle.getMimeType() == null) {
          logger.warn(Messages.getInstance().getString("Importer.WARN_0004_NO_MIME",bundlePath + bundle.getFile().getName())); //$NON-NLS-1$
          continue;
        }
        IRepositoryFileData data = null;
        try {
          data = converter.convert(bundle.getInputStream(), bundle.getCharset(), bundle.getMimeType());
        } catch (IOException e) {
          logger
              .warn(Messages.getInstance().getString("Importer.WARN_0003_IOEXCEPTION", bundle.getFile().getName()), e); //$NON-NLS-1$
          continue;
        }
        logger.debug("creating file " + bundlePath + bundle.getFile().getName());
        RepositoryFile createdFile = null;
        if (bundle.getAcl() != null) {
          createdFile = unifiedRepository.createFile(getParentId(destFolderPath, bundlePath, parentIdCache), bundle.getFile(),
              data, bundle.getAcl(), versionMessage);
        } else {
          createdFile = unifiedRepository.createFile(getParentId(destFolderPath, bundlePath, parentIdCache), bundle.getFile(),
              data, versionMessage);
        }
        try {
          data = converter.convert(bundle.getInputStream(), bundle.getCharset(), bundle.getMimeType());
        } catch (IOException e) {
          logger
              .warn(Messages.getInstance().getString("Importer.WARN_0003_IOEXCEPTION", bundle.getFile().getName()), e); //$NON-NLS-1$
          continue;
        }
        createdFile = new Builder(createdFile).hidden(!executableTypes.contains(ext.toLowerCase())).build();
        unifiedRepository.updateFile(createdFile, data, versionMessage);
        importedFileCount += 1;
      }
    }
    logger.debug("import end");
    logger
        .info(Messages
            .getInstance()
            .getString(
                "Importer.USER_0001_IMPORT_COUNT", importedFileCount, totalFileCount, System.currentTimeMillis() - beginTimeMillis)); //$NON-NLS-1$
  }

  /**
   * Gets (possibly from cache) id of parent folder of file pointed to by childPath. 
   */
  private Serializable getParentId(final String destFolderPath, final String childPath,
      Map<String, Serializable> parentIdCache) {
    Assert.notNull(destFolderPath);
    Assert.notNull(childPath);
    Assert.notNull(parentIdCache);
    String normalizedChildPath = childPath;
    if (!childPath.startsWith(RepositoryFile.SEPARATOR)) {
      normalizedChildPath = RepositoryFile.SEPARATOR + childPath;
    }
    if (!parentIdCache.containsKey(normalizedChildPath)) {
      // get path to parent from child path
      String parentPath;
      int lastSlash = normalizedChildPath.lastIndexOf('/');
      if (lastSlash > 0) {
        parentPath = normalizedChildPath.substring(0, lastSlash);
      } else {
        parentPath = ""; //$NON-NLS-1$
      }
      if (parentPath.startsWith("/")) {
        parentPath = parentPath.substring(1);
      }
      
      // get id of parent from parent path
      RepositoryFile parentFile = unifiedRepository.getFile(destFolderPath + parentPath);
      Assert.notNull(parentFile);
      parentIdCache.put(normalizedChildPath, parentFile.getId());
    }
    return parentIdCache.get(normalizedChildPath);
  }

  private String getExtension(final String name) {
    Assert.notNull(name);
    int lastDot = name.lastIndexOf('.');
    if (lastDot > -1) {
      return name.substring(lastDot + 1).toLowerCase();
    } else {
      return null;
    }
  }
}
