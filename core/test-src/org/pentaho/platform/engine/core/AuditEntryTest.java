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

package org.pentaho.platform.engine.core;

import junit.framework.TestCase;
import org.pentaho.platform.api.engine.IAuditEntry;
import org.pentaho.platform.engine.core.audit.AuditEntry;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.audit.NullAuditEntry;
import org.pentaho.platform.engine.core.output.SimpleContentItem;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;

import java.math.BigDecimal;

@SuppressWarnings( { "all" } )
public class AuditEntryTest extends TestCase {

  public void testAuditEntry() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    PentahoSystem.registerObjectFactory( factory );

    String jobId = "testjobid";
    String instId = "testinstid";
    String objId = "testobjid";
    String objType = "testobjtype";
    String actor = "testactor";
    String messageType = "testtype";
    String messageName = "testname";
    String messageTxtValue = MessageTypes.INSTANCE_END;
    BigDecimal messageNumValue = new BigDecimal( 99 );
    float duration = (float) 1.23;

    // this should not complain
    AuditEntry.auditJobDuration( jobId, instId, objId, objType, actor, messageType, messageName, messageTxtValue,
        duration );

    TestOutputHandler.contentItem = new SimpleContentItem();
    factory.defineObject( IAuditEntry.class.getSimpleName(), TestAuditEntry.class.getName(),
        StandaloneObjectFactory.Scope.GLOBAL );

    // this should not complain
    AuditEntry.auditJobDuration( jobId, instId, objId, objType, actor, messageType, messageName, messageTxtValue,
        duration );

    TestAuditEntry entry = (TestAuditEntry) factory.get( IAuditEntry.class, null );
    assertEquals( jobId, entry.jobId );
    assertEquals( "1.23", Double.toString( entry.duration ).substring( 0, 4 ) );
    assertEquals( null, entry.messageNumValue );
    assertEquals( messageType, entry.messageType );
    assertEquals( messageName, entry.messageName );
    assertEquals( messageTxtValue, entry.messageTxtValue );

    AuditEntry.auditJobNumValue( jobId, instId, objId, objType, actor, messageType, messageName, messageNumValue );
    assertEquals( "0", Double.toString( entry.duration ).substring( 0, 1 ) );
    assertEquals( messageNumValue, entry.messageNumValue );
    assertEquals( null, entry.messageTxtValue );

    AuditEntry.auditJobTxtValue( jobId, instId, objId, objType, actor, messageType, messageName, messageTxtValue );
    assertEquals( "0", Double.toString( entry.duration ).substring( 0, 1 ) );
    assertEquals( null, entry.messageNumValue );
    assertEquals( messageTxtValue, entry.messageTxtValue );

    new MessageTypes();
    new AuditHelper();
    new AuditEntry();

  }

  //Test does not test functionality on current code base
  /*public void testAuditHelper() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    PentahoSystem.registerObjectFactory( factory );
    factory.defineObject( IAuditEntry.class.getSimpleName(), TestAuditEntry.class.getName(),
        StandaloneObjectFactory.Scope.GLOBAL );

    StandaloneSession session = new StandaloneSession( "testuser" );
    String messageType = "testtype";
    String messageName = "testname";
    String messageTxtValue = "testtext";
    BigDecimal messageNumValue = new BigDecimal( 99 );
    float duration = (float) 1.23;

    AuditHelper.audit( null, session, messageType, messageName, messageTxtValue, duration, null );

    TestAuditEntry entry = (TestAuditEntry) factory.get( IAuditEntry.class, null );

    assertNotNull("AuditEntry should not be null", entry);

    if (entry != null) {
      assertEquals( "", entry.instId );
      assertEquals( "", entry.jobId );
      assertEquals( "testuser", entry.actor );
      assertEquals( messageTxtValue, entry.messageTxtValue );
    }
  }*/

  public void testNullAuditEntry() {
    IAuditEntry auditEntry = new NullAuditEntry();

    // this should not fail, even with all nulls as inputs
    auditEntry.auditAll( null, null, null, null, null, null, null, null, null, 0.0 );
  }

}
