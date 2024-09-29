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

import org.apache.jackrabbit.api.XASession;
import org.junit.Test;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.jackrabbit.LocalTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

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

}