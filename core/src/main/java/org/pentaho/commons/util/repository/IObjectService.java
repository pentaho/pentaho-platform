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


package org.pentaho.commons.util.repository;

import org.pentaho.commons.util.repository.exception.ConstraintViolationException;
import org.pentaho.commons.util.repository.exception.FolderNotValidException;
import org.pentaho.commons.util.repository.exception.InvalidArgumentException;
import org.pentaho.commons.util.repository.exception.OperationNotSupportedException;
import org.pentaho.commons.util.repository.exception.PermissionDeniedException;
import org.pentaho.commons.util.repository.exception.RuntimeException;
import org.pentaho.commons.util.repository.exception.StorageException;
import org.pentaho.commons.util.repository.exception.StreamNotSupportedException;
import org.pentaho.commons.util.repository.exception.TypeNotFoundException;
import org.pentaho.commons.util.repository.exception.UpdateConflictException;
import org.pentaho.commons.util.repository.type.AllowableActions;
import org.pentaho.commons.util.repository.type.CmisObject;
import org.pentaho.commons.util.repository.type.CmisProperties;
import org.pentaho.commons.util.repository.type.ContentStream;
import org.pentaho.commons.util.repository.type.ReturnVersion;
import org.pentaho.commons.util.repository.type.UnfileNonfolderObjects;
import org.pentaho.commons.util.repository.type.VersioningState;

import java.util.List;

public interface IObjectService {

  public String createDocument( String repositoryId, String typeId, CmisProperties properties, String folderId,
      ContentStream contentStream, VersioningState versioningState ) throws StorageException, InvalidArgumentException,
    ConstraintViolationException, RuntimeException, UpdateConflictException, StreamNotSupportedException,
    OperationNotSupportedException, PermissionDeniedException, TypeNotFoundException, FolderNotValidException;

  public String createFolder( String repositoryId, String typeId, CmisProperties properties, String folderId )
    throws StorageException, InvalidArgumentException, ConstraintViolationException, RuntimeException,
    UpdateConflictException, StreamNotSupportedException, OperationNotSupportedException, PermissionDeniedException,
    TypeNotFoundException, FolderNotValidException;

  public String createRelationship( String repositoryId, String typeId, CmisProperties properties,
      String sourceObjectId, String targetObjectId ) throws OperationNotSupportedException;

  public String createPolicy( String repositoryId, String typeId, CmisProperties properties, String folderId )
    throws OperationNotSupportedException;

  public AllowableActions getAllowableActions( String repositoryId, String objectId )
    throws OperationNotSupportedException;

  public CmisObject getProperties( String repositoryId, String objectId, ReturnVersion returnVersion, String filter,
      boolean includeAllowableActions, boolean includeRelationships ) throws OperationNotSupportedException;

  public ContentStream getContentStream( String repositoryId, String documentId ) throws OperationNotSupportedException;

  public void updateProperties( String repositoryId, String objectId, String changeToken, CmisProperties properties )
    throws OperationNotSupportedException;

  public void moveObject( String repositoryId, String objectId, String targetFolderId, String sourceFolderId )
    throws OperationNotSupportedException;

  public void deleteObject( String repositoryId, String objectId ) throws OperationNotSupportedException;

  public List<String> deleteTree( String repositoryId, String folderId, UnfileNonfolderObjects unfileNonfolderObjects,
      boolean continueOnFailure ) throws OperationNotSupportedException;

  public String setContentStream( String repositoryId, String documentId, boolean overwriteFlag,
      ContentStream contentStream ) throws OperationNotSupportedException;

  public void deleteContentStream( String repositoryId, String documentId ) throws OperationNotSupportedException;

}
