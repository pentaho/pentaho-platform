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
