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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

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
