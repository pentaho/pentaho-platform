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
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.metadata;

import junit.framework.TestCase;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.types.LocaleType;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.util.LocalizationUtil;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.unified.RepositoryUtils;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class PentahoMetadataDomainRepositoryTest extends TestCase {
  private static final String SAMPLE_DOMAIN_ID = "sample";
  private static final String STEEL_WHEELS = "steel-wheels";
  private static final Properties EMPTY_PROPERTIES = new Properties();
  private static final InputStream EMPTY_INPUT_STREAM = new ByteArrayInputStream( "".getBytes() );

  IUnifiedRepository repository;
  private PentahoMetadataDomainRepository domainRepository;
  private PentahoMetadataDomainRepository domainRepositorySpy;
  private IAclNodeHelper aclNodeHelper;

  protected PentahoMetadataDomainRepository createDomainRepository( final IUnifiedRepository repository ) {
    return new PentahoMetadataDomainRepository( repository );
  }

  protected PentahoMetadataDomainRepository createDomainRepository( final IUnifiedRepository repository,
      final RepositoryUtils repositoryUtils, final XmiParser xmiParser, final LocalizationUtil localizationUtil ) {
    return new PentahoMetadataDomainRepository( repository, repositoryUtils, xmiParser, localizationUtil );
  }

  @Before
  public void setUp() throws Exception {
    // File tempDir = File.createTempFile("test", "");
    // tempDir.delete();
    // tempDir.mkdir();
    // System.err.println("tempDir = " + tempDir.getAbsolutePath());
    // repository = new FileSystemBackedUnifiedRepository(tempDir);
    // new RepositoryUtils(repository).getFolder("/etc/metadata", true, true, null);

    final IPentahoSession mockSession = Mockito.mock( IPentahoSession.class );
    PentahoSessionHolder.setSession( mockSession );

    repository = new FileSystemBackedUnifiedRepository();

    repository.deleteFile( new RepositoryUtils( repository ).getFolder( "/etc/metadata", true, true, null ).getId(),
        true, null );
    assertNotNull( new RepositoryUtils( repository ).getFolder( "/etc/metadata", true, true, null ) );

    final MockXmiParser xmiParser = new MockXmiParser();
    domainRepository = createDomainRepository( repository, null, xmiParser, null );
    domainRepositorySpy = Mockito.spy( domainRepository );

    aclNodeHelper = Mockito.mock( IAclNodeHelper.class );
    Mockito.doReturn( aclNodeHelper ).when( domainRepositorySpy ).getAclHelper();
    Mockito.doNothing().when( aclNodeHelper ).removeAclFor( Mockito.any( RepositoryFile.class ) );
    Mockito.when( aclNodeHelper.canAccess(
        Mockito.any( RepositoryFile.class ), Mockito.any( EnumSet.class ) ) ).thenReturn( true );

    while ( domainRepositorySpy.getDomainIds().size() > 0 ) {
      domainRepositorySpy.removeDomain( domainRepositorySpy.getDomainIds().iterator().next() );
    }
  }

  @After
  public void tearDown() throws Exception {
    repository = null;
    domainRepository = null;
    domainRepositorySpy = null;
  }

  @Test
  public void testInitialization() throws Exception {
    try {
      createDomainRepository( null );
      fail( "An exception should be thrown" );
    } catch ( Exception success ) {
      //ignored
    }

    try {
      createDomainRepository( null, null, null, null );
      fail( "An exception should be thrown" );
    } catch ( Exception success ) {
      //ignored
    }

    final RepositoryUtils repositoryUtils = new RepositoryUtils( repository );
    final XmiParser xmiParser = new XmiParser();
    final LocalizationUtil localizationUtil = new LocalizationUtil();
    final PentahoMetadataDomainRepository repo = createDomainRepository( repository, repositoryUtils, xmiParser,
        localizationUtil );
    assertEquals( repository, repo.getRepository() );
    assertEquals( repositoryUtils, repo.getRepositoryUtils() );
    assertEquals( xmiParser, repo.getXmiParser() );
    assertEquals( localizationUtil, repo.getLocalizationUtil() );

  }

  @Test
  public void testStoreDomain() throws Exception {
    try {
      domainRepositorySpy.storeDomain( null, true );
      fail( "Invalid domain should throw exception" );
    } catch ( DomainIdNullException success ) {
      //ignored
    }

    try {
      domainRepositorySpy.storeDomain( new MockDomain( null ), true );
      fail( "Null domain id should throw exception" );
    } catch ( DomainIdNullException success ) {
      //ignored

    }

    try {
      domainRepositorySpy.storeDomain( new MockDomain( "" ), true );
      fail( "Empty domain id should throw exception" );
    } catch ( DomainIdNullException success ) {
      //ignored
    }

    try {
      domainRepositorySpy.storeDomain( null, null, true );
      fail( "Null InputStream should throw an exception" );
    } catch ( IllegalArgumentException success ) {
    }

    try {
      domainRepositorySpy.storeDomain( null, "", true );
      fail( "Null InputStream should throw an exception" );
    } catch ( IllegalArgumentException success ) {
      //ignored
    }

    try {
      domainRepositorySpy.storeDomain( null, "valid", true );
      fail( "Null InputStream should throw an exception" );
    } catch ( IllegalArgumentException success ) {
      //ignored
    }

    try {
      domainRepositorySpy.storeDomain( EMPTY_INPUT_STREAM, null, true );
      fail( "Invalid domain id should throw exception" );
    } catch ( DomainIdNullException success ) {
      //ignored
    }

    try {
      domainRepositorySpy.storeDomain( EMPTY_INPUT_STREAM, "", true );
      fail( "Invalid domain id should throw exception" );
    } catch ( DomainIdNullException success ) {
      //ignored
    }

    // Have the XmiParser fail
    try {
      domainRepositorySpy.storeDomain( new MockDomain( "exception" ), true );
      fail( "An unexpected exception should throw a DomainStorageException" );
    } catch ( DomainStorageException success ) {
      //ignored
    }

    try {
      domainRepositorySpy.storeDomain( new ByteArrayInputStream( null ), "valid", true );
      fail( "Error with byte array input stream should throw exception" );
    } catch ( Exception success ) {
      //ignored
    }

    domainRepositorySpy.removeDomain( "steel-wheels_test" );
    domainRepositorySpy.removeDomain( STEEL_WHEELS );
    domainRepositorySpy.removeDomain( SAMPLE_DOMAIN_ID );

    final MockDomain sample = new MockDomain( SAMPLE_DOMAIN_ID );
    domainRepositorySpy.storeDomain( sample, false );
    Mockito.doReturn( true ).when( aclNodeHelper ).canAccess( Mockito.any( RepositoryFile.class ),
        Mockito.eq( EnumSet.of( RepositoryFilePermission.READ ) ) );
    final Domain domain = domainRepositorySpy.getDomain( SAMPLE_DOMAIN_ID );
    assertNotNull( domain );
    final List<LogicalModel> logicalModels = domain.getLogicalModels();
    assertNotNull( logicalModels );
    assertEquals( 0, logicalModels.size() );

    try {
      domainRepositorySpy.storeDomain( sample, false ); fail( "A duplicate domain with overwrite=false should fail" );
    } catch ( DomainAlreadyExistsException success ) {
    }

    sample.addLogicalModel( "test" );
    domainRepositorySpy.storeDomain( sample, true );
    assertEquals( 1, domainRepositorySpy.getDomain( SAMPLE_DOMAIN_ID ).getLogicalModels().size() );

    final RepositoryFile folder = domainRepositorySpy.getMetadataDir();
    assertNotNull( folder );
    assertTrue( folder.isFolder() );
    assertEquals( 1, repository.getChildren( folder.getId() ).size() );

    final Domain steelWheelsDomain = loadDomain( STEEL_WHEELS, "./steel-wheels.xmi" );
    domainRepositorySpy.storeDomain( steelWheelsDomain, true );
    assertEquals( 2, repository.getChildren( folder.getId() ).size() );
  }

  public void testGetDomain() throws Exception {
    try {
      domainRepositorySpy.getDomain( null );
      fail( "Null domainID should throw exception" );
    } catch ( Exception success ) {
      //ignored
    }

    try {
      domainRepositorySpy.getDomain( "" );
      fail( "Empty domainID should throw exception" );
    } catch ( Exception success ) {
      //ignored
    }

    Mockito.doReturn( false ).when( aclNodeHelper ).canAccess(
      Mockito.any( RepositoryFile.class ), Mockito.eq( EnumSet.of( RepositoryFilePermission.READ ) ) );
    assertNull( domainRepositorySpy.getDomain( "doesn't exist" ) );
    Mockito.doNothing().when( aclNodeHelper ).removeAclFor( Mockito.any( RepositoryFile.class ) );
    domainRepositorySpy.removeDomain( "steel-wheels_test" );
    domainRepositorySpy.removeDomain( STEEL_WHEELS );
    domainRepositorySpy.removeDomain( SAMPLE_DOMAIN_ID );

    Mockito.doReturn( true ).when( aclNodeHelper ).canAccess(
      Mockito.any( RepositoryFile.class ), Mockito.eq( EnumSet.of( RepositoryFilePermission.READ ) ) );

    final MockDomain originalDomain = new MockDomain( SAMPLE_DOMAIN_ID );
    domainRepositorySpy.storeDomain( originalDomain, false );

    final Domain testDomain1 = domainRepositorySpy.getDomain( SAMPLE_DOMAIN_ID );

    assertNotNull( testDomain1 );
    assertEquals( SAMPLE_DOMAIN_ID, testDomain1.getId() );

    originalDomain.addLogicalModel( "MODEL 1" );
    originalDomain.addLogicalModel( "MODEL 2" );
    domainRepositorySpy.storeDomain( originalDomain, true );

    final Domain testDomain2 = domainRepositorySpy.getDomain( SAMPLE_DOMAIN_ID );
    assertNotNull( testDomain2 );
    final List<LogicalModel> logicalModels = testDomain2.getLogicalModels();
    assertEquals( 2, logicalModels.size() );
    assertTrue( "MODEL 1".equals( logicalModels.get( 0 ).getId() )
        || "MODEL 1".equals( logicalModels.get( 1 ).getId() ) );
    assertTrue( "MODEL 2".equals( logicalModels.get( 0 ).getId() )
        || "MODEL 2".equals( logicalModels.get( 1 ).getId() ) );
  }

  /*
   * public void testLocalizationFiles() throws Exception { // Add some invalid localization files try {
   * domainRepository.addLocalizationFile(null, null, EMPTY_INPUT_STREAM, true);
   * fail("Invalid parameters should throw exception"); } catch (IllegalArgumentException success) { }
   * 
   * try { domainRepository.addLocalizationFile("", null, EMPTY_PROPERTIES);
   * fail("Invalid parameters should throw exception"); } catch (IllegalArgumentException success) { }
   * 
   * try { domainRepository.addLocalizationFile("", null, EMPTY_INPUT_STREAM, true);
   * fail("Invalid parameters should throw exception"); } catch (IllegalArgumentException success) { }
   * 
   * try { domainRepository.addLocalizationFile("valid", null, EMPTY_PROPERTIES);
   * fail("Invalid parameters should throw exception"); } catch (IllegalArgumentException success) { }
   * 
   * try { domainRepository.addLocalizationFile("valid", null, EMPTY_INPUT_STREAM, true);
   * fail("Invalid parameters should throw exception"); } catch (IllegalArgumentException success) { }
   * 
   * try { domainRepository.addLocalizationFile("valid", "", EMPTY_PROPERTIES);
   * fail("Invalid parameters should throw exception"); } catch (IllegalArgumentException success) { }
   * 
   * try { domainRepository.addLocalizationFile("valid", "", EMPTY_INPUT_STREAM, true);
   * fail("Invalid parameters should throw exception"); } catch (IllegalArgumentException success) { }
   * 
   * // A null properties/input stream should not throw an exception - just do nothing
   * domainRepository.addLocalizationFile(null, null, (Properties) null); domainRepository.addLocalizationFile(null,
   * null, (InputStream) null, true); domainRepository.addLocalizationFile("", null, (Properties) null);
   * domainRepository.addLocalizationFile("", null, (InputStream) null, true);
   * domainRepository.addLocalizationFile("valid", null, (Properties) null);
   * domainRepository.addLocalizationFile("valid", null, (InputStream) null, true);
   * domainRepository.addLocalizationFile("valid", "", (Properties) null); domainRepository.addLocalizationFile("valid",
   * "", (InputStream) null, true); domainRepository.addLocalizationFile("valid", "valid", (Properties) null);
   * domainRepository.addLocalizationFile("valid", "valid", (InputStream) null, true);
   * 
   * 
   * // Create a domain that starts with "steel-wheels" to try to mess up any of the following tests final String
   * notSteelWheelsDomainId = "steel-wheels_test"; domainRepository.storeDomain(new MockDomain(notSteelWheelsDomainId),
   * false); domainRepository.addLocalizationFile(notSteelWheelsDomainId, "en", EMPTY_PROPERTIES);
   * 
   * // Get the current number of files final RepositoryFile folder = domainRepository.getMetadataDir(); int fileCount =
   * repository.getChildren(folder.getId()).size();
   * 
   * // Start using a real XmiParser with real data domainRepository.setXmiParser(new MockXmiParser()); final Domain
   * steelWheels = loadDomain(STEEL_WHEELS, "./steel-wheels.xmi"); final int originalLocaleCount =
   * steelWheels.getLocales().size();
   * 
   * domainRepository.storeDomain(steelWheels, true); assertEquals(++fileCount,
   * repository.getChildren(folder.getId()).size());
   * 
   * // Correct values for the I18N tests final String defaultDescription =
   * "This model contains information about Employees."; final String esDescription =
   * "Este modelo contiene la información sobre empleados."; final String testDescription = "test description"; final
   * String descriptionKey = "[LogicalModel-BV_HUMAN_RESOURCES].[description]";
   * 
   * final Properties newProperties = new Properties(); newProperties.setProperty(descriptionKey, testDescription);
   * 
   * { final Domain steelWheelsTest = domainRepository.getDomain(STEEL_WHEELS); assertNotNull(steelWheelsTest);
   * assertEquals(steelWheels.getId(), steelWheelsTest.getId()); final LogicalModel hrModel =
   * steelWheelsTest.findLogicalModel("BV_HUMAN_RESOURCES"); assertEquals(esDescription, hrModel.getDescription("es"));
   * assertEquals(defaultDescription, hrModel.getDescription("en_US")); assertEquals(defaultDescription,
   * hrModel.getDescription("en")); assertEquals(defaultDescription, hrModel.getDescription("ru"));
   * assertEquals(defaultDescription, hrModel.getDescription("pl")); }
   * 
   * domainRepository.addLocalizationFile(STEEL_WHEELS, "ru", toInputStream(newProperties), false);
   * assertEquals(++fileCount, repository.getChildren(folder.getId()).size());
   * 
   * { // Verify that the locale was loaded final Domain steelWheelsTest = domainRepository.getDomain(STEEL_WHEELS);
   * final List<LocaleType> locales = steelWheelsTest.getLocales(); assertNotNull(locales);
   * assertEquals(originalLocaleCount + 1, locales.size()); assertEquals("en_US", locales.get(0).getCode());
   * assertEquals("es", locales.get(1).getCode()); assertEquals("ru", locales.get(2).getCode()); }
   * 
   * // Veryify the overwrite flag on the stream import try { domainRepository.addLocalizationFile(STEEL_WHEELS, "ru",
   * toInputStream(newProperties), false); fail("Should throw a DomainStorageException"); } catch
   * (DomainStorageException success) { assertEquals(fileCount, repository.getChildren(folder.getId()).size()); }
   * 
   * domainRepository.addLocalizationFile(STEEL_WHEELS, "pl", newProperties); assertEquals(++fileCount,
   * repository.getChildren(folder.getId()).size()); { final Domain steelWheelsTest =
   * domainRepository.getDomain(STEEL_WHEELS); assertNotNull(steelWheelsTest); assertEquals(steelWheels.getId(),
   * steelWheelsTest.getId()); final LogicalModel hrModel = steelWheelsTest.findLogicalModel("BV_HUMAN_RESOURCES");
   * assertEquals(esDescription, hrModel.getDescription("es")); assertEquals(defaultDescription,
   * hrModel.getDescription("en_US")); assertEquals(defaultDescription, hrModel.getDescription("en"));
   * assertEquals(testDescription, hrModel.getDescription("ru")); assertEquals(testDescription,
   * hrModel.getDescription("pl")); }
   * 
   * final String newTestDescription = "new " + testDescription; newProperties.setProperty(descriptionKey,
   * newTestDescription); domainRepository.addLocalizationFile(STEEL_WHEELS, "ru", toInputStream(newProperties), true);
   * assertEquals(fileCount, repository.getChildren(folder.getId()).size()); { final Domain steelWheelsTest =
   * domainRepository.getDomain(STEEL_WHEELS); assertNotNull(steelWheelsTest); assertEquals(steelWheels.getId(),
   * steelWheelsTest.getId()); final LogicalModel hrModel = steelWheelsTest.findLogicalModel("BV_HUMAN_RESOURCES");
   * assertEquals(esDescription, hrModel.getDescription("es")); assertEquals(defaultDescription,
   * hrModel.getDescription("en_US")); assertEquals(defaultDescription, hrModel.getDescription("en"));
   * assertEquals(newTestDescription, hrModel.getDescription("ru")); assertEquals(testDescription,
   * hrModel.getDescription("pl")); }
   * 
   * domainRepository.addLocalizationFile(STEEL_WHEELS, "en_US", newProperties); assertEquals(++fileCount,
   * repository.getChildren(folder.getId()).size()); { final Domain steelWheelsTest =
   * domainRepository.getDomain(STEEL_WHEELS); assertNotNull(steelWheelsTest); assertEquals(steelWheels.getId(),
   * steelWheelsTest.getId()); final LogicalModel hrModel = steelWheelsTest.findLogicalModel("BV_HUMAN_RESOURCES");
   * assertEquals(esDescription, hrModel.getDescription("es")); assertEquals(newTestDescription,
   * hrModel.getDescription("en_US")); assertEquals(newTestDescription, hrModel.getDescription("en"));
   * assertEquals(newTestDescription, hrModel.getDescription("ru")); assertEquals(testDescription,
   * hrModel.getDescription("pl")); } }
   */

  @Test
  public void testGetDomainIds() throws Exception {
    Mockito.doReturn( true ).when( aclNodeHelper ).canAccess( Mockito.any( RepositoryFile.class ),
      Mockito.eq( EnumSet.of( RepositoryFilePermission.READ ) ) );
    Set<String> emptyDomainList = domainRepositorySpy.getDomainIds();
    assertNotNull( emptyDomainList );

    Mockito.doReturn( false ).when( aclNodeHelper ).canAccess( Mockito.any( RepositoryFile.class ),
      Mockito.eq( EnumSet.of( RepositoryFilePermission.READ ) ) );
    emptyDomainList = domainRepositorySpy.getDomainIds();
    assertNotNull( emptyDomainList );

    Mockito.doNothing().when( aclNodeHelper ).removeAclFor( Mockito.any( RepositoryFile.class ) );
    domainRepositorySpy.removeDomain( "steel-wheels_test" );
    domainRepositorySpy.removeDomain( STEEL_WHEELS );
    domainRepositorySpy.removeDomain( SAMPLE_DOMAIN_ID );

    Mockito.doReturn( true ).when( aclNodeHelper ).canAccess( Mockito.any( RepositoryFile.class ),
      Mockito.eq( EnumSet.of( RepositoryFilePermission.READ ) ) );
    domainRepositorySpy.storeDomain( new MockDomain( SAMPLE_DOMAIN_ID ), true );
    final Set<String> domainIds1 = domainRepositorySpy.getDomainIds();

    assertNotNull( domainIds1 );
    assertEquals( 1, domainIds1.size() );
    assertTrue( domainIds1.contains( SAMPLE_DOMAIN_ID ) );
  }

  @Test
  public void testRemoveDomain() throws Exception {
    Mockito.doReturn( true ).when( aclNodeHelper ).canAccess( Mockito.any( RepositoryFile.class ),
      Mockito.eq( EnumSet.of( RepositoryFilePermission.READ ) ) );
    Mockito.doNothing().when( aclNodeHelper ).removeAclFor( Mockito.any( RepositoryFile.class ) );
    Mockito.doNothing().when( aclNodeHelper ).setAclFor(
      Mockito.any( RepositoryFile.class ), Mockito.any( RepositoryFileAcl.class ) );
    // Errors / NoOps
    try {
      domainRepositorySpy.removeDomain( null );
      fail( "should throw exception" );
    } catch ( IllegalArgumentException success ) {
      //ignore
    }

    try {
      domainRepositorySpy.removeDomain( "" );
      fail( "should throw exception" );
    } catch ( IllegalArgumentException success ) {
      //ignore
    }

    domainRepositorySpy.removeDomain( "steel-wheels_test" );
    domainRepositorySpy.removeDomain( STEEL_WHEELS );
    domainRepositorySpy.removeDomain( SAMPLE_DOMAIN_ID );


    // Create a domain that starts with "steel-wheels" to try to mess up any of the following tests
    final String notSteelWheelsDomainId = STEEL_WHEELS + "_test";
    domainRepositorySpy.storeDomain( new MockDomain( notSteelWheelsDomainId ), false );
    domainRepositorySpy.addLocalizationFile( notSteelWheelsDomainId, "en", EMPTY_PROPERTIES );

    // Get the current number of files
    final RepositoryFile folder = domainRepositorySpy.getMetadataDir();
    final int originalFileCount = repository.getChildren( folder.getId() ).size();
    int fileCount = originalFileCount;

    // Add steel-wheels and some properties files
    domainRepositorySpy.storeDomain( new MockDomain( STEEL_WHEELS ), true );
    assertEquals( ++fileCount, repository.getChildren( folder.getId() ).size() );
    domainRepositorySpy.addLocalizationFile( STEEL_WHEELS, "en", EMPTY_PROPERTIES );
    assertEquals( ++fileCount, repository.getChildren( folder.getId() ).size() );
    domainRepositorySpy.addLocalizationFile( STEEL_WHEELS, "en_US", EMPTY_PROPERTIES );
    assertEquals( ++fileCount, repository.getChildren( folder.getId() ).size() );
    domainRepositorySpy.addLocalizationFile( STEEL_WHEELS, "ru", EMPTY_PROPERTIES );
    assertEquals( ++fileCount, repository.getChildren( folder.getId() ).size() );

    // Test the delete
    domainRepositorySpy.removeDomain( "fake" );
    assertEquals( fileCount, repository.getChildren( folder.getId() ).size() );
    domainRepositorySpy.removeDomain( STEEL_WHEELS );
    assertEquals( originalFileCount, repository.getChildren( folder.getId() ).size() );
    assertNull( domainRepositorySpy.getDomain( STEEL_WHEELS ) );
    domainRepositorySpy.removeDomain( STEEL_WHEELS );
    assertEquals( originalFileCount, repository.getChildren( folder.getId() ).size() );
    Mockito.verify( domainRepositorySpy.getAclHelper(), Mockito.never() ).removeAclFor( null );
  }

  @Test
  public void testRemoveModel() throws Exception {
    // Invalid parameters
    try {
      domainRepositorySpy.removeModel( null, null );
      fail( "Should throw exception" );
    } catch ( Exception success ) {
    }

    try {
      domainRepositorySpy.removeModel( "", null );
      fail( "Should throw exception" );
    } catch ( Exception success ) {
    }

    try {
      domainRepositorySpy.removeModel( "valid", null );
      fail( "Should throw exception" );
    } catch ( Exception success ) {
    }

    try {
      domainRepositorySpy.removeModel( "valid", "" );
      fail( "Should throw exception" );
    } catch ( Exception success ) {
    }

    // Deleting a model from a domain that doesn't exist should not throw exception
    domainRepositorySpy.removeModel( "does not exist", "does not exist" );

    // Use a real XmiParser with real data
    domainRepositorySpy.setXmiParser( new MockXmiParser() );
    domainRepositorySpy.storeDomain( loadDomain( STEEL_WHEELS, "./steel-wheels.xmi" ), true );
    Mockito.doReturn( true ).when( aclNodeHelper ).canAccess( Mockito.any( RepositoryFile.class ),
      Mockito.eq( EnumSet.of( RepositoryFilePermission.READ ) ) );
    Mockito.doNothing().when( domainRepositorySpy ).loadLocaleStrings(
      Mockito.anyString(), Mockito.any( Domain.class ) );
    final Domain steelWheels = domainRepositorySpy.getDomain( STEEL_WHEELS );
    assertNotNull( steelWheels );

    final String validModelName = "BV_HUMAN_RESOURCES";

    { // Can't delete a model that doesn't exist
      domainRepositorySpy.removeModel( STEEL_WHEELS, "no such model" );
      final Domain test = domainRepositorySpy.getDomain( STEEL_WHEELS );
      assertNotNull( test );
      assertEquals( steelWheels.getPhysicalModels().size(), test.getPhysicalModels().size() );
      assertEquals( steelWheels.getLogicalModels().size(), test.getLogicalModels().size() );
      assertNotNull( getLogicalModelByName( test, validModelName ) );
    }

    {
      // Delete a model that does exist
      // NOTE: if caching is enabled, the original STEEL_WHEELS is cached and is the one that will be modified ...
      // ... so we need to flush the cache to prevent that from happening
      domainRepositorySpy.flushDomains();
      domainRepositorySpy.removeModel( STEEL_WHEELS, validModelName );
      final Domain test = domainRepositorySpy.getDomain( STEEL_WHEELS );
      assertNotNull( test );
      assertNull( getLogicalModelByName( test, validModelName ) );
    }
  }

  private static LogicalModel getLogicalModelByName( final Domain domain, final String logicalModelName ) {
    for ( final LogicalModel logicalModel : domain.getLogicalModels() ) {
      if ( StringUtils.equals( logicalModelName, logicalModel.getId() ) ) {
        return logicalModel;
      }
    }
    return null;
  }

  @Test
  public void testReloadDomains() throws Exception {
    domainRepositorySpy.reloadDomains();
  }

  @Test
  public void testFlushDomains() throws Exception {
    domainRepositorySpy.flushDomains();
  }

  @Test
  public void testGenerateRowLevelSecurityConstraint() throws Exception {
    domainRepositorySpy.generateRowLevelSecurityConstraint( null );
  }

  @Test
  public void testHasAccess() throws Exception {
    domainRepositorySpy.hasAccess( 0, null );
  }

  @Test
  public void testToString() throws Exception {
    // Neither case should throw an exception
    domainRepositorySpy.toString( null );
    domainRepositorySpy.toString( new RepositoryFile.Builder( "" ).build() );
    domainRepositorySpy.toString( repository.getFile( "/etc/metadata" ) );
  }

  @Test
  public void testLoadLocaleStrings() throws Exception {
    // Add a domain with no external locale information
    domainRepositorySpy.setXmiParser( new XmiParser() );
    final Domain steelWheels = loadDomain( STEEL_WHEELS, "./steel-wheels.xmi" );
    domainRepositorySpy.storeDomain( steelWheels, true );

    final int initialLocaleSize = steelWheels.getLocaleCodes().length;
    assertEquals( initialLocaleSize, steelWheels.getLocales().size() );
    domainRepositorySpy.loadLocaleStrings( STEEL_WHEELS, steelWheels );
    assertEquals( initialLocaleSize, steelWheels.getLocaleCodes().length );
    assertEquals( initialLocaleSize, steelWheels.getLocales().size() );

    final Properties newLocale = new Properties();
    newLocale.put( "[LogicalModel-BV_HUMAN_RESOURCES].[description]", "New Description in Italian" );
    domainRepositorySpy.addLocalizationFile( STEEL_WHEELS, "it_IT", newLocale );

    domainRepositorySpy.loadLocaleStrings( STEEL_WHEELS, steelWheels );
    final int newLocaleSize = initialLocaleSize + 1;
    assertEquals( newLocaleSize, steelWheels.getLocales().size() );
    domainRepositorySpy.loadLocaleStrings( STEEL_WHEELS, steelWheels );
    assertEquals( newLocaleSize, steelWheels.getLocaleCodes().length );
    assertEquals( newLocaleSize, steelWheels.getLocales().size() );
  }

  @Test
  public void testSetXmiParser() throws Exception {
    domainRepositorySpy.setXmiParser( null );
    domainRepositorySpy.setXmiParser( new XmiParser() );
  }

  @Test
  public void testStoreAnnotationsXmlSkipped() throws Exception {

    String domainId = "test.xmi";
    String annotationsXml = "<annotations/>";

    domainRepositorySpy.storeAnnotationsXml( null, null );
    Mockito.verify( domainRepositorySpy, Mockito.times( 0 ) ).getRepository(); // nothing happened, skipped

    domainRepositorySpy.storeAnnotationsXml( null, annotationsXml );
    Mockito.verify( domainRepositorySpy, Mockito.times( 0 ) ).getRepository(); // nothing happened, skipped

    domainRepositorySpy.storeAnnotationsXml( domainId, null );
    Mockito.verify( domainRepositorySpy, Mockito.times( 0 ) ).getRepository(); // nothing happened, skipped

    Mockito.doReturn( null ).when( domainRepositorySpy ).getMetadataRepositoryFile( domainId );
    domainRepositorySpy.storeAnnotationsXml( domainId, annotationsXml );
    Mockito.verify( domainRepositorySpy, Mockito.times( 0 ) ).getRepository(); // nothing happened, skipped
  }

  @Test
  public void testStoreAnnotationsXml() throws Exception {

    String domainId = "test.xmi";
    String annotationsXml = "<annotations/>";

    domainRepositorySpy.storeAnnotationsXml( null, null );
    Mockito.verify( domainRepositorySpy, Mockito.times( 0 ) ).getMetadataRepositoryFile( Mockito.anyString() );

    domainRepositorySpy.storeAnnotationsXml( domainId, null );
    Mockito.verify( domainRepositorySpy, Mockito.times( 0 ) ).getMetadataRepositoryFile( Mockito.anyString() );

    domainRepositorySpy.storeAnnotationsXml( null, annotationsXml );
    Mockito.verify( domainRepositorySpy, Mockito.times( 0 ) ).getMetadataRepositoryFile( Mockito.anyString() );

    domainRepositorySpy.storeAnnotationsXml( domainId, annotationsXml );
    Mockito.verify( domainRepositorySpy, Mockito.times( 1 ) ).getMetadataRepositoryFile( Mockito.anyString() );
    Mockito.verify( domainRepositorySpy, Mockito.times( 1 ) )
      .getAnnotationsXmlFile( Mockito.any( RepositoryFile.class ) );
    Mockito.verify( domainRepositorySpy, Mockito.times( 1 ) )
      .createOrUpdateAnnotationsXml(
        Mockito.any( RepositoryFile.class ), Mockito.any( RepositoryFile.class ), Mockito.anyString() );
  }

  @Test
  public void testCreateOrUpdateAnnotationsXml() throws Exception {

    String metadataDirId = "00000000";
    String annotationsXml = "<annotations/>";
    RepositoryFile metaDataDir = Mockito.mock( RepositoryFile.class );
    IUnifiedRepository repository = Mockito.mock( IUnifiedRepository.class );
    Log logger = Mockito.mock( Log.class );

    Mockito.doReturn( logger ).when( domainRepositorySpy ).getLogger();
    Mockito.doReturn( repository ).when( domainRepositorySpy ).getRepository();
    Mockito.doReturn( metadataDirId ).when( metaDataDir ).getId();
    Mockito.doReturn( metaDataDir ).when( domainRepositorySpy ).getMetadataDir();

    // Domain Not Found
    domainRepositorySpy.createOrUpdateAnnotationsXml( null, null, annotationsXml );
    Mockito.verify( domainRepositorySpy, Mockito.times( 0 ) ).getRepository();

    RepositoryFile domainFile = Mockito.mock( RepositoryFile.class );

    // Create
    domainRepositorySpy.createOrUpdateAnnotationsXml( domainFile, null, annotationsXml );
    Mockito.verify( repository, Mockito.times( 1 ) ).createFile(
      Mockito.any( String.class ), Mockito.any( RepositoryFile.class ), Mockito.any( IRepositoryFileData.class ),
        Mockito.any( String.class ) );

    // Update
    RepositoryFile annotationsFile = Mockito.mock( RepositoryFile.class );
    domainRepositorySpy.createOrUpdateAnnotationsXml( domainFile, annotationsFile, annotationsXml );
    Mockito.verify( repository, Mockito.times( 1 ) ).updateFile(
      Mockito.any( RepositoryFile.class ), Mockito.any( IRepositoryFileData.class ), Mockito.any( String.class ) );

    // Error
    Mockito.doThrow( new RuntimeException() ).when( domainRepositorySpy ).getRepository();
    domainRepositorySpy.createOrUpdateAnnotationsXml( domainFile, annotationsFile, annotationsXml );
    Mockito.verify( logger, Mockito.times( 1 ) ).warn( Mockito.any(), Mockito.any( Throwable.class ) );
  }

  @Test
  public void testGetAnnotationsXmlFile() throws Exception {

    String domainFileId = "00000000";

    assertNull( domainRepositorySpy.getAnnotationsXmlFile( null ) );

    Log logger = Mockito.mock( Log.class );
    Mockito.doReturn( logger ).when( domainRepositorySpy ).getLogger();

    IUnifiedRepository repository = Mockito.mock( IUnifiedRepository.class );
    Mockito.doReturn( repository ).when( domainRepositorySpy ).getRepository();

    RepositoryFile domainFile = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( domainFileId ).when( domainFile ).getId();

    // Not Found
    Mockito.doReturn( domainFile ).when( repository ).getFileById( "someOtherId" );
    assertNull( domainRepositorySpy.getAnnotationsXmlFile( domainFile ) );

    // Found
    String annotationsFilePath =
        "/etc/metadata/" + domainFileId
            + IModelAnnotationsAwareMetadataDomainRepositoryImporter.ANNOTATIONS_FILE_ID_POSTFIX;
    Mockito.doReturn( domainFile ).when( repository ).getFile( annotationsFilePath );
    assertNotNull( domainRepositorySpy.getAnnotationsXmlFile( domainFile ) );

    // Error
    Mockito.doThrow( new RuntimeException() ).when( domainRepositorySpy ).getRepository();
    assertNull( domainRepositorySpy.getAnnotationsXmlFile( domainFile ) );
    Mockito.verify( logger, Mockito.times( 1 ) )
      .warn( "Unable to find annotations xml file for: " + domainFile.getId() );
  }

  @Test
  public void testLoadAnnotationsXml() throws Exception {

    String domainId = "test.xmi";
    String domainFileId = "00000000";
    String annotationsId =
        domainFileId + IModelAnnotationsAwareMetadataDomainRepositoryImporter.ANNOTATIONS_FILE_ID_POSTFIX;
    String annotationsXml = "<annotations/>";
    Log logger = Mockito.mock( Log.class );

    Mockito.doReturn( logger ).when( domainRepositorySpy ).getLogger();

    assertNull( domainRepositorySpy.loadAnnotationsXml( null ) );
    assertNull( domainRepositorySpy.loadAnnotationsXml( "" ) );

    IUnifiedRepository repository = Mockito.mock( IUnifiedRepository.class );
    Mockito.doReturn( repository ).when( domainRepositorySpy ).getRepository();

    // Success
    RepositoryFile domainFile = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( domainFile ).when( domainRepositorySpy ).getMetadataRepositoryFile( domainId );
    Mockito.doReturn( domainFileId ).when( domainFile ).getId();

    RepositoryFile annotationsFile = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( annotationsFile ).when( repository ).getFile( "/etc/metadata/" + annotationsId );
    Mockito.doReturn( annotationsId ).when( annotationsFile ).getId();

    SimpleRepositoryFileData data = Mockito.mock( SimpleRepositoryFileData.class );
    Mockito.doReturn( data ).when( repository ).getDataForRead( annotationsId, SimpleRepositoryFileData.class );
    Mockito.doReturn( IOUtils.toInputStream( annotationsXml ) ).when( data ).getInputStream();

    assertEquals( annotationsXml, domainRepositorySpy.loadAnnotationsXml( domainId ) );

    // Error
    Mockito.doThrow( new RuntimeException() ).when( data ).getInputStream();
    domainRepositorySpy.loadAnnotationsXml( domainId );
    Mockito.verify( logger, Mockito.times( 1 ) ).warn( "Unable to load annotations xml file for domain: test.xmi" );
  }

  @Test
  public void testResolveAnnotationsFilePath() {

    assertNull( domainRepositorySpy.resolveAnnotationsFilePath( null ) );

    String domainFileId = "00000000";
    String annotationsId =
        domainFileId + IModelAnnotationsAwareMetadataDomainRepositoryImporter.ANNOTATIONS_FILE_ID_POSTFIX;

    RepositoryFile domainFile = Mockito.mock( RepositoryFile.class );
    Mockito.doReturn( domainFileId ).when( domainFile ).getId();

    String actualPath = domainRepositorySpy.resolveAnnotationsFilePath( domainFile );
    assertEquals( "/etc/metadata/" + annotationsId, FilenameUtils.separatorsToUnix( actualPath ) );
  }

  @Test
  public void testEndsWithXmi() {
    PentahoMetadataDomainRepository repository =
      new PentahoMetadataDomainRepository( Mockito.mock( IUnifiedRepository.class ) );
    assertEquals( "domainId.xmi", repository.endsWithXmi( "domainId" ) );
    assertEquals( "domainId.xmi", repository.endsWithXmi( "domainId.xmi" ) );
  }

  @Test
  public void testNoXmi() {
    PentahoMetadataDomainRepository repository =
      new PentahoMetadataDomainRepository( Mockito.mock( IUnifiedRepository.class ) );
    assertEquals( "domainId", repository.noXmi( "domainId" ) );
    assertEquals( "domainId", repository.noXmi( "domainId.xmi" ) );
  }

  @Test
  public void testGetDomainIdFromXmi() {
    PentahoMetadataDomainRepository repository =
      new PentahoMetadataDomainRepository( Mockito.mock( IUnifiedRepository.class ) );
    String xmiTemplate =
        "<CWM:Description body=\"body_of_datasource\" name=\"datasourceModel\" type=\"String\" xmi.id=\"a68\"><CWM:Description.modelElement><CWMMDB:Schema xmi.idref=\"a64\"/></CWM:Description.modelElement></CWM:Description><CWM:Description body=\"{datasourceName}\" language=\"en_US\" name=\"name\" type=\"LocString\" xmi.id=\"a69\"><CWM:Description.modelElement><CWMMDB:Schema xmi.idref=\"a64\"/></CWM:Description.modelElement></CWM:Description>";

    String xmiString = xmiTemplate.replace( "{datasourceName}", "datasource-name" );
    StringBuilder xmi = new StringBuilder( xmiString );
    String domainIdXmi = repository.getDomainIdFromXmi( xmi );
    assertEquals( "datasource-name", domainIdXmi );

    xmiString = xmiTemplate.replace( "{datasourceName}", "datasource-name" ).replace( "datasourceModel", "empty" );
    xmi = new StringBuilder( xmiString );
    assertNull( repository.getDomainIdFromXmi( xmi ) );

    String xmiTemplate2 = "<CWM:Description xmi.id = 'a149' name = 'datasourceModel' body = '&lt;org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO&gt;&#10;  &lt;datasourceName&gt;table_rep&lt;/datasourceName&gt;&#10;  &lt;selectedConnection class=&quot;org.pentaho.database.model.DatabaseConnection&quot;&gt;&#10;    &lt;id&gt;6a215f65-fee8-4aae-b75a-59349042045f&lt;/id&gt;&#10;    &lt;name&gt;SampleData&lt;/name&gt;&#10;    &lt;databaseName&gt;SampleData&lt;/databaseName&gt;&#10;    &lt;databasePort&gt;9001&lt;/databasePort&gt;&#10;    &lt;hostname&gt;localhost&lt;/hostname&gt;&#10;    &lt;username&gt;pentaho_user&lt;/username&gt;&#10;    &lt;dataTablespace&gt;&lt;/dataTablespace&gt;&#10;    &lt;indexTablespace&gt;&lt;/indexTablespace&gt;&#10;    &lt;streamingResults&gt;false&lt;/streamingResults&gt;&#10;    &lt;quoteAllFields&gt;false&lt;/quoteAllFields&gt;&#10;    &lt;changed&gt;false&lt;/changed&gt;&#10;    &lt;usingDoubleDecimalAsSchemaTableSeparator&gt;false&lt;/usingDoubleDecimalAsSchemaTableSeparator&gt;&#10;    &lt;informixServername&gt;&lt;/informixServername&gt;&#10;    &lt;forcingIdentifiersToLowerCase&gt;false&lt;/forcingIdentifiersToLowerCase&gt;&#10;    &lt;forcingIdentifiersToUpperCase&gt;false&lt;/forcingIdentifiersToUpperCase&gt;&#10;    &lt;connectSql&gt;&lt;/connectSql&gt;&#10;    &lt;usingConnectionPool&gt;true&lt;/usingConnectionPool&gt;&#10;    &lt;accessTypeValue&gt;NATIVE&lt;/accessTypeValue&gt;&#10;    &lt;accessType&gt;NATIVE&lt;/accessType&gt;&#10;    &lt;databaseType class=&quot;org.pentaho.database.model.DatabaseType&quot;&gt;&#10;      &lt;name&gt;Hypersonic&lt;/name&gt;&#10;      &lt;shortName&gt;HYPERSONIC&lt;/shortName&gt;&#10;      &lt;defaultPort&gt;9001&lt;/defaultPort&gt;&#10;      &lt;supportedAccessTypes&gt;&#10;        &lt;org.pentaho.database.model.DatabaseAccessType&gt;NATIVE&lt;/org.pentaho.database.model.DatabaseAccessType&gt;&#10;        &lt;org.pentaho.database.model.DatabaseAccessType&gt;ODBC&lt;/org.pentaho.database.model.DatabaseAccessType&gt;&#10;        &lt;org.pentaho.database.model.DatabaseAccessType&gt;JNDI&lt;/org.pentaho.database.model.DatabaseAccessType&gt;&#10;      &lt;/supportedAccessTypes&gt;&#10;      &lt;extraOptionsHelpUrl&gt;http://hsqldb.sourceforge.net/doc/guide/ch04.html#N109DA&lt;/extraOptionsHelpUrl&gt;&#10;    &lt;/databaseType&gt;&#10;    &lt;extraOptions/&gt;&#10;    &lt;extraOptionsOrder/&gt;&#10;    &lt;attributes&gt;&#10;      &lt;entry&gt;&#10;        &lt;string&gt;PORT_NUMBER&lt;/string&gt;&#10;        &lt;string&gt;9001&lt;/string&gt;&#10;      &lt;/entry&gt;&#10;    &lt;/attributes&gt;&#10;    &lt;connectionPoolingProperties/&gt;&#10;    &lt;initialPoolSize&gt;0&lt;/initialPoolSize&gt;&#10;    &lt;maxPoolSize&gt;20&lt;/maxPoolSize&gt;&#10;    &lt;partitioned&gt;false&lt;/partitioned&gt;&#10;  &lt;/selectedConnection&gt;&#10;  &lt;schemaModel&gt;&#10;    &lt;joins class=&quot;org.pentaho.ui.xul.util.AbstractModelList&quot;&gt;&#10;      &lt;children/&gt;&#10;    &lt;/joins&gt;&#10;  &lt;/schemaModel&gt;&#10;  &lt;selectedTables&gt;&#10;    &lt;string&gt;&amp;quot;PUBLIC&amp;quot;.&amp;quot;CUSTOMERS&amp;quot;&lt;/string&gt;&#10;  &lt;/selectedTables&gt;&#10;  &lt;doOlap&gt;false&lt;/doOlap&gt;&#10;&lt;/org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO&gt;' type = 'String'><CWM:Description.modelElement><CWMMDB:Schema xmi.idref = 'a140'/></CWM:Description.modelElement></CWM:Description><CWM:Description xmi.id = 'a150' name = 'name' body = 'CUSTOMERS' language = 'en_US'type = 'LocString'><CWM:Description.modelElement><CWMMDB:Dimension xmi.idref = 'a151'/></CWM:Description.modelElement></CWM:Description>";
    repository.getDomainIdFromXmi( new StringBuilder( xmiTemplate2 ) );
  }

  @Test
  public void testIsDomainIdXmiEqualsOrNotPresent() {
    PentahoMetadataDomainRepository repository =
      new PentahoMetadataDomainRepository( Mockito.mock( IUnifiedRepository.class ) );
    assertEquals( true, repository.isDomainIdXmiEqualsOrNotPresent( "someDomainId", null ) );
    assertEquals( true, repository.isDomainIdXmiEqualsOrNotPresent( "someDomainId.xmi", null ) );
    assertEquals( true, repository.isDomainIdXmiEqualsOrNotPresent( "someDomainId.xmi", "someDomainId.xmi" ) );
    assertEquals( true, repository.isDomainIdXmiEqualsOrNotPresent( "someDomainId", "someDomainId" ) );
    assertEquals( true, repository.isDomainIdXmiEqualsOrNotPresent( "someDomainId.xmi", "someDomainId" ) );
    assertEquals( true, repository.isDomainIdXmiEqualsOrNotPresent( "someDomainId", "someDomainId.xmi" ) );
    assertEquals( false, repository.isDomainIdXmiEqualsOrNotPresent( "someDomain1", "someDomainId2" ) );
    assertEquals( false, repository.isDomainIdXmiEqualsOrNotPresent( "someDomain1", "someDomainId2.xmi" ) );
  }

  @Test
  public void testReplaceDomainId() {
    PentahoMetadataDomainRepository repository =
      new PentahoMetadataDomainRepository( Mockito.mock( IUnifiedRepository.class ) );
    String xmiTemplate =
        "<CWM:Description body=\"body_of_datasource\" name=\"datasourceModel\" type=\"String\" xmi.id=\"a68\"><CWM:Description.modelElement><CWMMDB:Schema xmi.idref=\"a64\"/></CWM:Description.modelElement></CWM:Description><CWM:Description body=\"{datasourceName}\" language=\"en_US\" name=\"name\" type=\"LocString\" xmi.id=\"a69\"><CWM:Description.modelElement><CWMMDB:Schema xmi.idref=\"a64\"/></CWM:Description.modelElement></CWM:Description>";

    // Import from metadata
    StringBuilder sb = new StringBuilder( xmiTemplate.replace( "{datasourceName}", "ds.xmi" ) );
    String result = repository.replaceDomainId( sb, "ds" );
    assertEquals( "ds.xmi", result );
    assertEquals( "ds.xmi", repository.getDomainIdFromXmi( sb ) );

    sb = new StringBuilder( xmiTemplate.replace( "{datasourceName}", "ds" ) );
    result = repository.replaceDomainId( sb, "ds" );
    assertEquals( "ds.xmi", result );
    assertEquals( "ds", repository.getDomainIdFromXmi( sb ) );

    // Import from metadata 2
    sb = new StringBuilder( xmiTemplate.replace( "{datasourceName}", "ds-oldName.xmi" ) );
    result = repository.replaceDomainId( sb, "ds" );
    assertEquals( "ds.xmi", result );
    assertEquals( "ds.xmi", repository.getDomainIdFromXmi( sb ) );

    // Create new data source
    sb = new StringBuilder( xmiTemplate.replace( "{datasourceName}", "ds-name" ) );
    result = repository.replaceDomainId( sb, "ds-name.xmi" );
    assertEquals( "ds-name.xmi", result );
    assertEquals( "ds-name", repository.getDomainIdFromXmi( sb ) );

    // Import from metadata with special character
    sb = new StringBuilder( xmiTemplate.replace( "{datasourceName}", "ds<ds.xmi" ) );
    result = repository.replaceDomainId( sb, "ds<ds" );
    assertEquals( "ds<ds.xmi", result );
    assertEquals( "ds<ds.xmi", repository.getDomainIdFromXmi( sb ) );
  }

  private InputStream toInputStream( final Properties newProperties ) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      newProperties.store( out, null );
      return new ByteArrayInputStream( out.toByteArray() );
    } catch ( IOException e ) {
      fail();
    }
    return null;
  }

  /**
   * Mock Domain object used for testing
   */
  private class MockDomain extends Domain {
    private String id;
    private List<LogicalModel> logicalModels;
    private List<LocaleType> locals;

    public MockDomain( final String id ) {
      this.id = id;
      logicalModels = new ArrayList<LogicalModel>();
      locals = new ArrayList<LocaleType>();
    }

    public String getId() {
      return id;
    }

    public List<LogicalModel> getLogicalModels() {
      return logicalModels;
    }

    public void addLogicalModel( String modelId ) {
      logicalModels.add( new MockLogicalModel( modelId ) );
    }

    private class MockLogicalModel extends LogicalModel {
      final String modelId;

      public MockLogicalModel( final String modelId ) {
        this.modelId = modelId;
      }

      public String getId() {
        return modelId;
      }
    }
  }

  /**
   * Mock XMI Parser for testing
   */
  private class MockXmiParser extends XmiParser {
    public String generateXmi( final Domain domain ) {
      if ( domain.getId().equals( "exception" ) ) {
        throw new NullPointerException();
      }
      StringBuilder sb = new StringBuilder( domain.getId() );
      final List<LogicalModel> logicalModels = domain.getLogicalModels();
      if ( null != logicalModels ) {
        for ( LogicalModel model : logicalModels ) {
          sb.append( '|' ).append( model.getId() );
        }
      }
      return sb.toString();
    }

    public Domain parseXmi( final InputStream xmi ) throws Exception {
      String[] lines = IOUtils.toString( xmi ).split( "\\|" );

      final MockDomain domain = new MockDomain( lines[0] );
      for ( int i = 1; i < lines.length; ++i ) {
        domain.addLogicalModel( lines[i] );
      }
      if ( domain.getId().equals( "exception" ) ) {
        throw new NullPointerException();
      }
      return domain;
    }
  }

  /**
   * Loads a "real" Pentaho Metadata Domain
   *
   * @param domainId
   * @param domainFile
   * @return
   * @throws Exception
   */
  private static final Domain loadDomain( final String domainId, final String domainFile ) throws Exception {
    final InputStream in = PentahoMetadataDomainRepositoryTest.class.getResourceAsStream( domainFile );
    final XmiParser parser = new XmiParser();
    final Domain domain = parser.parseXmi( in );
    domain.setId( domainId );
    IOUtils.closeQuietly( in );
    return domain;
  }

  /**
   *
   */
  private class MockUserProvider implements MockUnifiedRepository.ICurrentUserProvider {
    public String getUser() {
      return MockUnifiedRepository.root().getName();
    }

    public List<String> getRoles() {
      return new ArrayList<String>();
    }
  }
}
