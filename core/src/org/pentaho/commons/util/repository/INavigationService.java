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
