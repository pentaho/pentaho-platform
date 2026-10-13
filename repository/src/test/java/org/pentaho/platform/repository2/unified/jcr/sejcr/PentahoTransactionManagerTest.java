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

import org.apache.jackrabbit.api.XASession;
import org.junit.Test;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.jackrabbit.LocalTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import javax.jcr.Repository;
import javax.transaction.xa.XAResource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nbaker on 10/17/16.
 */
public class PentahoTransactionManagerTest {

  @Test
  public void getTransactionDefinition() throws Exception {
    PentahoTransactionManager transactionManager = new PentahoTransactionManager();
    SessionFactory sessionFactory = mock( SessionFactory.class );
    XASession xaSession = mock( XASession.class );
    when( xaSession.getXAResource() ).thenReturn( mock( XAResource.class ) );
    transactionManager.setSessionFactory( sessionFactory );
    TransactionDefinition transactionDefinition = mock( TransactionDefinition.class );
    when( transactionDefinition.getIsolationLevel() ).thenReturn( -1 );

    // Main assertion. While doBegin is being called, the transactionDefinition can be reached.
    AtomicBoolean wasAvailable = new AtomicBoolean( false );
    when( sessionFactory.getSession() ).then( invocation -> {
      assertEquals( transactionDefinition, transactionManager.getTransactionDefinition() );
      wasAvailable.set( true );
      return xaSession;
    } );

    transactionManager.getTransaction( transactionDefinition );

    assertTrue( wasAvailable.get() );
    // after life of doBegin call the definition is not available
    assertNull( transactionManager.getTransactionDefinition() );

  }

  @Test
  public void isCreatingTransaction() throws Exception {
    PentahoTransactionManager transactionManager = new PentahoTransactionManager();
    SessionFactory sessionFactory = mock( SessionFactory.class );
    XASession xaSession = mock( XASession.class );
    when( xaSession.getXAResource() ).thenReturn( mock( XAResource.class ) );
    transactionManager.setSessionFactory( sessionFactory );
    TransactionDefinition transactionDefinition = mock( TransactionDefinition.class );
    when( transactionDefinition.getIsolationLevel() ).thenReturn( -1 );

    // All of our assertions must be done while doBegin is being called
    when( sessionFactory.getSession() ).then( invocation -> {

      List<Integer> transDefs = Arrays
        .asList( TransactionDefinition.PROPAGATION_NESTED, TransactionDefinition.PROPAGATION_REQUIRED,
          TransactionDefinition.PROPAGATION_REQUIRES_NEW );

      for ( Integer transDef : transDefs ) {
        when( transactionDefinition.getPropagationBehavior() ).thenReturn( transDef );
        assertTrue( transactionManager.isCreatingTransaction() );
      }
      return xaSession;
    } );

    // Calls doBegin
    transactionManager.getTransaction( transactionDefinition );


  }

  /**
   * Verifies only a {@code REQUIRES_NEW} transaction is reported as independent, and only while it is being begun.
   * Other propagation behaviors keep using the thread's session, as does code that is not beginning a transaction.
   */
  @Test
  @SuppressWarnings( "java:S1874" ) // The manager's doBegin requires Jackrabbit's (deprecated) XASession.
  public void isCreatingIndependentTransaction() throws Exception {
    PentahoTransactionManager transactionManager = new PentahoTransactionManager();
    SessionFactory sessionFactory = mock( SessionFactory.class );
    XASession xaSession = mock( XASession.class );
    when( xaSession.getXAResource() ).thenReturn( mock( XAResource.class ) );
    transactionManager.setSessionFactory( sessionFactory );
    TransactionDefinition transactionDefinition = mock( TransactionDefinition.class );
    when( transactionDefinition.getIsolationLevel() ).thenReturn( -1 );
    assertFalse( "Nothing is being created outside doBegin", transactionManager.isCreatingIndependentTransaction() );

    List<Boolean> observed = new java.util.ArrayList<>();
    when( sessionFactory.getSession() ).then( invocation -> {
      for ( Integer propagation : Arrays.asList( TransactionDefinition.PROPAGATION_REQUIRED,
        TransactionDefinition.PROPAGATION_NESTED, TransactionDefinition.PROPAGATION_SUPPORTS,
        TransactionDefinition.PROPAGATION_REQUIRES_NEW ) ) {
        when( transactionDefinition.getPropagationBehavior() ).thenReturn( propagation );
        observed.add( transactionManager.isCreatingIndependentTransaction() );
      }
      return xaSession;
    } );

    transactionManager.getTransaction( transactionDefinition );

    assertEquals( Arrays.asList( false, false, false, true ), observed );
    assertFalse( "Nothing is being created after doBegin", transactionManager.isCreatingIndependentTransaction() );
  }

  /**
   * Verifies that binding the manager to a {@link CredentialsStrategySessionFactory} registers it with that factory
   * and its cache-backed delegate, which is what lets session creation tell that a transaction is being created.
   */
  @Test
  public void setSessionFactoryRegistersTheManagerWithACredentialsStrategySessionFactory() {
    CredentialsStrategySessionFactory sessionFactory =
      new CredentialsStrategySessionFactory( mock( Repository.class ), mock( CredentialsStrategy.class ) );
    PentahoTransactionManager transactionManager = new PentahoTransactionManager();

    transactionManager.setSessionFactory( sessionFactory );

    assertSame( sessionFactory, transactionManager.getSessionFactory() );
    assertSame( transactionManager,
      ReflectionTestUtils.getField( sessionFactory.getSessionFactory(), "transactionManager" ) );
  }

}
