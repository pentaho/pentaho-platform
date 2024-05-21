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
