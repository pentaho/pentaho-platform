package org.pentaho.platform.web.http.api.resources.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

import java.io.File;
import java.io.InputStream;

public class RepositoryPublishService {

  private static final Log logger = LogFactory.getLog( RepositoryPublishService.class );

  protected IAuthorizationPolicy policy;
  protected IPlatformImporter platformImporter;

  /**
   * Publishes the file to the provided path in the repository. The file will be overwritten if the overwrite flag
   * is set to true
   *
   * @param pathId (colon separated path for the repository file)
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @param fileContents (input stream containing the data)
   * @param overwriteFile (flag to determine whether to overwrite the existing file in the repository or not)
   *               <pre function="syntax.xml">
   *               true
   *               </pre>
   *
   * @returns response object indicating the success or failure of this operation
   */
  public void writeFile( String pathId, InputStream fileContents, Boolean overwriteFile )
    throws PlatformImportException, PentahoAccessControlException {
    try {
      validateAccess();
    } catch ( PentahoAccessControlException e ) {
      logger.error( e );
      throw e;
    }
    IPlatformImportBundle bundle = buildBundle( pathId, fileContents, overwriteFile );
    try {
      getPlatformImporter().importFile( bundle );
    } catch ( PlatformImportException e ) {
      logger.error( e );
      throw e;
    } catch ( Exception e ) {
      logger.error( e );
      throw new InternalError();
    }
  }

  /**
   * Check if user has the rights to publish or is administrator
   *
   * @throws PentahoAccessControlException
   */
  protected void validateAccess() throws PentahoAccessControlException {
    boolean isAdmin =
      getPolicy().isAllowed( RepositoryReadAction.NAME ) && getPolicy().isAllowed( RepositoryCreateAction.NAME )
        && ( getPolicy().isAllowed( AdministerSecurityAction.NAME ) || getPolicy().isAllowed( PublishAction.NAME ) );
    if ( !isAdmin ) {
      throw new PentahoAccessControlException( "Access Denied" );
    }
  }

  protected IAuthorizationPolicy getPolicy() {
    if ( policy == null ) {
      policy = PentahoSystem.get( IAuthorizationPolicy.class );
    }
    return policy;
  }

  protected IPlatformImporter getPlatformImporter() {
    if ( platformImporter == null ) {
      platformImporter = PentahoSystem.get( IPlatformImporter.class );
    }
    return platformImporter;
  }

  protected IPlatformImportBundle buildBundle( String pathId, InputStream fileContents, Boolean overwriteFile ) {
    File file = new File( pathId );
    RepositoryFileImportBundle.Builder bundleBuilder =
      new RepositoryFileImportBundle.Builder().input( fileContents ).charSet( "UTF-8" ).hidden( false ).mime(
        "text/xml" ).path( file.getParent() ).name( file.getName() ).overwriteFile( overwriteFile );
    return bundleBuilder.build();
  }
}
