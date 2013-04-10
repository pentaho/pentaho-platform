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
 * Modified: Tyler Band - April 8, 2013 - remove hard coded file extensions and process index.locale files for locale information.
 */

package org.pentaho.platform.plugin.services.importer;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.drools.util.StringUtils;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class LocaleImportHandler extends RepositoryFileImportFileHandler implements IPlatformImportHandler {

  private static final String FILE_DESCRIPTION = "file.description"; //jcr meta entry

  private static final String FILE_TLTLE = "file.tltle";//jcr meta entry

  private static final String LOCALE_FOLDER = "index"; //special type for folders

  private String localizationFileExtension = ".locale"; //spring injected

  private List<String> artifacts; //spring injected file extensions

  private IUnifiedRepository unifiedRepository;

  public LocaleImportHandler(List<String> artifacts) {
    this.unifiedRepository = PentahoSystem.get(IUnifiedRepository.class);
    this.artifacts = artifacts;    
  }

  public void importFile(IPlatformImportBundle bundle) throws PlatformImportException {
    RepositoryFileImportBundle localeBundle = (RepositoryFileImportBundle) bundle;
    RepositoryFile localeParent = getLocaleParent(localeBundle);

    Properties localeProperties = buildLocaleProperties(localeBundle);

    if (localeParent != null && unifiedRepository != null) {
      getLogger().trace("Processing Locale [" + bundle.getName() + "]");
      unifiedRepository.setLocalePropertiesForFile(localeParent, extractLocaleCode(localeBundle), localeProperties);
    }
  }

  private Properties buildLocaleProperties(RepositoryFileImportBundle locale) {
    Properties localeProperties = new Properties();
    try {
      localeProperties.load(locale.getInputStream());
    } catch (IOException ex) {
      getLogger().error(ex.getMessage());
    }
    String comment = locale.getComment();
    String fileTitle = locale.getName();
    if (!StringUtils.isEmpty((String) localeProperties.get("description"))) {
      comment = (String) localeProperties.get("description");
      localeProperties.remove("description");
    }
    if (!StringUtils.isEmpty(localeProperties.getProperty("name"))) {
      fileTitle = (String) localeProperties.getProperty("name");
      localeProperties.remove("name");
    }
    localeProperties.setProperty(FILE_DESCRIPTION, comment != null ? comment : "");
    localeProperties.setProperty(FILE_TLTLE, fileTitle != null ? fileTitle : "");

    return localeProperties;
  }

  /**
   * returns default of the name of the locale e.g. JA, FR, EN, ... or DEFAULT for root
   * @param localeBundle
   * @return
   */
  private String extractLocaleCode(RepositoryFileImportBundle localeBundle) {
    String localeCode = "default";
    String localeFileName = localeBundle.getName();
    if (localeBundle.getFile() != null) {
      localeFileName = localeBundle.getFile().getName();
      
    }
    for (Locale locale : Locale.getAvailableLocales()) {
      if (localeFileName.endsWith("_" + locale + localizationFileExtension)) {
        localeCode = locale.toString();
        break;
      }
    }
    return localeCode;
  }

  private RepositoryFile getLocaleParent(RepositoryFileImportBundle locale) {
    if (unifiedRepository == null) {
      return null;
    }

    RepositoryFile localeParent = null;
    String localeFileName = locale.getName();
    if (locale.getFile() != null) {
      localeFileName = locale.getFile().getName();
    }
    RepositoryFile localeFolder = unifiedRepository.getFile(locale.getPath());

    if (isLocaleFolder(localeFileName)) {
      localeParent = localeFolder;
    } else {
      List<RepositoryFile> localeFolderChildren = unifiedRepository.getChildren(localeFolder.getId());
      for (RepositoryFile localeChild : localeFolderChildren) {

        String localeChildName = extractFileName(localeChild.getName());
        String localeChildExtension = extractExtension(localeChild.getName());

        if (localeFileName.startsWith(localeChildName) && artifacts.contains(localeChildExtension)) {
          localeParent = localeChild;
          break;
        }
      }
    }
    return localeParent;
  }

  private boolean isLocaleFolder(String localeFileName) {
    return localeFileName.startsWith(LOCALE_FOLDER) && localeFileName.endsWith(localizationFileExtension);
  }

  private String extractExtension(String name) {
    int idx = name.lastIndexOf(".");
    if (idx == -1 || idx == name.length()) {
      return name;
    }
    return name.substring(idx + 1);
  }

  private String extractFileName(String name) {
    int idx = name.lastIndexOf(".");
    if (idx == -1 || idx == name.length()) {
      return name;
    }
    return name.substring(0, idx);
  }

  public String getLocalizationFileExtension() {
    return localizationFileExtension;
  }

  public void setLocalizationFileExtension(String localizationFileExtension) {
    this.localizationFileExtension = localizationFileExtension;
  }
}
