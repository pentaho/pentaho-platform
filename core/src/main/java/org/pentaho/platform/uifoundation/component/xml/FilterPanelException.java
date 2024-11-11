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


package org.pentaho.platform.uifoundation.component.xml;

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class FilterPanelException extends PentahoCheckedChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = 666L;

  public FilterPanelException() {
    super();
  }

  public FilterPanelException( final String message ) {
    super( message );
  }

  public FilterPanelException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  public FilterPanelException( final Throwable reas ) {
    super( reas );
  }

}
