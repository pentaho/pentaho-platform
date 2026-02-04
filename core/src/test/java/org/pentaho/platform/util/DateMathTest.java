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

import java.util.Calendar;
import java.util.Locale;

public class DateMathTest extends TestCase {

  public void testDateMethods() {

    Calendar cal = DateMath.calculateDate( "0:ME -1:DS" ); //$NON-NLS-1$

    System.out.println( "Time is " + cal.getTime() ); //$NON-NLS-1$

    Calendar cal2 = DateMath.calculateDate( cal, "0:MS  0:WE" ); //$NON-NLS-1$
    System.out.println( "Time is milliseconds " + cal2.getTimeInMillis() ); //$NON-NLS-1$

    String cal3 = DateMath.calculateDateString( cal2, "0:MS  0:WE" ); //$NON-NLS-1$
    System.out.println( "Calendar Date with 0:MS  0:WE as format " + cal3 ); //$NON-NLS-1$

    String usDateString = DateMath.calculateDateString( cal2, "0:ME", Locale.US ); //$NON-NLS-1$
    System.out.println( "Calendar Date String with 0:ME as format " + usDateString ); //$NON-NLS-1$        

    String dateString = DateMath.claculateDateString( "0:ME" ); //$NON-NLS-1$
    System.out.println( "Calendar Date with 0:ME as format " + dateString ); //$NON-NLS-1$        

    Assert.assertTrue( true );
  }
}
