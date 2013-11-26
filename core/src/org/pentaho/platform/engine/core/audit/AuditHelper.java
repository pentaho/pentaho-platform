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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class AuditHelper {
  private static final Log log = LogFactory.getLog( AuditHelper.class );

  public static void audit( final IRuntimeContext runtimeContext, final IPentahoSession session,
      final String messageType, final String message, final String value, final float duration, final ILogger logger ) {
    String instanceId = ( runtimeContext == null ) ? "" : runtimeContext.getInstanceId(); //$NON-NLS-1$
    String userId = session.getName();
    String actionName = ( runtimeContext == null ) ? null : runtimeContext.getActionName();
    String objectType = ( runtimeContext == null ) ? "" : runtimeContext.getCurrentComponentName(); //$NON-NLS-1$
    String processId = ( runtimeContext == null ) ? null : runtimeContext.getProcessId();

    AuditHelper.audit( instanceId, userId, actionName, objectType, processId, messageType, message, value, duration,
        logger );
  }

  private static void logTheAuditError( final ILogger logger, Exception e ) {
    String msg = null;
    try {
      msg =
          ( ( e.getMessage() != null ) ? e.getMessage() : Messages.getInstance().getErrorString(
              "AUDITHELPER.ERROR_0001_AUDIT_ENTRY_ERROR" ) ); //$NON-NLS-1$
    } catch ( Throwable ignored ) {
      msg = Messages.getInstance().getErrorString( "AUDITHELPER.ERROR_0001_AUDIT_ENTRY_ERROR" ); //$NON-NLS-1$
    }
    if ( ( msg.toLowerCase().indexOf( "not found" ) >= 0 ) ) { //$NON-NLS-1$
      e = null; // Prevent Stack Trace
    }
    if ( logger != null ) {
      if ( e != null ) {
        logger.error( msg, e );
      } else {
        logger.error( msg );
      }
    } else {
      if ( e != null ) {
        Logger.error( AuditHelper.class.getName(), msg, e );
      } else {
        Logger.error( AuditHelper.class.getName(), msg );
      }
    }
  }

  public static void audit( String instanceId, final String userId, String actionName, final String objectType,
      String processId, final String messageType, final String message, final String value, final float duration,
      final ILogger logger ) {
    try {

      if ( ( processId == null ) || ( instanceId == null ) || ( actionName == null ) || actionName.equals( "" ) ) { //$NON-NLS-1$
        if ( processId == null ) {
          processId = ""; //$NON-NLS-1$
          AuditHelper.log.error( Messages.getInstance().getString( "AUDITHELPER.ERROR_0002_PROCESS_ID_IS_NULL" ) ); //$NON-NLS-1$
        }
        if ( instanceId == null ) {
          instanceId = ""; //$NON-NLS-1$
          AuditHelper.log.error( Messages.getInstance().getString( "AUDITHELPER.ERROR_0003_INSTANCE_ID_IS_NULL" ) ); //$NON-NLS-1$
        }
        if ( actionName == null ) {
          actionName = ""; //$NON-NLS-1$
          AuditHelper.log.error( Messages.getInstance().getString( "AUDITHELPER.ERROR_0004_ACTION_NAME_IS_NULL" ) ); //$NON-NLS-1$
        }
      }
      AuditEntry.auditJobDuration( processId, instanceId, actionName, objectType, userId, messageType, message, value,
          duration );
    } catch ( Exception e ) {
      AuditHelper.logTheAuditError( logger, e );
    }
  }

}
