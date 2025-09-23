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

package org.pentaho.platform.engine.security.authorization.core;

import org.junit.Test;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class AuthorizationOptionsTest {

  @Test
  public void testDefaultConstructor() {
    var options = new AuthorizationOptions();
    assertEquals( AuthorizationDecisionReportingMode.SETTLED, options.getDecisionReportingMode() );
  }

  @Test
  public void testConstructorWithDecisionReportingMode() {
    var options = new AuthorizationOptions( AuthorizationDecisionReportingMode.FULL );
    assertEquals( AuthorizationDecisionReportingMode.FULL, options.getDecisionReportingMode() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorWithNullDecisionReportingModeThrows() {
    //noinspection DataFlowIssue
    new AuthorizationOptions( null );
  }

  @Test
  public void testEqualsAndHashCode() {
    var options1 = new AuthorizationOptions( AuthorizationDecisionReportingMode.FULL );
    var options2 = new AuthorizationOptions( AuthorizationDecisionReportingMode.FULL );
    var options3 = new AuthorizationOptions( AuthorizationDecisionReportingMode.SETTLED );

    var notOptions = new Object();
    assertNotEquals( options1, notOptions );

    assertEquals( options1, options2 );
    assertNotEquals( options1, options3 );
    assertEquals( options1.hashCode(), options2.hashCode() );
    assertNotEquals( options1.hashCode(), options3.hashCode() );
  }

  @Test
  public void testToStringFormat() {
    var options = new AuthorizationOptions( AuthorizationDecisionReportingMode.FULL );
    assertTrue( options.toString().contains( "FULL" ) );
  }
}
