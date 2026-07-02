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


package org.pentaho.platform.engine.core.system;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Yury_Bakhmutski on 12/27/2016.
 */
@RunWith( Parameterized.class )
public class BasePentahoRequestContextTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList( new Object[][]{
        { "//pentaho/Login", "/pentaho/Login/" },
        { "/pentaho/Login", "/pentaho/Login/" },
        { "localhost:8080//pentaho/Login", "localhost:8080/pentaho/Login/" },
        { "localhost:8080///pentaho/Login", "localhost:8080/pentaho/Login/" },
        { "http://localhost:8080///pentaho/Login", "http://localhost:8080/pentaho/Login/" },
        { "https://localhost:8080///pentaho/Login", "https://localhost:8080/pentaho/Login/" }
    } );
  }

  @Parameterized.Parameter( value = 0 )
  public String input;

  @Parameterized.Parameter( value = 1 )
  public String expected;

  @Test
  public void basePentahoRequestContextTest() throws Exception {
    BasePentahoRequestContext basePentahoRequestContext = new BasePentahoRequestContext( input );
    String actual = basePentahoRequestContext.getContextPath();
    Assert.assertEquals( expected, actual );
  }
}
