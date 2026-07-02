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


package org.pentaho.platform.engine.core;

import org.pentaho.platform.api.engine.IContentListener;

public class TestContentListener implements IContentListener {

  public String mimeType = null;

  public void close() {
  }

  public void setMimeType( String mimeType ) {
    this.mimeType = mimeType;
  }

  public void setName( String name ) {
  }

}
