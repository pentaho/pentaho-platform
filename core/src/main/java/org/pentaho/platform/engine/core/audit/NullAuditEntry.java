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



package org.pentaho.platform.engine.core.audit;

import org.pentaho.platform.api.engine.AuditException;
import org.pentaho.platform.api.engine.IAuditEntry;

import java.math.BigDecimal;

public class NullAuditEntry implements IAuditEntry {

  public void auditAll( String jobId, String instId, String objId, String objType, String actor, String messageType,
      String messageName, String messageTxtValue, BigDecimal messageNumValue, double duration ) throws AuditException {

    // ignore this, we are the null implementation

  }

}
