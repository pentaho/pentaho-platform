/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.repository2.unified.jcr.sejcr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionHolder;
import org.springframework.extensions.jcr.jackrabbit.LocalTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.jcr.Session;

/**
 * A customized version of the SE-JCR LocalTransactionManager which captures transaction information to inform
 * session creation and pooling.
 * <p>
 * Created by nbaker on 10/17/16.
 */
public class PentahoTransactionManager extends LocalTransactionManager {
  private static final Logger LOG = LoggerFactory.getLogger( PentahoTransactionManager.class );
  private ThreadLocal<TransactionDefinition> activeTransactionDefinition = new ThreadLocal<>();

  /**
   * Sets the session factory transactions are opened on and, when it is a {@link CredentialsStrategySessionFactory},
   * registers this manager with it. The factory's cache-backed delegate can then tell that a {@code REQUIRES_NEW}
   * transaction is being created and log in directly instead of sharing the thread's cached session.
   *
   * @param sessionFactory the session factory used to begin transactions
   */
  @Override
  public void setSessionFactory( SessionFactory sessionFactory ) {
    super.setSessionFactory( sessionFactory );
    if ( sessionFactory instanceof CredentialsStrategySessionFactory ) {
      ( (CredentialsStrategySessionFactory) sessionFactory ).setTransactionManager( this );
    }
  }

  @Override protected void doBegin( Object transaction, TransactionDefinition transactionDefinition )
    throws TransactionException {
    // We capture the transaction details for SessionFactories to leverage later on.
    activeTransactionDefinition.set( transactionDefinition );
    try {
      // Super ends up calling the sessionFactory to create a session.
      super.doBegin( transaction, transactionDefinition );
    } finally {
      // Likely not needed, but clean up just in-case.
      activeTransactionDefinition.remove();
    }
  }

  /**
   * Hands back the factory lease taken when this transaction began if no template callback ever consumed it, so a
   * transaction that completes without touching JCR cannot keep its cached session from being closed after eviction.
   * Runs before the bound session is released because Spring completes synchronizations first.
   */
  @Override
  protected void doCleanupAfterCompletion( Object transaction ) {
    try {
      releaseUnconsumedFactoryLease();
    } catch ( RuntimeException e ) {
      LOG.warn( "Could not release the unconsumed factory lease of a completed transaction", e );
    } finally {
      super.doCleanupAfterCompletion( transaction );
    }
  }

  private void releaseUnconsumedFactoryLease() {
    Object resource = TransactionSynchronizationManager.getResource( getSessionFactory() );
    if ( !( resource instanceof SessionHolder ) ) {
      return;
    }
    Session session = CredentialsStrategySessionFactory.unwrapSessionProxy( ( (SessionHolder) resource ).getSession() );
    Object attribute = session == null ? null : session.getAttribute( CachedJcrSession.SESSION_ATTRIBUTE );
    if ( attribute instanceof CachedJcrSession ) {
      ( (CachedJcrSession) attribute ).releaseUnconsumedFactoryLease();
    }
  }

  /**
   * Get the thread-bound TransactionDefinition.
   *
   * @return active definition
   */
  public TransactionDefinition getTransactionDefinition() {
    return activeTransactionDefinition.get();
  }

  /**
   * Returns whether or not a real transaction is in the process of being created.
   *
   * @return
   */
  public boolean isCreatingTransaction() {
    if ( activeTransactionDefinition.get() == null ) {
      return false;
    }
    switch ( activeTransactionDefinition.get().getPropagationBehavior() ) {
      case TransactionDefinition.PROPAGATION_NESTED:
      case TransactionDefinition.PROPAGATION_REQUIRED:
      case TransactionDefinition.PROPAGATION_REQUIRES_NEW:
        return true;
      default:
        return false;
    }
  }

  /**
   * Returns whether a {@code PROPAGATION_REQUIRES_NEW} transaction is in the process of being created. Such a
   * transaction suspends the one in progress on this thread and must run on a session of its own: handing it the
   * thread's cached session would share that session with the suspended transaction, and the inner commit would save
   * the outer transaction's unsaved changes.
   *
   * @return {@code true} only while a {@code REQUIRES_NEW} transaction is being begun on this thread
   */
  public boolean isCreatingIndependentTransaction() {
    TransactionDefinition definition = activeTransactionDefinition.get();
    return definition != null
      && definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW;
  }
}
