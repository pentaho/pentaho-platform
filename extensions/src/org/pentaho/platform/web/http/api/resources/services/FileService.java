package org.pentaho.platform.web.http.api.resources.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.InvalidPathException;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.BaseExportProcessor;
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.SimpleExportProcessor;
import org.pentaho.platform.plugin.services.importexport.ZipExportProcessor;
import org.pentaho.platform.repository.RepositoryDownloadWhitelist;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;
import org.pentaho.platform.web.http.messages.Messages;

public class FileService {

  private static final Log logger = LogFactory.getLog( FileService.class );

  protected IAuthorizationPolicy policy;

  protected DefaultUnifiedRepositoryWebService defaultUnifiedRepositoryWebService;

  protected RepositoryFileOutputStream mockRepositoryFileOutputStream;

  protected IUnifiedRepository repository;

  protected RepositoryDownloadWhitelist whitelist;

  /**
   * Moves the list of files to the user's trash folder
   *
   * Move a list of files to the user's trash folder, the list should be comma separated.
   *
   * @param params Comma separated list of the files to be deleted
   *
   * @throws Exception containing the string, "SystemResource.GENERAL_ERROR"
   */
  public void doDeleteFiles( String params ) throws Exception {
    String[] sourceFileIds = params.split( "[,]" );
    try {
      for ( int i = 0; i < sourceFileIds.length; i++ ) {
        getRepoWs().deleteFile( sourceFileIds[ i ], null );
      }
    } catch ( Exception e ) {
      throw e;
    }
  }

  /**
   * Permanently deletes the selected list of files from the repository
   *
   * Permanently deletes a comma separated list of files without sending them to the trash folder
   *
   * @param params Comma separated list of the files to be deleted
   *
   * @return Exception containing the string, "SystemResource.GENERAL_ERROR"
   */
  public void doDeleteFilesPermanent( String params ) throws Exception {
    String[] sourceFileIds = params.split( "[,]" ); //$NON-NLS-1$
    try {
      for ( int i = 0; i < sourceFileIds.length; i++ ) {
        getRepoWs().deleteFileWithPermanentFlag( sourceFileIds[i], true, null );
      }
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      throw e;
    }
  }

  /**
   * Creates a new file with the provided contents at a given path
   *
   * @param pathId
   *          (colon separated path for the repository file)
   * @param fileContents
   *          (content of the file)
   * @return
   * @throws IOException
   */
  public void createFile (HttpServletRequest httpServletRequest, String pathId, InputStream fileContents) throws Exception {
    try {
      String idToPath = FileUtils.idToPath( pathId );
      RepositoryFileOutputStream rfos = getRepositoryFileOutputStream( idToPath );
      rfos.setCharsetName(httpServletRequest.getCharacterEncoding());
      copy( fileContents, rfos );
      rfos.close();
      fileContents.close();
    } catch (Exception e) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      throw e;
    }
  }

  /**
   * Moves a list of files from its current location to another.
   *
   * Moves a list of files from its current location to another, the list should be comma separated.
   *
   * @param destPathId Destiny path where files should be moved
   * @param params Comma separated list of files to be moved
   *
   * @return boolean <code>true</code>  if all files were moved correctly or <code>false</code> if the destiny path is
   * not available
   *
   * @throws Exception containing the string, "SystemResource.GENERAL_ERROR"
   */
  public boolean doMoveFiles( String destPathId, String params ) throws Exception {
    String idToPath = FileUtils.idToPath( destPathId );
    RepositoryFileDto repositoryFileDto = getRepoWs().getFile( idToPath );
    if ( repositoryFileDto == null ) {
      return false;
    }
    String[] sourceFileIds = params.split( "[,]" );
    try {
      for ( int i = 0; i < sourceFileIds.length; i++ ) {
        getRepoWs().moveFile( sourceFileIds[ i ], repositoryFileDto.getPath(), null );
      }
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      throw e;
    }
    return true;
  }

  /**
   * Restores a list of files from the user's trash folder
   *
   * Restores a list of files from the user's trash folder to their previous locations. The list should be comma
   * separated.
   *
   * @param params Comma separated list of files to be restored
   *
   * @throws Exception containing the string, "SystemResource.GENERAL_ERROR"
   */
  public void doRestoreFiles( String params ) throws Exception {
    String[] sourceFileIds = params.split( "[,]" );
    try {
      for ( int i = 0; i < sourceFileIds.length; i++ ) {
        getRepoWs().undeleteFile( sourceFileIds[i], null );
      }
    } catch ( Exception e ){
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      throw e;
    }
  }

  public class DownloadFileWrapper {
    private StreamingOutput outputStream;
    private String attachment;
    private String encodedFileName;
    
    public DownloadFileWrapper( StreamingOutput outputStream, String attachment, String encodedFileName ) {
      super();
      this.outputStream = outputStream;
      this.attachment = attachment;
      this.encodedFileName = encodedFileName;
    }
    
    public StreamingOutput getOutputStream() {
      return outputStream;
    }
    public String getAttachment() {
      return attachment;
    }
    public String getEncodedFileName() {
      return encodedFileName;
    }
  }
  
  public DownloadFileWrapper doGetFileOrDirAsDownload( String userAgent, String pathId, String strWithManifest )
    throws Throwable {

    // you have to have PublishAction in order to download
    if ( getPolicy().isAllowed( PublishAction.NAME ) == false ) {
      throw new GeneralSecurityException();
    }

    String originalFileName, encodedFileName = null;

    // change file id to path
    String path = FileUtils.idToPath( pathId );

    // if no path is sent, return bad request
    if ( StringUtils.isEmpty( pathId ) ) {
      throw new InvalidParameterException(pathId);
    }

    // check if path is valid
    if ( !isPathValid( path ) ) {
      throw new InvalidPathException(path, null);
    }

    // check if entity exists in repo
    RepositoryFile repositoryFile = getRepository().getFile( path );

    if ( repositoryFile == null ) {
      // file does not exist or is not readable but we can't tell at this point
      throw new FileNotFoundException( path );
    }

    final InputStream is;
    StreamingOutput streamingOutput;
    BaseExportProcessor exportProcessor;

    // create processor
    // send zip with manifest by default
    boolean withManifest = "false".equals( strWithManifest ) ? false : true;
    if ( repositoryFile.isFolder() || withManifest ) {
      exportProcessor = new ZipExportProcessor( path, repository, withManifest );
      originalFileName = repositoryFile.getName() + ".zip"; //$NON-NLS-1$//$NON-NLS-2$
    } else {
      exportProcessor = new SimpleExportProcessor( path, repository );
      originalFileName = repositoryFile.getName(); //$NON-NLS-1$//$NON-NLS-2$
    }
    encodedFileName = URLEncoder.encode( originalFileName, "UTF-8" ).replaceAll( "\\+", "%20" );
    String quotedFileName = "\"" + originalFileName + "\"";

    // add export handlers for each expected file type
    exportProcessor.addExportHandler( PentahoSystem.get( DefaultExportHandler.class ) );

    File zipFile = exportProcessor.performExport( repositoryFile );
    is = new FileInputStream( zipFile );

    // copy streaming output
    streamingOutput = new StreamingOutput() {
      public void write( OutputStream output ) throws IOException {
        IOUtils.copy( is, output );
      }
    };

    final String attachment;
    if ( userAgent.contains( "Firefox" ) ) {
      // special content-disposition for firefox browser to support utf8-encoded symbols in filename
      attachment = "attachment; filename*=UTF-8\'\'" + encodedFileName;
    } else {
      attachment = "attachment; filename=" + quotedFileName;
    }

    return new DownloadFileWrapper( streamingOutput, attachment, encodedFileName );
  }

  public class RepositoryFileToStreamWrapper {
    private StreamingOutput outputStream;
    private RepositoryFile repositoryFile;
    private String mimetype;

    public RepositoryFileToStreamWrapper( StreamingOutput outputStream, RepositoryFile repositoryFile, String mimetype ) {
      this.outputStream = outputStream;
      this.repositoryFile = repositoryFile;
      this.mimetype = mimetype;
    }

    public StreamingOutput getOutputStream() {
      return outputStream;
    }

    public String getMimetype() {
      return mimetype;
    }

    public RepositoryFile getRepositoryFile() {
      return repositoryFile;
    }
  }
  public RepositoryFileToStreamWrapper doGetFileOrDir( String pathId ) throws FileNotFoundException {

    String path = FileUtils.idToPath( pathId );

    if ( !isPathValid( path ) ) {
      IllegalArgumentException illegalArgument = new IllegalArgumentException();
      throw illegalArgument;
    }

    RepositoryFile repoFile = getRepository().getFile( path );

    if ( repoFile == null ) {
      // file does not exist or is not readable but we can't tell at this point
      FileNotFoundException fileNotFound = new FileNotFoundException();
      throw fileNotFound;
    }

    // check whitelist acceptance of file (based on extension)
    if ( !getWhitelist().accept( repoFile.getName() ) ) {
      // if whitelist check fails, we can still inline if you have PublishAction, otherwise we're FORBIDDEN
      if ( !getPolicy().isAllowed( PublishAction.NAME ) ) {
        IllegalArgumentException illegalArgument = new IllegalArgumentException();
        throw illegalArgument;
      }
    }

    final RepositoryFileInputStream is = new RepositoryFileInputStream( repoFile );
    StreamingOutput streamingOutput = new StreamingOutput() {
      public void write( OutputStream output ) throws IOException {
        copy( is, output );
      }
    };

    return new RepositoryFileToStreamWrapper( streamingOutput, repoFile, is.getMimeType());
  }

  private RepositoryDownloadWhitelist getWhitelist() {
    if ( whitelist == null ) {
      whitelist = new RepositoryDownloadWhitelist();
    }
    return whitelist;
  }

  /**
   * Validate path and send appropriate response if necessary TODO: Add validation to IUnifiedRepository interface
   *
   * @param path
   * @return
   */
  private boolean isPathValid( String path ) {
    if ( path.startsWith( "/etc" ) || path.startsWith( "/system" ) ) {
      return false;
    }
    return true;
  }

  public IAuthorizationPolicy getPolicy() {
    if ( policy == null ) {
      policy = PentahoSystem.get( IAuthorizationPolicy.class );
    }
    return policy;
  }

  protected DefaultUnifiedRepositoryWebService getRepoWs() {
    return getDefaultUnifiedRepositoryWebService();
  }

  public int copy(InputStream input, OutputStream output) throws IOException {
    return IOUtils.copy( input, output );
  }

  public DefaultUnifiedRepositoryWebService getDefaultUnifiedRepositoryWebService() {
    if ( defaultUnifiedRepositoryWebService == null ) {
      defaultUnifiedRepositoryWebService = new DefaultUnifiedRepositoryWebService();
    }
    return defaultUnifiedRepositoryWebService;
  }

  public RepositoryFileOutputStream getRepositoryFileOutputStream( String path ) {
    if ( mockRepositoryFileOutputStream != null ) {
      return mockRepositoryFileOutputStream;
    }

    return new RepositoryFileOutputStream( path );
  }

  public IUnifiedRepository getRepository() {
    if ( repository == null ) {
      repository = PentahoSystem.get( IUnifiedRepository.class );
    }
    return repository;
  }

}
