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
 * Represents some other, usually fatal, exception.
 * 
 * @author mlowery
 */
public class UncategorizedUserRoleDaoException extends UserRoleDaoException {

  private static final long serialVersionUID = 5992292759147780152L;

  public UncategorizedUserRoleDaoException( final String msg ) {
    super( msg );
  }

  public UncategorizedUserRoleDaoException( final String msg, final Throwable t ) {
    super( msg, t );
  }

}
