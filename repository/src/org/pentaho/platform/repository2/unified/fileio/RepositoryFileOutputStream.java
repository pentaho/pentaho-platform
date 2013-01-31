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
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.repository2.unified.ISourcesStreamEvents;
import org.pentaho.platform.api.repository2.unified.IStreamListener;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.util.web.MimeHelper;

public class RepositoryFileOutputStream extends ByteArrayOutputStream implements ISourcesStreamEvents {

  protected String path = null;
  protected IUnifiedRepository repository;
  protected String charsetName = null;
  protected boolean autoCreateUniqueFileName = false;
  protected boolean autoCreateDirStructure = false;
  protected boolean closed = false;
  protected boolean flushed = false;
  protected ArrayList<IStreamListener> listeners = new ArrayList<IStreamListener>();

  public RepositoryFileOutputStream(final String path,
                                    final boolean autoCreateUniqueFileName,
                                    final boolean autoCreateDirStructure,
                                    final IUnifiedRepository repository) {
    setRepository(repository);
    this.path = path;
    this.autoCreateDirStructure = autoCreateDirStructure;
    this.autoCreateUniqueFileName = autoCreateUniqueFileName;
  }

  public RepositoryFileOutputStream(final Serializable id,
                                    final boolean autoCreateUniqueFileName,
                                    final boolean autoCreateDirStructure,
                                    final IUnifiedRepository repository)
      throws FileNotFoundException {
    setRepository(repository);
    RepositoryFile file = this.repository.getFileById(id);
    if (file == null) {
      throw new FileNotFoundException(MessageFormat.format(
          "Repository file with id {0} not readable or does not exist", id));
    }
    this.path = file.getPath();
    this.autoCreateDirStructure = autoCreateDirStructure;
    this.autoCreateUniqueFileName = autoCreateUniqueFileName;
  }

  public RepositoryFileOutputStream(final RepositoryFile file,
                                    final boolean autoCreateUniqueFileName,
                                    final boolean autoCreateDirStructure,
                                    final IUnifiedRepository repository) {
    this(file.getPath(), autoCreateUniqueFileName, autoCreateDirStructure, repository);
  }

  public RepositoryFileOutputStream(final String path,
                                    final boolean autoCreateUniqueFileName,
                                    final boolean autoCreateDirStructure) {
    this(path, autoCreateUniqueFileName, autoCreateDirStructure, null);
  }

  public RepositoryFileOutputStream(final Serializable id,
                                    final boolean autoCreateUniqueFileName,
                                    final boolean autoCreateDirStructure)
      throws FileNotFoundException {
    this(id, autoCreateUniqueFileName, autoCreateDirStructure, null);
  }

  public RepositoryFileOutputStream(final RepositoryFile file,
                                    final boolean autoCreateUniqueFileName,
                                    final boolean autoCreateDirStructure) {
    this(file, autoCreateUniqueFileName, autoCreateDirStructure, null);
  }

  public RepositoryFileOutputStream(final String path) {
    this(path, false, false, null);
  }

  public RepositoryFileOutputStream(final RepositoryFile file) {
    this(file.getPath(), false, false, null);
  }

  public RepositoryFileOutputStream(final Serializable id) throws FileNotFoundException {
    this(id, false, false, null);
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
    if (parentPath.isEmpty()) {
      parentPath = "/";
    }
    return repository.getFile(parentPath);
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      flush();
      closed = true;
      reset();
    }
  }

  @Override
  public void flush() throws IOException {
    if (closed) {
      return;
    }
    super.flush();

    ByteArrayInputStream bis = new ByteArrayInputStream(toByteArray());

    //make an effort to determine the correct mime type, default to application/octet-stream
    String ext = RepositoryFilenameUtils.getExtension(path);
    String mimeType = "application/octet-stream"; //$NON-NLS-1$
    if (ext != null) {
      String tempMimeType = MimeHelper.getMimeTypeFromExtension("." + ext); //$NON-NLS-1$
      if (tempMimeType != null) {
        mimeType = tempMimeType;
      }
    }

    //FIXME: not a good idea that we assume we are dealing with text.  Best if this is somehow moved to the RepositoryFileWriter
    // but I couldn't figure out a clean way to do that.  For now, charsetName is passed in here and we use it if available.
    final SimpleRepositoryFileData payload = new SimpleRepositoryFileData(bis, charsetName, mimeType);
    if (!flushed) {
      RepositoryFile file = repository.getFile(path);
      RepositoryFile parentFolder = getParent(path);
      String baseFileName = RepositoryFilenameUtils.getBaseName(path);
      String extension = RepositoryFilenameUtils.getExtension(path);
      if (file == null) {
        if (autoCreateDirStructure) {
          ArrayList<String> foldersToCreate = new ArrayList<String>();
          String parentPath = RepositoryFilenameUtils.getFullPathNoEndSeparator(path);
          // Make sure the parent path isn't the root
          while ((parentPath != null) && (parentPath.length() > 0 && !path.equals(parentPath))
              && (repository.getFile(parentPath) == null)) {
            foldersToCreate.add(RepositoryFilenameUtils.getName(parentPath));
            parentPath = RepositoryFilenameUtils.getFullPathNoEndSeparator(parentPath);
          }
          Collections.reverse(foldersToCreate);
          parentFolder = ((parentPath != null) && (parentPath.length() > 0)) ? repository.getFile(parentPath) : repository.getFile("/");
          if (!parentFolder.isFolder()) {
            throw new FileNotFoundException();
          }
          for (String folderName : foldersToCreate) {
            parentFolder = repository.createFolder(parentFolder.getId(), new RepositoryFile.Builder(folderName).folder(true).build(), null);
          }
        } else {
          if (parentFolder == null) {
            throw new FileNotFoundException();
          }
        }
        file = new RepositoryFile.Builder(RepositoryFilenameUtils.getName(path)).versioned(true).build(); // Default
        // versioned to true so that we're keeping history
        file = repository.createFile(parentFolder.getId(), file, payload, "commit from " + RepositoryFileOutputStream.class.getName()); //$NON-NLS-1$
        for (IRepositoryFileOutputStreamListener listener : listeners) {
          listener.fileCreated(path);
        }
      } else if (file.isFolder()) {
        throw new FileNotFoundException(MessageFormat.format("Repository file {0} is a directory", file.getPath()));
      } else {
        if (autoCreateUniqueFileName) {
          int nameCount = 1;
          String newFileName = null;
          while (file != null) {
            nameCount++;
            if ((extension != null) && (extension.length() > 0)) {
              newFileName = baseFileName + "(" + nameCount + ")." + extension;  //$NON-NLS-1$ //$NON-NLS-2$
            } else {
              newFileName = baseFileName + "(" + nameCount + ")";  //$NON-NLS-1$ //$NON-NLS-2$
            }
            file = repository.getFile(parentFolder.getPath() + "/" + newFileName); //$NON-NLS-1$
          }
          file = new RepositoryFile.Builder(newFileName).versioned(true).build(); // Default versioned to true so that we're keeping history
          file = repository.createFile(parentFolder.getId(), file, payload, "New File"); //$NON-NLS-1$
          path = file.getPath();
          for (IRepositoryFileOutputStreamListener listener : listeners) {
            listener.fileCreated(path);
          }
        } else {
          repository.updateFile(file, payload, "New File"); //$NON-NLS-1$
        }
      }
    } else {
      RepositoryFile file = repository.getFile(path);
      repository.updateFile(file, payload, "New File"); //$NON-NLS-1$
    }
    flushed = true;
  }

  public String getFilePath() {
    return path;
  }

  public void setFilePath(String path) {
    if (!path.equals(this.path)) {
      this.path = path;
      reset();
      flushed = false;
      closed = false;
    }
  }

  public boolean getAutoCreateUniqueFileName() {
    return autoCreateUniqueFileName;
  }

  public boolean getAutoCreateDirStructure() {
    return autoCreateDirStructure;
  }

  public void addListener(IStreamListener listener) {
    listeners.add(listener);
  }

  public void setRepository(final IUnifiedRepository repository) {
    this.repository = (repository != null ? repository : PentahoSystem.get(IUnifiedRepository.class));
  }

  public IUnifiedRepository getRepository() {
    return this.repository;
  }

  public String getCharsetName() {
    return charsetName;
  }

  public void setCharsetName(final String charsetName) {
    this.charsetName = charsetName;
  }
  
  public boolean isFlushed() {
  	return flushed;
  }
}
