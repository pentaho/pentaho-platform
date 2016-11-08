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

package org.pentaho.platform.engine.core.audit;

import org.pentaho.platform.api.engine.AuditException;
import org.pentaho.platform.api.engine.IAuditEntry;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mbatchel
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 *         Style - Code Templates TODO merge this into AuditHelper
 */
public class AuditEntry {

  private static final Map<String, Long> messageTypeCountMap = new HashMap<String, Long>();
  private static final Date counterResetDateTime = new Date();

  public static void auditJobDuration( final String jobId, final String instId, final String objId,
      final String objType, final String actor, final String messageType, final String messageName,
      final String messageTxtValue, final float duration ) throws AuditException {

    AuditEntry.auditAll( jobId, instId, objId, objType, actor, messageType, messageName, messageTxtValue, null,
        duration );

  }

  public static void auditAll( final String jobId, final String instId, final String objId, final String objType,
      final String actor, final String messageType, final String messageName, final String messageTxtValue,
      final BigDecimal messageNumValue, final float duration ) throws AuditException {
    IAuditEntry auditEntry = null;
    if ( PentahoSystem.getObjectFactory().objectDefined( IAuditEntry.class.getSimpleName() ) ) {
      auditEntry = PentahoSystem.get( IAuditEntry.class, null );
      auditEntry.auditAll( jobId, instId, objId, objType, actor, messageType, messageName, messageTxtValue,
          messageNumValue, duration );
    }
    Long count = messageTypeCountMap.get( messageType );
    if ( count == null ) {
      messageTypeCountMap.put( messageType, new Long( 1 ) );
    } else {
      messageTypeCountMap.put( messageType, new Long( count.longValue() + 1 ) );
    }
  }

  public static void auditJobTxtValue( final String jobId, final String instId, final String objId,
      final String objType, final String actor, final String messageType, final String messageName,
      final String messageTxtValue ) throws AuditException {
    AuditEntry.auditAll( jobId, instId, objId, objType, actor, messageType, messageName, messageTxtValue, null, 0 );
  }

  public static void auditJobNumValue( final String jobId, final String instId, final String objId,
      final String objType, final String actor, final String messageType, final String messageName,
      final BigDecimal messageNumValue ) throws AuditException {
    AuditEntry.auditAll( jobId, instId, objId, objType, actor, messageType, messageName, null, messageNumValue, 0 );

  }

  public static void clearCounts() {
    messageTypeCountMap.clear();
    counterResetDateTime.setTime( ( new Date() ).getTime() );
  }

  public static Map<String, Long> getCounts() {
    return messageTypeCountMap;
  }

  public static Date getCounterResetDateTime() {
    return counterResetDateTime;
  }

}
