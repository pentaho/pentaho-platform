/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.util;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.pentaho.platform.api.engine.PentahoSystemException;

public class PentahoSystemExceptionTest extends TestCase {

  public void testPentahoSystemException1() {
    //    info("Expected: Exception will be caught and thrown as a Pentaho System Exception"); //$NON-NLS-1$
    PentahoSystemException pse = new PentahoSystemException();
    System.out.println( "PentahoSystemException :" + pse ); //$NON-NLS-1$
    Assert.assertTrue( true );

  }

  public void testPentahoSystemException2() {
    //    info("Expected: Exception will be caught and thrown as a Pentaho System Exception"); //$NON-NLS-1$
    PentahoSystemException pse1 = new PentahoSystemException( "A test Pentaho System Exception has been thrown" ); //$NON-NLS-1$
    System.out.println( "PentahoSystemException :" + pse1 ); //$NON-NLS-1$    
    Assert.assertTrue( true );
  }

  public void testPentahoSystemException3() {
    //    info("Expected: A Pentaho System Exception will be created with Throwable as a parameter"); //$NON-NLS-1$
    PentahoSystemException pse2 = new PentahoSystemException( new Throwable( "This is a throwable exception" ) ); //$NON-NLS-1$
    System.out.println( "PentahoSystemException" + pse2 ); //$NON-NLS-1$    
    Assert.assertTrue( true );

  }

  public void testPentahoSystemException4() {
    //    info("Expected: Exception will be caught and thrown as a Pentaho System Exception"); //$NON-NLS-1$
    PentahoSystemException pse3 =
        new PentahoSystemException( "A test Pentaho System Exception has been thrown", new Throwable() ); //$NON-NLS-1$
    System.out.println( "PentahoSystemException :" + pse3 ); //$NON-NLS-1$    
    Assert.assertTrue( true );

  }

}
