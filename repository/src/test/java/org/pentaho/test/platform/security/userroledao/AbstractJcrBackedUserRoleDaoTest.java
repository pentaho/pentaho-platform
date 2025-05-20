/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.security.userroledao.jackrabbit.AbstractJcrBackedUserRoleDao;
import org.pentaho.platform.security.userroledao.jackrabbit.JcrUserRoleDao;

@RunWith( MockitoJUnitRunner.class )
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
    FileInputStream fis = new FileInputStream( "src/main/resources/repository.spring.properties" );
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
