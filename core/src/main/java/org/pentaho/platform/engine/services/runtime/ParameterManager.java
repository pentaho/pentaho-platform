/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.runtime;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IParameterManager;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.ISolutionActionDefinition;
import org.pentaho.platform.engine.services.actionsequence.ActionParameter;
import org.pentaho.platform.engine.services.actionsequence.ActionParameterSource;
import org.pentaho.platform.engine.services.messages.Messages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParameterManager implements IParameterManager {

  private static final String[] EMPTY_ARRAY = new String[0];

  private ListOrderedMap allParams;

  private ListOrderedMap allResources;

  private List<IActionParameter> waitingToDieParams;

  private ListOrderedMap currentInputs;

  private ListOrderedMap currentOutputs;

  private ListOrderedMap currentResources;

  private String[] sequenceInputNames;

  private String[] sequenceResourceNames;

  private static final Log logger = LogFactory.getLog( ParameterManager.class );

  private Map sequenceOutputDefs;

  ParameterManager() {
    allParams = new ListOrderedMap();
    allResources = new ListOrderedMap();

    waitingToDieParams = new ArrayList<IActionParameter>();

    currentInputs = new ListOrderedMap();
    currentResources = new ListOrderedMap();
    currentOutputs = new ListOrderedMap();

    sequenceOutputDefs = new ListOrderedMap();
    sequenceInputNames = ParameterManager.EMPTY_ARRAY;
  }

  @SuppressWarnings( { "all" } )
  ParameterManager( final IActionSequence actionSequence ) {
    this();
    allParams.putAll( actionSequence.getInputDefinitions() );
    sequenceInputNames =
        (String[]) actionSequence.getInputDefinitions().keySet().toArray( ParameterManager.EMPTY_ARRAY );

    allResources.putAll( actionSequence.getResourceDefinitions() );
    sequenceResourceNames =
        (String[]) actionSequence.getResourceDefinitions().keySet().toArray( ParameterManager.EMPTY_ARRAY );

    sequenceOutputDefs.putAll( actionSequence.getOutputDefinitions() );
  }

  public Map getAllParameters() {
    return ( allParams );
  }

  public IActionParameter getCurrentInput( final String inputName ) {
    return ( (IActionParameter) currentInputs.get( inputName ) );
  }

  public IActionParameter getCurrentOutput( final String outputName ) {
    return ( (IActionParameter) currentOutputs.get( outputName ) );
  }

  public IActionSequenceResource getCurrentResource( final String resourceName ) {
    return ( (IActionSequenceResource) currentResources.get( resourceName ) );
  }

  public Set getCurrentInputNames() {
    return currentInputs.keySet();
  }

  public IActionParameter getLoopParameter( final String inputName ) {
    return ( (IActionParameter) allParams.get( inputName ) );
  }

  public Set getCurrentOutputNames() {
    return currentOutputs.keySet();
  }

  public Set getCurrentResourceNames() {
    return ( currentResources.keySet() );
  }

  protected boolean disposeParameter( final ActionParameter param ) {
    try {
      if ( param != null ) {
        param.dispose();
        return true;
      }
    } catch ( Throwable th ) {
      // Do something here
      ParameterManager.logger.error( Messages.getInstance().getErrorString(
          "ParameterManager.ERROR_0001_DISPOSE_ERROR", param.getName() ), th ); //$NON-NLS-1$
    }
    return false;
  }

  public void dispose() {
    dispose( null );
  }

  public void dispose( final List exceptParameters ) {
    if ( allParams != null ) {
      for ( Iterator it = allParams.values().iterator(); it.hasNext(); ) {
        ActionParameter param = (ActionParameter) it.next();
        if ( ( exceptParameters == null ) || !exceptParameters.contains( param.getValue() ) ) {
          disposeParameter( param );
        }
      }
      for ( Iterator it = waitingToDieParams.iterator(); it.hasNext(); ) {
        ActionParameter param = (ActionParameter) it.next();
        if ( ( exceptParameters == null ) || !exceptParameters.contains( param.getValue() ) ) {
          disposeParameter( param );
        }
      }
    }
  }

  public void resetParameters() {
    dispose();
    currentInputs.clear();
    currentOutputs.clear();
    currentResources.clear();

    allParams = resetMap( sequenceInputNames, allParams );
    allResources = resetMap( sequenceResourceNames, allResources );
  }

  private ListOrderedMap resetMap( final String[] names, final ListOrderedMap oldMap ) {
    ListOrderedMap newMap = new ListOrderedMap();
    for ( String element : names ) {
      newMap.put( element, oldMap.get( element ) );
    }
    return ( newMap );
  }

  public void setCurrentParameters( final ISolutionActionDefinition actionDefinition ) {
    currentInputs.clear();
    currentOutputs.clear();
    currentResources.clear();

    if ( actionDefinition == null ) {
      currentInputs = resetMap( sequenceInputNames, allParams );
      currentResources = resetMap( sequenceResourceNames, allParams );

      for ( Iterator it = sequenceOutputDefs.entrySet().iterator(); it.hasNext(); ) {
        Map.Entry entry = (Map.Entry) it.next();
        String outputName = (String) entry.getKey();
        IActionParameter param = (IActionParameter) allParams.get( outputName );

        if ( ( (IActionParameter) entry.getValue() ).isOutputParameter() ) {
          if ( param == null ) {
            currentOutputs.put( outputName, entry.getValue() );
          } else {
            currentOutputs.put( outputName, param );
          }
        }
      }
      return;
    }

    String key;
    Object value;
    for ( Iterator it = actionDefinition.getActionInputDefinitions().keySet().iterator(); it.hasNext(); ) {
      key = (String) it.next();
      value = allParams.get( actionDefinition.getMappedInputName( key ) );
      if ( value == null ) {
        value = actionDefinition.getActionInputDefinitions().get( key );
        if ( !( (ActionParameter) value ).hasDefaultValue() ) {
          value = null; // Only use if there is a default value;
        }
      }

      if ( value != null ) {
        currentInputs.put( key, value );
      }
    }

    // currentOutputs.putAll(actionDefinition.getActionOutputDefinitions());
    // only put output parameters
    Map outParams = actionDefinition.getActionOutputDefinitions();
    for ( Object outKey : outParams.keySet() ) {
      ActionParameter param = (ActionParameter) outParams.get( outKey );
      if ( param.isOutputParameter() ) {
        currentOutputs.put( outKey, param );
      }
    }

    // This enables the old behavior - It should eventually be removed
    if ( !actionDefinition.hasActionResources() ) {
      currentResources.putAll( allResources );
    } else {
      for ( Iterator it = actionDefinition.getActionResourceDefinitionNames().iterator(); it.hasNext(); ) {
        key = (String) it.next();
        String key2 = actionDefinition.getMappedResourceName( key );
        value = allResources.get( key2 );
        currentResources.put( key, value );
      }
    }
  }

  public void addToAllInputs( final String key, final IActionParameter param ) {
    IActionParameter old = (IActionParameter) allParams.put( key, param );
    if ( ( old != null ) && !allParams.containsValue( old ) ) {
      waitingToDieParams.add( old ); // Just in case a parameter gets over written delete it at the final dispose
    }
  }

  public void addToCurrentInputs( final String key, final IActionParameter param ) {
    if ( currentInputs.containsKey( key ) ) {
      currentInputs.remove( key );
    }
    currentInputs.put( key, param );
  }

  public boolean addOutputParameters( final ISolutionActionDefinition actionDefinition ) {

    String key;
    for ( Iterator it = actionDefinition.getActionOutputDefinitions().keySet().iterator(); it.hasNext(); ) {
      key = (String) it.next();
      IActionParameter outputParam = (IActionParameter) currentOutputs.get( key );
      key = actionDefinition.getMappedOutputName( key );

      // If we already have a parameter with this name, set the value and reuse the definition.
      IActionParameter param = (IActionParameter) allParams.get( key );
      if ( param != null ) {
        if ( param != outputParam ) { // This is a trap for catching temp params that didn't get deleted at the end
                                      // of
                                      // the last loop
          param.dispose();
          param.setValue( outputParam.getValue() );
        }
      } else {
        addToAllInputs( key, outputParam );
      }
    }

    return ( true );
  }

  /**
   * Returns a mapping of output parameters and the value and destination.
   * 
   * @param actionSequence
   *          The Action Sequence definition to use
   * 
   * @return a map with the param name as the key and a ReturnParameter containing the data.
   */
  public Map getReturnParameters() {
    ListOrderedMap returnMap = new ListOrderedMap();

    // Iterate for each output defined
    for ( Iterator it = sequenceOutputDefs.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry entry = (Map.Entry) it.next();
      String outputName = (String) entry.getKey();
      IActionParameter outputParam = (IActionParameter) entry.getValue();

      if ( !outputParam.isOutputParameter() ) {
        continue;
      }

      // The output Action Parameter objects do not have values - they are just the definition
      // Pull the value from allParams
      IActionParameter inputParam = (IActionParameter) allParams.get( outputName );
      if ( inputParam == null ) {
        returnMap.put( outputParam.getName(), null );
      } else {
        for ( Iterator varIt = outputParam.getVariables().iterator(); varIt.hasNext(); ) {
          ActionParameterSource src = (ActionParameterSource) varIt.next();
          returnMap.put( outputParam.getName(), new ReturnParameter( src.getSourceName(), src.getValue(), inputParam
              .getValue() ) );
        }
      }
    }
    return ( returnMap );
  }

  public class ReturnParameter {
    public String destinationName;

    public String destinationParameter;

    public Object value;

    public ReturnParameter( final String destinationName, final String destinationParameter, final Object value ) {
      this.destinationName = destinationName;
      this.destinationParameter = destinationParameter;
      this.value = value;
    }
  }

  public String getActualRequestParameterName( final String fieldName ) {
    /*
     * This method solves the problem that exists when generating an xForm based on the parameter definition. The
     * parameter definition looks like this:
     * 
     * <REGION type="string"> <default-value></default-value> <sources> <request>regn</request> </sources>
     * </REGION>
     * 
     * In the above definition, the parameter name is REGION, but we'll be looking for the variable regn in the
     * request. Before this fix, the XForm would generate code that puts REGION on the request, not regn.
     * 
     * MB
     */
    // TODO - figure out the actual name used - this is just a guess - maybe store in the ActionParam
    IActionParameter actionParameter = getCurrentInput( fieldName );
    if ( actionParameter != null ) {
      List vars = actionParameter.getVariables();

      for ( int i = 0; i < vars.size(); i++ ) {
        ActionParameterSource source = (ActionParameterSource) ( vars.get( i ) );
        if ( source.getSourceName().equals( IParameterProvider.SCOPE_REQUEST ) ) {
          return source.getValue();
        }
      }
    }
    return fieldName;
  }

  public IActionParameter getInput( final String inputName ) {
    return ( (IActionParameter) allParams.get( inputName ) );
  }

}
