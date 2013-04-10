/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.plugin.services.importer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifest;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifestEntity;

public class SolutionImportHandler implements IPlatformImportHandler {

  private static final Log log = LogFactory.getLog(SolutionImportHandler.class);

  private IPlatformImportMimeResolver mimeResolver;

  private List<String> hideOnImport;

  private List<String> whiteList;

  private List<IContentInfo> contentInfoList;

  private ExportManifest manifest;


  public SolutionImportHandler(IPlatformImportMimeResolver mimeResolver, List<IContentInfo> aContentInfoList) {
    this.mimeResolver = mimeResolver;
    this.contentInfoList = aContentInfoList;
  }

  public void importFile(IPlatformImportBundle bundle) throws PlatformImportException, DomainIdNullException,
      DomainAlreadyExistsException, DomainStorageException, IOException {

    RepositoryFileImportBundle importBundle = (RepositoryFileImportBundle) bundle;
    ZipInputStream zipImportStream = new ZipInputStream(bundle.getInputStream());
    SolutionRepositoryImportSource importSource = new SolutionRepositoryImportSource(zipImportStream);
    LocaleFilesProcessor localeFilesProcessor = new LocaleFilesProcessor();

    IPlatformImporter importer = PentahoSystem.get(IPlatformImporter.class);
    for (IRepositoryFileBundle file : importSource.getFiles()) {
      String fileName = file.getFile().getName();
      RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
      String repositoryFilePath = RepositoryFilenameUtils.concat(
          PentahoPlatformImporter.computeBundlePath(file.getPath()), fileName);

      // Validate against importing system related artifacts.
      if (isSystemPath(repositoryFilePath)) {
        log.trace("Skipping [" + repositoryFilePath + "], it is in admin / system folders");
        continue;
      }

      InputStream bundleInputStream = null;
      if (file.getFile().isFolder()) {
        bundleBuilder.mime("text/directory");
        bundleBuilder.file(file.getFile());
        fileName = repositoryFilePath;
        repositoryFilePath = importBundle.getPath();
      } else {
        byte[] bytes = IOUtils.toByteArray(file.getInputStream());
        bundleInputStream = new ByteArrayInputStream(bytes);
        // If is locale file store it for later processing.
        if (localeFilesProcessor.isLocaleFile(file, importBundle.getPath(), bytes)) {
          log.trace("Skipping [" + repositoryFilePath + "], it is a locale property file");
          continue;
        }
        bundleBuilder.input(bundleInputStream);
        bundleBuilder.mime(mimeResolver.resolveMimeForFileName(fileName));
        String filePath = (file.getPath().equals("/") || file.getPath().equals("\\")) ? "" : file.getPath();
        repositoryFilePath = RepositoryFilenameUtils.concat(importBundle.getPath(), filePath);
      }

      bundleBuilder.name(fileName);
      bundleBuilder.path(repositoryFilePath);

      String sourcePath;
      if (file.getPath().startsWith("/")) {
        sourcePath = RepositoryFilenameUtils.concat(file.getPath().substring(1), fileName);
      } else {
        if (file.getFile().isFolder()) {
          sourcePath = fileName;
        } else {
          sourcePath = RepositoryFilenameUtils.concat(file.getPath(), fileName);
        }
      }

      bundleBuilder.charSet(bundle.getCharset());
      bundleBuilder.overwriteFile(bundle.overwriteInRepository());
      bundleBuilder.hidden(isFileHidden(bundle, sourcePath));
      bundleBuilder.applyAclSettings(bundle.isApplyAclSettings());
      bundleBuilder.retainOwnership(bundle.isRetainOwnership());
      bundleBuilder.overwriteAclSettings(bundle.isOverwriteAclSettings());
      bundleBuilder.acl(processAclForFile(bundle, sourcePath));
      IPlatformImportBundle platformImportBundle = bundleBuilder.build();
      importer.importFile(platformImportBundle);

      if (bundleInputStream != null) {
        bundleInputStream.close();
        bundleInputStream = null;
      }
    }
    // Process locale files.
    initContentInfo();
    localeFilesProcessor.processLocaleFiles(importer);
  }

  private void initContentInfo() {
    if (whiteList == null) {
      whiteList = new ArrayList();
    }
    for (IContentInfo type : contentInfoList) {
      if (!whiteList.contains(type.getExtension())) {
        whiteList.add(type.getExtension());
      }
    }
  }

  private RepositoryFileAcl processAclForFile(IPlatformImportBundle bundle, String filePath) {
    // If we are not overwriting ACL's or owners then make sure a null gets in the bundle.
    // If we are writing ACL's we'll have to check later in RepositoryFileImportHandler whether to overwrite
    // based on the isOverwriteAcl setting and whether we are creating or updating the RepositoryFile.
    RepositoryFileAcl acl = null;
    if (bundle.isApplyAclSettings() || !bundle.isRetainOwnership()) {
      try {
        if (manifest != null) {
          ExportManifestEntity entity = manifest.getExportManifestEntity(filePath);
          if (entity != null) {
            acl = entity.getRepositoryFileAcl();
          }
        }
      } catch (Exception e) {
        log.trace(e);
      }
    }
    return acl;
  }

  /**
   * Determines if the file or folder should be hidden.  If there is a manifest entry for
   * the file, and we are not ignoring the manifest, then set the hidden flag based on the
   * manifest.  Otherwise use the blacklist to determine if it is hidden.
   * 
   * @param bundle
   * @param filePath
   * @return true if file/folder should be hidden, false otherwise
   */
  private boolean isFileHidden(IPlatformImportBundle bundle, String filePath) {
    if ((bundle.isApplyAclSettings() || !bundle.isRetainOwnership()) && manifest != null
        && manifest.getExportManifestEntity(filePath) != null) {
      return manifest.getExportManifestEntity(filePath).getRepositoryFile().isHidden();
    }
    return hideOnImportExtensions(bundle.getName());
  }

  private boolean isSystemPath(final String bundlePath) {
    final String[] split = StringUtils.split(bundlePath, RepositoryFile.SEPARATOR);
    return isSystemDir(split, 0) || isSystemDir(split, 1);
  }

  private boolean isSystemDir(final String[] split, final int index) {
    return (split != null && index < split.length && (StringUtils.equals(split[index], "system") || StringUtils.equals(
        split[index], "admin")));
  }

  /**
   * hide file on import
   * @param fileName
   * @return
   */
  private boolean hideOnImportExtensions(String fileName) {
    boolean hideExtension = false;
    for (String extension : hideOnImport) {
      if (fileName.endsWith(extension)) {
        hideExtension = true;
        break;
      }
    }
    return hideExtension;
  }

  public void setHideOnImport(List hideonimport) {
    this.hideOnImport = hideonimport;
  }

  public void setWhiteList(List whiteList) {
    this.whiteList = whiteList;
  }

  class SolutionRepositoryImportSource {
    private static final String EXPORT_MANIFEST_XML = "exportManifest.xml";

    private ZipInputStream zipInputStream;

    private List<IRepositoryFileBundle> files;

    public SolutionRepositoryImportSource(final ZipInputStream zipInputStream) {
      this.zipInputStream = zipInputStream;
      this.files = new ArrayList<IRepositoryFileBundle>();
      initialize();
    }

    protected void initialize() {
      try {
        ZipEntry entry = zipInputStream.getNextEntry();
        while (entry != null) {
          final String entryName = RepositoryFilenameUtils.separatorsToRepository(entry.getName());
          File tempFile = null;
          boolean isDir = entry.isDirectory();
          if (!isDir) {
            if (!canExtensionBeImported(entryName)) {
              zipInputStream.closeEntry();
              entry = zipInputStream.getNextEntry();
              continue;
            }
            tempFile = File.createTempFile("zip", null);
            tempFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempFile);
            IOUtils.copy(zipInputStream, fos);
            fos.close();
          }
          File file = new File(entryName);
          RepositoryFile repoFile = new RepositoryFile.Builder(file.getName()).folder(isDir).hidden(false).build();
          String parentDir = new File(entryName).getParent() == null ? RepositoryFile.SEPARATOR : new File(entryName)
              .getParent() + RepositoryFile.SEPARATOR;
          IRepositoryFileBundle repoFileBundle = new RepositoryFileBundle(repoFile, null, parentDir, tempFile, "UTF-8",
              null);

          if (file.getName().equals(EXPORT_MANIFEST_XML)) {
            initializeAclManifest(repoFileBundle);
          } else {
            files.add(repoFileBundle);
          }
          zipInputStream.closeEntry();
          entry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
      } catch (IOException exception) {
        final String errorMessage = Messages.getInstance().getErrorString("", exception.getLocalizedMessage());
        log.trace(errorMessage);
      }
    }

    private void initializeAclManifest(IRepositoryFileBundle file) {
      try {
        byte[] bytes = IOUtils.toByteArray(file.getInputStream());
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        manifest = ExportManifest.fromXml(in);
      } catch (Exception e) {
        log.trace(e);
      }
    }

    private boolean canExtensionBeImported(String fileName) {
      boolean isWhiteListed = false;
      for (String extension : whiteList) {
        if (fileName.endsWith(extension)) {
          isWhiteListed = true;
          break;
        }
      }
      return isWhiteListed;
    }

    public List<IRepositoryFileBundle> getFiles() {
      return this.files;
    }
  }

  public List<String> getHideOnImport() {
    return hideOnImport;
  }

  public List<String> getWhiteList() {
    return whiteList;
  }

}
