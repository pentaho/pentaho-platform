package org.pentaho.platform.repository2.unified.fileio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
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
        outputStream = null;
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
      IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);
      RepositoryFile repositoryFile = repository.getFile(filePath);
      try {
        if ((repositoryFile == null) || repositoryFile.isFolder()) {
          throw new FileNotFoundException();
        }
        return new RepositoryFileInputStream(repositoryFile);
      } catch (FileNotFoundException e) {
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
      IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);
      
      RepositoryFile repositoryFile = repository.getFile(filePath);
      RepositoryFile parentFolder = null;
      if (repositoryFile != null) {
        if (repositoryFile.isFolder()) {
          throw new FileNotFoundException();
        }
        outputStream = new RepositoryFileOutputStream(repositoryFile);
      } else {
        ArrayList<String> foldersToCreate = new ArrayList<String>();
        String parentPath = FilenameUtils.getFullPathNoEndSeparator(filePath);
        while ((parentPath != null) && (parentPath.length() > 0) && (repository.getFile(parentPath) == null)) {
          foldersToCreate.add(FilenameUtils.getName(parentPath));
          parentPath = FilenameUtils.getFullPathNoEndSeparator(parentPath);
        }
        Collections.reverse(foldersToCreate);
        parentFolder = ((parentPath != null) && (parentPath.length() > 0)) ? repository.getFile(parentPath) : repository.getFile("/");
        if (!parentFolder.isFolder()) {
          throw new FileNotFoundException();
        }
        for (String folderName : foldersToCreate) {
          parentFolder = repository.createFolder(parentFolder.getId(), new RepositoryFile.Builder(folderName).folder(true).build(), null);
        }     
        RepositoryFile outputFile = new RepositoryFile.Builder(FilenameUtils.getName(filePath)).build();
        repositoryFile = repository.createFile(parentFolder.getId(), outputFile, new SimpleRepositoryFileData(IOUtils.toInputStream("", "UTF-8"), "UTF-8", getMimeType()), "");
        outputStream = new RepositoryFileOutputStream(repositoryFile);
      }
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
