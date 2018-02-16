/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

/**
 * 
 */
package org.pentaho.platform.security.policy.rolebased.actions;

import java.util.Locale;
import java.util.ResourceBundle;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;

/**
 * 
 *
 */
public abstract class AbstractAuthorizationAction implements IAuthorizationAction {

  protected ResourceBundle getResourceBundle( String localeString ) {
    final String UNDERSCORE = "_"; //$NON-NLS-1$
    Locale locale;
    if ( localeString == null ) {
      return Messages.getInstance().getBundle();
    } else {
      String[] tokens = localeString.split( UNDERSCORE );
      if ( tokens.length == 3 ) {
        locale = new Locale( tokens[0], tokens[1], tokens[2] );
      } else if ( tokens.length == 2 ) {
        locale = new Locale( tokens[0], tokens[1] );
      } else {
        locale = new Locale( tokens[0] );
      }
      return Messages.getInstance().getBundle( locale );
    }
  }
}
