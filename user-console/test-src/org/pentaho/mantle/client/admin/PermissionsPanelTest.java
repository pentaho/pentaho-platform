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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class ) public class PermissionsPanelTest {

  PermissionsPanel permissionsPanel;

  @Before public void setUp() {
    ListBox mockListBox = mock( ListBox.class );
    permissionsPanel = spy( new PermissionsPanel( mockListBox ) );
  }

  @Test public void testInitializeActionBaseSecurityElements() {
    String roleMappings = "roleMappings";

    JsLogicalRoleMap mockJavaScriptObject = mock( JsLogicalRoleMap.class );
    doReturn( mockJavaScriptObject ).when( permissionsPanel ).parseRoleMappings( anyString() );

    JsArray<JsLocalizedRoleName> mockJsLocalizedRoleNameJsArray = mock( JsArray.class );
    doReturn( mockJsLocalizedRoleNameJsArray ).when( mockJavaScriptObject ).getLogicalRoles();
    doReturn( 0 ).when( mockJsLocalizedRoleNameJsArray ).length();

    JsArray<JsLocalizedRoleName> mockJsLogicalRoleAssignmentJsArray = mock( JsArray.class );
    doReturn( mockJsLogicalRoleAssignmentJsArray ).when( mockJavaScriptObject ).getRoleAssignments();
    doReturn( 1 ).when( mockJsLogicalRoleAssignmentJsArray ).length();

    JsLogicalRoleAssigment mockJsLogicalRoleAssignment = mock( JsLogicalRoleAssigment.class );
    doReturn( mockJsLogicalRoleAssignment ).when( mockJsLogicalRoleAssignmentJsArray ).get( 0 );

    permissionsPanel.initializeActionBaseSecurityElements( roleMappings );

    verify( permissionsPanel ).parseRoleMappings( anyString() );
    verify( mockJavaScriptObject ).getLogicalRoles();
    verify( mockJsLocalizedRoleNameJsArray ).length();
    verify( mockJavaScriptObject, times( 5 ) ).getRoleAssignments();
    verify( mockJsLogicalRoleAssignmentJsArray, times( 2 ) ).length();
    verify( mockJsLogicalRoleAssignmentJsArray, times( 3 ) ).get( 0 );
    verify( mockJsLogicalRoleAssignment ).getRoleName();
  }

  @Test public void testSetSelectedPermissions() {
    Timer mockTimer = mock( Timer.class );
    doReturn( mockTimer ).when( permissionsPanel ).getSelectedPermissionsTimer();

    // TEST1
    permissionsPanel.setSelectedPermissions();

    verify( mockTimer ).scheduleRepeating( 100 );
  }
}
