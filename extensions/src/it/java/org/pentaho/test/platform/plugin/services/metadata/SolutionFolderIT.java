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

package org.pentaho.test.platform.plugin.services.metadata;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.concept.types.LocaleType;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Purpose: Tests the import of localization files that exist in the solution folders where metadata.xmi files may
 * exist.
 * 
 * If a metadata.xmi file exists i the solution folder then all files named metadata_"locale_code".properties will be
 * loaded into the domain that was created by the loading of the metadata.xmi file.
 * 
 * For each test a file named "mymodel.xmi" is created in this folder. The individual tests may call the methods
 * 
 * MetadataDomainRepository.getLocaleFromPropertyfilename() MetadataDomainRepository.getLocalePropertyFilenames()
 * 
 * The determines which method is to be tested and the criteria of the test. Each test method's javadoc contains the
 * goal of the test.
 * 
 * @author sflatley
 */
public class SolutionFolderIT {

  private final String STEEL_WHEELS = "metadata";	
  private String SOLUTION_PATH;
  // Legacy solution path and file name
  private String BI_DEVELOPERS_FOLDER_NAME = "bi-developers";
  private String LEGACY_XMI_FILENAME = "steel-wheels.xmi";
  private String XMI_FILENAME_EXTENSION = ".xmi";
  private String BI_DEVELOPERS_FULL_PATH;
  private File biDevelopersSolutionFolder;
  // localeTestUtil
  LocaleTestUtil localeTestUtil = null;

  /**
   * Creates the files system that mimics a BI Server solution. Also creates a LocaleTestUtility that is used to create
   * the files needed for the tests.
   * 
   * A MicroPlatform is also created as it is needed for filtering the property files that may exist in the metadata
   * folder.
   * 
   * @throws Exception
   */
  @Before
  public void init() {
    // create solution paths
    SOLUTION_PATH = System.getProperty( "java.io.tmpdir" );
    BI_DEVELOPERS_FULL_PATH = SOLUTION_PATH + "/" + BI_DEVELOPERS_FOLDER_NAME;
    //BI_DEVELOPERS_FULL_PATH = SOLUTION_PATH + BI_DEVELOPERS_FOLDER_NAME;
    biDevelopersSolutionFolder = new File( BI_DEVELOPERS_FULL_PATH );
    if ( !biDevelopersSolutionFolder.exists() ) {
      biDevelopersSolutionFolder.delete();
      biDevelopersSolutionFolder.mkdir();
    }
    // utility to make this testing a bit easier
    localeTestUtil = new LocaleTestUtil();
    // create a platform
    MicroPlatform mp = new MicroPlatform( SOLUTION_PATH );
    try {
      mp.start();
    } catch ( PlatformInitializationException pie ) {
      pie.printStackTrace();
    }
  }

  @Test
  public void dummyTest() {
    assertTrue( true );
  } 

  /**
   * Tests MetadataDomainRepository.getLocalePropertyFilenames() where one xmi resource and no property file exists in
   * the metadata folder.
   */
  @Test
  public void testNoLocaleFileDiscovery() { 	  
	  PentahoMetadataDomainRepository domainRepository;	  	  
	  IUnifiedRepository repository = new FileSystemBackedUnifiedRepository();
	  repository = new FileSystemBackedUnifiedRepository(BI_DEVELOPERS_FULL_PATH);	  
	  domainRepository = new PentahoMetadataDomainRepository(repository);	  
	  File metadataXmiFile=null;
	  try {
		  metadataXmiFile = localeTestUtil.createFile(BI_DEVELOPERS_FULL_PATH, XMI_FILENAME_EXTENSION);
		  Map<String, InputStream> localizationFiles = domainRepository.getDomainFilesData("metadata");
		  assertEquals(0, localizationFiles.size());
	  }
	  catch (IOException ioe) {
		  fail(ioe.getMessage());
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
	  finally {
		  if (metadataXmiFile != null) {
			  metadataXmiFile.delete();
		  }
	  }
  }
  
  /**
   * Tests MetadataDomainRepository.getLocalePropertyFilenames() when one xmi resource is in the meta data folder with
   * one property file.
   * Update: Test Domain.getLocaleCodes() when we added the new locale on existing domain.
   */
   @Test 
   public void testOneLocaleFileDiscovery() {	
	   try {
		   final Domain steelWheels = loadDomain( STEEL_WHEELS, "./" + LEGACY_XMI_FILENAME );   
		   //get a list of locale codes
		   final int initialLocaleSize = steelWheels.getLocaleCodes().length;
		   // add new locale
		   steelWheels.addLocale(new LocaleType("en_US", "Test locale"));
		   int localizationNewSize = steelWheels.getLocaleCodes().length;
		   // we expect a the size of previous list + 1
		   assertNotNull(localizationNewSize);
		   assertEquals(localizationNewSize , initialLocaleSize + 1);   
	   }
	   catch (IOException ioe) {
		   fail(ioe.getMessage());
	   }
	   catch (DomainStorageException dse) {
		   fail(dse.getMessage());
	   }
	   catch (Exception e)	{
		   e.printStackTrace();
	   }
   }
  
   /**
    * Tests MetadataDomainRepository.getLocalePropertyFilenames() when one xmi file and several property file exists in
    * the metadata folder.
    * Update: Get locales with use Domain.getLocaleCodes()
    */
    @Test
    public void testMultiLocaleFileDiscovery() { 
    	try {
    		Domain steelWheels = loadDomain( STEEL_WHEELS, "./" + LEGACY_XMI_FILENAME );
	    	// get count of current locales
    		final int previousLocaleSize = steelWheels.getLocaleCodes().length;
    		// add new locales
    		steelWheels.addLocale(new LocaleType("EN_US", LEGACY_XMI_FILENAME.substring(0, LEGACY_XMI_FILENAME.indexOf('.'))));
    		steelWheels.addLocale(new LocaleType("EN_GB", LEGACY_XMI_FILENAME.substring(0, LEGACY_XMI_FILENAME.indexOf('.'))));
    		steelWheels.addLocale(new LocaleType("NO_BOK", LEGACY_XMI_FILENAME.substring(0, LEGACY_XMI_FILENAME.indexOf('.'))));
    		// get the list of codes to import
    		String[] localizationFiles = steelWheels.getLocaleCodes();
    		// test the localization filenames for correctness
    		ArrayList<String> solutionFileNames = new ArrayList<String>();
    		for(String solutionFile: localizationFiles) {
    			solutionFileNames.add(solutionFile);
    		}
    		assertNotNull(localizationFiles);
    		assertEquals(previousLocaleSize + 3, localizationFiles.length);
    		assertTrue(solutionFileNames.contains("EN_US"));
    		assertTrue(solutionFileNames.contains("EN_GB"));
    		assertTrue(solutionFileNames.contains("NO_BOK"));
    	}
    	catch (IOException ioe) {
    		fail(ioe.getMessage());
    	}
    	catch (DomainStorageException dse) {
    		fail(dse.getMessage());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    @After
    public void cleanup() {
    	if ( biDevelopersSolutionFolder != null ) {
    		biDevelopersSolutionFolder.delete();
    	}
    }

   private static final Domain loadDomain( final String domainId, final String domainFile ) throws Exception {
	    final InputStream in = SolutionFolderIT.class.getResourceAsStream( domainFile );
	    final XmiParser parser = new XmiParser();
	    final Domain domain = parser.parseXmi( in );
	    domain.setId( domainId );
	    IOUtils.closeQuietly( in );
	    return domain;
   }
}
