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


package org.pentaho.platform.api.repository2.unified;

/**
 * Exception thrown from a {@link IBackingRepositoryLifecycleManager} method that fails
 */
public class RepositoryLifecycleManagerException extends RuntimeException {

  private static final long serialVersionUID = -8568973306515692541L;

  public RepositoryLifecycleManagerException() {
    super();
  }

  public RepositoryLifecycleManagerException( final String message ) {
    super( message );
  }

  public RepositoryLifecycleManagerException( final String message, final Throwable cause ) {
    super( message, cause );
  }

  public RepositoryLifecycleManagerException( final Throwable cause ) {
    super( cause );
  }
}
