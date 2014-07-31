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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.NameBaseMimeResolver;
import org.pentaho.platform.plugin.services.importexport.BaseExportProcessor;
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.SimpleExportProcessor;
import org.pentaho.platform.plugin.services.importexport.ZipExportProcessor;
import org.pentaho.platform.repository.RepositoryDownloadWhitelist;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAdapter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;
import org.pentaho.platform.web.http.messages.Messages;

public class FileService {

  private static final Integer MODE_OVERWRITE = 1;

  private static final Integer MODE_RENAME = 2;

  private static final Integer MODE_NO_OVERWRITE = 3;

  private static final Log logger = LogFactory.getLog( FileService.class );

  protected IAuthorizationPolicy policy;

  protected DefaultUnifiedRepositoryWebService defaultUnifiedRepositoryWebService;

  protected IUnifiedRepository repository;

  protected RepositoryDownloadWhitelist whitelist;

  /**
   * Moves the list of files to the user's trash folder
   * <p/>
   * Move a list of files to the user's trash folder, the list should be comma separated.
   *
   * @param params Comma separated list of the files to be deleted
   * @throws Exception containing the string, "SystemResource.GENERAL_ERROR"
   */
  public void doDeleteFiles( String params ) throws Exception {
    String[] sourceFileIds = params.split( "[,]" );
    try {
      for ( int i = 0; i < sourceFileIds.length; i++ ) {
        getRepoWs().deleteFile( sourceFileIds[i], null );
      }
    } catch ( Exception e ) {
      throw e;
    }
  }

  /**
   * Permanently deletes the selected list of files from the repository
   * <p/>
   * Permanently deletes a comma separated list of files without sending them to the trash folder
   *
   * @param params Comma separated list of the files to be deleted
   * @return Exception containing the string, "SystemResource.GENERAL_ERROR"
   */
  public void doDeleteFilesPermanent( String params ) throws Exception {
    String[] sourceFileIds = params.split( "[,]" ); //$NON-NLS-1$
    try {
      for ( int i = 0; i < sourceFileIds.length; i++ ) {
        getRepoWs().deleteFileWithPermanentFlag( sourceFileIds[ i ], true, null );
      }
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      throw e;
    }
  }
  
  /**
   * Delete the locale for the selected file and locale
   *
   * @param pathId Colon separated path for the repository file
   * @param locale The locale to be deleted
   * @throws Exception containing the string, "SystemResource.GENERAL_ERROR"
   */
  public void doDeleteLocale(String pathId, String locale) throws Exception {
    try {
      RepositoryFileDto file = getRepoWs().getFile( FileUtils.idToPath( pathId ) );
      getRepoWs().deleteLocalePropertiesForFile( file.getId(), locale );
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      throw e;
    }
  }

  /**
   * Creates a new file with the provided contents at a given path
   *
   * @param pathId       (colon separated path for the repository file)
   * @param fileContents (content of the file)
   * @return
   * @throws IOException
   */
  public void createFile( HttpServletRequest httpServletRequest, String pathId, InputStream fileContents )
      throws Exception {
    try {
      String idToPath = idToPath( pathId );
      RepositoryFileOutputStream rfos = getRepositoryFileOutputStream( idToPath );
      rfos.setCharsetName( httpServletRequest.getCharacterEncoding() );
      copy( fileContents, rfos );
      rfos.close();
      fileContents.close();
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      throw e;
    }
  }

  /**
   * Moves a list of files from its current location to another.
   * <p/>
   * Moves a list of files from its current location to another, the list should be comma separated.
   *
   * @param destPathId Destiny path where files should be moved
   * @param params     Comma separated list of files to be moved
   * @return boolean <code>true</code>  if all files were moved correctly or <code>false</code> if the destiny path is
   * not available
   * @throws Exception containing the string, "SystemResource.GENERAL_ERROR"
   */
  public boolean doMoveFiles( String destPathId, String params ) throws Exception {
    String idToPath = idToPath( destPathId );
    RepositoryFileDto repositoryFileDto = getRepoWs().getFile( idToPath );
    if ( repositoryFileDto == null ) {
      return false;
    }
    String[] sourceFileIds = params.split( "[,]" );
    try {
      for ( int i = 0; i < sourceFileIds.length; i++ ) {
        getRepoWs().moveFile( sourceFileIds[i], repositoryFileDto.getPath(), null );
      }
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      throw e;
    }
    return true;
  }

  /**
   * Restores a list of files from the user's trash folder
   * <p/>
   * Restores a list of files from the user's trash folder to their previous locations. The list should be comma
   * separated.
   *
   * @param params Comma separated list of files to be restored
   * @throws Exception containing the string, "SystemResource.GENERAL_ERROR"
   */
  public void doRestoreFiles( String params ) throws Exception {
    String[] sourceFileIds = params.split( "[,]" );
    try {
      for ( int i = 0; i < sourceFileIds.length; i++ ) {
        getRepoWs().undeleteFile( sourceFileIds[i], null );
      }
    } catch ( Exception e ) {
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
    if ( !getPolicy().isAllowed( PublishAction.NAME ) ) {
      throw new GeneralSecurityException();
    }

    String originalFileName, encodedFileName = null;

    // change file id to path
    String path = FileUtils.idToPath( pathId );

    // if no path is sent, return bad request
    if ( StringUtils.isEmpty( pathId ) ) {
      throw new InvalidParameterException( pathId );
    }

    // check if path is valid
    if ( !isPathValid( path ) ) {
      throw new InvalidPathException( path, null );
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

  /**
   * Retrieves the file from the repository as inline. This is mainly used for css or and dependent files for the html
   * document
   *
   * @param pathId (colon separated path for the repository file)
   * @return RepositoryFileToStreamWrapper
   * @throws FileNotFoundException
   */
  // have to accept anything for browsers to work
  public RepositoryFileToStreamWrapper doGetFileAsInline( String pathId ) throws FileNotFoundException {
    String path = null;
    RepositoryFile repositoryFile = null;
    // Check if the path is actually and ID
    if ( isPath( pathId ) ) {
      path = pathId;
      if ( !isPathValid( path ) ) {
        throw new IllegalArgumentException();
      }
      repositoryFile = getRepository().getFile( path );
    } else {
      // Yes path provided is an ID
      repositoryFile = getRepository().getFileById( pathId );
    }

    if ( repositoryFile == null ) {
      // file does not exist or is not readable but we can't tell at this point
      throw new FileNotFoundException();
    }

    // check whitelist acceptance of file (based on extension)
    if ( !getWhitelist().accept( repositoryFile.getName() ) ) {
      // if whitelist check fails, we can still inline if you have PublishAction, otherwise we're FORBIDDEN
      if ( !getPolicy().isAllowed( PublishAction.NAME ) ) {
        throw new IllegalArgumentException();
      }
    }

    try {
      SimpleRepositoryFileData fileData =
          getRepository().getDataForRead( repositoryFile.getId(), SimpleRepositoryFileData.class );
      final InputStream is = fileData.getInputStream();

      // copy streaming output
      StreamingOutput streamingOutput = getStreamingOutput( is );

      RepositoryFileToStreamWrapper wrapper = new RepositoryFileToStreamWrapper();
      wrapper.setOutputStream( streamingOutput );
      wrapper.setRepositoryFile( repositoryFile );

      return wrapper;
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString(
          "FileResource.EXPORT_FAILED", repositoryFile.getName() + " " + e.getMessage() ), e );
      throw new InternalError();
    }
  }

  /**
   * Copy files to a new location
   *
   * @param pathId
   * @param mode
   * @param params
   */
  public void doCopyFiles( String pathId, Integer mode, String params ) {
    if ( !getPolicy().isAllowed( RepositoryCreateAction.NAME ) ) {
      throw new IllegalArgumentException();
    }

    if ( mode == null ) {
      mode = MODE_RENAME;
    }

    String path = FileUtils.idToPath( pathId );
    RepositoryFile destDir = getRepository().getFile( path );
    String[] sourceFileIds = params.split( "[,]" ); //$NON-NLS-1$
    if ( mode == MODE_OVERWRITE || mode == MODE_NO_OVERWRITE ) {
      for ( String sourceFileId : sourceFileIds ) {
        RepositoryFile sourceFile = getRepository().getFileById( sourceFileId );
        if ( destDir != null && destDir.isFolder() && sourceFile != null && !sourceFile.isFolder() ) {
          String fileName = sourceFile.getName();
          String
              sourcePath =
              sourceFile.getPath().substring( 0, sourceFile.getPath().lastIndexOf( FileUtils.PATH_SEPARATOR ) );
          if ( !sourcePath.equals( destDir.getPath() ) ) { // We're saving to a different folder than we're copying
            // from
            IRepositoryFileData data = getData( sourceFile );
            RepositoryFileAcl acl = getRepository().getAcl( sourceFileId );
            RepositoryFile
                destFile =
                getRepository().getFile( destDir.getPath() + FileUtils.PATH_SEPARATOR + fileName );
            if ( destFile == null ) { // destFile doesn't exist so we'll create it.
              RepositoryFile duplicateFile =
                  new RepositoryFile.Builder( fileName ).hidden( sourceFile.isHidden() ).versioned(
                      sourceFile.isVersioned() ).build();
              final RepositoryFile repositoryFile =
                  getRepository().createFile( destDir.getId(), duplicateFile, data, acl, null );
              getRepository()
                  .setFileMetadata( repositoryFile.getId(), getRepository().getFileMetadata( sourceFileId ) );
            } else if ( mode == MODE_OVERWRITE ) { // destFile exists so check to see if we want to overwrite it.
              RepositoryFileDto destFileDto = RepositoryFileAdapter.toFileDto( destFile, null, false );
              destFileDto.setHidden( sourceFile.isHidden() );
              destFile = RepositoryFileAdapter.toFile( destFileDto );
              final RepositoryFile repositoryFile = getRepository().updateFile( destFile, data, null );
              getRepository().updateAcl( acl );
              getRepository()
                  .setFileMetadata( repositoryFile.getId(), getRepository().getFileMetadata( sourceFileId ) );
            }
          }
        }
      }
    } else {
      for ( String sourceFileId : sourceFileIds ) {
        RepositoryFile sourceFile = getRepository().getFileById( sourceFileId );
        if ( destDir != null && destDir.isFolder() && sourceFile != null && !sourceFile.isFolder() ) {

          // First try to see if regular name is available
          String fileName = sourceFile.getName();
          String copyText = "";
          String rootCopyText = "";
          String nameNoExtension = fileName;
          String extension = "";
          int indexOfDot = fileName.lastIndexOf( '.' );
          if ( !( indexOfDot == -1 ) ) {
            nameNoExtension = fileName.substring( 0, indexOfDot );
            extension = fileName.substring( indexOfDot );
          }

          RepositoryFileDto
              testFile =
              getRepoWs().getFile( path + FileUtils.PATH_SEPARATOR + nameNoExtension + extension ); //$NON-NLS-1$
          if ( testFile != null ) {
            // Second try COPY_PREFIX, If the name already ends with a COPY_PREFIX don't append twice
            if ( !nameNoExtension
                .endsWith( Messages.getInstance().getString( "FileResource.COPY_PREFIX" ) ) ) { //$NON-NLS-1$
              copyText = rootCopyText = Messages.getInstance().getString( "FileResource.COPY_PREFIX" );
              fileName = nameNoExtension + copyText + extension;
              testFile = getRepoWs().getFile( path + FileUtils.PATH_SEPARATOR + fileName );
            }
          }

          // Third try COPY_PREFIX + DUPLICATE_INDICATOR
          Integer nameCount = 1;
          while ( testFile != null ) {
            nameCount++;
            copyText =
                rootCopyText + Messages.getInstance().getString( "FileResource.DUPLICATE_INDICATOR", nameCount );
            fileName = nameNoExtension + copyText + extension;
            testFile = getRepoWs().getFile( path + FileUtils.PATH_SEPARATOR + fileName );
          }
          IRepositoryFileData data = getData( sourceFile );
          RepositoryFileAcl acl = getRepository().getAcl( sourceFileId );
          RepositoryFile duplicateFile = null;

          // If the title is different than the source file, copy it separately
          if ( !sourceFile.getName().equals( sourceFile.getTitle() ) ) {
            duplicateFile =
                new RepositoryFile.Builder( fileName ).title( RepositoryFile.DEFAULT_LOCALE,
                    sourceFile.getTitle() + copyText ).hidden( sourceFile.isHidden() ).versioned(
                    sourceFile.isVersioned() ).build();
          } else {
            duplicateFile = new RepositoryFile.Builder( fileName ).hidden( sourceFile.isHidden() ).build();
          }

          final RepositoryFile repositoryFile =
              getRepository().createFile( destDir.getId(), duplicateFile, data, acl, null );
          getRepository().setFileMetadata( repositoryFile.getId(), getRepository().getFileMetadata( sourceFileId ) );
        }
      }
    }
  }

  /**
   * Takes a pathId and returns a response object with the output stream based on the file located at the pathID
   *
   * @param pathId pathId to the file
   * @return Response object containing the file stream for the file located at the pathId, along with the mimetype,
   * and file name.
   * @throws FileNotFoundException, IllegalArgumentException
   */
  public RepositoryFileToStreamWrapper doGetFileOrDir( String pathId ) throws FileNotFoundException {

    String path = idToPath( pathId );

    if ( !isPathValid( path ) ) {
      throw new IllegalArgumentException();
    }

    RepositoryFile repoFile = getRepository().getFile( path );

    if ( repoFile == null ) {
      // file does not exist or is not readable but we can't tell at this point
      throw new FileNotFoundException();
    }

    // check whitelist acceptance of file (based on extension)
    if ( !getWhitelist().accept( repoFile.getName() ) ) {
      // if whitelist check fails, we can still inline if you have PublishAction, otherwise we're FORBIDDEN
      if ( !getPolicy().isAllowed( PublishAction.NAME ) ) {
        throw new IllegalArgumentException();
      }
    }

    final RepositoryFileInputStream is = getRepositoryFileInputStream( repoFile );
    StreamingOutput streamingOutput = getStreamingOutput( is );

    RepositoryFileToStreamWrapper wrapper = new RepositoryFileToStreamWrapper();
    wrapper.setOutputStream( streamingOutput );
    wrapper.setRepositoryFile( repoFile );
    wrapper.setMimetype( is.getMimeType() );

    return wrapper;
  }

  private IRepositoryFileData getData( RepositoryFile repositoryFile ) {
    IRepositoryContentConverterHandler converterHandler;
    Map<String, Converter> converters;
    NameBaseMimeResolver mimeResolver;

    IRepositoryFileData repositoryFileData = null;

    if ( !repositoryFile.isFolder() ) {
      // Get the extension
      final String ext = RepositoryFilenameUtils.getExtension( repositoryFile.getName() );
      if ( ( ext == null ) || ( ext.isEmpty() ) ) {
        return null;
      }

      // Find the converter

      // If we have not been given a handler, try PentahoSystem
      converterHandler = PentahoSystem.get( IRepositoryContentConverterHandler.class );

      // fail if we have no converter handler
      if ( converterHandler == null ) {
        return null;
      }

      converters = converterHandler.getConverters();

      final Converter converter = converters.get( ext );
      if ( converter == null ) {
        return null;
      }

      // Check the mime type
      mimeResolver = PentahoSystem.get( NameBaseMimeResolver.class );

      // fail if we have no mime resolver
      if ( mimeResolver == null ) {
        return null;
      }

      final String mimeType = mimeResolver.resolveMimeTypeForFileName( repositoryFile.getName() ).getName();
      if ( ( mimeType == null ) || ( mimeType.isEmpty() ) ) {
        return null;
      }

      // Get the input stream
      InputStream inputStream = converter.convert( repositoryFile.getId() );
      if ( inputStream == null ) {
        return null;
      }

      // Get the file data
      repositoryFileData = converter.convert( inputStream, "UTF-8", mimeType );
      if ( repositoryFileData == null ) {
        return null;
      }
    }

    return repositoryFileData;
  }

  public void setFileAcls( String pathId, RepositoryFileAclDto acl ) throws FileNotFoundException {
    RepositoryFileDto file = getRepoWs().getFile( FileUtils.idToPath( pathId ) );
    if ( file == null ) {
      // file does not exist or is not readable but we can't tell at this point
      throw new FileNotFoundException();
    }

    acl.setId( file.getId() );
    // here we remove fake admin role added for display purpose only
    List<RepositoryFileAclAceDto> aces = acl.getAces();
    if ( aces != null ) {
      Iterator<RepositoryFileAclAceDto> it = aces.iterator();
      while ( it.hasNext() ) {
        RepositoryFileAclAceDto ace = it.next();
        if ( !ace.isModifiable() ) {
          it.remove();
        }
      }
    }
    getRepoWs().updateAcl( acl );
  }

  protected RepositoryDownloadWhitelist getWhitelist() {
    if ( whitelist == null ) {
      whitelist = new RepositoryDownloadWhitelist();
    }
    return whitelist;
  }

  protected boolean isPathValid( String path ) {
    if ( path.startsWith( "/etc" ) || path.startsWith( "/system" ) ) {
      return false;
    }
    return true;
  }

  protected boolean isPath( String pathId ) {
    return pathId != null && pathId.contains( "/" );
  }

  public IAuthorizationPolicy getPolicy() {
    if ( policy == null ) {
      policy = PentahoSystem.get( IAuthorizationPolicy.class );
    }
    return policy;
  }

  protected DefaultUnifiedRepositoryWebService getRepoWs() {
    if ( defaultUnifiedRepositoryWebService == null ) {
      defaultUnifiedRepositoryWebService = new DefaultUnifiedRepositoryWebService();
    }
    return defaultUnifiedRepositoryWebService;
  }

  public int copy( InputStream input, OutputStream output ) throws IOException {
    return IOUtils.copy( input, output );
  }

  public RepositoryFileOutputStream getRepositoryFileOutputStream( String path ) {
    return new RepositoryFileOutputStream( path );
  }

  public RepositoryFileInputStream getRepositoryFileInputStream( RepositoryFile repositoryFile )
      throws FileNotFoundException {
    return new RepositoryFileInputStream( repositoryFile );
  }

  public StreamingOutput getStreamingOutput( final InputStream is ) {
    return new StreamingOutput() {
      public void write( OutputStream output ) throws IOException {
        copy( is, output );
      }
    };
  }

  public IUnifiedRepository getRepository() {
    if ( repository == null ) {
      repository = PentahoSystem.get( IUnifiedRepository.class );
    }
    return repository;
  }

  protected String idToPath( String pathId ) {
    return FileUtils.idToPath( pathId );
  }

  public class RepositoryFileToStreamWrapper {
    private StreamingOutput outputStream;
    private RepositoryFile repositoryFile;
    private String mimetype;

    public void setOutputStream( StreamingOutput outputStream ) {
      this.outputStream = outputStream;
    }

    public void setRepositoryFile( RepositoryFile repositoryFile ) {
      this.repositoryFile = repositoryFile;
    }

    public void setMimetype( String mimetype ) {
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
}
