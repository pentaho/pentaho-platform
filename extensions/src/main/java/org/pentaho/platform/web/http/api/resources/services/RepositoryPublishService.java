/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources.services;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;

public class RepositoryPublishService {

  private static final Log logger = LogFactory.getLog( RepositoryPublishService.class );

  protected IAuthorizationPolicy policy;
  protected IPlatformImporter platformImporter;

  /**
   * Publishes the file to the provided path in the repository. The file will be overwritten if the overwrite flag
   * is set to true
   *
   * @param pathId path for the repository file
   *               <pre function="syntax.xml">
   *               /path/to/file/id
   *               </pre>
   * @param fileContents (input stream containing the data)
   * @param overwriteFile (flag to determine whether to overwrite the existing file in the repository or not)
   *               <pre function="syntax.xml">
   *               true
   *               </pre>
   *
   * @deprecated use {@linkplain #publishFile(String, InputStream, Boolean)} instead
   */
  @Deprecated
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
   * We need to keep the additional options for file will use {@link #publishFile(String, InputStream, Boolean, String)}
   * We keep the method for backward compatibility 
   * 
   * @since pentaho 8.1
   */
  @Deprecated
  public void publishFile( String pathId, InputStream fileContents, Boolean overwriteFile ) throws PlatformImportException, PentahoAccessControlException {
    Optional<Properties> fileProperties = Optional.of( new Properties() );
    fileProperties.get().setProperty( "overwriteFile", String.valueOf( overwriteFile ) );
    publishFile( pathId, fileContents,  fileProperties );
  }

  /**
   * Publishes the file to the provided path in the repository via registered importers. The file will be overwritten if
   * the {@code overwrite} is {@code true}
   *
   * @param pathId        slash-separated path for the repository file <pre function="syntax.xml"> /path/to/file/id
   *                      </pre>
   * @param  fileContents  input stream containing the data
   * @param  options any options which can be applied to the file
   * @throws PentahoAccessControlException if current user is not allowed to publish files
   * @throws PlatformImportException rethrows any exception raised in the importer
   * @throws RuntimeException rethrows any exception raised in the importer
   */
  public void publishFile( String pathId, InputStream fileContents, Optional<Properties> fileProperties )
      throws PlatformImportException, PentahoAccessControlException {
    try {
      validateAccess();
    } catch ( PentahoAccessControlException e ) {
      logger.error( e );
      throw e;
    }

    IPlatformImportBundle bundle = prepareBundle( pathId, fileContents, fileProperties );
    try {
      getPlatformImporter().importFile( bundle );
    } catch ( PlatformImportException e ) {
      logger.error( e );
      throw e;
    } catch ( Exception e ) {
      logger.error( e );
      throw new RuntimeException();
    }
  }

  /**
   * Check if user has the rights to publish or is administrator
   *
   * @throws PentahoAccessControlException
   */
  protected void validateAccess() throws PentahoAccessControlException {
    if ( !getPolicy().isAllowed( PublishAction.NAME ) ) {
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

  @Deprecated
  protected IPlatformImportBundle buildBundle( String pathId, InputStream fileContents, Boolean overwriteFile ) {
    File file = new File( pathId );
    RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder()
        .input( fileContents )
        .charSet( "UTF-8" )
        .hidden( RepositoryFile.HIDDEN_BY_DEFAULT )
        .schedulable( RepositoryFile.SCHEDULABLE_BY_DEFAULT )
        .mime( "text/xml" )
        .path( file.getParent() )
        .name( file.getName() )
        .overwriteFile( overwriteFile );
    return bundleBuilder.build();
  }

  protected IPlatformImportBundle prepareBundle( String fullPath, InputStream fileContents, Optional<Properties> fileProperties ) {
    return new RepositoryFileImportBundle.Builder()
      .input( fileContents )
      .charSet( "UTF-8" )
      .hidden( RepositoryFile.HIDDEN_BY_DEFAULT )
      .schedulable( RepositoryFile.SCHEDULABLE_BY_DEFAULT )
      .mime( "text/xml" )
      .path( "/" + FilenameUtils.getPathNoEndSeparator( fullPath ) )
      .name( FilenameUtils.getName( fullPath ) )
      .overwriteFile( Boolean.valueOf( fileProperties.orElseGet( () -> new Properties() ).getProperty( "overwriteFile", "true" ) ) )
      .title( fileProperties.orElseGet( () -> new Properties() ).getProperty( "reportTitle" ) )
      .build();
  }
}
