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
 * Thrown when no object exists with the given identifier.
 * 
 * @author mlowery
 */
public class NotFoundException extends UserRoleDaoException {

  private static final long serialVersionUID = -818189401946835492L;

  public NotFoundException( final String msg ) {
    super( msg );
  }

  public NotFoundException( final String msg, final Throwable t ) {
    super( msg, t );
  }

}
