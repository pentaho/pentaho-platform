/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.services.metadata;

import org.junit.Before;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import java.io.File;

/**
 * Purpose: Tests the import of localization files that exist in the solution folders' /resources/metadata folder for
 * each XMI file that is located there.
 * <p/>
 * If the xmi file is named mymodel.xmi, then the locale files named mymodel_"local_code".properties will be loaded into
 * the domain created by the load of mymodel.xmi.
 * <p/>
 * For each test a file named "mymodel.xmi" is created in this folder. The individual tests may call the methods
 * <p/>
 * PentahoMetadataDomainRepositoryTest.getLocaleFromPropertyfilename()
 * PentahoMetadataDomainRepositoryTest.getLocalePropertyFilenames()
 * <p/>
 * The determines which method is to be tested and the criteria of the test. Each test method's javadoc contains the
 * goal of the test.
 * 
 * @author sflatley
 */
public class AgileBITests {

  // Solution path and file name
  private String SOLUTION_FOLDER_NAME = "mysolution";
  private String RESOURCE_FOLDER_NAME = "resources";
  private String METADATA_FOLDER_NAME = "metadata";
  private String SOLUTION_PATH;
  private String METADATA_PATH;

  // LocalTestUtil
  LocaleTestUtil localeTestUtil = null;

  /**
   * Creates the files system that mimicks a BI Server solution. Also creates a LocaleTestUtility that is used to create
   * the files needed for the tests.
   * <p/>
   * A MicroPlatform is also created as it is needed for filtering the property files that may exist in the metadata
   * folder.
   * 
   * @throws Exception
   */
  @Before
  public void init() throws Exception {

    // create the solution folder
    SOLUTION_PATH = System.getProperty( "java.io.tmpdir" ) + "/" + SOLUTION_FOLDER_NAME;
    METADATA_PATH = RESOURCE_FOLDER_NAME + "/" + METADATA_FOLDER_NAME;

    File solutionFolder = new File( SOLUTION_PATH );
    if ( !solutionFolder.exists() ) {
      if ( !solutionFolder.mkdir() ) {
        throw new Exception( "Unable to create " + SOLUTION_PATH );
      }
    }

    // Create the resource folder
    String resourcePath = SOLUTION_PATH + "/" + RESOURCE_FOLDER_NAME;
    File resourceFolder = new File( resourcePath );
    if ( !resourceFolder.exists() ) {
      if ( !resourceFolder.mkdir() ) {
        throw new Exception( "Unable to create " + resourcePath );
      }
    }

    // Create the resource metadata folder
    String metadataPath = SOLUTION_PATH + "/" + RESOURCE_FOLDER_NAME + "/" + METADATA_FOLDER_NAME;
    File metadataFolder = new File( metadataPath );
    if ( !metadataFolder.exists() ) {
      if ( !metadataFolder.mkdir() ) {
        throw new Exception( "Unable to create " + metadataPath );
      }
    }

    localeTestUtil = new LocaleTestUtil();

    MicroPlatform mp = new MicroPlatform( SOLUTION_PATH );
    try {
      mp.start();
    } catch ( PlatformInitializationException pie ) {
      pie.printStackTrace();
    }
  }

  /**
   * Test MetadataDomainRepository.getLocaleFromPropertyfilename() to ensure that the locale portion is being parsed out
   * of the filename correctly.
   * 
   * @throws Exception
   */
  // @Test
  // public void testLocalePropertyFilenameParsing() throws Exception {
  //
  // // create xmi resource
  // String xmiFilename = "mymodel.xmi";
  // String xmifileNamePrefix = "mymodel";
  //
  // // create the properties file to go with the xmi resource
  // File propertiesFile = localeTestUtil.createPropertiesFile("EN_US", SOLUTION_PATH + "/" + METADATA_PATH,
  // xmifileNamePrefix);
  // if (propertiesFile != null && propertiesFile.exists()) {
  // MetadataDomainRepositoryTestWrapper metadataDomainRepository = new MetadataDomainRepositoryTestWrapper();
  // String locale = metadataDomainRepository.getLocaleFromPropertyFilename(propertiesFile.getName(),
  // xmifileNamePrefix);
  // assertEquals("EN_US", locale);
  // propertiesFile.delete();
  // }
  // else {
  // throw new Exception("Could not create the properties file.");
  // }
  // }

  /**
   * Tests MetadataDomainRepository.getLocalePropertyFilenames() where one xmi resource and no property file exists in
   * the metadata folder.
   */
  // @Test
  // public void testNoLocaleFileDiscovery() {
  //
  // // define xmi resource and instantiate MetadataDomainRepository
  // String xmiFilename = "mymodel.xmi";
  // File xmiResource = null;
  // MetadataDomainRepositoryTestWrapper metadataDomainRepository = new MetadataDomainRepositoryTestWrapper();
  //
  // try {
  //
  // // create xmiResource. DO NOT create any locale property file4s with it
  // xmiResource = localeTestUtil.createFile(SOLUTION_PATH + "/" + METADATA_PATH, xmiFilename);
  //
  // // get a list of localization files in the same folder as xmiResource
  // ISolutionFile[] localizationFiles = metadataDomainRepository.getLocalePropertyFiles(METADATA_PATH + "/" +
  // xmiFilename);
  //
  // // we should not have a list
  // assertEquals(0, localizationFiles.length);
  // }
  // catch (IOException ioe) {
  // ioe.printStackTrace();
  // fail(ioe.getMessage());
  // }
  // catch (DomainStorageException dse) {
  // dse.printStackTrace();
  // fail(dse.getMessage());
  // }
  // finally {
  // if (xmiResource != null) {
  // xmiResource.delete();
  // }
  // }
  // }

  /**
   * Tests MetadataDomainRepository.getLocalePropertyFilenames() when one xmi resource is in the meta data folder with
   * one property file.
   */
  // @Test
  // public void testOneLocaleFileDiscovery() {
  //
  // // define xmi resource instantiate MetadataDomainRepository
  // String xmiFilename = "mymodel.xmi";
  // File xmiResource = null;
  // File propertiesFile = null;
  // MetadataDomainRepositoryTestWrapper metadataDomainRepository = new MetadataDomainRepositoryTestWrapper();
  //
  // // discover the localization files
  // try {
  //
  // // create the xmi resource and the property file
  // xmiResource = localeTestUtil.createFile(SOLUTION_PATH + "/" + METADATA_PATH, xmiFilename);
  // propertiesFile = localeTestUtil.createPropertiesFile("EN_US", SOLUTION_PATH + "/" + METADATA_PATH,
  // xmiFilename.substring(0, xmiFilename.indexOf('.')));
  //
  // // get the list of locale property files
  // ISolutionFile[] localizationFileNames = metadataDomainRepository.getLocalePropertyFiles(METADATA_PATH + "/" +
  // xmiFilename);
  //
  // // we expect a list of one file - the one we just created
  // assertNotNull(localizationFileNames);
  // assertEquals(1, localizationFileNames.length);
  // assertEquals(localizationFileNames[0].getFileName(), propertiesFile.getName());
  // }
  // catch (IOException ioe) {
  // ioe.printStackTrace();
  // fail(ioe.getMessage());
  // }
  // catch (DomainStorageException dse) {
  // dse.printStackTrace();
  // fail(dse.getMessage());
  // }
  // finally {
  // if (propertiesFile != null) { propertiesFile.delete(); }
  // if (xmiResource !=null) { xmiResource.delete(); }
  // }
  // }

  /**
   * Tests MetadataDomainRepository.getLocalePropertyFilenames() when one xmi file and several property file exists in
   * the metadata folder.
   */
  // @Test
  // public void testLegacyDomainMultiLocaleFileDiscovery() {
  //
  // // define xmi resource, property files and instantiate MetadataDomainRepository
  // String xmiFilename = "mymodel.xmi";
  // File xmiResource = null;
  // File en_us_properties=null, en_gb_properties=null, no_bok_properties=null;
  // MetadataDomainRepositoryTestWrapper metadataDomainRepository = new MetadataDomainRepositoryTestWrapper();
  //
  // try {
  //
  // xmiResource = localeTestUtil.createFile(SOLUTION_PATH + "/" + METADATA_PATH, xmiFilename);
  // en_us_properties = localeTestUtil.createPropertiesFile("EN_US", SOLUTION_PATH + "/" + METADATA_PATH,
  // xmiFilename.substring(0, xmiFilename.indexOf('.')));
  // en_gb_properties = localeTestUtil.createPropertiesFile("EN_GB", SOLUTION_PATH + "/" + METADATA_PATH,
  // xmiFilename.substring(0, xmiFilename.indexOf('.')));
  // no_bok_properties = localeTestUtil.createPropertiesFile("NO_BOK", SOLUTION_PATH + "/" + METADATA_PATH,
  // xmiFilename.substring(0, xmiFilename.indexOf('.')));
  //
  // ISolutionFile[] localizationFiles = metadataDomainRepository.getLocalePropertyFiles(METADATA_PATH + "/" +
  // xmiFilename);
  // if (localizationFiles == null) {
  // fail("List of localization files is null.  We expected a list of three.");
  // }
  //
  // // test the localization filenames for correctness
  // ArrayList<String> solutionFileNames = new ArrayList<String>();
  // for(ISolutionFile solutionFile: localizationFiles) {
  // solutionFileNames.add(solutionFile.getFileName());
  // }
  // assertNotNull(localizationFiles);
  // assertEquals(3, localizationFiles.length);
  // assertTrue(solutionFileNames.contains(en_us_properties.getName()));
  // assertTrue(solutionFileNames.contains(en_gb_properties.getName()));
  // assertTrue(solutionFileNames.contains(no_bok_properties.getName()));
  // }
  // catch (IOException ioe) {
  // ioe.printStackTrace();
  // fail(ioe.getMessage());
  // }
  // catch (DomainStorageException dse) {
  // dse.printStackTrace();
  // fail(dse.getMessage());
  // }
  // finally {
  // if(en_us_properties != null) { en_us_properties.delete(); }
  // if(en_gb_properties != null) { en_gb_properties.delete(); }
  // if(no_bok_properties != null) { no_bok_properties.delete(); }
  // xmiResource.delete();
  // }
  // }
}
