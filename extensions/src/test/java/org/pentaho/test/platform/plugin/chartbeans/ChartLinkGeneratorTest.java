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


package org.pentaho.test.platform.plugin.chartbeans;

import org.junit.Test;
import org.pentaho.platform.plugin.action.chartbeans.ChartLinkGenerator;

import static junit.framework.Assert.assertEquals;

/**
 * Created: 3/10/11
 * 
 * @author rfellows
 */
public class ChartLinkGeneratorTest {
  @Test
  public void testGenerateLinkEscapingSingleQuotes() {
    ChartLinkGenerator g = new ChartLinkGenerator( "javascript:test('{series}', '{domain}')" );
    String generated =
        g.generateLink( "1950's Chicago Surface Lines Streetcar", "1950's Chicago Surface Lines Streetcar", 8601 );
    assertEquals(
        "javascript:test('1950\\'s Chicago Surface Lines Streetcar', '1950\\'s Chicago Surface Lines Streetcar')",
        generated );

    generated = g.generateLink( "1950's Chicago Surface Lines Streetcar", 3, 8601 );
    assertEquals( "javascript:test('1950\\'s Chicago Surface Lines Streetcar', '3')", generated );

  }

  @Test
  public void testGenerateLinkNotEscapingSingleQuotes() {
    // single quotes should only be escaped for javascript links
    ChartLinkGenerator g = new ChartLinkGenerator( "http://www.google.com/#hl=en&sugexp=crnk_lssbd&xhr=t&q={series}" );
    String generated =
        g.generateLink( "1950's Chicago Surface Lines Streetcar", "1950's Chicago Surface Lines Streetcar", 8601 );
    assertEquals( "http://www.google.com/#hl=en&sugexp=crnk_lssbd&xhr=t&q=1950's Chicago Surface Lines Streetcar",
        generated );
  }

}
