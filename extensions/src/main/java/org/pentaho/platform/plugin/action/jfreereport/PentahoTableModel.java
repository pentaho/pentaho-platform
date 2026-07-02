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


package org.pentaho.platform.plugin.action.jfreereport;

import org.pentaho.commons.connection.IPentahoResultSet;

/**
 * Creation-Date: 08.07.2006, 13:19:45
 * 
 * @author Thomas Morgner
 * @deprecated This is an empty stub in case we have to maintain backward compatiblity.
 */
@Deprecated
public class PentahoTableModel extends org.pentaho.platform.plugin.action.jfreereport.helper.PentahoTableModel {
  private static final long serialVersionUID = 3946748761053175483L;

  public PentahoTableModel( final IPentahoResultSet rs ) {
    super( rs );
  }
}
