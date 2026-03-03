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


package org.pentaho.platform.web.http.session;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.messages.Messages;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PentahoHttpSessionListener implements HttpSessionListener {

  private static final boolean debug = PentahoSystem.debug;

  private static final Map<String, String[]> sessionMap = new ConcurrentHashMap<String, String[]>();

  public void sessionCreated( final HttpSessionEvent event ) {
    // We can't find out what the locale of the request is so we go with the
    // default for now...
    // Proper handling is done "ahead" by
    // HttpSessionPentahoSessionIntegrationFilter#localeLeftovers( HttpServletRequest )
    LocaleHelper.setThreadLocaleOverride( Locale.getDefault() );

    String sessionId = event.getSession().getId();
    if ( PentahoHttpSessionListener.debug ) {
      Logger.debug( this,
        Messages.getInstance().getString( "HttpSessionListener.DEBUG_SESSION_CREATED", sessionId ) ); //$NON-NLS-1$
    }

    // AuditHelper.audit( instanceId, String userId, String actionName,
    // String objectType, MessageTypes.PROCESS_ID_SESSION,
    // MessageTypes.SESSION_CREATE, "http session", "", 0, null );
  }

  public void sessionDestroyed( final HttpSessionEvent event ) {
    HttpSession session = event.getSession();
    try {
      if ( session != null ) {
        String sessionId = event.getSession().getId();
        Object obj = session.getAttribute( PentahoSystem.PENTAHO_SESSION_KEY ); //$NON-NLS-1$
        if ( obj != null ) {
          IPentahoSession userSession = (IPentahoSession) obj;

          if ( Logger.getLogLevel() <= ILogger.TRACE ) {
            Logger.trace(
              this,
              String.format(
                "HttpSessionListener.sessionDestroyed for %s %s. Invoke logout listeners",
                sessionId,
                userSession.getName() ) );
          }

          PentahoSystem.invokeLogoutListeners( userSession );
          userSession.destroy();
        } else {
          String[] info = PentahoHttpSessionListener.getSessionInfo( sessionId );
          if ( info != null ) {
            String instanceId = info[ 5 ];
            String userId = info[ 3 ];
            String activityId = info[ 1 ];
            String objectType = info[ 2 ];
            String processId = info[ 0 ];
            String messageType = MessageTypes.SESSION_END;
            String message = "http "; //$NON-NLS-1$
            String value = ""; //$NON-NLS-1$
            long startTime = Long.parseLong( info[ 4 ] );
            long endTime = new Date().getTime();
            if ( !"anonymousUser".equals( userId ) ) {
              AuditHelper.audit( instanceId, userId, activityId, objectType, processId, messageType, message, value,
                ( ( endTime - startTime ) / 1000 ), null );
            }
          }
        }
      }
    } catch ( Throwable e ) {
      Logger.error( this, Messages.getInstance().getErrorString(
        "HttpSessionListener.ERROR_0001_ERROR_DESTROYING_SESSION" ), e ); //$NON-NLS-1$
    }

  }

  public static void registerHttpSession( final String sessionId, final String processId, final String activityId,
                                          final String objectName, final String userName, final String id,
                                          final long start ) {
    PentahoHttpSessionListener.sessionMap.put( id, new String[] { processId, activityId, objectName, userName,
      new Long( start ).toString(), sessionId } );
  }

  public static void deregisterHttpSession( final String id ) {
    PentahoHttpSessionListener.sessionMap.remove( id );
  }

  private static String[] getSessionInfo( final String id ) {
    return (String[]) PentahoHttpSessionListener.sessionMap.get( id );
  }

}
