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
 * Copyright (c) 2002-2019 Hitachi Vantara. All rights reserved.
 *
 */

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
