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
