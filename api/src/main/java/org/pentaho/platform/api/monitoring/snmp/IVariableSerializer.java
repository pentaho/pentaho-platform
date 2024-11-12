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


package org.pentaho.platform.api.monitoring.snmp;

/**
 * Created by nbaker on 9/3/14.
 */
public interface IVariableSerializer {
  Integer serializeToInt( Object o );

  String serializeToString( Object o );

  public class BasicSerializer implements IVariableSerializer {
    @Override public Integer serializeToInt( Object o ) {
      if ( o == null ) {
        return -1;
      }
      if ( o instanceof Integer ) {
        return (Integer) o;
      }
      try {
        return Integer.parseInt( o.toString() );
      } catch ( NumberFormatException e ) {
        return 0;
      }

    }

    @Override public String serializeToString( Object o ) {
      return o != null ? o.toString() : "";
    }

  }
}
