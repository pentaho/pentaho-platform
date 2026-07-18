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



package org.pentaho.platform.plugin.action.mdx;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 11/5/15.
 */
public class MDXDataComponentTest {

  MDXDataComponent mdxDataComponent;

  @Before
  public void setUp() throws Exception {
    mdxDataComponent = new MDXDataComponent();
  }

  @Test
  public void testValidateSystemSettings() throws Exception {
    assertTrue( mdxDataComponent.validateSystemSettings() );
  }

  @Test
  public void testGetLogger() throws Exception {
    assertNotNull( mdxDataComponent.getLogger() );
  }

  @Test
  public void testInit() throws Exception {
    assertTrue( mdxDataComponent.init() );
  }
}
