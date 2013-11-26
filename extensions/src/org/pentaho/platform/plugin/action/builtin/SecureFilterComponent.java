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

package org.pentaho.platform.plugin.action.builtin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.ISelectionMapper;
import org.pentaho.platform.engine.services.runtime.SelectionMapper;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The secure filter component has two separate but related functions. It allows you to customize the default prompting
 * done by the runtime context and can verify that only valid selections are returned.
 */
public class SecureFilterComponent extends ComponentBase {

  private static final long serialVersionUID = 7119516440509549539L;

  List selList = new ArrayList(); // list of valid selections

  List hiddenList = new ArrayList(); // List of hidden fields that will be

  // added if any prompting happens.

  @Override
  public Log getLogger() {
    return LogFactory.getLog( SecureFilterComponent.class );
  }

  @Override
  protected boolean validateSystemSettings() {
    // No System Settings to validate
    return true;
  }

  @Override
  public boolean validateAction() {
    Node compDef = getComponentDefinition();
    List selNodes = compDef.selectNodes( "selections/*" ); //$NON-NLS-1$

    String inputName = null;
    boolean isOk = true;

    for ( Iterator it = selNodes.iterator(); it.hasNext(); ) {
      Node node = (Node) it.next();
      try {
        inputName = node.getName(); // Get the Data Node
        IActionParameter inputParam = getInputParameter( inputName );
        String filterType = XmlDom4JHelper.getNodeText( "@filter", node, null ); //$NON-NLS-1$

        // BISERVER-149 Changed isOptional param to default to false in order to
        // enable prompting when no default value AND no selection list is given...
        // This is also the default that Design Studio presumes.

        String optionalParm = XmlDom4JHelper.getNodeText( "@optional", node, "false" ); //$NON-NLS-1$ //$NON-NLS-2$
        boolean isOptional = "true".equals( optionalParm ); //$NON-NLS-1$

        if ( "none".equalsIgnoreCase( filterType ) ) { //$NON-NLS-1$
          IActionParameter selectParam = getInputParameter( inputName );
          String title = XmlDom4JHelper.getNodeText( "title", node, inputName ); //$NON-NLS-1$
          String valueCol = ""; //$NON-NLS-1$
          String dispCol = ""; //$NON-NLS-1$
          String displayStyle = XmlDom4JHelper.getNodeText( "@style", node, null ); //$NON-NLS-1$
          boolean promptOne =
              "true".equalsIgnoreCase( XmlDom4JHelper.getNodeText( "@prompt-if-one-value", node, "false" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          if ( "hidden".equals( displayStyle ) ) { //$NON-NLS-1$
            hiddenList.add( new SelEntry( inputParam, selectParam, valueCol, dispCol, title, displayStyle, promptOne,
                isOptional ) );
          } else {
            selList.add( new SelEntry( inputParam, selectParam, valueCol, dispCol, title, displayStyle, promptOne,
                isOptional ) );
          }
        } else {
          Node filterNode = node.selectSingleNode( "filter" ); //$NON-NLS-1$
          IActionParameter selectParam = getInputParameter( filterNode.getText().trim() );

          String valueCol = XmlDom4JHelper.getNodeText( "@value-col-name", filterNode, null ); //$NON-NLS-1$
          String dispCol = XmlDom4JHelper.getNodeText( "@display-col-name", filterNode, null ); //$NON-NLS-1$

          String title = XmlDom4JHelper.getNodeText( "title", node, null ); //$NON-NLS-1$
          String displayStyle = XmlDom4JHelper.getNodeText( "@style", node, null ); //$NON-NLS-1$
          boolean promptOne =
              "true".equalsIgnoreCase( XmlDom4JHelper.getNodeText( "@prompt-if-one-value", node, "false" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

          selList.add( new SelEntry( inputParam, selectParam, valueCol, dispCol, title, displayStyle, promptOne,
              isOptional ) );
        }

      } catch ( Exception e ) { // Catch the exception to let us test all
        // the params
        isOk = false;
        error( Messages.getInstance().getErrorString( "SecureFilterComponent.ERROR_0001_PARAM_MISSING", inputName ) ); //$NON-NLS-1$
      }
    }

    return ( isOk );
  }

  @Override
  public boolean executeAction() {

    boolean parameterUINeeded = false;
    if ( getOutputPreference() == IOutputHandler.OUTPUT_TYPE_PARAMETERS ) {
      parameterUINeeded = true;
    }

    boolean stopHere = getInputBooleanValue( StandardSettings.HANDLE_ALL_PROMPTS, true );

    SelEntry entry;
    boolean isOk = true;
    boolean causingPrompting = false; // Set to true if this securefilter
    // component actually adds feeback parameters.
    for ( Iterator it = selList.iterator(); it.hasNext(); ) {
      entry = (SelEntry) it.next();

      ISelectionMapper selMap =
          SelectionMapper.create( entry.selectionParam, entry.valueCol, entry.dispCol,
            entry.title, entry.displayStyle );

      // If we have a value for the input param, verify that it is within the selections
      if ( ( entry.inputParam.hasValue() || ( entry.inputParam.hasDefaultValue() && !feedbackAllowed() ) )
          && !parameterUINeeded ) {
        // TODO support numeric values here
        Object value = entry.inputParam.getValue();
        if ( value instanceof String ) {
          if ( ( selMap != null ) && !selMap.hasValue( (String) value ) ) {
            if ( !entry.isOptional || !"".equals( value ) ) { //$NON-NLS-1$
              error( Messages
                  .getInstance()
                  .getErrorString(
                    "SecureFilterComponent.ERROR_0001_INVALID_SELECTION", entry.inputParam.getValue().toString(),
                    entry.inputParam.getName() ) ); //$NON-NLS-1$
              isOk = false;
            }
          } // else this should be ok, we are just checking for selMap's existance
        } else if ( value instanceof Object[] ) {
          // test each item
          if ( selMap != null ) {
            Object[] values = (Object[]) value;
            for ( Object element : values ) {
              if ( !selMap.hasValue( element.toString() ) ) {
                if ( !entry.isOptional || !"".equals( value ) ) { //$NON-NLS-1$
                  error( Messages
                      .getInstance()
                      .getErrorString(
                        "SecureFilterComponent.ERROR_0001_INVALID_SELECTION", entry.inputParam.getValue().toString(),
                        entry.inputParam.getName() ) ); //$NON-NLS-1$
                  isOk = false;
                }
              }
            }
          } // else this should be ok, we are just checking for selMap's existence
        } else {
          // we cannot validate this
          error( Messages
              .getInstance()
              .getErrorString(
                "SecureFilterComponent.ERROR_0001_INVALID_SELECTION", entry.inputParam.getValue().toString(),
                entry.inputParam.getName() ) ); //$NON-NLS-1$
          isOk = false;
        }
      } else { // Need to prompt
        if ( selMap == null ) {
          entry.createFeedbackParam = true;
          if ( !entry.isOptional ) {
            causingPrompting = true; // trigger feedback below
          }
        } else if ( !entry.promptOne && ( selMap.selectionCount() == 1 ) ) {
          entry.inputParam.setValue( selMap.getValueAt( 0 ) );
        } else if ( !feedbackAllowed() && entry.isOptional ) {
          isOk = true;
        } else if ( !feedbackAllowed() ) {
          isOk = false;
        } else {
          entry.createFeedbackParam = true;
          entry.selMap = selMap;
          if ( !entry.isOptional ) {
            causingPrompting = true; // trigger feedback below
          }
        }
      }
    } // Done with the regular selections

    if ( causingPrompting ) {

      // Only make the call to createFeedbackParameter if causingPrompting is true.
      for ( Iterator it = selList.iterator(); it.hasNext(); ) {
        entry = (SelEntry) it.next();
        if ( entry.createFeedbackParam ) {
          if ( entry.selMap != null ) {
            createFeedbackParameter( entry.selMap, entry.inputParam.getName(), entry.inputParam.getValue(),
                entry.isOptional );
          } else {
            // TODO support help/hints
            createFeedbackParameter( entry.inputParam.getName(), entry.title,
                "", entry.inputParam.getValue().toString(), true, entry.isOptional ); //$NON-NLS-1$
          }
          entry.inputParam.setPromptStatus( IActionParameter.PROMPT_PENDING );
        }
      }

      promptNeeded();
      if ( stopHere ) {
        promptNow();
      }

      // We want the hidden fields to be processed after everything else is processed because
      // we only want to add the hidden fields if this component has caused prompting to occur.
      for ( int i = 0; i < hiddenList.size(); i++ ) {
        entry = (SelEntry) hiddenList.get( i );
        Object value = entry.inputParam.getValue();
        if ( value instanceof String ) {
          createFeedbackParameter( entry.inputParam.getName(), entry.inputParam.getName(), "", (String) value, false ); //$NON-NLS-1$
        } else {
          // Support other types of hidden field parameters (like
          // Integer/Decimal/Etc.) by using toString.
          createFeedbackParameter( entry.inputParam.getName(), entry.inputParam.getName(), "", value.toString(), false ); //$NON-NLS-1$
        }
      }
    }
    return isOk;
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
   * @see org.pentaho.component.ComponentBase#init()
   */
  @Override
  public boolean init() {
    return true;
  }

  class SelEntry {
    IActionParameter inputParam;

    IActionParameter selectionParam;

    String valueCol, dispCol, title, displayStyle;

    boolean promptOne;

    ISelectionMapper selMap;

    boolean isOptional;

    boolean createFeedbackParam = false;

    SelEntry( final IActionParameter inputParam, final IActionParameter selectionParam, final String valueCol,
        final String dispCol, final String title, final String displayStyle, final boolean promptOne,
        final boolean optional ) {
      this.inputParam = inputParam;
      this.selectionParam = selectionParam;
      this.valueCol = valueCol;
      this.dispCol = dispCol;
      this.title = title;
      this.displayStyle = displayStyle;
      this.promptOne = promptOne;
      this.isOptional = optional;
    }

  }

}
