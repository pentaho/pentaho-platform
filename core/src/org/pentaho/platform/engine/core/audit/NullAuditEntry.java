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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jan 13, 2009 
 * @author James Dixon
 */
package org.pentaho.platform.engine.core.audit;

import java.math.BigDecimal;

import org.pentaho.platform.api.engine.AuditException;
import org.pentaho.platform.api.engine.IAuditEntry;

public class NullAuditEntry implements IAuditEntry {

  public void auditAll(String jobId, String instId, String objId, String objType, String actor, String messageType,
      String messageName, String messageTxtValue, BigDecimal messageNumValue, double duration) throws AuditException {

    // ignore this, we are the null implementation
    
  }

}
