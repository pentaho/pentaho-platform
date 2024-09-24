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
public class RepositoryFileDtoTest {

  @Test
  public void testDto() {

    RepositoryFileDto dto = new RepositoryFileDto();
    String nameMock = "name";
    String idMock = "id";
    String createdDateMock = "create";
    String creatorIdMock = "creator";
    String lastModifiedDateMock = "last";
    long fileSizeMock = 999;
    boolean folderMock = true;
    String pathMock = "path";
    boolean hiddenMock = true;
    boolean notSchedulableMock = true;
    boolean aclNodeMock = true;
    boolean versionedMock = true;
    String versionIdMock = "versionId";
    boolean lockedMock = true;
    String lockOwnerMock = "lockOwner";
    String lockMessageMock = "lockMessage";
    String lockDateMock = "lockDate";
    String ownerMock = "owner";
    String ownerTenantPathMock = "ownerTenant";
    Boolean versioningEnabledMock = true;
    Boolean versionCommentEnabledMock = true;
    int ownerTypeMock = 99;
    String titleMock = "title";
    String descriptionMock = "description";
    String localeMock = "locale";
    String originalParentFolderPathMock = "original";
    String deletedDateMock = "delete";
    List<LocaleMapDto> localePropertiesMapEntriesMock = new ArrayList<>();
    RepositoryFileAclDto repositoryFileAclDtoMock = new RepositoryFileAclDto();
    dto.setName( nameMock );
    assertEquals( dto.getName(), nameMock );
    dto.setId( idMock );
    assertEquals( dto.getId(), idMock );
    dto.setCreatedDate( createdDateMock );
    assertEquals( dto.getCreatedDate(), createdDateMock );
    dto.setCreatorId( creatorIdMock );
    assertEquals( dto.getCreatorId(), creatorIdMock );
    dto.setLastModifiedDate( lastModifiedDateMock );
    assertEquals( dto.getLastModifiedDate(), lastModifiedDateMock );
    dto.setFileSize( fileSizeMock );
    assertEquals( dto.getFileSize(), fileSizeMock );
    dto.setFolder( folderMock );
    assertEquals( dto.isFolder(), folderMock );
    dto.setPath( pathMock );
    assertEquals( dto.getPath(), pathMock );
    dto.setHidden( hiddenMock );
    assertEquals( dto.isHidden(), hiddenMock );
    dto.setNotSchedulable( notSchedulableMock );
    assertEquals( dto.isNotSchedulable(), notSchedulableMock );
    dto.setAclNode( aclNodeMock );
    assertEquals( dto.isAclNode(), aclNodeMock );
    dto.setVersioned( versionedMock );
    assertEquals( dto.isVersioned(), versionedMock );
    dto.setVersionId( versionIdMock );
    assertEquals( dto.getVersionId(), versionIdMock );
    dto.setLocked( lockedMock );
    assertEquals( dto.isLocked(), lockedMock );
    dto.setLockOwner( lockOwnerMock );
    assertEquals( dto.getLockOwner(), lockOwnerMock );
    dto.setLockMessage( lockMessageMock );
    assertEquals( dto.getLockMessage(), lockMessageMock );
    dto.setLockDate( lockDateMock );
    assertEquals( dto.getLockDate(), lockDateMock );
    dto.setOwner( ownerMock );
    assertEquals( dto.getOwner(), ownerMock );
    dto.setOwnerTenantPath( ownerTenantPathMock );
    assertEquals( dto.getOwnerTenantPath(), ownerTenantPathMock );
    dto.setVersioningEnabled( versioningEnabledMock );
    assertEquals( dto.getVersioningEnabled(), versionCommentEnabledMock );
    dto.setOwnerType( ownerTypeMock );
    assertEquals( dto.getOwnerType(), ownerTypeMock );
    dto.setTitle( titleMock );
    assertEquals( dto.getTitle(), titleMock );
    dto.setDescription( descriptionMock );
    assertEquals( dto.getDescription(), descriptionMock );
    dto.setLocale( localeMock );
    assertEquals( dto.getLocale(), localeMock );
    dto.setOriginalParentFolderPath( originalParentFolderPathMock );
    assertEquals( dto.getOriginalParentFolderPath(), originalParentFolderPathMock );
    dto.setDeletedDate( deletedDateMock );
    assertEquals( dto.getDeletedDate(), deletedDateMock );
    dto.setLocalePropertiesMapEntries( localePropertiesMapEntriesMock );
    assertEquals( dto.getLocalePropertiesMapEntries(), localePropertiesMapEntriesMock );
    dto.setRepositoryFileAclDto( repositoryFileAclDtoMock );
    assertEquals( dto.getRepositoryFileAclDto(), repositoryFileAclDtoMock );
  }
}
