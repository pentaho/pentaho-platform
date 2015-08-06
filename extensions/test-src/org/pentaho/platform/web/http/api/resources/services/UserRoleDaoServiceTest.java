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

package org.pentaho.platform.web.http.api.resources.services;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.UserListWrapper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserRoleDaoServiceTest {
    private UserRoleDaoService userRoleService;
    @Before
    public void setUp() throws Exception {
        PentahoSystem.init();
        userRoleService = new UserRoleDaoService();
    }

    @After
    public void tearDown() throws Exception {
        PentahoSystem.clearObjectFactory();
        PentahoSystem.shutdown();
    }


    @Test
    public void testGetUsers() throws Exception {
        List<IPentahoUser> userList = new ArrayList<IPentahoUser>();
        IUserRoleDao roleDao = mock( IUserRoleDao.class );
        when( roleDao.getUsers() ).thenReturn( userList );
        PentahoSystem.registerObject(roleDao);

        UserListWrapper wrapUserList = new UserListWrapper( userList );

        assertEquals( wrapUserList.getUsers(), userRoleService.getUsers().getUsers() );
    }
}