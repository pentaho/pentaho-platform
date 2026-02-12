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


package org.pentaho.platform.api.engine;

import org.pentaho.platform.api.util.PentahoChainedException;

/**
 * @author mbatchel
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 *         Style - Code Templates
 */
public class AuditException extends PentahoChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = -1428382933476958337L;

  /**
   * 
   */
  public AuditException() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public AuditException( final String message ) {
    super( message );
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param reas
   */
  public AuditException( final String message, final Throwable reas ) {
    super( message, reas );
    // TODO Auto-generated constructor stub
  }

  /**
   * @param reas
   */
  public AuditException( final Throwable reas ) {
    super( reas );
    // TODO Auto-generated constructor stub
  }

}
