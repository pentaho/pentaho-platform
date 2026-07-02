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


package org.pentaho.platform.web.http;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;

import java.util.List;

/**
 * A helper class for the web module
 * encapsulating web-related checks as to pass it then to the core {@link IMessageFormatter}.
 */
public class MessageFormatUtils {

  public static void formatFailureMessage( String mimeType, IRuntimeContext context, StringBuffer messageBuffer,
                                           List defaultMessages ) {
    getFormatter().formatFailureMessage( mimeType, context, messageBuffer, defaultMessages, isAdmin() );
  }

  public static void formatSuccessMessage( String mimeType, IRuntimeContext context, StringBuffer messageBuffer,
                                           boolean doMessages, boolean doWrapper ) {
    getFormatter().formatSuccessMessage( mimeType, context, messageBuffer, doMessages, doWrapper );
  }

  public static void formatSuccessMessage( String mimeType, IRuntimeContext context,
                                    StringBuffer messageBuffer, boolean doMessages ) {
    getFormatter().formatSuccessMessage( mimeType, context, messageBuffer, doMessages );
  }

  private static boolean isAdmin() {
    return PentahoSystem.get( IAuthorizationPolicy.class ).isAllowed( AdministerSecurityAction.NAME );
  }

  private static IMessageFormatter getFormatter() {
    return PentahoSystem.get( IMessageFormatter.class, PentahoSessionHolder.getSession() );
  }
}
