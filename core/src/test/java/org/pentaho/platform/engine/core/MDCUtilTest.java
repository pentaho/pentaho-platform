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


package org.pentaho.platform.engine.core;

import org.slf4j.MDC;
import org.pentaho.platform.engine.core.audit.MDCUtil;

import junit.framework.TestCase;

public class MDCUtilTest extends TestCase {

  public void testCopyAndSetMDC() {

    // This is what happens in a parent thread
    MDC.put( MDCUtil.REMOTE_ADDR, "192.168.1.1" );
    MDCUtil mdc = new MDCUtil();
    assertEquals( 1, mdc.getContextMap().size() );
    assertEquals( "192.168.1.1", mdc.getContextMap().get( MDCUtil.REMOTE_ADDR ) );
    
    // This is what happens in a child thread
    MDC.clear();
    mdc.setContextMap();
    assertEquals("192.168.1.1", MDC.get( MDCUtil.REMOTE_ADDR ));
    
  }
}
