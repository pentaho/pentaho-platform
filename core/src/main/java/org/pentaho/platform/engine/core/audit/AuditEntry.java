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
