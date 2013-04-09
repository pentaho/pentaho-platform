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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
/**
 * this class is used to handle .properties files that are XACTION or URL files that contain the metadata
 * used for localization.  These files may contain additional information that will allow the properties file
 * to be stored and used by XACTION and URL as well as localize the title and description.
 * @author tband /ezequiel
 *
 */
public class LocaleFilesProcessor {

  private static final String FILE_LOCALE_RESOLVER = "file.locale";
  private static final String URL_DESCRIPTION = "url_description";
  private static final String URL_NAME = "url_name";
  private static final String DESCRIPTION = "description";
  private static final String TITLE = "title";
  private static final String NAME = "name";
  private static final String PROPERTIES_EXT = ".properties";
  private static final String LOCALE_EXT = ".locale";
  private List<LocaleFileDescriptor> localeFiles;

  public LocaleFilesProcessor() {
    localeFiles = new ArrayList<LocaleFileDescriptor>();
  }

  /**
   * 
   * @param file
   * @param parentPath
   * @param bytes
   * @return false - means discard the file extension type
   * @throws IOException
   */
  public boolean isLocaleFile(IRepositoryFileBundle file, String parentPath, byte[] bytes) throws IOException {

    boolean isLocale = false;
    String fileName = file.getFile().getName();
    if (fileName.endsWith(PROPERTIES_EXT)) {
      InputStream inputStream = new ByteArrayInputStream(bytes);
      Properties properties = loadProperties(inputStream);

      String name = properties.getProperty(NAME);
      String title = properties.getProperty(TITLE);
      String description = properties.getProperty(DESCRIPTION);
      String url_name = properties.getProperty(URL_NAME);
      String url_description = properties.getProperty(URL_DESCRIPTION);

      if (!StringUtils.isEmpty(url_name)) {
        name = url_name;
      }
      if (!StringUtils.isEmpty(title)) {
        name = title;
      }

      description = !StringUtils.isEmpty(description) ? description : "";
      if (!StringUtils.isEmpty(url_description)) {
        description = url_description;
      }

      if (!StringUtils.isEmpty(name)) {
        String filePath = (file.getPath().equals("/") || file.getPath().equals("\\")) ? "" : file.getPath();
        filePath = RepositoryFilenameUtils.concat(parentPath, filePath);

        LocaleFileDescriptor localeFile = new LocaleFileDescriptor(name, description, filePath, file.getFile(),
            inputStream);
        localeFiles.add(localeFile);

        /**
         * assumes that the properties file has additional localization attributes and should be imported
         */
        if (properties.size() <= 2) {
          isLocale = true;
        }
      }
    }
    return isLocale;
  }

  public Properties loadProperties(InputStream inputStream) throws IOException {
    Properties properties = new Properties();
    properties.load(inputStream);
    return properties;
  }

  public boolean createLocaleEntry(String filePath, String name, String title, String description, RepositoryFile file, InputStream is)
      throws IOException {

    boolean success = false;   
    //need to spoof the locales to think this is the actual parent .prpt and not the meta.xml
    RepositoryFile.Builder rf = new RepositoryFile.Builder(name);
    rf.path(filePath);
    if (!StringUtils.isEmpty(title)) {
      name = title;
    }
    
    if (!StringUtils.isEmpty(name)) {

      LocaleFileDescriptor localeFile = new LocaleFileDescriptor(name, description, filePath, rf.build() , is);
      localeFiles.add(localeFile);

      success = true;

    }
    return success;
  }

  public void processLocaleFiles(IPlatformImporter importer) throws PlatformImportException {
    RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
    NameBaseMimeResolver mimeResolver = PentahoSystem.get(NameBaseMimeResolver.class);
    String mimeType = mimeResolver.resolveMimeForFileName(FILE_LOCALE_RESOLVER);

    for (LocaleFileDescriptor localeFile : localeFiles) {
      bundleBuilder.name(localeFile.getName());
      bundleBuilder.comment(localeFile.getDescription());
      bundleBuilder.path(localeFile.getPath());
      bundleBuilder.file(localeFile.getFile());
      bundleBuilder.input(localeFile.getInputStream());
      bundleBuilder.mime(mimeType);
      IPlatformImportBundle platformImportBundle = bundleBuilder.build();
      importer.importFile(platformImportBundle);
    }
  }
}
