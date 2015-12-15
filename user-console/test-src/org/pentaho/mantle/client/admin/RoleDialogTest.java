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

package org.pentaho.mantle.client.admin;

import com.google.gwt.user.client.ui.*;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class ) public class RoleDialogTest {

  RoleDialog roleDialog;
  UserRolesAdminPanelController mockUserRolesAdminPanelController;

  @Before public void setUp() {
    mockUserRolesAdminPanelController = mock( UserRolesAdminPanelController.class );
    roleDialog = spy( new RoleDialog( mockUserRolesAdminPanelController ) );
  }

  @Test public void testGetButtonPanel() {
    HorizontalPanel mockHorizontalPanel = mock( HorizontalPanel.class );
    doReturn( mockHorizontalPanel ).when( roleDialog ).getHorizontalPanel();

    Panel val = roleDialog.getButtonPanel();

    assertEquals( mockHorizontalPanel, val );

    verify( mockHorizontalPanel, times( 2 ) ).add( any( Button.class ) );
    verify( mockHorizontalPanel ).setCellWidth( any( Button.class ), eq( "100%" ) );
    verify( mockHorizontalPanel ).setCellHorizontalAlignment( any( Button.class ), eq( HorizontalPanel.ALIGN_RIGHT ) );
  }

  @Test public void testGetDialogContents() {
    HorizontalPanel mockHorizontalPanel = mock( HorizontalPanel.class );
    doReturn( mockHorizontalPanel ).when( roleDialog ).getHorizontalPanel();

    VerticalPanel mockVerticalPanel = mock( VerticalPanel.class );
    doReturn( mockVerticalPanel ).when( roleDialog ).getVerticalPanel();

    roleDialog.getDialogContents();

    verify( mockHorizontalPanel, times( 2 ) ).add( any( SimplePanel.class ) );
    verify( mockVerticalPanel, times( 3 ) ).add( any( SimplePanel.class ) );
  }
}
