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
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport.exportManifest;

import junit.framework.TestCase;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.plugin.services.importexport.ExportManifestUserSetting;
import org.pentaho.platform.plugin.services.importexport.UserExport;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseAccessType;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseConnection;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseType;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestDto;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetadata;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMondrian;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

public class ExportManifestTest extends TestCase {
  private ExportManifest exportManifest;

  public ExportManifestTest() {
    String rootFolder = "/dir1/";
    exportManifest = new ExportManifest();
    ExportManifestDto.ExportManifestInformation exportManifestInformation = exportManifest.getManifestInformation();
    exportManifestInformation.setExportBy( "MickeyMouse" );
    exportManifestInformation.setExportDate( "2013-01-01" );
    exportManifestInformation.setRootFolder( rootFolder );

    List<RepositoryFileAce> aces1 = new ArrayList<>();
    aces1.add( createMockAce( "admin-/pentaho/tenant0", "USER", RepositoryFilePermission.READ,
      RepositoryFilePermission.WRITE ) );
    aces1.add( createMockAce( "TenantAdmin-/pentaho/tenant0", "ROLE", RepositoryFilePermission.READ ) );
    RepositoryFile repoDir2 = createMockRepositoryFile( "/dir1/dir2", true );
    RepositoryFileAcl repoDir2Acl = createMockRepositoryAcl( "acl2", "admin", false, aces1 );
    RepositoryFile repoFile3 = createMockRepositoryFile( "/dir1/dir2/file1", false );
    RepositoryFile badRepoFile = createMockRepositoryFile( "/baddir/dir2/file1", false );

    try {
      exportManifest.add( repoDir2, repoDir2Acl );
      exportManifest.add( repoFile3, null );
    } catch ( Exception e ) {
      fail( e.toString() );
    }

    try {
      exportManifest.add( badRepoFile, null );
      fail( "Bad path did not generate a ExportManifestFormatException" );
    } catch ( ExportManifestFormatException e ) {
      //ignored
    }

    ExportManifestEntity entity2 = exportManifest.getExportManifestEntity( "dir2" );
    assertNotNull( entity2 );
    ExportManifestEntity entity3 = exportManifest.getExportManifestEntity( "dir2/file1" );
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

    // JobScheduleRequest
    JobScheduleRequest jobScheduleRequest = new JobScheduleRequest();
    HashMap<String, String> pdiParameters = new HashMap<>();
    pdiParameters.put( "parm1", "val1" );
    jobScheduleRequest.setPdiParameters( pdiParameters );
    jobScheduleRequest.setJobName( "jobName" );
    jobScheduleRequest.setJobState( Job.JobState.UNKNOWN );
    jobScheduleRequest.setActionClass( "actionClass" );
    jobScheduleRequest.setDuration( 3600 );
    jobScheduleRequest.setTimeZone( "someTimeZone" );
    jobScheduleRequest.setJobId( "jobId" );
    exportManifest.addSchedule( jobScheduleRequest );

    // Datasource
    DatabaseConnection connection = new DatabaseConnection();
    connection.setAccessType( DatabaseAccessType.NATIVE );
    connection.setDatabaseName( "SampleData" );
    connection.setDatabasePort( "9001" );
    DatabaseType type = new DatabaseType();
    type.setName( "Hypersonic" );
    type.setShortName( "HYPERSONIC" );
    type.setDefaultDatabaseName( "defaultDatabaseName" );
    type.setDefaultDatabasePort( 8888 );
    type.setExtraOptionsHelpUrl( "extraOptionsHelpUrl" );
    DatabaseType.DefaultOptions defaultOptions = new DatabaseType.DefaultOptions();
    DatabaseType.DefaultOptions.Entry e = new DatabaseType.DefaultOptions.Entry();
    e.setKey( "someKey" );
    e.setValue( "someValue" );
    defaultOptions.getEntry().add( e );
    type.setDefaultOptions( defaultOptions );
    connection.setDatabaseType( type );
    connection.setHostname( "localhost" );
    connection.setUsername( "pentaho_user" );
    connection.setPassword( null );
    connection.setMaximumPoolSize( 20 );

    exportManifest.addDatasource( connection );

    // UserExport
    UserExport user = new UserExport();
    user.setUsername( "pentaho" );
    user.setRole( "open source champion" );
    user.setPassword( "123456" );
    user.addUserSetting( new ExportManifestUserSetting( "theme", "crystal" ) );
    user.addUserSetting( new ExportManifestUserSetting( "language", "en_US" ) );
    exportManifest.addUserExport( user );
    exportManifest.addGlobalUserSetting( new ExportManifestUserSetting( "viewHiddenFiles", "false" ) );
  }

  public void testEntityAccess() {
    ExportManifestEntity entityR1 = exportManifest.getExportManifestEntity( "dir2" );
    assertNotNull( entityR1 );
    assertEquals( "Path value", "dir2", entityR1.getPath() );
  }

  public void testMarshal() {
    try {
      exportManifest.toXml( System.out );
    } catch ( Exception e ) {
      fail( "Could not marshal to XML " + e );
    }
  }

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
    assertEquals( "dir2", r.getPath() );
    assertTrue( r.isFolder() );

    try {
      RepositoryFileAcl rfa = fileEntity.getRepositoryFileAcl();
      assertNotNull( rfa.getAces() );
    } catch ( ExportManifestFormatException e ) {
      e.printStackTrace();
      fail( "Could not un-marshal to RepositoryFileAcl" );
    }

    // JobScheduleRequest
    assertNotNull( importManifest.getScheduleList() );
    assertEquals( 1, importManifest.getScheduleList().size() );
    JobScheduleRequest jobScheduleRequest = importManifest.getScheduleList().get( 0 );
    assertNotNull( jobScheduleRequest );
    assertNotNull( jobScheduleRequest.getPdiParameters() );
    assertEquals( 1, jobScheduleRequest.getPdiParameters().size() );
    assertEquals( "val1", jobScheduleRequest.getPdiParameters().get( "parm1" ) );
    assertEquals( "jobName", jobScheduleRequest.getJobName() );
    assertEquals( Job.JobState.UNKNOWN, jobScheduleRequest.getJobState() );
    assertEquals( "actionClass", jobScheduleRequest.getActionClass() );
    assertEquals( 3600, jobScheduleRequest.getDuration() );
    assertEquals( "someTimeZone", jobScheduleRequest.getTimeZone() );
    assertEquals( "jobId", jobScheduleRequest.getJobId() );

    // Mondrian
    assertNotNull( importManifest.getMondrianList() );
    assertEquals( 1, importManifest.getMondrianList().size() );
    ExportManifestMondrian mondrian1 = importManifest.getMondrianList().get( 0 );
    assertNotNull( mondrian1 );
    assertEquals( "cat1", mondrian1.getCatalogName() );
    assertTrue( mondrian1.getParameters().containsKey( "testKey" ) );
    assertEquals( "testValue", mondrian1.getParameters().get( "testKey" ) );
    assertEquals( "testMondrian.xml", mondrian1.getFile() );

    // Metadata
    assertNotNull( importManifest.getMetadataList() );
    assertEquals( 1, importManifest.getMetadataList().size() );
    ExportManifestMetadata metadata1 = importManifest.getMetadataList().get( 0 );
    assertNotNull( metadata1 );
    assertEquals( "testDomain", metadata1.getDomainId() );
    assertEquals( "testMetadata.xml", metadata1.getFile() );

    // Datasource
    assertNotNull( importManifest.getDatasourceList() );
    assertEquals( 1, importManifest.getDatasourceList().size() );
    DatabaseConnection connection = importManifest.getDatasourceList().get( 0 );
    assertNotNull( connection );
    assertEquals( "SampleData", connection.getDatabaseName() );
    assertEquals( "9001", connection.getDatabasePort() );
    assertEquals( "Hypersonic", connection.getDatabaseType().getName() );
    assertEquals( "HYPERSONIC", connection.getDatabaseType().getShortName() );
    assertEquals( "defaultDatabaseName", connection.getDatabaseType().getDefaultDatabaseName() );
    assertEquals( 8888, connection.getDatabaseType().getDefaultDatabasePort() );
    assertEquals( "extraOptionsHelpUrl", connection.getDatabaseType().getExtraOptionsHelpUrl() );
    assertNotNull( connection.getDatabaseType().getDefaultOptions() );
    assertNotNull( connection.getDatabaseType().getDefaultOptions().getEntry() );
    assertEquals( 1, connection.getDatabaseType().getDefaultOptions().getEntry().size() );
    assertNotNull( connection.getDatabaseType().getDefaultOptions().getEntry().get( 0 ) );
    assertEquals( "someKey", connection.getDatabaseType().getDefaultOptions().getEntry().get( 0 ).getKey() );
    assertEquals( "someValue", connection.getDatabaseType().getDefaultOptions().getEntry().get( 0 ).getValue() );
    assertEquals( "localhost", connection.getHostname() );
    assertEquals( "pentaho_user", connection.getUsername() );
    assertEquals( 20, connection.getMaximumPoolSize() );

    // UserExport
    assertNotNull( importManifest.getUserExports() );
    assertEquals( 1, importManifest.getUserExports().size() );
    UserExport userExport = importManifest.getUserExports().get( 0 );
    assertNotNull( userExport );
    assertEquals( "pentaho", userExport.getUsername() );
    assertEquals( "123456", userExport.getPassword() );
    List<String> userRoles = userExport.getRoles();
    assertNotNull( userRoles );
    assertEquals( 1, userRoles.size() );
    assertEquals( "open source champion", userRoles.get( 0 ) );
    assertNotNull( userExport.getUserSettings() );
    assertEquals( 2, userExport.getUserSettings().size() );
    List<ExportManifestUserSetting> userSettings = userExport.getUserSettings();
    assertEquals( 2, userSettings.size() );
    userSettings.stream().filter( exportManifestUserSetting -> "theme".equals( exportManifestUserSetting.getName() ) )
      .forEach( exportManifestUserSetting -> assertEquals( "crystal", exportManifestUserSetting.getValue() ) );
    userSettings.stream()
      .filter( exportManifestUserSetting -> "language".equals( exportManifestUserSetting.getName() ) )
      .forEach( exportManifestUserSetting -> assertEquals( "en_US", exportManifestUserSetting.getValue() ) );
  }

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
    return
      new RepositoryFile( "12345", baseName, isFolder, false, false, false, "versionId", path, createdDate,
        lastModeDate,
        false, "lockOwner", "lockMessage", lockDate, "en_US", "title", "description",
        "/original/parent/folder/path", deletedDate, 4096, "creatorId", null );
  }

  public void testToXml_XmlUnsafeManifestParameters() {
    final String keyFirst = "DataSource";
    final String keySecond = "DynamicSchemaProcessor";
    final String valueFirst = "\"DS \"Test's\" & <Fun>\"";
    final String valueSecond = "\"DSP's & \"Other\" <stuff>\"";

    ExportManifestMondrian mondrian = new ExportManifestMondrian();
    Parameters mondrianParameters = new Parameters();

    mondrianParameters.put( keyFirst, valueFirst );
    mondrianParameters.put( keySecond, valueSecond );
    mondrian.setParameters( mondrianParameters );
    mondrian.setCatalogName( "mondrian" );
    mondrian.setXmlaEnabled( false );

    exportManifest.addMondrian( mondrian );
    try ( ByteArrayOutputStream out = new ByteArrayOutputStream() ) {
      try {
        exportManifest.toXml( out );
      } catch ( Exception e ) {
        fail( "Could not marshal to XML " + e );
      }
      try ( ByteArrayInputStream inputStream = new ByteArrayInputStream( out.toByteArray() ) ) {
        ExportManifest ex = null;
        try {
          ex = ExportManifest.fromXml( inputStream );
        } catch ( Exception e ) {
          fail( "Could not un-marshal from XML " + e );
        }
        List<ExportManifestMondrian> catalogs = ex.getMondrianList();
        assertNotNull( catalogs );
        assertFalse( catalogs.isEmpty() );

        ExportManifestMondrian mondrianCatalog = null;

        for ( ExportManifestMondrian catalog : catalogs ) {
          if ( "mondrian".equals( catalog.getCatalogName() ) ) {
            mondrianCatalog = catalog;
            break;
          }
        }
        assertNotNull( mondrianCatalog );
        Parameters parameters = mondrianCatalog.getParameters();
        assertNotNull( parameters );
        assertFalse( parameters.isEmpty() );

        String parameter = parameters.get( keyFirst );
        assertNotNull( parameter );
        assertEquals( valueFirst, parameter );

        parameter = parameters.get( keySecond );
        assertNotNull( parameter );
        assertEquals( valueSecond, parameter );
      }
    } catch ( Exception e ) {
      fail( e.toString() );
    }
  }

  public void testToXml_XmlUnsafeEscaped() {
    final String keyFirst = "DataSource";
    final String keySecond = "DynamicSchemaProcessor";
    final String valueFirst = "\"DS \"Test's\" & <Fun>\"";
    final String valueSecond = "\"DSP's & \"Other\" <stuff>\"";

    final String expectedValueFirst = "&quot;DS &quot;Test's&quot; &amp; &lt;Fun&gt;&quot;";
    final String expectedValueSecond = "&quot;DSP's &amp; &quot;Other&quot; &lt;stuff&gt;&quot;";

    ExportManifestMondrian mondrian = new ExportManifestMondrian();
    Parameters mondrianParameters = new Parameters();

    mondrianParameters.put( keyFirst, valueFirst );
    mondrianParameters.put( keySecond, valueSecond );
    mondrian.setParameters( mondrianParameters );
    mondrian.setCatalogName( "mondrian" );
    mondrian.setXmlaEnabled( false );

    String lineParamFirst = null;
    String lineParamSecond = null;

    exportManifest.addMondrian( mondrian );

    try ( ByteArrayOutputStream out = new ByteArrayOutputStream() ) {
      exportManifest.toXml( out );
      try ( ByteArrayInputStream inputStream = new ByteArrayInputStream( out.toByteArray() );
            BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) )
      ) {
        String line;
        while ( ( line = reader.readLine() ) != null ) {
          if ( line.contains( keyFirst ) ) {
            lineParamFirst = line;
          }
          if ( line.contains( keySecond ) ) {
            lineParamSecond = line;
          }
          if ( lineParamFirst != null && lineParamSecond != null ) {
            break;
          }
        }
      }
    } catch ( Exception e ) {
      fail( "Could not marshal to XML " + e );
    }
    assertNotNull( lineParamFirst );
    assertTrue( lineParamFirst.contains( "value=\"" + expectedValueFirst + "\"" ) );
    assertNotNull( lineParamSecond );
    assertTrue( lineParamSecond.contains( "value=\"" + expectedValueSecond + "\"" ) );
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
