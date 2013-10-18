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

package org.pentaho.test.platform.plugin;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;
import org.pentaho.platform.plugin.action.mondrian.mapper.MondrianLookupMapUserRoleListMapper;
import org.pentaho.platform.plugin.action.mondrian.mapper.MondrianOneToOneUserRoleListMapper;
import org.pentaho.platform.plugin.action.mondrian.mapper.MondrianUserSessionUserRoleListMapper;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.dao.DataAccessException;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@SuppressWarnings( "nls" )
public class UserRoleMapperTest {

  private MicroPlatform microPlatform;

  @Before
  public void init0() {
    microPlatform = new MicroPlatform( "test-src/solution" );
    microPlatform.define( ISolutionEngine.class, SolutionEngine.class );
    microPlatform.define( IUnifiedRepository.class, FileSystemBackedUnifiedRepository.class, Scope.GLOBAL );
    microPlatform.define( IMondrianCatalogService.class, MondrianCatalogHelper.class, Scope.GLOBAL );
    microPlatform.define( "connection-SQL", SQLConnection.class );
    microPlatform.define( "connection-MDX", MDXConnection.class );
    microPlatform.define( IDBDatasourceService.class, JndiDatasourceService.class, Scope.GLOBAL );
    microPlatform.define( IUserRoleListService.class, TestUserRoleListService.class, Scope.GLOBAL );
    microPlatform.define( UserDetailsService.class, TestUserDetailsService.class, Scope.GLOBAL );
    FileSystemBackedUnifiedRepository repo =
        (FileSystemBackedUnifiedRepository) PentahoSystem.get( IUnifiedRepository.class );
    repo.setRootDir( new File( "test-src/solution" ) );
    try {
      microPlatform.start();
    } catch ( PlatformInitializationException ex ) {
      Assert.fail();
    }

    MondrianCatalogHelper catalogService = (MondrianCatalogHelper) PentahoSystem.get( IMondrianCatalogService.class );
    catalogService.setDataSourcesConfig( "file:"
        + PentahoSystem.getApplicationContext().getSolutionPath( "test/analysis/test-datasources.xml" ) );

    // JNDI
    System.setProperty( "java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory" );
    System.setProperty( "org.osjava.sj.root", "test-src/solution/system/simple-jndi" );
    System.setProperty( "org.osjava.sj.delimiter", "/" );

  }

  @After
  public void tearDown() {
    microPlatform.stop();
    microPlatform = null;
  }

  @Test
  public void testReadRolesInSchema() throws Exception {
    final MondrianCatalogHelper helper = (MondrianCatalogHelper) PentahoSystem.get( IMondrianCatalogService.class );
    Assert.assertNotNull( helper );
    MondrianCatalog mc = SecurityHelper.getInstance().runAsUser( "admin", new Callable<MondrianCatalog>() {
      @Override
      public MondrianCatalog call() throws Exception {
        return helper.getCatalog( "SteelWheelsRoles", PentahoSessionHolder.getSession() );
      }
    } );

    Assert.assertNotNull( mc );
    MondrianSchema ms = mc.getSchema();
    Assert.assertNotNull( ms );
    String[] roleNames = ms.getRoleNames();
    Assert.assertNotNull( roleNames );
    Assert.assertEquals( 2, roleNames.length );
    Assert.assertEquals( "Role1", roleNames[0] );
    Assert.assertEquals( "Role2", roleNames[1] );
  }

  @Test
  public void testReadRolesInPlatform() throws Exception {
    SecurityHelper.getInstance().runAsUser( "admin", new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        Authentication auth = SecurityHelper.getInstance().getAuthentication();
        Assert.assertNotNull( auth );
        GrantedAuthority[] gAuths = auth.getAuthorities();
        Assert.assertNotNull( gAuths );
        Assert.assertEquals( 3, gAuths.length );
        Assert.assertEquals( "ceo", gAuths[0].getAuthority() );
        Assert.assertEquals( "Admin", gAuths[1].getAuthority() );
        Assert.assertEquals( "Authenticated", gAuths[2].getAuthority() );
        return null;
      }
    } );
  }

  @Test
  public void testMondrianUserSessionUserRoleListMapper() throws Exception {
    final MondrianUserSessionUserRoleListMapper mapper = new MondrianUserSessionUserRoleListMapper();
    mapper.setSessionProperty( "rolesAttribute" );

    try {
      String[] roles = SecurityHelper.getInstance().runAsUser( "admin", new Callable<String[]>() {
        @Override
        public String[] call() throws Exception {
          IPentahoSession session = PentahoSessionHolder.getSession();
          session.setAttribute( "rolesAttribute", new Object[] { "mondrianRole1", "mondrianRole2", "mondrianRole3" } );
          return mapper.mapConnectionRoles( session, "SteelWheelsRoles" );
        }
      } );

      Assert.assertNotNull( roles );
      Assert.assertEquals( 3, roles.length );
      Assert.assertEquals( "mondrianRole1", roles[0] );
      Assert.assertEquals( "mondrianRole2", roles[1] );
      Assert.assertEquals( "mondrianRole3", roles[2] );
    } catch ( PentahoAccessControlException e ) {
      Assert.fail( e.getMessage() );
    }
  }

  @Test
  public void testNoMatchMondrianUserSessionUserRoleListMapper() throws Exception {
    final MondrianUserSessionUserRoleListMapper mapper = new MondrianUserSessionUserRoleListMapper();
    mapper.setSessionProperty( "rolesAttribute" );

    try {
      String[] roles = SecurityHelper.getInstance().runAsUser( "admin", new Callable<String[]>() {
        @Override
        public String[] call() throws Exception {
          return mapper.mapConnectionRoles( PentahoSessionHolder.getSession(), "SteelWheelsRoles" );
        }
      } );

      Assert.assertNull( roles );
    } catch ( PentahoAccessControlException e ) {
      Assert.fail( e.getMessage() );
    }
  }

  @Test
  public void testLookupMapUserRoleListMapper() throws Exception {
    Map<String, String> lookup = new HashMap<String, String>();
    lookup.put( "ceo", "Role1" );
    lookup.put( "Not Pentaho", "Role2" );
    lookup.put( "Not Mondrian or Pentaho", "Role3" );

    final MondrianLookupMapUserRoleListMapper mapper = new MondrianLookupMapUserRoleListMapper();
    mapper.setLookupMap( lookup );

    try {
      String[] roles = SecurityHelper.getInstance().runAsUser( "admin", new Callable<String[]>() {
        @Override
        public String[] call() throws Exception {
          return mapper.mapConnectionRoles( PentahoSessionHolder.getSession(), "SteelWheelsRoles" );
        }
      } );
      Assert.assertNotNull( roles );
      Assert.assertEquals( 1, roles.length );
      Assert.assertEquals( "Role1", roles[0] );
    } catch ( PentahoAccessControlException e ) {
      Assert.fail( e.getMessage() );
    }
  }

  @Test
  public void testNoMatchLookupMapUserRoleListMapper() throws Exception {
    Map<String, String> lookup = new HashMap<String, String>();
    lookup.put( "No Match", "Role1" );
    lookup.put( "No Match Here Either", "Role2" );

    final MondrianLookupMapUserRoleListMapper mapper = new MondrianLookupMapUserRoleListMapper();
    mapper.setLookupMap( lookup );

    mapper.setFailOnEmptyRoleList( true );
    try {
      SecurityHelper.getInstance().runAsUser( "admin", new Callable<String[]>() {
        @Override
        public String[] call() throws Exception {
          return mapper.mapConnectionRoles( PentahoSessionHolder.getSession(), "SteelWheelsRoles" );
        }
      } );
      Assert.fail();
    } catch ( PentahoAccessControlException e ) {
      // no op.
    }

    mapper.setFailOnEmptyRoleList( false );

    try {
      String[] roles = SecurityHelper.getInstance().runAsUser( "admin", new Callable<String[]>() {
        @Override
        public String[] call() throws Exception {
          return mapper.mapConnectionRoles( PentahoSessionHolder.getSession(), "SteelWheelsRoles" );
        }
      } );
      Assert.assertNull( roles );
    } catch ( PentahoAccessControlException e ) {
      Assert.fail( e.getMessage() );
    }
  }

  @Test
  public void testMondrianOneToOneUserRoleListMapper() throws Exception {
    final IConnectionUserRoleMapper mapper = new MondrianOneToOneUserRoleListMapper();
    try {
      String[] roles = SecurityHelper.getInstance().runAsUser( "simplebob", new Callable<String[]>() {
        @Override
        public String[] call() throws Exception {
          return mapper.mapConnectionRoles( PentahoSessionHolder.getSession(), "SteelWheelsRoles" );
        }
      } );

      Assert.assertNotNull( roles );
      Assert.assertEquals( 2, roles.length );
      Assert.assertEquals( "Role1", roles[0] );
      Assert.assertEquals( "Role2", roles[1] );

    } catch ( PentahoAccessControlException e ) {
      Assert.fail( e.getMessage() );
    }
  }

  @Test
  public void testNoMatchMondrianOneToOneUserRoleListMapper() throws Exception {
    final MondrianOneToOneUserRoleListMapper mapper = new MondrianOneToOneUserRoleListMapper();
    mapper.setFailOnEmptyRoleList( true );
    try {
      SecurityHelper.getInstance().runAsUser( "admin", new Callable<String[]>() {
        @Override
        public String[] call() throws Exception {
          return mapper.mapConnectionRoles( PentahoSessionHolder.getSession(), "SteelWheelsRoles" );
        }
      } );
      Assert.fail();
    } catch ( PentahoAccessControlException e ) {
      // No op.
    }
    mapper.setFailOnEmptyRoleList( false );
    try {
      String[] roles = SecurityHelper.getInstance().runAsUser( "simplebob", new Callable<String[]>() {
        @Override
        public String[] call() throws Exception {
          return mapper.mapConnectionRoles( PentahoSessionHolder.getSession(), "SteelWheelsRoles" );
        }
      } );
      Assert.assertArrayEquals( new String[] { "Role1", "Role2" }, roles );
    } catch ( PentahoAccessControlException e ) {
      Assert.fail( e.getMessage() );
    }
  }

  public static class TestUserRoleListService implements IUserRoleListService {

    @Override
    public List<String> getAllRoles() {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getAllUsers() {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getAllRoles( ITenant tenant ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<String> getAllUsers( ITenant tenant ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<String> getUsersInRole( ITenant tenant, String role ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<String> getRolesForUser( ITenant tenant, String username ) {
      if ( username.equals( "admin" ) ) {
        return Arrays.asList( new String[] { "ceo", "Admin", "Authenticated" } );
      } else if ( username.equals( "simplebob" ) ) {
        return Arrays.asList( new String[] { "Role1", "Role2" } );
      }
      return Collections.emptyList();
    }

    @Override
    public List<String> getSystemRoles() {
      throw new UnsupportedOperationException();
    }

  }

  public static class TestUserDetailsService implements UserDetailsService {
    public UserDetails loadUserByUsername( final String username ) throws UsernameNotFoundException,
      DataAccessException {
      return new UserDetails() {
        private static final long serialVersionUID = 1L;

        public boolean isEnabled() {
          return true;
        }

        public boolean isCredentialsNonExpired() {
          return true;
        }

        public boolean isAccountNonLocked() {
          return true;
        }

        public boolean isAccountNonExpired() {
          return true;
        }

        public String getUsername() {
          return username;
        }

        public String getPassword() {
          return "password";
        }

        public GrantedAuthority[] getAuthorities() {
          if ( username == null ) {
            return new GrantedAuthority[0];
          }
          if ( username.equals( "admin" ) ) {
            return new GrantedAuthority[] { new GrantedAuthorityImpl( "ceo" ), new GrantedAuthorityImpl( "Admin" ),
              new GrantedAuthorityImpl( "Authenticated" ) };
          } else if ( username.equals( "simplebob" ) ) {
            return new GrantedAuthority[] { new GrantedAuthorityImpl( "Role1" ), new GrantedAuthorityImpl( "Role2" ) };
          }
          return new GrantedAuthority[0];
        }
      };
    }
  }
}
