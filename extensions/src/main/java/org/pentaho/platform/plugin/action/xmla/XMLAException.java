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


package org.pentaho.platform.plugin.action.xmla;

/**
 * @author William E. Seyler
 */
public class XMLAException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor for XMLAException.
   */
  public XMLAException() {
    super();
  }

  /**
   * Constructor for XMLAException.
   * 
   * @param arg0
   */
  public XMLAException( final String arg0 ) {
    super( arg0 );
  }

  /**
   * Constructor for XMLAException.
   * 
   * @param arg0
   * @param arg1
   */
  public XMLAException( final String arg0, final Throwable arg1 ) {
    super( arg0, arg1 );
  }

  /**
   * Constructor for XMLAException.
   * 
   * @param arg0
   */
  public XMLAException( final Throwable arg0 ) {
    super( arg0 );
  }

}
