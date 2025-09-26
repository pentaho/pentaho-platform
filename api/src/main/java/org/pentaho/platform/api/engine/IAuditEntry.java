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


package org.pentaho.platform.api.engine;

import java.math.BigDecimal;

/**
 * An implementation of the IAuditEntry interface describes the method by which audit messages are recorded and
 * what destination storage they are recorded to.
 */
public interface IAuditEntry {

  /**
   * This method will save the information passed as parameters to the audit storage location. One call to this
   * method represents one entry in the audit system.
   * 
   * @param jobId
   *          the id that ties together audit messages from the same execution
   * @param instId
   *          id to be handed to the runtime repository
   * @param objId
   *          the unique id for this instance of this object
   * @param objType
   *          class name of the component that is being audited
   * @param actor
   *          the username associated with the session this auditEntry is requested in
   * @param messageType
   *          the messageType as defined in the MessageType constants class
   * @param messageName
   * @param messageTxtValue
   * @param messageNumValue
   * @param duration
   * @throws AuditException
   * @see org.pentaho.platform.engine.core.audit.MessageTypes
   */
  public void auditAll( String jobId, String instId, String objId, String objType, String actor, String messageType,
      String messageName, String messageTxtValue, BigDecimal messageNumValue, double duration ) throws AuditException;

}
