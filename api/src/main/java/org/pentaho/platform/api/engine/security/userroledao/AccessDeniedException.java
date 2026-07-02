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
 * 
 * 
 * @author rmansoor
 */
public class AccessDeniedException extends UserRoleDaoException {

  private static final long serialVersionUID = -2371295088329118369L;

  public AccessDeniedException( final String msg ) {
    super( msg );
  }

  public AccessDeniedException( final String msg, final Throwable t ) {
    super( msg, t );
  }

}
