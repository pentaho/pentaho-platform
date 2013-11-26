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

import org.pentaho.actionsequence.dom.IActionDefinition;

public class ActionValidationException extends ActionSequenceException {

  /**
   * 
   */
  private static final long serialVersionUID = -6313915362847367483L;

  public ActionValidationException( String msg ) {
    super( msg );
  }

  public ActionValidationException( Throwable cause ) {
    super( cause );
  }

  public ActionValidationException( String msg, Throwable cause ) {
    super( msg, cause );
  }

  public ActionValidationException( String msg, String componentName ) {
    super( msg );
    setActionClass( componentName );
  }

  public ActionValidationException( String msg, Throwable cause, String componentName ) {
    super( msg, cause );
    setActionClass( componentName );
  }

  public ActionValidationException( String msg, Throwable cause, String sessionName, String instanceId,
      String actionSequenceName, String actionDescription, String componentName ) {
    super( msg, cause, sessionName, instanceId, actionSequenceName, actionDescription, componentName );
  }

  public ActionValidationException( String msg, Throwable cause, String sessionName, String instanceId,
      String actionSequenceName, IActionDefinition actionDefinition ) {
    super( msg, cause, sessionName, instanceId, actionSequenceName, actionDefinition );
  }

  public ActionValidationException( String msg, String sessionName, String instanceId, String actionSequenceName,
      IActionDefinition actionDefinition ) {
    super( msg, sessionName, instanceId, actionSequenceName, actionDefinition );
  }
}
