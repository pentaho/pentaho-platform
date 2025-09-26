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


package org.pentaho.test.platform.engine.core;

import java.util.Date;

public class EchoServiceBean {

  public String echo( String message ) {
    return new Date().toString() + ":" + message; //$NON-NLS-1$
  }

}
