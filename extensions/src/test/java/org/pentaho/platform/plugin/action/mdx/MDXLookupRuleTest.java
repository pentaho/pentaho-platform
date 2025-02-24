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


package org.pentaho.platform.plugin.action.mdx;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 11/5/15.
 */
public class MDXLookupRuleTest {

  MDXLookupRule mdxLookupRule;

  @Before
  public void setUp() throws Exception {
    mdxLookupRule = new MDXLookupRule();
  }

  @Test
  public void testValidateSystemSettings() throws Exception {
    assertTrue( mdxLookupRule.validateSystemSettings() );
  }

  @Test
  public void testGetLogger() throws Exception {
    assertNotNull( mdxLookupRule.getLogger() );
  }

  @Test
  public void testInit() throws Exception {
    assertTrue( mdxLookupRule.init() );
  }

}
