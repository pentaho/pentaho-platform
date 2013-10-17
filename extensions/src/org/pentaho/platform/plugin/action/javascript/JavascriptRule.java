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

package org.pentaho.platform.plugin.action.javascript;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.actions.JavascriptAction;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.condition.javascript.RhinoScriptable;
import org.pentaho.platform.plugin.services.connections.javascript.JavaScriptResultSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author James Dixon
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class JavascriptRule extends ComponentBase {

  /**
   * 
   */
  private static final long serialVersionUID = -8305132222755452461L;

  private boolean oldStyleOutputs;

  @Override
  public Log getLogger() {
    return LogFactory.getLog( JavascriptRule.class );
  }

  @Override
  protected boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  protected boolean validateAction() {
    boolean actionValidated = true;
    JavascriptAction jscriptAction = null;

    if ( getActionDefinition() instanceof JavascriptAction ) {
      jscriptAction = (JavascriptAction) getActionDefinition();

      // get report connection setting
      if ( jscriptAction.getScript() == ActionInputConstant.NULL_INPUT ) {
        error( Messages.getInstance().getErrorString( "JSRULE.ERROR_0001_SCRIPT_NOT_DEFINED", getActionName() ) ); //$NON-NLS-1$
        actionValidated = false;
      }

      if ( actionValidated ) {
        if ( jscriptAction.getOutputs().length <= 0 ) {
          error( Messages.getInstance().getString( "Template.ERROR_0002_OUTPUT_COUNT_WRONG" ) ); //$NON-NLS-1$
          actionValidated = false;
        }
      }

      if ( actionValidated ) {
        IActionOutput[] outputs = jscriptAction.getOutputs(); // getOutputNames();
        /*
         * if the number of action def outputs is more than 1 and if the fist output var defined in the input section is
         * named output1 then the xaction is defined oldstyle and we should check if there is corresponding o/p variable
         * defined in the input section for each output var defined in the output section. NOTE: With the old style you
         * can only have output defined as "output#" where # is a number.
         */
        if ( ( outputs.length > 1 ) && ( jscriptAction.getInput( "output1" ) != ActionInputConstant.NULL_INPUT ) ) { //$NON-NLS-1$
          oldStyleOutputs = true;
          for ( int i = 1; i <= outputs.length; ++i ) {
            if ( jscriptAction.getInput( "output" + i ) == ActionInputConstant.NULL_INPUT ) { //$NON-NLS-1$
              error( Messages.getInstance().getErrorString(
                  "JavascriptRule.ERROR_0006_NO_MAPPED_OUTPUTS", String.valueOf( outputs.length ), String.valueOf( i ) ) ); //$NON-NLS-1$
              actionValidated = false;
              break;
            }
          }
        }
      }
    } else {
      actionValidated = false;
      error( Messages.getInstance().getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML() ) ); //$NON-NLS-1$      
    }

    return actionValidated;
  }

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
    Context cx = ContextFactory.getGlobal().enterContext();
    StringBuffer buffer = new StringBuffer();
    @SuppressWarnings( "unchecked" )
    Iterator<String> iter = getResourceNames().iterator();
    while ( iter.hasNext() ) {
      IActionSequenceResource resource = getResource( iter.next().toString() );
      // If this is a javascript resource then append it to the script string
      if ( "text/javascript".equalsIgnoreCase( resource.getMimeType() ) ) { //$NON-NLS-1$
        buffer.append( getResourceAsString( resource ) );
      }
    }

    List<String> outputNames = new ArrayList<String>();
    JavascriptAction jscriptAction = (JavascriptAction) getActionDefinition();

    IActionOutput[] actionOutputs = jscriptAction.getOutputs();
    if ( actionOutputs.length == 1 ) {
      String outputName = actionOutputs[0].getName();
      outputNames.add( outputName );
    } else {
      if ( oldStyleOutputs ) {
        int i = 1;
        while ( true ) {
          if ( jscriptAction.getInput( "output" + i ) != ActionInputConstant.NULL_INPUT ) { //$NON-NLS-1$
            outputNames.add( jscriptAction.getInput( "output" + i ).getStringValue() ); //$NON-NLS-1$
          } else {
            break;
          }
          i++;
        }
      } else {
        for ( IActionOutput element : actionOutputs ) {
          outputNames.add( element.getName() );
        }
      }
    }

    boolean success = false;
    try {
      String script = jscriptAction.getScript().getStringValue();
      if ( script == null ) {
        error( Messages.getInstance().getErrorString( "JSRULE.ERROR_0001_SCRIPT_NOT_DEFINED", getActionName() ) ); //$NON-NLS-1$
      } else {
        buffer.append( script );
        script = buffer.toString();
        if ( ComponentBase.debug ) {
          debug( "script=" + script ); //$NON-NLS-1$
        }
        try {
          ScriptableObject scriptable = new RhinoScriptable();
          // initialize the standard javascript objects
          Scriptable scope = cx.initStandardObjects( scriptable );

          Object resultObject = executeScript( scriptable, scope, script, cx );
          if ( oldStyleOutputs ) {
            if ( resultObject instanceof org.mozilla.javascript.NativeArray ) {
              // we need to convert this to an ArrayList
              NativeArray jsArray = (NativeArray) resultObject;
              int length = (int) jsArray.getLength();
              for ( int i = 0; i < length; i++ ) {
                Object value = jsArray.get( i, scriptable );
                if ( i < outputNames.size() ) {
                  jscriptAction.getOutput( outputNames.get( i ).toString() )
                      .setValue( convertWrappedJavaObject( value ) );
                } else {
                  break;
                }
              }
            } else {
              jscriptAction.getOutput( outputNames.get( 0 ).toString() ).setValue(
                convertWrappedJavaObject( resultObject ) );
            }
          } else {
            if ( ( outputNames.size() == 1 ) && ( resultObject != null ) ) {
              jscriptAction.getOutput( outputNames.get( 0 ).toString() ).setValue(
                convertWrappedJavaObject( resultObject ) );
            } else {
              List<String> setOutputs = new ArrayList<String>( outputNames.size() );
              Object[] ids = ScriptableObject.getPropertyIds( scope );
              for ( Object element : ids ) {
                int idx = outputNames.indexOf( element.toString() );
                if ( idx >= 0 ) {
                  jscriptAction.getOutput( outputNames.get( idx ).toString() ).setValue(
                      convertWrappedJavaObject( ScriptableObject.getProperty( scope, (String) element ) ) );
                  setOutputs.add( outputNames.get( idx ) );
                }
              }
              // Javascript Component defined an output, but
              // didn't set it to anything.
              // So, set it to null.
              if ( setOutputs.size() != outputNames.size() ) {
                for ( int i = 0; i < outputNames.size(); i++ ) {
                  if ( setOutputs.indexOf( outputNames.get( i ) ) < 0 ) {
                    // An output that wasn't set in the
                    // javascript component
                    jscriptAction.getOutput( outputNames.get( i ).toString() ).setValue( null );
                  }
                }
              }
            }

          }

          success = true;
        } catch ( Exception e ) {
          error( Messages.getInstance().getErrorString( "JSRULE.ERROR_0003_EXECUTION_FAILED" ), e ); //$NON-NLS-1$
        }
      }
    } finally {
      Context.exit();
    }
    return success;
  }

  protected Object executeScript( final ScriptableObject scriptable, final Scriptable scope, final String script,
      final Context cx ) throws Exception {
    ScriptableObject.defineClass( scope, JavaScriptResultSet.class );
    @SuppressWarnings( "unchecked" )
    Set<String> inputNames = getInputNames();
    Iterator<String> inputNamesIterator = inputNames.iterator();
    String inputName;
    Object inputValue;
    while ( inputNamesIterator.hasNext() ) {
      inputName = (String) inputNamesIterator.next();
      if ( inputName.indexOf( '-' ) >= 0 ) {
        throw new IllegalArgumentException( Messages.getInstance().getErrorString(
            "JSRULE.ERROR_0006_INVALID_JS_VARIABLE", inputName ) ); //$NON-NLS-1$
      }
      inputValue = getInputValue( inputName );
      Object wrapper;
      if ( inputValue instanceof IPentahoResultSet ) {
        JavaScriptResultSet results = new JavaScriptResultSet();

        // Required as of Rhino 1.7R1 to resolve caching, base object
        // inheritance and property tree
        results.setPrototype( scriptable );

        results.setResultSet( (IPentahoResultSet) inputValue );
        wrapper = Context.javaToJS( inputValue, results );
      } else {
        wrapper = Context.javaToJS( inputValue, scope );
      }
      ScriptableObject.putProperty( scope, inputName, wrapper );
    }

    // Add system out and this object to the scope
    Object wrappedOut = Context.javaToJS( System.out, scope );
    Object wrappedThis = Context.javaToJS( this, scope );
    ScriptableObject.putProperty( scope, "out", wrappedOut ); //$NON-NLS-1$
    ScriptableObject.putProperty( scope, "rule", wrappedThis ); //$NON-NLS-1$
    // evaluate the script
    return cx.evaluateString( scope, script, "<cmd>", 1, null ); //$NON-NLS-1$

  }

  protected Object convertWrappedJavaObject( final Object obj ) {
    // If we wrap an object going in, and simply return the object,
    // without unwrapping it, we're left with an object we can't
    // use. Case in point was a Java array going in, and being
    // wrapped as a org.mozilla.javascript.NativeJavaArray. On
    // the way back into the context though, it stayed a mozilla
    // object. This unwraps objects properly so that they can be
    // recognized throughout the system.
    if ( obj instanceof org.mozilla.javascript.NativeJavaObject ) {
      return ( (org.mozilla.javascript.NativeJavaObject) obj ).unwrap();
    } else {
      return obj;
    }
  }

  @Override
  public boolean init() {
    return true;
  }

}
