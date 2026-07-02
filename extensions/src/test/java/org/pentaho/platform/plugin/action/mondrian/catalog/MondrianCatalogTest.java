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
