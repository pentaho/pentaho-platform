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

package org.pentaho.platform.engine.services.actionsequence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IDisposable;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionParameter implements IActionParameter {

  private String name;

  private String type;

  private Object value; // not persisted

  private List variables;

  private Object defaultValue;

  private ParamSelections paramSelections = null;

  private int promptType = IActionParameter.PROMPT_ALLOWED;

  private List saveLocations = null;

  private boolean isOutputParameter = true;

  private static final Log logger = LogFactory.getLog( ActionParameter.class );

  // should we force this to String for serialization?
  // TODO: add type checking

  public ActionParameter( final String name, final String type, final Object value, final List variables,
      final Object defaultValue ) {
    this.name = name;
    this.value = value;
    this.type = type;
    this.variables = variables;

    // DM - get this working here for now - should be it's own factory - fix
    // up all the conversions
    this.defaultValue = null;
    if ( defaultValue != null ) {
      if ( "string".equalsIgnoreCase( type ) ) { //$NON-NLS-1$ 
        this.defaultValue = defaultValue.toString();
      } else if ( "string-list".equalsIgnoreCase( type ) || "property-map-list".equalsIgnoreCase( type ) ) { //$NON-NLS-1$ //$NON-NLS-2$ 
        if ( defaultValue instanceof List ) {
          this.defaultValue = defaultValue;
        }
      } else if ( "property-map".equalsIgnoreCase( type ) ) { //$NON-NLS-1$
        if ( defaultValue instanceof Map ) {
          this.defaultValue = defaultValue;
        }
      } else if ( "result-set".equalsIgnoreCase( type ) ) { //$NON-NLS-1$
        if ( defaultValue instanceof IPentahoResultSet ) {
          this.defaultValue = defaultValue;
        }
      } else if ( "long".equalsIgnoreCase( type ) ) { //$NON-NLS-1$ 
        try {
          this.defaultValue = Long.valueOf( defaultValue.toString() );
        } catch ( NumberFormatException e ) {
          // @todo: throw an exception
          ActionParameter.logger.error( e.getLocalizedMessage() );
        }
      } else if ( "integer".equalsIgnoreCase( type ) ) { //$NON-NLS-1$ 
        try {
          this.defaultValue = Integer.valueOf( defaultValue.toString() );
        } catch ( NumberFormatException e ) {
          // @todo: throw an exception
          ActionParameter.logger.error( e.getLocalizedMessage() );
        }
      }
    }
    if ( ( value == null ) && ( defaultValue != null ) ) {
      promptType = IActionParameter.PROMPT_NEEDED;
    }
  }

  public List getSaveLocations() {
    return ( saveLocations == null ) ? new ArrayList() : saveLocations;
  }

  public void addSaveLocation( final String location ) {
    if ( saveLocations == null ) {
      saveLocations = new ArrayList();
    }
    saveLocations.add( location );
    return;
  }

  public List getVariables() {
    return ( variables == null ) ? new ArrayList() : variables;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getStringValue() {
    return ( ( value != null ) ? value.toString() : ( ( defaultValue != null ) ? defaultValue.toString() : null ) );
  }

  public Object getValue() {
    return ( ( value != null ) ? value : defaultValue );
  }

  public List getValueAsList() {
    Object rtn = ( value != null ) ? value : defaultValue;
    if ( rtn == null ) {
      return ( new ArrayList() );
    }

    if ( rtn instanceof List ) {
      return ( (List) rtn );
    }

    ArrayList al = new ArrayList();
    al.add( rtn );
    return ( al );
  }

  public IPentahoResultSet getValueAsResultSet() {
    Object rtn = ( value != null ) ? value : defaultValue;
    if ( rtn == null ) {
      return null;
    }

    if ( rtn instanceof IPentahoResultSet ) {
      return ( (IPentahoResultSet) rtn );
    }

    return null;
  }

  public void setValue( final Object value ) {
    // TODO need to validate the types here
    setPromptStatus( ( value == null ) ? IActionParameter.PROMPT_NEEDED : IActionParameter.PROMPT_ALLOWED );
    if ( this.value != value ) {
      dispose();
      this.value = value;
    }
  }

  public boolean hasDefaultValue() {
    return ( defaultValue != null );
  }

  public boolean hasValue() {
    return ( value != null );
  }

  public boolean isDefaultValue() {
    return ( ( value == null ) && ( defaultValue != null ) );
  }

  public boolean isNull() {
    return ( ( value == null ) && ( defaultValue == null ) );
  }

  public void dispose() {
    if ( ( value != null ) && ( value instanceof IDisposable ) ) {
      ( (IDisposable) value ).dispose();
    }
  }

  // // Selection Support

  public int getPromptType() {
    return ( promptType );
  }

  public boolean hasSelections() {
    return ( paramSelections != null );
  }

  public String getSelectionDisplayName() {
    return ( ( hasSelections() ) ? paramSelections.displayName : "" ); //$NON-NLS-1$
  }

  public String getSelectionNameForValue( final String val ) {
    Object rtn = null;
    if ( hasSelections() && ( paramSelections.selNames != null ) ) {
      rtn = paramSelections.selNames.get( val );
    }
    return ( ( rtn != null ) ? rtn.toString() : val );
  }

  public List getSelectionValues() {
    return ( ( hasSelections() ) ? paramSelections.selValues : new ArrayList() );
  }

  /**
   * Unused
   * 
   * @deprecated
   */
  @Deprecated
  public void setParamSelections( final List selValues, final Map selNames, final String displayname ) {
    paramSelections = new ParamSelections( selValues, selNames, displayname );
  }

  public Map getSelectionNameMap() {
    return ( ( hasSelections() ) ? paramSelections.selNames : new HashMap() );
  }

  class ParamSelections {
    Map selNames;

    List selValues;

    String displayName;

    ParamSelections( final List selValues, final Map selNames, final String displayname ) {
      this.displayName = ( displayname != null ) ? displayname : ""; //$NON-NLS-1$
      this.selNames = ( selNames != null ) ? selNames : new HashMap();
      this.selValues = ( selValues != null ) ? selValues : new ArrayList();
    }
  }

  public int getPromptStatus() {
    return ( promptType );
  }

  public boolean setPromptStatus( final int status ) {
    if ( ( promptType == IActionParameter.PROMPT_NEVER ) || ( status < 0 ) || ( status > 3 ) ) {
      return ( false );
    }

    promptType = status;
    return ( true );
  }

  public boolean isOutputParameter() {
    return isOutputParameter;
  }

  public void setOutputParameter( boolean isOutputParameter ) {
    this.isOutputParameter = isOutputParameter;
  }

}
