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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import org.drools.util.StringUtils;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.repository.RepositoryFilenameUtils;

public class LocaleImportHandler extends RepositoryFileImportFileHandler implements IPlatformImportHandler {

  private static final String FILE_DESCRIPTION = "file.description";

  private static final String FILE_TITLE = "file.title";

  private static final String DIRECTORY_NAME = "directory_name";
  private static final String DIRECTORY_DESCRIPTION = "directory_description";

  private static final String LOCALE_FOLDER = "index";

  private static final String LOCALE_EXT = ".locale";
  private static final String OLD_LOCALE_EXT = ".properties";

  private List<String> artifacts; //spring injected file extensions

  private IUnifiedRepository unifiedRepository;

  public LocaleImportHandler(List<String> artifacts, List<String> approvedExtensionList, List<String> hiddenExtensionList) {
	super(approvedExtensionList, hiddenExtensionList);
    this.unifiedRepository = PentahoSystem.get(IUnifiedRepository.class);
    this.artifacts = artifacts;
  }

  public void importFile(IPlatformImportBundle bundle) throws PlatformImportException {
    RepositoryFileImportBundle localeBundle = (RepositoryFileImportBundle) bundle;
    RepositoryFile localeParent = getLocaleParent(localeBundle);

    Properties localeProperties = buildLocaleProperties(localeBundle);
  
    if (localeParent != null && unifiedRepository != null && localeBundle.getFile() != null) {
      //If the parent file (content) got skipped because it existed then we will not import the locale information
      String fullPath = RepositoryFilenameUtils.concat(localeBundle.getPath(), localeBundle.getFile().getName());
      if (ImportSession.getSession().getSkippedFiles().contains(fullPath)) {
        getLogger().trace("Not importing Locale [" + localeBundle.getFile().getName() + "] since parent file not written ");
      } else {
        getLogger().trace("Processing Locale [" + localeBundle.getFile().getName() + "]");
        unifiedRepository.setLocalePropertiesForFile(localeParent, extractLocaleCode(localeBundle), localeProperties);
      }
    }
  }

  /**
   * return locale specific properties from resource bundle
   * @param locale
   * @return
   */
  private Properties buildLocaleProperties(RepositoryFileImportBundle locale) {
    Properties localeProperties = new Properties();
    PropertyResourceBundle rb = null;  
    String comment = locale.getComment();
    String fileTitle = locale.getName();

    try {      
      rb = new PropertyResourceBundle(locale.getInputStream());
     } catch (IOException returnEmptyIfError) {      
       return localeProperties;
    }

    if(rb != null){
      //this is the 4.8 style - name and description
      // First try file desc. If no file desc, try for a directory desc, else try fallback
      comment = rb.containsKey("description") ? rb.getString("description"):
          rb.containsKey(FILE_DESCRIPTION)? rb.getString(FILE_DESCRIPTION):
              rb.containsKey(DIRECTORY_DESCRIPTION) ? rb.getString(DIRECTORY_DESCRIPTION):
                  comment;

      // First try name. If no name, try title. If no title, try for a directory name, else use filename.
      fileTitle = rb.containsKey("name") ? rb.getString("name"):
          rb.containsKey("title") ? rb.getString("title"):
              rb.containsKey(FILE_TITLE) ? rb.getString(FILE_TITLE) :
                  rb.containsKey(DIRECTORY_NAME) ? rb.getString(DIRECTORY_NAME) :
                      fileTitle;

    }

    //this is the new .locale Jcr property names 
    localeProperties.setProperty(FILE_DESCRIPTION, comment != null ? comment : "");
    localeProperties.setProperty(FILE_TITLE, fileTitle != null ? fileTitle : "");

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
    if(localeBundle.getFile() != null){
      localeFileName = localeBundle.getFile().getName();;
    }
    for (Locale locale : Locale.getAvailableLocales()) {
      if (localeFileName.endsWith("_" + locale + LOCALE_EXT) || localeFileName.endsWith("_" + locale + OLD_LOCALE_EXT)) {
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
    return (localeFileName.startsWith(LOCALE_FOLDER) && localeFileName.endsWith(LOCALE_EXT))
        || (localeFileName.startsWith(LOCALE_FOLDER) && localeFileName.endsWith(OLD_LOCALE_EXT));
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
}
