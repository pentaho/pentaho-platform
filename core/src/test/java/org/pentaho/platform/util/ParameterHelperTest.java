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
