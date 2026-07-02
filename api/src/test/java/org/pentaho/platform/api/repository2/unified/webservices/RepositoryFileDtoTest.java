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
