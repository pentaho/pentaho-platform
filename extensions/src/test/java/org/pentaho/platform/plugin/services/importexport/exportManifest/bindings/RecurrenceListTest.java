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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by rfellows on 10/26/15.
 */
public class RecurrenceListTest {

  @Test
  public void testGetValues() throws Exception {
    RecurrenceList recurrenceList = new RecurrenceList();
    assertNotNull( recurrenceList.getValues() );
    assertEquals( 0, recurrenceList.getValues().size() );
  }
}
