/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.engine;

/**
 * A solution file is an abstract represention of either a file or a directory. This allows us to treat files and
 * directories as the same thing for the purposes of hierarchical navigation
 * 
 * @author jdixon
 */
public interface ISolutionFile {
  /**
   * @return true if the ISolutionFile points to a directory.
   */
  public boolean isDirectory();

  /**
   * @return The name of the file/directory
   */
  public String getFileName();

  /**
   * @return path within the solution to the file/directory.
   */
  public String getSolutionPath();

  /**
   * @return Name of the solution this file/directory is contained within
   */
  public String getSolution();

  /**
   * @return fully qualified path to this file/directory
   */
  public String getFullPath();

  // public String getFileType();
  /**
   * If this is a directory, will list all children files/directories for hierarchical navigation.
   */
  public ISolutionFile[] listFiles();

  public ISolutionFile[] listFiles( IFileFilter filter );

  /**
   * @return true if this is the root solution folder NOTE: This will always return false for a filebased solution
   */
  public boolean isRoot();

  /**
   * NOTE: This method is named 'retrieveParent' instead of 'getParent' because the generic ISolutionFile type is
   * not a 'Hibernate' described type and one of the base classes (RepositoryFile) will fail dependency checking.
   * 
   * @return The parent file for this ISolutionFile
   */
  public ISolutionFile retrieveParent();

  public byte[] getData();

  public boolean exists();

  /**
   * @return Returns the modDate.
   */
  public long getLastModified();

  public String getExtension();

}
