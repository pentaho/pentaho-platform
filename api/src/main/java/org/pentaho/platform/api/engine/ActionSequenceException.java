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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.engine;

import org.apache.commons.lang.StringUtils;
import org.pentaho.actionsequence.dom.IActionControlStatement;
import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.actionsequence.dom.IActionIfStatement;
import org.pentaho.actionsequence.dom.IActionLoop;
import org.pentaho.actionsequence.dom.IActionSequenceExecutableStatement;

import java.io.PrintWriter;
import java.util.Date;

public class ActionSequenceException extends Exception {
  /**
   * 
   */
  private static final long serialVersionUID = -2301587142420194146L;

  Date date = new Date();

  String stepDescription;

  String actionSequenceName;

  Integer stepNumber;

  String actionClass;

  String instanceId;

  String sessionId;

  Integer loopIndex;

  IActionDefinition actionDefinition;

  public ActionSequenceException() {
    super();
  }

  public ActionSequenceException( String msg ) {
    super( msg );
  }

  public ActionSequenceException( String msg, Throwable cause ) {
    super( msg, cause );
  }

  public ActionSequenceException( Throwable cause ) {
    super( cause );
  }

  public ActionSequenceException( String msg, Throwable cause, String sessionName, String instanceId,
      String actionSequenceName, String actionDescription, String componentName ) {
    this( msg, cause );
    setActionSequenceName( actionSequenceName );
    setInstanceId( instanceId );
    setSessionId( sessionName );
    setStepDescription( actionDescription );
    setActionClass( componentName );
  }

  /**
   * A convenience method for extracting all knowable information from an {@link IActionDefinition}
   * 
   * @param msg
   * @param cause
   * @param sessionName
   * @param instanceId
   * @param actionSequenceName
   * @param actionDefinition
   */
  public ActionSequenceException( String msg, Throwable cause, String sessionName, String instanceId,
      String actionSequenceName, IActionDefinition actionDefinition ) {
    this( msg, cause );
    setActionSequenceName( actionSequenceName );
    setInstanceId( instanceId );
    setSessionId( sessionName );
    setActionDefinition( actionDefinition );
  }

  /**
   * A convenience method for extracting all knowable information from an {@link IActionDefinition}
   * 
   * @param msg
   * @param sessionName
   * @param instanceId
   * @param actionSequenceName
   * @param actionDefinition
   */
  public ActionSequenceException( String msg, String sessionName, String instanceId, String actionSequenceName,
      IActionDefinition actionDefinition ) {
    this( msg );
    setActionSequenceName( actionSequenceName );
    setInstanceId( instanceId );
    setSessionId( sessionName );
    setActionDefinition( actionDefinition );
  }

  public String getStepDescription() {
    return stepDescription;
  }

  public void setStepDescription( String description ) {
    this.stepDescription = description;
  }

  public String getActionSequenceName() {
    return actionSequenceName;
  }

  public void setActionSequenceName( String actionSequenceName ) {
    this.actionSequenceName = actionSequenceName;
  }

  public Integer getStepNumber() {
    return stepNumber;
  }

  public void setStepNumber( Integer stepNumber ) {
    this.stepNumber = stepNumber;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId( String instanceId ) {
    this.instanceId = instanceId;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId( String sessionId ) {
    this.sessionId = sessionId;
  }

  public String getActionClass() {
    return actionClass;
  }

  public void setActionClass( String actionClass ) {
    this.actionClass = actionClass;
  }

  public Date getDate() {
    return date;
  }

  public IActionDefinition getActionDefinition() {
    return actionDefinition;
  }

  public void setActionDefinition( IActionDefinition actionDefinition ) {
    this.actionDefinition = actionDefinition;
    if ( actionDefinition != null ) {
      setStepDescription( actionDefinition.getDescription() );
      setActionClass( actionDefinition.getComponentName() );
    }
  }

  public Integer getLoopIndex() {
    return loopIndex;
  }

  public void setLoopIndex( Integer loopIndex ) {
    this.loopIndex = loopIndex;
  }

  @SuppressWarnings( "nls" )
  public void printActionExecutionStack( PrintWriter s ) {
    if ( actionDefinition != null ) {
      _printStack( actionDefinition, s, "" );
    }
  }

  /*
   * We are not i18n-ing these stack trace messages. This can be thought of as Throwable.printStackTrace()
   */
  @SuppressWarnings( "nls" )
  protected void _printStack( IActionSequenceExecutableStatement statement, PrintWriter s, String prefix ) {
    if ( statement instanceof IActionIfStatement ) {
      s.println( prefix + "IF STATEMENT: " + ( (IActionIfStatement) statement ).getCondition() );
    } else if ( statement instanceof IActionLoop ) {
      s.println( prefix + "LOOP ON: " + ( (IActionLoop) statement ).getLoopOn() );
    } else if ( statement instanceof IActionDefinition ) {
      String actionDesc = StringUtils.defaultString( ( (IActionDefinition) statement ).getDescription(), "" );
      s.println( prefix + "EXECUTING ACTION: " + actionDesc + " ("
          + ( (IActionDefinition) statement ).getComponentName() + ")" );
    } else if ( statement instanceof IActionControlStatement ) {
      s.println( prefix + "UNKNOWN CONTROL STATEMENT" );
    } else {
      s.println( prefix + "UNKNOWN EXECUTABLE STATEMENT" );
    }

    IActionSequenceExecutableStatement parent = statement.getParent();
    if ( parent != null ) {
      _printStack( statement.getParent(), s, "\tin " );
    }
  }
}
