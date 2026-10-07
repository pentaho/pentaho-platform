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



package org.pentaho.platform.repository2.unified.jcr.sejcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.jcr.Session;

import com.google.common.cache.Cache;
import org.junit.Test;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.springframework.extensions.jcr.SessionFactory;

/**
 * Verifies that cache eviction during an active template callback defers logout until the callback releases
 * its final cached-session lease.
 */
public class GuavaCachePoolPentahoJcrOrphanedSessionIT extends DefaultUnifiedRepositoryBase {

  @Test
  public void evictedLiveSessionIsClosedAfterTemplateCompletes() throws Exception {
    loginAsSysTenantAdmin();
    AtomicReference<Session> evictedSession = new AtomicReference<>();
    SessionFactory configuredFactory = jcrTemplate.getSessionFactory();
    CredentialsStrategySessionFactory credentialsFactory = (CredentialsStrategySessionFactory) configuredFactory;
    GuavaCachePoolPentahoJcrSessionFactory cacheFactory =
      (GuavaCachePoolPentahoJcrSessionFactory) credentialsFactory.getSessionFactory();
    Cache<?, ?> sessionCache = getSessionCache( cacheFactory );

    try {
      jcrTemplate.execute( session -> {
        evictedSession.set( session );
        Object usageCountAttribute = session.getAttribute( GuavaCachePoolPentahoJcrSessionFactory.USAGE_COUNT );
        assertTrue( "The real cached session should be protected during the callback",
          usageCountAttribute instanceof AtomicInteger && ( (AtomicInteger) usageCountAttribute ).get() > 0 );

        // Exercise Guava's real removal listener while the Spring JCR template is using this session.
        sessionCache.invalidateAll();
        sessionCache.cleanUp();
        assertTrue( "Eviction must defer logout while the callback is active", session.isLive() );
        return null;
      }, true );

      Session session = evictedSession.get();
      assertNotNull( "The callback should have received a repository session", session );
      assertEquals( "The template should release its usage count after the callback", 0,
        ( (AtomicInteger) session.getAttribute( GuavaCachePoolPentahoJcrSessionFactory.USAGE_COUNT ) ).get() );
      assertFalse( "An evicted session must be logged out after its final use", session.isLive() );
    } finally {
      Session session = evictedSession.get();
      if ( session != null && session.isLive() ) {
        // Keep cleanup deterministic if an assertion fails before the deferred logout occurs.
        session.logout();
      }
    }
  }

  @SuppressWarnings( "unchecked" )
  private Cache<?, ?> getSessionCache( GuavaCachePoolPentahoJcrSessionFactory factory ) throws Exception {
    Field cacheField = GuavaCachePoolPentahoJcrSessionFactory.class.getDeclaredField( "sessionCache" );
    cacheField.setAccessible( true );
    return (Cache<?, ?>) cacheField.get( factory );
  }
}