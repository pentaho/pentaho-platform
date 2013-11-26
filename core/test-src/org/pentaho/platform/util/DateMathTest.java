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
