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

package org.pentaho.platform.engine.core;

import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;

import java.util.List;
import java.util.Map;

@SuppressWarnings( { "all" } )
public class TestActionParameter implements IActionParameter {

  public Object value;

  public void dispose() {
    // TODO Auto-generated method stub

  }

  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  public int getPromptStatus() {
    // TODO Auto-generated method stub
    return 0;
  }

  public String getSelectionDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getSelectionNameForValue( String value ) {
    // TODO Auto-generated method stub
    return null;
  }

  public Map getSelectionNameMap() {
    // TODO Auto-generated method stub
    return null;
  }

  public List getSelectionValues() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getStringValue() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getType() {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getValue() {
    return value;
  }

  public List getValueAsList() {
    // TODO Auto-generated method stub
    return null;
  }

  public IPentahoResultSet getValueAsResultSet() {
    // TODO Auto-generated method stub
    return null;
  }

  public List getVariables() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasDefaultValue() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean hasSelections() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean hasValue() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isDefaultValue() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isNull() {
    // TODO Auto-generated method stub
    return false;
  }

  public void setParamSelections( List selValues, Map selNames, String displayname ) {
    // TODO Auto-generated method stub

  }

  public boolean setPromptStatus( int status ) {
    // TODO Auto-generated method stub
    return false;
  }

  public void setValue( Object value ) {
    this.value = value;
  }

  public boolean isOutputParameter() {
    return true;
  }
}
