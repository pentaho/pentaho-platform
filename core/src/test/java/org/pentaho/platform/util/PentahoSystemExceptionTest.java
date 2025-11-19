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


package org.pentaho.platform.util;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.pentaho.platform.api.engine.PentahoSystemException;

public class PentahoSystemExceptionTest extends TestCase {

  public void testPentahoSystemException1() {
    //    info("Expected: Exception will be caught and thrown as a Hitachi Vantara System Exception"); //$NON-NLS-1$
    PentahoSystemException pse = new PentahoSystemException();
    System.out.println( "PentahoSystemException :" + pse ); //$NON-NLS-1$
    Assert.assertTrue( true );

  }

  public void testPentahoSystemException2() {
    //    info("Expected: Exception will be caught and thrown as a Hitachi Vantara System Exception"); //$NON-NLS-1$
    PentahoSystemException pse1 = new PentahoSystemException( "A test Pentaho System Exception has been thrown" ); //$NON-NLS-1$
    System.out.println( "PentahoSystemException :" + pse1 ); //$NON-NLS-1$    
    Assert.assertTrue( true );
  }

  public void testPentahoSystemException3() {
    //    info("Expected: A Hitachi Vantara System Exception will be created with Throwable as a parameter"); //$NON-NLS-1$
    PentahoSystemException pse2 = new PentahoSystemException( new Throwable( "This is a throwable exception" ) ); //$NON-NLS-1$
    System.out.println( "PentahoSystemException" + pse2 ); //$NON-NLS-1$    
    Assert.assertTrue( true );

  }

  public void testPentahoSystemException4() {
    //    info("Expected: Exception will be caught and thrown as a Hitachi Vantara System Exception"); //$NON-NLS-1$
    PentahoSystemException pse3 =
        new PentahoSystemException( "A test Pentaho System Exception has been thrown", new Throwable() ); //$NON-NLS-1$
    System.out.println( "PentahoSystemException :" + pse3 ); //$NON-NLS-1$    
    Assert.assertTrue( true );

  }

}
