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
