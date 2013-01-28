/*
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
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created August 17, 2006
 * @author Michael D'Amour
 * 
 */
package org.pentaho.platform.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.pentaho.commons.util.repository.exception.FolderNotValidException;
import org.pentaho.commons.util.repository.exception.InvalidArgumentException;
import org.pentaho.commons.util.repository.exception.ObjectNotFoundException;
import org.pentaho.commons.util.repository.exception.OperationNotSupportedException;
import org.pentaho.commons.util.repository.type.CmisObject;
import org.pentaho.commons.util.repository.type.PropertiesBase;
import org.pentaho.commons.util.repository.type.TypesOfFileableObjects;
import org.pentaho.platform.util.client.BiPlatformRepositoryClient;
import org.pentaho.platform.util.client.BiPlatformRepositoryClientNavigationService;

import junit.framework.TestCase;

@SuppressWarnings({"all"})
public class BiPlatformRepositoryClientTest extends TestCase {

  private Document getServiceDocument() throws IOException, DocumentException {
    
    File file = new File("test-res/solution/test/xml/SolutionRepositoryService.xml");
    FileInputStream in = new FileInputStream( file );
    byte b[] = new byte[2048];
    StringBuilder sb = new StringBuilder();
    int n = 0;
    while( n != -1 ) {
      sb.append( new String(b, 0, n) );
      n = in.read(b);
    }
    
    String xml = sb.toString();
    Document doc = DocumentHelper.parseText( xml );
    return doc;
    
  }

  public void testBadObject() throws Exception {

    BiPlatformRepositoryClientNavigationService navigationService = new BiPlatformRepositoryClientNavigationService();
    Document doc = getServiceDocument();
    navigationService.setDoc(doc);
    
    assertEquals( doc, navigationService.getDoc() );
    
    TypesOfFileableObjects anyTypes = new TypesOfFileableObjects( TypesOfFileableObjects.ANY );

    try {
      navigationService.getObjectParent(BiPlatformRepositoryClient.PLATFORMORIG, "bogus", 
        null, false, false);
      assertTrue(false);
    } catch ( ObjectNotFoundException e) {
      
    }

  }
  
  public void testBadFolder() throws Exception {

    BiPlatformRepositoryClientNavigationService navigationService = new BiPlatformRepositoryClientNavigationService();
    Document doc = getServiceDocument();
    navigationService.setDoc(doc);
    
    assertEquals( doc, navigationService.getDoc() );
    
    TypesOfFileableObjects anyTypes = new TypesOfFileableObjects( TypesOfFileableObjects.ANY );

    try {
      navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "bogus", anyTypes, 
        1, null, false, false);
      assertTrue(false);
    } catch ( FolderNotValidException e) {
      
    }

  }
  
  public void testBasic() throws Exception {
    
    BiPlatformRepositoryClientNavigationService navigationService = new BiPlatformRepositoryClientNavigationService();
    Document doc = getServiceDocument();
    navigationService.setDoc(doc);
    
    assertEquals( doc, navigationService.getDoc() );
    
    TypesOfFileableObjects anyTypes = new TypesOfFileableObjects( TypesOfFileableObjects.ANY );
    try {
      navigationService.getDescendants("bogus", "", anyTypes, 
          1, null, false, false);
      assertTrue(false);
    } catch (InvalidArgumentException e) {
    }

    try {
      navigationService.getCheckedoutDocs("bogus", "", null, false, false, 0, 0);
      assertTrue(false);
    } catch (OperationNotSupportedException e) {
    }

    try {
      navigationService.getChildren("bogus", "", anyTypes, 
          null, false, false, 0, 0);
      assertTrue(false);
    } catch (InvalidArgumentException e) {
    }

    try {
      navigationService.getFolderParent("bogus", "", null, 
          false, false, false);
      assertTrue(false);
    } catch (InvalidArgumentException e) {
    }

    try {
      navigationService.getObjectParent("bogus", "", null, 
          false, false);
      assertTrue(false);
    } catch (InvalidArgumentException e) {
    }

  }
  
  public void testDescendents() throws Exception {

    BiPlatformRepositoryClientNavigationService navigationService = new BiPlatformRepositoryClientNavigationService();
    Document doc = getServiceDocument();
    navigationService.setDoc(doc);
    
    TypesOfFileableObjects folderTypes = new TypesOfFileableObjects( TypesOfFileableObjects.FOLDERS );
    TypesOfFileableObjects documentTypes = new TypesOfFileableObjects( TypesOfFileableObjects.DOCUMENTS );
    TypesOfFileableObjects anyTypes = new TypesOfFileableObjects( TypesOfFileableObjects.ANY );
    
    List<CmisObject> objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", documentTypes, 
        1, null, false, false);

    assertEquals( 0, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", folderTypes, 
        1, null, false, false);

    assertEquals( 6, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", anyTypes, 
        1, null, false, false);

    assertEquals( 6, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "/admin", documentTypes, 
        1, null, false, false);

    assertEquals( 14, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "/admin", folderTypes, 
        1, null, false, false);

    assertEquals( 2, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "/admin", anyTypes, 
        1, null, false, false);

    assertEquals( 16, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", documentTypes, 
        2, null, false, false);

    assertEquals( 21, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", folderTypes, 
        2, null, false, false);

    assertEquals( 31, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", anyTypes, 
        2, null, false, false);

    assertEquals( 52, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", documentTypes, 
        3, null, false, false);

    assertEquals( 144, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", folderTypes, 
        3, null, false, false);

    assertEquals( 43, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", anyTypes, 
        3, null, false, false);

    assertEquals( 187, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", documentTypes, 
        10, null, false, false);

    assertEquals( 268, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", folderTypes, 
        10, null, false, false);

    assertEquals( 99, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", anyTypes, 
        10, null, false, false);

    assertEquals( 367, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", documentTypes, 
        10, "xaction", false, false);

    assertEquals( 167, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", documentTypes, 
        10, "xcdf", false, false);

    assertEquals( 44, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", documentTypes, 
        10, "prpt", false, false);

    assertEquals( 23, objects.size() );

    objects = navigationService.getDescendants(BiPlatformRepositoryClient.PLATFORMORIG, "", documentTypes, 
        10, "xaction,xcdf,prpt", false, false);

    assertEquals( 234, objects.size() );

    
  }
  
  public void testFilters() throws Exception {
    
    BiPlatformRepositoryClientNavigationService navigationService = new BiPlatformRepositoryClientNavigationService();
    Document doc = getServiceDocument();
    navigationService.setDoc(doc);
    
    TypesOfFileableObjects types = new TypesOfFileableObjects( TypesOfFileableObjects.DOCUMENTS );
    List<CmisObject> objects = navigationService.getChildren(BiPlatformRepositoryClient.PLATFORMORIG, "/admin", types, CmisObject.OBJECT_TYPE_FOLDER, false, false, 0, 0);

    assertEquals( 0, objects.size() );

    objects = navigationService.getChildren(BiPlatformRepositoryClient.PLATFORMORIG, "/admin", types, "url", false, false, 0, 0);

    assertEquals( 7, objects.size() );
    CmisObject document = objects.get(0);
    String documentId = document.findIdProperty(PropertiesBase.OBJECTID, null);
    assertEquals( "admin/AuditReportList.url", documentId);

    objects = navigationService.getChildren(BiPlatformRepositoryClient.PLATFORMORIG, "/admin", types, "xaction", false, false, 0, 0);

    assertEquals( 7, objects.size() );
    document = objects.get(0);
    documentId = document.findIdProperty(PropertiesBase.OBJECTID, null);
    assertEquals( "admin/clean_repository.xaction", documentId);

    objects = navigationService.getChildren(BiPlatformRepositoryClient.PLATFORMORIG, "/admin", types, "url,xaction", false, false, 0, 0);

    assertEquals( 14, objects.size() );

  }
  
  public void testSkipAndMax() throws Exception {
    
    BiPlatformRepositoryClientNavigationService navigationService = new BiPlatformRepositoryClientNavigationService();
    Document doc = getServiceDocument();
    navigationService.setDoc(doc);
    
    TypesOfFileableObjects types = new TypesOfFileableObjects( TypesOfFileableObjects.DOCUMENTS );
    List<CmisObject> objects = navigationService.getChildren(BiPlatformRepositoryClient.PLATFORMORIG, "/admin", types, null, false, false, 2, 0);

    assertEquals( 2, objects.size() );
    
    CmisObject document = objects.get(0);
    String documentId = document.findIdProperty(PropertiesBase.OBJECTID, null);
    assertEquals( "admin/AuditReportList.url", documentId);
    
    document = objects.get(1);
    documentId = document.findIdProperty(PropertiesBase.OBJECTID, null);
    assertEquals( "admin/AuditReports.url", documentId);

    objects = navigationService.getChildren(BiPlatformRepositoryClient.PLATFORMORIG, "/admin", types, null, false, false, 2, 2);

    assertEquals( 2, objects.size() );
    
    document = objects.get(0);
    documentId = document.findIdProperty(PropertiesBase.OBJECTID, null);
    assertEquals( "admin/clean_repository.xaction", documentId);

    document = objects.get(1);
    documentId = document.findIdProperty(PropertiesBase.OBJECTID, null);
    assertEquals( "admin/clear_mondrian_schema_cache.xaction", documentId);

  }
  
  public void testSolutionParent() throws Exception {
    
    BiPlatformRepositoryClientNavigationService navigationService = new BiPlatformRepositoryClientNavigationService();
    Document doc = getServiceDocument();
    navigationService.setDoc(doc);
    
    List<CmisObject> parentList = navigationService.getFolderParent(BiPlatformRepositoryClient.PLATFORMORIG, "", null, false, false, false);

    assertEquals( 0, parentList.size() );
    
    parentList = navigationService.getObjectParent(BiPlatformRepositoryClient.PLATFORMORIG, "", null, false, false);

    assertEquals( 0, parentList.size() );
    
  }

  public void testObjectParent() throws Exception {
    
    BiPlatformRepositoryClientNavigationService navigationService = new BiPlatformRepositoryClientNavigationService();
    Document doc = getServiceDocument();
    navigationService.setDoc(doc);
    
    TypesOfFileableObjects types = new TypesOfFileableObjects( TypesOfFileableObjects.DOCUMENTS );
    List<CmisObject> objects = navigationService.getChildren(BiPlatformRepositoryClient.PLATFORMORIG, "/admin", types, null, false, false, -1, -1);

    assertEquals( 14, objects.size() );

    CmisObject document = objects.get(0);
    String name = document.findStringProperty(CmisObject.NAME, null);
    String documentId = document.findIdProperty(PropertiesBase.OBJECTID, null);
    
    assertEquals( "AuditReportList.url", name);
    assertEquals( "admin/AuditReportList.url", documentId);
    
    CmisObject parent = navigationService.getObjectParent(BiPlatformRepositoryClient.PLATFORMORIG, documentId, null, false, false).get(0);

    assertNotNull( parent );
    assertEquals( "admin", parent.findStringProperty(CmisObject.NAME, null));
    assertEquals( "Admin Services", parent.findStringProperty(CmisObject.LOCALIZEDNAME, null));
    assertEquals( "admin", parent.findIdProperty(PropertiesBase.OBJECTID, null));
    assertEquals( CmisObject.OBJECT_TYPE_FOLDER, parent.findStringProperty(PropertiesBase.OBJECTTYPEID, null));
    
  }
  
  public void testFolderParent() throws Exception {
    
    BiPlatformRepositoryClientNavigationService navigationService = new BiPlatformRepositoryClientNavigationService();
    Document doc = getServiceDocument();
    navigationService.setDoc(doc);
    
    TypesOfFileableObjects types = new TypesOfFileableObjects( TypesOfFileableObjects.FOLDERS );
    List<CmisObject> objects = navigationService.getChildren(BiPlatformRepositoryClient.PLATFORMORIG, "/admin", types, CmisObject.OBJECT_TYPE_FOLDER, false, false, -1, -1);

    assertEquals( 2, objects.size() );

    CmisObject folder = objects.get(0);
    String folderId = folder.findIdProperty(PropertiesBase.OBJECTID, null);
    
    assertEquals( "audit", folder.findStringProperty(CmisObject.NAME, null));
    assertEquals( "admin/audit", folderId);
    
    CmisObject parent = navigationService.getFolderParent(BiPlatformRepositoryClient.PLATFORMORIG, folderId, null, false, false, false).get(0);

    assertNotNull( parent );
    assertEquals( "admin", parent.findStringProperty(CmisObject.NAME, null));
    assertEquals( "Admin Services", parent.findStringProperty(CmisObject.LOCALIZEDNAME, null));
    assertEquals( "admin", parent.findIdProperty(PropertiesBase.OBJECTID, null));
    assertEquals( CmisObject.OBJECT_TYPE_FOLDER, parent.findStringProperty(PropertiesBase.OBJECTTYPEID, null));
    
  }
  
  public void testSolutionsAllTypes() throws Exception {
    
    BiPlatformRepositoryClientNavigationService navigationService = new BiPlatformRepositoryClientNavigationService();
    Document doc = getServiceDocument();
    navigationService.setDoc(doc);
    
    List<CmisObject> objects = navigationService.getChildren(BiPlatformRepositoryClient.PLATFORMORIG, "", null, CmisObject.OBJECT_TYPE_FOLDER, false, false, -1, -1);
    
    assertNotNull(objects);
    
    assertEquals( 6, objects.size() );
    
    assertEquals( "admin", objects.get(0).findStringProperty(CmisObject.NAME, null));
    assertEquals( "Admin Services", objects.get(0).findStringProperty(CmisObject.LOCALIZEDNAME, null));
    assertEquals( "admin", objects.get(0).findIdProperty(PropertiesBase.OBJECTID, null));
    assertEquals( CmisObject.OBJECT_TYPE_FOLDER, objects.get(0).findStringProperty(PropertiesBase.OBJECTTYPEID, null));
    
    assertEquals( "bi-developers", objects.get(1).findStringProperty(CmisObject.NAME, null));
    assertEquals( "BI Developer Examples", objects.get(1).findStringProperty(CmisObject.LOCALIZEDNAME, null));
    assertEquals( "bi-developers", objects.get(1).findIdProperty(PropertiesBase.OBJECTID, null));
    assertEquals( CmisObject.OBJECT_TYPE_FOLDER, objects.get(1).findStringProperty(PropertiesBase.OBJECTTYPEID, null));
    
  }
  
  public void testSolutionsFolders() throws Exception {
    
    BiPlatformRepositoryClientNavigationService navigationService = new BiPlatformRepositoryClientNavigationService();
    Document doc = getServiceDocument();
    navigationService.setDoc(doc);
    
    TypesOfFileableObjects types = new TypesOfFileableObjects( TypesOfFileableObjects.FOLDERS );
    List<CmisObject> objects = navigationService.getChildren(BiPlatformRepositoryClient.PLATFORMORIG, "", types, CmisObject.OBJECT_TYPE_FOLDER, false, false, -1, -1);
    
    assertNotNull(objects);
    
    assertEquals( 6, objects.size() );
    
    assertEquals( "admin", objects.get(0).findStringProperty(CmisObject.NAME, null));
    assertEquals( "Admin Services", objects.get(0).findStringProperty(CmisObject.LOCALIZEDNAME, null));
    assertEquals( "admin", objects.get(0).findIdProperty(PropertiesBase.OBJECTID, null));
    assertEquals( CmisObject.OBJECT_TYPE_FOLDER, objects.get(0).findStringProperty(PropertiesBase.OBJECTTYPEID, null));
    
    assertEquals( "bi-developers", objects.get(1).findStringProperty(CmisObject.NAME, null));
    assertEquals( "BI Developer Examples", objects.get(1).findStringProperty(CmisObject.LOCALIZEDNAME, null));
    assertEquals( "bi-developers", objects.get(1).findIdProperty(PropertiesBase.OBJECTID, null));
    assertEquals( CmisObject.OBJECT_TYPE_FOLDER, objects.get(1).findStringProperty(PropertiesBase.OBJECTTYPEID, null));
    
  }


  public void testSolutionsDocuments() throws Exception {
    
    BiPlatformRepositoryClientNavigationService navigationService = new BiPlatformRepositoryClientNavigationService();
    Document doc = getServiceDocument();
    navigationService.setDoc(doc);
    
    TypesOfFileableObjects types = new TypesOfFileableObjects( TypesOfFileableObjects.DOCUMENTS );
    List<CmisObject> objects = navigationService.getChildren(BiPlatformRepositoryClient.PLATFORMORIG, "", types, CmisObject.OBJECT_TYPE_FOLDER, false, false, -1, -1);
    
    assertNotNull(objects);
    
    assertEquals( 0, objects.size() );
    
  }
}
