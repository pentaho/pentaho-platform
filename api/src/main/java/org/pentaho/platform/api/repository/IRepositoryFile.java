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


package org.pentaho.platform.api.repository;

import org.pentaho.platform.api.engine.IFileFilter;
import org.pentaho.platform.api.engine.ISolutionFile;

import java.util.List;
import java.util.Set;

public interface IRepositoryFile extends ISearchable {

  public static final char SEPARATOR = '/';

  /**
   * This method's purpose is to allow Hibernate to initialize the ACLs from the data-store. Application clients
   * should likely use resetAccessControls.
   */
  @SuppressWarnings( "rawtypes" )
  public void setAccessControls( List acls );

  @SuppressWarnings( "rawtypes" )
  public void resetAccessControls( List acls );

  public int getRevision();

  public String getFileId();

  public String getSolution();

  public String getSolutionPath();

  public String getFileName();

  public String getFullPath();

  public void setParent( IRepositoryFile parent );

  public IRepositoryFile getParent();

  public ISolutionFile retrieveParent();

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.repository.ISearchable#getSearchableColumns()
   */
  public String[] getSearchableColumns();

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.repository.ISearchable#getSearchableTable()
   */
  public String getSearchableTable();

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.repository.ISearchable#getPhraseSearchQueryName()
   */
  public String getPhraseSearchQueryName();

  public boolean isDirectory();

  /**
   * @return Returns the childrenResources.
   */
  @SuppressWarnings( "rawtypes" )
  public Set getChildrenFiles();

  /**
   * @param childrenResources
   *          The childrenResources to set.
   */
  @SuppressWarnings( "rawtypes" )
  public void setChildrenFiles( Set childrenFiles );

  public void addChildFile( IRepositoryFile file );

  public void removeChildFile( IRepositoryFile file );

  /**
   * @return Returns the data.
   */
  public byte[] getData();

  /**
   * @param data
   *          The data to set.
   */
  public void setData( byte[] data );

  public ISolutionFile[] listFiles( IFileFilter filter );

  public ISolutionFile[] listFiles();

  public IRepositoryFile[] listRepositoryFiles();

  public int compareTo( Object o );

  /**
   * @return Returns the modDate.
   */
  public long getLastModified();

  /**
   * @param modDate
   *          The modDate to set.
   */
  public void setLastModified( long modDate );

  public boolean containsActions();

  public boolean isRoot();

  /**
   * @return a boolean indicating if this file has an extension
   */
  public boolean hasExtension();

  /**
   * @return the extension (including the . seperator) of this file
   */
  public String getExtension();

  public boolean exists();

}
