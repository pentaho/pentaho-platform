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
 * Copyright 2011 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.repository2.unified.importexport;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.messages.Messages;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class Description
 * User: dkincade
 */
public class DefaultImportContentHandler implements ImportContentHandler {
  private static final Log logger = LogFactory.getLog(DefaultImportContentHandler.class);
  private static final Messages messages = Messages.getInstance();
  private IUnifiedRepository repository;
  private String destFolderPath;
  private String versionMessage;
  private Map<String, Converter> converters;
  private Set<String> executableTypes;
  private IPluginManager pluginManager;
  private HashMap<String, Serializable> parentIdCache;

  public DefaultImportContentHandler() {
    try {
      pluginManager = PentahoSystem.get(IPluginManager.class, null);
    } catch (Exception e) {
      logger.debug("Executing outside the BIPLATFORM");
    }

    executableTypes = new HashSet<String>();
    if (pluginManager != null && pluginManager.getContentTypes() != null) {
      executableTypes.addAll(pluginManager.getContentTypes());
    }
    executableTypes.add("xaction"); // Add non-plugin types //$NON-NLS-1$
    executableTypes.add("url"); //$NON-NLS-1$
  }

  /**
   * Returns a simple name to describe this ImportContentHandler
   */
  @Override
  public String getName() {
    return "DefaultContentHandler";
  }

  /**
   * Performs any initialization required prior to handling any import processing
   *
   * @param repository            the {@link org.pentaho.platform.api.repository2.unified.IUnifiedRepository} into which content is being imported
   * @param converters
   * @param destinationFolderPath
   * @param versionMessage
   */
  @Override
  public void initialize(final IUnifiedRepository repository, final Map<String, Converter> converters, final String destinationFolderPath, final String versionMessage) throws InitializationException {
    if (null == repository) {
      throw new InitializationException();
    }
    this.repository = repository;

    if (null == converters) {
      throw new InitializationException();
    }
    this.converters = converters;

    // Make sure we're working with unix type paths
    this.destFolderPath = FilenameUtils.separatorsToUnix(destinationFolderPath);
    if (!this.destFolderPath.endsWith("/")) {
      this.destFolderPath += "/";
    }

    this.versionMessage = versionMessage;

    // Parent ID Cache ???
    parentIdCache = new HashMap<String, Serializable>();
  }

  /**
   * Will perform the default import operation by imports the content as a file into the same location in the repository
   *
   * @param bundle    the information being imported
   * @param overwrite indicates if this content handler should overwrite existing content with this new content
   * @return {@code true} if processing on this bundle should continue by other handlers, {@code false} otherwise.
   * @throws ImportException indicates an error trying to perform the import process on this content
   */
  @Override
  public Result performImport(final ImportSource.IRepositoryFileBundle bundle, final boolean overwrite) throws
      ImportException {
    // Compute the bundle path
    final String bundlePath = computeBundlePath(bundle);

    // Compute the destination location in the repository
    final String repoFilePath = destFolderPath + bundlePath + bundle.getFile().getName();

    // See if the destination already exists in the repository
    final RepositoryFile file = repository.getFile(repoFilePath);

    // Does the file already exists in the repository?
    if (file != null) {
      // If we can't overwrite - then skip
      if (!overwrite) {
        logger.debug("File already exists in repository and overwrite is false - skip");
        return Result.SKIPPED;
      }

      // If it is a folder that exists, skip
      if (file.isFolder()) {
        logger.debug("File is a folder that already exists - skip");
        return Result.SKIPPED;
      }

      // It is a file we can overwrite...
      return copyFileToRepository(bundle, bundlePath, file);
    }

    // The file doesn't exist - if it is a folder then this is easy
    if (bundle.getFile().isFolder()) {
      logger.debug("creating folder " + bundlePath);
      if (bundle.getAcl() != null) {
        repository.createFolder(getParentId(destFolderPath, bundlePath), bundle.getFile(), bundle.getAcl(),
            versionMessage);
      } else {
        repository.createFolder(getParentId(destFolderPath, bundlePath), bundle.getFile(), versionMessage);
      }

      // Handled
      return Result.SUCCESS;
    }

    // It is a file ...
    return copyFileToRepository(bundle, bundlePath, null);
  }

  protected Result copyFileToRepository(final ImportSource.IRepositoryFileBundle bundle,
                                        final String bundlePath,
                                        final RepositoryFile file) {
    final String bundlePathName = bundlePath + bundle.getFile().getName();
    logger.debug("copying file to repository: " + bundlePathName);

    // Compute the file extension
    final String ext = getExtension(bundle.getFile().getName());
    if (ext == null) {
      // ... a file without an extension ... skip
      logger.warn(messages.getString("DefaultImportContentHandler.WARN_0001_NO_EXT", bundlePathName)); //$NON-NLS-1$
      return Result.SKIPPED;
    }

    // Check the mime type
    final String mimeType = bundle.getMimeType();
    if (mimeType == null) {
      // No mime type
      logger.warn(messages.getString("DefaultImportContentHandler.WARN_0004_NO_MIME", bundlePathName)); //$NON-NLS-1$
      return Result.SKIPPED;
    }

    // Find the converter
    final Converter converter = converters.get(ext);
    if (converter == null) {
      // No converter for this file
      logger.warn(messages.getString("DefaultImportContentHandler.WARN_0002_NO_CONVERTER", bundlePathName)); //$NON-NLS-1$
      return Result.SKIPPED;
    }

    // Copy the file into the repository
    try {
      IRepositoryFileData data = converter.convert(bundle.getInputStream(), bundle.getCharset(), bundle.getMimeType());
      if (null == file) {
        return createFile(bundle, bundlePath, ext, data);
      } else {
        return updateFile(file, data);
      }
    } catch (IOException e) {
      logger.warn(messages.getString("DefaultImportContentHandler.WARN_0003_IOEXCEPTION",
          bundle.getFile().getName()), e); //$NON-NLS-1$
      return Result.SKIPPED;
    }
  }

  private Result updateFile(final RepositoryFile file, final IRepositoryFileData data) {
    repository.updateFile(file, data, versionMessage);
    return Result.SUCCESS;
  }

  protected Result createFile(final ImportSource.IRepositoryFileBundle bundle,
                              final String bundlePath, final String ext,
                              final IRepositoryFileData data) {
    RepositoryFile createdFile = null;
    if (bundle.getAcl() != null) {
      createdFile = repository.createFile(getParentId(destFolderPath, bundlePath), bundle.getFile(),
          data, bundle.getAcl(), versionMessage);
    } else {
      createdFile = repository.createFile(getParentId(destFolderPath, bundlePath), bundle.getFile(),
          data, versionMessage);
    }
    final boolean hidden = !executableTypes.contains(ext.toLowerCase());
    logger.debug("\tsetting hidden="+hidden+" for file with extension "+ext.toLowerCase());
    createdFile = new RepositoryFile.Builder(createdFile).hidden(hidden).build();
    return updateFile(createdFile, data);
  }

  protected String computeBundlePath(final ImportSource.IRepositoryFileBundle bundle) {
    String bundlePath = bundle.getPath();
    bundlePath = FilenameUtils.separatorsToUnix(bundlePath);
    if (bundlePath.startsWith("/")) {
      bundlePath = bundlePath.substring(1);
    }
    return bundlePath;
  }


  private String getExtension(final String name) {
    int lastDot = name.lastIndexOf('.');
    if (lastDot > -1) {
      return name.substring(lastDot + 1).toLowerCase();
    } else {
      return null;
    }
  }

  /**
   * Gets (possibly from cache) id of parent folder of file pointed to by childPath.
   */
  private Serializable getParentId(final String destFolderPath, final String childPath) {
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
      RepositoryFile parentFile = repository.getFile(destFolderPath + parentPath);
      Assert.notNull(parentFile);
      parentIdCache.put(normalizedChildPath, parentFile.getId());
    }
    return parentIdCache.get(normalizedChildPath);
  }
}
