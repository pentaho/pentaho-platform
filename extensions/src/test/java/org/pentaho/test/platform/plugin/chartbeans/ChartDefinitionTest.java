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
