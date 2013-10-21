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

import java.util.List;

/**
 * An ActionSequence is the functional object that wraps a SequenceDefinition for processing by the RuntimeContext.
 * When a request results in a RuntimeContext execution, it is the ActionSequence that the context is operazting
 * on.
 * 
 */
public interface IActionSequence extends ISequenceDefinition {

  /**
   * Returns the list of ActionDefinition objects retrieved from the SequenceDefinition.
   * 
   * @return list of ActionDefinitions
   */
  @SuppressWarnings( "rawtypes" )
  public List getActionDefinitionsAndSequences();

  /**
   * If the ActionSequence contains a loop, returns the parameter that the execution should loop on.
   * 
   * @return the parameter to loop on, if looping is defined, otherwise null
   */
  public String getLoopParameter();

  /**
   * If the ActionSequence contains a loop, returns the parameter that the execution should loop on.
   * 
   * @return the parameter to loop on, if looping is defined, otherwise null
   */
  public boolean getLoopUsingPeek();

  /**
   * Returns whether the ActionSequence has a loop in its definition.
   * 
   * @return true if looping is defined, otherwise false
   */
  public boolean hasLoop();

  /**
   * @return The conditional execution object that determines whether a set of actions will be executed.
   */
  public IConditionalExecution getConditionalExecution();

  /**
   * Sets the ConditionalExecution object that determines whether a set of actions will be executed.
   * 
   * @param value
   *          The ConditionalExecution object
   */
  public void setConditionalExecution( IConditionalExecution value );
}
