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
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author James Dixon
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class TestComponent extends ComponentBase {

  private static final long serialVersionUID = -5888733281367666385L;

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.component.ComponentBase#validate()
   */

  @Override
  public Log getLogger() {
    return LogFactory.getLog( TestComponent.class );
  }

  private void message( final String message ) {
    info( message );
    System.out.println( message );
  }

  @Override
  protected boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  protected boolean validateAction() {

    // describe the inputs, outputs and resources available to us
    Set inputNames = getInputNames();
    Iterator inputNamesIterator = inputNames.iterator();
    String inputName;
    IActionParameter actionParameter;
    while ( inputNamesIterator.hasNext() ) {
      inputName = (String) inputNamesIterator.next();
      actionParameter = getInputParameter( inputName );
      message( Messages.getInstance().getString(
        "TestComponent.DEBUG_INPUT_DESCRIPTION", inputName, actionParameter.getType() ) ); //$NON-NLS-1$
    }

    Set outputNames = getOutputNames();
    Iterator outputNamesIterator = outputNames.iterator();
    String outputName;
    while ( outputNamesIterator.hasNext() ) {
      outputName = (String) outputNamesIterator.next();
      actionParameter = getOutputItem( outputName );
      message( Messages.getInstance().getString(
        "TestComponent.DEBUG_OUTPUT_DESCRIPTION", outputName, actionParameter.getType() ) ); //$NON-NLS-1$
    }

    Set resourceNames = getResourceNames();
    Iterator resourceNamesIterator = resourceNames.iterator();
    String resourceName;
    IActionSequenceResource actionResource;
    while ( resourceNamesIterator.hasNext() ) {
      resourceName = (String) resourceNamesIterator.next();
      actionResource = getResource( resourceName );
      message( Messages
          .getInstance()
          .getString(
            "TestComponent.DEBUG_RESOURCE_DESCRIPTION", resourceName, actionResource.getMimeType(),
            actionResource.getAddress() ) ); //$NON-NLS-1$
      try {
        String content = getResourceAsString( actionResource );
        message( Messages.getInstance().getString(
            "TestComponent.DEBUG_RESOURCE_CONTENTS", ( ( content == null ) ? "null" : content.substring( 0, 100 ) ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
      } catch ( Exception e ) {
        message( Messages.getInstance().getString( "TestComponent.ERROR_0005_RESOURCE_NOT_LOADED", e.getMessage() ) ); //$NON-NLS-1$
      }
    }

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.component.ComponentBase#done()
   */
  @Override
  public void done() {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.component.ComponentBase#execute()
   */
  @Override
  protected boolean executeAction() {
    message( Messages.getInstance().getString( "TestComponent.DEBUG_EXECUTING_TEST" ) ); //$NON-NLS-1$
    Node componentNode = getComponentDefinition();

    Set inputNames = getInputNames();
    Iterator inputNamesIterator = inputNames.iterator();
    String inputName;
    IActionParameter actionParameter;
    while ( inputNamesIterator.hasNext() ) {
      inputName = (String) inputNamesIterator.next();
      actionParameter = getInputParameter( inputName );

      message( Messages
          .getInstance()
          .getString(
            "TestComponent.DEBUG_INPUT_DESCRIPTION", inputName,
            actionParameter.getValue().getClass().toString() + "=" + actionParameter.getValue().toString() ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    String test = XmlDom4JHelper.getNodeText( "test", componentNode ); //$NON-NLS-1$
    if ( ( test == null ) || ( test.length() < 1 ) ) {
      message( componentNode.asXML() );
      return ( true );
    }

    String newName = XmlDom4JHelper.getNodeText( "newname", componentNode ); //$NON-NLS-1$
    Object theResult = null;

    if ( "format".equals( test ) ) { //$NON-NLS-1$
      MessageFormat mf = new MessageFormat( XmlDom4JHelper.getNodeText( "p1", componentNode, "" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      Object[] obj =
      { getParamFromComponentNode( "p2", componentNode ), getParamFromComponentNode( "p3", componentNode ) }; //$NON-NLS-1$ //$NON-NLS-2$
      theResult = mf.format( obj );
    } else {
      Object p1 = getParamFromComponentNode( "p1", componentNode ); //$NON-NLS-1$
      if ( p1 == null ) {
        return ( false );
      } else if ( "toupper".equals( test ) ) { //$NON-NLS-1$

        theResult = p1.toString().toUpperCase();
      } else if ( "rename".equals( test ) ) { //$NON-NLS-1$
        theResult = p1;
      } else if ( "map2params".equals( test ) ) { //$NON-NLS-1$

        if ( !( p1 instanceof Map ) ) {
          error( Messages.getInstance().getErrorString( "TestComponent.ERROR_0003_PARAMETER_NOT_MAP", "p1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
          return ( false );
        }

        Map srcMap = (Map) p1;
        for ( Iterator it = srcMap.keySet().iterator(); it.hasNext(); ) {
          String key = it.next().toString();
          setOutputValue( key, srcMap.get( key ) );
        }

      } else if ( "print".equals( test ) ) { //$NON-NLS-1$

        String delim = "\r\n***************************************************************\r\n"; //$NON-NLS-1$
        theResult = delim + p1.toString() + delim;
      } else if ( "getkeys".equals( test ) ) { //$NON-NLS-1$

        if ( !( p1 instanceof Map ) ) {
          error( Messages.getInstance().getErrorString( "TestComponent.ERROR_0003_PARAMETER_NOT_MAP", "p1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
          return ( false );
        }
        theResult = new ArrayList( ( (Map) p1 ).keySet() );
      } else {

        Object p2 = getParamFromComponentNode( "p2", componentNode ); //$NON-NLS-1$
        if ( p2 == null ) {
          return ( false );
        }

        if ( "concat".equals( test ) ) { //$NON-NLS-1$
          theResult = p1.toString() + p2.toString();
        } else if ( "print2".equals( test ) ) { //$NON-NLS-1$

          String delim = Messages.getInstance().getString( "TestComponent.CODE_PRINT_DELIM" ); //$NON-NLS-1$
          theResult = delim + p1.toString() + " - " + p2.toString() + delim; //$NON-NLS-1$
        } else {

          Object p3 = getParamFromComponentNode( "p3", componentNode ); //$NON-NLS-1$
          if ( p3 == null ) {
            return ( false );
          }

          if ( "merge".equals( test ) ) { //$NON-NLS-1$ 

            // merge cycles through each property map in list p2.
            // For each map, it cycles through the keys in map p1
            // and compares the key name
            // from p1 with a value from p2. p3 specifies the key in
            // p2 to compare with. When a match is found, an entry
            // is added to an output
            // output list that is identical to the map from p2. The
            // value specified by the key in p1 will be added to the
            // output under the key "NewKey"

            if ( !( p1 instanceof Map ) || !( p2 instanceof List ) || !( p3 instanceof String ) ) {
              error( Messages.getInstance().getErrorString( "TestComponent.ERROR_0004_P1_P2_WRONG_TYPE" ) ); //$NON-NLS-1$
              return ( false );
            }

            theResult = merge( (Map) p1, (List) p2, (String) p3 );
          } else {
            message( Messages.getInstance().getErrorString( "TestComponent.ERROR_0001_TEST_NODE_NOT_FOUND" ) ); //$NON-NLS-1$
            return false;
          }
        }
      }
    }

    if ( newName != null ) {
      message( newName + " = " + theResult ); //$NON-NLS-1$
      try {
        setOutputValue( newName, theResult );
      } catch ( Exception e ) {
        //ignore
      } // setOutputValue logs an error mesage
    } else {
      message( "The result = " + theResult ); //$NON-NLS-1$
    }

    return ( true );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.component.ComponentBase#init()
   */
  @Override
  public boolean init() {
    message( Messages.getInstance().getString( "TestComponent.DEBUG_INITIALIZING_TEST" ) ); //$NON-NLS-1$
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

  private List merge( final Map hm, final List list, final String keyName ) {
    ArrayList al = new ArrayList();
    for ( Iterator it = list.iterator(); it.hasNext(); ) {
      Object item = it.next();
      if ( item instanceof Map ) {
        Map resMap = merge( hm, (Map) item, keyName );
        if ( resMap != null ) {
          al.add( resMap );
        }
      }
    }
    return ( al );
  }

  private Map merge( final Map srcMap, final Map destMap, final String keyName ) {
    Object keyValue = destMap.get( keyName );
    Map rtnMap = null;
    for ( Iterator it = srcMap.keySet().iterator(); it.hasNext(); ) {
      String key = it.next().toString();
      if ( ( keyValue != null ) && key.equalsIgnoreCase( keyValue.toString() ) ) {
        rtnMap = new HashMap( destMap );
        rtnMap.put( "NewKey", srcMap.get( key ) ); //$NON-NLS-1$
        return ( rtnMap );
      }
    }

    return ( rtnMap );
  }

  private Object getParamFromComponentNode( final String paramName, final Node componentNode ) {
    String param = XmlDom4JHelper.getNodeText( paramName, componentNode );
    if ( ( param == null ) || ( param.length() < 1 ) ) {
      error( Messages.getInstance().getErrorString( "TestComponent.ERROR_0002_PARAMETER_MISSING", paramName ) ); //$NON-NLS-1$
      return ( null );
    }
    return ( getActionParameterValue( param ) );
  }
}
