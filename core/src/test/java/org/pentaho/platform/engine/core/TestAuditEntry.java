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

import java.math.BigDecimal;

import org.pentaho.platform.api.engine.AuditException;
import org.pentaho.platform.api.engine.IAuditEntry;

@SuppressWarnings( { "all" } )
public class TestAuditEntry implements IAuditEntry {

  String     jobId;
  String     instId;
  String     objId;
  String     objType;
  String     actor;
  String     messageType;
  String     messageName;
  String     messageTxtValue;
  BigDecimal messageNumValue;
  double     duration;

  public void auditAll( String jobId, String instId, String objId, String objType, String actor, String messageType,
      String messageName, String messageTxtValue, BigDecimal messageNumValue, double duration ) throws AuditException {

    this.jobId = jobId;
    this.instId = instId;
    this.objId = objId;
    this.objType = objType;
    this.actor = actor;
    this.messageType = messageType;
    this.messageName = messageName;
    this.messageTxtValue = messageTxtValue;
    this.messageNumValue = messageNumValue;
    this.duration = duration;

  }

}
