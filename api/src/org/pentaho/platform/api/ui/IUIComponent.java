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

package org.pentaho.platform.api.ui;

import org.pentaho.platform.api.engine.IActionRequestHandler;
import org.pentaho.platform.api.engine.IPentahoSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public interface IUIComponent {

  @SuppressWarnings( "rawtypes" )
  public void handleRequest( OutputStream outputStream, IActionRequestHandler actionRequestHandler, String contentType,
      HashMap requestParameterProviders ) throws IOException;

  public boolean validate();

  /**
   * Set the userSession member, generate a Log Id, set the requestHandler, and validate the component's
   * configuration. NOTE: this method has several side effects not related to validation. could probably use some
   * refactoring
   * 
   * @param session
   * @param actionRequestHandler
   * @return boolean true if component configuration is valid, else false
   */
  public boolean validate( IPentahoSession session, IActionRequestHandler actionRequestHandler );

}
