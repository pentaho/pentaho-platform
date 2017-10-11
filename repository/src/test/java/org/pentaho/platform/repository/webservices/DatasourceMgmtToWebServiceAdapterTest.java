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
 * Copyright 2006 - 2017 Hitachi Vantara.  All rights reserved.
 */

package org.pentaho.platform.repository.webservices;

import junit.framework.TestCase;
import org.junit.Test;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.repository.JcrBackedDatasourceMgmtService;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.READ;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.WRITE;

@SuppressWarnings( "nls" )
public class DatasourceMgmtToWebServiceAdapterTest extends TestCase {

  private static final String EXP_DBMETA_NAME = "haha";

  private static final String EXP_DBMETA_HOSTNAME = "acme";

  private static final String EXP_DBMETA_PORT = "10521";

  private static final String EXP_UPDATED_DBMETA_NAME = "hahaUpdated";

  private static final String EXP_UPDATED_DBMETA_HOSTNAME = "acmeUpdated";

  private static final String EXP_UPDATED_DBMETA_PORT = "10522";

  public static final String EXP_LOGIN = "admin";

  private static final String FOLDER_PDI = "pdi";

  private static final String FOLDER_DATABASES = "databases"; //$NON-NLS-1$
  private DatasourceMgmtToWebServiceAdapter adapter;
  private IDatasourceMgmtWebService datasourceMgmtWS;
  private IDatasourceMgmtService datasourceMgmtService;

  @Override
  protected void setUp() throws Exception {
    IUnifiedRepository repository =
        new MockUnifiedRepository( new MockUnifiedRepository.SpringSecurityCurrentUserProvider() );
    datasourceMgmtService = new JcrBackedDatasourceMgmtService( repository, new DatabaseDialectService() );
    datasourceMgmtWS = new DefaultDatasourceMgmtWebService( datasourceMgmtService );
    adapter = new DatasourceMgmtToWebServiceAdapter( datasourceMgmtWS );
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken( MockUnifiedRepository.root().getName(), null,
                new ArrayList<GrantedAuthority>() ) );
    repository.createFolder( repository.getFile( "/etc" ).getId(), new RepositoryFile.Builder( FOLDER_PDI ).folder(
        true ).build(), new RepositoryFileAcl.Builder( MockUnifiedRepository.root() ).ace(
          MockUnifiedRepository.everyone(), READ, WRITE ).build(), null );
    repository.createFolder( repository.getFile( "/etc/pdi" ).getId(), new RepositoryFile.Builder( FOLDER_DATABASES )
        .folder( true ).build(), null );
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken( EXP_LOGIN, null, new ArrayList<GrantedAuthority>() ) );

    KettleClientEnvironment.init();

  }

  @Test
  public void testEverything() throws Exception {
    String id = adapter.createDatasource( createDatabaseConnection( "testDatabase" ) );
    assertNotNull( id );
    IDatabaseConnection databaseConnection = adapter.getDatasourceByName( "testDatabase" );
    assertNotNull( databaseConnection );
    assertEquals( EXP_DBMETA_HOSTNAME, databaseConnection.getHostname() );
    IDatabaseConnection databaseConnection1 = adapter.getDatasourceById( id );
    assertEquals( EXP_DBMETA_HOSTNAME, databaseConnection.getHostname() );
    String id1 = adapter.createDatasource( createDatabaseConnection( "testDatabase1" ) );
    assertNotNull( id1 );
    IDatabaseConnection databaseConnection2 = adapter.getDatasourceByName( "testDatabase1" );
    assertNotNull( databaseConnection2 );
    assertEquals( EXP_DBMETA_HOSTNAME, databaseConnection1.getHostname() );
    IDatabaseConnection databaseConnection3 = adapter.getDatasourceById( id1 );
    assertEquals( EXP_DBMETA_HOSTNAME, databaseConnection1.getHostname() );
    List<IDatabaseConnection> databaseConnections = adapter.getDatasources();
    assertEquals( 2, databaseConnections.size() );
    List<String> ids = adapter.getDatasourceIds();
    assertEquals( 2, ids.size() );
    updateDatabaseConnection( databaseConnection );
    String id2 = adapter.updateDatasourceByName( "testDatabase", databaseConnection );
    assertNotNull( id2 );
    IDatabaseConnection updatedDatabaseConnection = adapter.getDatasourceByName( "testDatabase" );
    String id3 = adapter.updateDatasourceById( id, databaseConnection );
    assertNotNull( id3 );
    updatedDatabaseConnection = adapter.getDatasourceById( id3 );
    assertNotNull( updatedDatabaseConnection );
    assertEquals( EXP_UPDATED_DBMETA_HOSTNAME, databaseConnection.getHostname() );

    String id4 = adapter.createDatasource( createDatabaseConnection( "testDatabase3" ) );
    assertNotNull( id4 );
    adapter.deleteDatasourceByName( "testDatabase3" );
    IDatabaseConnection deletedDatabaseConnection = adapter.getDatasourceByName( "testDatabase3" );
    assertNull( deletedDatabaseConnection );
    IDatabaseConnection deletedDatabaseConnection111 = adapter.getDatasourceById( id4 );
    assertNull( deletedDatabaseConnection111 );

    String id5 = adapter.createDatasource( createDatabaseConnection( "testDatabase4" ) );
    assertNotNull( id5 );
    adapter.deleteDatasourceById( id5 );
    IDatabaseConnection deletedDatabaseConnection11 = adapter.getDatasourceByName( "testDatabase4" );
    assertNull( deletedDatabaseConnection11 );
    IDatabaseConnection deletedDatabaseConnection1 = adapter.getDatasourceById( id5 );
    assertNull( deletedDatabaseConnection1 );
  }

  private IDatabaseConnection createDatabaseConnection( final String dbName ) throws Exception {
    IDatabaseConnection dbConnection = new DatabaseConnection();
    dbConnection.setName( dbName );
    dbConnection.setHostname( EXP_DBMETA_HOSTNAME );
    dbConnection.setDatabaseType( mockDatabaseType( "Hypersonic" ) );
    dbConnection.setAccessType( DatabaseAccessType.NATIVE );
    dbConnection.setDatabasePort( EXP_DBMETA_PORT );
    return dbConnection;
  }

  private void updateDatabaseConnection( IDatabaseConnection dbConnection ) throws Exception {
    dbConnection.setHostname( EXP_UPDATED_DBMETA_HOSTNAME );
    dbConnection.setDatabaseType( mockDatabaseType( "Generic database" ) );
    dbConnection.setAccessType( DatabaseAccessType.JNDI );
    dbConnection.setDatabasePort( EXP_UPDATED_DBMETA_PORT );
  }

  private IDatabaseType mockDatabaseType( final String shortName ) {
    IDatabaseType dbType = mock( IDatabaseType.class );
    doReturn( shortName ).when( dbType ).getShortName();
    return dbType;
  }
}
