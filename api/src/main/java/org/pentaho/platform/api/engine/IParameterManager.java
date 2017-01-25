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
import java.util.Map;
import java.util.Set;

public interface IParameterManager {

  @SuppressWarnings( "rawtypes" )
  public Map getAllParameters();

  public IActionParameter getCurrentInput( String inputName );

  public IActionParameter getCurrentOutput( String outputName );

  public IActionSequenceResource getCurrentResource( String resource );

  @SuppressWarnings( "rawtypes" )
  public Set getCurrentInputNames();

  public IActionParameter getLoopParameter( String inputName );

  public String getActualRequestParameterName( String fieldName );

  @SuppressWarnings( "rawtypes" )
  public Set getCurrentOutputNames();

  @SuppressWarnings( "rawtypes" )
  public Set getCurrentResourceNames();

  public void dispose();

  @SuppressWarnings( "rawtypes" )
  public void dispose( List exceptParameters );

  public void resetParameters();

  public void setCurrentParameters( ISolutionActionDefinition actionDefinition );

  public void addToAllInputs( String key, IActionParameter param );

  public void addToCurrentInputs( String key, IActionParameter param );

  public boolean addOutputParameters( ISolutionActionDefinition actionDefinition );

  /**
   * Returns a mapping of output parameters and the value and destination.
   * 
   * @param actionSequence
   *          The Action Sequence definition to use
   * 
   * @return a map with the param name as the key and a ReturnParameter containing the data.
   */
  @SuppressWarnings( "rawtypes" )
  public Map getReturnParameters();

  public IActionParameter getInput( String inputName );

}
