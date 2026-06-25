/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 10/26/15.
 */
public class CronJobTriggerTest {
  @Test
  public void testConstructor() throws Exception {
    CronJobTrigger trigger = new CronJobTrigger();
    assertNotNull( trigger );
  }
}
