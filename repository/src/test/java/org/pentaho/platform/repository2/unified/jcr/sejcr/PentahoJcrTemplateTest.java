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
