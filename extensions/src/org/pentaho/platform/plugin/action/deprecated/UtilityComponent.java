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

package org.pentaho.platform.plugin.action.deprecated;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.actionsequence.dom.ActionInput;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.actions.CopyParamAction;
import org.pentaho.actionsequence.dom.actions.FormatMsgAction;
import org.pentaho.actionsequence.dom.actions.PrintMapValsAction;
import org.pentaho.actionsequence.dom.actions.PrintParamAction;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Provides utilities to help manipulate parameters used in action sequences.
 * <p>
 * <ul>
 * <li><i>format</i> - Java style message formatting</li>
 * <li><i>getvalues</i> - Make the key value pairs from a property map available as action-outputs</li>
 * <li><i>copy</i> - Set the action-output with the value of the action input</li>
 * <li><i>tostring</i> - Sets the action-output to the string value of the action-input</li>
 * <li><i></i> -</li>
 */
@SuppressWarnings( "deprecation" )
public class UtilityComponent extends ComponentBase {

  /**
   * 
   */
  private static final long serialVersionUID = -3257037449482351540L;

  HashMap tmpOutputs = new HashMap();

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.component.ComponentBase#validate()
   */

  @Override
  public Log getLogger() {
    return LogFactory.getLog( UtilityComponent.class );
  }

  @Override
  protected boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  /**
   * @deprecated
   */
  @Deprecated
  private boolean validateAction( final IActionDefinition actionDefinition ) {
    boolean result = true;
    Element[] elements = actionDefinition.getComponentDefElements( "*" ); //$NON-NLS-1$
    for ( Element element : elements ) {
      String commandName = element.getName();
      if ( "format".equalsIgnoreCase( commandName ) ) { //$NON-NLS-1$
        if ( element.selectSingleNode( "format-string" ) == null ) { //$NON-NLS-1$
          error( Messages.getInstance().getErrorString( "TestComponent.ERROR_0002_PARAMETER_MISSING", "format-string" ) ); //$NON-NLS-1$ //$NON-NLS-2$
          result = false;
        }
      } else if ( "getmapvalues".equalsIgnoreCase( commandName ) ) { //$NON-NLS-1$
        if ( element.selectSingleNode( "property-map" ) == null ) { //$NON-NLS-1$
          error( Messages.getInstance().getErrorString( "TestComponent.ERROR_0002_PARAMETER_MISSING", "format-string" ) ); //$NON-NLS-1$ //$NON-NLS-2$
          result = false;
        }
        List paramList = element.selectNodes( "arg" ); //$NON-NLS-1$
        if ( paramList.size() < 1 ) {
          error( Messages
              .getInstance()
              .getErrorString(
                "TestComponent.ERROR_0003_PARAMETER_MISSING", "arg", String.valueOf( 1 ),
                String.valueOf( paramList.size() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
          result = false;
        }
      }
    }
    return result;
  }

  private boolean validateCopyAction( final CopyParamAction copyParamAction ) {
    return true;
  }

  private boolean validateFormatAction( final FormatMsgAction formatMsgAction ) {
    boolean result = true;
    if ( formatMsgAction.getFormatString() == null ) {
      error( Messages.getInstance().getErrorString( "TestComponent.ERROR_0002_PARAMETER_MISSING", "format-string" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      result = false;
    }
    return result;
  }

  private boolean validatePrintParamAction( final PrintParamAction printParamAction ) {
    return true;
  }

  private boolean validateGetMapValuesAction( final PrintMapValsAction getMapValsAction ) {
    boolean result = true;
    if ( getMapValsAction.getPropertyMap() == ActionInputConstant.NULL_INPUT ) {
      error( Messages.getInstance().getErrorString( "TestComponent.ERROR_0002_PARAMETER_MISSING", "format-string" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      result = false;
    }
    if ( getMapValsAction.getKeys().length < 1 ) {
      error( Messages.getInstance().getErrorString(
        "TestComponent.ERROR_0003_PARAMETER_MISSING", "arg", String.valueOf( 1 ), "0" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      result = false;
    }
    return result;
  }

  /**
   * @deprecated
   */
  @Deprecated
  private boolean executeAction( final IActionDefinition actionDefinition ) {
    boolean result = true;
    Element[] elements = actionDefinition.getComponentDefElements( "*" ); //$NON-NLS-1$
    for ( Element element : elements ) {
      String commandName = element.getName();
      if ( "format".equalsIgnoreCase( commandName ) ) { //$NON-NLS-1$
        result = executeFormatAction( element );
      } else if ( "print".equalsIgnoreCase( commandName ) ) { //$NON-NLS-1$
        result = executePrintParamAction( element );
      } else if ( "copy".equalsIgnoreCase( commandName ) ) { //$NON-NLS-1$
        result = executeCopyAction( element );
      } else if ( "getmapvalues".equalsIgnoreCase( commandName ) ) { //$NON-NLS-1$
        result = executeGetMapValuesAction( element );
      }
    }

    if ( result ) {
      Set outNames = getOutputNames();
      for ( Iterator it = outNames.iterator(); it.hasNext(); ) {
        String name = (String) it.next();
        Object value = tmpOutputs.get( name );
        if ( value != null ) {
          setOutputValue( name, value );
        }
      }
    }
    return result;
  }

  /**
   * @deprecated
   */
  @Deprecated
  private boolean executeCopyAction( final Element componentDefinition ) {
    boolean result = true;

    String inputName = null;
    Element element = componentDefinition.element( "from" ); //$NON-NLS-1$
    if ( element != null ) {
      inputName = element.getText();
    }

    String outputName = null;
    element = componentDefinition.element( "return" ); //$NON-NLS-1$
    if ( element != null ) {
      outputName = element.getText();
    }

    if ( ( inputName != null ) && ( outputName != null ) ) {
      try {
        tmpOutputs.put( outputName, getValueOf( inputName ) );
      } catch ( Exception e ) {
        error( Messages.getInstance().getString( "UtilityComponent.ERROR_0003_ERROR_COPYING_PARAMETER" ) ); //$NON-NLS-1$
        result = false;
      }
    }

    return result;
  }

  /**
   * @deprecated
   */
  @Deprecated
  private boolean executeFormatAction( final Element componentDefinition ) {
    String formatString = componentDefinition.element( "format-string" ).getText(); //$NON-NLS-1$

    String outputName = null;
    Element element = componentDefinition.element( "return" ); //$NON-NLS-1$
    if ( element != null ) {
      outputName = element.getText();
    }

    ArrayList formatArgs = new ArrayList();
    List paramList = componentDefinition.selectNodes( "arg" ); //$NON-NLS-1$
    for ( Iterator it = paramList.iterator(); it.hasNext(); ) {
      formatArgs.add( ( (Node) it.next() ).getText() );
    }

    boolean result = true;
    try {
      MessageFormat mf = new MessageFormat( formatString );
      String theResult = mf.format( formatArgs.toArray() );
      tmpOutputs.put( outputName, theResult );
    } catch ( Exception e ) {
      error( Messages.getInstance().getString( "UtilityComponent.ERROR_0001_FORMAT_ERROR" ) ); //$NON-NLS-1$
      result = false;
    }
    return result;
  }

  /**
   * @deprecated
   */
  @Deprecated
  private boolean executePrintParamAction( final Element componentDefinition ) {
    String delimiter = ""; //$NON-NLS-1$
    Element element = componentDefinition.element( "delimiter" ); //$NON-NLS-1$
    if ( element != null ) {
      delimiter = element.getText();
    }

    ArrayList paramNames = new ArrayList();
    List paramList = componentDefinition.selectNodes( "arg" ); //$NON-NLS-1$
    for ( Iterator it = paramList.iterator(); it.hasNext(); ) {
      paramNames.add( ( (Node) it.next() ).getText() );
    }

    boolean result = true;
    try {
      StringBuffer sb = new StringBuffer( "\n***************************************************************\n" ); //$NON-NLS-1$
      for ( Iterator it = paramNames.iterator(); it.hasNext(); ) {
        sb.append( getValueOf( it.next().toString() ) ).append( delimiter );
      }
      sb.append( "\n***************************************************************\n" ); //$NON-NLS-1$
      info( sb.toString() );
    } catch ( Exception e ) {
      error( Messages.getInstance().getString( "UtilityComponent.ERROR_0002_MESSAGE_LOG_ERROR" ) ); //$NON-NLS-1$
      result = false;
    }
    return result;
  }

  /**
   * @deprecated
   */
  @Deprecated
  private boolean executeGetMapValuesAction( final Element componentDefinition ) {
    String propertyMapName = null;
    Element element = componentDefinition.element( "property-map" ); //$NON-NLS-1$
    if ( element != null ) {
      propertyMapName = element.getText();
    }

    ArrayList keyNames = new ArrayList();
    List paramList = componentDefinition.selectNodes( "arg" ); //$NON-NLS-1$
    for ( Iterator it = paramList.iterator(); it.hasNext(); ) {
      keyNames.add( ( (Node) it.next() ).getText() );
    }

    boolean result = true;
    try {
      Object mapObj = getValueOf( propertyMapName );

      if ( !( mapObj instanceof Map ) ) {
        error( Messages.getInstance().getErrorString( "UtilityComponent.ERROR_0004_PARAMETER_NOT_MAP", "property-map" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        result = false;
      } else {
        Map srcMap = (Map) mapObj;
        for ( Iterator it = keyNames.iterator(); it.hasNext(); ) {
          String key = it.next().toString();
          tmpOutputs.put( key, srcMap.get( key ) );
        }
      }
    } catch ( Exception e ) {
      error( Messages.getInstance().getString( "UtilityComponent.ERROR_0005_GET_MAP_VALUES_ERROR" ) ); //$NON-NLS-1$
      result = false;
    }
    return result;
  }

  private boolean executeCopyAction( final CopyParamAction copyParamAction ) {
    boolean result = true;

    IActionInput actionInput = copyParamAction.getCopyFrom();
    IActionOutput actionOutput = copyParamAction.getOutputCopy();

    if ( ( actionInput instanceof ActionInput ) && ( actionOutput != null ) ) {
      try {
        actionOutput.setValue( actionInput.getValue() );
      } catch ( Exception ex ) {
        result = false;
      }
    }

    return result;
  }

  private boolean executeFormatAction( final FormatMsgAction formatMsgAction ) {

    boolean result = true;
    String formatString = formatMsgAction.getFormatString().getStringValue();
    IActionOutput actionOutput = formatMsgAction.getOutputString();
    IActionInput[] msgInputs = formatMsgAction.getMsgInputs();

    ArrayList formatArgs = new ArrayList();
    for ( IActionInput element : msgInputs ) {
      formatArgs.add( element.getStringValue() );
    }

    try {
      MessageFormat mf = new MessageFormat( formatString );
      String theResult = mf.format( formatArgs.toArray() );
      if ( actionOutput != null ) {
        actionOutput.setValue( theResult );
      }
    } catch ( Exception ex ) {
      result = false;
    }
    return result;
  }

  private boolean executePrintParamAction( final PrintParamAction printParamAction ) {
    String delimiter = printParamAction.getDelimiter().getStringValue( "" ); //$NON-NLS-1$
    IActionInput[] inputsToPrint = printParamAction.getInputsToPrint();
    boolean result = true;
    try {
      StringBuffer sb = new StringBuffer( "\n***************************************************************\n" ); //$NON-NLS-1$
      for ( IActionInput element : inputsToPrint ) {
        sb.append( element.getStringValue( "" ) ).append( delimiter ); //$NON-NLS-1$
      }
      sb.append( "\n***************************************************************\n" ); //$NON-NLS-1$
      info( sb.toString() );
    } catch ( Exception e ) {
      error( Messages.getInstance().getString( "UtilityComponent.ERROR_0002_MESSAGE_LOG_ERROR" ) ); //$NON-NLS-1$
      result = false;
    }
    return result;
  }

  private boolean executeGetMapValuesAction( final PrintMapValsAction getMapValsAction ) {
    IActionInput propertyMap = getMapValsAction.getPropertyMap();
    IActionInput[] keys = getMapValsAction.getKeys();
    boolean result = true;
    try {
      if ( !( propertyMap.getValue() instanceof Map ) ) {
        error( Messages.getInstance().getErrorString( "UtilityComponent.ERROR_0004_PARAMETER_NOT_MAP", "property-map" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        result = false;
      } else {
        Map srcMap = (Map) propertyMap.getValue();
        for ( IActionInput element : keys ) {
          String key = element.getStringValue();
          getMapValsAction.getOutput( key ).setValue( srcMap.get( key ) );
        }
      }
    } catch ( Exception e ) {
      error( Messages.getInstance().getString( "UtilityComponent.ERROR_0005_GET_MAP_VALUES_ERROR" ) ); //$NON-NLS-1$
      result = false;
    }
    return result;
  }

  @Override
  protected boolean validateAction() {
    boolean result = true;
    IActionDefinition actionDefinition = getActionDefinition();
    if ( actionDefinition instanceof CopyParamAction ) {
      result = validateCopyAction( (CopyParamAction) actionDefinition );
    } else if ( actionDefinition instanceof FormatMsgAction ) {
      result = validateFormatAction( (FormatMsgAction) actionDefinition );
    } else if ( actionDefinition instanceof PrintMapValsAction ) {
      result = validateGetMapValuesAction( (PrintMapValsAction) actionDefinition );
    } else if ( actionDefinition instanceof PrintParamAction ) {
      result = validatePrintParamAction( (PrintParamAction) actionDefinition );
    } else {
      // This component allows multiple actions to be defined in a single action definition.
      // While this is no longer supported by the design studio, it needs to be supported here
      // for backwards compatibility with older action sequence documents.
      result = validateAction( actionDefinition );
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.component.ComponentBase#done()
   */
  @Override
  public void done() {

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.component.ComponentBase#execute()
   */
  @Override
  protected boolean executeAction() {
    IActionDefinition actionDefinition = getActionDefinition();
    tmpOutputs = new HashMap(); // Make sure we start with an empty list in
    boolean result = true;
    if ( actionDefinition instanceof CopyParamAction ) {
      executeCopyAction( (CopyParamAction) actionDefinition );
    } else if ( actionDefinition instanceof FormatMsgAction ) {
      executeFormatAction( (FormatMsgAction) actionDefinition );
    } else if ( actionDefinition instanceof PrintMapValsAction ) {
      executeGetMapValuesAction( (PrintMapValsAction) actionDefinition );
    } else if ( actionDefinition instanceof PrintParamAction ) {
      executePrintParamAction( (PrintParamAction) actionDefinition );
    } else {
      // This component allows multiple actions to be defined in a single action definition.
      // While this is no longer supported by the design studio, it needs to be supported here
      // for backwards compatibility with older action sequence documents.
      result = executeAction( actionDefinition );
    }
    return result;
    // this iteration
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.component.ComponentBase#init()
   */
  @Override
  public boolean init() {
    if ( ComponentBase.debug ) {
      debug( Messages.getInstance().getString( "TestComponent.DEBUG_INITIALIZING_TEST" ) ); //$NON-NLS-1$
    }
    return true;
  }

  protected Object getActionParameterValue( final String name ) {
    try {
      return ( getInputValue( name ) );
    } catch ( Exception e ) {
      //ignore
    } // Return null if it doesn't exist

    return ( null );
  }

  Object getValueOf( final String paramName ) {
    if ( paramName == null ) {
      return ( null );
    }

    if ( paramName.startsWith( "\"" ) && paramName.endsWith( "\"" ) ) { //$NON-NLS-1$ //$NON-NLS-2$
      if ( paramName.length() < 3 ) {
        return ( "" ); //$NON-NLS-1$
      }
      return ( paramName.substring( 1, paramName.length() - 1 ) );
    }

    Object obj = tmpOutputs.get( paramName );
    if ( obj != null ) {
      return ( obj );
    }

    return ( getInputValue( paramName ) );
  }
}
