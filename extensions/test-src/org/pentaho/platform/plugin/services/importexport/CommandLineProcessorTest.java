/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importexport;

import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.importer.NameBaseMimeResolver;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.web.http.filters.PentahoRequestContextFilter;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Class Description
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class CommandLineProcessorTest extends JerseyTest {
  private static String VALID_URL_OPTION = "--url=http://localhost:8080/pentaho";
  private static String VALID_IMPORT_COMMAND_LINE = "--import --username=admin "
      + "--password=password --charset=UTF-8 --path=/public "
      + "--file-path=/home/dkincade/pentaho/platform/trunk/biserver-ee/pentaho-solutions " + VALID_URL_OPTION;

  private String tmpZipFileName;
  private static WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder(
      "org.pentaho.platform.web.http.api.resources" ).contextPath( "api" ).addFilter(
      PentahoRequestContextFilter.class, "pentahoRequestContextFilter" ).build();
  private MicroPlatform mp = new MicroPlatform( "test-src/solution" );

  public CommandLineProcessorTest() throws IOException {
    final TemporaryFolder tmpFolder = new TemporaryFolder();
    tmpFolder.create();
    tmpZipFileName = tmpFolder.getRoot().getAbsolutePath() + File.separator + "test.zip";

    NameBaseMimeResolver mimeResolver = mock( NameBaseMimeResolver.class );
    IRepositoryContentConverterHandler converterHandler = mock( IRepositoryContentConverterHandler.class );

    mp.setFullyQualifiedServerUrl( getBaseURI() + webAppDescriptor.getContextPath() + "/" );
    mp.define( ISolutionEngine.class, SolutionEngine.class );
    mp.define( IUnifiedRepository.class, FileSystemBackedUnifiedRepository.class
        , IPentahoDefinableObjectFactory.Scope.GLOBAL );
    mp.define( IAuthorizationPolicy.class, TestAuthorizationPolicy.class );
    mp.define( IAuthorizationAction.class, AdministerSecurityAction.class );
    mp.define( DefaultExportHandler.class, DefaultExportHandler.class );
    mp.defineInstance( IRepositoryContentConverterHandler.class, converterHandler );
    mp.defineInstance( NameBaseMimeResolver.class, mimeResolver );

    FileSystemBackedUnifiedRepository repo =
        (FileSystemBackedUnifiedRepository) PentahoSystem.get( IUnifiedRepository.class );
    repo.setRootDir( new File( "test-src/solution" ) );

    StandaloneSession session = new StandaloneSession();
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );
    PentahoSessionHolder.setSession( session );
  }

  private static final String[] toStringArray( final String s ) {
    return StringUtils.split( s, ' ' );
  }

  private static final String[] toStringArray( final String s1, final String s2 ) {
    return StringUtils.split( s1 + " " + s2, ' ' );
  }

  @Test
  public void testInvalidCommandLineParameters() throws Exception {
    CommandLineProcessor.main( new String[] {} );
    assertEquals( ParseException.class, CommandLineProcessor.getException().getClass() );

    CommandLineProcessor.main( toStringArray( VALID_IMPORT_COMMAND_LINE, "--export" ) );
    assertEquals( ParseException.class, CommandLineProcessor.getException().getClass() );

    CommandLineProcessor.main( toStringArray( "--help" ) );
    assertNull( CommandLineProcessor.getException() );

    CommandLineProcessor.main( toStringArray( VALID_IMPORT_COMMAND_LINE.replace( VALID_URL_OPTION, "" ) ) );
    assertEquals( ParseException.class, CommandLineProcessor.getException().getClass() );
  }

  @Test
  public void testGetProperty() throws Exception {

  }

  @Test
  public void testGetOptionValue() throws Exception {

  }

  @Test
  public void testGetImportProcessor() throws Exception {

  }

  @Test
  public void testGetImportSource() throws Exception {

  }

  @Test
  public void testAddImportHandlers() throws Exception {

  }

  @Test
  public void testExportFileParameter() throws Exception {
    final String baseOptions = "-e -a " + getBaseUrl() + " -u admin -p password -f \"/test\"";
    String fileOption;

    // correct file path
    fileOption = "-fp " + tmpZipFileName;
    CommandLineProcessor.main( toStringArray( baseOptions, fileOption ) );
    assertNull( CommandLineProcessor.getException() );

    // incorrect file path
    fileOption = "-fp test.zip";
    CommandLineProcessor.main( toStringArray( baseOptions, fileOption ) );
    assertEquals( ParseException.class, CommandLineProcessor.getException().getClass() );
  }

  @Test
  public void testExportPathParameter() throws Exception {
    final String baseOptions = "-e -a " + getBaseUrl() + " -u admin -p password -fp " + tmpZipFileName;
    String pathOption;

    //correct path
    pathOption = "-f \"/test\"";
    CommandLineProcessor.main( toStringArray( baseOptions, pathOption ) );
    assertNull( CommandLineProcessor.getException() );

    //path with trailing slash
    pathOption = "-f \"/test/\"";
    CommandLineProcessor.main( toStringArray( baseOptions, pathOption ) );
    assertNull( CommandLineProcessor.getException() );

    //path that doesn't exist
    pathOption = "-f \"/path_that_not_exists\"";
    CommandLineProcessor.main( toStringArray( baseOptions, pathOption ) );
    assertEquals( ParseException.class, CommandLineProcessor.getException().getClass() );
  }

  @Test
  public void testExportAll() throws Exception {
    final String baseOptions = "-e -a " + getBaseUrl() + " -u admin -p password -fp " + tmpZipFileName + " -f \"/\"";

    CommandLineProcessor.main( toStringArray( baseOptions ) );
    assertNull( CommandLineProcessor.getException() );
  }

  @Test
  public void testExportNotAdmin() throws Exception {
    mp.defineInstance( IAuthorizationPolicy.class, new IAuthorizationPolicy() {
      @Override
      public boolean isAllowed( String actionName ) {
        return !actionName.equals( AdministerSecurityAction.NAME );
      }

      @Override
      public List<String> getAllowedActions( String actionNamespace ) {
        return null;
      }
    } );

    final String baseOptions = "-e -a " + getBaseUrl() + " -u admin -p password -fp " + tmpZipFileName + " -f \"/\"";
    CommandLineProcessor.main( toStringArray( baseOptions ) );
    assertEquals( InitializationException.class, CommandLineProcessor.getException().getClass() );
  }

  @Override
  protected AppDescriptor configure() {
    return webAppDescriptor;
  }

  @Override
  protected TestContainerFactory getTestContainerFactory() {
    return new GrizzlyWebTestContainerFactory();
  }

  private String getBaseUrl() {
    String baseUrl = getBaseURI().toString();
    if ( baseUrl.endsWith( "/" ) ) {
      baseUrl = baseUrl.substring( 0, baseUrl.length() - 1 );
    }
    return baseUrl;
  }
}
