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
package org.pentaho.platform.repository2.unified.fileio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.MessageFormat;

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class RepositoryFileInputStream extends InputStream {

  protected InputStream is = null;

  protected IUnifiedRepository repository = null;

  protected RepositoryFile file;
  
  protected SimpleRepositoryFileData fileData;

  public RepositoryFileInputStream(String path) throws FileNotFoundException {
    if (path == null) {
      throw new FileNotFoundException("Repository file path cannot be null");
    }
    repository = PentahoSystem.get(IUnifiedRepository.class);
    this.file = repository.getFile(path);
    if (file == null) {
      throw new FileNotFoundException(MessageFormat.format("Repository file {0} not readable or does not exist", path));
    }
    if (file.isFolder()) {
      throw new FileNotFoundException(MessageFormat.format("Repository file {0} is a directory", file.getPath()));
    }
  }

  public RepositoryFileInputStream(RepositoryFile file) throws FileNotFoundException {
    if (file == null) {
      throw new FileNotFoundException("Repository file cannot be null");
    }
    if (file.isFolder()) {
      throw new FileNotFoundException(MessageFormat.format("Repository file {0} is a directory", file.getPath()));
    }
    repository = PentahoSystem.get(IUnifiedRepository.class);
    this.file = repository.getFile(file.getPath());
  }

  public RepositoryFileInputStream(Serializable id) throws FileNotFoundException {
    repository = PentahoSystem.get(IUnifiedRepository.class);
    file = repository.getFileById(id);
    if (file == null) {
      throw new FileNotFoundException(MessageFormat.format(
          "Repository file with id {0} not readable or does not exist", id));
    }
    if (file.isFolder()) {
      throw new FileNotFoundException(MessageFormat.format("Repository file {0} is a directory", file.getPath()));
    }
  }

  public RepositoryFile getFile() {
    return file;
  }

  protected void setStream() throws FileNotFoundException {
    if (fileData == null) {
      fileData = repository.getDataForRead(file.getId(), SimpleRepositoryFileData.class);
    }
    is = fileData.getStream();
  }

  @Override
  public int read() throws IOException {
    if (is == null) {
      setStream();
    }
    return is.read();
  }
  
  public String getMimeType() {
    if (fileData == null) {
      fileData = repository.getDataForRead(file.getId(), SimpleRepositoryFileData.class);
    }
	  return fileData.getMimeType();
  }
}
