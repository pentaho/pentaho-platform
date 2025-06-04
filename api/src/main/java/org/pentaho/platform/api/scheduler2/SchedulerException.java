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


package org.pentaho.platform.api.scheduler2;

public class SchedulerException extends Exception {

  private static final long serialVersionUID = -2991994661244477148L;

  public SchedulerException( String msg ) {
    super( msg );
  }

  public SchedulerException( Throwable t ) {
    super( t );
  }

  public SchedulerException( String msg, Throwable t ) {
    super( msg, t );
  }

}
