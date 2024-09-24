/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
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
