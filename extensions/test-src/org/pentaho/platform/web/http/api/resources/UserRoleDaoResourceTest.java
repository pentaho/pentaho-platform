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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.web.http.api.resources.services.UserRoleDaoService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserRoleDaoResourceTest {
    private UserRoleDaoResource userRoleResource;

    //Mocks
    private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;
    private ITenantManager tenantManager;
    private ArrayList<String> systemRoles;
    private String adminRole;
    private UserRoleDaoService userRoleService;

    @Before
    public void setUp() throws Exception {
        roleBindingDao = mock( IRoleAuthorizationPolicyRoleBindingDao.class );
        tenantManager = mock( ITenantManager.class );
        systemRoles = new ArrayList<String>();
        adminRole = "MockSession";
        userRoleService = mock( UserRoleDaoService.class );
        userRoleResource = new UserRoleDaoResource( roleBindingDao, tenantManager, systemRoles, adminRole, userRoleService );
    }

    @Test
    public void testGetUsers() throws Exception {
        UserListWrapper userListWrapper = new UserListWrapper( new ArrayList<IPentahoUser>() );
        when( userRoleService.getUsers() ).thenReturn( userListWrapper );

        assertEquals( userListWrapper, userRoleResource.getUsers() );
    }

    @Test
    public void testGetUsersError() throws Exception {
        try {
            when( userRoleService.getUsers() ).thenThrow( new Exception() );
        } catch( WebApplicationException e ) {
            assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getResponse().getStatus() );
        }
    }
}