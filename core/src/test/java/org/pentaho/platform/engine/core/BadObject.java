/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.engine.core;

import org.pentaho.platform.api.engine.ComponentException;

public class BadObject {

  public BadObject() throws ComponentException {
    throw new ComponentException( "BadObject constructor" ); //$NON-NLS-1$
  }

}
