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
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class RepositoryFileReader extends InputStreamReader {
  
  static protected String getEncoding(RepositoryFile file) throws FileNotFoundException {
    IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);
    SimpleRepositoryFileData fileData = repository.getDataForRead(file.getId(), SimpleRepositoryFileData.class);
    return fileData.getEncoding();
  }
  static protected String getEncoding(String path) throws FileNotFoundException {
    IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);
    RepositoryFile file = (new RepositoryFileInputStream(path)).getFile();
    SimpleRepositoryFileData fileData = repository.getDataForRead(file.getId(), SimpleRepositoryFileData.class);
    return fileData.getEncoding();
  }
  static protected String getEncoding(Serializable id) throws FileNotFoundException {
    IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);
    RepositoryFile file = (new RepositoryFileInputStream(id)).getFile();
    SimpleRepositoryFileData fileData = repository.getDataForRead(file.getId(), SimpleRepositoryFileData.class);
    return fileData.getEncoding();
  }

  public RepositoryFileReader(String path) throws FileNotFoundException, UnsupportedEncodingException {
    super(new RepositoryFileInputStream(path), getEncoding(path));
  }

  public RepositoryFileReader(RepositoryFile file) throws FileNotFoundException, UnsupportedEncodingException {
    super(new RepositoryFileInputStream(file.getPath()), getEncoding(file.getPath()));
  }

  public RepositoryFileReader(Serializable id) throws FileNotFoundException, UnsupportedEncodingException {
    super(new RepositoryFileInputStream(id), getEncoding(id));
  }
}
