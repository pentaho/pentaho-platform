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

package org.pentaho.mantle.login.client.messages;

import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;

public class Messages {

  private static ResourceBundle messageBundle;

  public static String getString( String key ) {
    if ( messageBundle == null ) {
      return key;
    }
    return messageBundle.getString( key );
  }

  public static String getString( String key, String... parameters ) {
    if ( messageBundle == null ) {
      return key;
    }
    return messageBundle.getString( key, null, parameters );
  }

  public static ResourceBundle getResourceBundle() {
    return messageBundle;
  }

  public static void setResourceBundle( ResourceBundle messageBundle ) {
    Messages.messageBundle = messageBundle;
  }

}
