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


package org.pentaho.platform.plugin.action.chartbeans;

/**
 * This exception wraps the generic exception thrown by the ChartBeans boot process so that developers can properly
 * account for boot errors.
 * 
 * @author cboyden
 * 
 */
public class ChartBootException extends Exception {

  private static final long serialVersionUID = 4840561957831529L;

  public ChartBootException( Throwable t ) {
    super( t );
  }
}
