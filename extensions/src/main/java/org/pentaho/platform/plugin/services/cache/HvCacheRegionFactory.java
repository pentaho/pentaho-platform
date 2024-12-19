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

package org.pentaho.platform.plugin.services.cache;

import org.hibernate.cache.ehcache.internal.SingletonEhcacheRegionFactory;
import org.hibernate.cache.spi.TimestampsRegion;
import org.hibernate.engine.spi.SessionFactoryImplementor;

public class HvCacheRegionFactory extends SingletonEhcacheRegionFactory {
  @Override
  public TimestampsRegion buildTimestampsRegion(
    String regionName, SessionFactoryImplementor sessionFactory) {
    verifyStarted();
    return new HvTimestampsRegion(
      regionName, this, createTimestampsRegionStorageAccess( regionName, sessionFactory ) );
  }
}
