/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.jcr.sejcr;

import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.GuavaCachePoolPentahoJcrSessionFactory.USAGE_COUNT;

@RunWith( MockitoJUnitRunner.class )
public class PentahoJcrTemplateTest {

  private PentahoJcrTemplate jcrTemplate = new PentahoJcrTemplate();

  private AtomicInteger usedUsageCount = new AtomicInteger( 0 );
  private AtomicInteger releasedUsageCount = new AtomicInteger( 0 );

  @Mock private SessionFactory sessionFactory;
  @Mock private Session session;
  @Mock private JcrCallback action;
  @Mock private JcrCallback nestedAction;

  @Before public void before() throws RepositoryException {
    when( sessionFactory.getSession() ).thenReturn( session );
    // using two AtomicIntegers to independently check the "use" vs. "release" counts.
    when( session.getAttribute( USAGE_COUNT ) )
      .thenReturn( usedUsageCount, releasedUsageCount );
    jcrTemplate.setSessionFactory( sessionFactory );
    jcrTemplate.setAllowCreate( true );
  }

  @Test
  public void executeUpdatesUsageCount() throws IOException, RepositoryException {
    jcrTemplate.setSessionFactory( sessionFactory );

    jcrTemplate.execute( action, true );

    verify( action ).doInJcr( session );
    verify( session, times( 2 ) ).getAttribute( USAGE_COUNT );

    assertThat( "first usage increments", usedUsageCount.get(), equalTo( 1 ) );
    assertThat( "second usage decrements", releasedUsageCount.get(), equalTo( -1 ) );
  }

  @Test
  public void executeUpdatesUsageCountNestedExec() throws IOException, RepositoryException {
    jcrTemplate.setSessionFactory( sessionFactory );

    // the session will be used 2 times,
    // then released two times.
    when( session.getAttribute( USAGE_COUNT ) )
      .thenReturn( usedUsageCount, usedUsageCount, releasedUsageCount, releasedUsageCount );

    when( action.doInJcr( session ) )
      .thenAnswer( s -> {
        jcrTemplate.execute( nestedAction, true );
        return null;
      } );

    jcrTemplate.execute( action, true );

    verify( action ).doInJcr( session );
    verify( nestedAction ).doInJcr( session );
    verify( session, times( 4 ) ).getAttribute( USAGE_COUNT );
    assertThat( "first usage increments", usedUsageCount.get(), equalTo( 2 ) );
    assertThat( "second usage decrements", releasedUsageCount.get(), equalTo( -2 ) );
  }

}
