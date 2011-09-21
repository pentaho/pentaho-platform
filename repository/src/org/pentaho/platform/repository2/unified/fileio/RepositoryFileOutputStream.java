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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.web.MimeHelper;

public class RepositoryFileOutputStream extends ByteArrayOutputStream {

  protected RepositoryFile file = null;
  protected RepositoryFile parent = null;
  protected IUnifiedRepository repository = null;
  protected String charsetName = null;
  protected boolean isNew;

  public RepositoryFileOutputStream(String path) throws FileNotFoundException {
    if (path == null) {
      throw new FileNotFoundException("Repository file path cannot be null");
    }
    prepFile(path);
  }

  public RepositoryFileOutputStream(RepositoryFile file) throws FileNotFoundException {
    if (file == null) {
      throw new FileNotFoundException("Repository file cannot be null");
    }
    prepFile(file.getPath());
  }

  public RepositoryFileOutputStream(Serializable id) throws FileNotFoundException {
    repository = PentahoSystem.get(IUnifiedRepository.class);
    file = repository.getFileById(id);
    if (file == null) {
      throw new FileNotFoundException(MessageFormat.format(
          "Repository file with id {0} not readable or does not exist", id));
    }
    parent = getParent(file.getPath());
  }
  
  protected void prepFile(String path) throws FileNotFoundException {
    repository = PentahoSystem.get(IUnifiedRepository.class);
    //see if file already exists
    file = repository.getFile(path);
    if (file == null) {
      //it doesn't exist, so try to create the file in the parent dir
      parent = getParent(path);
      if (parent == null) {
        throw new FileNotFoundException(MessageFormat.format(
            "Repository file {0} does not exist nor does it's parent, so the file cannot be created", path));
      }
      String relPath = path.substring(path.lastIndexOf('/')).replace("/", ""); //$NON-NLS-1$ //$NON-NLS-2$
      file = new RepositoryFile.Builder(relPath).versioned(true).build(); // Default versioned to true so that we're keeping history
      isNew = true;
    } else {
      if (file.isFolder()) {
        throw new FileNotFoundException(MessageFormat.format("Repository file {0} is a directory", file.getPath()));
      }
    }
  }
  
  ////
  //charsetName is required as metadata so the JCR can be provided the correct encoding, provided
  //we are storing text.  These are package private methosd only and should only be called by 
  //RepositoryFileWriter, because if you are wanting to write characters to a repository file,
  //you need to use RepositoryFileWriter, not this class.
  //
  protected RepositoryFileOutputStream(String path, String charsetName) throws FileNotFoundException {
    this(path);
    this.charsetName = charsetName;
  }
  protected RepositoryFileOutputStream(RepositoryFile file, String charsetName) throws FileNotFoundException {
    this(file);
    this.charsetName = charsetName;
  }
  protected RepositoryFileOutputStream(Serializable id, String charsetName) throws FileNotFoundException {
    this(id);
    this.charsetName = charsetName;
  }
  //
  ////
  
  protected RepositoryFile getParent(String path) {
    String newFilePath = StringUtils.removeEnd(path, "/"); //$NON-NLS-1$
    String parentPath = StringUtils.substringBeforeLast(newFilePath, "/"); //$NON-NLS-1$
    if(parentPath.isEmpty()) {
    	parentPath = "/";
    }
    return repository.getFile(parentPath);
  }

  @Override
  public void close() throws IOException {
    flush();
  }

  @Override
  public void flush() throws IOException {
    super.flush();
    
    ByteArrayInputStream bis = new ByteArrayInputStream(toByteArray());
    
    //make an effort to determine the correct mime type, default to application/octet-stream
    String ext = FilenameUtils.getExtension(file.getName());
    String mimeType = "application/octet-stream"; //$NON-NLS-1$
    if(ext != null) {
      String tempMimeType = MimeHelper.getMimeTypeFromExtension("."+ext); //$NON-NLS-1$
      if(tempMimeType != null) {
        mimeType = tempMimeType;
      }
    }
    
    //FIXME: not a good idea that we assume we are dealing with text.  Best if this is somehow moved to the RepositoryFileWriter
    // but I couldn't figure out a clean way to do that.  For now, charsetName is passed in here and we use it if available.
    final SimpleRepositoryFileData payload = new SimpleRepositoryFileData(bis, charsetName, mimeType);

    if(isNew) {
      repository.createFile(parent.getId(), file, payload, "commit from " + RepositoryFileOutputStream.class.getName()); //$NON-NLS-1$
    } else {
      repository.updateFile(file, payload, "commit from " + RepositoryFileOutputStream.class.getName()); //$NON-NLS-1$
    }
  }
}
