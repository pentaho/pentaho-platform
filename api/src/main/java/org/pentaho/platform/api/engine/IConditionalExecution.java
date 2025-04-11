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

import org.apache.commons.logging.Log;

import java.util.Map;

/**
 * This interface supports conditional execution of action sequence 'actions' blocks
 * 
 * @author Marc Batchelor June 2008
 */
public interface IConditionalExecution {

  /**
   * 
   * @return Script that will be executed in shouldExecute to evaluate condition
   */
  public String getScript();

  /**
   * Sets the script that will be executed in shouldExecute to evaluate condition
   * 
   * @param script
   */
  public void setScript( String script );

  /**
   * Uses current inputs, and the set script to "decide" whether to execute the current block of actions.
   * 
   * Condition node example: <actions> <condition><![CDATA[chart_type == 'bar']]></condition> ... action
   * definitions ...
   * 
   * @param currentInputs
   * @param logger
   * @return true if the actions should be executed
   * @throws Exception
   */
  @SuppressWarnings( "rawtypes" )
  public boolean shouldExecute( Map currentInputs, Log logger ) throws Exception;

}
