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


package org.pentaho.platform.api.engine.security.userroledao;

/**
 * Superclass of all exception types thrown by {@link IUserRoleDao} implementations.
 * 
 * @author mlowery
 */
public abstract class UserRoleDaoException extends RuntimeException {

  private static final long serialVersionUID = -80813880351536263L;

  public UserRoleDaoException( final String msg ) {
    super( msg );
  }

  public UserRoleDaoException( final String msg, final Throwable t ) {
    super( msg, t );
  }

}
