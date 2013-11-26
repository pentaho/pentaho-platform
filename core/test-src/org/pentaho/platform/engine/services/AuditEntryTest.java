/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services;

import org.pentaho.platform.engine.core.audit.AuditEntry;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.math.BigDecimal;

@SuppressWarnings( "nls" )
public class AuditEntryTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-res/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testAuditEntry() {
    startTest();
    AuditEntry
        .auditAll(
            "234234", "2342342342", "234234234", "String", "actor", "messageType", "messageName", "messageTxtValue", new BigDecimal( 2324323.23 ), 23 ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
    AuditEntry.auditJobDuration(
        "2342342", "242423", "23423423", "objType", "actor", "messageType", "messageName", "messageTxtValue", 23 ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
    AuditEntry
        .auditJobNumValue(
            "2342342", "242423", "23423423", "objType", "actor", "messageType", "messageName", new BigDecimal( 2324323.23 ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ 
    AuditEntry.auditJobTxtValue(
        "2342342", "242423", "23423423", "objType", "actor", "messageType", "messageName", "messageTxtValue" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$

    assertTrue( true );
    finishTest();
  }

}
