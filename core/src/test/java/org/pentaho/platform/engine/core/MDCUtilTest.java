/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

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
