/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
