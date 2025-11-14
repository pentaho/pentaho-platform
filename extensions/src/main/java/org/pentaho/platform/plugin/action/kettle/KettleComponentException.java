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


package org.pentaho.platform.plugin.action.kettle;

public class KettleComponentException extends Exception {

  private static final long serialVersionUID = -6175673842464085091L;

  public KettleComponentException() {
    super();
  }

  public KettleComponentException( Throwable t ) {
    super( t );
  }

  public KettleComponentException( String msg ) {
    super( msg );
  }

  public KettleComponentException( String msg, Throwable t ) {
    super( msg, t );
  }

}
