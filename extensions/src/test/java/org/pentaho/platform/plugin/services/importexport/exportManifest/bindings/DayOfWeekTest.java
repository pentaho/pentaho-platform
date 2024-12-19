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


package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 10/26/15.
 */
public class DayOfWeekTest {

  @Test
  public void testValue() throws Exception {
    assertEquals( "FRI", DayOfWeek.FRI.value() );
  }

  @Test
  public void testFromValue() throws Exception {
    assertEquals( DayOfWeek.FRI, DayOfWeek.fromValue( "FRI" ) );
  }
}
