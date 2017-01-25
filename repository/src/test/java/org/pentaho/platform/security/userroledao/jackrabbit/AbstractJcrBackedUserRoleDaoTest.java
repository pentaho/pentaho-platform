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
 * Copyright 2006 - 2015 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.security.userroledao.jackrabbit;

import org.apache.commons.collections.map.LRUMap;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.core.SessionImpl;
import org.junit.Test;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategySessionFactory;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link org.pentaho.platform.security.userroledao.jackrabbit.AbstractJcrBackedUserRoleDao} Class is created
 * in order to have access to package-private methods.
 *
 * @author Yury_Bakhmutski
 */
public class AbstractJcrBackedUserRoleDaoTest {

  @Test
  public void testConvertToPentahoUserEnableCache() throws RepositoryException {
    AbstractJcrBackedUserRoleDao abstractJcrBackedUserRoleDaoMock = mock( AbstractJcrBackedUserRoleDao.class );
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).convertToPentahoUser( any( User.class ) );

    ITenantedPrincipleNameResolver resolverMock = mock( ITenantedPrincipleNameResolver.class );
    when( abstractJcrBackedUserRoleDaoMock.getTenantedUserNameUtils() ).thenReturn( resolverMock );

    when( abstractJcrBackedUserRoleDaoMock.isUseJackrabbitUserCache() ).thenReturn( true );

    //Cache mocking
    LRUMap cacheMock = mock( LRUMap.class );
    when( abstractJcrBackedUserRoleDaoMock.getUserCache() ).thenReturn( cacheMock );

    User userMock = mock( User.class );
    abstractJcrBackedUserRoleDaoMock.convertToPentahoUser( userMock );

    verify( cacheMock ).put( anyObject(), anyString() );
  }

  @Test
  public void testConvertToPentahoUserDisableCache() throws RepositoryException {
    AbstractJcrBackedUserRoleDao abstractJcrBackedUserRoleDaoMock = mock( AbstractJcrBackedUserRoleDao.class );
    doCallRealMethod().when( abstractJcrBackedUserRoleDaoMock ).convertToPentahoUser( any( User.class ) );

    ITenantedPrincipleNameResolver resolverMock = mock( ITenantedPrincipleNameResolver.class );
    when( abstractJcrBackedUserRoleDaoMock.getTenantedUserNameUtils() ).thenReturn( resolverMock );

    //Cache mocking
    LRUMap cacheMock = mock( LRUMap.class );
    when( abstractJcrBackedUserRoleDaoMock.getUserCache() ).thenReturn( cacheMock );

    User userMock = mock( User.class );
    abstractJcrBackedUserRoleDaoMock.convertToPentahoUser( userMock );

    verify( cacheMock, never() ).put( anyObject(), anyString() );
  }


  @Test
  public void getSessionImplTest() throws Exception {

    CredentialsStrategySessionFactory factory =
        new CredentialsStrategySessionFactory( mock( Repository.class ), mock( CredentialsStrategy.class ) );
    Session sessionProxy = factory.createSessionProxy( mock( SessionImpl.class ) );
    SessionImpl sessionImpl = AbstractJcrBackedUserRoleDao.getSessionImpl( sessionProxy );
    assertThat( sessionImpl, is( notNullValue() ) );

  }
}
