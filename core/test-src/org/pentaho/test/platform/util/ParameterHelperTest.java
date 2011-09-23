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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Aug 18, 2005 
 * @author James Dixon
 */

package org.pentaho.test.platform.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.pentaho.platform.util.ParameterHelper;

public class ParameterHelperTest extends TestCase {

  public void testUtil() {
    Date today = new Date();
    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.US);
    String strToday = df.format(today);
    Date date = ParameterHelper.parameterToDate(strToday, today); 
    Assert.assertNotNull(date);
    BigDecimal dec = ParameterHelper.parameterToDecimal("100.43", null); //$NON-NLS-1$
    Assert.assertNotNull(dec);
    long longVal = ParameterHelper.parameterToLong("1000000", 0); //$NON-NLS-1$
    Assert.assertEquals(longVal, Long.parseLong("1000000"));//$NON-NLS-1$
    String str = ParameterHelper.parameterToString("New String", "Default String");//$NON-NLS-1$ //$NON-NLS-2$
    Assert.assertEquals(str, "New String");//$NON-NLS-1$

  }

  public static void main(final String[] args) {
    ParameterHelperTest test = new ParameterHelperTest();
    try {
      test.testUtil();
    } finally {
    }
  }

}
