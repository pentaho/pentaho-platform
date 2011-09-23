package org.pentaho.platform.web.http.api.resources;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.action.IStreamingAction;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;

public class RepositoryFileStreamProvider implements IBackgroundExecutionStreamProvider {

  public String outputFilePath;
  public String inputFilePath;
  private IStreamingAction streamingAction;
  
  public static final String MIME_TYPE_HTML = "text/html"; //$NON-NLS-1$
  public static final String MIME_TYPE_PDF = "application/pdf"; //$NON-NLS-1$
  public static final String MIME_TYPE_XLS = "application/vnd.ms-excel"; //$NON-NLS-1$
  public static final String MIME_TYPE_RTF = "application/rtf"; //$NON-NLS-1$
  public static final String MIME_TYPE_CSV = "text/csv"; //$NON-NLS-1$
  public static final String MIME_TYPE_TXT = "text/plain"; //$NON-NLS-1$
  public static final String MIME_TYPE_XML = "application/xml"; //$NON-NLS-1$
  public static final String MIME_TYPE_PNG = "image/png"; //$NON-NLS-1$
  
  public RepositoryFileStreamProvider(String inputFilePath, String outputFilePath) {
    this.outputFilePath = outputFilePath;
    this.inputFilePath = inputFilePath;
  }
  
  public RepositoryFileStreamProvider() {
  }
  
  
  public IStreamingAction getStreamingAction() {
    return streamingAction;
  }

  public void setStreamingAction(IStreamingAction streamingAction) {
    this.streamingAction = streamingAction;
  }

  public OutputStream getOutputStream() throws Exception {
    IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);
    String tempOutputFilePath = outputFilePath;
    String baseFileName = FilenameUtils.getBaseName(tempOutputFilePath);
    String extension = FilenameUtils.getExtension(tempOutputFilePath);
    if ("*".equals(extension)) { //$NON-NLS-1$
      tempOutputFilePath = tempOutputFilePath.substring(0, tempOutputFilePath.lastIndexOf('.'));
      if (streamingAction != null) {
        String mimeType = streamingAction.getMimeType(null);
        if (MIME_TYPE_HTML.equals(mimeType)) {
          extension = "html"; //$NON-NLS-1$
        } else if (MIME_TYPE_XLS.equals(mimeType)) {
          extension = "xls"; //$NON-NLS-1$
        } else if (MIME_TYPE_CSV.equals(mimeType)) {
          extension = "csv"; //$NON-NLS-1$
        } else if (MIME_TYPE_RTF.equals(mimeType)) {
          extension = "rtf"; //$NON-NLS-1$
        } else if (MIME_TYPE_PDF.equals(mimeType)) {
          extension = "pdf"; //$NON-NLS-1$
        } else if (MIME_TYPE_TXT.equals(mimeType)) {
          extension = "txt"; //$NON-NLS-1$
        } else if (MIME_TYPE_XML.equals(mimeType)) {
          extension = "xml"; //$NON-NLS-1$
        } else if (MIME_TYPE_PNG.equals(mimeType)) {
          extension = "png"; //$NON-NLS-1$
        } else {
          extension = ""; //$NON-NLS-1$
        }    
        if (extension.length() > 0){
          tempOutputFilePath += ("." + extension); //$NON-NLS-1$
        }
      }
    }
    
    if (extension.length() > 0){
      extension = "." + extension; //$NON-NLS-1$
    }
    
    RepositoryFile repositoryFile = repository.getFile(tempOutputFilePath);
    RepositoryFile parentFolder = null;
    String newFileName = null;
    if (repositoryFile != null) {
      if (repositoryFile.isFolder()) {
        throw new FileNotFoundException();
      }
      parentFolder = repository.getFile(FilenameUtils.getFullPathNoEndSeparator(tempOutputFilePath));
      int nameCount = 1;
      while (repositoryFile != null) {
        nameCount ++;
        newFileName = baseFileName + "(" + nameCount + ")" + extension;  //$NON-NLS-1$ //$NON-NLS-2$
        repositoryFile = repository.getFile(FilenameUtils.getFullPathNoEndSeparator(tempOutputFilePath) + "/" + newFileName); //$NON-NLS-1$
      }
    } else {
      newFileName = FilenameUtils.getName(tempOutputFilePath);
      ArrayList<String> foldersToCreate = new ArrayList<String>();
      String parentPath = FilenameUtils.getFullPathNoEndSeparator(tempOutputFilePath);
      while ((parentPath != null) && (parentPath.length() > 0) && (repository.getFile(parentPath) == null)) {
        foldersToCreate.add(FilenameUtils.getName(parentPath));
        parentPath = FilenameUtils.getFullPathNoEndSeparator(parentPath);
      }
      Collections.reverse(foldersToCreate);
      parentFolder = ((parentPath != null) && (parentPath.length() > 0)) ? repository.getFile(parentPath) : repository.getFile(FileResource.PATH_SEPERATOR);
      if (!parentFolder.isFolder()) {
        throw new FileNotFoundException();
      }
      for (String folderName : foldersToCreate) {
        parentFolder = repository.createFolder(parentFolder.getId(), new RepositoryFile.Builder(folderName).folder(true).build(), null);
      }     
    }
    RepositoryFile outputFile = null;
    if (inputFilePath != null) {
      RepositoryFile inputFile = repository.getFile(inputFilePath);
      if ((inputFile != null) && !inputFile.isFolder()) {
        outputFile = new RepositoryFile.Builder(newFileName).creatorId(inputFile.getId().toString()).build();
      } else {
        outputFile = new RepositoryFile.Builder(newFileName).build();
      }
    }

    repositoryFile = repository.createFile(parentFolder.getId(), outputFile, new SimpleRepositoryFileData(IOUtils.toInputStream("", "UTF-8"), "UTF-8", "text/plain"), "");
    return new RepositoryFileOutputStream(repositoryFile);
  }

  public String getOutputFilePath() {
    return outputFilePath;
  }

  public void setOutputFilePath(String filePath) {
    this.outputFilePath = filePath;
  }

  public String getInputFilePath() {
    return inputFilePath;
  }

  public void setInputFilePath(String filePath) {
    this.inputFilePath = filePath;
  }

  public InputStream getInputStream() throws Exception {
    IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);
    RepositoryFile repositoryFile = repository.getFile(inputFilePath);
    if ((repositoryFile == null) || repositoryFile.isFolder()) {
      throw new FileNotFoundException();
    }
    return new RepositoryFileInputStream(repositoryFile);
  }
}
