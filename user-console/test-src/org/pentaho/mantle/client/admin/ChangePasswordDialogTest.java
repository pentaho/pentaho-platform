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

@RunWith( GwtMockitoTestRunner.class ) public class ChangePasswordDialogTest {

  ChangePasswordDialog changePasswordDialog;

  @Before public void setUp() {
    UpdatePasswordController mockUpdatePasswordController = mock( UpdatePasswordController.class );

    changePasswordDialog = spy( new ChangePasswordDialog( mockUpdatePasswordController ) );
  }

  @Test public void testGetButtonPanel() {
    HorizontalPanel mockHorizontalPanel = mock( HorizontalPanel.class );
    doReturn( mockHorizontalPanel ).when( changePasswordDialog ).getHorizontalPanel();

    // TEST1
    Panel val = changePasswordDialog.getButtonPanel();

    verify( mockHorizontalPanel ).setCellWidth( any( Button.class ), eq( "100%" ) );
    verify( mockHorizontalPanel ).setCellHorizontalAlignment( any( Button.class ), eq( HorizontalPanel.ALIGN_RIGHT ) );
    verify( mockHorizontalPanel, times( 2 ) ).add( any( Button.class ) );
    assertEquals( val, mockHorizontalPanel );
  }

  @Test public void testGetDialogContents() {
    HorizontalPanel mockHorizontalPanel = mock( HorizontalPanel.class );
    doReturn( mockHorizontalPanel ).when( changePasswordDialog ).getHorizontalPanel();

    VerticalPanel mockVerticalPanel = mock( VerticalPanel.class );
    doReturn( mockVerticalPanel ).when( changePasswordDialog ).getVerticalPanel();

    // TEST1
    Panel val = changePasswordDialog.getDialogContents();

    verify( mockHorizontalPanel, times( 2 ) ).add( any( SimplePanel.class ) );
    verify( mockHorizontalPanel, times( 2 ) ).add( any( VerticalPanel.class ) );
    verify( mockVerticalPanel, times( 5 ) ).add( any( SimplePanel.class ) );
    verify( mockVerticalPanel, times( 5 ) ).add( any( Label.class ) );
    verify( mockVerticalPanel, times( 5 ) ).add( any( PasswordTextBox.class ) );
    assertEquals( val, mockHorizontalPanel );
  }
}
