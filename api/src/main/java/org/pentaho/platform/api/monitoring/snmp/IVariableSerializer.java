/*
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
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

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
