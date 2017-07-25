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
 * Copyright (c) 2017 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.web.http.api.resources;


import org.apache.commons.lang.time.StopWatch;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.util.ActionUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class WorkerNodeActionInvokerAuditor implements IActionInvoker {

  private final IActionInvoker actionInvoker;

  public WorkerNodeActionInvokerAuditor( IActionInvoker actionInvoker ) {
    this.actionInvoker = actionInvoker;
  }

  @Override
  public IActionInvokeStatus invokeAction( IAction action, String user, Map<String, Serializable> params ) throws Exception {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    Map<String, Serializable> auditParams = new HashMap<>( params ); // the prams list key change after invokeAction. Need to preserve.
    makeAuditRecord( 0, MessageTypes.INSTANCE_START, auditParams, action.getClass().getName() );
    try {
      return actionInvoker.invokeAction( action, user, params );
    } finally {
      makeAuditRecord( stopWatch.getTime() / 1000, MessageTypes.INSTANCE_END, auditParams, action.getClass().getName() );
    }
  }


  protected void makeAuditRecord( final float time, final String messageType,
                                 final Map<String, Serializable> actionParams, String className ) {

    AuditHelper.audit( PentahoSessionHolder.getSession() != null ? PentahoSessionHolder.getSession().getId() : "",
            getValue( actionParams, ActionUtil.INVOKER_ACTIONUSER ),
            getValue( actionParams, ActionUtil.INVOKER_STREAMPROVIDER ),
            className,
            getValue( actionParams, ActionUtil.INVOKER_ACTIONID ),
            messageType,
            getValue( actionParams, ActionUtil.QUARTZ_LINEAGE_ID ),
            null,
            time,
            null );
  }

  private String getValue( Map<String, Serializable> actionParams, String key ) {
    return actionParams.get( key ) != null ? actionParams.get( key ).toString() : "";
  }
}
