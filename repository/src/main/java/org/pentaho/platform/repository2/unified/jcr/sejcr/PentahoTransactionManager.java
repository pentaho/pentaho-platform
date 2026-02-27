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

import org.springframework.extensions.jcr.jackrabbit.LocalTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * A customized version of the SE-JCR LocalTransactionManager which captures transaction information to inform
 * session creation and pooling.
 * <p>
 * Created by nbaker on 10/17/16.
 */
public class PentahoTransactionManager extends LocalTransactionManager {
  private ThreadLocal<TransactionDefinition> activeTransactionDefinition = new ThreadLocal<>();

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
}
