package org.pentaho.platform.repository2.unified.fileio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.web.MimeHelper;

public class RepositoryFileContentItem implements IContentItem {

  String filePath;
  RepositoryFileInputStream inputStream;
  RepositoryFileOutputStream outputStream;
  
  public RepositoryFileContentItem(String filePath) {
    this.filePath = filePath;
  }
  
  public void closeOutputStream() {
    if (outputStream != null) {
      try {
        outputStream.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public IPentahoStreamSource getDataSource() {
    return new IPentahoStreamSource() {
      
      public OutputStream getOutputStream() throws IOException {
        return RepositoryFileContentItem.this.getOutputStream(null);
      }
      
      public String getName() {
        return FilenameUtils.getName(filePath);
      }
      
      public InputStream getInputStream() throws IOException {
        return RepositoryFileContentItem.this.getInputStream();
      }
      
      public String getContentType() {
        return getMimeType();
      }
    };
  }

  public InputStream getInputStream() throws ContentException {
    if (inputStream == null) {
      try {
        RepositoryFileOutputStream outputStream = (RepositoryFileOutputStream)getOutputStream(null);
        if ((outputStream.version) && !(outputStream.flushed)) {
          throw new FileNotFoundException("File not yet versioned.");
        }
        if (inputStream == null) {
          IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);
          RepositoryFile repositoryFile = repository.getFile(outputStream.path);
            if ((repositoryFile == null) || repositoryFile.isFolder()) {
              throw new FileNotFoundException();
            }
            return new RepositoryFileInputStream(repositoryFile);
        }
      } catch (FileNotFoundException e) {
        throw new ContentException(e);
      } catch (IOException e) {
        throw new ContentException(e);
      }
    }
    return inputStream;
  }

  public String getMimeType() {
    return MimeHelper.getMimeTypeFromExtension("." + FilenameUtils.getExtension(filePath));
  }

  public OutputStream getOutputStream(String arg0) throws IOException {
    if (outputStream == null) {
      outputStream = new RepositoryFileOutputStream(filePath);
    }
    return outputStream;
  }

  public String getPath() {
    return filePath;
  }

  public void setMimeType(String mimeType) {
    String fileExtension = MimeHelper.getExtension(mimeType);
    if (fileExtension == null) {
      throw new IllegalArgumentException("Unknown mime type");
    }
    String currentFileExtension = FilenameUtils.getExtension(filePath);
    if (!fileExtension.equals(currentFileExtension)) {
      outputStream = null;
      inputStream = null;
      filePath = FilenameUtils.getFullPathNoEndSeparator(filePath) + "/" + FilenameUtils.getBaseName(filePath) + fileExtension;
    }
  }

  public void setName(String arg0) {
    // TODO Auto-generated method stub

  }

}
