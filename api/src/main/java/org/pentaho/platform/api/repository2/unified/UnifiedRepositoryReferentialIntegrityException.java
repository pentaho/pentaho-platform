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



package org.pentaho.platform.api.repository2.unified;

/**
 * An exception that can be thrown from {@code IUnifiedRepository} implementations.
 * 
 * @author mlowery
 */
public class UnifiedRepositoryReferentialIntegrityException extends UnifiedRepositoryException {

  private static final long serialVersionUID = -7800484179397724352L;

  public UnifiedRepositoryReferentialIntegrityException() {
    super();
  }

  public UnifiedRepositoryReferentialIntegrityException( final String message, final Throwable cause ) {
    super( message, cause );
  }

  public UnifiedRepositoryReferentialIntegrityException( final String message ) {
    super( message );
  }

  public UnifiedRepositoryReferentialIntegrityException( final Throwable cause ) {
    super( cause );
  }

}
