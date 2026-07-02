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
