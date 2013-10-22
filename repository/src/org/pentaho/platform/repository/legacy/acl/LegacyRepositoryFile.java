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
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository.legacy.acl;

import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IFileFilter;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class LegacyRepositoryFile implements ISolutionFile, IAclHolder, Serializable {

  private static final long serialVersionUID = -3181545217413101032L;

  public static final char EXTENSION_CHAR = '.';

  protected Serializable id;
  private String fileName;
  private String fullPath;
  private long lastModified;
  private boolean root;
  private boolean directory;
  private byte[] data;
  private String solution;
  private String solutionPath;
  protected LegacyRepositoryFile parent;
  private List<IPentahoAclEntry> accessControls = new ArrayList<IPentahoAclEntry>();
  private Set childrenFiles = new TreeSet();

  public LegacyRepositoryFile( String fileName, String fullPath, boolean directory ) {

    Assert.notNull( fileName );
    Assert.notNull( fullPath );
    Assert.notNull( directory );

    this.fileName = fileName;
    this.fullPath = fullPath;
    this.directory = directory;
  }

  @Override
  public String getFileName() {
    return fileName;
  }

  @Override
  public String getFullPath() {
    return fullPath;
  }

  @Override
  public String getExtension() {
    return hasExtension() ? fileName.substring( fileName.lastIndexOf( EXTENSION_CHAR ) ) : ""; //$NON-NLS-1$
  }

  @Override
  public long getLastModified() {
    return lastModified;
  }

  @Override
  public boolean isDirectory() {
    return directory;
  }

  @Override
  public boolean isRoot() {
    return root;
  }

  @Override
  public byte[] getData() {
    return data;
  }

  @Override
  public String getSolution() {
    return solution;
  }

  @Override
  public String getSolutionPath() {
    return solutionPath;
  }

  @Override
  public List<IPentahoAclEntry> getAccessControls() {
    return accessControls;
  }

  @Override
  public void resetAccessControls( List<IPentahoAclEntry> arg0 ) {
    this.accessControls = new ArrayList<IPentahoAclEntry>();
  }

  @Override
  public void setAccessControls( List<IPentahoAclEntry> arg0 ) {
    this.accessControls = arg0;
  }

  /**
   * Chains up to find the access controls that are in force on this object. Could end up chaining all the way to
   * the root.
   * 
   * <p>
   * Note that (1) defining no access control entries of your own and (2) removing all of your access control
   * entries is indistiguishable in the current design. In #1, we chain up because we inherit. But in #2, it might
   * be expected that by explicitly removing all access control entries, the chaining up ends. That is not the case
   * in the current design.
   * </p>
   */
  @Override
  public List<IPentahoAclEntry> getEffectiveAccessControls() {
    List acls = this.getAccessControls();
    if ( acls.size() == 0 ) {
      LegacyRepositoryFile chain = this;
      while ( !chain.isRoot() && ( acls.size() == 0 ) ) {
        chain = (LegacyRepositoryFile) chain.retrieveParent();
        acls = chain.getAccessControls();
      }
    }
    return acls;
  }

  @Override
  public boolean exists() {
    return true;
  }

  @Override
  public ISolutionFile[] listFiles() {
    Set<ISolutionFile> files = getChildrenFiles();
    return files.toArray( new ISolutionFile[] {} );
  }

  @Override
  public ISolutionFile[] listFiles( IFileFilter filter ) {
    List matchedFiles = new ArrayList();
    Object[] objArray = getChildrenFiles().toArray();
    for ( Object element : objArray ) {
      if ( filter.accept( (ISolutionFile) element ) ) {
        matchedFiles.add( element );
      }
    }
    return (ISolutionFile[]) matchedFiles.toArray( new ISolutionFile[] {} );
  }

  @Override
  public ISolutionFile retrieveParent() {
    return parent;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals( final Object other ) {
    if ( this == other ) {
      return true;
    }
    if ( !( other instanceof LegacyRepositoryFile ) ) {
      return false;
    }
    final LegacyRepositoryFile that = (LegacyRepositoryFile) other;
    return this.getId().equals( that.getId() );
  }

  public void setFileName( String fileName ) {
    this.fileName = fileName;
  }

  public void setFullPath( String fullPath ) {
    this.fullPath = fullPath;
  }

  public void setLastModified( long lastModified ) {
    this.lastModified = lastModified;
  }

  public void setRoot( boolean root ) {
    this.root = root;
  }

  public void setDirectory( boolean directory ) {
    this.directory = directory;
  }

  public void setData( byte[] data ) {
    this.data = data;
  }

  public void setSolution( String solution ) {
    this.solution = solution;
  }

  public void setSolutionPath( String solutionPath ) {
    this.solutionPath = solutionPath;
  }

  public Set getChildrenFiles() {
    return childrenFiles;
  }

  public void setChildrenFiles( Set childrenFiles ) {
    this.childrenFiles = childrenFiles;
  }

  private boolean hasExtension() {
    return fileName.lastIndexOf( LegacyRepositoryFile.EXTENSION_CHAR ) != -1;
  }

  public Serializable getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public LegacyRepositoryFile getParent() {
    return parent;
  }

  public void setParent( LegacyRepositoryFile parent ) {
    this.parent = parent;
  }

  public void setId( Serializable id ) {
    this.id = id;
  }
}
