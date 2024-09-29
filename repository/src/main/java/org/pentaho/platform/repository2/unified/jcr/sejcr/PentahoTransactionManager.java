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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
