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


package org.pentaho.platform.engine.core;

import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;

import java.util.List;
import java.util.Map;

@SuppressWarnings( { "all" } )
public class TestActionParameter implements IActionParameter {

  public Object value;

  public void dispose() {
    // Auto-generated method stub

  }

  public String getName() {
    // Auto-generated method stub
    return null;
  }

  public int getPromptStatus() {
    // Auto-generated method stub
    return 0;
  }

  public String getSelectionDisplayName() {
    // Auto-generated method stub
    return null;
  }

  public String getSelectionNameForValue( String value ) {
    // Auto-generated method stub
    return null;
  }

  public Map getSelectionNameMap() {
    // Auto-generated method stub
    return null;
  }

  public List getSelectionValues() {
    // Auto-generated method stub
    return null;
  }

  public String getStringValue() {
    // Auto-generated method stub
    return null;
  }

  public String getType() {
    // Auto-generated method stub
    return null;
  }

  public Object getValue() {
    return value;
  }

  public List getValueAsList() {
    // Auto-generated method stub
    return null;
  }

  public IPentahoResultSet getValueAsResultSet() {
    // Auto-generated method stub
    return null;
  }

  public List getVariables() {
    // Auto-generated method stub
    return null;
  }

  public boolean hasDefaultValue() {
    // Auto-generated method stub
    return false;
  }

  public boolean hasSelections() {
    // Auto-generated method stub
    return false;
  }

  public boolean hasValue() {
    // Auto-generated method stub
    return false;
  }

  public boolean isDefaultValue() {
    // Auto-generated method stub
    return false;
  }

  public boolean isNull() {
    // Auto-generated method stub
    return false;
  }

  public void setParamSelections( List selValues, Map selNames, String displayname ) {
    // Auto-generated method stub

  }

  public boolean setPromptStatus( int status ) {
    // Auto-generated method stub
    return false;
  }

  public void setValue( Object value ) {
    this.value = value;
  }

  public boolean isOutputParameter() {
    return true;
  }
}
