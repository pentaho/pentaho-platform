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
 * Copyright (c) 2002-2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.metadata;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.references.SingletonPentahoObjectReference;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.READ;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.WRITE;
import static org.pentaho.test.platform.repository2.unified.MockUnifiedRepository.everyone;

@RunWith ( MockitoJUnitRunner.class )
public class PentahoMetadataDomainRepositoryTest {

  private IUnifiedRepository repos = new MockUnifiedRepository( new UserProvider() );
  @Mock private IUserRoleListService roleListService;

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  private PentahoMetadataDomainRepository domainRepos;

  private InputStream xmi1, xmi2, xmi3;

  @Before public void before()
    throws IOException, DomainStorageException, DomainIdNullException, DomainAlreadyExistsException {
    repos.createFolder( repos.getFile( "/etc" ).getId(),
      new RepositoryFile.Builder( "metadata" ).folder( true ).build(),
      new RepositoryFileAcl.Builder( MockUnifiedRepository.root() ).ace( everyone(), READ, WRITE ).build(), null );
    File jaFile = tempFolder.newFile( "messages_ja.properties" );
    File frFile = tempFolder.newFile( "messages_fr_FR.properties" );
    PentahoSystem.registerReference(
      new SingletonPentahoObjectReference.Builder<>( String.class ).object( "__root__" )
        .attributes( Collections.singletonMap( "id", "singleTenantAdminUserName" ) ).build() );
    PentahoSystem.registerReference(
      new SingletonPentahoObjectReference.Builder<>( IUnifiedRepository.class ).object( repos ).build() );
    SecurityHelper.setMockInstance( new MockedSecurityHelper() );

    xmi1 = getXmiInputStream();
    xmi2 = getXmiInputStream();
    xmi3 = getXmiInputStream();

    domainRepos = new PentahoMetadataDomainRepository( repos );
    domainRepos.addLocalizationFile( "testDomain1.xmi", "ja", new FileInputStream( jaFile ), false );
    domainRepos.addLocalizationFile( "testDomain1.xmi", "fr_FR", new FileInputStream( frFile ), false );
    domainRepos.storeDomain( xmi1, "testDomain1.xmi", false );
    domainRepos.storeDomain( xmi2, "testDomain_noLocaleFiles.xmi", false );
    domainRepos.storeDomain( xmi3, "testDomain_doesntEndIn_dotXMI", false );
  }

  @After public void after() throws IOException {
    xmi1.close();
    xmi2.close();
    xmi3.close();
  }

  private InputStream getXmiInputStream() throws FileNotFoundException {
    return new FileInputStream( new File( TestResourceLocation.TEST_RESOURCES + "/ImportTest/steel-wheels.xmi" ) );
  }

  @Test public void testGetDomainFilesNoSuchDomain() {
    assertTrue( domainRepos.getDomainFilesData( "NOSUCH_DOMAIN" ).isEmpty() );
  }

  @Test public void testGetDomainFiles() {
    Map<String, InputStream> domainFiles = domainRepos.getDomainFilesData( "testDomain1.xmi" );
    assertThat( domainFiles.size(), equalTo( 3 ) );
    assertThat( domainFiles.keySet().stream().sorted().collect( Collectors.joining( "," ) ),
      equalTo( "messages_fr_FR.properties,messages_ja.properties,testDomain1.xmi" ) );

    domainFiles = domainRepos.getDomainFilesData( "testDomain_noLocaleFiles.xmi" );
    assertThat( domainFiles.size(), equalTo( 1 ) );
    assertThat( domainFiles.keySet().stream().sorted().collect( Collectors.joining( "," ) ),
      equalTo( "testDomain_noLocaleFiles.xmi" ) );

    domainFiles = domainRepos.getDomainFilesData( "testDomain_doesntEndIn_dotXMI" );
    assertThat( domainFiles.size(), equalTo( 1 ) );
    assertThat( domainFiles.keySet().stream().sorted().collect( Collectors.joining( "," ) ),
      equalTo( "testDomain_doesntEndIn_dotXMI.xmi" ) );

  }


  public static class UserProvider implements MockUnifiedRepository.ICurrentUserProvider {
    public String getUser() {
      return "__root__";
    }

    public List<String> getRoles() {
      return Arrays.asList( "__everyone__", "role2" );
    }
  }

  private class MockedSecurityHelper extends SecurityHelper {
    @Override public IUserRoleListService getUserRoleListService() {
      return roleListService;
    }
  }
}
