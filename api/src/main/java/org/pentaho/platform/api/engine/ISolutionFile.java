/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
