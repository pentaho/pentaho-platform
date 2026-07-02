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


package org.pentaho.platform.repository2.unified.exception;

public class RepositoryFileDaoException extends RuntimeException {

  private static final long serialVersionUID = 1274516138533769935L;

  public RepositoryFileDaoException() {
    super();
  }

  public RepositoryFileDaoException( final String message, final Throwable cause ) {
    super( message, cause );
  }

  public RepositoryFileDaoException( final String message ) {
    super( message );
  }

  public RepositoryFileDaoException( final Throwable cause ) {
    super( cause );
  }

}
