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


package org.pentaho.platform.plugin.services.importexport;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * An {@link org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle} that uses temporary
 * files.
 * 
 * @author mlowery
 */
public class RepositoryFileBundle implements IRepositoryFileBundle, Serializable {

  private static final long serialVersionUID = 4714531660593425523L;

  private RepositoryFileAcl acl;

  private RepositoryFile file;

  private File tmpFile;

  private String path;

  private String charset;

  private String mimeType;

  public RepositoryFileBundle( final RepositoryFile file, final RepositoryFileAcl acl, final String path,
      final File tmpFile, final String charset, final String mimeType ) {
    super();
    this.file = file;
    this.acl = acl;
    this.path = path;
    this.tmpFile = tmpFile;
    this.charset = charset;
    this.mimeType = mimeType;
  }

  public RepositoryFileAcl getAcl() {
    return acl;
  }

  public RepositoryFile getFile() {
    return file;
  }

  public InputStream getInputStream() throws IOException {
    return new BufferedInputStream( FileUtils.openInputStream( tmpFile ) );
  }

  public String getPath() {
    return path;
  }

  public void setPath( String path ) {
    this.path = path;
  }

  public String getCharset() {
    return charset;
  }

  public String getMimeType() {
    return mimeType;
  }

  public boolean equals( Object obj ) { // Bundles are equal if the path and the name are the same
    if ( !( obj instanceof RepositoryFileBundle ) ) {
      return false;
    }
    RepositoryFileBundle repoObj = (RepositoryFileBundle) obj;
    if ( !repoObj.getPath().equals( path ) ) {
      return false;
    }
    if ( !repoObj.getFile().getName().equals( file.getName() ) ) {
      return false;
    }
    return true;
  }

  public int hashCode() {
    return new HashCodeBuilder( 47, 53 ).append( path ).append( file.getName() ).toHashCode();
  }
}
