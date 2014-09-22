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

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.chart.model.ChartDataDefinition;

public class ChartDefinitionTest {

  @Test
  public void testEquality() {
    // Test base object
    ChartDataDefinition cdd1 = new ChartDataDefinition();
    ChartDataDefinition cdd2 = new ChartDataDefinition();

    Assert.assertTrue( cdd1.equals( cdd2 ) );
    Assert.assertTrue( cdd2.equals( cdd1 ) );

    // Test partial configuration
    cdd1.setCategoryColumn( "category" ); //$NON-NLS-1$
    cdd2.setCategoryColumn( "category" ); //$NON-NLS-1$
    Assert.assertTrue( cdd1.equals( cdd2 ) );
    Assert.assertTrue( cdd2.equals( cdd1 ) );

    // Test whole configuration
    cdd1.setDomainColumn( "domain" ); //$NON-NLS-1$
    cdd1.setQuery( "a query goes here" ); //$NON-NLS-1$
    cdd1.setRangeColumn( "range" ); //$NON-NLS-1$

    cdd2.setDomainColumn( "domain" ); //$NON-NLS-1$
    cdd2.setQuery( "a query goes here" ); //$NON-NLS-1$
    cdd2.setRangeColumn( "range" ); //$NON-NLS-1$

    Assert.assertTrue( cdd1.equals( cdd2 ) );
    Assert.assertTrue( cdd2.equals( cdd1 ) );

    // Test case mismatch
    cdd1.setDomainColumn( "Domain" ); //$NON-NLS-1$

    cdd2.setDomainColumn( "domain" ); //$NON-NLS-1$

    Assert.assertFalse( cdd1.equals( cdd2 ) );
    Assert.assertFalse( cdd2.equals( cdd1 ) );

    // Test partial mismatch
    cdd1.setDomainColumn( "not a domain" ); //$NON-NLS-1$

    Assert.assertFalse( cdd1.equals( cdd2 ) );
    Assert.assertFalse( cdd2.equals( cdd1 ) );

    // Test null mismatch
    cdd1.setDomainColumn( null );

    Assert.assertFalse( cdd1.equals( cdd2 ) );
    Assert.assertFalse( cdd2.equals( cdd1 ) );

    // Test full null mismatch
    cdd1.setCategoryColumn( null );
    cdd1.setQuery( null );
    cdd1.setRangeColumn( null );

    Assert.assertFalse( cdd1.equals( cdd2 ) );
    Assert.assertFalse( cdd2.equals( cdd1 ) );

    cdd2.setScalingFactor( 10 );

    Assert.assertFalse( cdd1.equals( cdd2 ) );
    Assert.assertFalse( cdd2.equals( cdd1 ) );

  }
}
