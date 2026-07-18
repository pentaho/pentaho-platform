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



package org.pentaho.platform.api.engine;

import java.io.IOException;
import java.util.Properties;

/**
 * User: nbaker Date: 4/2/13
 */
public interface IConfiguration {
  String getId();

  Properties getProperties() throws IOException;

  void update( Properties properties ) throws IOException;
}
