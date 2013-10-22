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

package org.pentaho.platform.repository.solution.dbbased;

import org.pentaho.platform.api.engine.IAclSolutionFile;
import org.pentaho.platform.api.engine.IFileFilter;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.repository.ISearchable;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.UUIDUtil;
import org.springframework.security.acl.basic.AclObjectIdentity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings( "deprecation" )
public class RepositoryFile implements ISearchable, Comparable, AclObjectIdentity, IAclSolutionFile, ISolutionFile {
  public static final char EXTENSION_CHAR = '.';

  private static final long serialVersionUID = -4129429077568560627L;

  private static final String EMPTY_STRING = ""; //$NON-NLS-1$

  private static final String[] SearchableColumns = {
    Messages.getInstance().getString( "SolutionRepository.QUERY_COLUMN_NAME" ), //$NON-NLS-1$
    Messages.getInstance().getString( "SolutionRepository.QUERY_COLUMN_PATH" ), //$NON-NLS-1$
    Messages.getInstance().getString( "SolutionRepository.QUERY_COLUMN_PARENT" ) //$NON-NLS-1$
  };

  private static final String SearchableTable = "org.pentaho.platform.repository.solution.dbbased.RepositoryFile"; //$NON-NLS-1$

  private static final String SearchablePhraseNamedQuery =
      "org.pentaho.platform.repository.solution.dbbased.RepositoryFile.folderSearcher"; //$NON-NLS-1$

  protected int revision;

  protected String fileId;

  protected RepositoryFile parent;

  protected String fileName;

  protected String fullPath;

  protected long lastModified;

  protected boolean directory = true;

  private byte[] data = null;

  private Set childrenFiles = new TreeSet();

  private List<IPentahoAclEntry> accessControls = new ArrayList<IPentahoAclEntry>();

  public RepositoryFile() {
    super();
  }

  public RepositoryFile( final String fileName, final RepositoryFile parent, final byte[] data ) {
    this( fileName, parent, data, System.currentTimeMillis() );
  }

  public RepositoryFile( final String fileName, final RepositoryFile parent, final byte[] data,
                         final long lastModified ) {
    this();
    this.fileId = UUIDUtil.getUUIDAsString();

    this.fileName = fileName;
    if ( parent != null ) {
      parent.addChildFile( this );
    }
    setParent( parent );
    setData( data );
    setLastModified( lastModified );
    directory = data == null;
  }

  @Override
  public int hashCode() {
    return fileId.hashCode();
  }

  @Override
  public boolean equals( final Object other ) {
    if ( this == other ) {
      return true;
    }
    if ( !( other instanceof RepositoryFile ) ) {
      return false;
    }
    final RepositoryFile that = (RepositoryFile) other;
    return getFileId().equals( that.getFileId() );
  }

  protected void resolvePath() {
    StringBuffer buffer = new StringBuffer( RepositoryFile.EMPTY_STRING );

    if ( parent != null ) {
      buffer.append( parent.getFullPath() );
    }
    buffer.append( org.pentaho.platform.api.repository2.unified.RepositoryFile.SEPARATOR );
    buffer.append( fileName );

    setFullPath( buffer.toString() );
  }

  public List<IPentahoAclEntry> getAccessControls() {
    return this.accessControls;
  }

  /**
   * This method's purpose is to allow Hibernate to initialize the ACLs from the data-store. Application clients
   * should likely use resetAccessControls.
   */
  public void setAccessControls( final List<IPentahoAclEntry> acls ) {
    this.accessControls = acls;
  }

  public void resetAccessControls( final List<IPentahoAclEntry> acls ) {
    if ( this.accessControls != null ) {
      this.accessControls.clear();
      this.accessControls.addAll( acls );
    }
  }

  public int getRevision() {
    return revision;
  }

  protected void setRevision( final int revision ) {
    this.revision = revision;
  }

  public String getFileId() {
    return fileId;
  }

  protected void setFileId( final String fileId ) {
    this.fileId = fileId;
  }

  public String getSolution() {
    return getTopFolder().getFileName();
  }

  public String getSolutionPath() {
    ArrayList pathList = new ArrayList();
    ISolutionFile folder = parent;
    while ( !folder.isRoot() && folder.retrieveParent() != null ) {
      pathList.add( folder.getFileName() );
      folder = folder.retrieveParent();
    }
    StringBuffer buffer = new StringBuffer( RepositoryFile.EMPTY_STRING );
    for ( int i = pathList.size() - 1; i >= 0; i-- ) {
      buffer.append( org.pentaho.platform.api.repository2.unified.RepositoryFile.SEPARATOR );
      buffer.append( pathList.get( i ).toString() );
    }
    return buffer.toString();
  }

  public String getFileName() {
    return fileName;
  }

  protected void setFileName( final String fileName ) {
    this.fileName = fileName;
    resolvePath();
  }

  public String getFullPath() {
    return fullPath;
  }

  protected void setFullPath( final String fullPath ) {
    this.fullPath = fullPath;
  }

  public void setParent( final RepositoryFile parent ) {
    this.parent = parent;
    resolvePath();
  }

  public RepositoryFile getParent() {
    return parent;
  }

  public ISolutionFile retrieveParent() {
    return parent;
  }

  protected RepositoryFile getTopFolder() {
    RepositoryFile topFolder = parent;
    if ( topFolder == null ) {
      return this;
    }
    while ( !topFolder.isRoot() ) {
      topFolder = (RepositoryFile) topFolder.retrieveParent();
    }
    return topFolder;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.repository.ISearchable#getSearchableColumns()
   */
  public String[] getSearchableColumns() {
    return RepositoryFile.SearchableColumns;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.repository.ISearchable#getSearchableTable()
   */
  public String getSearchableTable() {
    return RepositoryFile.SearchableTable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.repository.ISearchable#getPhraseSearchQueryName()
   */
  public String getPhraseSearchQueryName() {
    return RepositoryFile.SearchablePhraseNamedQuery;
  }

  protected void setDirectory( final boolean directory ) {
    this.directory = directory;
  }

  protected boolean getDirectory() {
    return directory;
  }

  public boolean isDirectory() {
    return getDirectory();
  }

  /**
   * @return Returns the childrenResources.
   */
  public Set getChildrenFiles() {
    return childrenFiles;
  }

  /**
   * @param childrenResources
   *          The childrenResources to set.
   */
  public void setChildrenFiles( final Set childrenFiles ) {
    this.childrenFiles = childrenFiles;
  }

  public void addChildFile( final RepositoryFile file ) {
    getChildrenFiles().add( file );
  }

  public void removeChildFile( final RepositoryFile file ) {
    getChildrenFiles().remove( file );
    file.setParent( null ); // as of now this file has no parent.
  }

  /**
   * @return Returns the data.
   */
  public byte[] getData() {
    return data;
  }

  /**
   * @param data
   *          The data to set.
   */
  public void setData( final byte[] data ) {
    this.data = data;
  }

  public ISolutionFile[] listFiles( final IFileFilter filter ) {
    List matchedFiles = new ArrayList();
    Object[] objArray = getChildrenFiles().toArray();
    for ( Object element : objArray ) {
      if ( filter.accept( (ISolutionFile) element ) ) {
        matchedFiles.add( element );
      }
    }
    return (ISolutionFile[]) matchedFiles.toArray( new ISolutionFile[] {} );
  }

  public ISolutionFile[] listFiles() {
    Set<ISolutionFile> files = getChildrenFiles();
    return files.toArray( new ISolutionFile[] {} );
  }

  public RepositoryFile[] listRepositoryFiles() {
    RepositoryFile[] files = new RepositoryFile[getChildrenFiles().size()];
    Iterator iter = getChildrenFiles().iterator();
    int i = 0;
    while ( iter.hasNext() ) {
      files[i] = (RepositoryFile) iter.next();
      i++;
    }
    return files;
  }

  public int compareTo( final Object o ) {
    if ( o == null ) {
      return 1;
    } else if ( o instanceof RepositoryFile ) {
      RepositoryFile that = (RepositoryFile) o;
      if ( ( this.getFullPath() == null ) && ( that.getFullPath() == null ) ) {
        return 0;
      } else if ( ( this.getFullPath() == null ) && ( that.getFullPath() != null ) ) {
        return -1;
      } else if ( ( this.getFullPath() != null ) && ( that.getFullPath() == null ) ) {
        return 1;
      } else {
        return this.getFullPath().compareTo( ( (RepositoryFile) o ).getFullPath() );
      }
    } else {
      return this.getFullPath().compareTo( o.toString() );
    }
  }

  /**
   * @return Returns the modDate.
   */
  public long getLastModified() {
    return lastModified;
  }

  /**
   * @param modDate
   *          The modDate to set.
   */
  public void setLastModified( final long modDate ) {
    this.lastModified = modDate;
  }

  public boolean containsActions() {
    boolean hasActions = false;
    if ( this.isDirectory() ) {
      Set children = getChildrenFiles();
      Iterator iter = children.iterator();
      while ( iter.hasNext() && !hasActions ) {
        RepositoryFile file = (RepositoryFile) iter.next();
        hasActions = file.getFileName().toLowerCase().endsWith( ".xaction" ); //$NON-NLS-1$
      }
    }
    return hasActions;
  }

  public boolean isRoot() {
    return retrieveParent() == null;
  }

  /**
   * @return a boolean indicating if this file has an extension
   */
  public boolean hasExtension() {
    return fileName.lastIndexOf( RepositoryFile.EXTENSION_CHAR ) != -1;
  }

  /**
   * @return the extension (including the . seperator) of this file
   */
  public String getExtension() {
    return hasExtension() ? fileName.substring( fileName.lastIndexOf( RepositoryFile.EXTENSION_CHAR ) ) : ""; //$NON-NLS-1$
  }

  public boolean exists() {
    return true;
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
  public List<IPentahoAclEntry> getEffectiveAccessControls() {
    List acls = this.getAccessControls();
    if ( acls.size() == 0 ) {
      RepositoryFile chain = this;
      while ( !chain.isRoot() && ( acls.size() == 0 ) ) {
        chain = (RepositoryFile) chain.retrieveParent();
        acls = chain.getAccessControls();
      }
    }
    return acls;
  }

}
