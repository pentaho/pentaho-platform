package org.pentaho.test.platform.plugin.services.metadata;

import org.pentaho.platform.engine.core.system.SimpleMapCacheManager;

public class MockDisabledCacheManager extends SimpleMapCacheManager {
  @Override
  public boolean cacheEnabled() {
    return false;
  }

  @Override
  public boolean cacheEnabled(String region) {
    return false;
  }

  @Override
  public boolean addCacheRegion(String region) {
    return false;
  }
}
