/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.commons.util.repository.exception.FolderNotValidException;
import org.pentaho.commons.util.repository.exception.InvalidArgumentException;
import org.pentaho.commons.util.repository.exception.ObjectNotFoundException;
import org.pentaho.commons.util.repository.exception.OperationNotSupportedException;
import org.pentaho.commons.util.repository.type.CmisObject;
import org.pentaho.commons.util.repository.type.CmisObjectImpl;
import org.pentaho.commons.util.repository.type.CmisProperties;
import org.pentaho.commons.util.repository.type.PropertiesBase;
import org.pentaho.commons.util.repository.type.PropertyId;
import org.pentaho.commons.util.repository.type.PropertyString;
import org.pentaho.commons.util.repository.type.TypesOfFileableObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BiPlatformRepositoryClientNavigationServiceTest {

  private static Logger mLog = LoggerFactory.getLogger( BiPlatformRepositoryClientNavigationServiceTest.class );

  private BiPlatformRepositoryClientNavigationService navService;
  private CmisObject cmisObject;
  private CmisProperties cmisProperties;
  private List<CmisProperties> cmisPropList;
  private List<CmisObject> cmisObjects;

  private Document mockDocument;
  private Document document;
  private Element mockElement;

  @Before
  public void setup() {
    mockDocument = Mockito.mock( Document.class );
    navService = new BiPlatformRepositoryClientNavigationService();
    cmisObject = new CmisObjectImpl();
    cmisProperties = new CmisProperties();
    cmisObject.setProperties( cmisProperties );

    document = DocumentHelper.createDocument();
    Element repository = document.addElement( "repository" );
    Element admin =
        repository.addElement( "file" ).addAttribute( "isDirectory", "true" ).addAttribute( "name", "admin" );
    admin.addElement( "file" ).addAttribute( "isDirectory", "false" ).addAttribute( "name", "sales_data.csv" );
  }

  @After
  public void destroy() {
  }

  @Test
  public void testSetDoc() {
    mLog.info( "testSetDoc.." );
    assertNull( navService.getDoc() );
    navService.setDoc( mockDocument );
    assertNotNull( navService.getDoc() );
  }

  @Test
  public void testObjectParent() {

    mLog.info( "testObjectParent.." );
    // CASE 1: Provide a wrong key for the original BI platform repository
    try {
      navService.getObjectParent( "WRONG_REPOSITORY_ID", "objectId", "filter", false, false );
    } catch ( Exception exe ) {
      assertTrue( exe instanceof InvalidArgumentException );
    }

    // CASE 2: Provide a right key, but document is null
    try {
      cmisObjects =
          navService.getObjectParent( BiPlatformRepositoryClient.PLATFORMORIG, "/admin/training/sales_data.csv",
              "filter", false, false );
    } catch ( Exception exe ) {
      assertTrue( "Should have thrown NPE here, as lNavService doc is null", true );
    }

    // CASE 3: the document is empty
    try {
      navService.setDoc( mockDocument );

      cmisObjects =
          navService.getObjectParent( BiPlatformRepositoryClient.PLATFORMORIG, "/admin/training/sales_data.csv",
              "filter", false, false );
    } catch ( Exception exe ) {
      assertTrue( exe instanceof ObjectNotFoundException );
    }

    // CASE 4: now the document has valid contents
    try {
      navService.setDoc( document );
      cmisObjects =
          navService.getObjectParent( BiPlatformRepositoryClient.PLATFORMORIG, "/admin/sales_data.csv", "filter", false,
              false );
      assertTrue( cmisObjects != null );
      assertTrue( cmisObjects.get( 0 ).getProperties() != null );
      CmisObject lCmisObject = cmisObjects.get( 0 );
      assertTrue( CmisObject.OBJECT_TYPE_FOLDER.equals( lCmisObject.findStringProperty( PropertiesBase.OBJECTTYPEID,
          null ) ) );

    } catch ( Exception exe ) {
      assertTrue( "Shouldn't throw exception here", false );
      mLog.error( "Problem testing testObjectParent() - Fails Unit Test" );
    }
  }

  @Test
  public void testGetFolderParent() {

    mLog.info( "testGetFolderParent.." );
    // CASE 1: Provide a wrong key for the original BI platform repository
    try {
      navService.getFolderParent( "WRONG_REPOSITORY_ID", "/admin/sales_data.csv", "filter", false, false, false );
    } catch ( Exception exe ) {
      assertTrue( exe instanceof InvalidArgumentException );
    }

    // CASE 2: Provide a right key, but the document is null
    try {
      cmisObjects =
          navService.getFolderParent( BiPlatformRepositoryClient.PLATFORMORIG, "/admin/sales_data.csv", "filter", false,
              false, false );
    } catch ( Exception exe ) {
      assertTrue( "Should have thrown exception here", true );
    }

    // CASE 3: document is empty
    try {
      navService.setDoc( mockDocument );

      navService.getFolderParent( BiPlatformRepositoryClient.PLATFORMORIG, "/admin/sales_data.csv", "filter", false,
          false, false );
    } catch ( Exception exe ) {
      assertTrue( exe instanceof FolderNotValidException );
    }

    // CASE 4: now the document has valid contents
    try {
      navService.setDoc( document );
      cmisObjects =
          navService.getFolderParent( BiPlatformRepositoryClient.PLATFORMORIG, "/admin", "filter", false, false,
              false );
      assertTrue( cmisObjects != null );
      assertTrue( cmisObjects.get( 0 ).getProperties() != null );
      CmisObject lCmisObject = cmisObjects.get( 0 );
      assertTrue( "".equals( lCmisObject.findStringProperty( PropertiesBase.OBJECTTYPEID, null ) ) );
    } catch ( Exception exe ) {
      assertTrue( "Shouldn't throw exception here", false );
      mLog.error( "Exception in testGetFolderParent() - Fails Unit Test" );
    }
  }

  @Test
  public void testGetDescendants() {

    mLog.info( "testGetDescendants.." );
    // CASE 1: Provide a wrong key for the original BI platform repository
    try {
      navService.getDescendants( "WRONG_REPOSITORY_ID", "/admin", null, 0, "filter", false, false );
    } catch ( Exception exe ) {
      assertTrue( exe instanceof InvalidArgumentException );
    }

    // CASE 2: Provide a right key, but the document is null
    try {
      navService.getDescendants( BiPlatformRepositoryClient.PLATFORMORIG, "/admin", null, 0, "filter", false, false );
    } catch ( Exception exe ) {
      assertTrue( "Shouldn have thrown exception here", true );
    }

    // CASE 3: document is empty
    try {
      navService.setDoc( mockDocument );
      navService.getDescendants( BiPlatformRepositoryClient.PLATFORMORIG, "/admin", null, 0, "filter", false, false );
    } catch ( Exception exe ) {
      assertTrue( exe instanceof FolderNotValidException );
    }

    // CASE 4: the document has valid contents
    try {
      navService.setDoc( document );
      cmisObjects =
          navService.getDescendants( BiPlatformRepositoryClient.PLATFORMORIG, "/admin", null, 0, "filter", false,
              false );
      assertTrue( cmisObjects != null );
    } catch ( Exception exe ) {
      assertTrue( "Shouldn't throw exception here", false );
      mLog.error( "Exception in testGetDescendants() - Fails Unit Test" );
    }

    // CASE 5: TypesOfFileableObjects.FOLDERS
    TypesOfFileableObjects fileableFolders = new TypesOfFileableObjects( TypesOfFileableObjects.FOLDERS );
    cmisObject.getProperties().getProperties().add( new PropertyString( PropertiesBase.OBJECTTYPEID,
        CmisObject.OBJECT_TYPE_FOLDER ) );

    try {
      navService.setDoc( document );
      cmisObjects =
          navService.getDescendants( BiPlatformRepositoryClient.PLATFORMORIG, "/admin", fileableFolders, 1, "filter",
              false, false );
      assertTrue( cmisObjects != null );
    } catch ( Exception exe ) {
      assertTrue( "Shouldn't throw exception here", false );
      mLog.error( "Exception in testGetDescendants() - Fails Unit Test" );
    }

    // CASE 5: TypesOfFileableObjects.DOCUMENTS
    TypesOfFileableObjects fileableDocuments = new TypesOfFileableObjects( TypesOfFileableObjects.DOCUMENTS );
    try {
      navService.setDoc( document );
      cmisObjects =
          navService.getDescendants( BiPlatformRepositoryClient.PLATFORMORIG, "/admin", fileableDocuments, 0, "filter",
              false, false );
      assertTrue( cmisObjects != null );
    } catch ( Exception exe ) {
      assertTrue( "Shouldn't throw exception here", false );
      mLog.error( "Exception in testGetDescendants() - Fails Unit Test" );
    }

  }

  @Test
  public void testGetChildren() {

    mLog.info( "testGetChildren.." );

    // CASE 1: Provide a wrong key for the original BI platform repository
    try {
      navService.getChildren( "WRONG_REPOSITORY_ID", "/admin", null, "filter", false, false, 3, 0 );
    } catch ( Exception exe ) {
      assertTrue( exe instanceof InvalidArgumentException );
    }

    // CASE 2: Provide a right key, but the doc is null
    try {
      navService.getChildren( BiPlatformRepositoryClient.PLATFORMORIG, "/admin", null, "filter", false, false, 3, 0 );
    } catch ( Exception exe ) {
      assertTrue( "Shouldn have thrown exception here", true );
    }

    // CASE 3: doc is empty
    try {
      navService.setDoc( mockDocument );

      navService.getChildren( BiPlatformRepositoryClient.PLATFORMORIG, "/admin", null, "filter", false, false, 3, 0 );
    } catch ( Exception exe ) {
      assertTrue( exe instanceof FolderNotValidException );
    }

    // CASE 4: empty filters
    try {
      navService.setDoc( mockDocument );

      navService.getChildren( BiPlatformRepositoryClient.PLATFORMORIG, "/admin", null, null, false, false, 3, 0 );
    } catch ( Exception exe ) {
      assertTrue( exe instanceof FolderNotValidException );
    }

    // CASE 5: the document is not empty and has valid contents
    try {
      navService.setDoc( document );
      cmisObjects =
          navService.getChildren( BiPlatformRepositoryClient.PLATFORMORIG, "/admin", null, null, false, false, 3, 0 );
      assertTrue( cmisObjects != null );
    } catch ( Exception exe ) {
      assertTrue( "Shouldn't throw exception here", false );
      mLog.error( "Exception in testGetChildren() - Fails Unit Test" );
    }
  }

  @Test
  public void testGetCheckedoutDocs() {
    mLog.info( "testGetCheckedoutDocs.." );

    try {
      navService.getCheckedoutDocs( BiPlatformRepositoryClient.PLATFORMORIG, "/admin/sales_data.csv", "filter", false,
          false, 3, 0 );
    } catch ( Exception exe ) {
      assertTrue( exe instanceof OperationNotSupportedException );
    }
  }

  @Test
  public void testGetRepositoryPath() {

    mLog.info( "testGetRepositoryPath.." );

    // Following line throws NPE
    // lNavService.getRepositoryPath(cmisObject);

    // CASE 1: Set object type id to folder
    cmisObject.getProperties().getProperties().add( new PropertyId( PropertiesBase.OBJECTID, "repository" ) );
    cmisObject.getProperties().getProperties().add( new PropertyString( PropertiesBase.OBJECTTYPEID,
        CmisObject.OBJECT_TYPE_FOLDER ) );

    String path = navService.getRepositoryPath( cmisObject );
    assertTrue( "repository".equals( path ) );

    // CASE 2: Object type id is null and object id is not null
    cmisObject.setProperties( new CmisProperties() );
    cmisObject.getProperties().getProperties().add( new PropertyId( PropertiesBase.OBJECTID, "/admin/pat" ) );
    cmisObject.getProperties().getProperties().add( new PropertyString( PropertiesBase.OBJECTTYPEID, null ) );

    path = navService.getRepositoryPath( cmisObject );
    assertTrue( "Repository path as expected", "/admin".equals( path ) );

    // CASE 3: Object type id is null and object id is not null
    cmisObject.setProperties( new CmisProperties() );
    cmisObject.getProperties().getProperties().add( new PropertyId( PropertiesBase.OBJECTID, "repository" ) );
    cmisObject.getProperties().getProperties().add( new PropertyString( PropertiesBase.OBJECTTYPEID, null ) );

    path = navService.getRepositoryPath( cmisObject );
    assertTrue( "Repository path as expected", "".equals( path ) );
  }

  @Test
  public void testGetRepositoryFilename() {

    mLog.info( "testGetRepositoryFilename.." );
    // Following line throws NPE
    // lNavService.getRepositoryFilename(null);

    cmisObject.getProperties().getProperties().add( new PropertyString( CmisObject.NAME, "UNIT_TEST" ) );

    String filename = navService.getRepositoryFilename( cmisObject );
    assertTrue( "UNIT_TEST".equals( filename ) );

    cmisObject.getProperties().getProperties().add( new PropertyString( PropertiesBase.OBJECTTYPEID,
        CmisObject.OBJECT_TYPE_FOLDER ) );

    filename = navService.getRepositoryFilename( cmisObject );
    assertTrue( "Repository filename as expected", "".equals( filename ) );
  }

}
