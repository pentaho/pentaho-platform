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


package org.pentaho.platform.api.engine;

import java.util.List;
import java.util.Map;

public interface ISelectionMapper {

  public String getDisplayStyle();

  public String getSelectionDisplayName();

  public String getSelectionNameForValue( String val );

  @SuppressWarnings( "rawtypes" )
  public List getSelectionValues();

  @SuppressWarnings( "rawtypes" )
  public Map getSelectionNameMap();

  public boolean hasValue( String value );

  public int selectionCount();

  public String getValueAt( int index );

  public String toString();

}
