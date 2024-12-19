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


package org.pentaho.platform.plugin.action.mondrian;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class InvalidDocumentException extends PentahoCheckedChainedException {

  private static final long serialVersionUID = -3318198454699925064L;

  public InvalidDocumentException( final String message ) {
    super( message );
  }

  public InvalidDocumentException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  public InvalidDocumentException( final Throwable reas ) {
    super( reas );
  }

}
