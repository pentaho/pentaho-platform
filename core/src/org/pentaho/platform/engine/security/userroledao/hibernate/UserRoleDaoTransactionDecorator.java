/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.engine.security.userroledao.hibernate;

import java.util.List;

import org.pentaho.platform.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Wraps a {@link IUserRoleDao}, beginning, committing, and rolling back transactions before and after each operation.
 * 
 * <p>Why not just do the transactions in the DAO implementation?  Because transactions are a 
 * <a href="http://en.wikipedia.org/wiki/Cross-cutting_concern">cross-cutting concern</a>, an aspect that is often 
 * scattered throughout the code but is best separated from other code.</p>
 * 
 * @author mlowery
 */
public class UserRoleDaoTransactionDecorator implements IUserRoleDao {

  /**
   * Spring's transaction template that begins and commits a transaction, and automatically rolls back on a runtime
   * exception. Recommended configuration for this bean: <code>propagationBehavior</code> set to 
   * <code>TransactionDefinition.PROPAGATION_REQUIRES_NEW)</code>. 
   */
  private TransactionTemplate transactionTemplate;

  /**
   * The wrapped DAO to which to delegate.
   */
  private IUserRoleDao userRoleDao;

  public void createRole(final IPentahoRole roleToCreate) throws AlreadyExistsException,
      UncategorizedUserRoleDaoException {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        userRoleDao.createRole(roleToCreate);
      }
    });
  }

  public void createUser(final IPentahoUser userToCreate) throws AlreadyExistsException,
      UncategorizedUserRoleDaoException {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        userRoleDao.createUser(userToCreate);
      }
    });
  }

  public void deleteRole(final IPentahoRole roleToDelete) throws NotFoundException, UncategorizedUserRoleDaoException {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        userRoleDao.deleteRole(roleToDelete);
      }
    });
  }

  public void deleteUser(final IPentahoUser userToDelete) throws NotFoundException, UncategorizedUserRoleDaoException {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        userRoleDao.deleteUser(userToDelete);
      }
    });
  }

  public IPentahoRole getRole(final String name) throws UncategorizedUserRoleDaoException {
    return (IPentahoRole) transactionTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        return userRoleDao.getRole(name);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public List<IPentahoRole> getRoles() throws UncategorizedUserRoleDaoException {
    return (List<IPentahoRole>) transactionTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        return userRoleDao.getRoles();
      }
    });
  }

  public IPentahoUser getUser(final String username) throws UncategorizedUserRoleDaoException {
    return (IPentahoUser) transactionTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        return userRoleDao.getUser(username);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public List<IPentahoUser> getUsers() throws UncategorizedUserRoleDaoException {
    return (List<IPentahoUser>) transactionTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        return userRoleDao.getUsers();
      }
    });
  }

  public void updateRole(final IPentahoRole roleToUpdate) throws NotFoundException, UncategorizedUserRoleDaoException {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        userRoleDao.updateRole(roleToUpdate);
      }
    });
  }

  public void updateUser(final IPentahoUser userToUpdate) throws NotFoundException, UncategorizedUserRoleDaoException {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        userRoleDao.updateUser(userToUpdate);
      }
    });
  }

  public void setTransactionTemplate(final TransactionTemplate transactionTemplate) {
    this.transactionTemplate = transactionTemplate;
  }

  public void setUserRoleDao(final IUserRoleDao userRoleDao) {
    this.userRoleDao = userRoleDao;
  }

}
