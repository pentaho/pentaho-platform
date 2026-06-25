/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.api.engine.security.userroledao;

/**
 * Thrown when an object already exists with the given identifier.
 * 
 * @author mlowery
 */
public class AlreadyExistsException extends UserRoleDaoException {

  private static final long serialVersionUID = -2371295088329118369L;

  public AlreadyExistsException( final String msg ) {
    super( msg );
  }

  public AlreadyExistsException( final String msg, final Throwable t ) {
    super( msg, t );
  }

}
