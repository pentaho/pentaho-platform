package org.pentaho.platform.web.http.api.resources.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.IllegalSelectorException;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.NameBaseMimeResolver;
import org.pentaho.platform.plugin.services.importexport.BaseExportProcessor;
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.ExportException;
import org.pentaho.platform.plugin.services.importexport.ExportHandler;
import org.pentaho.platform.plugin.services.importexport.SimpleExportProcessor;
import org.pentaho.platform.plugin.services.importexport.ZipExportProcessor;
import org.pentaho.platform.repository.RepositoryDownloadWhitelist;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.locale.PentahoLocale;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.LocaleMapDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAdapter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.platform.repository2.unified.webservices.StringKeyStringValueDto;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.web.http.api.resources.Setting;
import org.pentaho.platform.web.http.api.resources.StringListWrapper;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.web.http.api.resources.SessionResource;
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

  protected SessionResource sessionResource;

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
  public void doDeleteLocale( String pathId, String locale ) throws Exception {
    try {
      RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
      if ( file != null ) {
        getRepoWs().deleteLocalePropertiesForFile( file.getId(), locale );
      }
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      throw e;
    }
  }

  public List<Setting> doGetCanAccessList( String pathId, String permissions ) {
    StringTokenizer tokenizer = new StringTokenizer( permissions, "|" );
    ArrayList<Setting> permMap = new ArrayList<Setting>();
    while ( tokenizer.hasMoreTokens() ) {
      Integer perm = Integer.valueOf( tokenizer.nextToken() );
      EnumSet<RepositoryFilePermission> permission = EnumSet.of( RepositoryFilePermission.values()[perm] );
      permMap.add( new Setting( perm.toString(), new Boolean( getRepository()
          .hasAccess( idToPath( pathId ), permission ) ).toString() ) );
    }
    return permMap;
  }

  public List<Setting> doGetPathsAccessList( StringListWrapper pathsWrapper ) {
    List<Setting> pathsPermissonsSettings = new ArrayList<Setting>();

    String permissions =
        RepositoryFilePermission.READ.ordinal() + "|" + RepositoryFilePermission.WRITE.ordinal() + "|"
            + RepositoryFilePermission.DELETE.ordinal() + "|" + RepositoryFilePermission.ACL_MANAGEMENT.ordinal() + "|"
            + RepositoryFilePermission.ALL.ordinal();

    List<String> paths = pathsWrapper.getStrings();
    for ( String path : paths ) {
      List<Setting> permList = doGetCanAccessList( path, permissions );
      for ( Setting perm : permList ) {
        if ( Boolean.parseBoolean( perm.getValue() ) ) {
          Setting setting = new Setting();
          setting.setName( path );
          setting.setValue( perm.getName() );
          pathsPermissonsSettings.add( setting );
        }
      }
    }
    return pathsPermissonsSettings;
  }

  /**
   * Creates a new file with the provided contents at a given path
   *
   * @param pathId       (colon separated path for the repository file)
   * @param fileContents (content of the file)
   * @return
   * @throws IOException
   */
  public void createFile( String charsetName, String pathId, InputStream fileContents )
    throws Exception {
    try {
      String idToPath = idToPath( pathId );
      RepositoryFileOutputStream rfos = getRepositoryFileOutputStream( idToPath );
      rfos.setCharsetName( charsetName );
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
   * @param destPathId colon separated path for the repository file
   * <pre function="syntax.xml">
   *    :path:to:file:id
   * </pre>
   * @param params comma separated list of files to be moved
   * <pre function="syntax.xml">
   *    path1,path2,...
   * </pre>
   *
   * @return boolean <code>true</code>  if all files were moved correctly or <code>false</code> if the destiny path is
   * not available
   * @throws FileNotFoundException
   */
  public void doMoveFiles( String destPathId, String params ) throws FileNotFoundException {
    String idToPath = idToPath( destPathId );
    RepositoryFileDto repositoryFileDto = getRepoWs().getFile( idToPath );
    if ( repositoryFileDto == null ) {
      throw new FileNotFoundException( idToPath );
    }
    String[] sourceFileIds = params.split( "[,]" );
    int i = 0;
    try {
      for ( ; i < sourceFileIds.length; i++ ) {
        getRepoWs().moveFile( sourceFileIds[ i ], repositoryFileDto.getPath(), null );
      }
    } catch ( IllegalArgumentException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new InternalError();
    }
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
  public void doRestoreFiles( String params ) throws InternalError {
    String[] sourceFileIds = params.split( "[,]" );
    try {
      for ( int i = 0; i < sourceFileIds.length; i++ ) {
        getRepoWs().undeleteFile( sourceFileIds[i], null );
      }
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.FILE_RESTORE_FAILED" ), e );
      throw new InternalError();
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
    String path = idToPath( pathId );

    // if no path is sent, return bad request
    if ( StringUtils.isEmpty( pathId ) ) {
      throw new InvalidParameterException( pathId );
    }

    // check if path is valid
    if ( !isPathValid( path ) ) {
      throw new IllegalSelectorException();
    }

    // check if entity exists in repo
    RepositoryFile repositoryFile = getRepository().getFile( path );

    if ( repositoryFile == null ) {
      // file does not exist or is not readable but we can't tell at this point
      throw new FileNotFoundException( path );
    }

   // send zip with manifest by default
    boolean withManifest = "false".equals( strWithManifest ) ? false : true;
    boolean requiresZip = repositoryFile.isFolder() || withManifest;
    BaseExportProcessor exportProcessor = getDownloadExportProcessor( path, requiresZip, withManifest );
    originalFileName = requiresZip ? repositoryFile.getName() + ".zip" : repositoryFile.getName(); //$NON-NLS-1$//$NON-NLS-2$
    encodedFileName = URLEncoder.encode( originalFileName, "UTF-8" ).replaceAll( "\\+", "%20" );
    String quotedFileName = "\"" + originalFileName + "\"";

    // add export handlers for each expected file type
    exportProcessor.addExportHandler( getDownloadExportHandler() );

    // copy streaming output
    StreamingOutput streamingOutput = getDownloadStream( repositoryFile, exportProcessor );

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
   * Retrieve the list of locale properties for a given locale
   *
   * @param pathId (colon separated path for the repository file)
   * @param locale
   * @return
   */
  public List<StringKeyStringValueDto> doGetLocaleProperties( String pathId, String locale ) {
    RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
    List<StringKeyStringValueDto> keyValueList = new ArrayList<StringKeyStringValueDto>();
    if ( file != null ) {
      Properties properties = getRepoWs().getLocalePropertiesForFileById( file.getId(), locale );
      if ( properties != null && !properties.isEmpty() ) {
        for ( String key : properties.stringPropertyNames() ) {
          keyValueList.add( getStringKeyStringValueDto( key, properties.getProperty( key ) ) );
        }
      }
    }
    return keyValueList;
  }

  /**
   * Set the list of locale properties for a given locale
   *
   * @param pathId
   * @param locale
   * @param properties
   */
  public void doSetLocaleProperties( String pathId, String locale, List<StringKeyStringValueDto> properties )
    throws Exception {
    RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
    Properties fileProperties = new Properties();
    if ( properties != null && !properties.isEmpty() ) {
      for ( StringKeyStringValueDto dto : properties ) {
        fileProperties.put( dto.getKey(), dto.getValue() );
      }
    }
    getRepoWs().setLocalePropertiesForFileByFileId( file.getId(), locale, fileProperties );
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

    String path = idToPath( pathId );
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
              RepositoryFileDto destFileDto = toFileDto( destFile, null, false );
              destFileDto.setHidden( sourceFile.isHidden() );
              destFile = toFile( destFileDto );
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

  public IRepositoryFileData getData( RepositoryFile repositoryFile ) {
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

  /**
   * Save the acls of the selected file to the repository
   *
   * This method is used to update and save the acls of the selected file to the repository
   *
   * @param pathId @param pathId colon separated path for the repository file
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @param acl    Acl of the repository file <code> RepositoryFileAclDto </code>
   * @throws FileNotFoundException
   */
  public void setFileAcls( String pathId, RepositoryFileAclDto acl ) throws FileNotFoundException {
    RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
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

  /**
   * Check whether the current user has specific permission on the selected repository file
   *
   * @param pathId
   * @param permissions
   * @return
   */
  public String doGetCanAccess( String pathId, String permissions ) {
    StringTokenizer tokenizer = new StringTokenizer( permissions, "|" );
    List<Integer> permissionList = new ArrayList<Integer>();
    while ( tokenizer.hasMoreTokens() ) {
      Integer perm = Integer.valueOf( tokenizer.nextToken() );
      switch ( perm ) {
        case 0: {
          permissionList.add( RepositoryFilePermission.READ.ordinal() );
          break;
        }
        case 1: {
          permissionList.add( RepositoryFilePermission.WRITE.ordinal() );
          break;
        }
        case 2: {
          permissionList.add( RepositoryFilePermission.DELETE.ordinal() );
          break;
        }
        case 3: {
          permissionList.add( RepositoryFilePermission.ACL_MANAGEMENT.ordinal() );
          break;
        }
        case 4: {
          permissionList.add( RepositoryFilePermission.ALL.ordinal() );
          break;
        }
      }
    }
    return getRepoWs().hasAccess( idToPath( pathId ), permissionList ) ? "true" : "false";
  }

  public StringBuffer doGetReservedChars() {
    List<Character> reservedCharacters = getRepoWs().getReservedChars();
    StringBuffer buffer = new StringBuffer();
    for ( int i = 0; i < reservedCharacters.size(); i++ ) {
      buffer.append( reservedCharacters.get( i ) );
    }
    return buffer;
  }

  public StringBuffer doGetReservedCharactersDisplay() {
    List<Character> reservedCharacters = getRepoWs().getReservedChars();
    StringBuffer buffer = new StringBuffer();
    for ( int i = 0; i < reservedCharacters.size(); i++ ) {
      if ( reservedCharacters.get( i ) >= 0x07 && reservedCharacters.get( i ) <= 0x0d ) {
        buffer.append( escapeJava( "" + reservedCharacters.get( i ) ) );
      } else {
        buffer.append( reservedCharacters.get( i ) );
      }
      if ( i + 1 < reservedCharacters.size() ) {
        buffer.append( ',' );
      }
    }
    return buffer;
  }

  /**
   * Retrieves the properties of the root directory
   *
   * @return file properties object <code> RepositoryFileDto </code> for the root directory
   */
  public RepositoryFileDto doGetRootProperties() {
    return getRepoWs().getFile( FileUtils.PATH_SEPARATOR );
  }

  /**
   * Retrieves the properties of a selected repository file
   *
   * @param pathId @param pathId colon separated path for the repository file
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @return file properties object <code> RepositoryFileDto </code>
   * @throws FileNotFoundException
   */
  public RepositoryFileDto doGetProperties( String pathId ) throws FileNotFoundException {
    RepositoryFileDto file = getRepoWs().getFile( FileUtils.idToPath( pathId ) );
    if ( file == null ) {
      throw new FileNotFoundException();
    }
    return file;
  }

  /**
   * Gets the permission for whether or not a user can create files
   *
   * @return Boolean representing whether or not user can create files
   */
  public String doGetCanCreate() {
    return getPolicy().isAllowed( RepositoryCreateAction.NAME ) ? "true" : "false"; //$NON-NLS-1$//$NON-NLS-2$
  }

  /**
   * Gets the content creator of the specified file
   *
   * @param pathId
   * @return
   */
  public RepositoryFileDto doGetContentCreator( String pathId ) throws FileNotFoundException {
    RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
    if ( file == null ) {
      throw new FileNotFoundException();
    }
    Map<String, Serializable> fileMetadata = getRepository().getFileMetadata( file.getId() );
    String creatorId = (String) fileMetadata.get( PentahoJcrConstants.PHO_CONTENTCREATOR );
    if ( creatorId != null && creatorId.length() > 0 ) {
      return getRepoWs().getFileById( creatorId );
    }

    return null;
  }

  /**
   * Get deleted files
   *
   * @return
   */
  public List<RepositoryFileDto> doGetDeletedFiles() {
    return getRepoWs().getDeletedFiles();
  }

  /**
   * Get metadata for a file by path id
   *
   * @param pathId
   * @return
   */
  public List<StringKeyStringValueDto> doGetMetadata( String pathId ) throws FileNotFoundException {
    List<StringKeyStringValueDto> list = null;
    String path = null;
    if ( pathId == null || pathId.equals( FileUtils.PATH_SEPARATOR ) ) {
      path = FileUtils.PATH_SEPARATOR;
    } else {
      if ( !pathId.startsWith( FileUtils.PATH_SEPARATOR ) ) {
        path = idToPath( pathId );
      }
    }
    final RepositoryFileDto file = getRepoWs().getFile( path );
    if ( file == null ) {
      throw new FileNotFoundException();
    }

    list = getRepoWs().getFileMetadata( file.getId() );

    if ( list != null ) {
      boolean hasSchedulable = false;
      for ( StringKeyStringValueDto value : list ) {
        if ( value.getKey().equals( "_PERM_SCHEDULABLE" ) ) {
          hasSchedulable = true;
          break;
        }
      }
      if ( !hasSchedulable ) {
        StringKeyStringValueDto schedPerm = new StringKeyStringValueDto( "_PERM_SCHEDULABLE", "true" );
        list.add( schedPerm );
      }

      // check file object for hidden value and add it to the list
      list.add( new StringKeyStringValueDto( "_PERM_HIDDEN", String.valueOf( file.isHidden() ) ) );
    }

    return list;
  }

  /**
   * Set the metadata on a file
   *
   * @param pathId
   * @param metadata
   * @throws GeneralSecurityException
   */
  public void doSetMetadata( String pathId, List<StringKeyStringValueDto> metadata ) throws GeneralSecurityException {
    RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
    RepositoryFileAclDto fileAcl = getRepoWs().getAcl( file.getId() );

    boolean canManage =
      getSession().getName().equals( fileAcl.getOwner() )
        || ( getPolicy().isAllowed( RepositoryReadAction.NAME )
        && getPolicy().isAllowed( RepositoryCreateAction.NAME ) && getPolicy().isAllowed(
          AdministerSecurityAction.NAME ) );

    if ( !canManage ) {

      if ( fileAcl.isEntriesInheriting() ) {
        List<RepositoryFileAclAceDto> aces = getRepoWs().getEffectiveAces( file.getId() );
        fileAcl.setAces( aces, fileAcl.isEntriesInheriting() );
      }

      for ( int i = 0; i < fileAcl.getAces().size(); i++ ) {
        RepositoryFileAclAceDto acl = fileAcl.getAces().get( i );
        if ( acl.getRecipient().equals( getSession().getName() ) ) {
          if ( acl.getPermissions().contains( RepositoryFilePermission.ACL_MANAGEMENT.ordinal() )
            || acl.getPermissions().contains( RepositoryFilePermission.ALL.ordinal() ) ) {
            canManage = true;
            break;
          }
        }
      }
    }

    if ( canManage ) {
      Map<String, Serializable> fileMetadata = getRepository().getFileMetadata( file.getId() );
      boolean isHidden = false;

      for ( StringKeyStringValueDto nv : metadata ) {
        // don't add hidden to the list because it is not actually part of the metadata node
        if ( ( nv.getKey().contentEquals( "_PERM_HIDDEN" ) ) ) {
          isHidden = Boolean.parseBoolean( nv.getValue() );
        } else {
          fileMetadata.put( nv.getKey(), nv.getValue() );
        }
      }

      // now update the rest of the metadata
      if ( !file.isFolder() ) {
        getRepository().setFileMetadata( file.getId(), fileMetadata );
      }

      // handle hidden flag if it is different
      if ( file.isHidden() != isHidden ) {
        file.setHidden( isHidden );

          /*
           * Since we cannot simply set the new value, use the RepositoryFileAdapter to create a new instance and then
           * update the original.
           */
        RepositoryFile sourceFile = getRepository().getFileById( file.getId() );
        RepositoryFileDto destFileDto = toFileDto( sourceFile, null, false );

        destFileDto.setHidden( isHidden );

        RepositoryFile destFile = toFile( destFileDto );

        // add the existing acls and file data
        RepositoryFileAcl acl = getRepository().getAcl( sourceFile.getId() );
        if ( !file.isFolder() ) {
          IRepositoryFileData data = getData( sourceFile );

          getRepository().updateFile( destFile, data, null );
          getRepository().updateAcl( acl );
        } else {
          getRepository().updateFolder( destFile, null );
        }
      }
    } else {
      throw new GeneralSecurityException();
    }
  }

  protected RepositoryFileDto toFileDto( RepositoryFile repositoryFile, Set<String> memberSet, boolean exclude ) {
    return RepositoryFileAdapter.toFileDto( repositoryFile, memberSet, exclude );
  }

  public RepositoryFile toFile( final RepositoryFileDto repositoryFileDto ) {
    return RepositoryFileAdapter.toFile( repositoryFileDto );
  }

  public RepositoryDownloadWhitelist getWhitelist() {
    if ( whitelist == null ) {
      whitelist = new RepositoryDownloadWhitelist();
    }
    return whitelist;
  }

  public boolean isPathValid( String path ) {
    if ( path.startsWith( "/etc" ) || path.startsWith( "/system" ) ) {
      return false;
    }
    return true;
  }

  public boolean isPath( String pathId ) {
    return pathId != null && pathId.contains( "/" );
  }

  public IAuthorizationPolicy getPolicy() {
    if ( policy == null ) {
      policy = PentahoSystem.get( IAuthorizationPolicy.class );
    }
    return policy;
  }

  /**
   * Store content creator of the selected repository file
   *
   * @param pathId colon separated path for the repository file
   * <pre function="syntax.xml">
   *    :path:to:file:id
   * </pre>
   * @param contentCreator repository file
   * <pre function="syntax.xml">
   *   <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   *     &lt;repositoryFileDto&gt;
   *     &lt;createdDate&gt;1402911997019&lt;/createdDate&gt;
   *     &lt;fileSize&gt;3461&lt;/fileSize&gt;
   *     &lt;folder&gt;false&lt;/folder&gt;
   *     &lt;hidden&gt;false&lt;/hidden&gt;
   *     &lt;id&gt;ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/id&gt;
   *     &lt;lastModifiedDate&gt;1406647160536&lt;/lastModifiedDate&gt;
   *     &lt;locale&gt;en&lt;/locale&gt;
   *     &lt;localePropertiesMapEntries&gt;
   *       &lt;localeMapDto&gt;
   *         &lt;locale&gt;default&lt;/locale&gt;
   *         &lt;properties&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;file.title&lt;/key&gt;
   *             &lt;value&gt;myFile&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;jcr:primaryType&lt;/key&gt;
   *             &lt;value&gt;nt:unstructured&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;title&lt;/key&gt;
   *             &lt;value&gt;myFile&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;file.description&lt;/key&gt;
   *             &lt;value&gt;myFile Description&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *         &lt;/properties&gt;
   *       &lt;/localeMapDto&gt;
   *     &lt;/localePropertiesMapEntries&gt;
   *     &lt;locked&gt;false&lt;/locked&gt;
   *     &lt;name&gt;myFile.prpt&lt;/name&gt;&lt;/name&gt;
   *     &lt;originalParentFolderPath&gt;/public/admin&lt;/originalParentFolderPath&gt;
   *     &lt;ownerType&gt;-1&lt;/ownerType&gt;
   *     &lt;path&gt;/public/admin/ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/path&gt;
   *     &lt;title&gt;myFile&lt;/title&gt;
   *     &lt;versionId&gt;1.9&lt;/versionId&gt;
   *     &lt;versioned&gt;true&lt;/versioned&gt;
   *   &lt;/repositoryFileAclDto&gt;
   * </pre>
   * @throws FileNotFoundException
   */
  public void doSetContentCreator( String pathId, RepositoryFileDto contentCreator ) throws FileNotFoundException {
    RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
    if ( file == null ) {
      throw new FileNotFoundException();
    }
    try {
      Map<String, Serializable> fileMetadata = getRepository().getFileMetadata( file.getId() );
      fileMetadata.put( PentahoJcrConstants.PHO_CONTENTCREATOR, contentCreator.getId() );
      getRepository().setFileMetadata( file.getId(), fileMetadata );
    } catch ( Exception e ) {
      throw new InternalError();
    }
  }

  /**
   * Retrieves the list of locale map for the selected repository file. The list will be empty if a problem occurs.
   *
   * @param pathId colon separated path for the repository file
   * <pre function="syntax.xml">
   *    :path:to:file:id
   * </pre>
   * @return <code>List<LocaleMapDto></code> the list of locales
   *         <pre function="syntax.xml">
   *           <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   *           &lt;localePropertiesMapEntries&gt;
   *             &lt;localeMapDto&gt;
   *               &lt;locale&gt;default&lt;/locale&gt;
   *               &lt;properties&gt;
   *                 &lt;stringKeyStringValueDto&gt;
   *                   &lt;key&gt;file.title&lt;/key&gt;
   *                   &lt;value&gt;myFile&lt;/value&gt;
   *                 &lt;/stringKeyStringValueDto&gt;
   *                 &lt;stringKeyStringValueDto&gt;
   *                   &lt;key&gt;jcr:primaryType&lt;/key&gt;
   *                   &lt;value&gt;nt:unstructured&lt;/value&gt;
   *                 &lt;/stringKeyStringValueDto&gt;
   *                 &lt;stringKeyStringValueDto&gt;
   *                   &lt;key&gt;title&lt;/key&gt;
   *                   &lt;value&gt;myFile&lt;/value&gt;
   *                 &lt;/stringKeyStringValueDto&gt;
   *                 &lt;stringKeyStringValueDto&gt;
   *                   &lt;key&gt;file.description&lt;/key&gt;
   *                   &lt;value&gt;myFile Description&lt;/value&gt;
   *                 &lt;/stringKeyStringValueDto&gt;
   *               &lt;/properties&gt;
   *             &lt;/localeMapDto&gt;
   *           &lt;/localePropertiesMapEntries&gt;
   *         </pre>
   * @throws FileNotFoundException
   */
  public List<LocaleMapDto> doGetFileLocales( String pathId ) throws FileNotFoundException {
    List<LocaleMapDto> availableLocales = new ArrayList<LocaleMapDto>();
    RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
    if ( file == null ) {
      throw new FileNotFoundException();
    }
    try {
      List<PentahoLocale> locales = getRepoWs().getAvailableLocalesForFileById( file.getId() );
      if ( locales != null && !locales.isEmpty() ) {
        for ( PentahoLocale locale : locales ) {
          availableLocales.add( new LocaleMapDto( locale.toString(), null ) );
        }
      }
    } catch ( Exception e ) {
      throw new InternalError();
    }
    return availableLocales;
  }

  /**
   * Checks whether the current user can administer the platform.
   * The conditions are <code>RepositoryReadAction</code>, <code>RepositoryCreateAction</code> and
   * <code>AdministerSecurityAction</code>
   *
   * @return <code>boolean</code>
   */
  public boolean doCanAdminister() throws Exception {
    boolean status = false;
    try {
      status = getPolicy().isAllowed( RepositoryReadAction.NAME )
        && getPolicy().isAllowed( RepositoryCreateAction.NAME )
        && getPolicy().isAllowed( AdministerSecurityAction.NAME );
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.CAN_ADMINISTER" ), e );
    }
    return status;
  }

  /**
   * Retrieves the acls of the selected repository file
   *
   * @param pathId (colon separated path for the repository file)
   * @return <code> RepositoryFileAclDto </code>
   */
  public RepositoryFileAclDto doGetFileAcl( String pathId ) {
    RepositoryFileDto file = getRepoWs().getFile( FileUtils.idToPath( pathId ) );
    RepositoryFileAclDto fileAcl = getRepoWs().getAcl( file.getId() );
    if ( fileAcl.isEntriesInheriting() ) {
      List<RepositoryFileAclAceDto> aces =
        getRepoWs().getEffectiveAcesWithForceFlag( file.getId(), fileAcl.isEntriesInheriting() );
      fileAcl.setAces( aces, fileAcl.isEntriesInheriting() );
    }
    addAdminRole( fileAcl );
    return fileAcl;
  }

  protected void addAdminRole( RepositoryFileAclDto fileAcl ) {
    String adminRoleName =
      PentahoSystem.get( String.class, "singleTenantAdminAuthorityName", PentahoSessionHolder.getSession() );
    if ( fileAcl.getAces() == null ) {
      fileAcl.setAces( new LinkedList<RepositoryFileAclAceDto>() );
    }
    for ( RepositoryFileAclAceDto facl : fileAcl.getAces() ) {
      if ( facl.getRecipient().equals( adminRoleName ) && facl.getRecipientType() == 1 ) {
        return;
      }
    }
    RepositoryFileAclAceDto adminGroup = new RepositoryFileAclAceDto();
    adminGroup.setRecipient( adminRoleName );
    adminGroup.setRecipientType( 1 );
    adminGroup.setModifiable( false );
    List<Integer> perms = new LinkedList<Integer>();
    perms.add( 4 );
    adminGroup.setPermissions( perms );
    fileAcl.getAces().add( adminGroup );
  }

  public RepositoryFileTreeDto doGetTree( String pathId, Integer depth, String filter, Boolean showHidden,
                                          Boolean includeAcls ) {
    String path = null;
    if ( pathId == null || pathId.equals( FileUtils.PATH_SEPARATOR ) ) {
      path = FileUtils.PATH_SEPARATOR;
    } else if ( !pathId.startsWith( FileUtils.PATH_SEPARATOR ) ) {
      path = idToPath( pathId );
    }

    RepositoryRequest repositoryRequest = getRepositoryRequest( path, showHidden, depth, filter );
    repositoryRequest.setIncludeAcls( includeAcls );

    RepositoryFileTreeDto tree = getRepoWs().getTreeFromRequest( repositoryRequest );
    List<RepositoryFileTreeDto> filteredChildren = new ArrayList<RepositoryFileTreeDto>();

    // BISERVER-9599 - Use special sort order
    if ( isShowingTitle( repositoryRequest ) ) {
      Collator collator = getCollatorInstance();
      collator.setStrength( Collator.PRIMARY ); // ignore case
      sortByLocaleTitle( collator, tree );
    }

    for ( RepositoryFileTreeDto child : tree.getChildren() ) {
      RepositoryFileDto file = child.getFile();
      Map<String, Serializable> fileMeta = getRepository().getFileMetadata( file.getId() );
      boolean isSystemFolder =
        fileMeta.containsKey( IUnifiedRepository.SYSTEM_FOLDER ) ? (Boolean) fileMeta
          .get( IUnifiedRepository.SYSTEM_FOLDER ) : false;
      if ( !isSystemFolder ) {
        filteredChildren.add( child );
      }
    }
    tree.setChildren( filteredChildren );

    return tree;
  }

  public void sortByLocaleTitle( final Collator collator, final RepositoryFileTreeDto tree ) {

    if ( tree == null || tree.getChildren() == null || tree.getChildren().size() <= 0 ) {
      return;
    }

    for ( RepositoryFileTreeDto rft : tree.getChildren() ) {
      sortByLocaleTitle( collator, rft );
      Collections.sort( tree.getChildren(), new Comparator<RepositoryFileTreeDto>() {
        @Override
        public int compare( RepositoryFileTreeDto repositoryFileTree, RepositoryFileTreeDto repositoryFileTree2 ) {
          String title1 = repositoryFileTree.getFile().getTitle();
          String title2 = repositoryFileTree2.getFile().getTitle();

          if ( collator.compare( title1, title2 ) == 0 ) {
            return title1.compareTo( title2 ); // use lexical order if equals ignore case
          }

          return collator.compare( title1, title2 );
        }
      } );
    }
  }

  /**
   * Retrieve the executed contents for a selected repository file
   *
   * @param pathId the path for the file
   * @return list of <code> repositoryFileDto </code>
   * @throws FileNotFoundException if the file is not found
   */
  public List<RepositoryFileDto> doGetGeneratedContent( String pathId ) throws FileNotFoundException {
    SessionResource sessionResource = getSessionResource();
    return doGetGeneratedContentForUser( pathId, sessionResource.doGetCurrentUserDir() );
  }

  /**
   * Retrieve the executed contents for a selected repository file and a given user
   *
   * @param pathId the path for the file
   * @param user   the username for the generated content folder
   * @return list of <code> repositoryFileDto </code>
   * @throws FileNotFoundException
   */
  public List<RepositoryFileDto> doGetGeneratedContent( String pathId, String user ) throws FileNotFoundException {
    SessionResource sessionResource = getSessionResource();
    return doGetGeneratedContentForUser( pathId, sessionResource.doGetUserDir( user ) );
  }

  /**
   * Retrieve the executed contents for a selected repository file and a given user
   *
   * @param pathId  the path for the file
   * @param userDir the user home directory
   * @return list of <code> repositoryFileDto </code>
   * @throws FileNotFoundException
   * @private
   */
  private List<RepositoryFileDto> doGetGeneratedContentForUser( String pathId, String userDir )
    throws FileNotFoundException {
    RepositoryFileDto targetFile = doGetProperties( pathId );
    if ( targetFile != null ) {
      String targetFileId = targetFile.getId();
      return searchGeneratedContent( userDir, targetFileId, PentahoJcrConstants.PHO_CONTENTCREATOR );
    } else {
      logger.error( Messages.getInstance().getString( "FileResource.FILE_NOT_FOUND", pathId ) );
      throw new FileNotFoundException( pathId );
    }
  }

  /**
   * @param userDir          the user home directory
   * @param targetComparator the comparator to filter
   * @param metadataConstant the property used to get the file property to compare
   * @return list of <code> repositoryFileDto </code>
   * @throws FileNotFoundException
   * @private
   */
  protected List<RepositoryFileDto> searchGeneratedContent( String userDir, String targetComparator,
                                                          String metadataConstant )
    throws FileNotFoundException {
    List<RepositoryFileDto> content = new ArrayList<RepositoryFileDto>();

    RepositoryFile workspaceFolder = getRepository().getFile( userDir );
    if ( workspaceFolder != null ) {
      List<RepositoryFile> children = getRepository().getChildren( workspaceFolder.getId() );
      for ( RepositoryFile child : children ) {
        if ( !child.isFolder() ) {
          Map<String, Serializable> fileMetadata = getRepository().getFileMetadata( child.getId() );
          String creatorId = (String) fileMetadata.get( metadataConstant );
          if ( creatorId != null && creatorId.equals( targetComparator ) ) {
            content.add( toFileDto( child, null, false ) );
          }
        }
      }
    } else {
      logger.error( Messages.getInstance().getString( "FileResource.WORKSPACE_FOLDER_NOT_FOUND", userDir ) );
      throw new FileNotFoundException( userDir );
    }

    return content;
  }

  /**
   * Gets an instance of SessionResource
   *
   * @return <code>SessionResource</code>
   */
  protected SessionResource getSessionResource() {
    if ( sessionResource == null ) {
      sessionResource = new SessionResource();
    }
    return sessionResource;
  }

  /**
   * Rename the name of the selected file
   *
   * @param pathId  (colon separated path for the repository file)
   * @param newName (New name of the file)
   * @return
   */
  public boolean doRename( String pathId, String newName ) throws Exception {
    IUnifiedRepository repository = getRepository();
    RepositoryFile fileToBeRenamed = repository.getFile( FileUtils.idToPath( pathId ) );
    StringBuilder buf = new StringBuilder( fileToBeRenamed.getPath().length() );
    buf.append( getParentPath( fileToBeRenamed.getPath() ) );
    buf.append( RepositoryFile.SEPARATOR );
    buf.append( newName );
    if ( !fileToBeRenamed.isFolder() ) {
      String extension = getExtension( fileToBeRenamed.getName() );
      if ( extension != null ) {
        buf.append( extension );
      }
    }
    repository.moveFile( fileToBeRenamed.getId(), buf.toString(), "Renaming the file" );
    RepositoryFile movedFile = repository.getFileById( fileToBeRenamed.getId() );
    if ( movedFile != null ) {
      if ( !movedFile.isFolder() ) {
        Map<String, Properties> localePropertiesMap = movedFile.getLocalePropertiesMap();
        if ( localePropertiesMap == null ) {
          localePropertiesMap = new HashMap<String, Properties>();
          Properties properties = new Properties();
          properties.setProperty( "file.title", newName );
          properties.setProperty( "title", newName );
          localePropertiesMap.put( "default", properties );
        } else {
          for ( Map.Entry<String, Properties> entry : localePropertiesMap.entrySet() ) {
            Properties properties = entry.getValue();
            if ( properties.containsKey( "file.title" ) ) {
              properties.setProperty( "file.title", newName );
            }
            if ( properties.containsKey( "title" ) ) {
              properties.setProperty( "title", newName );
            }
          }
        }
        RepositoryFile updatedFile =
          new RepositoryFile.Builder( movedFile ).localePropertiesMap( localePropertiesMap ).name( newName ).title(
            newName ).build();
        repository.updateFile( updatedFile, getData( movedFile ), "Updating the file" );
      }
      return true;
    } else {
      return false;
      //return Response.ok( "File to be renamed does not exist" ).build();
    }
  }

  /**
   * Creates a new folder with the specified name
   *
   * @param pathId      The path from the root folder to the root node of the tree to return using colon characters in
   *                    place of / or \ characters. To clarify /path/to/file, the encoded pathId would be :path:to:file
   *                    <pre function="syntax.xml">
   *                      :path:to:file
   *                    </pre>
   * @return            A jax-rs Response object with the appropriate status code, header, and body.
   */
  public boolean doCreateDir( String pathId ) {
    String path = idToPath( pathId );
    String[] folders = path.split( "[" + FileUtils.PATH_SEPARATOR + "]" ); //$NON-NLS-1$//$NON-NLS-2$
    RepositoryFileDto parentDir = getRepoWs().getFile( FileUtils.PATH_SEPARATOR );
    boolean dirCreated = false;
    for ( String folder : folders ) {
      String currentFolderPath = ( parentDir.getPath() + FileUtils.PATH_SEPARATOR + folder ).substring( 1 );
      if ( !currentFolderPath.startsWith( FileUtils.PATH_SEPARATOR ) ) {
        currentFolderPath = FileUtils.PATH_SEPARATOR + currentFolderPath;
      }
      RepositoryFileDto currentFolder = getRepoWs().getFile( currentFolderPath );
      if ( currentFolder == null ) {
        currentFolder = new RepositoryFileDto();
        currentFolder.setFolder( true );
        currentFolder.setName( decode( folder ) );
        currentFolder.setPath( parentDir.getPath() + FileUtils.PATH_SEPARATOR + folder );
        currentFolder = getRepoWs().createFolder( parentDir.getId(), currentFolder, currentFolderPath );
        dirCreated = true;
      }
      parentDir = currentFolder;
    }
    return dirCreated;
  }

  private String getParentPath( final String path ) {
    if ( path == null ) {
      throw new IllegalArgumentException();
    } else if ( RepositoryFile.SEPARATOR.equals( path ) ) {
      return null;
    }
    int lastSlashIndex = path.lastIndexOf( RepositoryFile.SEPARATOR );
    if ( lastSlashIndex == 0 ) {
      return RepositoryFile.SEPARATOR;
    } else if ( lastSlashIndex > 0 ) {
      return path.substring( 0, lastSlashIndex );
    } else {
      throw new IllegalArgumentException();
    }
  }

  private String getExtension( final String name ) {
    int startIndex = name.lastIndexOf( '.' );
    if ( startIndex >= 0 ) {
      return name.substring( startIndex, name.length() );
    }
    return null;
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

  public StringKeyStringValueDto getStringKeyStringValueDto( String key, String value ) {
    return new StringKeyStringValueDto( key, value );
  }

  public IUnifiedRepository getRepository() {
    if ( repository == null ) {
      repository = PentahoSystem.get( IUnifiedRepository.class );
    }
    return repository;
  }

  public String idToPath( String pathId ) {
    return FileUtils.idToPath( pathId );
  }

  protected IPentahoSession getSession() {
    return PentahoSessionHolder.getSession();
  }

  protected String escapeJava( String value ) {
    return StringEscapeUtils.escapeJava( value );
  }

  protected BaseExportProcessor getDownloadExportProcessor( String path, boolean requiresZip, boolean withManifest ) {
    return requiresZip ? new ZipExportProcessor( path, getRepository(), withManifest ) : new SimpleExportProcessor( path, getRepository() );
  }

  protected ExportHandler getDownloadExportHandler() {
    return PentahoSystem.get( DefaultExportHandler.class );
  }

  protected StreamingOutput getDownloadStream( RepositoryFile repositoryFile, BaseExportProcessor exportProcessor )
    throws ExportException, IOException {
    File zipFile = exportProcessor.performExport( repositoryFile );
    final FileInputStream is = new FileInputStream( zipFile );
    // copy streaming output
    return new StreamingOutput() {
      public void write( OutputStream output ) throws IOException {
        IOUtils.copy( is, output );
      }
    };
  }

  protected RepositoryRequest getRepositoryRequest( String  path, Boolean showHidden, Integer depth, String filter ) {
    return new RepositoryRequest( path, showHidden, depth, filter );
  }

  protected Collator getCollatorInstance() {
    return Collator.getInstance( PentahoSessionHolder.getSession().getLocale() );
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

  public List<RepositoryFileDto> doGetChildren( String pathId, String filter, Boolean showHidden,
                                                Boolean includeAcls ) {

    List<RepositoryFileDto> repositoryFileDtoList = new ArrayList<RepositoryFileDto>();
    RepositoryFileDto repositoryFileDto = getRepoWs().getFile( FileUtils.idToPath( pathId ) );

    if ( repositoryFileDto != null && isPathValid( repositoryFileDto.getPath() ) ) {
      RepositoryRequest repositoryRequest = getRepositoryRequest( repositoryFileDto, showHidden, filter, includeAcls );
      repositoryFileDtoList = getRepoWs().getChildrenFromRequest( repositoryRequest );

      // BISERVER-9599 - Use special sort order
      if ( isShowingTitle( repositoryRequest ) ) {
        Collator collator = getCollator( Collator.PRIMARY );
        sortByLocaleTitle( collator, repositoryFileDtoList );
      }
    }
    return repositoryFileDtoList;
  }

  public boolean isShowingTitle( RepositoryRequest repositoryRequest ) {
    if ( repositoryRequest.getExcludeMemberSet() != null && !repositoryRequest.getExcludeMemberSet().isEmpty() ) {
      if ( repositoryRequest.getExcludeMemberSet().contains( "title" ) ) {
        return false;
      }
    } else if ( repositoryRequest.getIncludeMemberSet() != null
        && !repositoryRequest.getIncludeMemberSet().contains( "title" ) ) {
      return false;
    }
    return true;
  }

  public void sortByLocaleTitle( final Collator collator, final List<RepositoryFileDto> repositoryFileDtoList ) {

    if ( repositoryFileDtoList == null || repositoryFileDtoList.size() <= 0 ) {
      return;
    }

    for ( RepositoryFileDto rft : repositoryFileDtoList ) {
      Collections.sort( repositoryFileDtoList, new Comparator<RepositoryFileDto>() {
        @Override
        public int compare( RepositoryFileDto repositoryFile, RepositoryFileDto repositoryFile2 ) {
          String title1 = repositoryFile.getTitle();
          String title2 = repositoryFile2.getTitle();

          if ( collator.compare( title1, title2 ) == 0 ) {
            return title1.compareTo( title2 ); // use lexical order if equals ignore case
          }

          return collator.compare( title1, title2 );
        }
      } );
    }
  }

  protected RepositoryRequest getRepositoryRequest( RepositoryFileDto repositoryFileDto, Boolean showHidden,
                                                    String filter, Boolean includeAcls ) {
    RepositoryRequest repositoryRequest = new RepositoryRequest( repositoryFileDto.getId(), showHidden, 0, filter );
    repositoryRequest.setIncludeAcls( includeAcls );
    return repositoryRequest;
  }

  protected Collator getCollator( int strength ) {
    Collator collator = Collator.getInstance( PentahoSessionHolder.getSession().getLocale() );
    collator.setStrength( strength ); // ignore case
    return collator;
  }

  protected String decode( String folder ) {
    String decodeName = folder;
    try {
      decodeName = URLDecoder.decode( folder, "UTF-8" );
    } catch ( Exception ex ) {
      logger.error( ex );
    }
    return decodeName;
  }
}
