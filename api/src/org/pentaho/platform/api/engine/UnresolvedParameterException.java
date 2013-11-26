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

public class UnresolvedParameterException extends ActionSequenceException {
  /**
   * 
   */
  private static final long serialVersionUID = -2676655593926463813L;
  private String parameterName;

  public UnresolvedParameterException() {

  }

  public UnresolvedParameterException( String parameterName ) {
    this.parameterName = parameterName;
  }

  public UnresolvedParameterException( String msg, String parameterName ) {
    super( msg );
    this.parameterName = parameterName;
  }

  public UnresolvedParameterException( String msg, Throwable cause, String sessionName, String instanceId,
      String actionSequenceName, IActionDefinition actionDefinition ) {
    super( msg, cause, sessionName, instanceId, actionSequenceName, actionDefinition );
  }

  public UnresolvedParameterException( String msg, String sessionName, String instanceId, String actionSequenceName,
      IActionDefinition actionDefinition ) {
    super( msg, sessionName, instanceId, actionSequenceName, actionDefinition );
  }

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName( String parameterName ) {
    this.parameterName = parameterName;
  }

}
