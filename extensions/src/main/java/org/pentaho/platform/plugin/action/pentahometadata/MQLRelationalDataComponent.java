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


package org.pentaho.platform.plugin.action.pentahometadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.actions.MQLAction;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This component has been replaced with the MetadataQueryComponent
 * 
 * @deprecated
 */
public class MQLRelationalDataComponent extends ComponentBase {

  private static final long serialVersionUID = -6376955619869902045L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog( MQLRelationalDataComponent.class );
  }

  private boolean initialize() {
    return true;
  }

  @Override
  public boolean validateAction() {

    boolean result = true;
    if ( !( getActionDefinition() instanceof MQLAction ) ) {
      error( Messages.getInstance().getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML() ) ); //$NON-NLS-1$
      result = false;
    } else if ( !initialize() ) {
      result = false;
    } else {
      MQLAction mqlAction = (MQLAction) getActionDefinition();
      IActionInput query = mqlAction.getQuery();
      result = ( query != ActionInputConstant.NULL_INPUT );
    }

    return result;
  }

  @Override
  public boolean executeAction() {
    //
    // For backwards compatibility, call into the new metadata query component
    //

    MetadataQueryComponent component = new MetadataQueryComponent();
    // setup component
    MQLAction actionDefinition = (MQLAction) getActionDefinition();

    String mql = actionDefinition.getQuery().getStringValue();

    component.setQuery( mql );

    if ( actionDefinition.getMaxRows() != ActionInputConstant.NULL_INPUT ) {
      component.setMaxRows( actionDefinition.getMaxRows().getIntValue() );
    }

    if ( actionDefinition.getQueryTimeout() != ActionInputConstant.NULL_INPUT ) {
      component.setTimeout( actionDefinition.getQueryTimeout().getIntValue() );
    }

    if ( actionDefinition.getReadOnly() != ActionInputConstant.NULL_INPUT ) {
      component.setReadOnly( actionDefinition.getReadOnly().getBooleanValue() );
    }

    // log the sql to info if set
    if ( isDefinedInput( "logSql" ) ) { //$NON-NLS-1$
      component.setLogSql( "true".equals( actionDefinition.getInput( "logSql" ).getStringValue() ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    // TODO: We also need to pass in the component definitions
    Set<String> inputNames = getInputNames();
    if ( inputNames != null ) {
      Map<String, Object> inputMap = new HashMap<String, Object>();
      for ( String inputName : inputNames ) {
        inputMap.put( ActionDefinitionEncoder.decodeBlankSpaces( inputName ), getInputValue( inputName ) );
      }
      component.setInputs( inputMap );
    }

    boolean success = component.execute();

    if ( success ) {
      IActionOutput actionOutput = actionDefinition.getOutputResultSet();
      if ( actionOutput != null ) {
        actionOutput.setValue( component.getResultSet() );
      }
    }

    return success;
  }

  @Override
  public void done() {
  }

  @Override
  public boolean init() {
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }
}
