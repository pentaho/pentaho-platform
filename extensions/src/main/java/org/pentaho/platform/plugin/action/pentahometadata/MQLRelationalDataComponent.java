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
