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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.api.repository2.unified.webservices;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by efreitas on 30-07-2018.
 */
public class RepositoryFileAclAceDtoTest {
  @Test
  public void testDto() {

    RepositoryFileAclAceDto dto = new RepositoryFileAclAceDto();
    String recipientMock = "recipientMock";
    String tenantPathMock = "tenantPath";
    boolean modifiableMock = true;
    int recipientTypeMock = 99;
    List<Integer> permissionsMock = new ArrayList<Integer>();
    dto.setRecipient( recipientMock );
    assertEquals( dto.getRecipient(), recipientMock );
    dto.setTenantPath( tenantPathMock );
    assertEquals( dto.getTenantPath(), tenantPathMock );
    dto.setModifiable( modifiableMock );
    assertEquals( dto.isModifiable(), modifiableMock );
    dto.setRecipientType( recipientTypeMock );
    assertEquals( dto.getRecipientType(), recipientTypeMock );
    dto.setPermissions( permissionsMock );
    assertEquals( dto.getPermissions(), permissionsMock );

  }
}
