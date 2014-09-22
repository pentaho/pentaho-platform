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

package org.pentaho.test.platform.security.userroledao;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.security.userroledao.jackrabbit.AbstractJcrBackedUserRoleDao;
import org.pentaho.platform.security.userroledao.jackrabbit.JcrUserRoleDao;

public class AbstractJcrBackedUserRoleDaoTest {

  private AbstractJcrBackedUserRoleDao roleDao;
  private IPentahoSession pentahoSession;

  @Before
  public void prepare() throws NamespaceException {
    roleDao = new JcrUserRoleDao( null, null, null, null, null, null, null, null, null, null, null, null, null, null );
    pentahoSession = mock( IPentahoSession.class );
    PentahoSessionHolder.setSession( pentahoSession );
  }

  @Test( expected = RepositoryException.class )
  public void testIsMyself() throws NotFoundException, RepositoryException {
    Session jcrSession = mock( Session.class );
    ITenant tenant = mock( ITenant.class );
    String[] newRoles = { "first_role" };
    setUserName( "user-mock" );

    roleDao.setUserRoles( jcrSession, tenant, "user-mock", newRoles );
  }

  @Test( expected = RepositoryException.class )
  public void testIsDefaultAdmin() throws NotFoundException, RepositoryException, IOException {
    FileInputStream fis = new FileInputStream( "src/repository.spring.properties" );
    Properties properties = new Properties();
    properties.load( fis );
    String adminDefaultUserName = properties.getProperty( "singleTenantAdminUserName" );

    Session jcrSession = mock( Session.class );
    ITenant tenant = mock( ITenant.class );
    String[] newRoles = { "first_role" };
    setUserName( adminDefaultUserName );

    roleDao.setUserRoles( jcrSession, tenant, adminDefaultUserName, newRoles );
  }

  private void setUserName( String newUserName ) {
    when( pentahoSession.getName() ).thenReturn( newUserName );
  }

}
