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

package org.pentaho.platform.plugin.services.importexport.exportManifest;

import junit.framework.TestCase;
import org.junit.Test;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestDto;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetadata;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMondrian;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

public class ExportManifestTest extends TestCase {
  ExportManifest exportManifest;
  RepositoryFile repoDir2;
  RepositoryFile repoFile3;
  RepositoryFileAcl repoDir1Acl;
  RepositoryFileAcl repoDir2Acl;
  RepositoryFileAcl repoFile3Acl;
  ExportManifestEntity entity1;
  ExportManifestEntity entity2;
  ExportManifestEntity entity3;
  private ExportManifest importManifest;

  public ExportManifestTest() {
    String rootFolder = "/dir1/";
    exportManifest = new ExportManifest();
    ExportManifestDto.ExportManifestInformation exportManifestInformation = exportManifest.getManifestInformation();
    exportManifestInformation.setExportBy( "MickeyMouse" );
    exportManifestInformation.setExportDate( "2013-01-01" );
    exportManifestInformation.setRootFolder( rootFolder );

    List<RepositoryFileAce> aces1 = new ArrayList<RepositoryFileAce>();
    aces1.add( createMockAce( "admin-/pentaho/tenant0", "USER", RepositoryFilePermission.READ,
        RepositoryFilePermission.WRITE ) );
    aces1.add( createMockAce( "TenantAdmin-/pentaho/tenant0", "ROLE", RepositoryFilePermission.READ ) );
    repoDir2 = createMockRepositoryFile( "/dir1/dir2", true );
    repoDir2Acl = createMockRepositoryAcl( "acl2", "admin", false, aces1 );
    repoFile3 = createMockRepositoryFile( "/dir1/dir2/file1", false );
    RepositoryFile badRepoFile = createMockRepositoryFile( "/baddir/dir2/file1", false );

    try {
      exportManifest.add( repoDir2, repoDir2Acl );
      exportManifest.add( repoFile3, null );
    } catch ( Exception e ) {
      fail( e.getStackTrace().toString() );
    }

    try {
      exportManifest.add( badRepoFile, null );
      fail( "Bad path did not generate a ExportManifestFormatException" );
    } catch ( ExportManifestFormatException e ) {
      //ignored
    }

    entity2 = exportManifest.getExportManifestEntity( "dir2" );
    assertNotNull( entity2 );
    entity3 = exportManifest.getExportManifestEntity( "dir2/file1" );
    assertNotNull( entity3 );

    // Mondrian
    ExportManifestMondrian mondrian = new ExportManifestMondrian();
    mondrian.setCatalogName( "cat1" );
    mondrian.setParameters( new Parameters() {
      {
        put( "testKey", "testValue" );
      }
    } );
    mondrian.setFile( "testMondrian.xml" );
    exportManifest.addMondrian( mondrian );

    // Metadata
    ExportManifestMetadata metadata = new ExportManifestMetadata();
    metadata.setDomainId( "testDomain" );
    metadata.setFile( "testMetadata.xml" );
    exportManifest.addMetadata( metadata );
    exportManifest.addSchedule( new JobScheduleRequest() );

    DatabaseConnection connection = new DatabaseConnection();
    connection.setAccessType( DatabaseAccessType.NATIVE );
    connection.setDatabaseName( "SampleData" );
    connection.setDatabasePort( "9001" );
    DatabaseType type = new DatabaseType();
    type.setName( "Hypersonic" );
    type.setShortName( "HYPERSONIC" );
    connection.setDatabaseType( type );
    connection.setHostname( "localhost" );
    connection.setUsername( "pentaho_user" );
    connection.setPassword( "password" );
    connection.setMaximumPoolSize( 20 );

    exportManifest.addDatasource( connection );

  }

  @Test
  public void testEntityAccess() {
    ExportManifestEntity entityR1 = exportManifest.getExportManifestEntity( "dir2" );
    assertNotNull( entityR1 );
    assertEquals( "Path value", "dir2", entityR1.getPath() );
  }

  @Test
  public void testMarshal() {
    try {
      exportManifest.toXml( System.out );
    } catch ( Exception e ) {
      fail( "Could not marshal to XML " + e );
    }
  }

  @Test
  public void testUnMarshal() {
    String xml = XmlToString();
    ExportManifest importManifest = null;
    ByteArrayInputStream input = new ByteArrayInputStream( xml.getBytes() );
    try {
      importManifest = ExportManifest.fromXml( input );
    } catch ( JAXBException e ) {
      fail( "Could not un-marshal to object " + e );
    }
    ExportManifestEntity fileEntity = importManifest.getExportManifestEntity( "dir2/file1" );
    assertNotNull( fileEntity );
    assertEquals( "dir2/file1", fileEntity.getPath() );
    assertNotNull( fileEntity.getEntityMetaData() );
    assertFalse( fileEntity.getEntityMetaData().isIsFolder() );

    fileEntity = importManifest.getExportManifestEntity( "dir2" );
    assertNotNull( fileEntity );
    assertNotNull( fileEntity.getEntityMetaData() );
    assertTrue( fileEntity.getEntityMetaData().isIsFolder() );

    RepositoryFile r = fileEntity.getRepositoryFile();
    try {
      RepositoryFileAcl rfa = fileEntity.getRepositoryFileAcl();
      assertNotNull( rfa.getAces() );
    } catch ( ExportManifestFormatException e ) {
      e.printStackTrace();
      fail( "Could not un-marshal to RepositoryFileAcl" );
    }

    assertEquals( 1, importManifest.getMetadataList().size() );
    assertEquals( 1, importManifest.getMondrianList().size() );
    assertEquals( 1, importManifest.getScheduleList().size() );
    assertEquals( 1, importManifest.getDatasourceList().size() );

    ExportManifestMondrian mondrian1 = importManifest.getMondrianList().get( 0 );
    assertEquals( "cat1", mondrian1.getCatalogName() );
    assertTrue( mondrian1.getParameters().containsKey( "testKey" ) );
    assertEquals( "testValue", mondrian1.getParameters().get( "testKey" ) );
    assertEquals( "testMondrian.xml", mondrian1.getFile() );

    ExportManifestMetadata metadata1 = importManifest.getMetadataList().get( 0 );
    assertEquals( "testDomain", metadata1.getDomainId() );
    assertEquals( "testMetadata.xml", metadata1.getFile() );

    DatabaseConnection connection = importManifest.getDatasourceList().get( 0 );
    assertEquals( "SampleData", connection.getDatabaseName() );
    assertEquals( "9001", connection.getDatabasePort() );
    assertEquals( "Hypersonic", connection.getDatabaseType().getName() );
    assertEquals( "HYPERSONIC", connection.getDatabaseType().getShortName() );
    assertEquals( "localhost", connection.getHostname() );
    assertEquals( "pentaho_user", connection.getUsername() );
    assertEquals( "password", connection.getPassword() );
    assertEquals( 20, connection.getMaximumPoolSize() );

  }

  @Test
  public void testXmlToString() {
    String s = XmlToString();
    assertNotNull( s );
  }

  private String XmlToString() {
    String s = null;
    try {
      s = exportManifest.toXmlString();
    } catch ( JAXBException e ) {
      e.printStackTrace();
      fail( "Could not marshal to XML to string " + e );
    }
    return s;
  }

  private RepositoryFile createMockRepositoryFile( String path, boolean isFolder ) {
    Date createdDate = new Date();
    Date lastModeDate = new Date();
    Date lockDate = new Date();
    Date deletedDate = new Date();
    String baseName = path.substring( path.lastIndexOf( "/" ) + 1 );
    RepositoryFile mockRepositoryFile =
        new RepositoryFile( "12345", baseName, isFolder, false, false, "versionId", path, createdDate, lastModeDate,
            false, "lockOwner", "lockMessage", lockDate, "en_US", "title", "description",
            "/original/parent/folder/path", deletedDate, 4096, "creatorId", null );
    return mockRepositoryFile;
  }

  private RepositoryFileAcl createMockRepositoryAcl( Serializable id, String owner, boolean entriesInheriting,
      List<RepositoryFileAce> aces ) {
    RepositoryFileSid ownerSid = new RepositoryFileSid( owner );
    return new RepositoryFileAcl( id, ownerSid, entriesInheriting, aces );
  }

  private RepositoryFileAce createMockAce( String recipientName, String recipientType, RepositoryFilePermission first,
      RepositoryFilePermission... rest ) {
    RepositoryFileSid.Type type = RepositoryFileSid.Type.valueOf( recipientType );
    RepositoryFileSid recipient = new RepositoryFileSid( recipientName, type );
    return new RepositoryFileAce( recipient, EnumSet.of( first, rest ) );
  }

}
