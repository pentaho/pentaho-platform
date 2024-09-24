/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action.mondrian.catalog;

import mondrian.olap.Util;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * @author Vadim_Polynkov
 */
public class MondrianCatalogTest {

  MondrianCatalog catalog;

  @Test
  public void testGetConnectProperties() {
    final String parameterName = "AdditionalParameter";
    final String parameterValue = "\"TestValue\"";
    final String expectedParameterValue = "TestValue";

    final String dataSourceInfo = parameterName + "=" + parameterValue;
    catalog = new MondrianCatalog( "", dataSourceInfo, "", null );

    Util.PropertyList connectProperties = catalog.getConnectProperties();
    assertNotNull( connectProperties );
    assertNotNull( connectProperties.get( parameterName ) );
    assertEquals( connectProperties.get( parameterName ), expectedParameterValue );
  }

}
