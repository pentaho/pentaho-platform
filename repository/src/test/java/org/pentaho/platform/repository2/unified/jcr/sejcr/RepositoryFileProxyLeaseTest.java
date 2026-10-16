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

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.repository2.unified.jcr.RepositoryFileProxy;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;

/**
 * Unit tests for how {@link RepositoryFileProxy} claims its session for a lazy read. The proxy lives outside this
 * package, so the tests sit here to publish the package-private lifecycle holder on the test session exactly as the
 * cache factory does. Real eviction against Jackrabbit is covered by {@code RepositoryFileProxyEvictionIT}.
 */
public class RepositoryFileProxyLeaseTest {

  private final Node node = mock( Node.class );
  private final Session session = mock( Session.class );
  private final JcrTemplate template = mock( JcrTemplate.class );
  private RepositoryFileProxy proxy;

  @Before
  public void setUp() throws RepositoryException {
    when( node.getSession() ).thenReturn( session );
    when( session.isLive() ).thenReturn( true );
    proxy = new RepositoryFileProxy( node, template, null );
  }

  /** Eviction while a lazy read runs must not close the session under it; the logout follows the read. */
  @Test
  public void lazyReadHoldsALeaseOnItsSessionWhileItRuns() throws RepositoryException {
    CachedJcrSession holder = trackedSession();
    when( node.hasProperty( anyString() ) ).thenAnswer( invocation -> {
      holder.retire();
      verify( session, never() ).logout();
      return false;
    } );

    proxy.getFileSize();

    verify( node ).hasProperty( anyString() );
    verify( session, times( 1 ) ).logout();
  }

  /** A session that is already closing cannot be leased, so the proxy reloads its node instead of reading from it. */
  @Test
  public void lazyReadReloadsThroughTheTemplateWhenItsSessionIsClosing() throws RepositoryException {
    trackedSession().retire();

    proxy.getFileSize();

    verify( template ).execute( any( JcrCallback.class ) );
    verify( node, never() ).hasProperty( anyString() );
  }

  /** Sessions without a lifecycle holder keep the previous behavior: read in place while live. */
  @Test
  public void lazyReadOnAnUntrackedLiveSessionReadsInPlace() throws RepositoryException {
    proxy.getFileSize();

    verify( node ).hasProperty( anyString() );
    verify( template, never() ).execute( any( JcrCallback.class ) );
  }

  /** Sessions without a lifecycle holder keep the previous behavior: reload through the template when closed. */
  @Test
  public void lazyReadOnAClosedUntrackedSessionReloadsThroughTheTemplate() throws RepositoryException {
    when( session.isLive() ).thenReturn( false );

    proxy.getFileSize();

    verify( template ).execute( any( JcrCallback.class ) );
    verify( node, never() ).hasProperty( anyString() );
  }

  /** A failing read must still give the session back, otherwise a retired session would never be closed. */
  @Test
  public void lazyReadReleasesItsLeaseWhenTheReadFails() throws RepositoryException {
    CachedJcrSession holder = trackedSession();
    when( node.hasProperty( anyString() ) ).thenThrow( new IllegalStateException( "read failed" ) );

    assertThrows( IllegalStateException.class, () -> proxy.getFileSize() );
    holder.retire();

    verify( session, times( 1 ) ).logout();
  }

  /** Publishes a lifecycle holder on the proxy's session, as the cache factory does for cached sessions. */
  private CachedJcrSession trackedSession() {
    CachedJcrSession holder = new CachedJcrSession( session );
    when( session.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE ) ).thenReturn( holder );
    return holder;
  }
}
