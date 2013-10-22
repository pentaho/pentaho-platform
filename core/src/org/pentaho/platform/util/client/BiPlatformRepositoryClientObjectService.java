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

package org.pentaho.platform.util.client;

import org.dom4j.Document;
import org.pentaho.commons.util.repository.IObjectService;
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

public class BiPlatformRepositoryClientObjectService implements IObjectService {

  private Document doc;

  public String createDocument( String arg0, String arg1, CmisProperties arg2, String arg3, ContentStream arg4,
      VersioningState arg5 ) throws StorageException, InvalidArgumentException, ConstraintViolationException,
    RuntimeException, UpdateConflictException, StreamNotSupportedException, OperationNotSupportedException,
    PermissionDeniedException, TypeNotFoundException, FolderNotValidException {

    // TODO implement this via PublisherUtil
    throw new OperationNotSupportedException();
  }

  public String createFolder( String arg0, String arg1, CmisProperties arg2, String arg3 ) throws StorageException,
    InvalidArgumentException, ConstraintViolationException, RuntimeException, UpdateConflictException,
    StreamNotSupportedException, OperationNotSupportedException, PermissionDeniedException, TypeNotFoundException,
    FolderNotValidException {
    // TODO implement this via PublisherUtil
    throw new OperationNotSupportedException();
  }

  public String createPolicy( String arg0, String arg1, CmisProperties arg2, String arg3 )
    throws OperationNotSupportedException {

    throw new OperationNotSupportedException();
  }

  public String createRelationship( String arg0, String arg1, CmisProperties arg2, String arg3, String arg4 )
    throws OperationNotSupportedException {

    throw new OperationNotSupportedException();
  }

  public void deleteContentStream( String arg0, String arg1 ) throws OperationNotSupportedException {

    throw new OperationNotSupportedException();
  }

  public void deleteObject( String arg0, String arg1 ) throws OperationNotSupportedException {

    throw new OperationNotSupportedException();
  }

  public List<String> deleteTree( String arg0, String arg1, UnfileNonfolderObjects arg2, boolean arg3 )
    throws OperationNotSupportedException {

    throw new OperationNotSupportedException();
  }

  public AllowableActions getAllowableActions( String arg0, String arg1 ) throws OperationNotSupportedException {

    throw new OperationNotSupportedException();
  }

  public ContentStream getContentStream( String arg0, String arg1 ) throws OperationNotSupportedException {

    throw new OperationNotSupportedException();
  }

  public CmisObject
  getProperties( String arg0, String arg1, ReturnVersion arg2, String arg3, boolean arg4, boolean arg5 )
    throws OperationNotSupportedException {
    throw new OperationNotSupportedException();
  }

  public void moveObject( String arg0, String arg1, String arg2, String arg3 ) throws OperationNotSupportedException {

    throw new OperationNotSupportedException();
  }

  public String setContentStream( String arg0, String arg1, boolean arg2, ContentStream arg3 )
    throws OperationNotSupportedException {

    // TODO implement this via PublisherUtil
    throw new OperationNotSupportedException();
  }

  public void updateProperties( String arg0, String arg1, String arg2, CmisProperties arg3 )
    throws OperationNotSupportedException {

    throw new OperationNotSupportedException();
  }

  public Document getDoc() {
    return doc;
  }

  public void setDoc( Document doc ) {
    this.doc = doc;
  }

}
