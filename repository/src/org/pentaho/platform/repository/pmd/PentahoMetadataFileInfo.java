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
package org.pentaho.platform.repository.pmd;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class PentahoMetadataFileInfo {
  public enum FileType {XMI, PROPERTIES, UNKNOWN}

  private FileType fileType;
  private String path;
  private String filename;
  private String basename;
  private String extension;
  private String locale;
  private String domainId;

  public String getPath() {
    return path;
  }

  public FileType getFileType() {
    return fileType;
  }

  public String getFilename() {
    return filename;
  }

  public String getBasename() {
    return basename;
  }

  public String getExtension() {
    return extension;
  }

  public String getLocale() {
    return locale;
  }

  public String getDomainId() {
    return domainId;
  }

  public PentahoMetadataFileInfo(final String path) {
    this.path = path;
    this.basename = FilenameUtils.getBaseName(path);
    this.extension = FilenameUtils.getExtension(path);
    this.fileType = (StringUtils.equals(extension, "xmi") ? FileType.XMI
        : (StringUtils.equals(extension, "properties") ? FileType.PROPERTIES : FileType.UNKNOWN));

    this.locale = computeLocale();
    this.filename = (locale == null ? basename : basename.substring(0, basename.length() - locale.length() - 1));
    this.domainId = computeDomainId(path);
  }

  private String computeDomainId(final String path) {
    if (fileType == FileType.XMI || fileType == FileType.PROPERTIES) {
      if (StringUtils.equals(filename, "metadata")) {
        return FilenameUtils.getBaseName(FilenameUtils.getFullPathNoEndSeparator(path));
      }
      return filename;
    }
    return null;
  }

  protected String computeLocale() {
    String locale = null;
    if (!StringUtils.isEmpty(basename) && fileType == FileType.PROPERTIES) {
      final String[] parts = basename.split("_");
      for (int index = 1; index < parts.length; ++index) {
        if (parts[index].length() == 2
            && Character.isLowerCase(parts[index].charAt(0))
            && Character.isLowerCase(parts[index].charAt(1))) {
          if (index == parts.length - 1) {
            locale = parts[index];
            break;
          }
          if (parts[index + 1].length() == 2
              && Character.isUpperCase(parts[index + 1].charAt(0))
              && Character.isUpperCase(parts[index + 1].charAt(1))) {
            locale = parts[index];
            for (int subindex = index + 1; subindex < parts.length; ++subindex) {
              locale += '_' + parts[subindex];
            }
            break;
          }
        }
      }
    }
    return locale;
  }
}
