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

import org.pentaho.platform.api.engine.IMimeTypeListener;

public class MimeTypeListener implements IMimeTypeListener {

  public String mimeType = null;

  public String name = null;

  public void setMimeType( String mimeType ) {
    this.mimeType = mimeType;
  }

  public void setName( String name ) {
    this.name = name;
  }

}
