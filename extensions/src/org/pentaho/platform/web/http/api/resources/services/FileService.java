package org.pentaho.platform.web.http.api.resources.services;

import com.sun.tools.javac.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.util.RepositoryPathEncoder;
import org.pentaho.platform.web.http.messages.Messages;

import javax.ws.rs.core.Response;
import org.pentaho.platform.util.RepositoryPathEncoder;
import org.pentaho.platform.web.http.messages.Messages;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class FileService {
  public static final String PATH_SEPARATOR = "/";

  private static final Log logger = LogFactory.getLog( FileService.class );

  protected FileServiceFactory fileServiceFactory;

  protected FileServiceUtils fileServiceUtils;

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
        getRepoWs().deleteFile( sourceFileIds[i], null );
      }
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
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
      String idToPath = getFileServiceUtils().idToPath(pathId);
      RepositoryFileOutputStream rfos = getFileServiceFactory().getRepositoryFileOutputStream(idToPath);
      rfos.setCharsetName(httpServletRequest.getCharacterEncoding());
      getFileServiceUtils().copy(fileContents, rfos);
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
    String idToPath = getFileServiceUtils().idToPath( destPathId );
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

  public FileServiceUtils getFileServiceUtils() {
    if (this.fileServiceUtils == null) {
      this.fileServiceUtils = new FileServiceUtils();
    }

    return this.fileServiceUtils;
  }

  protected DefaultUnifiedRepositoryWebService getRepoWs() {
    return getFileServiceFactory().getDefaultUnifiedRepositoryWebService();
  }

  protected FileServiceFactory getFileServiceFactory() {
    if ( fileServiceFactory == null ) {
      fileServiceFactory = new FileServiceFactory();
    }

    return fileServiceFactory;
  }

  public static class FileServiceUtils {
    public int copy(InputStream obj1, RepositoryFileOutputStream obj2) throws IOException {
      return IOUtils.copy(obj1, obj2);
    }

    public String idToPath( String pathId ) {
      String path = null;
      // slashes in pathId are illegal.. we scrub them out so the file will not be found
      // if the pathId was given in slash separated format
      if ( pathId.contains( PATH_SEPARATOR ) ) {
        logger.warn( Messages.getInstance().getString( "FileResource.ILLEGAL_PATHID", pathId ) );
      }
      path = pathId.replaceAll( PATH_SEPARATOR, "" );
      path = RepositoryPathEncoder.decodeRepositoryPath(path);
      if ( !path.startsWith( PATH_SEPARATOR ) ) {
        path = PATH_SEPARATOR + path;
      }
      return path;
    }
  }

  public static class FileServiceFactory {

    protected DefaultUnifiedRepositoryWebService defaultUnifiedRepositoryWebService;
    protected RepositoryFileOutputStream mockRepositoryFileOutputStream;

    public DefaultUnifiedRepositoryWebService getDefaultUnifiedRepositoryWebService() {
      if (this.defaultUnifiedRepositoryWebService == null) {
        this.defaultUnifiedRepositoryWebService = new DefaultUnifiedRepositoryWebService();
      }

      return this.defaultUnifiedRepositoryWebService;
    }

    public RepositoryFileOutputStream getRepositoryFileOutputStream(String path) {
      if (this.mockRepositoryFileOutputStream != null) {
        return this.mockRepositoryFileOutputStream;
      }

      return new RepositoryFileOutputStream(path);
    }

  }
}
