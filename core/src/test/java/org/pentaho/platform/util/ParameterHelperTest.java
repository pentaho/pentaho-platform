/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.util;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class ParameterHelperTest extends TestCase {

  public void testUtil() {
    Date today = new Date();
    DateFormat df = DateFormat.getDateTimeInstance( DateFormat.FULL, DateFormat.FULL, Locale.US );
    String strToday = df.format( today );
    Date date = ParameterHelper.parameterToDate( strToday, today );
    Assert.assertNotNull( date );
    BigDecimal dec = ParameterHelper.parameterToDecimal( "100.43", null ); //$NON-NLS-1$
    Assert.assertNotNull( dec );
    long longVal = ParameterHelper.parameterToLong( "1000000", 0 ); //$NON-NLS-1$
    Assert.assertEquals( longVal, Long.parseLong( "1000000" ) ); //$NON-NLS-1$
    String str = ParameterHelper.parameterToString( "New String", "Default String" ); //$NON-NLS-1$ //$NON-NLS-2$
    Assert.assertEquals( str, "New String" ); //$NON-NLS-1$

  }

}
