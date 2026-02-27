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


package org.pentaho.platform.engine.services;

import java.math.BigDecimal;

import org.pentaho.platform.engine.services.audit.AuditFileEntry;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings( "nls" )
public class AuditFileEntryTest extends BaseTest {
  private static final String SOLUTION_PATH = "src/test/resources/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testAuditFileEntry() {
    startTest();
    AuditFileEntry auditFileEntry = new AuditFileEntry();
    auditFileEntry.auditAll( "234234", "2342342342", "234234234", "String", "actor", "messageType", "messageName", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        "messageTxtValue", new BigDecimal( 2324323.23 ), 23 ); //$NON-NLS-1$

    assertTrue( true );
    finishTest();
  }

}
