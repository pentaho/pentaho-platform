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
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by efreitas on 30-07-2018.
 */
public class VersionSummaryDtoTest {
  @Test
  public void testDto() {
    VersionSummaryDto dto = new VersionSummaryDto();
    String messageMock = "message";
    Date dateMock = new Date();
    String authorMock = "author";
    String idMock = "id";
    String versionedFileIdMock = "versionFile";
    boolean aclOnlyChangeMock = true;
    List<String> labels = new ArrayList<String>( 0 );
    dto.setMessage( messageMock );
    assertEquals( dto.getMessage(), messageMock );
    dto.setDate( dateMock );
    assertEquals( dto.getDate(), dateMock );
    dto.setAuthor( authorMock );
    assertEquals( dto.getAuthor(), authorMock );
    dto.setId( idMock );
    assertEquals( dto.getId(), idMock );
    dto.setVersionedFileId( versionedFileIdMock );
    assertEquals( dto.getVersionedFileId(), versionedFileIdMock );
    dto.setAclOnlyChange( aclOnlyChangeMock );
    assertEquals( dto.isAclOnlyChange(), aclOnlyChangeMock );
    dto.setLabels( labels );
    assertEquals( dto.isAclOnlyChange(), aclOnlyChangeMock );
  }
}
