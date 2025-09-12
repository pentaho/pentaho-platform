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
import org.pentaho.commons.util.repository.exception.FilterNotValidException;
import org.pentaho.commons.util.repository.exception.FolderNotValidException;
import org.pentaho.commons.util.repository.exception.InvalidArgumentException;
import org.pentaho.commons.util.repository.exception.ObjectNotFoundException;
import org.pentaho.commons.util.repository.exception.OperationNotSupportedException;
import org.pentaho.commons.util.repository.exception.PermissionDeniedException;
import org.pentaho.commons.util.repository.exception.RuntimeException;
import org.pentaho.commons.util.repository.exception.UpdateConflictException;
import org.pentaho.commons.util.repository.type.CmisObject;
import org.pentaho.commons.util.repository.type.TypesOfFileableObjects;

import java.util.List;

public interface INavigationService {

  public List<CmisObject> getObjectParent( String repositoryId, String objectId, String filter,
      boolean includeAllowableActions, boolean includeRelationships ) throws InvalidArgumentException,
    ConstraintViolationException, FilterNotValidException, RuntimeException, UpdateConflictException,
    ObjectNotFoundException, OperationNotSupportedException, PermissionDeniedException, FolderNotValidException;

  public List<CmisObject> getFolderParent( String repositoryId, String folderId, String filter,
      boolean includeAllowableActions, boolean includeRelationships, boolean returnToRoot )
    throws InvalidArgumentException, ConstraintViolationException, FilterNotValidException, RuntimeException,
    UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, PermissionDeniedException,
    FolderNotValidException;

  public List<CmisObject> getDescendants( String repositoryId, String folderId, TypesOfFileableObjects type, int depth,
      String filter, boolean includeAllowableActions, boolean includeRelationships ) throws InvalidArgumentException,
    ConstraintViolationException, FilterNotValidException, RuntimeException, UpdateConflictException,
    ObjectNotFoundException, OperationNotSupportedException, PermissionDeniedException, FolderNotValidException;

  public List<CmisObject> getChildren( String repositoryId, String folderId, TypesOfFileableObjects type,
      String filter, boolean includeAllowableActions, boolean includeRelationships, int maxItems, int skipCount )
    throws InvalidArgumentException, ConstraintViolationException, FilterNotValidException, RuntimeException,
    UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, PermissionDeniedException,
    FolderNotValidException;

  public GetCheckedoutDocsResponse getCheckedoutDocs( String repositoryId, String folderId, String filter,
      boolean includeAllowableActions, boolean includeRelationships, int maxItems, int skipCount )
    throws InvalidArgumentException, ConstraintViolationException, FilterNotValidException, RuntimeException,
    UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, PermissionDeniedException,
    FolderNotValidException;

  public String getRepositoryPath( CmisObject object );

  public String getRepositoryFilename( CmisObject object );
}
