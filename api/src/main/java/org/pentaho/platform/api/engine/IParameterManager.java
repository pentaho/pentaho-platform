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
