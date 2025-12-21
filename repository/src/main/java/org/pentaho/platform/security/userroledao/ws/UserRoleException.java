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


package org.pentaho.platform.security.userroledao.ws;

import java.io.Serializable;

public class UserRoleException extends Exception implements Serializable {

  private static final long serialVersionUID = 691L;

  public UserRoleException( String msg ) {
    super( msg );
  }

  public UserRoleException( Throwable cause ) {
    super( cause );
  }

  public UserRoleException( String msg, Throwable cause ) {
    super( msg, cause );
  }

  public UserRoleException() {
    super();
  }
}
