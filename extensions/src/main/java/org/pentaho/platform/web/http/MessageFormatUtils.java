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
 * Copyright (c) 2002-2018 Hitachi Vantara..  All rights reserved.
 */

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
