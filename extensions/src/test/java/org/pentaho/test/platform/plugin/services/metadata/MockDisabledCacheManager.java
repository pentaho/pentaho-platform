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


package org.pentaho.test.platform.plugin.services.metadata;

import org.pentaho.platform.engine.core.system.SimpleMapCacheManager;

public class MockDisabledCacheManager extends SimpleMapCacheManager {
  @Override
  public boolean cacheEnabled() {
    return false;
  }

  @Override
  public boolean cacheEnabled( String region ) {
    return false;
  }

  @Override
  public boolean addCacheRegion( String region ) {
    return false;
  }
}
