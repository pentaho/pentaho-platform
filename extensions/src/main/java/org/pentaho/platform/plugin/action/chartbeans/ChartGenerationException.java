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

public class ChartGenerationException extends Exception {

  private static final long serialVersionUID = 4840561957831529L;

  public ChartGenerationException( String s ) {
    super( s );
  }

  public ChartGenerationException( Throwable t ) {
    super( t );
  }
}
