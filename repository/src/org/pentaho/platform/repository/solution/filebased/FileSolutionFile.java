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
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.repository.solution.filebased;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IFileFilter;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.util.FileHelper;

public class FileSolutionFile implements ISolutionFile {
  String solutionName = ""; //$NON-NLS-1$

  String pathName = ""; //$NON-NLS-1$

  String fileName = ""; //$NON-NLS-1$

  String extension = ""; //$NON-NLS-1$

  int solutionAbsoluteStart = 0;

  File file = null;

  File solutionRoot = null;

  public FileSolutionFile(final File inFile, final File inSolutionRoot) {
    if (inFile == null) {
      throw new NullPointerException("The file can not be null!"); //$NON-NLS-1$
    }

    file = inFile;
    solutionRoot = inSolutionRoot;
    solutionAbsoluteStart = (solutionRoot == null) ? 0 : solutionRoot.getAbsolutePath().length() - solutionRoot.getName().length();
    int solutionNameLength = (solutionRoot == null) ? 0 : solutionRoot.getName().length();
    // Keep track of where the solution root is
    // Chop off the path info before the root dir to make it start at
    // solution root
    String fullName = file.getAbsolutePath().substring(solutionAbsoluteStart);
    // windows \ characters in the path gets messy in urls and xml, so
    // switch them to /
    fullName = fullName.replace('\\', '/');
    solutionName = fullName.substring(0, solutionNameLength);
    fileName = file.getName();
    if (file.isDirectory()) {
      if (fullName.length() > solutionNameLength) {
        pathName = fullName.substring(solutionNameLength + 1);
      }
    } else {
      if ((solutionNameLength > 0) && (fullName.length() > (solutionNameLength + fileName.length() + 1))) {
        pathName = fullName.substring(solutionNameLength + 1, fullName.length() - fileName.length() - 1);
      } else {
        pathName = fullName.substring(0, fullName.length() - fileName.length() - 1);
      }
      if (pathName.equals(solutionName)) {
        pathName = ""; //$NON-NLS-1$
      }
    }
    int pos = fileName.lastIndexOf('.');
    if (pos != -1) {
      extension = fileName.substring(pos + 1);
    }
  }

  public boolean isDirectory() {
    return (file != null) && (file.isDirectory());
  }

  public String getFileName() {
    return (fileName);
  }

  public String getSolutionPath() {
    return (pathName);
  }

  public String getSolution() {
    return (solutionName);
  }

  public String getExtension() {
    return extension;
  }

  public String getFullPath() {
    String fullName = "/" + solutionName;//$NON-NLS-1$
    if (pathName.length() > 0) {
      fullName += "/" + pathName; //$NON-NLS-1$
    }
    if (!isDirectory() && (fileName.length() > 0)) {
      fullName += "/" + fileName; //$NON-NLS-1$
    }
    return (fullName);
  }

  public String getFileType() {
    int dotIndex = fileName.lastIndexOf('.');
    return ((dotIndex < 0) ? "" : fileName.substring(dotIndex)); //$NON-NLS-1$
  }

  public ISolutionFile[] listFiles() {
    if (file == null) {
      return (null);
    }
    File files[] = file.listFiles();
    if (files == null) {
      return (null);
    }
    ISolutionFile solFiles[] = new ISolutionFile[files.length];
    FileSolutionFile solFile;
    for (int i = 0; i < files.length; ++i) {
      solFile = new FileSolutionFile(files[i], solutionRoot);
      solFiles[i] = solFile;
    }
    return (solFiles);
  }

  @Override
  public String toString() {
    return (getSolution() + " : " + getSolutionPath() + " : " + getFileName() + " : " + getFileType()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public static void main(final String[] args) {
    File f = new File("E:/eclipse/workspace/pentaho-samples/solutions/test-solution"); //$NON-NLS-1$
    FileSolutionFile.printRecursive(new FileSolutionFile(f, f));
  }

  private static void printRecursive(final ISolutionFile sFile) {
    if (sFile.isDirectory()) {
      ISolutionFile sFiles[] = sFile.listFiles();
      for (ISolutionFile element : sFiles) {
        FileSolutionFile.printRecursive(element);
      }
    }
  }

  public boolean isRoot() {
    if (file != null) {
      if (solutionRoot != null) {
        try {
          return (solutionRoot.getCanonicalPath().equals(file.getCanonicalPath()));
        } catch (IOException e) {
        }
      } else {
        return (file.getParentFile() == null);
      }
    }
    return false;
  }

  public byte[] getData() {
    try {
      return FileHelper.getBytesFromFile(file);
    } catch (IOException e) {
      return null;
    }
  }

  public ISolutionFile retrieveParent() {
    return new FileSolutionFile(file.getParentFile(), solutionRoot);
  }

  public boolean exists() {
    return file.exists();
  }

  public File getFile() {
    return file;
  }

  public long getLastModified() {
    return file.lastModified();
  }

  public ISolutionFile[] listFiles(final IFileFilter filter) {
    List matchedFiles = new ArrayList();
    Object[] objArray = listFiles();
    for (Object element : objArray) {
      if (filter.accept((ISolutionFile) element)) {
        matchedFiles.add(element);
      }
    }
    return (ISolutionFile[]) matchedFiles.toArray(new ISolutionFile[] {});
  }

}
