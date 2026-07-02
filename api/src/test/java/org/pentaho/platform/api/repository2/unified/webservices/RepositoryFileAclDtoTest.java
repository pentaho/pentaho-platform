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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by efreitas on 30-07-2018.
 */
public class RepositoryFileAclDtoTest {
  @Test
  public void testDto() {

    RepositoryFileAclDto dto = new RepositoryFileAclDto();
    String idMock = "idMock";
    String ownerMock = "ownerMock";
    String tenantPathMock = "tenantPathMock";
    int ownerTypeMock = 99;
    boolean entriesInheritingMock = true;
    dto.setId( idMock );
    assertEquals( dto.getId(), idMock );
    dto.setOwner( ownerMock );
    assertEquals( dto.getOwner(), ownerMock );
    dto.setTenantPath( tenantPathMock );
    assertEquals( dto.getTenantPath(), tenantPathMock );
    dto.setOwnerType( ownerTypeMock );
    assertEquals( dto.getOwnerType(), ownerTypeMock );
    dto.setEntriesInheriting( entriesInheritingMock );
    assertEquals( dto.isEntriesInheriting(), entriesInheritingMock );
  }
}
