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

package org.pentaho.platform.scheduler2.ws;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.scheduler2.messsages.Messages;

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
 * Copyright 2010 Pentaho Corporation.  All rights reserved.
 *
 */

@XmlRootElement
public class JaxBSafeMap {
  public List<JaxBSafeEntry> entry = new ArrayList<JaxBSafeEntry>();

  public JaxBSafeMap( Map<String, ParamValue> map ) {
    for ( Map.Entry<String, ParamValue> e : map.entrySet() ) {
      entry.add( new JaxBSafeEntry( e ) );
    }
  }

  public JaxBSafeMap() {
  }

  public static class JaxBSafeEntry {
    public String key;

    private StringParamValue stringValue;

    public boolean listValueIsEmptyList;
    private ListParamValue listValue;

    private MapParamValue mapValue;

    public JaxBSafeEntry() {
    }

    public JaxBSafeEntry( Map.Entry<String, ParamValue> e ) {
      key = e.getKey();
      ParamValue v = e.getValue();
      if ( v instanceof StringParamValue ) {
        stringValue = (StringParamValue) v;
      } else if ( v instanceof ListParamValue ) {
        listValue = (ListParamValue) v;
        /*
         * To overcome JAXB nulling out empty lists, we set a flag here so when we read back the value on the receiving
         * side we can properly construct an empty list.
         */
        if ( listValue.size() == 0 ) {
          listValueIsEmptyList = true;
        }
      } else if ( v instanceof MapParamValue ) {
        mapValue = (MapParamValue) v;
      } else {
        throw new UnsupportedOperationException( MessageFormat.format( Messages.getInstance().getErrorString(
            "JobParamsAdapter.ERROR_0001" ), v.getClass(), //$NON-NLS-1$
            this.getClass() ) );
      }
    }

    /*
     * See comments above regarding JAXBs faulty handling of empty lists
     */
    public ListParamValue getListValue() {
      if ( listValue == null && listValueIsEmptyList ) {
        return new ListParamValue();
      }
      if ( listValue != null ) {
        return listValue;
      }
      return listValue; // (which is null)
    }

    public void setListValue( ListParamValue v ) {
      listValue = v;
    }

    public StringParamValue getStringValue() {
      return stringValue;
    }

    public void setStringValue( StringParamValue v ) {
      stringValue = v;
    }

    public MapParamValue getMapValue() {
      return mapValue;
    }

    public void setMapValue( MapParamValue v ) {
      mapValue = v;
    }
  }
}
