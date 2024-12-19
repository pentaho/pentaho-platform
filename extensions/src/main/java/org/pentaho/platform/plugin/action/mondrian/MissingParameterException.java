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

public class MissingParameterException extends PentahoCheckedChainedException {

  private static final long serialVersionUID = -9080786045214145674L;

  public MissingParameterException( final String message ) {
    super( message );
  }

  public MissingParameterException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  public MissingParameterException( final Throwable reas ) {
    super( reas );
  }

}
