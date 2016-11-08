/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
