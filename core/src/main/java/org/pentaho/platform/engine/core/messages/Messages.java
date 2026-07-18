/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.engine.core.messages;

import org.pentaho.platform.util.messages.MessagesBase;

public class Messages extends MessagesBase {
  
  private static Messages instance = new Messages();

  private Messages() {
    super();
  }

  public static Messages getInstance() {
    return instance;
  }

}
