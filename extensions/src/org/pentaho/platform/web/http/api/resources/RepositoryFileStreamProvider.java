package org.pentaho.platform.web.http.api.resources;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import org.pentaho.platform.api.action.IStreamingAction;
import org.pentaho.platform.api.repository2.unified.IStreamListener;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.util.web.MimeHelper;

public class RepositoryFileStreamProvider implements IBackgroundExecutionStreamProvider, IStreamListener {

  // providing a serialVersionUID will help prevent quartz from throwing incompatible class exceptions
  private static final long serialVersionUID = 2812310908328498989L;
  
  public String outputFilePath;
  public String inputFilePath;
  private IStreamingAction streamingAction;
  
  public static final String MIME_TYPE_HTML = "text/html"; //$NON-NLS-1$
  public static final String MIME_TYPE_PDF = "application/pdf"; //$NON-NLS-1$
  public static final String MIME_TYPE_XLS = "application/vnd.ms-excel"; //$NON-NLS-1$
  public static final String MIME_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; //$NON-NLS-1$
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

  public String getOutputPath() {
    return outputFilePath;
  }
  
  public String getMimeType() {
	String mimeType = null;
    if (streamingAction != null) {
      mimeType = streamingAction.getMimeType(null);
    }
	if (mimeType == null) {
      mimeType = MimeHelper.getMimeTypeFromFileName(outputFilePath);
	}
	if (mimeType == null) {
      mimeType="binary/octet-stream";
	}
    return mimeType;  
  }
  
  public OutputStream getOutputStream() throws Exception {
    String tempOutputFilePath = outputFilePath;
    String extension = RepositoryFilenameUtils.getExtension(tempOutputFilePath);
    if ("*".equals(extension)) { //$NON-NLS-1$
      tempOutputFilePath = tempOutputFilePath.substring(0, tempOutputFilePath.lastIndexOf('.'));
      if (streamingAction != null) {
        String mimeType = streamingAction.getMimeType(null);
        if (MIME_TYPE_HTML.equals(mimeType)) {
          extension = "html"; //$NON-NLS-1$
        } else if (MIME_TYPE_XLS.equals(mimeType)) {
          extension = "xls"; //$NON-NLS-1$
        } else if (MIME_TYPE_XLSX.equals(mimeType)) {
          extension = "xlsx"; //$NON-NLS-1$
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
    
    RepositoryFileOutputStream outputStream = new RepositoryFileOutputStream(tempOutputFilePath, true, true);
    outputStream.addListener(this);
    return outputStream;
  }
  
  public void fileCreated(String filePath) {
    IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);
    RepositoryFile outputFile = repository.getFile(filePath);
    if (outputFile != null) {
      Map<String, Serializable> fileMetadata = repository.getFileMetadata(outputFile.getId());
      RepositoryFile inputFile = repository.getFile(inputFilePath);
      if (inputFile != null) {
        fileMetadata.put(PentahoJcrConstants.PHO_CONTENTCREATOR, inputFile.getId());
        repository.setFileMetadata(outputFile.getId(), fileMetadata);
      }
    }
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

  public String toString() {
    // TODO Auto-generated method stub
    return "input file = " + inputFilePath + ":" + "outputFile = " + outputFilePath;
  }
}
