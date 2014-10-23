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

package org.pentaho.platform.plugin.services.metadata;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.types.LocaleType;
import org.pentaho.metadata.util.LocalizationUtil;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.RepositoryUtils;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

  IUnifiedRepository repository = new FileSystemBackedUnifiedRepository();
  private PentahoMetadataDomainRepository domainRepository;

  protected PentahoMetadataDomainRepository createDomainRepository( final IUnifiedRepository repository ) {
    return new PentahoMetadataDomainRepository( repository );
  }

  protected PentahoMetadataDomainRepository createDomainRepository( final IUnifiedRepository repository,
                                                                    final RepositoryUtils repositoryUtils, final XmiParser xmiParser, final LocalizationUtil localizationUtil ) {
    return new PentahoMetadataDomainRepository( repository, repositoryUtils, xmiParser, localizationUtil );
  }

  public void setUp() throws Exception {
    // File tempDir = File.createTempFile("test", "");
    // tempDir.delete();
    // tempDir.mkdir();
    // System.err.println("tempDir = " + tempDir.getAbsolutePath());
    // repository = new FileSystemBackedUnifiedRepository(tempDir);
    // new RepositoryUtils(repository).getFolder("/etc/metadata", true, true, null);
    repository.deleteFile( new RepositoryUtils( repository ).getFolder( "/etc/metadata", true, true, null ).getId(), true, null );
    assertNotNull( new RepositoryUtils( repository ).getFolder( "/etc/metadata", true, true, null ) );

    final MockXmiParser xmiParser = new MockXmiParser();
    domainRepository = createDomainRepository( repository, null, xmiParser, null );
    while ( domainRepository.getDomainIds().size() > 0 ) {
      domainRepository.removeDomain( domainRepository.getDomainIds().iterator().next() );
    }
  }

  public void tearDown() throws Exception {
    repository = null;
    domainRepository = null;
  }

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
    final PentahoMetadataDomainRepository repo =
        createDomainRepository( repository, repositoryUtils, xmiParser, localizationUtil );
    assertEquals( repository, repo.getRepository() );
    assertEquals( repositoryUtils, repo.getRepositoryUtils() );
    assertEquals( xmiParser, repo.getXmiParser() );
    assertEquals( localizationUtil, repo.getLocalizationUtil() );

  }

  /*
   * public void testStoreDomain() throws Exception { try { domainRepository.storeDomain(null, true);
   * fail("Invalid domain should throw exception"); } catch (DomainIdNullException success) { }
   * 
   * try { domainRepository.storeDomain(new MockDomain(null), true); fail("Null domain id should throw exception"); }
   * catch (DomainIdNullException success) { }
   * 
   * try { domainRepository.storeDomain(new MockDomain(""), true); fail("Empty domain id should throw exception"); }
   * catch (DomainIdNullException success) { }
   * 
   * try { domainRepository.storeDomain(null, null, true); fail("Null InputStream should throw an exception"); } catch
   * (IllegalArgumentException success) { }
   * 
   * try { domainRepository.storeDomain(null, "", true); fail("Null InputStream should throw an exception"); } catch
   * (IllegalArgumentException success) { }
   * 
   * try { domainRepository.storeDomain(null, "valid", true); fail("Null InputStream should throw an exception"); }
   * catch (IllegalArgumentException success) { }
   * 
   * try { domainRepository.storeDomain(EMPTY_INPUT_STREAM, null, true);
   * fail("Invalid domain id should throw exception"); } catch (DomainIdNullException success) { }
   * 
   * try { domainRepository.storeDomain(EMPTY_INPUT_STREAM, "", true); fail("Invalid domain id should throw exception");
   * } catch (DomainIdNullException success) { }
   * 
   * // Have the XmiParser fail try { domainRepository.storeDomain(new MockDomain("exception"), true);
   * fail("An unexpected exception should throw a DomainStorageException"); } catch (DomainStorageException success) { }
   * 
   * try { domainRepository.storeDomain(new ByteArrayInputStream(null), "valid", true);
   * fail("Error with byte array input stream should throw exception"); } catch (Exception success) { }
   * domainRepository.removeDomain("steel-wheels_test"); domainRepository.removeDomain(STEEL_WHEELS);
   * domainRepository.removeDomain(SAMPLE_DOMAIN_ID);
   * 
   * final MockDomain sample = new MockDomain(SAMPLE_DOMAIN_ID); domainRepository.storeDomain(sample, false); final
   * Domain domain = domainRepository.getDomain(SAMPLE_DOMAIN_ID); assertNotNull(domain); final List<LogicalModel>
   * logicalModels = domain.getLogicalModels(); assertNotNull(logicalModels); assertEquals(0, logicalModels.size());
   * 
   * try { domainRepository.storeDomain(sample, false); fail("A duplicate domain with overwrite=false should fail"); }
   * catch (DomainAlreadyExistsException success) { }
   * 
   * sample.addLogicalModel("test"); domainRepository.storeDomain(sample, true); assertEquals(1,
   * domainRepository.getDomain(SAMPLE_DOMAIN_ID).getLogicalModels().size());
   * 
   * final RepositoryFile folder = domainRepository.getMetadataDir(); assertNotNull(folder);
   * assertTrue(folder.isFolder()); assertEquals(1, repository.getChildren(folder.getId()).size());
   * 
   * final Domain steelWheelsDomain = loadDomain(STEEL_WHEELS, "./steel-wheels.xmi");
   * domainRepository.storeDomain(steelWheelsDomain, true); assertEquals(2,
   * repository.getChildren(folder.getId()).size()); }
   */

  public void testGetDomain() throws Exception {
    try {
      domainRepository.getDomain( null );
      fail( "Null domainID should throw exception" );
    } catch ( Exception success ) {
      //ignored
    }

    try {
      domainRepository.getDomain( "" );
      fail( "Empty domainID should throw exception" );
    } catch ( Exception success ) {
      //ignored
    }

    assertNull( domainRepository.getDomain( "doesn't exist" ) );
    domainRepository.removeDomain( "steel-wheels_test" );
    domainRepository.removeDomain( STEEL_WHEELS );
    domainRepository.removeDomain( SAMPLE_DOMAIN_ID );

    final MockDomain originalDomain = new MockDomain( SAMPLE_DOMAIN_ID );
    domainRepository.storeDomain( originalDomain, false );
    final Domain testDomain1 = domainRepository.getDomain( SAMPLE_DOMAIN_ID );
    assertNotNull( testDomain1 );
    assertEquals( SAMPLE_DOMAIN_ID, testDomain1.getId() );

    originalDomain.addLogicalModel( "MODEL 1" );
    originalDomain.addLogicalModel( "MODEL 2" );
    domainRepository.storeDomain( originalDomain, true );

    final Domain testDomain2 = domainRepository.getDomain( SAMPLE_DOMAIN_ID );
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
  public void testGetDomainIds() throws Exception {
    final Set<String> emptyDomainList = domainRepository.getDomainIds();
    assertNotNull( emptyDomainList );

    domainRepository.removeDomain( "steel-wheels_test" );
    domainRepository.removeDomain( STEEL_WHEELS );
    domainRepository.removeDomain( SAMPLE_DOMAIN_ID );

    domainRepository.storeDomain( new MockDomain( SAMPLE_DOMAIN_ID ), true );
    final Set<String> domainIds1 = domainRepository.getDomainIds();
    assertNotNull( domainIds1 );
    assertEquals( 1, domainIds1.size() );
    assertTrue( domainIds1.contains( SAMPLE_DOMAIN_ID ) );
  }

  public void testRemoveDomain() throws Exception {
    // Errors / NoOps
    try {
      domainRepository.removeDomain( null );
      fail( "should throw exception" );
    } catch ( IllegalArgumentException success ) {
      //ignore
    }

    try {
      domainRepository.removeDomain( "" );
      fail( "should throw exception" );
    } catch ( IllegalArgumentException success ) {
      //ignore
    }

    // Create a domain that starts with "steel-wheels" to try to mess up any of the following tests
    final String notSteelWheelsDomainId = STEEL_WHEELS + "_test";
    domainRepository.storeDomain( new MockDomain( notSteelWheelsDomainId ), false );
    domainRepository.addLocalizationFile( notSteelWheelsDomainId, "en", EMPTY_PROPERTIES );

    // Get the current number of files
    final RepositoryFile folder = domainRepository.getMetadataDir();
    final int originalFileCount = repository.getChildren( folder.getId() ).size();
    int fileCount = originalFileCount;

    // Add steel-wheels and some properties files
    domainRepository.storeDomain( new MockDomain( STEEL_WHEELS ), true );
    assertEquals( ++fileCount, repository.getChildren( folder.getId() ).size() );
    domainRepository.addLocalizationFile( STEEL_WHEELS, "en", EMPTY_PROPERTIES );
    assertEquals( ++fileCount, repository.getChildren( folder.getId() ).size() );
    domainRepository.addLocalizationFile( STEEL_WHEELS, "en_US", EMPTY_PROPERTIES );
    assertEquals( ++fileCount, repository.getChildren( folder.getId() ).size() );
    domainRepository.addLocalizationFile( STEEL_WHEELS, "ru", EMPTY_PROPERTIES );
    assertEquals( ++fileCount, repository.getChildren( folder.getId() ).size() );

    // Test the delete
    domainRepository.removeDomain( "fake" );
    assertEquals( fileCount, repository.getChildren( folder.getId() ).size() );
    domainRepository.removeDomain( STEEL_WHEELS );
    assertEquals( originalFileCount, repository.getChildren( folder.getId() ).size() );
    assertNull( domainRepository.getDomain( STEEL_WHEELS ) );
    domainRepository.removeDomain( STEEL_WHEELS );
    assertEquals( originalFileCount, repository.getChildren( folder.getId() ).size() );
  }

  /*
   * public void testRemoveModel() throws Exception { // Invalid parameters try { domainRepository.removeModel(null,
   * null); fail("Should throw exception"); } catch (Exception success) { }
   * 
   * try { domainRepository.removeModel("", null); fail("Should throw exception"); } catch (Exception success) { }
   * 
   * try { domainRepository.removeModel("valid", null); fail("Should throw exception"); } catch (Exception success) { }
   * 
   * try { domainRepository.removeModel("valid", ""); fail("Should throw exception"); } catch (Exception success) { }
   * 
   * // Deleting a model from a domain that doesn't exist should not throw exception
   * domainRepository.removeModel("does not exist", "does not exist");
   * 
   * // Use a real XmiParser with real data domainRepository.setXmiParser(new MockXmiParser());
   * domainRepository.storeDomain(loadDomain(STEEL_WHEELS, "./steel-wheels.xmi"), true); final Domain steelWheels =
   * domainRepository.getDomain(STEEL_WHEELS); assertNotNull(steelWheels);
   * 
   * final String validModelName = "BV_HUMAN_RESOURCES";
   * 
   * { // Can't delete a model that doesn't exist domainRepository.removeModel(STEEL_WHEELS, "no such model"); final
   * Domain test = domainRepository.getDomain(STEEL_WHEELS); assertNotNull(test);
   * assertEquals(steelWheels.getPhysicalModels().size(), test.getPhysicalModels().size());
   * assertEquals(steelWheels.getLogicalModels().size(), test.getLogicalModels().size());
   * assertNotNull(getLogicalModelByName(test, validModelName)); }
   * 
   * { // Delete a model that does exist // NOTE: if caching is enabled, the original STEEL_WHEELS is cached and is the
   * one that will be modified ... // ... so we need to flush the cache to prevent that from happening
   * domainRepository.flushDomains(); domainRepository.removeModel(STEEL_WHEELS, validModelName); final Domain test =
   * domainRepository.getDomain(STEEL_WHEELS); assertNotNull(test); assertEquals(steelWheels.getPhysicalModels().size(),
   * test.getPhysicalModels().size()); assertEquals(steelWheels.getLogicalModels().size() - 1,
   * test.getLogicalModels().size()); assertNull(getLogicalModelByName(test, validModelName)); } }
   */

  private static LogicalModel getLogicalModelByName( final Domain domain, final String logicalModelName ) {
    for ( final LogicalModel logicalModel : domain.getLogicalModels() ) {
      if ( StringUtils.equals( logicalModelName, logicalModel.getId() ) ) {
        return logicalModel;
      }
    }
    return null;
  }

  public void testReloadDomains() throws Exception {
    domainRepository.reloadDomains();
  }

  public void testFlushDomains() throws Exception {
    domainRepository.flushDomains();
  }

  public void testGenerateRowLevelSecurityConstraint() throws Exception {
    domainRepository.generateRowLevelSecurityConstraint( null );
  }

  public void testHasAccess() throws Exception {
    domainRepository.hasAccess( 0, null );
  }

  public void testToString() throws Exception {
    // Neither case should throw an exception
    domainRepository.toString( null );
    domainRepository.toString( new RepositoryFile.Builder( "" ).build() );
    domainRepository.toString( repository.getFile( "/etc/metadata" ) );
  }

  public void testLoadLocaleStrings() throws Exception {
    // Add a domain with no external locale information
    domainRepository.setXmiParser( new XmiParser() );
    final Domain steelWheels = loadDomain( STEEL_WHEELS, "./steel-wheels.xmi" );
    domainRepository.storeDomain( steelWheels, true );

    final int initialLocaleSize = steelWheels.getLocaleCodes().length;
    assertEquals( initialLocaleSize, steelWheels.getLocales().size() );
    domainRepository.loadLocaleStrings( STEEL_WHEELS, steelWheels );
    assertEquals( initialLocaleSize, steelWheels.getLocaleCodes().length );
    assertEquals( initialLocaleSize, steelWheels.getLocales().size() );

    final Properties newLocale = new Properties();
    newLocale.put( "[LogicalModel-BV_HUMAN_RESOURCES].[description]", "New Description in Italian" );
    domainRepository.addLocalizationFile( STEEL_WHEELS, "it_IT", newLocale );

    domainRepository.loadLocaleStrings( STEEL_WHEELS, steelWheels );
    final int newLocaleSize = initialLocaleSize + 1;
    assertEquals( newLocaleSize, steelWheels.getLocales().size() );
    domainRepository.loadLocaleStrings( STEEL_WHEELS, steelWheels );
    assertEquals( newLocaleSize, steelWheels.getLocaleCodes().length );
    assertEquals( newLocaleSize, steelWheels.getLocales().size() );
  }

  public void testSetXmiParser() throws Exception {
    domainRepository.setXmiParser( null );
    domainRepository.setXmiParser( new XmiParser() );
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
