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
 */
package org.pentaho.platform.repository2.unified.importexport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.repository2.unified.importexport.ImportSource.IRepositoryFileBundle;

/**
 * An {@link IRepositoryFileBundle} that uses temporary files.
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

  public RepositoryFileBundle(final RepositoryFile file, final RepositoryFileAcl acl, final String path,
      final File tmpFile, final String charset, final String mimeType) {
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
    return FileUtils.openInputStream(tmpFile);
  }

  public String getPath() {
    return path;
  }

  public String getCharset() {
    return charset;
  }

  public String getMimeType() {
    return mimeType;  
  }
  
  public boolean equals(Object obj) { // Bundles are equal if the path and the name are the same
    if (!(obj instanceof RepositoryFileBundle)) {
      return false;
    }
    RepositoryFileBundle repoObj = (RepositoryFileBundle) obj;
    if (!repoObj.getPath().equals(path)) {
      return false;
    }
    if (!repoObj.getFile().getName().equals(file.getName())) {
      return false;
    }
    return true;
  }

}