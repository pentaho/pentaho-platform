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
